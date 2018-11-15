package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;
import java.util.Objects;

public class SingleScanSettings implements Parcelable {
    public static final Creator<SingleScanSettings> CREATOR = new Creator<SingleScanSettings>() {
        public SingleScanSettings createFromParcel(Parcel in) {
            SingleScanSettings result = new SingleScanSettings();
            result.scanType = in.readInt();
            if (!SingleScanSettings.isValidScanType(result.scanType)) {
                String str = SingleScanSettings.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Invalid scan type ");
                stringBuilder.append(result.scanType);
                Log.wtf(str, stringBuilder.toString());
            }
            result.channelSettings = new ArrayList();
            in.readTypedList(result.channelSettings, ChannelSettings.CREATOR);
            result.hiddenNetworks = new ArrayList();
            in.readTypedList(result.hiddenNetworks, HiddenNetwork.CREATOR);
            if (in.dataAvail() != 0) {
                Log.e(SingleScanSettings.TAG, "Found trailing data after parcel parsing.");
            }
            return result;
        }

        public SingleScanSettings[] newArray(int size) {
            return new SingleScanSettings[size];
        }
    };
    private static final String TAG = "SingleScanSettings";
    public ArrayList<ChannelSettings> channelSettings;
    public ArrayList<HiddenNetwork> hiddenNetworks;
    public int scanType;

    public boolean equals(Object rhs) {
        boolean z = true;
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof SingleScanSettings)) {
            return false;
        }
        SingleScanSettings settings = (SingleScanSettings) rhs;
        if (settings == null) {
            return false;
        }
        if (!(this.scanType == settings.scanType && this.channelSettings.equals(settings.channelSettings) && this.hiddenNetworks.equals(settings.hiddenNetworks))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.scanType), this.channelSettings, this.hiddenNetworks});
    }

    public int describeContents() {
        return 0;
    }

    private static boolean isValidScanType(int scanType) {
        return scanType == 0 || scanType == 1 || scanType == 2;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (!isValidScanType(this.scanType)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid scan type ");
            stringBuilder.append(this.scanType);
            Log.wtf(str, stringBuilder.toString());
        }
        out.writeInt(this.scanType);
        out.writeTypedList(this.channelSettings);
        out.writeTypedList(this.hiddenNetworks);
    }
}
