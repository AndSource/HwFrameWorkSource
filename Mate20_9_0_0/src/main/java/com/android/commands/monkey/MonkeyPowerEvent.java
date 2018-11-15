package com.android.commands.monkey;

import android.app.IActivityManager;
import android.content.ContentValues;
import android.os.Build;
import android.util.Log;
import android.view.IWindowManager;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MonkeyPowerEvent extends MonkeyEvent {
    private static final String LOG_FILE = "/sdcard/autotester.log";
    private static final String TAG = "PowerTester";
    private static final String TEST_DELAY_STARTED = "AUTOTEST_TEST_BEGIN_DELAY";
    private static final String TEST_ENDED = "AUTOTEST_TEST_SUCCESS";
    private static final String TEST_IDLE_ENDED = "AUTOTEST_IDLE_SUCCESS";
    private static final String TEST_SEQ_BEGIN = "AUTOTEST_SEQUENCE_BEGIN";
    private static final String TEST_STARTED = "AUTOTEST_TEST_BEGIN";
    private static final long USB_DELAY_TIME = 10000;
    private static ArrayList<ContentValues> mLogEvents = new ArrayList();
    private static long mTestStartTime;
    private String mPowerLogTag;
    private String mTestResult;

    public MonkeyPowerEvent(String powerLogTag, String powerTestResult) {
        super(4);
        this.mPowerLogTag = powerLogTag;
        this.mTestResult = powerTestResult;
    }

    public MonkeyPowerEvent(String powerLogTag) {
        super(4);
        this.mPowerLogTag = powerLogTag;
        this.mTestResult = null;
    }

    public MonkeyPowerEvent() {
        super(4);
        this.mPowerLogTag = null;
        this.mTestResult = null;
    }

    private void bufferLogEvent(String tag, String value) {
        long tagTime = System.currentTimeMillis();
        if (tag.compareTo(TEST_STARTED) == 0) {
            mTestStartTime = tagTime;
        } else if (tag.compareTo(TEST_IDLE_ENDED) == 0) {
            tagTime = mTestStartTime + Long.parseLong(value);
            tag = TEST_ENDED;
        } else if (tag.compareTo(TEST_DELAY_STARTED) == 0) {
            mTestStartTime = USB_DELAY_TIME + tagTime;
            tagTime = mTestStartTime;
            tag = TEST_STARTED;
        }
        ContentValues event = new ContentValues();
        event.put("date", Long.valueOf(tagTime));
        event.put("tag", tag);
        if (value != null) {
            event.put("value", value);
        }
        mLogEvents.add(event);
    }

    private void writeLogEvents() {
        int i = 0;
        ContentValues[] events = (ContentValues[]) mLogEvents.toArray(new ContentValues[0]);
        mLogEvents.clear();
        FileWriter writer = null;
        try {
            StringBuffer buffer = new StringBuffer();
            while (i < events.length) {
                ContentValues event = events[i];
                buffer.append(MonkeyUtils.toCalendarTime(event.getAsLong("date").longValue()));
                buffer.append(event.getAsString("tag"));
                if (event.containsKey("value")) {
                    String value = event.getAsString("value");
                    buffer.append(" ");
                    buffer.append(value.replace(10, '/'));
                }
                buffer.append("\n");
                i++;
            }
            writer = new FileWriter(LOG_FILE, true);
            writer.write(buffer.toString());
            try {
                writer.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            Log.w(TAG, "Can't write sdcard log file", e2);
            if (writer != null) {
                writer.close();
            }
        } catch (Throwable th) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e3) {
                }
            }
        }
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (this.mPowerLogTag == null) {
            writeLogEvents();
        } else if (this.mPowerLogTag.compareTo(TEST_SEQ_BEGIN) == 0) {
            bufferLogEvent(this.mPowerLogTag, Build.FINGERPRINT);
        } else if (this.mTestResult != null) {
            bufferLogEvent(this.mPowerLogTag, this.mTestResult);
        }
        return 1;
    }
}
