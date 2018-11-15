package com.android.server.mtm.iaware.brjob.controller;

import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

public class WidgetStatusController extends AwareStateController {
    private static final String CONDITION_NAME = "WidgetStatus";
    private static final String TAG = "WidgetStatusController";
    private static WidgetStatusController mSingleton;
    private static Object sCreationLock = new Object();
    private AwareAppAssociate mAwareAppAssociate = AwareAppAssociate.getInstance();
    @GuardedBy("mLock")
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList();

    public static WidgetStatusController get(AwareJobSchedulerService jms) {
        WidgetStatusController widgetStatusController;
        synchronized (sCreationLock) {
            if (mSingleton == null) {
                mSingleton = new WidgetStatusController(jms, jms.getContext(), jms.getLock());
            }
            widgetStatusController = mSingleton;
        }
        return widgetStatusController;
    }

    private WidgetStatusController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
    }

    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("WidgetStatus")) {
            String str;
            Set<String> widgetsPkg = this.mAwareAppAssociate.getWidgetsPkg();
            if (this.DEBUG) {
                str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("iaware_brjob, widgetsPkg: ");
                stringBuilder.append(widgetsPkg == null ? "null" : widgetsPkg);
                AwareLog.i(str, stringBuilder.toString());
            }
            str = job.getReceiverPkg();
            if (widgetsPkg == null || !widgetsPkg.contains(str)) {
                job.setSatisfied("WidgetStatus", false);
            } else {
                job.setSatisfied("WidgetStatus", true);
            }
            addJobLocked(this.mTrackedJobs, job);
        }
    }

    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("WidgetStatus")) {
            if (this.DEBUG) {
                AwareLog.i(TAG, "iaware_brjob stop tracking begin");
            }
            this.mTrackedJobs.remove(job);
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("    WidgetStatusController tracked job num: ");
            stringBuilder.append(this.mTrackedJobs.size());
            pw.println(stringBuilder.toString());
            Set<String> widgetsPkg = this.mAwareAppAssociate.getWidgetsPkg();
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("        now widget package: ");
            stringBuilder2.append(widgetsPkg);
            pw.println(stringBuilder2.toString());
        }
    }
}
