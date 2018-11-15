package com.android.server.hidata.arbitration;

import com.android.server.hidata.appqoe.HwAPPStateInfo;

public class HwArbitrationAppBoostInfo {
    public int mAppID;
    private HwAPPStateInfo mHwAPPStateInfo;
    public boolean mIsCoex;
    public boolean mIsMPLink;
    public int mNetwork;
    public int mSceneId;
    public int mSolution;
    public int mUID;

    public HwArbitrationAppBoostInfo() {
        this.mAppID = -1;
        this.mUID = -1;
        this.mSceneId = -1;
        this.mNetwork = -1;
        this.mIsMPLink = false;
        this.mIsCoex = false;
        this.mSolution = -1;
        this.mHwAPPStateInfo = null;
    }

    public void setHwAPPStateInfo(HwAPPStateInfo mHwAPPStateInfo) {
        this.mHwAPPStateInfo = mHwAPPStateInfo;
    }

    public HwAPPStateInfo getHwAPPStateInfo() {
        return this.mHwAPPStateInfo;
    }

    public HwArbitrationAppBoostInfo(int AppID, int UID, int sceneId, int network, boolean isCoex, boolean isMPlink, int solution) {
        this.mAppID = AppID;
        this.mUID = UID;
        this.mSceneId = sceneId;
        this.mNetwork = network;
        this.mIsCoex = isCoex;
        this.mIsMPLink = isMPlink;
        this.mSolution = solution;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HwArbitrationAppBoostInfo AppID ");
        stringBuilder.append(this.mAppID);
        stringBuilder.append(" mUID, ");
        stringBuilder.append(this.mUID);
        stringBuilder.append(", mSceneId:");
        stringBuilder.append(this.mSceneId);
        stringBuilder.append(", mNetwork:");
        stringBuilder.append(this.mNetwork);
        stringBuilder.append(", mIsCoex:");
        stringBuilder.append(this.mIsCoex);
        stringBuilder.append(", mIsMPLink:");
        stringBuilder.append(this.mIsMPLink);
        stringBuilder.append(",  mSolution:");
        stringBuilder.append(this.mSolution);
        return stringBuilder.toString();
    }

    public int getAppID() {
        return this.mAppID;
    }

    public void setAppID(int AppID) {
        this.mAppID = AppID;
    }

    public int getBoostUID() {
        return this.mUID;
    }

    public void setBoostUID(int UID) {
        this.mUID = UID;
    }

    public int getSceneId() {
        return this.mSceneId;
    }

    public void setSceneId(int sceneId) {
        this.mSceneId = sceneId;
    }

    public int getNetwork() {
        return this.mNetwork;
    }

    public void setNetwork(int network) {
        this.mNetwork = network;
    }

    public boolean getIsMPlink() {
        return this.mIsMPLink;
    }

    public void setIsMPLink(boolean isMPlink) {
        this.mIsMPLink = isMPlink;
    }

    public int getSolution() {
        return this.mSolution;
    }

    public void setSolution(int solution) {
        this.mSolution = solution;
    }

    public boolean getIsCoex() {
        return this.mIsCoex;
    }

    public void setIsCoex(boolean mIsCoex) {
        this.mIsCoex = mIsCoex;
    }
}
