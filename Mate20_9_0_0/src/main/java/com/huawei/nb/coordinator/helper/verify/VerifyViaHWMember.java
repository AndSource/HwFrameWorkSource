package com.huawei.nb.coordinator.helper.verify;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import com.huawei.nb.coordinator.common.CoordinatorJsonAnalyzer;
import com.huawei.nb.coordinator.helper.http.HttpClient;
import com.huawei.nb.coordinator.helper.http.HttpRequest.Builder;
import com.huawei.nb.coordinator.helper.http.HttpRequestBody;
import com.huawei.nb.coordinator.helper.http.HttpResponse;
import com.huawei.nb.io.IOUtils;
import com.huawei.nb.utils.DeviceUtil;
import com.huawei.nb.utils.logger.DSLog;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

public class VerifyViaHWMember implements IVerify {
    private static final String HWMEMBER_FLAG = "PUBPRI";
    private static final String HWMEMBER_TOKEN_HEADER = "PKI_token";
    private static final String HWMEMBER_URL_BETA = "https://lfopenplatformtest.hwcloudtest.cn:8543";
    private static final String HWMEMBER_URL_FORMAL = "https://travelservice-drcn.emui.hicloud.com:8443";
    private static final String HWMEMBER_URL_SIM_FORMAL = "https://lfopenplatformsectest.hwcloudtest.cn:8444";
    private static final String SIGNATURE_TYPE = "OSPID";
    private static final String VERIFY_URL_INTERFACE = "/api/v1/authservice/pki/session";
    private String TAG = "VerifyViaHWMember";

    public String getSignature(String challenge, String packageName) {
        try {
            return getSignatureWithoutCatchThrowable(challenge, packageName);
        } catch (Throwable throwable) {
            DSLog.e(this.TAG + " dit not get the signature", new Object[0]);
            StringBuffer stackTrace = new StringBuffer();
            for (StackTraceElement elem : throwable.getStackTrace()) {
                stackTrace.append(elem);
            }
            DSLog.e(this.TAG + " caught a throwable,message: " + throwable.getMessage() + ", cause" + throwable.getCause() + ", stacktrace: " + stackTrace, new Object[0]);
            return "";
        }
    }

    private String getSignatureWithoutCatchThrowable(String challenge, String packageName) {
        byte[] challengeByteArr = challenge.getBytes(StandardCharsets.UTF_8);
        if (DeviceUtil.getIntFiled(DeviceUtil.DEVICE_ATTESTATION_MANAGER, "KEY_INDEX_HWCLOUD", -1) == -1) {
            DSLog.e("getAttestationSignature failed: keyIndexHwCloud == -1", new Object[0]);
            return "";
        }
        if (DeviceUtil.getIntFiled(DeviceUtil.DEVICE_ATTESTATION_MANAGER, "DEVICE_ID_TYPE_EMMC", -1) == -1) {
            DSLog.e(this.TAG + "getAttestationSignature failed: deviceIdTypeEmmc == -1", new Object[0]);
            return "";
        }
        try {
            Class<?> cls = Class.forName(DeviceUtil.DEVICE_ATTESTATION_MANAGER);
            if (cls == null) {
                DSLog.e(this.TAG + " can not get cls.", new Object[0]);
                return "";
            }
            Object retObj = cls.getDeclaredMethod("getAttestationSignatureWithPkgName", new Class[]{Integer.TYPE, Integer.TYPE, String.class, byte[].class, String.class}).invoke(cls.newInstance(), new Object[]{Integer.valueOf(keyIndexHwCloud), Integer.valueOf(deviceIdTypeEmmc), "OSPID", challengeByteArr, packageName});
            if (retObj != null && (retObj instanceof byte[])) {
                return Base64.encodeToString((byte[]) retObj, 0);
            }
            DSLog.e(this.TAG + " can not get signature.", new Object[0]);
            return "";
        } catch (ClassNotFoundException e) {
            DSLog.e(this.TAG + "getAttestationSignature failed: ", new Object[0]);
            return "";
        } catch (NoSuchMethodException e2) {
            DSLog.e(this.TAG + "getAttestationSignature failed: ", new Object[0]);
            return "";
        } catch (InvocationTargetException e3) {
            DSLog.e(this.TAG + "getAttestationSignature failed: ", new Object[0]);
            return "";
        } catch (IllegalAccessException e4) {
            DSLog.e(this.TAG + "getAttestationSignature failed: ", new Object[0]);
            return "";
        } catch (InstantiationException e5) {
            DSLog.e(this.TAG + "getAttestationSignature failed: ", new Object[0]);
            return "";
        }
    }

    public boolean generateAuthorization(Context context, Builder builder, String appID) throws VerifyException {
        HttpResponse sessionAndRandResponse = getSessionAndRand(appID, builder.build().getUrl());
        String sessionAndRand = sessionAndRandResponse.getResponseString();
        if (sessionAndRandResponse.getStatusCode() != 200) {
            DSLog.e(this.TAG + ": status code is not 200: " + sessionAndRandResponse.getStatusCode(), new Object[0]);
            throw new VerifyException(sessionAndRandResponse.getStatusCode(), " Fail to verify, error:" + sessionAndRandResponse.getResponseMsg());
        }
        String sessionId = CoordinatorJsonAnalyzer.getJsonValue(sessionAndRand, CoordinatorJsonAnalyzer.SESSIONID_TYPE);
        String rand = CoordinatorJsonAnalyzer.getJsonValue(sessionAndRand, CoordinatorJsonAnalyzer.RAND_TYPE);
        if (TextUtils.isEmpty(sessionId)) {
            DSLog.e(this.TAG + ": empty sessionId", new Object[0]);
            return false;
        } else if (TextUtils.isEmpty(rand)) {
            DSLog.e(this.TAG + ": empty rand", new Object[0]);
            return false;
        } else {
            String packageName = context.getPackageName();
            if (TextUtils.isEmpty(packageName)) {
                DSLog.e(this.TAG + ": empty packageName.", new Object[0]);
                return false;
            }
            String signature = getSignature(rand, packageName);
            if (TextUtils.isEmpty(signature)) {
                DSLog.e(this.TAG + ": empty signature.", new Object[0]);
                throw new VerifyException(-8, " signature is empty");
            }
            String emmcID = DeviceUtil.getEMMCID();
            if (TextUtils.isEmpty(emmcID)) {
                DSLog.e(this.TAG + " empty emmcID.", new Object[0]);
                return false;
            }
            String serialNumber = DeviceUtil.getSerialNumber();
            if (TextUtils.isEmpty(serialNumber)) {
                DSLog.e(this.TAG + " empty serialNumber.", new Object[0]);
                return false;
            }
            StringBuilder authorization = new StringBuilder();
            authorization.append(HWMEMBER_FLAG).append(" ").append(IVerifyVar.APPID_KEY).append("=").append(appID).append(",").append(IVerifyVar.SESSION_KEY).append("=").append(sessionId).append(",").append(IVerifyVar.SIGNATURE_KEY).append("=").append(signature).append(",").append("SN").append("=").append(serialNumber).append(",").append(IVerifyVar.EMMCID_KEY).append("=").append(emmcID).append(",").append(IVerifyVar.PACKAGE_NAME_KEY).append("=").append(packageName);
            String temp = authorization.toString().replace(IOUtils.LINE_SEPARATOR_UNIX, "");
            if (TextUtils.isEmpty(temp)) {
                DSLog.e(this.TAG + ": empty authorization", new Object[0]);
                return false;
            }
            builder.addRequestHeader(IVerifyVar.AUTHORIZATION_KEY, temp);
            return true;
        }
    }

    public String verifyTokenHeader() {
        return HWMEMBER_TOKEN_HEADER;
    }

    private HttpResponse getSessionAndRand(String appID, String requestUrl) {
        String verifyUrl = switchUrlEnvironment(requestUrl);
        return new HttpClient().newCall(new Builder().url(verifyUrl).get(new HttpRequestBody.Builder().add(IVerifyVar.APP_ID, appID).build()).build()).syncExecute();
    }

    private String switchUrlEnvironment(String requestUrl) {
        if (TextUtils.isEmpty(requestUrl)) {
            DSLog.e(" Input url is empty. Use default url.", new Object[0]);
            return "https://travelservice-drcn.emui.hicloud.com:8443/api/v1/authservice/pki/session";
        } else if (requestUrl.startsWith(HWMEMBER_URL_FORMAL)) {
            return "https://travelservice-drcn.emui.hicloud.com:8443/api/v1/authservice/pki/session";
        } else {
            if (requestUrl.startsWith(HWMEMBER_URL_SIM_FORMAL)) {
                return "https://lfopenplatformsectest.hwcloudtest.cn:8444/api/v1/authservice/pki/session";
            }
            if (requestUrl.startsWith(HWMEMBER_URL_BETA)) {
                return "https://lfopenplatformtest.hwcloudtest.cn:8543/api/v1/authservice/pki/session";
            }
            DSLog.e(" Input url is invalid. Use default url.", new Object[0]);
            return "https://travelservice-drcn.emui.hicloud.com:8443/api/v1/authservice/pki/session";
        }
    }
}
