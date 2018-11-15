package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ComponentChangeMonitor {
    static final String COMCHANGE_REPORT_FILE_DIR;
    static final String COM_CHANGE_REPORT_FILE_LATEST = "com_change_report.1";
    static final String COM_CHANGE_REPORT_FILE_PREFIX = "com_change_report.";
    static final boolean DEBUG = false;
    static final int MAX_FILE_COUNT = 5;
    static final int MAX_FILE_SIZE = 102400;
    static final int MSG_WIRTE_FILE = 0;
    static final String TAG = "ComponentChangeMonitor";
    private Context mContext;
    private Looper mHandlerLooper = null;
    private List<String> mMonitorList = new ArrayList();
    private WriteFileHandler mWriteHandler = null;

    private class WriteFileHandler extends Handler {
        public WriteFileHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 0) {
                String str = ComponentChangeMonitor.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("WriteFileHandler unsupport operator ");
                stringBuilder.append(msg.what);
                Log.w(str, stringBuilder.toString());
                return;
            }
            ComponentChangeMonitor.this.writeLogToFile((String) msg.obj);
        }
    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getDataDirectory());
        stringBuilder.append("/log/com_change/");
        COMCHANGE_REPORT_FILE_DIR = stringBuilder.toString();
    }

    public ComponentChangeMonitor(Context context, Looper looper) {
        this.mContext = context;
        this.mHandlerLooper = looper;
        init();
    }

    private void init() {
        parseComChangeMonitorsXml();
        if (!this.mMonitorList.isEmpty()) {
            initComChangeDir();
            if (this.mHandlerLooper == null) {
                HandlerThread handlerThread = new HandlerThread(TAG);
                handlerThread.start();
                this.mHandlerLooper = handlerThread.getLooper();
            }
            this.mWriteHandler = new WriteFileHandler(this.mHandlerLooper);
        }
    }

    private void initComChangeDir() {
        File gmsDir = new File(COMCHANGE_REPORT_FILE_DIR);
        if (!gmsDir.exists()) {
            boolean mdir = gmsDir.mkdir();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("comChange directory not exist, make it: ");
            stringBuilder.append(mdir);
            Log.i(str, stringBuilder.toString());
        }
    }

    protected void writeComponetChangeLogToFile(ComponentName componentName, int newState, int userId) {
        if (componentName != null && isMonitorComponent(componentName.getPackageName()) && this.mWriteHandler != null) {
            String enable;
            String callPkgName = getAppNameByPid(Binder.getCallingPid());
            if (newState <= 1) {
                enable = "enable";
            } else {
                enable = "disable";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long nowRTC = System.currentTimeMillis();
            String writeLine = new StringBuilder();
            writeLine.append("User{");
            writeLine.append(userId);
            writeLine.append("}: ");
            writeLine.append(callPkgName);
            writeLine.append(" ");
            writeLine.append(enable);
            writeLine.append(" ");
            writeLine.append(componentName);
            writeLine.append(" at ");
            writeLine.append(sdf.format(new Date(nowRTC)));
            writeLine.append(".\n");
            writeLine = writeLine.toString();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("writeComponetChangeLogToFile writeLine: ");
            stringBuilder.append(writeLine);
            Log.i(str, stringBuilder.toString());
            this.mWriteHandler.sendMessage(this.mWriteHandler.obtainMessage(0, writeLine));
        }
    }

    private boolean isMonitorComponent(String appName) {
        if (this.mMonitorList.isEmpty()) {
            return false;
        }
        return this.mMonitorList.contains(appName);
    }

    private void writeLogToFile(String oneLine) {
        BufferedWriter out = null;
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(COMCHANGE_REPORT_FILE_DIR);
            stringBuilder.append(COM_CHANGE_REPORT_FILE_LATEST);
            fos = new FileOutputStream(stringBuilder.toString(), true);
            osw = new OutputStreamWriter(fos, "utf-8");
            out = new BufferedWriter(osw);
            out.write(oneLine);
            try {
                out.close();
            } catch (IOException e) {
                Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST out IOException");
            }
            try {
                osw.close();
            } catch (IOException e2) {
                Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST osw IOException");
            }
            try {
                fos.close();
            } catch (IOException e3) {
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST FileNotFoundException");
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                    Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST out IOException");
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e6) {
                    Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST osw IOException");
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e7) {
                }
            }
        } catch (IOException e8) {
            Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST IOException");
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e9) {
                    Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST out IOException");
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e10) {
                    Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST osw IOException");
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e11) {
                }
            }
        } catch (Exception e12) {
            Log.e(TAG, "writeLogToFile Exception: ", e12);
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e13) {
                    Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST out IOException");
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e14) {
                    Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST osw IOException");
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e15) {
                }
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e16) {
                    Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST out IOException");
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e17) {
                    Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST osw IOException");
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e18) {
                    Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST fos IOException");
                }
            }
        }
        checkFilesStatus();
        Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST fos IOException");
        checkFilesStatus();
    }

    private void checkFilesStatus() {
        File[] subFiles = new File(COMCHANGE_REPORT_FILE_DIR).listFiles();
        if (subFiles != null) {
            int dirFileCount = subFiles.length;
            File tempFile;
            StringBuilder stringBuilder;
            if (dirFileCount > 5) {
                String str = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("deleteOldAndCreateNewFile abnormal file Count: ");
                stringBuilder2.append(dirFileCount);
                Log.w(str, stringBuilder2.toString());
                for (int fileIndex = 0; fileIndex < dirFileCount; fileIndex++) {
                    tempFile = subFiles[fileIndex];
                    if (!(tempFile == null || !tempFile.exists() || tempFile.delete())) {
                        String str2 = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("checkFilesStatus delete failed: ");
                        stringBuilder.append(fileIndex);
                        Log.w(str2, stringBuilder.toString());
                    }
                }
                return;
            }
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append(COMCHANGE_REPORT_FILE_DIR);
            stringBuilder3.append(COM_CHANGE_REPORT_FILE_LATEST);
            tempFile = new File(stringBuilder3.toString());
            if (tempFile.exists() && tempFile.length() > MemoryConstant.RECLAIM_KILL_GAP_MEMORY) {
                for (int i = dirFileCount; i > 0; i--) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(COMCHANGE_REPORT_FILE_DIR);
                    stringBuilder.append(COM_CHANGE_REPORT_FILE_PREFIX);
                    stringBuilder.append(i);
                    File tempFile2 = new File(stringBuilder.toString());
                    if (tempFile2.exists()) {
                        StringBuilder stringBuilder4 = new StringBuilder();
                        stringBuilder4.append(COMCHANGE_REPORT_FILE_DIR);
                        stringBuilder4.append(COM_CHANGE_REPORT_FILE_PREFIX);
                        stringBuilder4.append(i + 1);
                        if (!tempFile2.renameTo(new File(stringBuilder4.toString()))) {
                            String str3 = TAG;
                            StringBuilder stringBuilder5 = new StringBuilder();
                            stringBuilder5.append("checkFilesStatus rename failed: ");
                            stringBuilder5.append(i);
                            Log.w(str3, stringBuilder5.toString());
                        }
                    }
                }
                if (5 == dirFileCount) {
                    stringBuilder3 = new StringBuilder();
                    stringBuilder3.append(COMCHANGE_REPORT_FILE_DIR);
                    stringBuilder3.append(COM_CHANGE_REPORT_FILE_PREFIX);
                    stringBuilder3.append(6);
                    File delFile = new File(stringBuilder3.toString());
                    if (delFile.exists() && !delFile.delete()) {
                        Log.w(TAG, "checkFilesStatus delete last file failed");
                    }
                }
            }
        }
    }

    private String getAppNameByPid(int pid) {
        List<RunningAppProcessInfo> processes = getRunningProcesses();
        if (processes == null) {
            Log.d(TAG, "get app name, get running process failed");
            return null;
        }
        for (RunningAppProcessInfo processInfo : processes) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return null;
    }

    private List<RunningAppProcessInfo> getRunningProcesses() {
        if (this.mContext == null) {
            Log.d(TAG, "getRunningProcesses, mContext is null");
            return null;
        }
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager != null) {
            return activityManager.getRunningAppProcesses();
        }
        Log.d(TAG, "get process status, get ams service failed");
        return null;
    }

    private void parseComChangeMonitorsXml() {
        File monitorFile = null;
        InputStream inputstream = null;
        try {
            monitorFile = HwCfgFilePolicy.getCfgFile("xml/com_change_monitors.xml", 0);
        } catch (NoClassDefFoundError e) {
            try {
                Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "com_change_monitors.xml FileNotFoundException");
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (XmlPullParserException e4) {
                Log.e(TAG, "com_change_monitors.xml XmlPullParserException");
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (IOException e6) {
                Log.e(TAG, "com_change_monitors.xml IOException");
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (IOException e7) {
                    }
                }
            } catch (Exception e8) {
                Log.e(TAG, "parseComChangeMonitorsXml e: ", e8);
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (IOException e9) {
                    }
                }
            } catch (Throwable th) {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (IOException e10) {
                        Log.e(TAG, "inputstream IOException");
                    }
                }
            }
        }
        if (monitorFile == null) {
            Log.i(TAG, "com_change_monitors.xml is not exist");
            if (inputstream != null) {
                try {
                    inputstream.close();
                } catch (IOException e11) {
                    Log.e(TAG, "inputstream IOException");
                }
            }
            return;
        }
        inputstream = new FileInputStream(monitorFile);
        parseComChangeData(inputstream);
        try {
            inputstream.close();
        } catch (IOException e12) {
        }
        Log.e(TAG, "inputstream IOException");
    }

    private void parseComChangeData(InputStream inputstream) throws XmlPullParserException, IOException {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputstream, null);
        for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
            if (event != 0) {
                switch (event) {
                    case 2:
                        if (!"component".equals(pullParser.getName())) {
                            break;
                        }
                        this.mMonitorList.add(pullParser.nextText());
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
