package com.android.server.print;

import java.util.function.Consumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$UserState$LdWYUAKz4cbWqoxOD4oZ_ZslKdg implements Consumer {
    public static final /* synthetic */ -$$Lambda$UserState$LdWYUAKz4cbWqoxOD4oZ_ZslKdg INSTANCE = new -$$Lambda$UserState$LdWYUAKz4cbWqoxOD4oZ_ZslKdg();

    private /* synthetic */ -$$Lambda$UserState$LdWYUAKz4cbWqoxOD4oZ_ZslKdg() {
    }

    public final void accept(Object obj) {
        ((UserState) obj).handleDispatchPrintServicesChanged();
    }
}
