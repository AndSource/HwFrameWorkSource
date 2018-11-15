package com.android.server;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.util.Log;
import android.util.Slog;
import com.android.server.input.InputManagerService;
import com.android.server.input.InputManagerService.WiredAccessoryCallbacks;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class WiredAccessoryManager implements WiredAccessoryCallbacks {
    private static final int BIT_HDMI_AUDIO = 16;
    private static final int BIT_HEADSET = 1;
    private static final int BIT_HEADSET_NO_MIC = 2;
    private static final int BIT_LINEOUT = 32;
    private static final int BIT_USB_HEADSET_ANLG = 4;
    private static final int BIT_USB_HEADSET_DGTL = 8;
    private static final int HEADSET_REVERT_SEQUENCE = 3;
    private static final boolean LOG = true;
    private static final int MSG_NEW_DEVICE_STATE = 1;
    private static final int MSG_SYSTEM_READY = 2;
    private static final String NAME_H2W = "h2w";
    private static final String NAME_HDMI = "hdmi";
    private static final String NAME_HDMI_AUDIO = "hdmi_audio";
    private static final String NAME_USB_AUDIO = "usb_audio";
    private static final int SUPPORTED_HEADSETS = 63;
    private static final String TAG = WiredAccessoryManager.class.getSimpleName();
    private final AudioManager mAudioManager;
    private final Handler mHandler = new Handler(Looper.myLooper(), null, true) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    WiredAccessoryManager.this.setDevicesState(msg.arg1, msg.arg2, (String) msg.obj);
                    WiredAccessoryManager.this.mWakeLock.release();
                    return;
                case 2:
                    WiredAccessoryManager.this.onSystemReady();
                    WiredAccessoryManager.this.mWakeLock.release();
                    return;
                default:
                    return;
            }
        }
    };
    private int mHeadsetState;
    private final InputManagerService mInputManager;
    private final Object mLock = new Object();
    private final WiredAccessoryObserver mObserver;
    private int mSwitchValues;
    private final boolean mUseDevInputEventForAudioJack;
    private final WakeLock mWakeLock;

    class WiredAccessoryObserver extends UEventObserver {
        private final List<UEventInfo> mUEventInfo = makeObservedUEventList();

        private final class UEventInfo {
            private final String mDevName;
            private final int mState1Bits;
            private final int mState2Bits;
            private final int mStateNbits;

            public UEventInfo(String devName, int state1Bits, int state2Bits, int stateNbits) {
                this.mDevName = devName;
                this.mState1Bits = state1Bits;
                this.mState2Bits = state2Bits;
                this.mStateNbits = stateNbits;
            }

            public String getDevName() {
                return this.mDevName;
            }

            public String getDevPath() {
                return String.format(Locale.US, "/devices/virtual/switch/%s", new Object[]{this.mDevName});
            }

            public String getSwitchStatePath() {
                return String.format(Locale.US, "/sys/class/switch/%s/state", new Object[]{this.mDevName});
            }

            public boolean checkSwitchExists() {
                return new File(getSwitchStatePath()).exists();
            }

            public int computeNewHeadsetState(int headsetState, int switchState) {
                int preserveMask = ~((this.mState1Bits | this.mState2Bits) | this.mStateNbits);
                int setBits = 0;
                int i = 1;
                if (switchState == 1) {
                    setBits = this.mState1Bits;
                } else {
                    int i2 = switchState == 2 ? 1 : 0;
                    if (switchState != 3) {
                        i = 0;
                    }
                    if ((i | i2) != 0) {
                        setBits = this.mState2Bits;
                    } else if (switchState == this.mStateNbits) {
                        setBits = this.mStateNbits;
                    }
                }
                return (headsetState & preserveMask) | setBits;
            }
        }

        private IBinder getAudioService() {
            return ServiceManager.getService("audio");
        }

        private void setHeadsetRevertSequenceState(boolean isRevertSeq) {
            String access$300 = WiredAccessoryManager.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("setHeadsetRevertSequenceState isRevertSeq: ");
            stringBuilder.append(isRevertSeq);
            Slog.v(access$300, stringBuilder.toString());
            IBinder b = getAudioService();
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            if (isRevertSeq) {
                data.writeInt(1);
            } else {
                data.writeInt(0);
            }
            try {
                b.transact(102, data, reply, 0);
            } catch (RemoteException e) {
                String access$3002 = WiredAccessoryManager.TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("setHeadsetRevertSequenceState transact e: ");
                stringBuilder2.append(e);
                Slog.e(access$3002, stringBuilder2.toString());
            }
        }

        void init() {
            int i;
            synchronized (WiredAccessoryManager.this.mLock) {
                Slog.v(WiredAccessoryManager.TAG, "init()");
                char[] buffer = new char[1024];
                i = 0;
                for (int i2 = 0; i2 < this.mUEventInfo.size(); i2++) {
                    UEventInfo uei = (UEventInfo) this.mUEventInfo.get(i2);
                    try {
                        FileReader file = new FileReader(uei.getSwitchStatePath());
                        int len = file.read(buffer, 0, 1024);
                        file.close();
                        int curState = Integer.parseInt(new String(buffer, 0, len).trim());
                        if (curState > 0) {
                            updateStateLocked(uei.getDevPath(), uei.getDevName(), curState);
                        }
                    } catch (FileNotFoundException e) {
                        String access$300 = WiredAccessoryManager.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(uei.getSwitchStatePath());
                        stringBuilder.append(" not found while attempting to determine initial switch state");
                        Slog.w(access$300, stringBuilder.toString());
                    } catch (Exception e2) {
                        Slog.e(WiredAccessoryManager.TAG, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, e2);
                    }
                }
            }
            while (true) {
                int i3 = i;
                if (i3 < this.mUEventInfo.size()) {
                    UEventInfo uei2 = (UEventInfo) this.mUEventInfo.get(i3);
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("DEVPATH=");
                    stringBuilder2.append(uei2.getDevPath());
                    startObserving(stringBuilder2.toString());
                    i = i3 + 1;
                } else {
                    return;
                }
            }
        }

        private List<UEventInfo> makeObservedUEventList() {
            List<UEventInfo> retVal = new ArrayList();
            if (!WiredAccessoryManager.this.mUseDevInputEventForAudioJack) {
                UEventInfo uEventInfo = new UEventInfo(WiredAccessoryManager.NAME_H2W, 1, 2, 32);
                if (uEventInfo.checkSwitchExists()) {
                    retVal.add(uEventInfo);
                } else {
                    Slog.w(WiredAccessoryManager.TAG, "This kernel does not have wired headset support");
                }
            }
            UEventInfo uEventInfo2 = new UEventInfo(WiredAccessoryManager.NAME_USB_AUDIO, 4, 8, 0);
            if (uEventInfo2.checkSwitchExists()) {
                retVal.add(uEventInfo2);
            } else {
                Slog.w(WiredAccessoryManager.TAG, "This kernel does not have usb audio support");
            }
            UEventInfo uei = new UEventInfo(WiredAccessoryManager.NAME_HDMI_AUDIO, 16, 0, 0);
            if (uei.checkSwitchExists()) {
                retVal.add(uei);
            } else {
                uei = new UEventInfo(WiredAccessoryManager.NAME_HDMI, 16, 0, 0);
                if (uei.checkSwitchExists()) {
                    retVal.add(uei);
                } else {
                    Slog.w(WiredAccessoryManager.TAG, "This kernel does not have HDMI audio support");
                }
            }
            return retVal;
        }

        public void onUEvent(UEvent event) {
            String access$300 = WiredAccessoryManager.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Headset UEVENT: ");
            stringBuilder.append(event.toString());
            Slog.v(access$300, stringBuilder.toString());
            String name;
            try {
                access$300 = event.get("DEVPATH");
                name = event.get("SWITCH_NAME");
                int state = Integer.parseInt(event.get("SWITCH_STATE"));
                synchronized (WiredAccessoryManager.this.mLock) {
                    updateStateLocked(access$300, name, state);
                }
            } catch (NumberFormatException e) {
                name = WiredAccessoryManager.TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Could not parse switch state from event ");
                stringBuilder2.append(event);
                Slog.e(name, stringBuilder2.toString());
            }
        }

        private void updateStateLocked(String devPath, String name, int state) {
            int i = 0;
            if (WiredAccessoryManager.NAME_H2W.equals(name)) {
                if (state == 3) {
                    setHeadsetRevertSequenceState(true);
                } else {
                    setHeadsetRevertSequenceState(false);
                }
            }
            while (true) {
                int i2 = i;
                if (i2 < this.mUEventInfo.size()) {
                    UEventInfo uei = (UEventInfo) this.mUEventInfo.get(i2);
                    if (devPath.equals(uei.getDevPath())) {
                        WiredAccessoryManager.this.updateLocked(name, uei.computeNewHeadsetState(WiredAccessoryManager.this.mHeadsetState, state));
                        return;
                    }
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
    }

    public WiredAccessoryManager(Context context, InputManagerService inputManager) {
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "WiredAccessoryManager");
        this.mWakeLock.setReferenceCounted(false);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mInputManager = inputManager;
        this.mUseDevInputEventForAudioJack = context.getResources().getBoolean(17957058);
        this.mObserver = new WiredAccessoryObserver();
    }

    private void onSystemReady() {
        if (this.mUseDevInputEventForAudioJack) {
            int switchValues = 0;
            if (this.mInputManager.getSwitchState(-1, -256, 2) == 1) {
                switchValues = 0 | 4;
            }
            if (this.mInputManager.getSwitchState(-1, -256, 4) == 1) {
                switchValues |= 16;
            }
            if (this.mInputManager.getSwitchState(-1, -256, 6) == 1) {
                switchValues |= 64;
            }
            notifyWiredAccessoryChanged(0, switchValues, 84);
        }
        this.mObserver.init();
    }

    public void notifyWiredAccessoryChanged(long whenNanos, int switchValues, int switchMask) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("notifyWiredAccessoryChanged: when=");
        stringBuilder.append(whenNanos);
        stringBuilder.append(" bits=");
        stringBuilder.append(switchCodeToString(switchValues, switchMask));
        stringBuilder.append(" mask=");
        stringBuilder.append(Integer.toHexString(switchMask));
        Slog.v(str, stringBuilder.toString());
        synchronized (this.mLock) {
            this.mSwitchValues = (this.mSwitchValues & (~switchMask)) | switchValues;
            int i = this.mSwitchValues & 84;
            if (i == 0) {
                i = 0;
            } else if (i == 4) {
                i = 2;
            } else if (i == 16) {
                i = 1;
            } else if (i == 20) {
                i = 1;
            } else if (i != 64) {
                i = 0;
            } else {
                i = 32;
            }
            updateLocked(NAME_H2W, (this.mHeadsetState & -36) | i);
        }
    }

    public void systemReady() {
        synchronized (this.mLock) {
            this.mWakeLock.acquire();
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, 0, 0, null));
        }
    }

    private void updateLocked(String newName, int newState) {
        int headsetState = newState & SUPPORTED_HEADSETS;
        int usb_headset_anlg = headsetState & 4;
        int usb_headset_dgtl = headsetState & 8;
        int h2w_headset = headsetState & 35;
        boolean h2wStateChange = true;
        boolean usbStateChange = true;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("newName=");
        stringBuilder.append(newName);
        stringBuilder.append(" newState=");
        stringBuilder.append(newState);
        stringBuilder.append(" headsetState=");
        stringBuilder.append(headsetState);
        stringBuilder.append(" prev headsetState=");
        stringBuilder.append(this.mHeadsetState);
        Slog.v(str, stringBuilder.toString());
        if (this.mHeadsetState == headsetState) {
            Log.e(TAG, "No state change.");
            return;
        }
        if (h2w_headset == 35) {
            Log.e(TAG, "Invalid combination, unsetting h2w flag");
            h2wStateChange = false;
        }
        if (usb_headset_anlg == 4 && usb_headset_dgtl == 8) {
            Log.e(TAG, "Invalid combination, unsetting usb flag");
            usbStateChange = false;
        }
        if (h2wStateChange || usbStateChange) {
            this.mWakeLock.acquire();
            Log.i(TAG, "MSG_NEW_DEVICE_STATE");
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, headsetState, this.mHeadsetState, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
            this.mHeadsetState = headsetState;
            return;
        }
        Log.e(TAG, "invalid transition, returning ...");
    }

    private void setDevicesState(int headsetState, int prevHeadsetState, String headsetName) {
        synchronized (this.mLock) {
            int curHeadset = 1;
            int allHeadsets = SUPPORTED_HEADSETS;
            int curHeadset2 = 1;
            while (curHeadset2 < SUPPORTED_HEADSETS) {
                if ((headsetState & curHeadset2) == 0 && (prevHeadsetState & curHeadset2) == curHeadset2 && (curHeadset2 & allHeadsets) != 0) {
                    setDeviceStateLocked(curHeadset2, headsetState, prevHeadsetState, headsetName);
                    allHeadsets &= ~curHeadset2;
                }
                curHeadset2 <<= 1;
            }
            while (true) {
                curHeadset2 = curHeadset;
                if (curHeadset2 < SUPPORTED_HEADSETS) {
                    if ((headsetState & curHeadset2) == curHeadset2 && (prevHeadsetState & curHeadset2) == 0 && (curHeadset2 & allHeadsets) != 0) {
                        setDeviceStateLocked(curHeadset2, headsetState, prevHeadsetState, headsetName);
                        allHeadsets = (~curHeadset2) & allHeadsets;
                    }
                    curHeadset = curHeadset2 << 1;
                }
            }
        }
    }

    private void setDeviceStateLocked(int headset, int headsetState, int prevHeadsetState, String headsetName) {
        if ((headsetState & headset) != (prevHeadsetState & headset)) {
            int state;
            int inDevice = 0;
            if ((headsetState & headset) != 0) {
                state = 1;
            } else {
                state = 0;
            }
            if (1 != headsetState || 2 != headset) {
                if (2 != headsetState || 1 != headset) {
                    int outDevice;
                    String str;
                    if (headset == 1) {
                        outDevice = 4;
                        inDevice = -2147483632;
                    } else if (headset == 2) {
                        outDevice = 8;
                    } else if (headset == 32) {
                        outDevice = 131072;
                    } else if (headset == 4) {
                        outDevice = 2048;
                    } else if (headset == 8) {
                        outDevice = 4096;
                    } else if (headset == 16) {
                        outDevice = 1024;
                    } else {
                        str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("setDeviceState() invalid headset type: ");
                        stringBuilder.append(headset);
                        Slog.e(str, stringBuilder.toString());
                        return;
                    }
                    str = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("headsetName: ");
                    stringBuilder2.append(headsetName);
                    stringBuilder2.append(state == 1 ? " connected" : " disconnected");
                    Slog.v(str, stringBuilder2.toString());
                    if (inDevice != 0) {
                        this.mAudioManager.setWiredDeviceConnectionState(inDevice, state, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, headsetName);
                    }
                    if (outDevice != 0) {
                        this.mAudioManager.setWiredDeviceConnectionState(outDevice, state, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, headsetName);
                    }
                }
            }
        }
    }

    private String switchCodeToString(int switchValues, int switchMask) {
        StringBuffer sb = new StringBuffer();
        if (!((switchMask & 4) == 0 || (switchValues & 4) == 0)) {
            sb.append("SW_HEADPHONE_INSERT ");
        }
        if (!((switchMask & 16) == 0 || (switchValues & 16) == 0)) {
            sb.append("SW_MICROPHONE_INSERT");
        }
        return sb.toString();
    }
}
