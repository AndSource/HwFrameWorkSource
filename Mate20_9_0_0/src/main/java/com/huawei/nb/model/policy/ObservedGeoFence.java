package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ObservedGeoFence extends AManagedObject {
    public static final Creator<ObservedGeoFence> CREATOR = new Creator<ObservedGeoFence>() {
        public ObservedGeoFence createFromParcel(Parcel in) {
            return new ObservedGeoFence(in);
        }

        public ObservedGeoFence[] newArray(int size) {
            return new ObservedGeoFence[size];
        }
    };
    private String mCategory;
    private String mFenceID;
    private String mGeoValue;
    private Long mID;
    private String mMaxTriggersPerDay;
    private String mName;
    private String mReserve;
    private String mSameFenceMaxTriggersPerDay;
    private String mSameFenceMinTriggerInterval;
    private Integer mShape;
    private Short mStatus;
    private String mSubCategory;
    private String mWorkTime;

    public ObservedGeoFence(Cursor cursor) {
        Short sh = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mID = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.mFenceID = cursor.getString(2);
        this.mName = cursor.getString(3);
        this.mCategory = cursor.getString(4);
        this.mSubCategory = cursor.getString(5);
        this.mShape = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mGeoValue = cursor.getString(7);
        if (!cursor.isNull(8)) {
            sh = Short.valueOf(cursor.getShort(8));
        }
        this.mStatus = sh;
        this.mReserve = cursor.getString(9);
        this.mWorkTime = cursor.getString(10);
        this.mSameFenceMaxTriggersPerDay = cursor.getString(11);
        this.mSameFenceMinTriggerInterval = cursor.getString(12);
        this.mMaxTriggersPerDay = cursor.getString(13);
    }

    public ObservedGeoFence(Parcel in) {
        String str = null;
        super(in);
        if (in.readByte() == (byte) 0) {
            this.mID = null;
            in.readLong();
        } else {
            this.mID = Long.valueOf(in.readLong());
        }
        this.mFenceID = in.readByte() == (byte) 0 ? null : in.readString();
        this.mName = in.readByte() == (byte) 0 ? null : in.readString();
        this.mCategory = in.readByte() == (byte) 0 ? null : in.readString();
        this.mSubCategory = in.readByte() == (byte) 0 ? null : in.readString();
        this.mShape = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mGeoValue = in.readByte() == (byte) 0 ? null : in.readString();
        this.mStatus = in.readByte() == (byte) 0 ? null : Short.valueOf((short) in.readInt());
        this.mReserve = in.readByte() == (byte) 0 ? null : in.readString();
        this.mWorkTime = in.readByte() == (byte) 0 ? null : in.readString();
        this.mSameFenceMaxTriggersPerDay = in.readByte() == (byte) 0 ? null : in.readString();
        this.mSameFenceMinTriggerInterval = in.readByte() == (byte) 0 ? null : in.readString();
        if (in.readByte() != (byte) 0) {
            str = in.readString();
        }
        this.mMaxTriggersPerDay = str;
    }

    private ObservedGeoFence(Long mID, String mFenceID, String mName, String mCategory, String mSubCategory, Integer mShape, String mGeoValue, Short mStatus, String mReserve, String mWorkTime, String mSameFenceMaxTriggersPerDay, String mSameFenceMinTriggerInterval, String mMaxTriggersPerDay) {
        this.mID = mID;
        this.mFenceID = mFenceID;
        this.mName = mName;
        this.mCategory = mCategory;
        this.mSubCategory = mSubCategory;
        this.mShape = mShape;
        this.mGeoValue = mGeoValue;
        this.mStatus = mStatus;
        this.mReserve = mReserve;
        this.mWorkTime = mWorkTime;
        this.mSameFenceMaxTriggersPerDay = mSameFenceMaxTriggersPerDay;
        this.mSameFenceMinTriggerInterval = mSameFenceMinTriggerInterval;
        this.mMaxTriggersPerDay = mMaxTriggersPerDay;
    }

    public int describeContents() {
        return 0;
    }

    public Long getMID() {
        return this.mID;
    }

    public void setMID(Long mID) {
        this.mID = mID;
        setValue();
    }

    public String getMFenceID() {
        return this.mFenceID;
    }

    public void setMFenceID(String mFenceID) {
        this.mFenceID = mFenceID;
        setValue();
    }

    public String getMName() {
        return this.mName;
    }

    public void setMName(String mName) {
        this.mName = mName;
        setValue();
    }

    public String getMCategory() {
        return this.mCategory;
    }

    public void setMCategory(String mCategory) {
        this.mCategory = mCategory;
        setValue();
    }

    public String getMSubCategory() {
        return this.mSubCategory;
    }

    public void setMSubCategory(String mSubCategory) {
        this.mSubCategory = mSubCategory;
        setValue();
    }

    public Integer getMShape() {
        return this.mShape;
    }

    public void setMShape(Integer mShape) {
        this.mShape = mShape;
        setValue();
    }

    public String getMGeoValue() {
        return this.mGeoValue;
    }

    public void setMGeoValue(String mGeoValue) {
        this.mGeoValue = mGeoValue;
        setValue();
    }

    public Short getMStatus() {
        return this.mStatus;
    }

    public void setMStatus(Short mStatus) {
        this.mStatus = mStatus;
        setValue();
    }

    public String getMReserve() {
        return this.mReserve;
    }

    public void setMReserve(String mReserve) {
        this.mReserve = mReserve;
        setValue();
    }

    public String getMWorkTime() {
        return this.mWorkTime;
    }

    public void setMWorkTime(String mWorkTime) {
        this.mWorkTime = mWorkTime;
        setValue();
    }

    public String getMSameFenceMaxTriggersPerDay() {
        return this.mSameFenceMaxTriggersPerDay;
    }

    public void setMSameFenceMaxTriggersPerDay(String mSameFenceMaxTriggersPerDay) {
        this.mSameFenceMaxTriggersPerDay = mSameFenceMaxTriggersPerDay;
        setValue();
    }

    public String getMSameFenceMinTriggerInterval() {
        return this.mSameFenceMinTriggerInterval;
    }

    public void setMSameFenceMinTriggerInterval(String mSameFenceMinTriggerInterval) {
        this.mSameFenceMinTriggerInterval = mSameFenceMinTriggerInterval;
        setValue();
    }

    public String getMMaxTriggersPerDay() {
        return this.mMaxTriggersPerDay;
    }

    public void setMMaxTriggersPerDay(String mMaxTriggersPerDay) {
        this.mMaxTriggersPerDay = mMaxTriggersPerDay;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mID != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mID.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.mFenceID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mFenceID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCategory != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCategory);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSubCategory != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSubCategory);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mShape != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mShape.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mGeoValue != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mGeoValue);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mStatus != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mStatus.shortValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReserve != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReserve);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWorkTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mWorkTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSameFenceMaxTriggersPerDay != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSameFenceMaxTriggersPerDay);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSameFenceMinTriggerInterval != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSameFenceMinTriggerInterval);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMaxTriggersPerDay != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mMaxTriggersPerDay);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ObservedGeoFence> getHelper() {
        return ObservedGeoFenceHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.policy.ObservedGeoFence";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ObservedGeoFence { mID: ").append(this.mID);
        sb.append(", mFenceID: ").append(this.mFenceID);
        sb.append(", mName: ").append(this.mName);
        sb.append(", mCategory: ").append(this.mCategory);
        sb.append(", mSubCategory: ").append(this.mSubCategory);
        sb.append(", mShape: ").append(this.mShape);
        sb.append(", mGeoValue: ").append(this.mGeoValue);
        sb.append(", mStatus: ").append(this.mStatus);
        sb.append(", mReserve: ").append(this.mReserve);
        sb.append(", mWorkTime: ").append(this.mWorkTime);
        sb.append(", mSameFenceMaxTriggersPerDay: ").append(this.mSameFenceMaxTriggersPerDay);
        sb.append(", mSameFenceMinTriggerInterval: ").append(this.mSameFenceMinTriggerInterval);
        sb.append(", mMaxTriggersPerDay: ").append(this.mMaxTriggersPerDay);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }
}
