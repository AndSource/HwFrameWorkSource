package com.huawei.security.keymaster;

import java.util.HashMap;
import java.util.Map;

public final class HwKeymasterDefs {
    public static final int HW_AUTH_FACE = 4;
    public static final int HW_AUTH_FINGERPRINT = 2;
    public static final int HW_AUTH_PASSWORD = 1;
    public static final int KM_ALGORITHM_AES = 32;
    public static final int KM_ALGORITHM_EC = 3;
    public static final int KM_ALGORITHM_HMAC = 128;
    public static final int KM_ALGORITHM_RSA = 1;
    public static final int KM_BIGNUM = Integer.MIN_VALUE;
    public static final int KM_BLOB_REQUIRES_FILE_SYSTEM = 1;
    public static final int KM_BLOB_STANDALONE = 0;
    public static final int KM_BOOL = 1879048192;
    public static final int KM_BYTES = -1879048192;
    public static final int KM_DATE = 1610612736;
    public static final int KM_DIGEST_MD5 = 1;
    public static final int KM_DIGEST_NONE = 0;
    public static final int KM_DIGEST_SHA1 = 2;
    public static final int KM_DIGEST_SHA_2_224 = 3;
    public static final int KM_DIGEST_SHA_2_256 = 4;
    public static final int KM_DIGEST_SHA_2_384 = 5;
    public static final int KM_DIGEST_SHA_2_512 = 6;
    public static final int KM_ENUM = 268435456;
    public static final int KM_ENUM_REP = 536870912;
    public static final int KM_ERROR_CALLER_NONCE_PROHIBITED = -55;
    public static final int KM_ERROR_CONCURRENT_ACCESS_CONFLICT = -47;
    public static final int KM_ERROR_DELEGATION_NOT_ALLOWED = -23;
    public static final int KM_ERROR_IMPORTED_KEY_DECRYPTION_FAILED = -35;
    public static final int KM_ERROR_IMPORTED_KEY_NOT_ENCRYPTED = -34;
    public static final int KM_ERROR_IMPORTED_KEY_NOT_SIGNED = -36;
    public static final int KM_ERROR_IMPORTED_KEY_VERIFICATION_FAILED = -37;
    public static final int KM_ERROR_IMPORT_PARAMETER_MISMATCH = -44;
    public static final int KM_ERROR_INCOMPATIBLE_ALGORITHM = -5;
    public static final int KM_ERROR_INCOMPATIBLE_BLOCK_MODE = -8;
    public static final int KM_ERROR_INCOMPATIBLE_DIGEST = -13;
    public static final int KM_ERROR_INCOMPATIBLE_KEY_FORMAT = -18;
    public static final int KM_ERROR_INCOMPATIBLE_PADDING_MODE = -11;
    public static final int KM_ERROR_INCOMPATIBLE_PURPOSE = -3;
    public static final int KM_ERROR_INSUFFICIENT_BUFFER_SPACE = -29;
    public static final int KM_ERROR_INVALID_ARGUMENT = -38;
    public static final int KM_ERROR_INVALID_AUTHORIZATION_TIMEOUT = -16;
    public static final int KM_ERROR_INVALID_EXPIRATION_TIME = -14;
    public static final int KM_ERROR_INVALID_INPUT_LENGTH = -21;
    public static final int KM_ERROR_INVALID_KEY_BLOB = -33;
    public static final int KM_ERROR_INVALID_MAC_LENGTH = -57;
    public static final int KM_ERROR_INVALID_NONCE = -52;
    public static final int KM_ERROR_INVALID_OPERATION_HANDLE = -28;
    public static final int KM_ERROR_INVALID_RESCOPING = -42;
    public static final int KM_ERROR_INVALID_TAG = -40;
    public static final int KM_ERROR_INVALID_USER_ID = -15;
    public static final int KM_ERROR_KEY_EXPIRED = -25;
    public static final int KM_ERROR_KEY_EXPORT_OPTIONS_INVALID = -22;
    public static final int KM_ERROR_KEY_MAX_OPS_EXCEEDED = -56;
    public static final int KM_ERROR_KEY_NOT_YET_VALID = -24;
    public static final int KM_ERROR_KEY_RATE_LIMIT_EXCEEDED = -54;
    public static final int KM_ERROR_KEY_USER_NOT_AUTHENTICATED = -26;
    public static final int KM_ERROR_MEMORY_ALLOCATION_FAILED = -41;
    public static final int KM_ERROR_MISSING_MAC_LENGTH = -53;
    public static final int KM_ERROR_MISSING_MIN_MAC_LENGTH = -58;
    public static final int KM_ERROR_MISSING_NONCE = -51;
    public static final int KM_ERROR_OK = 0;
    public static final int KM_ERROR_OPERATION_CANCELLED = -46;
    public static final int KM_ERROR_OUTPUT_PARAMETER_NULL = -27;
    public static final int KM_ERROR_ROOT_OF_TRUST_ALREADY_SET = -1;
    public static final int KM_ERROR_SECURE_HW_ACCESS_DENIED = -45;
    public static final int KM_ERROR_SECURE_HW_BUSY = -48;
    public static final int KM_ERROR_SECURE_HW_COMMUNICATION_FAILED = -49;
    public static final int KM_ERROR_TOO_MANY_OPERATIONS = -31;
    public static final int KM_ERROR_UNEXPECTED_NULL_POINTER = -32;
    public static final int KM_ERROR_UNIMPLEMENTED = -100;
    public static final int KM_ERROR_UNKNOWN_ERROR = -1000;
    public static final int KM_ERROR_UNSUPPORTED_ALGORITHM = -4;
    public static final int KM_ERROR_UNSUPPORTED_BLOCK_MODE = -7;
    public static final int KM_ERROR_UNSUPPORTED_DIGEST = -12;
    public static final int KM_ERROR_UNSUPPORTED_EC_FIELD = -50;
    public static final int KM_ERROR_UNSUPPORTED_KEY_ENCRYPTION_ALGORITHM = -19;
    public static final int KM_ERROR_UNSUPPORTED_KEY_FORMAT = -17;
    public static final int KM_ERROR_UNSUPPORTED_KEY_SIZE = -6;
    public static final int KM_ERROR_UNSUPPORTED_KEY_VERIFICATION_ALGORITHM = -20;
    public static final int KM_ERROR_UNSUPPORTED_MAC_LENGTH = -9;
    public static final int KM_ERROR_UNSUPPORTED_MIN_MAC_LENGTH = -59;
    public static final int KM_ERROR_UNSUPPORTED_PADDING_MODE = -10;
    public static final int KM_ERROR_UNSUPPORTED_PURPOSE = -2;
    public static final int KM_ERROR_UNSUPPORTED_TAG = -39;
    public static final int KM_ERROR_VERIFICATION_FAILED = -30;
    public static final int KM_ERROR_VERSION_MISMATCH = -101;
    public static final int KM_INVALID = 0;
    public static final int KM_KEY_FORMAT_PKCS8 = 1;
    public static final int KM_KEY_FORMAT_RAW = 3;
    public static final int KM_KEY_FORMAT_SOTER_JSON = 1000;
    public static final int KM_KEY_FORMAT_X509 = 0;
    public static final int KM_MODE_CBC = 2;
    public static final int KM_MODE_CTR = 3;
    public static final int KM_MODE_ECB = 1;
    public static final int KM_MODE_GCM = 32;
    public static final int KM_ORIGIN_GENERATED = 0;
    public static final int KM_ORIGIN_IMPORTED = 2;
    public static final int KM_ORIGIN_UNKNOWN = 3;
    public static final int KM_PAD_NONE = 1;
    public static final int KM_PAD_PKCS7 = 64;
    public static final int KM_PAD_RSA_OAEP = 2;
    public static final int KM_PAD_RSA_PKCS1_1_5_ENCRYPT = 4;
    public static final int KM_PAD_RSA_PKCS1_1_5_SIGN = 5;
    public static final int KM_PAD_RSA_PSS = 3;
    public static final int KM_PURPOSE_DECRYPT = 1;
    public static final int KM_PURPOSE_ENCRYPT = 0;
    public static final int KM_PURPOSE_SIGN = 2;
    public static final int KM_PURPOSE_SOTER_ATTEST_KEY = -16711679;
    public static final int KM_PURPOSE_VERIFY = 3;
    public static final int KM_TAG_ACTIVE_DATETIME = 1610613136;
    public static final int KM_TAG_ADDITIONAL_PROTECTION_ALLOWED = 1879147493;
    public static final int KM_TAG_ADD_BIND_TEMPLATE = 1879147495;
    public static final int KM_TAG_ADD_USER_AUTH_TYPE = 805405670;
    public static final int KM_TAG_ALGORITHM = 268435458;
    public static final int KM_TAG_ALLOW_WHILE_ON_BODY = 1879048698;
    public static final int KM_TAG_ALL_APPLICATIONS = 1879048792;
    public static final int KM_TAG_ALL_USERS = 1879048692;
    public static final int KM_TAG_APPLICATION_ID = -1879047591;
    public static final int KM_TAG_ASSETSTORE_ACCESSLIMITATION = 268525463;
    public static final int KM_TAG_ASSETSTORE_AEADASSET = -1878958190;
    public static final int KM_TAG_ASSETSTORE_ALIAS_FORECDH = -1878958178;
    public static final int KM_TAG_ASSETSTORE_APPTAG = -1878958192;
    public static final int KM_TAG_ASSETSTORE_ASSETHANDLE = -1878958184;
    public static final int KM_TAG_ASSETSTORE_ASSETTYPE = 268525459;
    public static final int KM_TAG_ASSETSTORE_AUTHENTICATELIMITATION = 268525461;
    public static final int KM_TAG_ASSETSTORE_BATCHASSET = -1878958191;
    public static final int KM_TAG_ASSETSTORE_CLONE_INDATA = -1878958181;
    public static final int KM_TAG_ASSETSTORE_EXTINFO = -1878958188;
    public static final int KM_TAG_ASSETSTORE_OPCODE = 268525466;
    public static final int KM_TAG_ASSETSTORE_PUBLICKEY = -1878958180;
    public static final int KM_TAG_ASSETSTORE_PUBLICKEY_FORECDH = -1878958179;
    public static final int KM_TAG_ASSETSTORE_RESETFLAG = 268525465;
    public static final int KM_TAG_ASSETSTORE_SYNCLIMITATION = 268525462;
    public static final int KM_TAG_ASSOCIATED_DATA = -1879047192;
    public static final int KM_TAG_ATTESTATION_APPLICATION_ID = -1879047483;
    public static final int KM_TAG_ATTESTATION_CHALLENGE = -1879047484;
    public static final int KM_TAG_ATTESTATION_ID_BRAND = -1879047482;
    public static final int KM_TAG_ATTESTATION_ID_DEVICE = -1879047481;
    public static final int KM_TAG_ATTESTATION_ID_IMEI = -1879047478;
    public static final int KM_TAG_ATTESTATION_ID_MANUFACTURER = -1879047476;
    public static final int KM_TAG_ATTESTATION_ID_MEID = -1879047477;
    public static final int KM_TAG_ATTESTATION_ID_MODEL = -1879047475;
    public static final int KM_TAG_ATTESTATION_ID_PRODUCT = -1879047480;
    public static final int KM_TAG_ATTESTATION_ID_SERIAL = -1879047479;
    public static final int KM_TAG_AUTH_TIMEOUT = 805306873;
    public static final int KM_TAG_AUTH_TOKEN = -1879047190;
    public static final int KM_TAG_BLOB_USAGE_REQUIREMENTS = 268436161;
    public static final int KM_TAG_BLOCK_MODE = 536870916;
    public static final int KM_TAG_CALLER_NONCE = 1879048199;
    public static final int KM_TAG_CREATION_DATETIME = 1610613437;
    public static final int KM_TAG_DIGEST = 536870917;
    public static final int KM_TAG_INCLUDE_UNIQUE_ID = 1879048394;
    public static final int KM_TAG_INVALID = 0;
    public static final int KM_TAG_KEY_SIZE = 805306371;
    public static final int KM_TAG_MAC_LENGTH = 805307371;
    public static final int KM_TAG_MAX_USES_PER_BOOT = 805306772;
    public static final int KM_TAG_MIN_MAC_LENGTH = 805306376;
    public static final int KM_TAG_MIN_SECONDS_BETWEEN_OPS = 805306771;
    public static final int KM_TAG_NONCE = -1879047191;
    public static final int KM_TAG_NO_AUTH_REQUIRED = 1879048695;
    public static final int KM_TAG_ORIGIN = 268436158;
    public static final int KM_TAG_ORIGINATION_EXPIRE_DATETIME = 1610613137;
    public static final int KM_TAG_PADDING = 536870918;
    public static final int KM_TAG_PURPOSE = 536870913;
    public static final int KM_TAG_RESCOPING_ADD = 536871013;
    public static final int KM_TAG_RESCOPING_DEL = 536871014;
    public static final int KM_TAG_ROLLBACK_RESISTANT = 1879048895;
    public static final int KM_TAG_ROOT_OF_TRUST = -1879047488;
    public static final int KM_TAG_RSA_PUBLIC_EXPONENT = 1342177480;
    public static final int KM_TAG_SOTER_AUTO_ADD_COUNTER_WHEN_GET_PUBLIC_KEY = 1879059196;
    public static final int KM_TAG_SOTER_AUTO_SIGNED_COMMON_KEY_WHEN_GET_PUBLIC_KEY = -1879037189;
    public static final int KM_TAG_SOTER_AUTO_SIGNED_COMMON_KEY_WHEN_GET_PUBLIC_KEY_BLOB = -1879037184;
    public static final int KM_TAG_SOTER_IS_AUTO_SIGNED_WITH_COMMON_KEY_WHEN_GET_PUBLIC_KEY = 1879059194;
    public static final int KM_TAG_SOTER_IS_FROM_SOTER = 1879059192;
    public static final int KM_TAG_SOTER_IS_SECMSG_FID_COUNTER_SIGNED_WHEN_SIGN = 1879059197;
    public static final int KM_TAG_SOTER_UID = 805317375;
    public static final int KM_TAG_SOTER_USE_NEXT_ATTK = 1879059198;
    public static final int KM_TAG_UNIQUE_ID = -1879047485;
    public static final int KM_TAG_USAGE_EXPIRE_DATETIME = 1610613138;
    public static final int KM_TAG_USER_AUTH_TYPE = 268435960;
    public static final int KM_TAG_USER_ID = 805306869;
    public static final int KM_TAG_USER_SECURE_ID = -1610612234;
    public static final int KM_UINT = 805306368;
    public static final int KM_UINT_REP = 1073741824;
    public static final int KM_ULONG = 1342177280;
    public static final int KM_ULONG_REP = -1610612736;
    public static final Map<Integer, String> sErrorCodeToString = new HashMap();

    private HwKeymasterDefs() {
    }

    static {
        sErrorCodeToString.put(Integer.valueOf(0), "OK");
        sErrorCodeToString.put(Integer.valueOf(-2), "Unsupported purpose");
        sErrorCodeToString.put(Integer.valueOf(-3), "Incompatible purpose");
        sErrorCodeToString.put(Integer.valueOf(-4), "Unsupported algorithm");
        sErrorCodeToString.put(Integer.valueOf(-5), "Incompatible algorithm");
        sErrorCodeToString.put(Integer.valueOf(-6), "Unsupported key size");
        sErrorCodeToString.put(Integer.valueOf(-7), "Unsupported block mode");
        sErrorCodeToString.put(Integer.valueOf(-8), "Incompatible block mode");
        sErrorCodeToString.put(Integer.valueOf(-9), "Unsupported MAC or authentication tag length");
        sErrorCodeToString.put(Integer.valueOf(-10), "Unsupported padding mode");
        sErrorCodeToString.put(Integer.valueOf(-11), "Incompatible padding mode");
        sErrorCodeToString.put(Integer.valueOf(-12), "Unsupported digest");
        sErrorCodeToString.put(Integer.valueOf(-13), "Incompatible digest");
        sErrorCodeToString.put(Integer.valueOf(-14), "Invalid expiration time");
        sErrorCodeToString.put(Integer.valueOf(-15), "Invalid user ID");
        sErrorCodeToString.put(Integer.valueOf(-16), "Invalid user authorization timeout");
        sErrorCodeToString.put(Integer.valueOf(-17), "Unsupported key format");
        sErrorCodeToString.put(Integer.valueOf(-18), "Incompatible key format");
        sErrorCodeToString.put(Integer.valueOf(-21), "Invalid input length");
        sErrorCodeToString.put(Integer.valueOf(-24), "Key not yet valid");
        sErrorCodeToString.put(Integer.valueOf(-25), "Key expired");
        sErrorCodeToString.put(Integer.valueOf(-26), "Key user not authenticated");
        sErrorCodeToString.put(Integer.valueOf(-28), "Invalid operation handle");
        sErrorCodeToString.put(Integer.valueOf(-30), "Signature/MAC verification failed");
        sErrorCodeToString.put(Integer.valueOf(-31), "Too many operations");
        sErrorCodeToString.put(Integer.valueOf(-33), "Invalid key blob");
        sErrorCodeToString.put(Integer.valueOf(-38), "Invalid argument");
        sErrorCodeToString.put(Integer.valueOf(-39), "Unsupported tag");
        sErrorCodeToString.put(Integer.valueOf(-40), "Invalid tag");
        sErrorCodeToString.put(Integer.valueOf(-41), "Memory allocation failed");
        sErrorCodeToString.put(Integer.valueOf(-50), "Unsupported EC field");
        sErrorCodeToString.put(Integer.valueOf(-51), "Required IV missing");
        sErrorCodeToString.put(Integer.valueOf(-52), "Invalid IV");
        sErrorCodeToString.put(Integer.valueOf(-55), "Caller-provided IV not permitted");
        sErrorCodeToString.put(Integer.valueOf(-57), "Invalid MAC or authentication tag length");
        sErrorCodeToString.put(Integer.valueOf(-100), "Not implemented");
        sErrorCodeToString.put(Integer.valueOf(KM_ERROR_UNKNOWN_ERROR), "Unknown error");
    }

    public static int getTagType(int tag) {
        return -268435456 & tag;
    }

    public static String getErrorMessage(int errorCode) {
        String result = (String) sErrorCodeToString.get(Integer.valueOf(errorCode));
        if (result != null) {
            return result;
        }
        return String.valueOf(errorCode);
    }
}
