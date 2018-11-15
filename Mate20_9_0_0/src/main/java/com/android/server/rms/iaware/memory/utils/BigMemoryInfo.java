package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;

public class BigMemoryInfo {
    private static final String TAG = "AwareMem_BigMemConfig";
    private static final Object mLock = new Object();
    private static BigMemoryInfo sBigMemoryInfo;
    private ArrayMap<String, Long> memoryRequestMap;

    public static BigMemoryInfo getInstance() {
        BigMemoryInfo bigMemoryInfo;
        synchronized (mLock) {
            if (sBigMemoryInfo == null) {
                sBigMemoryInfo = new BigMemoryInfo();
            }
            bigMemoryInfo = sBigMemoryInfo;
        }
        return bigMemoryInfo;
    }

    private BigMemoryInfo() {
        this.memoryRequestMap = null;
        this.memoryRequestMap = new ArrayMap();
    }

    public boolean isBigMemoryApp(String appName) {
        synchronized (this) {
            if (this.memoryRequestMap == null || appName == null || this.memoryRequestMap.isEmpty()) {
                return false;
            }
            boolean containsKey = this.memoryRequestMap.containsKey(appName);
            return containsKey;
        }
    }

    public long getAppLaunchRequestMemory(String appName) {
        synchronized (this) {
            if (this.memoryRequestMap == null || appName == null || this.memoryRequestMap.isEmpty() || !this.memoryRequestMap.containsKey(appName)) {
                return 0;
            }
            long longValue = ((Long) this.memoryRequestMap.get(appName)).longValue();
            return longValue;
        }
    }

    public void resetLaunchMemConfig() {
        synchronized (this) {
            if (this.memoryRequestMap == null) {
                this.memoryRequestMap = new ArrayMap();
            }
            this.memoryRequestMap.clear();
        }
    }

    public void setRequestMemForLaunch(String appName, long launchRequestMem) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setRequestMemForLaunch appname is ");
        stringBuilder.append(appName);
        AwareLog.d(str, stringBuilder.toString());
        synchronized (this) {
            if (appName == null) {
                return;
            }
            if (this.memoryRequestMap == null) {
                this.memoryRequestMap = new ArrayMap();
            }
            this.memoryRequestMap.put(appName, Long.valueOf(launchRequestMem));
        }
    }

    public void removeRequestMemForLaunch(String appName) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("removeRequestMemForLaunch appname is ");
        stringBuilder.append(appName);
        AwareLog.d(str, stringBuilder.toString());
        synchronized (this) {
            if (!(this.memoryRequestMap == null || appName == null)) {
                this.memoryRequestMap.remove(appName);
            }
        }
    }
}
