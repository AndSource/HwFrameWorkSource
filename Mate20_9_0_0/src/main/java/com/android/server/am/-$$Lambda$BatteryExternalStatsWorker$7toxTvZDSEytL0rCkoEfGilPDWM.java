package com.android.server.am;

import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.util.function.TriConsumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BatteryExternalStatsWorker$7toxTvZDSEytL0rCkoEfGilPDWM implements TriConsumer {
    public static final /* synthetic */ -$$Lambda$BatteryExternalStatsWorker$7toxTvZDSEytL0rCkoEfGilPDWM INSTANCE = new -$$Lambda$BatteryExternalStatsWorker$7toxTvZDSEytL0rCkoEfGilPDWM();

    private /* synthetic */ -$$Lambda$BatteryExternalStatsWorker$7toxTvZDSEytL0rCkoEfGilPDWM() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((BatteryStatsImpl) obj).copyFromAllUidsCpuTimes(((Boolean) obj2).booleanValue(), ((Boolean) obj3).booleanValue());
    }
}
