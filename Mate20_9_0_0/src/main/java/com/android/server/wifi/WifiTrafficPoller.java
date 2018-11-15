package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.HwLog;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiTrafficPoller {
    private static final int ADD_CLIENT = 3;
    private static final boolean DBG = false;
    private static final int ENABLE_TRAFFIC_STATS_POLL = 1;
    private static final int POLL_TRAFFIC_STATS_INTERVAL_MSECS = 1000;
    private static final int REMOVE_CLIENT = 4;
    private static final String TAG = "WifiTrafficPoller";
    private static final int TRAFFIC_STATS_POLL = 2;
    private final List<Messenger> mClients = new ArrayList();
    private int mDataActivity;
    private boolean mEnableTrafficStatsPoll = false;
    private NetworkInfo mNetworkInfo;
    private long mRxPkts;
    private AtomicBoolean mScreenOn = new AtomicBoolean(true);
    private final TrafficHandler mTrafficHandler;
    private int mTrafficStatsPollToken = 0;
    private long mTxPkts;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiNative mWifiNative;

    private class TrafficHandler extends Handler {
        public TrafficHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            String str;
            switch (msg.what) {
                case 1:
                    WifiTrafficPoller wifiTrafficPoller = WifiTrafficPoller.this;
                    boolean z = true;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    wifiTrafficPoller.mEnableTrafficStatsPoll = z;
                    if (WifiTrafficPoller.this.mVerboseLoggingEnabled) {
                        str = WifiTrafficPoller.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("ENABLE_TRAFFIC_STATS_POLL ");
                        stringBuilder.append(WifiTrafficPoller.this.mEnableTrafficStatsPoll);
                        stringBuilder.append(" Token ");
                        stringBuilder.append(Integer.toString(WifiTrafficPoller.this.mTrafficStatsPollToken));
                        Log.d(str, stringBuilder.toString());
                    }
                    WifiTrafficPoller.this.mTrafficStatsPollToken = WifiTrafficPoller.this.mTrafficStatsPollToken + 1;
                    str = WifiTrafficPoller.this.mWifiNative.getClientInterfaceName();
                    if (WifiTrafficPoller.this.mEnableTrafficStatsPoll && !TextUtils.isEmpty(str)) {
                        WifiTrafficPoller.this.notifyOnDataActivity(str);
                        sendMessageDelayed(Message.obtain(this, 2, WifiTrafficPoller.this.mTrafficStatsPollToken, 0), 1000);
                        return;
                    }
                    return;
                case 2:
                    if (msg.arg1 == WifiTrafficPoller.this.mTrafficStatsPollToken) {
                        str = WifiTrafficPoller.this.mWifiNative.getClientInterfaceName();
                        if (!TextUtils.isEmpty(str)) {
                            WifiTrafficPoller.this.notifyOnDataActivity(str);
                            sendMessageDelayed(Message.obtain(this, 2, WifiTrafficPoller.this.mTrafficStatsPollToken, 0), 1000);
                            return;
                        }
                        return;
                    }
                    return;
                case 3:
                    WifiTrafficPoller.this.mClients.add((Messenger) msg.obj);
                    if (WifiTrafficPoller.this.mVerboseLoggingEnabled) {
                        str = WifiTrafficPoller.TAG;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("ADD_CLIENT: ");
                        stringBuilder2.append(Integer.toString(WifiTrafficPoller.this.mClients.size()));
                        Log.d(str, stringBuilder2.toString());
                        return;
                    }
                    return;
                case 4:
                    WifiTrafficPoller.this.mClients.remove(msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    WifiTrafficPoller(Context context, Looper looper, WifiNative wifiNative) {
        this.mTrafficHandler = new TrafficHandler(looper);
        this.mWifiNative = wifiNative;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                        WifiTrafficPoller.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                        WifiTrafficPoller.this.mScreenOn.set(false);
                    } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                        WifiTrafficPoller.this.mScreenOn.set(true);
                    }
                    WifiTrafficPoller.this.evaluateTrafficStatsPolling();
                }
            }
        }, filter);
    }

    public void addClient(Messenger client) {
        Message.obtain(this.mTrafficHandler, 3, client).sendToTarget();
    }

    public void removeClient(Messenger client) {
        Message.obtain(this.mTrafficHandler, 4, client).sendToTarget();
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
    }

    private void evaluateTrafficStatsPolling() {
        if (this.mNetworkInfo != null) {
            Message msg;
            if (this.mNetworkInfo.getDetailedState() == DetailedState.CONNECTED && this.mScreenOn.get()) {
                msg = Message.obtain(this.mTrafficHandler, 1, 1, 0);
            } else {
                msg = Message.obtain(this.mTrafficHandler, 1, 0, 0);
            }
            msg.sendToTarget();
        }
    }

    private void notifyOnDataActivity(String ifaceName) {
        long preTxPkts = this.mTxPkts;
        long preRxPkts = this.mRxPkts;
        int dataActivity = 0;
        this.mTxPkts = this.mWifiNative.getTxPackets(ifaceName);
        this.mRxPkts = this.mWifiNative.getRxPackets(ifaceName);
        if (preTxPkts > 0 || preRxPkts > 0) {
            StringBuilder stringBuilder;
            long received = this.mRxPkts - preRxPkts;
            if (this.mTxPkts - preTxPkts > 0) {
                dataActivity = 0 | 2;
            }
            if (received > 0) {
                dataActivity |= 1;
            }
            if (dataActivity != this.mDataActivity) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("activity=");
                stringBuilder.append(dataActivity);
                HwLog.dubaie("DUBAI_TAG_WIFI_ACTIVITY", stringBuilder.toString());
            }
            if (dataActivity != this.mDataActivity && this.mScreenOn.get()) {
                this.mDataActivity = dataActivity;
                if (this.mVerboseLoggingEnabled) {
                    String str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("notifying of data activity ");
                    stringBuilder.append(Integer.toString(this.mDataActivity));
                    Log.e(str, stringBuilder.toString());
                }
                for (Messenger client : this.mClients) {
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.arg1 = this.mDataActivity;
                    try {
                        client.send(msg);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mEnableTrafficStatsPoll ");
        stringBuilder.append(this.mEnableTrafficStatsPoll);
        pw.println(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("mTrafficStatsPollToken ");
        stringBuilder.append(this.mTrafficStatsPollToken);
        pw.println(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("mTxPkts ");
        stringBuilder.append(this.mTxPkts);
        pw.println(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("mRxPkts ");
        stringBuilder.append(this.mRxPkts);
        pw.println(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("mDataActivity ");
        stringBuilder.append(this.mDataActivity);
        pw.println(stringBuilder.toString());
    }
}
