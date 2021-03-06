package com.android.server.notification;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.provider.Settings.Secure;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.ScheduleCalendar;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.notification.NotificationManagerService.DumpFilter;
import com.android.server.pm.PackageManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleConditionProvider extends SystemConditionProviderService {
    private static final String ACTION_EVALUATE;
    public static final ComponentName COMPONENT = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, ScheduleConditionProvider.class.getName());
    static final boolean DEBUG = true;
    private static final String EXTRA_TIME = "time";
    private static final String NOT_SHOWN = "...";
    private static final int REQUEST_CODE_EVALUATE = 1;
    private static final String SCP_SETTING = "snoozed_schedule_condition_provider";
    private static final String SEPARATOR = ";";
    private static final String SIMPLE_NAME = ScheduleConditionProvider.class.getSimpleName();
    static final String TAG = "ConditionProviders.SCP";
    private AlarmManager mAlarmManager;
    private boolean mConnected;
    private final Context mContext = this;
    private long mNextAlarmTime;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ScheduleConditionProvider.DEBUG) {
                String str = ScheduleConditionProvider.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("onReceive ");
                stringBuilder.append(intent.getAction());
                Slog.d(str, stringBuilder.toString());
            }
            if ("android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                synchronized (ScheduleConditionProvider.this.mSubscriptions) {
                    for (Uri conditionId : ScheduleConditionProvider.this.mSubscriptions.keySet()) {
                        ScheduleCalendar cal = (ScheduleCalendar) ScheduleConditionProvider.this.mSubscriptions.get(conditionId);
                        if (cal != null) {
                            cal.setTimeZone(Calendar.getInstance().getTimeZone());
                        }
                    }
                }
            }
            ScheduleConditionProvider.this.evaluateSubscriptions();
        }
    };
    private boolean mRegistered;
    private ArraySet<Uri> mSnoozedForAlarm = new ArraySet();
    private final ArrayMap<Uri, ScheduleCalendar> mSubscriptions = new ArrayMap();

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SIMPLE_NAME);
        stringBuilder.append(".EVALUATE");
        ACTION_EVALUATE = stringBuilder.toString();
    }

    public ScheduleConditionProvider() {
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("new ");
            stringBuilder.append(SIMPLE_NAME);
            stringBuilder.append("()");
            Slog.d(str, stringBuilder.toString());
        }
    }

    public ComponentName getComponent() {
        return COMPONENT;
    }

    public boolean isValidConditionId(Uri id) {
        return ZenModeConfig.isValidScheduleConditionId(id);
    }

    public void dump(PrintWriter pw, DumpFilter filter) {
        pw.print("    ");
        pw.print(SIMPLE_NAME);
        pw.println(":");
        pw.print("      mConnected=");
        pw.println(this.mConnected);
        pw.print("      mRegistered=");
        pw.println(this.mRegistered);
        pw.println("      mSubscriptions=");
        long now = System.currentTimeMillis();
        synchronized (this.mSubscriptions) {
            for (Uri conditionId : this.mSubscriptions.keySet()) {
                pw.print("        ");
                pw.print(meetsSchedule((ScheduleCalendar) this.mSubscriptions.get(conditionId), now) ? "* " : "  ");
                pw.println(conditionId);
                pw.print("            ");
                pw.println(((ScheduleCalendar) this.mSubscriptions.get(conditionId)).toString());
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("      snoozed due to alarm: ");
        stringBuilder.append(TextUtils.join(SEPARATOR, this.mSnoozedForAlarm));
        pw.println(stringBuilder.toString());
        SystemConditionProviderService.dumpUpcomingTime(pw, "mNextAlarmTime", this.mNextAlarmTime, now);
    }

    public void onConnected() {
        if (DEBUG) {
            Slog.d(TAG, "onConnected");
        }
        this.mConnected = true;
        readSnoozed();
    }

    public void onBootComplete() {
    }

    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Slog.d(TAG, "onDestroy");
        }
        this.mConnected = false;
    }

    public void onSubscribe(Uri conditionId) {
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onSubscribe ");
            stringBuilder.append(conditionId);
            Slog.d(str, stringBuilder.toString());
        }
        if (ZenModeConfig.isValidScheduleConditionId(conditionId)) {
            synchronized (this.mSubscriptions) {
                this.mSubscriptions.put(conditionId, ZenModeConfig.toScheduleCalendar(conditionId));
            }
            evaluateSubscriptions();
            return;
        }
        notifyCondition(createCondition(conditionId, 3, "invalidId"));
    }

    public void onUnsubscribe(Uri conditionId) {
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onUnsubscribe ");
            stringBuilder.append(conditionId);
            Slog.d(str, stringBuilder.toString());
        }
        synchronized (this.mSubscriptions) {
            this.mSubscriptions.remove(conditionId);
        }
        removeSnoozed(conditionId);
        evaluateSubscriptions();
    }

    public void attachBase(Context base) {
        attachBaseContext(base);
    }

    public IConditionProvider asInterface() {
        return (IConditionProvider) onBind(null);
    }

    private void evaluateSubscriptions() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        }
        long now = System.currentTimeMillis();
        this.mNextAlarmTime = 0;
        long nextUserAlarmTime = getNextAlarm();
        List<Condition> conditionsToNotify = new ArrayList();
        synchronized (this.mSubscriptions) {
            setRegistered(this.mSubscriptions.isEmpty() ^ 1);
            for (Uri conditionId : this.mSubscriptions.keySet()) {
                Condition condition = evaluateSubscriptionLocked(conditionId, (ScheduleCalendar) this.mSubscriptions.get(conditionId), now, nextUserAlarmTime);
                if (condition != null) {
                    conditionsToNotify.add(condition);
                }
            }
        }
        notifyConditions((Condition[]) conditionsToNotify.toArray(new Condition[conditionsToNotify.size()]));
        updateAlarm(now, this.mNextAlarmTime);
    }

    @GuardedBy("mSubscriptions")
    @VisibleForTesting
    Condition evaluateSubscriptionLocked(Uri conditionId, ScheduleCalendar cal, long now, long nextUserAlarmTime) {
        if (DEBUG) {
            Slog.d(TAG, String.format("evaluateSubscriptionLocked cal=%s, now=%s, nextUserAlarmTime=%s", new Object[]{cal, SystemConditionProviderService.ts(now), SystemConditionProviderService.ts(nextUserAlarmTime)}));
        }
        Condition condition;
        if (cal == null) {
            condition = createCondition(conditionId, 3, "!invalidId");
            removeSnoozed(conditionId);
            return condition;
        }
        if (!cal.isInSchedule(now)) {
            condition = createCondition(conditionId, 0, "!meetsSchedule");
            removeSnoozed(conditionId);
        } else if (conditionSnoozed(conditionId)) {
            condition = createCondition(conditionId, 0, "snoozed");
        } else if (cal.shouldExitForAlarm(now)) {
            condition = createCondition(conditionId, 0, "alarmCanceled");
            addSnoozed(conditionId);
        } else {
            condition = createCondition(conditionId, 1, "meetsSchedule");
        }
        cal.maybeSetNextAlarm(now, nextUserAlarmTime);
        long nextChangeTime = cal.getNextChangeTime(now);
        if (nextChangeTime > 0 && nextChangeTime > now && (this.mNextAlarmTime == 0 || nextChangeTime < this.mNextAlarmTime)) {
            this.mNextAlarmTime = nextChangeTime;
        }
        return condition;
    }

    private void updateAlarm(long now, long time) {
        AlarmManager alarms = (AlarmManager) this.mContext.getSystemService("alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 1, new Intent(ACTION_EVALUATE).addFlags(268435456).putExtra(EXTRA_TIME, time), 134217728);
        alarms.cancel(pendingIntent);
        if (time > now) {
            if (DEBUG) {
                Slog.d(TAG, String.format("Scheduling evaluate for %s, in %s, now=%s", new Object[]{SystemConditionProviderService.ts(time), SystemConditionProviderService.formatDuration(time - now), SystemConditionProviderService.ts(now)}));
            }
            alarms.setExact(0, time, pendingIntent);
        } else if (DEBUG) {
            Slog.d(TAG, "Not scheduling evaluate");
        }
    }

    public long getNextAlarm() {
        AlarmClockInfo info = this.mAlarmManager.getNextAlarmClock(ActivityManager.getCurrentUser());
        return info != null ? info.getTriggerTime() : 0;
    }

    private boolean meetsSchedule(ScheduleCalendar cal, long time) {
        return cal != null && cal.isInSchedule(time);
    }

    private void setRegistered(boolean registered) {
        if (this.mRegistered != registered) {
            if (DEBUG) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("setRegistered ");
                stringBuilder.append(registered);
                Slog.d(str, stringBuilder.toString());
            }
            this.mRegistered = registered;
            if (this.mRegistered) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.TIME_SET");
                filter.addAction("android.intent.action.TIMEZONE_CHANGED");
                filter.addAction(ACTION_EVALUATE);
                filter.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
                filter.addAction("android.intent.action.USER_SWITCHED");
                registerReceiver(this.mReceiver, filter);
            } else {
                unregisterReceiver(this.mReceiver);
            }
        }
    }

    private Condition createCondition(Uri id, int state, String reason) {
        String str;
        if (DEBUG) {
            str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("notifyCondition ");
            stringBuilder.append(id);
            stringBuilder.append(" ");
            stringBuilder.append(Condition.stateToString(state));
            stringBuilder.append(" reason=");
            stringBuilder.append(reason);
            Slog.d(str, stringBuilder.toString());
        }
        str = NOT_SHOWN;
        String line1 = NOT_SHOWN;
        String line2 = NOT_SHOWN;
        return new Condition(id, NOT_SHOWN, NOT_SHOWN, NOT_SHOWN, 0, state, 2);
    }

    private boolean conditionSnoozed(Uri conditionId) {
        boolean contains;
        synchronized (this.mSnoozedForAlarm) {
            contains = this.mSnoozedForAlarm.contains(conditionId);
        }
        return contains;
    }

    @VisibleForTesting
    void addSnoozed(Uri conditionId) {
        synchronized (this.mSnoozedForAlarm) {
            this.mSnoozedForAlarm.add(conditionId);
            saveSnoozedLocked();
        }
    }

    private void removeSnoozed(Uri conditionId) {
        synchronized (this.mSnoozedForAlarm) {
            this.mSnoozedForAlarm.remove(conditionId);
            saveSnoozedLocked();
        }
    }

    private void saveSnoozedLocked() {
        Secure.putStringForUser(this.mContext.getContentResolver(), SCP_SETTING, TextUtils.join(SEPARATOR, this.mSnoozedForAlarm), ActivityManager.getCurrentUser());
    }

    private void readSnoozed() {
        synchronized (this.mSnoozedForAlarm) {
            long identity = Binder.clearCallingIdentity();
            try {
                String setting = Secure.getStringForUser(this.mContext.getContentResolver(), SCP_SETTING, ActivityManager.getCurrentUser());
                if (setting != null) {
                    String[] tokens = setting.split(SEPARATOR);
                    for (String token : tokens) {
                        String token2;
                        if (token2 != null) {
                            token2 = token2.trim();
                        }
                        if (!TextUtils.isEmpty(token2)) {
                            this.mSnoozedForAlarm.add(Uri.parse(token2));
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }
}
