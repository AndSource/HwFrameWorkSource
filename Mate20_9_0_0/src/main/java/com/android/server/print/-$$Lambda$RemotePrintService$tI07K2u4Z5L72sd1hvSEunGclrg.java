package com.android.server.print;

import java.util.function.Consumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg implements Consumer {
    public static final /* synthetic */ -$$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg INSTANCE = new -$$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg();

    private /* synthetic */ -$$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg() {
    }

    public final void accept(Object obj) {
        ((RemotePrintService) obj).handleDestroy();
    }
}
