package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Authority extends AManagedObject {
    public static final Creator<Authority> CREATOR = new Creator<Authority>() {
        public Authority createFromParcel(Parcel in) {
            return new Authority(in);
        }

        public Authority[] newArray(int size) {
            return new Authority[size];
        }
    };
    private String column0;
    private String column1;
    private String column2;
    private String column3;
    private String column4;
    private String column5;
    private String entity;
    private Integer id;
    private String right;
    private Integer type;

    public Authority(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        if (!cursor.isNull(2)) {
            num = Integer.valueOf(cursor.getInt(2));
        }
        this.type = num;
        this.entity = cursor.getString(3);
        this.right = cursor.getString(4);
        this.column0 = cursor.getString(5);
        this.column1 = cursor.getString(6);
        this.column2 = cursor.getString(7);
        this.column3 = cursor.getString(8);
        this.column4 = cursor.getString(9);
        this.column5 = cursor.getString(10);
    }

    public Authority(Parcel in) {
        String str = null;
        super(in);
        if (in.readByte() == (byte) 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.type = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.entity = in.readByte() == (byte) 0 ? null : in.readString();
        this.right = in.readByte() == (byte) 0 ? null : in.readString();
        this.column0 = in.readByte() == (byte) 0 ? null : in.readString();
        this.column1 = in.readByte() == (byte) 0 ? null : in.readString();
        this.column2 = in.readByte() == (byte) 0 ? null : in.readString();
        this.column3 = in.readByte() == (byte) 0 ? null : in.readString();
        this.column4 = in.readByte() == (byte) 0 ? null : in.readString();
        if (in.readByte() != (byte) 0) {
            str = in.readString();
        }
        this.column5 = str;
    }

    private Authority(Integer id, Integer type, String entity, String right, String column0, String column1, String column2, String column3, String column4, String column5) {
        this.id = id;
        this.type = type;
        this.entity = entity;
        this.right = right;
        this.column0 = column0;
        this.column1 = column1;
        this.column2 = column2;
        this.column3 = column3;
        this.column4 = column4;
        this.column5 = column5;
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
        setValue();
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
        setValue();
    }

    public String getEntity() {
        return this.entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
        setValue();
    }

    public String getRight() {
        return this.right;
    }

    public void setRight(String right) {
        this.right = right;
        setValue();
    }

    public String getColumn0() {
        return this.column0;
    }

    public void setColumn0(String column0) {
        this.column0 = column0;
        setValue();
    }

    public String getColumn1() {
        return this.column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
        setValue();
    }

    public String getColumn2() {
        return this.column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
        setValue();
    }

    public String getColumn3() {
        return this.column3;
    }

    public void setColumn3(String column3) {
        this.column3 = column3;
        setValue();
    }

    public String getColumn4() {
        return this.column4;
    }

    public void setColumn4(String column4) {
        this.column4 = column4;
        setValue();
    }

    public String getColumn5() {
        return this.column5;
    }

    public void setColumn5(String column5) {
        this.column5 = column5;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.type != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.type.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.entity != null) {
            out.writeByte((byte) 1);
            out.writeString(this.entity);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.right != null) {
            out.writeByte((byte) 1);
            out.writeString(this.right);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.column0 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.column0);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.column1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.column1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.column2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.column2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.column3 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.column3);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.column4 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.column4);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.column5 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.column5);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<Authority> getHelper() {
        return AuthorityHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.pengine.Authority";
    }

    public String getDatabaseName() {
        return "dsPengineData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Authority { id: ").append(this.id);
        sb.append(", type: ").append(this.type);
        sb.append(", entity: ").append(this.entity);
        sb.append(", right: ").append(this.right);
        sb.append(", column0: ").append(this.column0);
        sb.append(", column1: ").append(this.column1);
        sb.append(", column2: ").append(this.column2);
        sb.append(", column3: ").append(this.column3);
        sb.append(", column4: ").append(this.column4);
        sb.append(", column5: ").append(this.column5);
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
        return "0.0.7";
    }

    public int getDatabaseVersionCode() {
        return 7;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
