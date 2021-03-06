package java.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

class DeleteOnExitHook {
    private static LinkedHashSet<String> files = new LinkedHashSet();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                DeleteOnExitHook.runHooks();
            }
        });
    }

    private DeleteOnExitHook() {
    }

    static synchronized void add(String file) {
        synchronized (DeleteOnExitHook.class) {
            if (files != null) {
                files.add(file);
            } else {
                throw new IllegalStateException("Shutdown in progress");
            }
        }
    }

    static void runHooks() {
        Collection theFiles;
        synchronized (DeleteOnExitHook.class) {
            theFiles = files;
            files = null;
        }
        ArrayList<String> toBeDeleted = new ArrayList(theFiles);
        Collections.reverse(toBeDeleted);
        Iterator it = toBeDeleted.iterator();
        while (it.hasNext()) {
            new File((String) it.next()).delete();
        }
    }
}
