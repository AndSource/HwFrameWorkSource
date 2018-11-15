package com.android.server.pm;

import android.content.pm.PackageInfo;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

abstract class ShortcutPackageItem {
    private static final String KEY_NAME = "name";
    private static final String TAG = "ShortcutService";
    private final ShortcutPackageInfo mPackageInfo;
    private final String mPackageName;
    private final int mPackageUserId;
    protected ShortcutUser mShortcutUser;

    protected abstract boolean canRestoreAnyVersion();

    public abstract int getOwnerUserId();

    protected abstract void onRestored(int i);

    public abstract void saveToXml(XmlSerializer xmlSerializer, boolean z) throws IOException, XmlPullParserException;

    protected ShortcutPackageItem(ShortcutUser shortcutUser, int packageUserId, String packageName, ShortcutPackageInfo packageInfo) {
        this.mShortcutUser = shortcutUser;
        this.mPackageUserId = packageUserId;
        this.mPackageName = (String) Preconditions.checkStringNotEmpty(packageName);
        this.mPackageInfo = (ShortcutPackageInfo) Preconditions.checkNotNull(packageInfo);
    }

    public void replaceUser(ShortcutUser user) {
        this.mShortcutUser = user;
    }

    public ShortcutUser getUser() {
        return this.mShortcutUser;
    }

    public int getPackageUserId() {
        return this.mPackageUserId;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public ShortcutPackageInfo getPackageInfo() {
        return this.mPackageInfo;
    }

    public void refreshPackageSignatureAndSave() {
        if (!this.mPackageInfo.isShadow()) {
            ShortcutService s = this.mShortcutUser.mService;
            this.mPackageInfo.refreshSignature(s, this);
            s.scheduleSaveUser(getOwnerUserId());
        }
    }

    public void attemptToRestoreIfNeededAndSave() {
        if (this.mPackageInfo.isShadow()) {
            ShortcutService s = this.mShortcutUser.mService;
            if (s.isPackageInstalled(this.mPackageName, this.mPackageUserId)) {
                int restoreBlockReason;
                if (this.mPackageInfo.hasSignatures()) {
                    PackageInfo pi = s.getPackageInfoWithSignatures(this.mPackageName, this.mPackageUserId);
                    long currentVersionCode = pi.getLongVersionCode();
                    restoreBlockReason = this.mPackageInfo.canRestoreTo(s, pi, canRestoreAnyVersion());
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Attempted to restore package ");
                    stringBuilder.append(this.mPackageName);
                    stringBuilder.append("/u");
                    stringBuilder.append(this.mPackageUserId);
                    stringBuilder.append(" but signatures not found in the restore data.");
                    s.wtf(stringBuilder.toString());
                    restoreBlockReason = 102;
                }
                onRestored(restoreBlockReason);
                this.mPackageInfo.setShadow(false);
                s.scheduleSaveUser(this.mPackageUserId);
            }
        }
    }

    public JSONObject dumpCheckin(boolean clear) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("name", this.mPackageName);
        return result;
    }

    public void verifyStates() {
    }
}
