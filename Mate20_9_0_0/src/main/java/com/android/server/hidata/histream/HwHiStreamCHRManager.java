package com.android.server.hidata.histream;

import android.content.Context;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.arbitration.IHiDataCHRCallBack;
import com.android.server.hidata.channelqoe.HwChannelQoEManager;
import com.android.server.hidata.channelqoe.HwChannelQoEManager.CurrentSignalState;
import com.android.server.hidata.channelqoe.HwChannelQoEManager.HistoryMseasureInfo;
import com.android.server.hidata.channelqoe.IChannelQoECallback;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.histream.HwHistreamCHRStatisticsInfo.AppTraffic;
import com.android.server.hidata.wavemapping.chr.QueryHistAppQoeService;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class HwHiStreamCHRManager implements IChannelQoECallback {
    private static final int CHR_COLLECT_QOE_AFTER_HANDOVER = 15000;
    private static final int CHR_COLLECT_QOE_PERIOD = 6000;
    private static final int CHR_COLLECT_TUP_PERIOD = 3;
    private static final int CHR_DS_QOE_REPORT_MIN_INTERVAL = 30000;
    private static final int CHR_GET_CUR_RTT_DELAY = 6000;
    private static final int CHR_HANDOVER_EVENT_GET_TUP_DELAY = 8000;
    private static final int CHR_HANDOVER_EVENT_UPLOAD_DELAY = 22000;
    public static final int CHR_MPLINK_FAIL_BIND_FAIL = 8;
    public static final int CHR_MPLINK_FAIL_COEXISTENCE = 2;
    public static final int CHR_MPLINK_FAIL_ENVIRONMENT = 1;
    public static final int CHR_MPLINK_FAIL_HISTRORYQOE = 4;
    public static final int CHR_MPLINK_FAIL_ISBINDING = 6;
    public static final int CHR_MPLINK_FAIL_OTHERS = 20;
    public static final int CHR_MPLINK_FAIL_PINGPONG = 3;
    public static final int CHR_MPLINK_FAIL_QUERY_TIMEOUT = 7;
    public static final int CHR_MPLINK_FAIL_TARGETNETWORK = 5;
    public static final int CHR_MPLINK_FAIL_UNBIND_FAIL = 9;
    public static final int CHR_MPLINK_SUCCESS = 0;
    private static final int CHR_START_COLLECT_PARA_DELAY = 30000;
    private static final int CHR_TRAFFIC_UPDATE_PERIOD = 2;
    private static final int CHR_UPLOAD_COLLECT_PARAMETER_MAX_COUNT = 12;
    private static final int CHR_UPLOAD_EVENT_APPQOE_HISTORY_RECORDS = 909009048;
    private static final int CHR_UPLOAD_EVENT_DOUYIN_STATISTICS = 909002053;
    private static final int CHR_UPLOAD_EVENT_HANDOVER = 909002039;
    private static final int CHR_UPLOAD_EVENT_QOE_PARA_COLLECT = 909002042;
    private static final int CHR_UPLOAD_EVENT_SPACE_INFO = 909009047;
    private static final int CHR_UPLOAD_EVENT_STALL_INFO = 909009035;
    private static final int CHR_UPLOAD_EVENT_WECHAT_AUDIO_STATISTICS = 909002041;
    private static final int CHR_UPLOAD_EVENT_WECHAT_VIDEO_STATISTICS = 909002040;
    private static final int CHR_UPLOAD_HANOVER_EVNET_MAX_COUNT = 20;
    private static final int CHR_UPLOAD_STALL_EVNET_MAX_COUNT = 20;
    public static final int EVENT_CHR_APPEND_MPLINK_TO_WIFI = 6;
    public static final int EVENT_CHR_ERROR_STOP_MPLINK = 7;
    public static final int EVENT_CHR_HANDOVER_TO_CELLULAR = 2;
    public static final int EVENT_CHR_HANDOVER_TO_WIFI = 1;
    public static final int EVENT_CHR_STALL_BEGIN_MPLINK = 8;
    public static final int EVENT_CHR_STALL_MPLINK_TO_CELLULAR = 5;
    public static final int EVENT_CHR_STALL_MPLINK_TO_WIFI = 3;
    public static final int EVENT_CHR_USER_STOP_MPLINK = 9;
    public static final int EVENT_CHR_WIFI_RECOVER_MPLINK_TO_WIFI = 4;
    private static final int USER_MANUALLY_SWITCH_INTERVAL = 3000;
    private static final int USER_MANUALLY_WIFI_ENABLE_INTERVAL = 6000;
    private static HwHiStreamCHRManager mHwHiStreamCHRManager;
    private Handler mCHRManagerHandler;
    private int mCallId = 0;
    private Context mContext;
    private int mCurNetRtt = -1;
    private long mDsQoeLastReportTime = 0;
    private int mHandoverEventId = 0;
    private ArrayList<HwHistreamCHRHandoverInfo> mHandoverInfolist = new ArrayList();
    private IHiDataCHRCallBack mIHiDataCHRCallBack;
    private long mLastCollectCellularRttTime = 0;
    private long mLastGetCurRttTime = 0;
    private HistreamWifiInfo mLastWifiInfo = new HistreamWifiInfo();
    private HwHistreamCHRStatisticsInfo mOtherAppStatisticsInfo;
    private int mStallEventId = 0;
    private ArrayList<HwHistreamCHRStallInfo> mStallInfoList = new ArrayList();
    private int mUploadCollectParaCount = 0;
    private long mUploadCollectParaStartCountTime = 0;
    private int mUploadHandoverCount = 0;
    private long mUploadHandoverStartCountTime = 0;
    private int mUploadStallCount = 0;
    private long mUploadStallStartCountTime = 0;
    private HwHistreamCHRStatisticsInfo mWechatStatisticsInfo;
    private WifiManager mWifiManager;

    public static class HistreamWifiInfo {
        int mApType = -1;
        int mChLoad = -1;
        int mChannel = -1;
        int mFailRate = -1;
        int mLastTxBad = 0;
        int mLastTxGood = 0;
        int mRssi = -1;
        int mSnr = -1;
        String mSsid = HwAPPQoEUtils.INVALID_STRING_VALUE;
    }

    private HwHiStreamCHRManager(Context context, Handler handler) {
        this.mContext = context;
        this.mCHRManagerHandler = handler;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public static HwHiStreamCHRManager createInstance(Context context, Handler handler) {
        if (mHwHiStreamCHRManager == null) {
            mHwHiStreamCHRManager = new HwHiStreamCHRManager(context, handler);
        }
        return mHwHiStreamCHRManager;
    }

    public static HwHiStreamCHRManager getInstance() {
        return mHwHiStreamCHRManager;
    }

    public synchronized void registCHRCallback(IHiDataCHRCallBack callback) {
        this.mIHiDataCHRCallBack = callback;
    }

    public void handleUploadCollectPara() {
        HwHistreamCHRStatisticsInfo statisticsInfo = getCurStatisticsInfo();
        HwChannelQoEManager mHwChannelQoEManager = HwChannelQoEManager.getInstance();
        long curTime = System.currentTimeMillis();
        if (statisticsInfo != null && mHwChannelQoEManager != null && HwArbitrationDEFS.DelayTimeMillisA < curTime - statisticsInfo.mCallStartTime) {
            HwHistreamCHRMachineInfo machineInfo = new HwHistreamCHRMachineInfo();
            machineInfo.mApkName = statisticsInfo.mApkName;
            machineInfo.mScenario = statisticsInfo.mScenario;
            machineInfo.mRAT = statisticsInfo.mCurrNetwork;
            if (HwAPPQoEUtils.SCENE_VIDEO == statisticsInfo.mScenario) {
                HwHidataJniAdapter mHwHidataJniAdapter = HwHidataJniAdapter.getInstance();
                if (mHwHidataJniAdapter != null) {
                    machineInfo.mWechatVideoQoe = mHwHidataJniAdapter.getCurFrameDetectResult();
                }
            } else if (6000 >= curTime - statisticsInfo.mLastBadQoeTime) {
                machineInfo.mStreamQoe = statisticsInfo.mLastQoe;
            } else {
                machineInfo.mStreamQoe = 106;
            }
            CurrentSignalState signalInfo = mHwChannelQoEManager.getCurrentSignalState(statisticsInfo.mCurrNetwork, false, this);
            if (800 == statisticsInfo.mCurrNetwork) {
                updateTup(statisticsInfo, 800);
                machineInfo.mRxTup1Bef = statisticsInfo.mWifiRxTup1;
                machineInfo.mRxTup2Bef = statisticsInfo.mWifiRxTup2;
                WifiInfo info = this.mWifiManager.getConnectionInfo();
                if (!(info == null || -1 == info.getFrequency())) {
                    machineInfo.mTxFail1Bef = (int) info.txBadRate;
                }
                if (signalInfo != null) {
                    machineInfo.mWifiRssi = signalInfo.getSigPwr();
                    machineInfo.mWifiSnr = signalInfo.getSigSnr();
                    machineInfo.mChLoad = signalInfo.getSigLoad();
                }
            } else if (801 == statisticsInfo.mCurrNetwork) {
                updateTup(statisticsInfo, 801);
                machineInfo.mRxTup1Bef = statisticsInfo.mCelluarRxTup1;
                machineInfo.mRxTup2Bef = statisticsInfo.mCelluarRxTup2;
                if (signalInfo != null) {
                    machineInfo.mCellSig = signalInfo.getSigPwr();
                    machineInfo.mCellQuality = signalInfo.getSigQual();
                    machineInfo.mCellSinr = signalInfo.getSigSnr();
                }
            }
            if (6000 > curTime - this.mLastGetCurRttTime) {
                machineInfo.mNetRtt = this.mCurNetRtt;
            }
            machineInfo.printCHRMachineInfo();
            sendCHRCollectParaEvent(machineInfo);
        }
    }

    public void handleStartCollectPara() {
        long curTime = System.currentTimeMillis();
        if (86400000 < curTime - this.mUploadCollectParaStartCountTime) {
            this.mUploadCollectParaCount = 0;
            this.mUploadCollectParaStartCountTime = curTime;
        }
        HwHistreamCHRStatisticsInfo statisticsInfo = getCurStatisticsInfo();
        if (statisticsInfo != null && 12 > this.mUploadCollectParaCount) {
            boolean isNeedQueryRtt = true;
            if (801 == statisticsInfo.mCurrNetwork) {
                if (86400000 > curTime - this.mLastCollectCellularRttTime) {
                    isNeedQueryRtt = false;
                } else {
                    this.mLastCollectCellularRttTime = curTime;
                }
            }
            HwChannelQoEManager mHwChannelQoEManager = HwChannelQoEManager.getInstance();
            if (true == isNeedQueryRtt && mHwChannelQoEManager != null) {
                mHwChannelQoEManager.getCurrentSignalState(statisticsInfo.mCurrNetwork, true, this);
            }
            this.mCHRManagerHandler.sendEmptyMessageDelayed(8, 6000);
        }
    }

    public void handleUploadStallEventDelay(Message msg) {
        if (msg != null && msg.obj != null) {
            int eventNum = msg.obj.getInt("eventNum");
            HwHistreamCHRStallInfo curStallEvent = null;
            int pos = 0;
            int size = this.mStallInfoList.size();
            int i = 0;
            while (i < size) {
                HwHistreamCHRStallInfo stallEvent = (HwHistreamCHRStallInfo) this.mStallInfoList.get(i);
                if (stallEvent != null) {
                    if (eventNum == stallEvent.mEventId) {
                        curStallEvent = stallEvent;
                        pos = i;
                        break;
                    }
                    i++;
                } else {
                    return;
                }
            }
            if (curStallEvent != null) {
                if (6000 > System.currentTimeMillis() - this.mLastGetCurRttTime) {
                    curStallEvent.mNetRtt = this.mCurNetRtt;
                }
                Bundle chrBundle = new Bundle();
                chrBundle.putString("mAPKName", curStallEvent.mAPKName);
                chrBundle.putInt("mScenario", curStallEvent.mScenario);
                chrBundle.putInt("mRAT", curStallEvent.mRAT);
                chrBundle.putInt("mUlTup", curStallEvent.mUlTup);
                chrBundle.putInt("mDlTup", curStallEvent.mDlTup);
                chrBundle.putInt("mApRtt", curStallEvent.mApRtt);
                chrBundle.putInt("mNetRtt", curStallEvent.mNetRtt);
                chrBundle.putInt("mCellSig", curStallEvent.mCellSig);
                chrBundle.putInt("mCellRsrq", curStallEvent.mCellRsrq);
                chrBundle.putInt("mCellSinr", curStallEvent.mCellSinr);
                chrBundle.putInt("mNeiborApRssi", curStallEvent.mNeiborApRssi);
                chrBundle.putInt("mWifiSnr", curStallEvent.mWifiSnr);
                chrBundle.putInt("mWifiChload", curStallEvent.mWifiChload);
                chrBundle.putInt("mNetDlTup", curStallEvent.mNetDlTup);
                if (this.mIHiDataCHRCallBack != null) {
                    HwHiStreamUtils.logD("handleUploadStallEventDelay:send stream stall chr event");
                    this.mIHiDataCHRCallBack.uploadHiDataDFTEvent(CHR_UPLOAD_EVENT_STALL_INFO, chrBundle);
                }
                this.mUploadStallCount++;
                curStallEvent.printCHRStallInfo();
                this.mStallInfoList.remove(pos);
            }
        }
    }

    public void handleUploadHandoverInfo(Message msg) {
        if (msg != null && msg.obj != null) {
            Bundle bundle = msg.obj;
            int eventNum = bundle.getInt("eventNum");
            HwHistreamCHRHandoverInfo curHandoverEvent = null;
            int pos = 0;
            int size = this.mHandoverInfolist.size();
            int i = 0;
            while (i < size) {
                HwHistreamCHRHandoverInfo handoverEvent = (HwHistreamCHRHandoverInfo) this.mHandoverInfolist.get(i);
                if (handoverEvent != null) {
                    if (eventNum == handoverEvent.mEventId) {
                        curHandoverEvent = handoverEvent;
                        pos = i;
                        break;
                    }
                    i++;
                } else {
                    return;
                }
            }
            if (curHandoverEvent != null) {
                long curTime = System.currentTimeMillis();
                HwHistreamCHRStatisticsInfo appStatisticsInfo = getCurStatisticsInfo();
                if (appStatisticsInfo == null) {
                    curHandoverEvent.printCHRHandoverInfo();
                    sendCHRHandoverEvent(curHandoverEvent);
                    this.mHandoverInfolist.remove(pos);
                    return;
                }
                if (4 == msg.what) {
                    if (800 == appStatisticsInfo.mCurrNetwork) {
                        updateTup(appStatisticsInfo, 800);
                        curHandoverEvent.mWifiRxTupAft = appStatisticsInfo.mWifiRxTup1;
                    } else if (801 == appStatisticsInfo.mCurrNetwork) {
                        updateTup(appStatisticsInfo, 801);
                        curHandoverEvent.mCellRxTup = appStatisticsInfo.mCelluarRxTup1;
                    }
                    HwHiStreamUtils.logD("handleUploadHandoverInfo: update throughput after handover");
                    this.mCHRManagerHandler.sendMessageDelayed(this.mCHRManagerHandler.obtainMessage(5, bundle), 22000);
                } else if (5 == msg.what) {
                    if (15000 >= curTime - appStatisticsInfo.mLastBadQoeTime) {
                        curHandoverEvent.mStreamQoeAft = 107;
                    } else {
                        curHandoverEvent.mStreamQoeAft = 106;
                    }
                    curHandoverEvent.printCHRHandoverInfo();
                    sendCHRHandoverEvent(curHandoverEvent);
                    this.mHandoverInfolist.remove(pos);
                }
            }
        }
    }

    public void onCHRAppStateChange(HwAPPStateInfo stateInfo, int appState) {
        HwAPPStateInfo hwAPPStateInfo = stateInfo;
        int i = appState;
        HwHiStreamTraffic mHwHiStreamTraffic = HwHiStreamTraffic.getInstance();
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (mHwHiStreamTraffic != null && mHwHiStreamNetworkMonitor != null && hwAPPStateInfo != null) {
            int scenceId = hwAPPStateInfo.mScenceId;
            int appUID = hwAPPStateInfo.mAppUID;
            int networkType = hwAPPStateInfo.mNetworkType;
            HwHistreamCHRStatisticsInfo currAppStatisticsInfo;
            if (100 == i) {
                HwHistreamCHRStatisticsInfo currAppStatisticsInfo2;
                if (HwAPPQoEUtils.SCENE_AUDIO == scenceId || HwAPPQoEUtils.SCENE_VIDEO == scenceId) {
                    this.mWechatStatisticsInfo = new HwHistreamCHRStatisticsInfo(HwHiStreamUtils.WECHAT_NAME, scenceId, appUID);
                    currAppStatisticsInfo2 = this.mWechatStatisticsInfo;
                } else {
                    this.mOtherAppStatisticsInfo = new HwHistreamCHRStatisticsInfo(HwHiStreamUtils.DOUYIN_NAME, scenceId, appUID);
                    currAppStatisticsInfo2 = this.mOtherAppStatisticsInfo;
                }
                HwHistreamCHRStatisticsInfo currAppStatisticsInfo3 = currAppStatisticsInfo2;
                long currentTimeMillis = System.currentTimeMillis();
                currAppStatisticsInfo3.mCallStartTime = currentTimeMillis;
                long startTime = currentTimeMillis;
                currAppStatisticsInfo = currAppStatisticsInfo3;
                currAppStatisticsInfo.mStartCellularTraffic = mHwHiStreamTraffic.getTotalTraffic(0, currentTimeMillis, appUID, 801);
                currAppStatisticsInfo.mCurrNetwork = networkType;
                if (800 == networkType) {
                    currAppStatisticsInfo.mStartInWiFiCnt = 1;
                    currAppStatisticsInfo.mLastWifiTime = startTime;
                } else {
                    long startTime2 = startTime;
                    if (801 == networkType) {
                        currAppStatisticsInfo.mStartInCellularCnt = 1;
                        currAppStatisticsInfo.mLastCellTime = startTime2;
                    }
                }
                currAppStatisticsInfo.mUserType = hwAPPStateInfo.mUserType;
                currAppStatisticsInfo.mCallId = this.mCallId;
                this.mCallId++;
                this.mCHRManagerHandler.removeMessages(7);
                this.mCHRManagerHandler.sendEmptyMessageDelayed(7, HwArbitrationDEFS.DelayTimeMillisA);
            } else {
                if (101 == i) {
                    if (this.mWechatStatisticsInfo != null) {
                        updateAppStatisticInfo(this.mWechatStatisticsInfo);
                        uploadCHRAppStatisticInfo(this.mWechatStatisticsInfo);
                        this.mWechatStatisticsInfo = null;
                    }
                    if (this.mOtherAppStatisticsInfo != null) {
                        updateAppStatisticInfo(this.mOtherAppStatisticsInfo);
                        uploadCHRAppStatisticInfo(this.mOtherAppStatisticsInfo);
                        this.mOtherAppStatisticsInfo = null;
                    }
                    this.mCHRManagerHandler.removeMessages(7);
                }
                currAppStatisticsInfo = null;
            }
        }
    }

    private void uploadCHRAppStatisticInfo(HwHistreamCHRStatisticsInfo appStatisticsInfo) {
        HwHiStreamDataBaseManager mHwHiStreamDataBaseManager = HwHiStreamDataBaseManager.getInstance(this.mContext);
        if (appStatisticsInfo != null && mHwHiStreamDataBaseManager != null) {
            long curTime = System.currentTimeMillis();
            HwHistreamCHRStatisticsInfo statisticsBef = mHwHiStreamDataBaseManager.getAppStatistics(appStatisticsInfo.mScenario);
            if (statisticsBef == null) {
                appStatisticsInfo.mLastUploadTime = curTime;
                mHwHiStreamDataBaseManager.insertAppStatistics(appStatisticsInfo);
                sendCHRStatisticEvent(appStatisticsInfo);
                HwHiStreamUtils.logD("uploadCHRAppStatisticInfo: first call");
            } else {
                statisticsBef.updateCHRStatisticsInfo(appStatisticsInfo);
                if (86400000 <= curTime - statisticsBef.mLastUploadTime) {
                    statisticsBef.printCHRStatisticsInfo();
                    statisticsBef.mLastUploadTime = curTime;
                    sendCHRStatisticEvent(statisticsBef);
                    statisticsBef = new HwHistreamCHRStatisticsInfo(statisticsBef.mApkName, statisticsBef.mScenario, statisticsBef.mUid);
                    statisticsBef.mNum = 0;
                    statisticsBef.mLastUploadTime = curTime;
                }
                statisticsBef.printCHRStatisticsInfo();
                mHwHiStreamDataBaseManager.updateAppStatistics(statisticsBef);
            }
        }
    }

    private void updateAppStatisticInfo(HwHistreamCHRStatisticsInfo appStatisticsInfo) {
        HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo = appStatisticsInfo;
        HwHiStreamTraffic mHwHiStreamTraffic = HwHiStreamTraffic.getInstance();
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (mHwHiStreamTraffic != null && mHwHiStreamNetworkMonitor != null && hwHistreamCHRStatisticsInfo != null) {
            long curTime = System.currentTimeMillis();
            long curCellularTraffic = mHwHiStreamTraffic.getTotalTraffic(0, curTime, hwHistreamCHRStatisticsInfo.mUid, 801);
            if (800 == hwHistreamCHRStatisticsInfo.mCurrNetwork && 0 != hwHistreamCHRStatisticsInfo.mLastWifiTime) {
                hwHistreamCHRStatisticsInfo.mCallInWiFiDur = (int) (((long) hwHistreamCHRStatisticsInfo.mCallInWiFiDur) + ((curTime - hwHistreamCHRStatisticsInfo.mLastWifiTime) / 1000));
                hwHistreamCHRStatisticsInfo.mLastWifiTime = 0;
            } else if (801 == hwHistreamCHRStatisticsInfo.mCurrNetwork && 0 != hwHistreamCHRStatisticsInfo.mLastCellTime) {
                hwHistreamCHRStatisticsInfo.mCallInCellularDur = (int) (((long) hwHistreamCHRStatisticsInfo.mCallInCellularDur) + ((curTime - hwHistreamCHRStatisticsInfo.mLastCellTime) / 1000));
                hwHistreamCHRStatisticsInfo.mLastCellTime = 0;
            }
            if (0 != hwHistreamCHRStatisticsInfo.mLastMplinkTime) {
                hwHistreamCHRStatisticsInfo.mMplinkDur = (int) (((long) hwHistreamCHRStatisticsInfo.mMplinkDur) + ((curTime - hwHistreamCHRStatisticsInfo.mLastMplinkTime) / 1000));
                hwHistreamCHRStatisticsInfo.mMplinkEnTraf = (int) (((long) hwHistreamCHRStatisticsInfo.mMplinkEnTraf) + ((mHwHiStreamTraffic.getTotalTraffic(0, curTime, hwHistreamCHRStatisticsInfo.mUid, 801) - hwHistreamCHRStatisticsInfo.mMplinkStartTraffic) / 1000));
            }
            hwHistreamCHRStatisticsInfo.mTrfficCell = (int) ((curCellularTraffic - hwHistreamCHRStatisticsInfo.mStartCellularTraffic) / 1000);
            if (hwHistreamCHRStatisticsInfo.mCallInCellularDur < 300 && hwHistreamCHRStatisticsInfo.mCallInCellularDur > 0) {
                hwHistreamCHRStatisticsInfo.mCellLv1Cnt = 1;
            } else if (hwHistreamCHRStatisticsInfo.mCallInCellularDur > 900) {
                hwHistreamCHRStatisticsInfo.mCellLv3Cnt = 1;
            } else if (hwHistreamCHRStatisticsInfo.mCallInCellularDur > 0) {
                hwHistreamCHRStatisticsInfo.mCellLv2Cnt = 1;
            }
            if (hwHistreamCHRStatisticsInfo.mCallInWiFiDur < 300 && hwHistreamCHRStatisticsInfo.mCallInWiFiDur > 0) {
                hwHistreamCHRStatisticsInfo.mWiFiLv1Cnt = 1;
            } else if (hwHistreamCHRStatisticsInfo.mCallInWiFiDur > 900) {
                hwHistreamCHRStatisticsInfo.mWiFiLv3Cnt = 1;
            } else if (hwHistreamCHRStatisticsInfo.mCallInWiFiDur > 0) {
                hwHistreamCHRStatisticsInfo.mWiFiLv2Cnt = 1;
            }
            if (1 == hwHistreamCHRStatisticsInfo.mStallSwitchCnt) {
                hwHistreamCHRStatisticsInfo.mStallSwitch1Cnt = 1;
            } else if (hwHistreamCHRStatisticsInfo.mStallSwitchCnt > 1) {
                hwHistreamCHRStatisticsInfo.mStallSwitchAbove1Cnt = 1;
            }
            if (2 == hwHistreamCHRStatisticsInfo.mUserType) {
                hwHistreamCHRStatisticsInfo.mVipSwitchCnt = hwHistreamCHRStatisticsInfo.mStallSwitchCnt;
            }
            appStatisticsInfo.printCHRStatisticsInfo();
        }
    }

    public void onUpdateQuality(int wifiRxTraffic, int cellularRxTraffic, int wifiTxTraffic, int celluarTxTraffic, int uid) {
        HwHistreamCHRStatisticsInfo appStatisticsInfo;
        if (this.mWechatStatisticsInfo != null && uid == this.mWechatStatisticsInfo.mUid) {
            appStatisticsInfo = this.mWechatStatisticsInfo;
        } else if (this.mOtherAppStatisticsInfo != null && uid == this.mOtherAppStatisticsInfo.mUid) {
            appStatisticsInfo = this.mOtherAppStatisticsInfo;
        } else {
            return;
        }
        appStatisticsInfo.mTafficPtr %= appStatisticsInfo.mWifiTraffic.length;
        AppTraffic wifiTraffic = appStatisticsInfo.mWifiTraffic[appStatisticsInfo.mTafficPtr];
        AppTraffic cellularTraffic = appStatisticsInfo.mCellularTraffic[appStatisticsInfo.mTafficPtr];
        if (!(wifiTraffic == null || cellularTraffic == null)) {
            if (800 == appStatisticsInfo.mCurrNetwork) {
                wifiTraffic.rx = wifiRxTraffic;
                wifiTraffic.tx = wifiTxTraffic;
                cellularTraffic.rx = -1;
                cellularTraffic.tx = -1;
            } else if (801 == appStatisticsInfo.mCurrNetwork) {
                cellularTraffic.rx = cellularRxTraffic;
                cellularTraffic.tx = celluarTxTraffic;
                wifiTraffic.rx = -1;
                wifiTraffic.tx = -1;
            }
        }
        appStatisticsInfo.mTafficPtr++;
        updateNetworkInfo(appStatisticsInfo.mCurrNetwork);
    }

    private void updateNetworkInfo(int curNetwork) {
        if (800 == curNetwork && this.mWifiManager != null) {
            WifiInfo info = this.mWifiManager.getConnectionInfo();
            if (!(info == null || -1 == info.getFrequency())) {
                this.mLastWifiInfo.mSsid = info.getSSID();
                this.mLastWifiInfo.mRssi = info.getRssi();
                this.mLastWifiInfo.mChannel = info.getFrequency();
                this.mLastWifiInfo.mFailRate = (int) info.txBadRate;
                this.mLastWifiInfo.mApType = getAPType();
                this.mLastWifiInfo.mSnr = info.getSnr();
                this.mLastWifiInfo.mChLoad = info.getChload();
            }
            HwWifiBoost mHwWifiBoost = HwWifiBoost.getInstance(this.mContext);
            if (mHwWifiBoost != null) {
                RssiPacketCountInfo otaInfo = mHwWifiBoost.getOTAInfo();
                if (otaInfo != null) {
                    int txBad = otaInfo.txbad;
                    int txGood = otaInfo.txgood;
                    if (txGood - this.mLastWifiInfo.mLastTxGood > 0) {
                        this.mLastWifiInfo.mFailRate = ((txBad - this.mLastWifiInfo.mLastTxBad) * 10000) / (txGood - this.mLastWifiInfo.mLastTxGood);
                    }
                    this.mLastWifiInfo.mLastTxBad = txBad;
                    this.mLastWifiInfo.mLastTxGood = txGood;
                }
            }
        }
    }

    private void updateTup(HwHistreamCHRStatisticsInfo appStatisticsInfo, int networkType) {
        if (appStatisticsInfo != null) {
            int i;
            int tafficLen = appStatisticsInfo.mWifiTraffic.length;
            int ptr = appStatisticsInfo.mTafficPtr;
            int count = 0;
            int i2 = 0;
            AppTraffic appTraffic = null;
            int rxTraffic = 0;
            int i3 = 0;
            while (true) {
                i = -1;
                if (i3 >= 3) {
                    break;
                }
                ptr %= tafficLen;
                appTraffic = 800 == networkType ? appStatisticsInfo.mWifiTraffic[ptr] : appStatisticsInfo.mCellularTraffic[ptr];
                if (!(appTraffic == null || -1 == appTraffic.rx)) {
                    rxTraffic += appTraffic.rx;
                    count++;
                }
                ptr++;
                i3++;
            }
            int rxTup2 = count == 0 ? -1 : rxTraffic / (2 * count);
            rxTraffic = 0;
            int txTraffic = 0;
            int ptr2 = appStatisticsInfo.mTafficPtr + 3;
            ptr = 0;
            while (true) {
                count = i2;
                if (count >= 3) {
                    break;
                }
                ptr2 %= tafficLen;
                appTraffic = 800 == networkType ? appStatisticsInfo.mWifiTraffic[ptr2] : appStatisticsInfo.mCellularTraffic[ptr2];
                if (!(appTraffic == null || -1 == appTraffic.rx)) {
                    rxTraffic += appTraffic.rx;
                    txTraffic += appTraffic.tx;
                    ptr++;
                }
                ptr2++;
                i2 = count + 1;
            }
            count = ptr == 0 ? -1 : rxTraffic / (2 * ptr);
            if (ptr != 0) {
                i = txTraffic / (2 * ptr);
            }
            i3 = i;
            if (800 == networkType) {
                appStatisticsInfo.mWifiRxTup2 = rxTup2;
                appStatisticsInfo.mWifiRxTup1 = count;
                appStatisticsInfo.mWifiTxTup = i3;
            } else {
                appStatisticsInfo.mCelluarRxTup2 = rxTup2;
                appStatisticsInfo.mCelluarRxTup1 = count;
                appStatisticsInfo.mCelluarTxTup = i3;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("updateTup: networkType=");
            stringBuilder.append(networkType);
            stringBuilder.append(",WifiTxTup=");
            stringBuilder.append(appStatisticsInfo.mWifiTxTup);
            stringBuilder.append(",WifiRxTup1=");
            stringBuilder.append(appStatisticsInfo.mWifiRxTup1);
            stringBuilder.append(",WifiRxTup2=");
            stringBuilder.append(appStatisticsInfo.mWifiRxTup2);
            stringBuilder.append(",CelluarTxTup=");
            stringBuilder.append(appStatisticsInfo.mCelluarTxTup);
            stringBuilder.append(",CelluarRxTup1=");
            stringBuilder.append(appStatisticsInfo.mCelluarRxTup1);
            stringBuilder.append(",CelluarRxTup2=");
            stringBuilder.append(appStatisticsInfo.mCelluarRxTup2);
            stringBuilder.append(",TafficPtr= ");
            stringBuilder.append(appStatisticsInfo.mTafficPtr);
            HwHiStreamUtils.logD(stringBuilder.toString());
        }
    }

    public void onStallDetect(int appSceneId, int detectResult, IHwHistreamQoeCallback qoeCallback) {
        HwHistreamCHRStatisticsInfo appStatisticsInfo;
        int i = appSceneId;
        IHwHistreamQoeCallback iHwHistreamQoeCallback = qoeCallback;
        HwChannelQoEManager mHwChannelQoEManager = HwChannelQoEManager.getInstance();
        if ((100105 == i || 100106 == i) && this.mWechatStatisticsInfo != null) {
            appStatisticsInfo = this.mWechatStatisticsInfo;
        } else if (this.mOtherAppStatisticsInfo != null) {
            appStatisticsInfo = this.mOtherAppStatisticsInfo;
        } else {
            return;
        }
        HwHistreamCHRStatisticsInfo appStatisticsInfo2 = appStatisticsInfo;
        appStatisticsInfo2.mLastQoe = 100106 == i ? detectResult : 107;
        appStatisticsInfo2.mLastBadQoeTime = System.currentTimeMillis();
        if (HwArbitrationDEFS.DelayTimeMillisA <= appStatisticsInfo2.mLastBadQoeTime - this.mDsQoeLastReportTime) {
            if (86400000 < appStatisticsInfo2.mLastBadQoeTime - this.mUploadStallStartCountTime) {
                this.mUploadStallCount = 0;
                this.mUploadStallStartCountTime = appStatisticsInfo2.mLastBadQoeTime;
            }
            if (20 > this.mUploadStallCount && mHwChannelQoEManager != null) {
                HwHistreamCHRStallInfo chrStallInfo = new HwHistreamCHRStallInfo();
                chrStallInfo.mAPKName = appStatisticsInfo2.mApkName;
                chrStallInfo.mScenario = appStatisticsInfo2.mScenario;
                chrStallInfo.mRAT = appStatisticsInfo2.mCurrNetwork;
                chrStallInfo.mEventId = this.mStallEventId;
                this.mStallEventId++;
                CurrentSignalState signalInfo;
                if (800 == appStatisticsInfo2.mCurrNetwork) {
                    updateTup(appStatisticsInfo2, 800);
                    chrStallInfo.mUlTup = appStatisticsInfo2.mWifiTxTup;
                    chrStallInfo.mDlTup = appStatisticsInfo2.mWifiRxTup1;
                    signalInfo = mHwChannelQoEManager.getCurrentSignalState(appStatisticsInfo2.mCurrNetwork, true, this);
                    chrStallInfo.mWifiSnr = this.mLastWifiInfo.mSnr;
                    chrStallInfo.mWifiChload = this.mLastWifiInfo.mChLoad;
                } else if (801 == appStatisticsInfo2.mCurrNetwork) {
                    updateTup(appStatisticsInfo2, 801);
                    chrStallInfo.mUlTup = appStatisticsInfo2.mCelluarTxTup;
                    chrStallInfo.mDlTup = appStatisticsInfo2.mCelluarRxTup1;
                    CurrentSignalState signalInfo2 = mHwChannelQoEManager.getCurrentSignalState(appStatisticsInfo2.mCurrNetwork, false, this);
                    if (signalInfo2 != null) {
                        chrStallInfo.mCellSinr = signalInfo2.getSigSnr();
                        chrStallInfo.mCellRsrq = signalInfo2.getSigQual();
                        chrStallInfo.mCellSig = signalInfo2.getSigPwr();
                    }
                    if ((100105 == i || 100106 == i) && iHwHistreamQoeCallback != null) {
                        HwHistreamCHRQoeInfo qoeInfo = new HwHistreamCHRQoeInfo(i, appStatisticsInfo2.mLastQoe, chrStallInfo.mUlTup, chrStallInfo.mDlTup, chrStallInfo.mNetDlTup);
                        iHwHistreamQoeCallback.onHistreamBadQoedetect(qoeInfo);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("onStallDetect: sceneId= ");
                        stringBuilder.append(qoeInfo.mSceneId);
                        stringBuilder.append(",videoQoe= ");
                        stringBuilder.append(qoeInfo.mVideoQoe);
                        stringBuilder.append(",ulTup= ");
                        stringBuilder.append(qoeInfo.mUlTup);
                        stringBuilder.append(",dlTup= ");
                        stringBuilder.append(qoeInfo.mDlTup);
                        stringBuilder.append(",netDlTup= ");
                        stringBuilder.append(qoeInfo.mNetDlTup);
                        HwHiStreamUtils.logD(stringBuilder.toString());
                    }
                    signalInfo = signalInfo2;
                }
                this.mDsQoeLastReportTime = appStatisticsInfo2.mLastBadQoeTime;
                this.mStallInfoList.add(chrStallInfo);
                Bundle bundle = new Bundle();
                bundle.putInt("eventNum", chrStallInfo.mEventId);
                this.mCHRManagerHandler.sendMessageDelayed(this.mCHRManagerHandler.obtainMessage(6, bundle), 6000);
            }
        }
    }

    public void onNetworkChange() {
        if (this.mOtherAppStatisticsInfo != null) {
            handleNetworkChange(this.mOtherAppStatisticsInfo);
        }
        if (this.mWechatStatisticsInfo != null) {
            handleNetworkChange(this.mWechatStatisticsInfo);
        }
    }

    public void onMplinkStateChange(Message msg) {
        Message message = msg;
        if (message != null && message.obj != null) {
            Bundle bundle = message.obj;
            int sceneId = bundle.getInt("sceneId");
            int mplinkEvent = bundle.getInt("mplinkEvent");
            int failReason = bundle.getInt("failReason");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onMplinkStateChange: sceneId= ");
            stringBuilder.append(sceneId);
            stringBuilder.append(",mplinkEvent=");
            stringBuilder.append(mplinkEvent);
            stringBuilder.append(",failReason=");
            stringBuilder.append(failReason);
            HwHiStreamUtils.logD(stringBuilder.toString());
            HwHistreamCHRStatisticsInfo appStatisticsInfo = null;
            if (this.mOtherAppStatisticsInfo != null && sceneId == this.mOtherAppStatisticsInfo.mScenario) {
                appStatisticsInfo = this.mOtherAppStatisticsInfo;
            } else if (this.mWechatStatisticsInfo != null && sceneId == this.mWechatStatisticsInfo.mScenario) {
                appStatisticsInfo = this.mWechatStatisticsInfo;
            }
            HwHiStreamTraffic mHwHiStreamTraffic = HwHiStreamTraffic.getInstance();
            HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
            if (appStatisticsInfo != null && mHwHiStreamNetworkMonitor != null && mHwHiStreamTraffic != null) {
                handleNetworkChange(appStatisticsInfo);
                long curTime = System.currentTimeMillis();
                long curTime2;
                if (5 != mplinkEvent) {
                    curTime2 = curTime;
                    if (4 == mplinkEvent || 3 == mplinkEvent || 7 == mplinkEvent || 9 == mplinkEvent) {
                        if (failReason == 0) {
                            int i;
                            if (0 != appStatisticsInfo.mLastMplinkTime) {
                                appStatisticsInfo.mMplinkDur = (int) (((long) appStatisticsInfo.mMplinkDur) + ((curTime2 - appStatisticsInfo.mLastMplinkTime) / 1000));
                                i = 9;
                                appStatisticsInfo.mMplinkEnTraf = (int) (((long) appStatisticsInfo.mMplinkEnTraf) + ((mHwHiStreamTraffic.getTotalTraffic(0, curTime2, appStatisticsInfo.mUid, 801) - appStatisticsInfo.mMplinkStartTraffic) / 1000));
                                StringBuilder stringBuilder2 = new StringBuilder();
                                stringBuilder2.append("onMplinkStateChange: mplink disable,mMplinkDur= ");
                                stringBuilder2.append(appStatisticsInfo.mMplinkDur);
                                stringBuilder2.append(",mMplinkEnTraf=");
                                stringBuilder2.append(appStatisticsInfo.mMplinkEnTraf);
                                HwHiStreamUtils.logD(stringBuilder2.toString());
                            } else {
                                i = 9;
                            }
                            appStatisticsInfo.mLastMplinkTime = 0;
                            updateHandoverInfo(appStatisticsInfo, mplinkEvent);
                            if (3 == mplinkEvent) {
                                appStatisticsInfo.mMplinkDisStallCnt++;
                                appStatisticsInfo.mStallSwitchCnt++;
                            } else if (4 == mplinkEvent) {
                                appStatisticsInfo.mMplinkDisWifiGoodCnt++;
                            } else if (i == mplinkEvent) {
                                appStatisticsInfo.mStallSwitch0Cnt++;
                            }
                        } else {
                            appStatisticsInfo.mMplinkDisFailCnt++;
                        }
                    }
                } else if (failReason != 20) {
                    switch (failReason) {
                        case 0:
                            appStatisticsInfo.mStallSwitchCnt++;
                            appStatisticsInfo.mMplinkEnCnt++;
                            appStatisticsInfo.mLastMplinkTime = curTime;
                            curTime2 = curTime;
                            appStatisticsInfo.mMplinkStartTraffic = mHwHiStreamTraffic.getTotalTraffic(0, curTime, appStatisticsInfo.mUid, 801);
                            updateHandoverInfo(appStatisticsInfo, mplinkEvent);
                            break;
                        case 1:
                            appStatisticsInfo.mMplinkEnFailCnt++;
                            if (!MpLinkCommonUtils.isMpLinkEnabled(this.mContext)) {
                                appStatisticsInfo.mHicureEnCnt++;
                                break;
                            } else {
                                appStatisticsInfo.mMplinkEnFailEnvironCnt++;
                                break;
                            }
                        case 2:
                            appStatisticsInfo.mMplinkEnFailCnt++;
                            appStatisticsInfo.mMplinkEnFailCoexistCnt++;
                            break;
                        case 3:
                            appStatisticsInfo.mMplinkEnFailCnt++;
                            appStatisticsInfo.mMplinkEnFailPingPongCnt++;
                            break;
                        case 4:
                            appStatisticsInfo.mMplinkEnFailCnt++;
                            appStatisticsInfo.mMplinkEnFailHistoryQoeCnt++;
                            break;
                        case 5:
                            appStatisticsInfo.mMplinkEnFailCnt++;
                            appStatisticsInfo.mMplinkEnFailChQoeCnt++;
                            break;
                    }
                } else {
                    appStatisticsInfo.mMplinkEnFailCnt++;
                }
            }
        }
    }

    private void handleNetworkChange(HwHistreamCHRStatisticsInfo appStatisticsInfo) {
        HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo = appStatisticsInfo;
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (hwHistreamCHRStatisticsInfo != null && mHwHiStreamNetworkMonitor != null) {
            int networkType = mHwHiStreamNetworkMonitor.getCurrNetworkType(hwHistreamCHRStatisticsInfo.mUid);
            if (hwHistreamCHRStatisticsInfo.mCurrNetwork != networkType) {
                long curTime = System.currentTimeMillis();
                mHwHiStreamNetworkMonitor.mLastHandoverTime = curTime;
                if (800 != hwHistreamCHRStatisticsInfo.mCurrNetwork || 0 == hwHistreamCHRStatisticsInfo.mLastWifiTime) {
                    long j = 0;
                    if (801 == hwHistreamCHRStatisticsInfo.mCurrNetwork && j != hwHistreamCHRStatisticsInfo.mLastCellTime) {
                        hwHistreamCHRStatisticsInfo.mCallInCellularDur = (int) (((long) hwHistreamCHRStatisticsInfo.mCallInCellularDur) + ((curTime - hwHistreamCHRStatisticsInfo.mLastCellTime) / 1000));
                        hwHistreamCHRStatisticsInfo.mLastCellTime = 0;
                    }
                } else {
                    hwHistreamCHRStatisticsInfo.mCallInWiFiDur = (int) (((long) hwHistreamCHRStatisticsInfo.mCallInWiFiDur) + ((curTime - hwHistreamCHRStatisticsInfo.mLastWifiTime) / 1000));
                    hwHistreamCHRStatisticsInfo.mLastWifiTime = 0;
                }
                if (800 == networkType) {
                    hwHistreamCHRStatisticsInfo.mLastWifiTime = curTime;
                    if (HwArbitrationDEFS.NotificationMonitorPeriodMillis > curTime - mHwHiStreamNetworkMonitor.mLastCellDisableTime || (true == mHwHiStreamNetworkMonitor.getMoblieDateSettings() && 6000 > curTime - mHwHiStreamNetworkMonitor.mLastWifiEnabledTime)) {
                        HwHiStreamUtils.logD("+++++handleNetworkChange: USER manually handover to WiFi++++++");
                        hwHistreamCHRStatisticsInfo.mSwitch2WifiCnt++;
                        updateHandoverInfo(hwHistreamCHRStatisticsInfo, 1);
                    }
                } else if (801 == networkType) {
                    hwHistreamCHRStatisticsInfo.mLastCellTime = curTime;
                    if (HwArbitrationDEFS.NotificationMonitorPeriodMillis > curTime - mHwHiStreamNetworkMonitor.mLastWifiDisabledTime) {
                        HwHiStreamUtils.logD("+++++handleNetworkChange: USER manually handover to cellular++++++");
                        if (!MpLinkCommonUtils.isMpLinkEnabled(this.mContext)) {
                            hwHistreamCHRStatisticsInfo.mHicureSucCnt++;
                        }
                        hwHistreamCHRStatisticsInfo.mSwitch2CellCnt++;
                        updateHandoverInfo(hwHistreamCHRStatisticsInfo, 2);
                    }
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("handleNetworkChange: mScenario=");
                stringBuilder.append(hwHistreamCHRStatisticsInfo.mScenario);
                stringBuilder.append(",oldNetwork=");
                stringBuilder.append(hwHistreamCHRStatisticsInfo.mCurrNetwork);
                stringBuilder.append(",currNetwork=");
                stringBuilder.append(networkType);
                stringBuilder.append(",CallInWiFiDur=");
                stringBuilder.append(hwHistreamCHRStatisticsInfo.mCallInWiFiDur);
                stringBuilder.append(",mCallInCellularDur=");
                stringBuilder.append(hwHistreamCHRStatisticsInfo.mCallInCellularDur);
                HwHiStreamUtils.logD(stringBuilder.toString());
                hwHistreamCHRStatisticsInfo.mCurrNetwork = networkType;
            }
        }
    }

    private void updateHandoverInfo(HwHistreamCHRStatisticsInfo appStatisticsInfo, int event) {
        long curTime = System.currentTimeMillis();
        HwChannelQoEManager mHwChannelQoEManager = HwChannelQoEManager.getInstance();
        if (86400000 < curTime - this.mUploadHandoverStartCountTime) {
            this.mUploadHandoverCount = 0;
            this.mUploadHandoverStartCountTime = curTime;
        }
        if (20 > this.mUploadHandoverCount && appStatisticsInfo != null && mHwChannelQoEManager != null) {
            HwHistreamCHRHandoverInfo mHwHistreamCHRHandoverInfo = new HwHistreamCHRHandoverInfo();
            mHwHistreamCHRHandoverInfo.mCallId = appStatisticsInfo.mCallId;
            mHwHistreamCHRHandoverInfo.mApkName = appStatisticsInfo.mApkName;
            mHwHistreamCHRHandoverInfo.mScenario = appStatisticsInfo.mScenario;
            mHwHistreamCHRHandoverInfo.mEventType = event;
            mHwHistreamCHRHandoverInfo.mEventId = this.mHandoverEventId;
            mHwHistreamCHRHandoverInfo.mSwitchCauseBef = appStatisticsInfo.mLastHandoverCause;
            if (6000 >= System.currentTimeMillis() - appStatisticsInfo.mLastBadQoeTime) {
                mHwHistreamCHRHandoverInfo.mStreamQoeBef = appStatisticsInfo.mLastQoe;
            } else {
                mHwHistreamCHRHandoverInfo.mStreamQoeBef = 106;
            }
            if (2 == appStatisticsInfo.mUserType) {
                mHwHistreamCHRHandoverInfo.mCellFreq = 2;
            }
            this.mHandoverEventId++;
            if (1 == event || 3 == event || 4 == event || 7 == event || 9 == event) {
                WifiInfo info = this.mWifiManager.getConnectionInfo();
                if (info != null) {
                    mHwHistreamCHRHandoverInfo.mWifiSsidAft = info.getSSID();
                    mHwHistreamCHRHandoverInfo.mWifiRssiAft = info.getRssi();
                    mHwHistreamCHRHandoverInfo.mWifiChAft = info.getFrequency();
                    mHwHistreamCHRHandoverInfo.mWifiSnr = info.getSnr();
                    mHwHistreamCHRHandoverInfo.mWifiChLoad = info.getChload();
                    mHwHistreamCHRHandoverInfo.mApType = getAPType();
                }
                HistoryMseasureInfo historyMseasureInfo = mHwChannelQoEManager.getHistoryMseasureInfo(800);
                if (historyMseasureInfo != null) {
                    mHwHistreamCHRHandoverInfo.mRttBef = historyMseasureInfo.getRttBef();
                }
                updateTup(appStatisticsInfo, 801);
                mHwHistreamCHRHandoverInfo.mCellRxTup = appStatisticsInfo.mCelluarRxTup1;
                historyMseasureInfo = mHwChannelQoEManager.getHistoryMseasureInfo(801);
                if (historyMseasureInfo != null) {
                    mHwHistreamCHRHandoverInfo.mCellQuality = historyMseasureInfo.getQual();
                    mHwHistreamCHRHandoverInfo.mCellSinr = historyMseasureInfo.getSnr();
                    mHwHistreamCHRHandoverInfo.mCellRat = historyMseasureInfo.getRat();
                    mHwHistreamCHRHandoverInfo.mCellSig = historyMseasureInfo.getPwr();
                }
            } else if (2 == event || 5 == event) {
                mHwHistreamCHRHandoverInfo.mWifiSsidBef = this.mLastWifiInfo.mSsid;
                mHwHistreamCHRHandoverInfo.mWifiRssiBef = this.mLastWifiInfo.mRssi;
                mHwHistreamCHRHandoverInfo.mWifiChBef = this.mLastWifiInfo.mChannel;
                mHwHistreamCHRHandoverInfo.mWifiTxFail1Bef = this.mLastWifiInfo.mFailRate;
                mHwHistreamCHRHandoverInfo.mWifiChLoad = this.mLastWifiInfo.mChLoad;
                mHwHistreamCHRHandoverInfo.mWifiSnr = this.mLastWifiInfo.mSnr;
                mHwHistreamCHRHandoverInfo.mApType = this.mLastWifiInfo.mApType;
                updateTup(appStatisticsInfo, 800);
                mHwHistreamCHRHandoverInfo.mWifiRxTup1Bef = appStatisticsInfo.mWifiRxTup1;
                mHwHistreamCHRHandoverInfo.mWifiRxTup2Bef = appStatisticsInfo.mWifiRxTup2;
                HistoryMseasureInfo historyMseasureInfo2 = mHwChannelQoEManager.getHistoryMseasureInfo(801);
                if (historyMseasureInfo2 != null) {
                    mHwHistreamCHRHandoverInfo.mRttBef = historyMseasureInfo2.getRttBef();
                }
                CurrentSignalState signalInfo = mHwChannelQoEManager.getCurrentSignalState(801, false, this);
                if (signalInfo != null) {
                    mHwHistreamCHRHandoverInfo.mCellSig = signalInfo.getSigPwr();
                    mHwHistreamCHRHandoverInfo.mCellQuality = signalInfo.getSigQual();
                    mHwHistreamCHRHandoverInfo.mCellSinr = signalInfo.getSigSnr();
                    mHwHistreamCHRHandoverInfo.mCellRat = signalInfo.getNetwork();
                }
            }
            appStatisticsInfo.mLastHandoverCause = event;
            if (3 == event || 5 == event) {
                QueryHistAppQoeService wmService = QueryHistAppQoeService.getInstance();
                if (wmService != null) {
                    mHwHistreamCHRHandoverInfo.mWavemappingInfo = wmService.queryRecordByApp(HwAPPQoEUtils.APP_TYPE_STREAMING, 0, mHwHistreamCHRHandoverInfo.mScenario);
                }
            }
            this.mHandoverInfolist.add(mHwHistreamCHRHandoverInfo);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("updateHandoverInfo : handover event, event=");
            stringBuilder.append(event);
            stringBuilder.append(",eventId=");
            stringBuilder.append(mHwHistreamCHRHandoverInfo.mEventId);
            HwHiStreamUtils.logD(stringBuilder.toString());
            Bundle bundle = new Bundle();
            bundle.putInt("eventNum", mHwHistreamCHRHandoverInfo.mEventId);
            this.mCHRManagerHandler.sendMessageDelayed(this.mCHRManagerHandler.obtainMessage(4, bundle), 8000);
        }
    }

    public int getAPType() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            int type = getAuthType(wifiInfo.getNetworkId());
            if (type == 1 || type == 4) {
                return 0;
            }
        }
        return 1;
    }

    private int getAuthType(int networkId) {
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            return -1;
        }
        for (WifiConfiguration config : configs) {
            if (config != null && isValid(config) && networkId == config.networkId) {
                return config.getAuthType();
            }
        }
        return -1;
    }

    private boolean isValid(WifiConfiguration config) {
        boolean z = false;
        if (config == null) {
            return false;
        }
        if (config.allowedKeyManagement.cardinality() <= 1) {
            z = true;
        }
        return z;
    }

    public void onCurrentRtt(int rtt) {
        this.mCurNetRtt = rtt;
        this.mLastGetCurRttTime = System.currentTimeMillis();
    }

    public void onChannelQuality(int UID, int sense, int network, int label) {
    }

    public void onWifiLinkQuality(int UID, int sense, int label) {
    }

    public void onCellPSAvailable(boolean isOK, int reason) {
    }

    private HwHistreamCHRStatisticsInfo getCurStatisticsInfo() {
        if (this.mOtherAppStatisticsInfo != null) {
            return this.mOtherAppStatisticsInfo;
        }
        if (this.mWechatStatisticsInfo != null) {
            return this.mWechatStatisticsInfo;
        }
        return null;
    }

    private void sendCHRCollectParaEvent(HwHistreamCHRMachineInfo machineInfo) {
        if (machineInfo != null) {
            this.mUploadCollectParaCount++;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sendCHRCollectParaEvent: mUploadCollectParaCount=");
            stringBuilder.append(this.mUploadCollectParaCount);
            HwHiStreamUtils.logD(stringBuilder.toString());
            EventStream collectParaEventStream = IMonitor.openEventStream(CHR_UPLOAD_EVENT_QOE_PARA_COLLECT);
            if (collectParaEventStream != null) {
                collectParaEventStream.setParam((short) 0, machineInfo.mRxTup1Bef).setParam((short) 1, machineInfo.mRxTup2Bef).setParam((short) 2, machineInfo.mChLoad).setParam((short) 3, machineInfo.mTxFail1Bef).setParam((short) 4, machineInfo.mTxFail2Bef).setParam((short) 5, machineInfo.mApkName).setParam((short) 6, machineInfo.mScenario).setParam((short) 7, machineInfo.mStreamQoe).setParam((short) 8, machineInfo.mWechatVideoQoe).setParam((short) 9, machineInfo.mRAT).setParam((short) 10, machineInfo.mWifiRssi).setParam((short) 11, machineInfo.mWifiSnr).setParam((short) 12, machineInfo.mCellSig).setParam((short) 13, machineInfo.mCellQuality).setParam((short) 14, machineInfo.mCellSinr).setParam((short) 15, machineInfo.mNetDlTup).setParam((short) 16, machineInfo.mNetRtt);
                IMonitor.sendEvent(collectParaEventStream);
                IMonitor.closeEventStream(collectParaEventStream);
            }
        }
    }

    private void sendCHRHandoverEvent(HwHistreamCHRHandoverInfo handoverInfo) {
        HwHistreamCHRHandoverInfo hwHistreamCHRHandoverInfo = handoverInfo;
        EventStream handoverEventStream = IMonitor.openEventStream(CHR_UPLOAD_EVENT_HANDOVER);
        if (hwHistreamCHRHandoverInfo != null && handoverEventStream != null) {
            EventStream spaceInfoStream = null;
            EventStream historyRecordStream = null;
            this.mUploadHandoverCount++;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sendCHRHandoverEvent: mUploadHandoverCount=");
            stringBuilder.append(this.mUploadHandoverCount);
            HwHiStreamUtils.logD(stringBuilder.toString());
            handoverEventStream.setParam((short) 0, hwHistreamCHRHandoverInfo.mApkName).setParam((short) 1, hwHistreamCHRHandoverInfo.mScenario).setParam((short) 2, hwHistreamCHRHandoverInfo.mEventType).setParam((short) 3, hwHistreamCHRHandoverInfo.mWifiSsidBef).setParam((short) 4, hwHistreamCHRHandoverInfo.mWifiRssiBef).setParam((short) 5, hwHistreamCHRHandoverInfo.mWifiChBef).setParam((short) 6, hwHistreamCHRHandoverInfo.mWifiSsidAft).setParam((short) 7, hwHistreamCHRHandoverInfo.mWifiRssiAft).setParam((short) 8, hwHistreamCHRHandoverInfo.mWifiChAft).setParam((short) 9, hwHistreamCHRHandoverInfo.mCellRat).setParam((short) 10, hwHistreamCHRHandoverInfo.mCellSig).setParam((short) 11, hwHistreamCHRHandoverInfo.mCellFreq).setParam((short) 12, hwHistreamCHRHandoverInfo.mWifiRxTup1Bef).setParam((short) 13, hwHistreamCHRHandoverInfo.mWifiRxTup2Bef).setParam((short) 14, hwHistreamCHRHandoverInfo.mWifiChLoad).setParam((short) 15, hwHistreamCHRHandoverInfo.mWifiTxFail1Bef).setParam((short) 16, hwHistreamCHRHandoverInfo.mWifiTxFail2Bef).setParam((short) 17, hwHistreamCHRHandoverInfo.mWifiRxTupAft).setParam((short) 18, hwHistreamCHRHandoverInfo.mCellRxTup).setParam((short) 19, hwHistreamCHRHandoverInfo.mSwitchCauseBef).setParam((short) 20, hwHistreamCHRHandoverInfo.mWifiSnr).setParam((short) 21, hwHistreamCHRHandoverInfo.mCellQuality).setParam((short) 22, hwHistreamCHRHandoverInfo.mCellSinr).setParam((short) 23, hwHistreamCHRHandoverInfo.mStreamQoeBef).setParam((short) 24, hwHistreamCHRHandoverInfo.mStreamQoeAft).setParam((short) 25, hwHistreamCHRHandoverInfo.mApType).setParam((short) 27, hwHistreamCHRHandoverInfo.mTupBef).setParam((short) 28, hwHistreamCHRHandoverInfo.mRttBef);
            if (hwHistreamCHRHandoverInfo.mWavemappingInfo != null) {
                spaceInfoStream = IMonitor.openEventStream(909009047);
                historyRecordStream = IMonitor.openEventStream(909009048);
                if (!(spaceInfoStream == null || historyRecordStream == null)) {
                    spaceInfoStream.setParam((short) 0, hwHistreamCHRHandoverInfo.mWavemappingInfo.spaceId_all).setParam((short) 1, hwHistreamCHRHandoverInfo.mWavemappingInfo.modelVer_all).setParam((short) 2, hwHistreamCHRHandoverInfo.mWavemappingInfo.spaceId_main).setParam((short) 3, hwHistreamCHRHandoverInfo.mWavemappingInfo.modelVer_main).setParam((short) 4, hwHistreamCHRHandoverInfo.mWavemappingInfo.spaceId_cell).setParam((short) 5, hwHistreamCHRHandoverInfo.mWavemappingInfo.modelVer_cell).setParam((short) 6, hwHistreamCHRHandoverInfo.mWavemappingInfo.netIdCnt).setParam((short) 7, hwHistreamCHRHandoverInfo.mWavemappingInfo.netName).setParam((short) 8, hwHistreamCHRHandoverInfo.mWavemappingInfo.netFreq).setParam((short) 9, hwHistreamCHRHandoverInfo.mWavemappingInfo.netType).setParam((short) 10, hwHistreamCHRHandoverInfo.mWavemappingInfo.recordDays);
                    historyRecordStream.setParam((short) 0, hwHistreamCHRHandoverInfo.mScenario).setParam((short) 1, spaceInfoStream).setParam((short) 2, hwHistreamCHRHandoverInfo.mWavemappingInfo.duration).setParam((short) 3, hwHistreamCHRHandoverInfo.mWavemappingInfo.goodCnt).setParam((short) 4, hwHistreamCHRHandoverInfo.mWavemappingInfo.poorCnt).setParam((short) 5, hwHistreamCHRHandoverInfo.mWavemappingInfo.dataRx).setParam((short) 6, hwHistreamCHRHandoverInfo.mWavemappingInfo.dataTx);
                    handoverEventStream.setParam((short) 26, historyRecordStream);
                }
            } else {
                HwHiStreamUtils.logD("sendCHRHandoverEvent:wave mapping history info is null");
            }
            IMonitor.sendEvent(handoverEventStream);
            IMonitor.closeEventStream(spaceInfoStream);
            IMonitor.closeEventStream(historyRecordStream);
            IMonitor.closeEventStream(handoverEventStream);
        }
    }

    private void sendCHRStatisticEvent(HwHistreamCHRStatisticsInfo appStatisticsInfo) {
        HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo = appStatisticsInfo;
        if (hwHistreamCHRStatisticsInfo != null) {
            EventStream statisticsStream = null;
            int i = hwHistreamCHRStatisticsInfo.mScenario;
            if (i != HwAPPQoEUtils.SCENE_DOUYIN) {
                switch (i) {
                    case HwAPPQoEUtils.SCENE_AUDIO /*100105*/:
                        statisticsStream = IMonitor.openEventStream(CHR_UPLOAD_EVENT_WECHAT_AUDIO_STATISTICS);
                        if (statisticsStream != null) {
                            statisticsStream.setParam((short) 0, hwHistreamCHRStatisticsInfo.mNum).setParam((short) 1, hwHistreamCHRStatisticsInfo.mStartInCellularCnt).setParam((short) 2, hwHistreamCHRStatisticsInfo.mStartInWiFiCnt).setParam((short) 3, hwHistreamCHRStatisticsInfo.mCallInCellularDur).setParam((short) 4, hwHistreamCHRStatisticsInfo.mCallInWiFiDur).setParam((short) 5, hwHistreamCHRStatisticsInfo.mCellLv1Cnt).setParam((short) 6, hwHistreamCHRStatisticsInfo.mCellLv2Cnt).setParam((short) 7, hwHistreamCHRStatisticsInfo.mCellLv3Cnt).setParam((short) 8, hwHistreamCHRStatisticsInfo.mWiFiLv1Cnt).setParam((short) 9, hwHistreamCHRStatisticsInfo.mWiFiLv2Cnt).setParam((short) 10, hwHistreamCHRStatisticsInfo.mWiFiLv3Cnt).setParam((short) 11, hwHistreamCHRStatisticsInfo.mTrfficCell).setParam((short) 12, hwHistreamCHRStatisticsInfo.mVipSwitchCnt).setParam((short) 13, hwHistreamCHRStatisticsInfo.mStallSwitchCnt).setParam((short) 14, hwHistreamCHRStatisticsInfo.mStallSwitch0Cnt).setParam((short) 15, hwHistreamCHRStatisticsInfo.mStallSwitch1Cnt).setParam((short) 16, hwHistreamCHRStatisticsInfo.mStallSwitchAbove1Cnt).setParam((short) 17, hwHistreamCHRStatisticsInfo.mSwitch2CellCnt).setParam((short) 18, hwHistreamCHRStatisticsInfo.mSwitch2WifiCnt).setParam((short) 19, hwHistreamCHRStatisticsInfo.mMplinkDur).setParam((short) 20, hwHistreamCHRStatisticsInfo.mMplinkEnCnt).setParam((short) 21, hwHistreamCHRStatisticsInfo.mMplinkDisStallCnt).setParam((short) 22, hwHistreamCHRStatisticsInfo.mMplinkDisWifiGoodCnt).setParam((short) 23, hwHistreamCHRStatisticsInfo.mMplinkEnFailCnt).setParam((short) 24, hwHistreamCHRStatisticsInfo.mMplinkDisFailCnt).setParam((short) 25, hwHistreamCHRStatisticsInfo.mMplinkEnTraf).setParam((short) 26, hwHistreamCHRStatisticsInfo.mMplinkEnFailEnvironCnt).setParam((short) 27, hwHistreamCHRStatisticsInfo.mMplinkEnFailCoexistCnt).setParam((short) 28, hwHistreamCHRStatisticsInfo.mMplinkEnFailPingPongCnt).setParam((short) 29, hwHistreamCHRStatisticsInfo.mMplinkEnFailHistoryQoeCnt).setParam((short) 30, hwHistreamCHRStatisticsInfo.mMplinkEnFailChQoeCnt).setParam((short) 31, hwHistreamCHRStatisticsInfo.mHicureEnCnt).setParam((short) 32, hwHistreamCHRStatisticsInfo.mHicureSucCnt);
                            break;
                        }
                        return;
                    case HwAPPQoEUtils.SCENE_VIDEO /*100106*/:
                        statisticsStream = IMonitor.openEventStream(CHR_UPLOAD_EVENT_WECHAT_VIDEO_STATISTICS);
                        if (statisticsStream != null) {
                            statisticsStream.setParam((short) 0, hwHistreamCHRStatisticsInfo.mNum).setParam((short) 1, hwHistreamCHRStatisticsInfo.mStartInCellularCnt).setParam((short) 2, hwHistreamCHRStatisticsInfo.mStartInWiFiCnt).setParam((short) 3, hwHistreamCHRStatisticsInfo.mCallInCellularDur).setParam((short) 4, hwHistreamCHRStatisticsInfo.mCallInWiFiDur).setParam((short) 5, hwHistreamCHRStatisticsInfo.mCellLv1Cnt).setParam((short) 6, hwHistreamCHRStatisticsInfo.mCellLv2Cnt).setParam((short) 7, hwHistreamCHRStatisticsInfo.mCellLv3Cnt).setParam((short) 8, hwHistreamCHRStatisticsInfo.mWiFiLv1Cnt).setParam((short) 9, hwHistreamCHRStatisticsInfo.mWiFiLv2Cnt).setParam((short) 10, hwHistreamCHRStatisticsInfo.mWiFiLv3Cnt).setParam((short) 11, hwHistreamCHRStatisticsInfo.mTrfficCell).setParam((short) 12, hwHistreamCHRStatisticsInfo.mVipSwitchCnt).setParam((short) 13, hwHistreamCHRStatisticsInfo.mStallSwitchCnt).setParam((short) 14, hwHistreamCHRStatisticsInfo.mStallSwitch0Cnt).setParam((short) 15, hwHistreamCHRStatisticsInfo.mStallSwitch1Cnt).setParam((short) 16, hwHistreamCHRStatisticsInfo.mStallSwitchAbove1Cnt).setParam((short) 17, hwHistreamCHRStatisticsInfo.mSwitch2CellCnt).setParam((short) 18, hwHistreamCHRStatisticsInfo.mSwitch2WifiCnt).setParam((short) 19, hwHistreamCHRStatisticsInfo.mMplinkDur).setParam((short) 20, hwHistreamCHRStatisticsInfo.mMplinkEnCnt).setParam((short) 21, hwHistreamCHRStatisticsInfo.mMplinkDisStallCnt).setParam((short) 22, hwHistreamCHRStatisticsInfo.mMplinkDisWifiGoodCnt).setParam((short) 23, hwHistreamCHRStatisticsInfo.mMplinkEnFailCnt).setParam((short) 24, hwHistreamCHRStatisticsInfo.mMplinkDisFailCnt).setParam((short) 25, hwHistreamCHRStatisticsInfo.mMplinkEnTraf).setParam((short) 26, hwHistreamCHRStatisticsInfo.mMplinkEnFailEnvironCnt).setParam((short) 27, hwHistreamCHRStatisticsInfo.mMplinkEnFailCoexistCnt).setParam((short) 28, hwHistreamCHRStatisticsInfo.mMplinkEnFailPingPongCnt).setParam((short) 29, hwHistreamCHRStatisticsInfo.mMplinkEnFailHistoryQoeCnt).setParam((short) 30, hwHistreamCHRStatisticsInfo.mMplinkEnFailChQoeCnt).setParam((short) 31, hwHistreamCHRStatisticsInfo.mHicureEnCnt).setParam((short) 32, hwHistreamCHRStatisticsInfo.mHicureSucCnt);
                            break;
                        }
                        return;
                }
            }
            statisticsStream = IMonitor.openEventStream(CHR_UPLOAD_EVENT_DOUYIN_STATISTICS);
            if (statisticsStream != null) {
                statisticsStream.setParam((short) 0, hwHistreamCHRStatisticsInfo.mNum).setParam((short) 1, hwHistreamCHRStatisticsInfo.mStartInCellularCnt).setParam((short) 2, hwHistreamCHRStatisticsInfo.mStartInWiFiCnt).setParam((short) 3, hwHistreamCHRStatisticsInfo.mCallInCellularDur).setParam((short) 4, hwHistreamCHRStatisticsInfo.mCallInWiFiDur).setParam((short) 5, hwHistreamCHRStatisticsInfo.mCellLv1Cnt).setParam((short) 6, hwHistreamCHRStatisticsInfo.mCellLv2Cnt).setParam((short) 7, hwHistreamCHRStatisticsInfo.mCellLv3Cnt).setParam((short) 8, hwHistreamCHRStatisticsInfo.mWiFiLv1Cnt).setParam((short) 9, hwHistreamCHRStatisticsInfo.mWiFiLv2Cnt).setParam((short) 10, hwHistreamCHRStatisticsInfo.mWiFiLv3Cnt).setParam((short) 11, hwHistreamCHRStatisticsInfo.mTrfficCell).setParam((short) 12, hwHistreamCHRStatisticsInfo.mStallSwitch0Cnt).setParam((short) 13, hwHistreamCHRStatisticsInfo.mStallSwitch1Cnt).setParam((short) 14, hwHistreamCHRStatisticsInfo.mStallSwitchAbove1Cnt).setParam((short) 15, hwHistreamCHRStatisticsInfo.mSwitch2CellCnt).setParam((short) 16, hwHistreamCHRStatisticsInfo.mSwitch2WifiCnt).setParam((short) 17, hwHistreamCHRStatisticsInfo.mMplinkDur).setParam((short) 18, hwHistreamCHRStatisticsInfo.mMplinkEnCnt).setParam((short) 19, hwHistreamCHRStatisticsInfo.mMplinkDisStallCnt).setParam((short) 20, hwHistreamCHRStatisticsInfo.mMplinkDisWifiGoodCnt).setParam((short) 21, hwHistreamCHRStatisticsInfo.mMplinkEnFailCnt).setParam((short) 22, hwHistreamCHRStatisticsInfo.mMplinkDisFailCnt).setParam((short) 23, hwHistreamCHRStatisticsInfo.mMplinkEnTraf).setParam((short) 24, hwHistreamCHRStatisticsInfo.mMplinkEnFailEnvironCnt).setParam((short) 25, hwHistreamCHRStatisticsInfo.mMplinkEnFailCoexistCnt).setParam((short) 26, hwHistreamCHRStatisticsInfo.mMplinkEnFailPingPongCnt).setParam((short) 27, hwHistreamCHRStatisticsInfo.mMplinkEnFailHistoryQoeCnt).setParam((short) 28, hwHistreamCHRStatisticsInfo.mMplinkEnFailChQoeCnt).setParam((short) 29, hwHistreamCHRStatisticsInfo.mHicureEnCnt).setParam((short) 30, hwHistreamCHRStatisticsInfo.mHicureSucCnt);
            } else {
                return;
            }
            IMonitor.sendEvent(statisticsStream);
            IMonitor.closeEventStream(statisticsStream);
        }
    }
}
