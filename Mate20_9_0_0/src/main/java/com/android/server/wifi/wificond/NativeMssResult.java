package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class NativeMssResult implements Parcelable {
    public static final Creator<NativeMssResult> CREATOR = new Creator<NativeMssResult>() {
        public NativeMssResult createFromParcel(Parcel in) {
            NativeMssResult result = new NativeMssResult();
            int compvapNum = in.readInt();
            if (compvapNum != 3) {
                String str = NativeMssResult.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("read vap number:");
                stringBuilder.append(compvapNum);
                stringBuilder.append(",expect:");
                stringBuilder.append(3);
                Log.d(str, stringBuilder.toString());
                return result;
            }
            for (int i = 0; i < compvapNum; i++) {
                in.readByteArray(result.mssVapList[i].userMacAddr);
                result.mssVapList[i].actionType = in.readByte();
                result.mssVapList[i].mssResult = in.readByte();
            }
            result.mssResult = in.readByte();
            result.mssMode = in.readByte();
            result.mssState = in.readByte();
            result.vapNum = in.readByte();
            return result;
        }

        public NativeMssResult[] newArray(int size) {
            return new NativeMssResult[size];
        }
    };
    private static final int ETH_ALEN = 6;
    private static final int MAX_VAP_NUM = 3;
    public static final int RET_FAIL = 0;
    public static final int RET_SUCC = 1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_MIMO = 2;
    public static final int STATE_MISO = 3;
    public static final int STATE_SIMO = 4;
    public static final int STATE_SISO = 1;
    private static final String TAG = "NativeMssResult";
    public byte mssMode;
    public byte mssResult;
    public byte mssState;
    public MssVapInfo[] mssVapList = new MssVapInfo[3];
    public byte vapNum;

    public static class MssVapInfo {
        public byte actionType;
        public byte mssResult;
        public byte[] userMacAddr;
    }

    public NativeMssResult() {
        for (int i = 0; i < 3; i++) {
            this.mssVapList[i] = new MssVapInfo();
            this.mssVapList[i].userMacAddr = new byte[6];
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(3);
        for (int i = 0; i < 3; i++) {
            out.writeByteArray(this.mssVapList[i].userMacAddr);
            out.writeByte(this.mssVapList[i].actionType);
            out.writeByte(this.mssVapList[i].mssResult);
        }
        out.writeByte(this.mssResult);
        out.writeByte(this.mssMode);
        out.writeByte(this.mssState);
        out.writeByte(this.vapNum);
    }

    public void readFromParcel(Parcel in) {
        int compvapNum = in.readInt();
        if (compvapNum != 3) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("read vap number:");
            stringBuilder.append(compvapNum);
            stringBuilder.append(",expect:");
            stringBuilder.append(3);
            Log.d(str, stringBuilder.toString());
            return;
        }
        for (int i = 0; i < compvapNum; i++) {
            in.readByteArray(this.mssVapList[i].userMacAddr);
            this.mssVapList[i].actionType = in.readByte();
            this.mssVapList[i].mssResult = in.readByte();
        }
        this.mssResult = in.readByte();
        this.mssMode = in.readByte();
        this.mssState = in.readByte();
        this.vapNum = in.readByte();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" result:");
        sb.append(this.mssResult);
        sb.append("\n mode:");
        sb.append(this.mssMode);
        sb.append("\n state:");
        sb.append(this.mssState);
        sb.append("\n vapnum:");
        sb.append(this.vapNum);
        for (int i = 0; i < this.mssVapList.length; i++) {
            sb.append("\n***************:");
            sb.append("\n    actiontype:");
            sb.append(this.mssVapList[i].actionType);
            sb.append("\n    m2sResult:");
            sb.append(this.mssVapList[i].mssResult);
            String macaddr = String.format("%02x:%02x:%02x:ff:ff:%02x", new Object[]{Byte.valueOf(this.mssVapList[i].userMacAddr[0]), Byte.valueOf(this.mssVapList[i].userMacAddr[1]), Byte.valueOf(this.mssVapList[i].userMacAddr[2]), Byte.valueOf(this.mssVapList[i].userMacAddr[5])});
            sb.append("\n    mac:");
            sb.append(macaddr);
        }
        return sb.toString();
    }
}
