package com.android.server.hidata.histream;

import com.android.server.hidata.wavemapping.chr.entity.HistAppQoeChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;

public class HwHistreamCHRHandoverInfo {
    public int mApType = -1;
    public String mApkName = Constant.USERDB_APP_NAME_NONE;
    public int mCallId = -1;
    public int mCellFreq = 1;
    public int mCellQuality = -1;
    public int mCellRat = -1;
    public int mCellRxTup = -1;
    public int mCellSig = -1;
    public int mCellSinr = -1;
    public int mEventId = -1;
    public int mEventType = -1;
    public int mRttBef = -1;
    public int mScenario = -1;
    public int mStreamQoeAft = -1;
    public int mStreamQoeBef = -1;
    public int mSwitchCauseBef = -1;
    public int mTupBef = -1;
    public HistAppQoeChrInfo mWavemappingInfo = null;
    public int mWifiChAft = -1;
    public int mWifiChBef = -1;
    public int mWifiChLoad = -1;
    public int mWifiRssiAft = -1;
    public int mWifiRssiBef = -1;
    public int mWifiRxTup1Bef = -1;
    public int mWifiRxTup2Bef = -1;
    public int mWifiRxTupAft = -1;
    public int mWifiSnr = -1;
    public String mWifiSsidAft = Constant.USERDB_APP_NAME_NONE;
    public String mWifiSsidBef = Constant.USERDB_APP_NAME_NONE;
    public int mWifiTxFail1Bef = -1;
    public int mWifiTxFail2Bef = -1;

    public void printCHRHandoverInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("printCHRHandoverInfo mEventId = ");
        stringBuilder.append(this.mEventId);
        stringBuilder.append(" mCallId = ");
        stringBuilder.append(this.mCallId);
        stringBuilder.append(" mApkName = ");
        stringBuilder.append(this.mApkName);
        stringBuilder.append(" mScenario = ");
        stringBuilder.append(this.mScenario);
        stringBuilder.append(" mEventType = ");
        stringBuilder.append(this.mEventType);
        stringBuilder.append(" mWifiSsidAft = ");
        stringBuilder.append(this.mWifiSsidAft);
        stringBuilder.append(" mWifiRssiAft = ");
        stringBuilder.append(this.mWifiRssiAft);
        stringBuilder.append(" mWifiChAft = ");
        stringBuilder.append(this.mWifiChAft);
        stringBuilder.append(" mWifiRxTupAft = ");
        stringBuilder.append(this.mWifiRxTupAft);
        stringBuilder.append(" mWifiSsidBef = ");
        stringBuilder.append(this.mWifiSsidBef);
        stringBuilder.append(" mWifiRssiBef = ");
        stringBuilder.append(this.mWifiRssiBef);
        stringBuilder.append(" mWifiChBef = ");
        stringBuilder.append(this.mWifiChBef);
        stringBuilder.append(" mWifiRxTup1Bef = ");
        stringBuilder.append(this.mWifiRxTup1Bef);
        stringBuilder.append(" mWifiRxTup2Bef = ");
        stringBuilder.append(this.mWifiRxTup2Bef);
        stringBuilder.append(" mApType = ");
        stringBuilder.append(this.mApType);
        stringBuilder.append(" mWifiTxFail1Bef = ");
        stringBuilder.append(this.mWifiTxFail1Bef);
        stringBuilder.append(" mWifiTxFail2Bef = ");
        stringBuilder.append(this.mWifiTxFail2Bef);
        stringBuilder.append(" mWifiChLoad = ");
        stringBuilder.append(this.mWifiChLoad);
        stringBuilder.append(" mCellRat = ");
        stringBuilder.append(this.mCellRat);
        stringBuilder.append(" mCellSig = ");
        stringBuilder.append(this.mCellSig);
        stringBuilder.append(" mCellFreq = ");
        stringBuilder.append(this.mCellFreq);
        stringBuilder.append(" mCellRxTup = ");
        stringBuilder.append(this.mCellRxTup);
        stringBuilder.append(" mSwitchCauseBef = ");
        stringBuilder.append(this.mSwitchCauseBef);
        stringBuilder.append(" mStreamQoeBef = ");
        stringBuilder.append(this.mStreamQoeBef);
        stringBuilder.append(" mStreamQoeAft = ");
        stringBuilder.append(this.mStreamQoeAft);
        HwHiStreamUtils.logD(stringBuilder.toString());
    }
}
