package com.android.commands.uiautomator;

import android.app.UiAutomation;
import android.graphics.Point;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Environment;
import android.view.Display;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.commands.uiautomator.Launcher.Command;
import com.android.uiautomator.core.AccessibilityNodeInfoDumper;
import com.android.uiautomator.core.UiAutomationShellWrapper;
import java.io.File;
import java.util.concurrent.TimeoutException;

public class DumpCommand extends Command {
    private static final File DEFAULT_DUMP_FILE = new File(Environment.getLegacyExternalStorageDirectory(), "window_dump.xml");

    public DumpCommand() {
        super("dump");
    }

    public String shortHelp() {
        return "creates an XML dump of current UI hierarchy";
    }

    public String detailedOptions() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("    dump [--verbose][file]\n      [--compressed]: dumps compressed layout information.\n      [file]: the location where the dumped XML should be stored, default is\n      ");
        stringBuilder.append(DEFAULT_DUMP_FILE.getAbsolutePath());
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public void run(String[] args) {
        File dumpFile = DEFAULT_DUMP_FILE;
        boolean verboseMode = true;
        File dumpFile2 = dumpFile;
        for (String arg : args) {
            if (arg.equals("--compressed")) {
                verboseMode = false;
            } else if (!arg.startsWith("-")) {
                dumpFile2 = new File(arg);
            }
        }
        UiAutomationShellWrapper automationWrapper = new UiAutomationShellWrapper();
        automationWrapper.connect();
        if (verboseMode) {
            automationWrapper.setCompressedLayoutHierarchy(false);
        } else {
            automationWrapper.setCompressedLayoutHierarchy(true);
        }
        try {
            UiAutomation uiAutomation = automationWrapper.getUiAutomation();
            uiAutomation.waitForIdle(1000, 10000);
            AccessibilityNodeInfo info = uiAutomation.getRootInActiveWindow();
            if (info == null) {
                System.err.println("ERROR: null root node returned by UiTestAutomationBridge.");
                return;
            }
            Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
            int rotation = display.getRotation();
            Point size = new Point();
            display.getSize(size);
            AccessibilityNodeInfoDumper.dumpWindowToFile(info, dumpFile2, rotation, size.x, size.y);
            automationWrapper.disconnect();
            System.out.println(String.format("UI hierchary dumped to: %s", new Object[]{dumpFile2.getAbsolutePath()}));
        } catch (TimeoutException e) {
            System.err.println("ERROR: could not get idle state.");
        } finally {
            automationWrapper.disconnect();
        }
    }
}
