package com.android.server.pm;

import java.util.function.Consumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ShortcutUser$bsc89E_40a5X2amehalpqawQ5hY implements Consumer {
    public static final /* synthetic */ -$$Lambda$ShortcutUser$bsc89E_40a5X2amehalpqawQ5hY INSTANCE = new -$$Lambda$ShortcutUser$bsc89E_40a5X2amehalpqawQ5hY();

    private /* synthetic */ -$$Lambda$ShortcutUser$bsc89E_40a5X2amehalpqawQ5hY() {
    }

    public final void accept(Object obj) {
        ((ShortcutPackageItem) obj).attemptToRestoreIfNeededAndSave();
    }
}
