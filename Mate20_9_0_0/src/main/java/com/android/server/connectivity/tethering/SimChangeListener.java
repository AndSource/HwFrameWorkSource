package com.android.server.connectivity.tethering;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.util.VersionedBroadcastListener;
import android.os.Handler;
import android.util.Log;
import java.util.function.Consumer;

public class SimChangeListener extends VersionedBroadcastListener {
    private static final boolean DBG = false;
    private static final String TAG = SimChangeListener.class.getSimpleName();

    public SimChangeListener(Context ctx, Handler handler, Runnable onSimCardLoadedCallback) {
        super(TAG, ctx, handler, makeIntentFilter(), makeCallback(onSimCardLoadedCallback));
    }

    private static IntentFilter makeIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        return filter;
    }

    private static Consumer<Intent> makeCallback(final Runnable onSimCardLoadedCallback) {
        return new Consumer<Intent>() {
            private boolean mSimNotLoadedSeen = false;

            public void accept(Intent intent) {
                String state = intent.getStringExtra("ss");
                String access$000 = SimChangeListener.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("got Sim changed to state ");
                stringBuilder.append(state);
                stringBuilder.append(", mSimNotLoadedSeen=");
                stringBuilder.append(this.mSimNotLoadedSeen);
                Log.d(access$000, stringBuilder.toString());
                if ("LOADED".equals(state)) {
                    if (this.mSimNotLoadedSeen) {
                        this.mSimNotLoadedSeen = false;
                        onSimCardLoadedCallback.run();
                    }
                    return;
                }
                this.mSimNotLoadedSeen = true;
            }
        };
    }
}
