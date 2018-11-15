package com.android.server.rms.iaware.hiber;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.os.BackgroundThread;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.feature.MemoryFeature2;
import com.android.server.rms.iaware.hiber.bean.AbsAppInfo;
import com.android.server.rms.iaware.hiber.bean.HiberAppInfo;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.hiber.constant.EReclaimResult;
import com.android.server.rms.iaware.hiber.util.AppHiberUtil;
import com.android.server.rms.iaware.memory.action.GpuCompressAction;
import com.android.server.rms.iaware.memory.utils.CpuReader;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.pgmng.plug.PGSdk.Sink;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AppHibernateTask {
    private static final AbsAppInfo INVALID_ABSAPPINFO = new AbsAppInfo(-1, "");
    private static final String TAG_PG = "AppHiber_Task";
    private static AppHibernateTask mAppHibernateTask = null;
    private static final Object mLock = new Object();
    private ArraySet<AbsAppInfo> frzHashSet = new ArraySet();
    private final Semaphore frzSemaphore = new Semaphore(1);
    private AppHibernateMgr mAppHiberMgr = AppHibernateMgr.getInstance();
    private Context mContext;
    private CpuReader mCpuReader = CpuReader.getInstance();
    private AbsAppInfo mCurFrontAbsApp = INVALID_ABSAPPINFO;
    private Handler mHiberEventHandler = new HiberHanldler(BackgroundThread.get().getLooper());
    private final AtomicBoolean mIsScreenOff = new AtomicBoolean(false);
    private int mLastInputEvent = 0;
    private long mLastInputTime = 0;
    private long mLastResEventTime = 0;
    private PGSdk mPGSdk = null;
    private ArrayMap<AbsAppInfo, ArraySet<HiberAppInfo>> mReclaimedRecordMap = new ArrayMap();
    private AbsAppInfo mReclaimingApp = INVALID_ABSAPPINFO;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private Sink mStateRecognitionListener = new Sink() {
        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            String str;
            String str2 = AppHibernateTask.TAG_PG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onStateChanged    Enter  ");
            if (eventType == 1) {
                str = "FRZ CallBK: ";
            } else {
                str = "THW CallBK: ";
            }
            stringBuilder.append(str);
            stringBuilder.append(", pkg[");
            stringBuilder.append(pkg);
            stringBuilder.append("], uid= ");
            stringBuilder.append(uid);
            stringBuilder.append(", pid= ");
            stringBuilder.append(pid);
            AwareLog.d(str2, stringBuilder.toString());
            if (stateType != 6) {
                str2 = AppHibernateTask.TAG_PG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("stateType");
                stringBuilder.append(stateType);
                stringBuilder.append(" != STATE_HIBERNATE, return");
                AwareLog.i(str2, stringBuilder.toString());
            } else if (AppHiberUtil.isStrEmpty(pkg)) {
                AwareLog.i(AppHibernateTask.TAG_PG, "null == pkg || pkg.trim().isEmpty()");
            } else if (AppHiberUtil.illegalUid(uid)) {
                str2 = AppHibernateTask.TAG_PG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("uid = ");
                stringBuilder.append(uid);
                stringBuilder.append(" not in the range of [10000,+)");
                AwareLog.i(str2, stringBuilder.toString());
            } else {
                AbsAppInfo keyVlaue = new AbsAppInfo(uid, pkg);
                if (1 == eventType) {
                    synchronized (AppHibernateTask.this.frzHashSet) {
                        AppHibernateTask.this.frzHashSet.add(keyVlaue);
                    }
                    AppHibernateTask.this.sendMsgToHiberEventHandler(eventType, 0);
                } else if (2 == eventType) {
                    synchronized (AppHibernateTask.this.frzHashSet) {
                        AppHibernateTask.this.frzHashSet.remove(keyVlaue);
                    }
                    AppHibernateTask.this.unFrozenInterrupt(keyVlaue, false);
                } else {
                    AwareLog.i(AppHibernateTask.TAG_PG, "eventType is not frozen/thawed, Neglect!");
                }
            }
        }
    };

    final class HiberHanldler extends Handler {
        public HiberHanldler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.w(AppHibernateTask.TAG_PG, "null == msg");
                return;
            }
            int i = msg.what;
            if (i == 1) {
                AppHibernateTask.this.frozenInhandleMsg();
            } else if (i != AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK) {
                switch (i) {
                    case AppHibernateCst.ETYPE_MSG_WHAT_CREATE /*90001*/:
                        AppHibernateTask.this.createInhandleMsg();
                        break;
                    case AppHibernateCst.ETYPE_MSG_WHAT_DESTORY /*90002*/:
                        AppHibernateTask.this.destoryInhandleMsg();
                        break;
                    default:
                        String str = AppHibernateTask.TAG_PG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("msg.what = ");
                        stringBuilder.append(msg.what);
                        stringBuilder.append("  is Invalid !");
                        AwareLog.w(str, stringBuilder.toString());
                        break;
                }
            } else {
                AppHibernateTask.this.getPGSdk();
            }
        }
    }

    public static AppHibernateTask getInstance() {
        AppHibernateTask appHibernateTask;
        synchronized (mLock) {
            if (mAppHibernateTask == null) {
                mAppHibernateTask = new AppHibernateTask();
            }
            appHibernateTask = mAppHibernateTask;
        }
        return appHibernateTask;
    }

    private AppHibernateTask() {
    }

    public void initBeforeCreate(Context context) {
        this.mContext = context;
    }

    public void create() {
        AwareLog.d(TAG_PG, "create  Enter");
        sendMsgToHiberEventHandler(AppHibernateCst.ETYPE_MSG_WHAT_CREATE, 0);
    }

    public void destory() {
        AwareLog.d(TAG_PG, "destory  Enter");
        sendMsgToHiberEventHandler(AppHibernateCst.ETYPE_MSG_WHAT_DESTORY, 0);
    }

    public int interruptReclaim(int uid, String pkgName, long timestamp) {
        if (!this.mRunning.get()) {
            AwareLog.w(TAG_PG, "interruptReclaim     failed  , because AppHibernateTask is not  enable");
            return -1;
        } else if (AppHiberUtil.illegalUid(uid) || AppHiberUtil.isStrEmpty(pkgName)) {
            return -1;
        } else {
            AbsAppInfo targetApp = new AbsAppInfo(uid, pkgName);
            setResAppEventData(targetApp, timestamp);
            return unFrozenInterrupt(targetApp, true);
        }
    }

    public void setScreenState(int screenState) {
        if (this.mRunning.get()) {
            if (screenState == 90011) {
                this.mIsScreenOff.set(true);
            } else if (screenState == 20011) {
                this.mIsScreenOff.set(false);
            } else {
                String str = TAG_PG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(screenState);
                stringBuilder.append(" is not EVENT_SCREEN_OFF/ON, Neglect!");
                AwareLog.i(str, stringBuilder.toString());
            }
            return;
        }
        AwareLog.w(TAG_PG, "setScreenState     failed  , because AppHibernateTask is not  enable");
    }

    public ArrayList<DumpData> getDumpData(int time) {
        try {
            this.frzSemaphore.acquire();
            AwareLog.d(TAG_PG, "getDumpData frzSemaphore : acquire");
            if (this.mRunning.get()) {
                this.mAppHiberMgr.doHiberDumpApi(1);
            }
            this.frzSemaphore.release();
            AwareLog.d(TAG_PG, "getDumpData frzSemaphore : release");
            return AppHiberRadar.getInstance().getDumpData(time);
        } catch (InterruptedException e) {
            AwareLog.e(TAG_PG, "getDumpData happened InterruptedException");
            return null;
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        try {
            this.frzSemaphore.acquire();
            AwareLog.d(TAG_PG, "getStatisticsData frzSemaphore : acquire");
            if (this.mRunning.get()) {
                this.mAppHiberMgr.doHiberDumpApi(2);
            }
            this.frzSemaphore.release();
            AwareLog.d(TAG_PG, "getStatisticsData frzSemaphore : release");
            return AppHiberRadar.getInstance().getStatisticsData();
        } catch (InterruptedException e) {
            AwareLog.e(TAG_PG, "getStatisticsData happened InterruptedException");
            return null;
        }
    }

    public boolean isAppHiberEnabled() {
        return this.mRunning.get();
    }

    public int reclaimApp(AwareProcessInfo awareProcessInfo) {
        if (awareProcessInfo == null) {
            AwareLog.i(TAG_PG, "recliamApps     refused, because awareProcessInfo is Null");
            return EReclaimResult.OTHER_ERR.getValue();
        } else if (this.mIsScreenOff.get()) {
            AwareLog.i(TAG_PG, "recliamApps     refused, because Screen   Off ");
            return EReclaimResult.OTHER_ERR.getValue();
        } else {
            ArraySet<HiberAppInfo> currentList = new ArraySet();
            ProcessInfo process = awareProcessInfo.mProcInfo;
            if (AppHiberUtil.illegalProcessInfo(process)) {
                return EReclaimResult.OTHER_ERR.getValue();
            }
            int tmpUid = process.mUid;
            String tmpPkgName = (String) process.mPackageName.get(0);
            if (AppHiberUtil.isStrEmpty(tmpPkgName)) {
                AwareLog.d(TAG_PG, "the awareProcessInfo.mProcInfo.mPackageName is empty, Illeagal! Return.");
                return EReclaimResult.OTHER_ERR.getValue();
            }
            currentList.add(new HiberAppInfo(process.mUid, tmpPkgName, process.mPid, process.mProcessName));
            try {
                this.frzSemaphore.acquire();
                AwareLog.d(TAG_PG, "reclaimApp frzSemaphore : acquire");
                int retValue = EReclaimResult.OTHER_ERR.getValue();
                if (this.mRunning.get()) {
                    retValue = analysisAPBInfo(new AbsAppInfo(tmpUid, tmpPkgName), currentList);
                }
                this.frzSemaphore.release();
                AwareLog.d(TAG_PG, "reclaimApp frzSemaphore : release");
                return retValue;
            } catch (InterruptedException e) {
                AwareLog.e(TAG_PG, "reclaimApp happened InterruptedException");
                return EReclaimResult.OTHER_ERR.getValue();
            }
        }
    }

    private void sendMsgToHiberEventHandler(int eventType, long delay) {
        if (this.mHiberEventHandler == null) {
            AwareLog.e(TAG_PG, "sendMsgToHiberEventHandler     exit  , because  NULL == mHiberEventHandler");
            return;
        }
        if (eventType == AppHibernateCst.ETYPE_MSG_WHAT_CREATE || eventType == AppHibernateCst.ETYPE_MSG_WHAT_DESTORY) {
            removeAllMsgFromHiberEventHandler();
        } else if (eventType == AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK) {
            this.mHiberEventHandler.removeMessages(AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK);
        } else if (eventType == 1) {
            this.mHiberEventHandler.removeMessages(1);
            synchronized (this.frzHashSet) {
                if (this.frzHashSet.isEmpty()) {
                    AwareLog.i(TAG_PG, "frzHashSet  is null, no need to request reclaim");
                    return;
                }
            }
        } else {
            String str = TAG_PG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("eventType=");
            stringBuilder.append(eventType);
            stringBuilder.append(" is not the legal msgWhat, Neglect!");
            AwareLog.i(str, stringBuilder.toString());
            return;
        }
        Message msg = this.mHiberEventHandler.obtainMessage();
        msg.what = eventType;
        this.mHiberEventHandler.sendMessageDelayed(msg, delay);
    }

    private void removeAllMsgFromHiberEventHandler() {
        if (this.mHiberEventHandler == null) {
            AwareLog.w(TAG_PG, "null == mHiberEventHandler");
            return;
        }
        this.mHiberEventHandler.removeMessages(AppHibernateCst.ETYPE_MSG_WHAT_CREATE);
        removeAllMsgFromHiberEventHandlerExcpCreate();
    }

    private void removeAllMsgFromHiberEventHandlerExcpCreate() {
        if (this.mHiberEventHandler != null) {
            this.mHiberEventHandler.removeMessages(AppHibernateCst.ETYPE_MSG_WHAT_DESTORY);
            this.mHiberEventHandler.removeMessages(1);
            this.mHiberEventHandler.removeMessages(AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK);
        }
    }

    private void createInhandleMsg() {
        if (this.mRunning.get()) {
            AwareLog.d(TAG_PG, "AppHiberTask has been Created!");
            return;
        }
        this.mAppHiberMgr.notifyHiberStart();
        getPGSdk();
        this.mRunning.set(true);
    }

    private void destoryInhandleMsg() {
        if (this.mRunning.get()) {
            try {
                this.frzSemaphore.acquire();
                AwareLog.d(TAG_PG, "destoryInhandleMsg frzSemaphore : acquire");
                this.mRunning.set(false);
                callPGunRegisterListener();
                this.mPGSdk = null;
                removeAllMsgFromHiberEventHandlerExcpCreate();
                clearLocalData();
                this.mAppHiberMgr.notifyHiberStop();
                this.frzSemaphore.release();
                AwareLog.d(TAG_PG, "destoryInhandleMsg frzSemaphore : release");
                return;
            } catch (InterruptedException e) {
                AwareLog.e(TAG_PG, "destoryInhandleMsg happened InterruptedException");
                return;
            }
        }
        AwareLog.d(TAG_PG, "AppHiberTask has been Destroyed!");
    }

    private int frozenInhandleMsg() {
        if (this.mIsScreenOff.get()) {
            AwareLog.i(TAG_PG, " Screen   Off  State, frozenInhandleMsg Return");
            return -1;
        } else if (!MemoryReader.isZramOK()) {
            AwareLog.i(TAG_PG, " Zram Space may be full, frozenInhandleMsg Return");
            return -1;
        } else if (isInteracting()) {
            AwareLog.i(TAG_PG, "at the moment: Interactioning, frozenInhandleMsg delay 10 s");
            sendMsgToHiberEventHandler(1, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            return -1;
        } else if (isCpuLoadHeavy()) {
            AwareLog.i(TAG_PG, "at the moment: CpuLoad is heavy, frozenInhandleMsg delay 1 min");
            sendMsgToHiberEventHandler(1, AppHibernateCst.DELAY_ONE_MINS);
            return -1;
        } else {
            int retValue = -1;
            long delayTime = 5000;
            if (this.frzSemaphore.tryAcquire()) {
                AwareLog.d(TAG_PG, "frozenInhandleMsg frzSemaphore : tryAcquire");
                AbsAppInfo targetApp = null;
                synchronized (this.frzHashSet) {
                    if (!this.frzHashSet.isEmpty()) {
                        targetApp = (AbsAppInfo) this.frzHashSet.iterator().next();
                        this.frzHashSet.remove(targetApp);
                    }
                }
                retValue = analysisAPBInfo(targetApp, AppHiberUtil.getHiberProcInfoListByAbsAppInfo(this.mContext, targetApp));
                if (!(!MemoryFeature2.isUpMemoryFeature.get() || MemoryConstant.getConfigGmcSwitch() == 0 || targetApp == null)) {
                    GpuCompressAction.doGmc(targetApp.mUid);
                }
                this.frzSemaphore.release();
                AwareLog.d(TAG_PG, "frozenInhandleMsg frzSemaphore : release");
                delayTime = 0;
            }
            sendMsgToHiberEventHandler(1, delayTime);
            return retValue;
        }
    }

    private int unFrozenInterrupt(AbsAppInfo keyValue, boolean needRmv) {
        ArraySet<HiberAppInfo> tmpSet;
        synchronized (this.mReclaimedRecordMap) {
            tmpSet = (ArraySet) this.mReclaimedRecordMap.get(keyValue);
        }
        if (tmpSet == null) {
            String str = TAG_PG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[uid = ");
            stringBuilder.append(keyValue.mUid);
            stringBuilder.append(", pkg = ");
            stringBuilder.append(keyValue.mPkgName);
            stringBuilder.append("] not in relaimedMap");
            AwareLog.d(str, stringBuilder.toString());
            return 0;
        }
        int retValue = 0;
        if (!this.mIsScreenOff.get() && keyValue.equals(this.mReclaimingApp)) {
            retValue = this.mAppHiberMgr.doHiberFrzApi(keyValue.mPkgName, AppHiberUtil.getPidsFromList(tmpSet), 0);
        }
        if (needRmv) {
            synchronized (this.mReclaimedRecordMap) {
                this.mReclaimedRecordMap.remove(keyValue);
            }
        }
        return retValue;
    }

    private int analysisAPBInfo(AbsAppInfo keyValue, ArraySet<HiberAppInfo> currentChildList) {
        if (AppHiberUtil.illegalAbsAppInfo(keyValue) || AppHiberUtil.illegalHiberAppInfoArraySet(currentChildList)) {
            synchronized (this.mReclaimedRecordMap) {
                this.mReclaimedRecordMap.remove(keyValue);
            }
            return EReclaimResult.OTHER_ERR.getValue();
        }
        ArraySet<HiberAppInfo> hisChildList;
        int[] pidArray;
        synchronized (this.mReclaimedRecordMap) {
            hisChildList = (ArraySet) this.mReclaimedRecordMap.get(keyValue);
            pidArray = AppHiberUtil.getDiffPidArray(hisChildList, currentChildList);
        }
        String str;
        if (pidArray.length == 0) {
            str = TAG_PG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(keyValue.mPkgName);
            stringBuilder.append("  has no diff pid for reclaim! analysisAPBInfo Return");
            AwareLog.d(str, stringBuilder.toString());
            return EReclaimResult.HAS_BEEN_RECLAIMED.getValue();
        }
        int cmdRet = this.mAppHiberMgr.doHiberFrzApi(keyValue.mPkgName, pidArray, 1);
        if (-1 == cmdRet) {
            str = TAG_PG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(keyValue.mPkgName);
            stringBuilder2.append("  send to native err! analysisAPBInfo Return");
            AwareLog.d(str, stringBuilder2.toString());
            return EReclaimResult.SEND_PRO_TO_NATIVE_ERR.getValue();
        }
        StringBuilder stringBuilder3;
        if (!AppHiberUtil.illegalHiberAppInfoArraySet(hisChildList)) {
            currentChildList.addAll(hisChildList);
        }
        synchronized (this.mReclaimedRecordMap) {
            ArraySet<HiberAppInfo> validReclaimedSet = new ArraySet();
            validReclaimedSet.addAll(currentChildList);
            this.mReclaimedRecordMap.put(keyValue, validReclaimedSet);
        }
        int[] failArray = AppHibernateCst.EMPTY_INT_ARRAY;
        int i = 0;
        if (isTopFrontApp(keyValue)) {
            this.mAppHiberMgr.doHiberFrzApi(keyValue.mPkgName, pidArray, 0);
            failArray = pidArray;
            String str2 = TAG_PG;
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append(keyValue.mPkgName);
            stringBuilder3.append("  is Front, stop reclaim.");
            AwareLog.i(str2, stringBuilder3.toString());
        } else {
            this.mReclaimingApp = keyValue;
            failArray = this.mAppHiberMgr.doHiberReclaimApi();
            this.mReclaimingApp = INVALID_ABSAPPINFO;
        }
        if (failArray.length > 0) {
            List<HiberAppInfo> tmpList = new ArrayList();
            int length = failArray.length;
            while (i < length) {
                int pid = failArray[i];
                Iterator it = currentChildList.iterator();
                while (it.hasNext()) {
                    HiberAppInfo aware = (HiberAppInfo) it.next();
                    if (aware.mPid == pid) {
                        tmpList.add(aware);
                        break;
                    }
                }
                i++;
            }
            currentChildList.removeAll(tmpList);
            String str3 = TAG_PG;
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("reclaim ");
            stringBuilder3.append(Arrays.toString(pidArray));
            stringBuilder3.append(", be interrupted ");
            stringBuilder3.append(Arrays.toString(failArray));
            AwareLog.i(str3, stringBuilder3.toString());
            synchronized (this.mReclaimedRecordMap) {
                if (this.mReclaimedRecordMap.containsKey(keyValue)) {
                    if (AppHiberUtil.illegalHiberAppInfoArraySet(currentChildList)) {
                        this.mReclaimedRecordMap.remove(keyValue);
                    } else {
                        this.mReclaimedRecordMap.put(keyValue, currentChildList);
                    }
                }
            }
            cmdRet = EReclaimResult.RECLAIM_BE_INTERRUPT.getValue();
        }
        return cmdRet;
    }

    private void clearLocalData() {
        synchronized (this.mReclaimedRecordMap) {
            this.mReclaimedRecordMap.clear();
        }
        synchronized (this.frzHashSet) {
            this.frzHashSet.clear();
        }
        this.mLastInputEvent = 0;
        this.mLastInputTime = 0;
        this.mLastResEventTime = 0;
        this.mReclaimingApp = INVALID_ABSAPPINFO;
        this.mCurFrontAbsApp = INVALID_ABSAPPINFO;
    }

    private void callPGregisterListener() {
        if (this.mPGSdk != null) {
            try {
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 6);
            } catch (RemoteException e) {
                this.mPGSdk = null;
                AwareLog.e(TAG_PG, "mPGSdk registerSink && enableStateEvent happend RemoteException ");
            }
        }
    }

    private void callPGunRegisterListener() {
        if (this.mPGSdk != null) {
            try {
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, 6);
            } catch (RemoteException e) {
                AwareLog.e(TAG_PG, "callPG unRegisterListener  happend RemoteException ");
            }
        }
    }

    private String callPGGetTopFrontApp() {
        if (this.mPGSdk == null) {
            return null;
        }
        try {
            return this.mPGSdk.getTopFrontApp(this.mContext);
        } catch (RemoteException e) {
            AwareLog.e(TAG_PG, "callPG getTopFrontApp  happend RemoteException ");
            return null;
        }
    }

    private boolean getPGSdk() {
        if (this.mPGSdk != null) {
            return true;
        }
        this.mPGSdk = PGSdk.getInstance();
        if (this.mPGSdk != null) {
            callPGregisterListener();
        }
        if (this.mPGSdk != null) {
            return true;
        }
        sendMsgToHiberEventHandler(AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK, AppHibernateCst.DELAY_ONE_MINS);
        return false;
    }

    public int setLastInputEventData(int lastInputEvent, long lastInputTime) {
        if (this.mRunning.get()) {
            this.mLastInputEvent = lastInputEvent;
            this.mLastInputTime = lastInputTime;
            if (this.mLastInputEvent == IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT) {
                foreceInterruptReclaim();
            }
            return 0;
        }
        AwareLog.w(TAG_PG, "setLastInputEventData     failed  , because AppHibernateTask is not  enable");
        return -1;
    }

    private int foreceInterruptReclaim() {
        if (AppHiberUtil.illegalAbsAppInfo(this.mReclaimingApp)) {
            return -1;
        }
        return unFrozenInterrupt(this.mReclaimingApp, false);
    }

    private boolean isInteracting() {
        if (this.mLastInputEvent == IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT) {
            return true;
        }
        if (this.mLastInputEvent == 80001) {
            return SystemClock.uptimeMillis() - this.mLastInputTime < 4000;
        }
        String str = TAG_PG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mLastInputEvent=");
        stringBuilder.append(this.mLastInputEvent);
        stringBuilder.append(" is not EVENT_TOUCH_DOWN/UP, Neglect!");
        AwareLog.i(str, stringBuilder.toString());
        return false;
    }

    private boolean isCpuLoadHeavy() {
        return this.mCpuReader.getCpuPercent() > MemoryConstant.getNormalThresHold();
    }

    public ArrayMap<Integer, HiberAppInfo> getRelaimedRecord() {
        if (this.mRunning.get()) {
            synchronized (this.mReclaimedRecordMap) {
                if (this.mReclaimedRecordMap.isEmpty()) {
                    AwareLog.i(TAG_PG, "current  reclaimed record is Empty");
                    return null;
                }
                ArrayMap<Integer, HiberAppInfo> returnMap = new ArrayMap();
                for (Entry<AbsAppInfo, ArraySet<HiberAppInfo>> entry : this.mReclaimedRecordMap.entrySet()) {
                    Iterator it = ((ArraySet) entry.getValue()).iterator();
                    while (it.hasNext()) {
                        HiberAppInfo appinfo = (HiberAppInfo) it.next();
                        returnMap.put(Integer.valueOf(appinfo.mPid), appinfo);
                    }
                }
                return returnMap;
            }
        }
        AwareLog.w(TAG_PG, "getRelaimedList     failed  , because AppHibernateTask is not  enable");
        return null;
    }

    private void setResAppEventData(AbsAppInfo keyValue, long lastEventTime) {
        if (!keyValue.equals(this.mCurFrontAbsApp)) {
            this.mCurFrontAbsApp = keyValue;
            this.mLastResEventTime = lastEventTime;
        }
    }

    private boolean isTopFrontApp(AbsAppInfo keyValue) {
        if (AppHiberUtil.illegalAbsAppInfo(this.mCurFrontAbsApp) || SystemClock.uptimeMillis() - this.mLastResEventTime >= 500) {
            return AppHiberUtil.isTheSameAppUnderMultiUser(callPGGetTopFrontApp(), keyValue);
        }
        return keyValue.equals(this.mCurFrontAbsApp);
    }
}
