package com.android.server.backup.restore;

import android.os.PowerManager.WakeLock;
import com.android.server.backup.TransportManager;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.transport.TransportClient;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ActiveRestoreSession$0wzV_GqtA0thM1WxLthNBKD3Ygw implements OnTaskFinishedListener {
    private final /* synthetic */ TransportManager f$0;
    private final /* synthetic */ TransportClient f$1;
    private final /* synthetic */ WakeLock f$2;

    public /* synthetic */ -$$Lambda$ActiveRestoreSession$0wzV_GqtA0thM1WxLthNBKD3Ygw(TransportManager transportManager, TransportClient transportClient, WakeLock wakeLock) {
        this.f$0 = transportManager;
        this.f$1 = transportClient;
        this.f$2 = wakeLock;
    }

    public final void onFinished(String str) {
        ActiveRestoreSession.lambda$getAvailableRestoreSets$0(this.f$0, this.f$1, this.f$2, str);
    }
}
