package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifiChip.getNanIfaceCallback;
import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.WifiStatus;
import android.util.MutableBoolean;
import android.util.MutableInt;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$HalDeviceManager$OTxRCq8TAZZlX8UFhmqaHcpXJYQ implements getNanIfaceCallback {
    private final /* synthetic */ HalDeviceManager f$0;
    private final /* synthetic */ MutableBoolean f$1;
    private final /* synthetic */ String f$2;
    private final /* synthetic */ WifiIfaceInfo[] f$3;
    private final /* synthetic */ MutableInt f$4;

    public /* synthetic */ -$$Lambda$HalDeviceManager$OTxRCq8TAZZlX8UFhmqaHcpXJYQ(HalDeviceManager halDeviceManager, MutableBoolean mutableBoolean, String str, WifiIfaceInfo[] wifiIfaceInfoArr, MutableInt mutableInt) {
        this.f$0 = halDeviceManager;
        this.f$1 = mutableBoolean;
        this.f$2 = str;
        this.f$3 = wifiIfaceInfoArr;
        this.f$4 = mutableInt;
    }

    public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
        HalDeviceManager.lambda$getAllChipInfo$17(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiNanIface);
    }
}
