package com.android.server.wifi.p2p;

import android.os.IHwBinder.DeathRecipient;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SupplicantP2pIfaceHal$Wvwk6xCSAknWmsVUgpUqV_3NQiE implements DeathRecipient {
    private final /* synthetic */ SupplicantP2pIfaceHal f$0;

    public /* synthetic */ -$$Lambda$SupplicantP2pIfaceHal$Wvwk6xCSAknWmsVUgpUqV_3NQiE(SupplicantP2pIfaceHal supplicantP2pIfaceHal) {
        this.f$0 = supplicantP2pIfaceHal;
    }

    public final void serviceDied(long j) {
        SupplicantP2pIfaceHal.lambda$new$0(this.f$0, j);
    }
}
