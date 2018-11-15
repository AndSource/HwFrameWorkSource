package com.android.systemui.shared.recents.model;

import android.app.ActivityManager.TaskSnapshot;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

public class ThumbnailData {
    public Rect insets;
    public boolean isRealSnapshot;
    public boolean isTranslucent;
    public int orientation;
    public boolean reducedResolution;
    public float scale;
    public int systemUiVisibility;
    public final Bitmap thumbnail;
    public int windowingMode;

    public ThumbnailData() {
        this.insets = new Rect();
        this.scale = 1.0f;
        this.thumbnail = null;
        this.orientation = 0;
        this.insets = new Rect();
        this.reducedResolution = false;
        this.scale = 1.0f;
        this.isRealSnapshot = true;
        this.isTranslucent = false;
        this.windowingMode = 0;
        this.systemUiVisibility = 0;
    }

    public ThumbnailData(TaskSnapshot snapshot) {
        this.insets = new Rect();
        this.scale = 1.0f;
        this.thumbnail = Bitmap.createHardwareBitmap(snapshot.getSnapshot());
        Rect r = snapshot.getContentInsets();
        if (r != null) {
            this.insets = new Rect(r);
        } else {
            Log.e("ThumbnailData", "createFromTaskSnapshot error napshot.getContentInsets() is null");
        }
        this.orientation = snapshot.getOrientation();
        this.reducedResolution = snapshot.isReducedResolution();
        this.scale = snapshot.getScale();
        this.isRealSnapshot = snapshot.isRealSnapshot();
        this.isTranslucent = snapshot.isTranslucent();
        this.windowingMode = snapshot.getWindowingMode();
        this.systemUiVisibility = snapshot.getSystemUiVisibility();
    }
}
