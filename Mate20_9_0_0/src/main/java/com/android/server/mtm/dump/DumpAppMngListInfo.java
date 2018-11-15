package com.android.server.mtm.dump;

import android.content.Context;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.PrintWriter;

public final class DumpAppMngListInfo {
    private static AwareAppMngSort appGroupMng = null;

    public static final void dump(Context context, PrintWriter pw, String[] args) {
        appGroupMng = AwareAppMngSort.getInstance(context);
        if (appGroupMng != null) {
            if (args.length < 2) {
                pw.println("Bad command");
            } else if (args[1] != null && pw != null) {
                String cmd = args[1];
                StringBuilder stringBuilder;
                if ("dump".equals(cmd)) {
                    if (args.length < 3) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Bad command :");
                        stringBuilder.append(cmd);
                        pw.println(stringBuilder.toString());
                        return;
                    }
                    appGroupMng.dump(pw, args[2]);
                } else if ("enable_log".equals(cmd)) {
                    AwareAppMngSort.enableDebug();
                } else if ("disable_log".equals(cmd)) {
                    AwareAppMngSort.disableDebug();
                } else if ("enable_assoc".equals(cmd)) {
                    appGroupMng.enableAssocDebug();
                } else if ("disable_assoc".equals(cmd)) {
                    appGroupMng.disableAssocDebug();
                } else if ("getstatus_assoc".equals(cmd)) {
                    boolean status = appGroupMng.getAssocDebug();
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("assoc status: ");
                    stringBuilder2.append(status);
                    pw.println(stringBuilder2.toString());
                } else if (HwSecDiagnoseConstant.ANTIMAL_APK_TYPE.equals(cmd)) {
                    appGroupMng.dumpClassInfo(pw);
                } else if ("removeAlarm".equals(cmd)) {
                    appGroupMng.dumpRemoveAlarm(pw, args);
                } else if ("removeInvalidAlarm".equals(cmd)) {
                    appGroupMng.dumpRemoveInvalidAlarm(pw, args);
                } else if ("alarm".equals(cmd)) {
                    appGroupMng.dumpAlarm(pw, args);
                } else {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Bad command :");
                    stringBuilder.append(cmd);
                    pw.println(stringBuilder.toString());
                }
            }
        }
    }
}
