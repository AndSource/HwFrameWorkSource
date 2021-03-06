package com.android.server.slice;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Binder;
import android.os.Bundle;
import android.os.ShellCommand;
import android.util.ArraySet;
import com.android.server.power.IHwShutdownThread;
import com.android.server.zrhung.IZRHungService;
import java.io.PrintWriter;
import java.util.Set;

public class SliceShellCommand extends ShellCommand {
    private final SliceManagerService mService;

    public SliceShellCommand(SliceManagerService service) {
        this.mService = service;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        int i = -1;
        if (cmd.hashCode() == -185318259 && cmd.equals("get-permissions")) {
            i = 0;
        }
        if (i != 0) {
            return 0;
        }
        return runGetPermissions(getNextArgRequired());
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Status bar commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("  get-permissions <authority>");
        pw.println("    List the pkgs that have permission to an authority.");
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    }

    private int runGetPermissions(String authority) {
        if (Binder.getCallingUid() == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || Binder.getCallingUid() == 0) {
            Context context = this.mService.getContext();
            long ident = Binder.clearCallingIdentity();
            try {
                Uri uri = new Builder().scheme("content").authority(authority).build();
                if ("vnd.android.slice".equals(context.getContentResolver().getType(uri))) {
                    Bundle b = context.getContentResolver().call(uri, "get_permissions", null, null);
                    if (b == null) {
                        getOutPrintWriter().println("An error occurred getting permissions");
                        Binder.restoreCallingIdentity(ident);
                        return -1;
                    }
                    String[] permissions = b.getStringArray(IZRHungService.PARA_RESULT);
                    PrintWriter pw = getOutPrintWriter();
                    Set<String> listedPackages = new ArraySet();
                    if (!(permissions == null || permissions.length == 0)) {
                        for (PackageInfo app : context.getPackageManager().getPackagesHoldingPermissions(permissions, 0)) {
                            pw.println(app.packageName);
                            listedPackages.add(app.packageName);
                        }
                    }
                    for (String pkg : this.mService.getAllPackagesGranted(authority)) {
                        if (!listedPackages.contains(pkg)) {
                            pw.println(pkg);
                            listedPackages.add(pkg);
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                    return 0;
                }
                PrintWriter outPrintWriter = getOutPrintWriter();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(authority);
                stringBuilder.append(" is not a slice provider");
                outPrintWriter.println(stringBuilder.toString());
                return -1;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            getOutPrintWriter().println("Only shell can get permissions");
            return -1;
        }
    }
}
