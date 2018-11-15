package com.huawei.nb.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public interface IOFileFilter extends FileFilter, FilenameFilter {
    boolean accept(File file);

    boolean accept(File file, String str);
}
