package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import java.util.Arrays;

public abstract class EnrollClient extends ClientMonitor {
    private static final int ENROLLMENT_TIMEOUT_MS = 60000;
    private static final long MS_PER_SEC = 1000;
    private byte[] mCryptoToken;

    public EnrollClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int userId, int groupId, byte[] cryptoToken, boolean restricted, String owner) {
        byte[] bArr = cryptoToken;
        super(context, halDeviceId, token, receiver, userId, groupId, restricted, owner);
        if (bArr != null) {
            this.mCryptoToken = Arrays.copyOf(bArr, bArr.length);
        }
    }

    public boolean onEnrollResult(int fingerId, int groupId, int remaining) {
        StringBuilder stringBuilder;
        if (groupId != getGroupId()) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("groupId != getGroupId(), groupId: ");
            stringBuilder.append(groupId);
            stringBuilder.append(" getGroupId():");
            stringBuilder.append(getGroupId());
            Slog.w("FingerprintService", stringBuilder.toString());
        }
        if (remaining == 0 && fingerId != 0) {
            if (FingerprintUtils.getInstance().isDualFp()) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("dualFingerprint-> addFingerprint for targetDevice:");
                stringBuilder.append(getTargetDevice());
                Slog.d("FingerprintService", stringBuilder.toString());
                FingerprintUtils.getInstance().addFingerprintForUser(getContext(), fingerId, getTargetUserId(), getTargetDevice());
            } else {
                FingerprintUtils.getInstance().addFingerprintForUser(getContext(), fingerId, getTargetUserId());
            }
        }
        return sendEnrollResult(fingerId, groupId, remaining);
    }

    private boolean sendEnrollResult(int fpId, int groupId, int remaining) {
        IFingerprintServiceReceiver receiver = getReceiver();
        boolean z = true;
        if (receiver == null) {
            return true;
        }
        notifyUserActivity();
        mAcquiredInfo = -1;
        vibrateSuccess();
        MetricsLogger.action(getContext(), 251);
        try {
            receiver.onEnrollResult(getHalDeviceId(), fpId, groupId, remaining);
            if (remaining != 0) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Slog.w("FingerprintService", "Failed to notify EnrollResult:", e);
            return true;
        } catch (Exception e2) {
            Slog.e("FingerprintService", "sendEnrollResult failed", e2);
            return true;
        }
    }

    public int start() {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "enroll: no fingerprint HAL!");
            return 3;
        }
        int timeout = 60;
        try {
            int result = daemon.enroll(this.mCryptoToken, getRealUserIdForHal(getGroupId()), 60);
            if (result != 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("startEnroll failed, result=");
                stringBuilder.append(result);
                Slog.w("FingerprintService", stringBuilder.toString());
                MetricsLogger.histogram(getContext(), "fingerprintd_enroll_start_error", result);
                onError(1, 0);
                return result;
            }
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "startEnroll failed", e);
        } catch (Exception e2) {
            Slog.e("FingerprintService", "startEnroll failed", e2);
        }
        return 0;
    }

    public int stop(boolean initiatedByClient) {
        if (this.mAlreadyCancelled) {
            Slog.w("FingerprintService", "stopEnroll: already cancelled!");
            return 0;
        }
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "stopEnrollment: no fingerprint HAL!");
            return 3;
        }
        try {
            int result = daemon.cancel();
            if (result != 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("startEnrollCancel failed, result = ");
                stringBuilder.append(result);
                Slog.w("FingerprintService", stringBuilder.toString());
                return result;
            }
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "stopEnrollment failed", e);
        } catch (Exception e2) {
            Slog.e("FingerprintService", "stop failed", e2);
        }
        if (initiatedByClient) {
            onError(5, 0);
        }
        this.mAlreadyCancelled = true;
        return 0;
    }

    public boolean onRemoved(int fingerId, int groupId, int remaining) {
        Slog.w("FingerprintService", "onRemoved() called for enroll!");
        return true;
    }

    public boolean onEnumerationResult(int fingerId, int groupId, int remaining) {
        Slog.w("FingerprintService", "onEnumerationResult() called for enroll!");
        return true;
    }

    public boolean onAuthenticated(int fingerId, int groupId) {
        Slog.w("FingerprintService", "onAuthenticated() called for enroll!");
        return true;
    }
}
