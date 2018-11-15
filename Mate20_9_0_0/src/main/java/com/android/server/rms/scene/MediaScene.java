package com.android.server.rms.scene;

import android.content.Context;
import android.media.AudioSystem;
import android.os.Bundle;
import android.util.Log;
import com.android.server.rms.IScene;

public class MediaScene implements IScene {
    private final Context mContext;

    public MediaScene(Context context) {
        this.mContext = context;
    }

    public boolean identify(Bundle extras) {
        if (this.mContext == null) {
            return false;
        }
        return isAnySoundPlay();
    }

    public boolean isAnySoundPlay() {
        int streamCnt = AudioSystem.getNumStreamTypes();
        int streamType = 0;
        while (streamType < streamCnt) {
            if (AudioSystem.isStreamActive(streamType, 0) || AudioSystem.isStreamActiveRemotely(streamType, 0)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("is sound in streamType=");
                stringBuilder.append(streamType);
                Log.w("Rms.MediaScene", stringBuilder.toString());
                return true;
            }
            streamType++;
        }
        return false;
    }
}
