package com.android.server.print;

import android.print.IPrinterDiscoveryObserver;
import com.android.internal.util.function.TriConsumer;
import java.util.ArrayList;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$UserState$PrinterDiscoverySessionMediator$vhz2AcQkYu3SdMlMt9bsncMGW7E implements TriConsumer {
    public static final /* synthetic */ -$$Lambda$UserState$PrinterDiscoverySessionMediator$vhz2AcQkYu3SdMlMt9bsncMGW7E INSTANCE = new -$$Lambda$UserState$PrinterDiscoverySessionMediator$vhz2AcQkYu3SdMlMt9bsncMGW7E();

    private /* synthetic */ -$$Lambda$UserState$PrinterDiscoverySessionMediator$vhz2AcQkYu3SdMlMt9bsncMGW7E() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((PrinterDiscoverySessionMediator) obj).handlePrintersAdded((IPrinterDiscoveryObserver) obj2, (ArrayList) obj3);
    }
}
