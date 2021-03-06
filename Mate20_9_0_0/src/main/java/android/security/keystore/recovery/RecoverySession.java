package android.security.keystore.recovery;

import android.annotation.SystemApi;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.ArrayMap;
import android.util.Log;
import java.security.Key;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SystemApi
public class RecoverySession implements AutoCloseable {
    private static final int SESSION_ID_LENGTH_BYTES = 16;
    private static final String TAG = "RecoverySession";
    private final RecoveryController mRecoveryController;
    private final String mSessionId;

    private RecoverySession(RecoveryController recoveryController, String sessionId) {
        this.mRecoveryController = recoveryController;
        this.mSessionId = sessionId;
    }

    static RecoverySession newInstance(RecoveryController recoveryController) {
        return new RecoverySession(recoveryController, newSessionId());
    }

    private static String newSessionId() {
        byte[] sessionId = new byte[16];
        new SecureRandom().nextBytes(sessionId);
        StringBuilder sb = new StringBuilder();
        for (byte b : sessionId) {
            sb.append(Byte.toHexString(b, false));
        }
        return sb.toString();
    }

    @Deprecated
    public byte[] start(byte[] verifierPublicKey, byte[] vaultParams, byte[] vaultChallenge, List<KeyChainProtectionParams> list) throws CertificateException, InternalRecoveryServiceException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public byte[] start(CertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, List<KeyChainProtectionParams> list) throws CertificateException, InternalRecoveryServiceException {
        throw new UnsupportedOperationException();
    }

    public byte[] start(String rootCertificateAlias, CertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, List<KeyChainProtectionParams> secrets) throws CertificateException, InternalRecoveryServiceException {
        try {
            return this.mRecoveryController.getBinder().startRecoverySessionWithCertPath(this.mSessionId, rootCertificateAlias, RecoveryCertPath.createRecoveryCertPath(verifierCertPath), vaultParams, vaultChallenge, secrets);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            if (e2.errorCode == 25 || e2.errorCode == 28) {
                throw new CertificateException("Invalid certificate for recovery session", e2);
            }
            throw this.mRecoveryController.wrapUnexpectedServiceSpecificException(e2);
        }
    }

    @Deprecated
    public Map<String, byte[]> recoverKeys(byte[] recoveryKeyBlob, List<WrappedApplicationKey> list) throws SessionExpiredException, DecryptionFailedException, InternalRecoveryServiceException {
        throw new UnsupportedOperationException();
    }

    public Map<String, Key> recoverKeyChainSnapshot(byte[] recoveryKeyBlob, List<WrappedApplicationKey> applicationKeys) throws SessionExpiredException, DecryptionFailedException, InternalRecoveryServiceException {
        try {
            return getKeysFromGrants(this.mRecoveryController.getBinder().recoverKeyChainSnapshot(this.mSessionId, recoveryKeyBlob, applicationKeys));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (ServiceSpecificException e2) {
            if (e2.errorCode == 26) {
                throw new DecryptionFailedException(e2.getMessage());
            } else if (e2.errorCode == 24) {
                throw new SessionExpiredException(e2.getMessage());
            } else {
                throw this.mRecoveryController.wrapUnexpectedServiceSpecificException(e2);
            }
        }
    }

    private Map<String, Key> getKeysFromGrants(Map<String, String> grantAliases) throws InternalRecoveryServiceException {
        ArrayMap<String, Key> keysByAlias = new ArrayMap(grantAliases.size());
        for (String alias : grantAliases.keySet()) {
            try {
                keysByAlias.put(alias, this.mRecoveryController.getKeyFromGrant((String) grantAliases.get(alias)));
            } catch (UnrecoverableKeyException e) {
                throw new InternalRecoveryServiceException(String.format(Locale.US, "Failed to get key '%s' from grant '%s'", new Object[]{alias, grantAlias}), e);
            }
        }
        return keysByAlias;
    }

    String getSessionId() {
        return this.mSessionId;
    }

    public void close() {
        try {
            this.mRecoveryController.getBinder().closeSession(this.mSessionId);
        } catch (RemoteException | ServiceSpecificException e) {
            Log.e(TAG, "Unexpected error trying to close session", e);
        }
    }
}
