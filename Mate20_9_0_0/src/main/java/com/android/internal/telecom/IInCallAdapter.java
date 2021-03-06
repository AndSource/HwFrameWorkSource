package com.android.internal.telecom;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.PhoneAccountHandle;
import java.util.List;

public interface IInCallAdapter extends IInterface {

    public static abstract class Stub extends Binder implements IInCallAdapter {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IInCallAdapter";
        static final int TRANSACTION_answerCall = 1;
        static final int TRANSACTION_conference = 14;
        static final int TRANSACTION_deflectCall = 2;
        static final int TRANSACTION_disconnectCall = 4;
        static final int TRANSACTION_handoverTo = 30;
        static final int TRANSACTION_holdCall = 5;
        static final int TRANSACTION_mergeConference = 16;
        static final int TRANSACTION_mute = 7;
        static final int TRANSACTION_phoneAccountSelected = 13;
        static final int TRANSACTION_playDtmfTone = 10;
        static final int TRANSACTION_postDialContinue = 12;
        static final int TRANSACTION_pullExternalCall = 20;
        static final int TRANSACTION_putExtras = 22;
        static final int TRANSACTION_rejectCall = 3;
        static final int TRANSACTION_removeExtras = 23;
        static final int TRANSACTION_respondToRttRequest = 27;
        static final int TRANSACTION_sendCallEvent = 21;
        static final int TRANSACTION_sendRttRequest = 26;
        static final int TRANSACTION_setAudioRoute = 8;
        static final int TRANSACTION_setBluetoothAudioRoute = 9;
        static final int TRANSACTION_setRttMode = 29;
        static final int TRANSACTION_splitFromConference = 15;
        static final int TRANSACTION_stopDtmfTone = 11;
        static final int TRANSACTION_stopRtt = 28;
        static final int TRANSACTION_swapConference = 17;
        static final int TRANSACTION_switchToOtherActiveSub = 24;
        static final int TRANSACTION_turnOffProximitySensor = 19;
        static final int TRANSACTION_turnOnProximitySensor = 18;
        static final int TRANSACTION_unholdCall = 6;
        static final int TRANSACTION_updateRcsPreCallInfo = 25;

        private static class Proxy implements IInCallAdapter {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void answerCall(String callId, int videoState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(videoState);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void deflectCall(String callId, Uri address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (address != null) {
                        _data.writeInt(1);
                        address.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void rejectCall(String callId, boolean rejectWithMessage, String textMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(rejectWithMessage);
                    _data.writeString(textMessage);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void disconnectCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void holdCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unholdCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void mute(boolean shouldMute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(shouldMute);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setAudioRoute(int route, String bluetoothAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(route);
                    _data.writeString(bluetoothAddress);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setBluetoothAudioRoute(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void playDtmfTone(String callId, char digit) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(digit);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopDtmfTone(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void postDialContinue(String callId, boolean proceed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(proceed);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void phoneAccountSelected(String callId, PhoneAccountHandle accountHandle, boolean setDefault) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(setDefault);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void conference(String callId, String otherCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(otherCallId);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void splitFromConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void mergeConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void swapConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void turnOnProximitySensor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void turnOffProximitySensor(boolean screenOnImmediately) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(screenOnImmediately);
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void pullExternalCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendCallEvent(String callId, String event, int targetSdkVer, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(event);
                    _data.writeInt(targetSdkVer);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void putExtras(String callId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void removeExtras(String callId, List<String> keys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeStringList(keys);
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void switchToOtherActiveSub(String sub, boolean retainLch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sub);
                    _data.writeInt(retainLch);
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateRcsPreCallInfo(String callId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendRttRequest(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(26, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void respondToRttRequest(String callId, int id, boolean accept) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(id);
                    _data.writeInt(accept);
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopRtt(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setRttMode(String callId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(mode);
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void handoverTo(String callId, PhoneAccountHandle destAcct, int videoState, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (destAcct != null) {
                        _data.writeInt(1);
                        destAcct.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(videoState);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(30, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInCallAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInCallAdapter)) {
                return new Proxy(obj);
            }
            return (IInCallAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = DESCRIPTOR;
            if (code != 1598968902) {
                boolean _arg0 = false;
                Uri _arg1 = null;
                String _arg02;
                String _arg03;
                String _arg04;
                int _arg2;
                Bundle _arg3;
                switch (code) {
                    case 1:
                        data.enforceInterface(descriptor);
                        answerCall(data.readString(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(descriptor);
                        _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (Uri) Uri.CREATOR.createFromParcel(data);
                        }
                        deflectCall(_arg02, _arg1);
                        return true;
                    case 3:
                        data.enforceInterface(descriptor);
                        _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        rejectCall(_arg03, _arg0, data.readString());
                        return true;
                    case 4:
                        data.enforceInterface(descriptor);
                        disconnectCall(data.readString());
                        return true;
                    case 5:
                        data.enforceInterface(descriptor);
                        holdCall(data.readString());
                        return true;
                    case 6:
                        data.enforceInterface(descriptor);
                        unholdCall(data.readString());
                        return true;
                    case 7:
                        data.enforceInterface(descriptor);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        mute(_arg0);
                        return true;
                    case 8:
                        data.enforceInterface(descriptor);
                        setAudioRoute(data.readInt(), data.readString());
                        return true;
                    case 9:
                        data.enforceInterface(descriptor);
                        setBluetoothAudioRoute(data.readString());
                        return true;
                    case 10:
                        data.enforceInterface(descriptor);
                        playDtmfTone(data.readString(), (char) data.readInt());
                        return true;
                    case 11:
                        data.enforceInterface(descriptor);
                        stopDtmfTone(data.readString());
                        return true;
                    case 12:
                        data.enforceInterface(descriptor);
                        _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        postDialContinue(_arg03, _arg0);
                        return true;
                    case 13:
                        PhoneAccountHandle _arg12;
                        data.enforceInterface(descriptor);
                        _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        phoneAccountSelected(_arg04, _arg12, _arg0);
                        return true;
                    case 14:
                        data.enforceInterface(descriptor);
                        conference(data.readString(), data.readString());
                        return true;
                    case 15:
                        data.enforceInterface(descriptor);
                        splitFromConference(data.readString());
                        return true;
                    case 16:
                        data.enforceInterface(descriptor);
                        mergeConference(data.readString());
                        return true;
                    case 17:
                        data.enforceInterface(descriptor);
                        swapConference(data.readString());
                        return true;
                    case 18:
                        data.enforceInterface(descriptor);
                        turnOnProximitySensor();
                        return true;
                    case 19:
                        data.enforceInterface(descriptor);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        turnOffProximitySensor(_arg0);
                        return true;
                    case 20:
                        data.enforceInterface(descriptor);
                        pullExternalCall(data.readString());
                        return true;
                    case 21:
                        data.enforceInterface(descriptor);
                        _arg02 = data.readString();
                        _arg04 = data.readString();
                        _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        sendCallEvent(_arg02, _arg04, _arg2, _arg3);
                        return true;
                    case 22:
                        data.enforceInterface(descriptor);
                        _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        putExtras(_arg02, _arg3);
                        return true;
                    case 23:
                        data.enforceInterface(descriptor);
                        removeExtras(data.readString(), data.createStringArrayList());
                        return true;
                    case 24:
                        data.enforceInterface(descriptor);
                        _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        switchToOtherActiveSub(_arg03, _arg0);
                        return true;
                    case 25:
                        data.enforceInterface(descriptor);
                        _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        updateRcsPreCallInfo(_arg02, _arg3);
                        return true;
                    case 26:
                        data.enforceInterface(descriptor);
                        sendRttRequest(data.readString());
                        return true;
                    case 27:
                        data.enforceInterface(descriptor);
                        _arg03 = data.readString();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        respondToRttRequest(_arg03, _arg13, _arg0);
                        return true;
                    case 28:
                        data.enforceInterface(descriptor);
                        stopRtt(data.readString());
                        return true;
                    case 29:
                        data.enforceInterface(descriptor);
                        setRttMode(data.readString(), data.readInt());
                        return true;
                    case 30:
                        PhoneAccountHandle _arg14;
                        data.enforceInterface(descriptor);
                        _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        handoverTo(_arg02, _arg14, _arg2, _arg3);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
            reply.writeString(descriptor);
            return true;
        }
    }

    void answerCall(String str, int i) throws RemoteException;

    void conference(String str, String str2) throws RemoteException;

    void deflectCall(String str, Uri uri) throws RemoteException;

    void disconnectCall(String str) throws RemoteException;

    void handoverTo(String str, PhoneAccountHandle phoneAccountHandle, int i, Bundle bundle) throws RemoteException;

    void holdCall(String str) throws RemoteException;

    void mergeConference(String str) throws RemoteException;

    void mute(boolean z) throws RemoteException;

    void phoneAccountSelected(String str, PhoneAccountHandle phoneAccountHandle, boolean z) throws RemoteException;

    void playDtmfTone(String str, char c) throws RemoteException;

    void postDialContinue(String str, boolean z) throws RemoteException;

    void pullExternalCall(String str) throws RemoteException;

    void putExtras(String str, Bundle bundle) throws RemoteException;

    void rejectCall(String str, boolean z, String str2) throws RemoteException;

    void removeExtras(String str, List<String> list) throws RemoteException;

    void respondToRttRequest(String str, int i, boolean z) throws RemoteException;

    void sendCallEvent(String str, String str2, int i, Bundle bundle) throws RemoteException;

    void sendRttRequest(String str) throws RemoteException;

    void setAudioRoute(int i, String str) throws RemoteException;

    void setBluetoothAudioRoute(String str) throws RemoteException;

    void setRttMode(String str, int i) throws RemoteException;

    void splitFromConference(String str) throws RemoteException;

    void stopDtmfTone(String str) throws RemoteException;

    void stopRtt(String str) throws RemoteException;

    void swapConference(String str) throws RemoteException;

    void switchToOtherActiveSub(String str, boolean z) throws RemoteException;

    void turnOffProximitySensor(boolean z) throws RemoteException;

    void turnOnProximitySensor() throws RemoteException;

    void unholdCall(String str) throws RemoteException;

    void updateRcsPreCallInfo(String str, Bundle bundle) throws RemoteException;
}
