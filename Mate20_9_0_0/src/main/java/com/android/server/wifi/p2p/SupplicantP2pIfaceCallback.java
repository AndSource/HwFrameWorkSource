package com.android.server.wifi.p2p;

import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIfaceCallback.Stub;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pProvDiscEvent;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceResponse;
import android.util.Log;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStatus;
import com.android.server.wifi.util.NativeUtil;
import java.util.ArrayList;
import java.util.Arrays;

public class SupplicantP2pIfaceCallback extends Stub {
    private static final boolean DBG = true;
    private static final String TAG = "SupplicantP2pIfaceCallback";
    private final String mInterface;
    private final WifiP2pMonitor mMonitor;

    public SupplicantP2pIfaceCallback(String iface, WifiP2pMonitor monitor) {
        this.mInterface = iface;
        this.mMonitor = monitor;
    }

    protected static void logd(String s) {
        Log.d(TAG, s);
    }

    public void onNetworkAdded(int networkId) {
    }

    public void onNetworkRemoved(int networkId) {
    }

    public void onDeviceFound(byte[] srcAddress, byte[] p2pDeviceAddress, byte[] primaryDeviceType, String deviceName, short configMethods, byte deviceCapabilities, int groupCapabilities, byte[] wfdDeviceInfo) {
        WifiP2pDevice device = new WifiP2pDevice();
        device.deviceName = deviceName;
        if (deviceName == null) {
            Log.e(TAG, "Missing device name.");
            return;
        }
        try {
            device.deviceAddress = NativeUtil.macAddressFromByteArray(p2pDeviceAddress);
            try {
                device.primaryDeviceType = NativeUtil.wpsDevTypeStringFromByteArray(primaryDeviceType);
                device.deviceCapability = deviceCapabilities;
                device.groupCapability = groupCapabilities;
                device.wpsConfigMethodsSupported = configMethods;
                device.status = 3;
                if (wfdDeviceInfo != null && wfdDeviceInfo.length >= 6) {
                    device.wfdInfo = new WifiP2pWfdInfo((wfdDeviceInfo[0] << 8) + wfdDeviceInfo[1], (wfdDeviceInfo[2] << 8) + wfdDeviceInfo[3], (wfdDeviceInfo[4] << 8) + wfdDeviceInfo[5]);
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Device discovered on ");
                stringBuilder.append(this.mInterface);
                stringBuilder.append(": ");
                stringBuilder.append(device);
                logd(stringBuilder.toString());
                this.mMonitor.broadcastP2pDeviceFound(this.mInterface, device);
            } catch (Exception e) {
                Log.e(TAG, "Could not encode device primary type.", e);
            }
        } catch (Exception e2) {
            Log.e(TAG, "Could not decode device address.", e2);
        }
    }

    public void onDeviceLost(byte[] p2pDeviceAddress) {
        WifiP2pDevice device = new WifiP2pDevice();
        try {
            device.deviceAddress = NativeUtil.macAddressFromByteArray(p2pDeviceAddress);
            device.status = 4;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Device lost on ");
            stringBuilder.append(this.mInterface);
            stringBuilder.append(": ");
            stringBuilder.append(device);
            logd(stringBuilder.toString());
            this.mMonitor.broadcastP2pDeviceLost(this.mInterface, device);
        } catch (Exception e) {
            Log.e(TAG, "Could not decode device address.", e);
        }
    }

    public void onFindStopped() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Search stopped on ");
        stringBuilder.append(this.mInterface);
        logd(stringBuilder.toString());
        this.mMonitor.broadcastP2pFindStopped(this.mInterface);
    }

    public void onGoNegotiationRequest(byte[] srcAddress, short passwordId) {
        WifiP2pConfig config = new WifiP2pConfig();
        try {
            config.deviceAddress = NativeUtil.macAddressFromByteArray(srcAddress);
            config.wps = new WpsInfo();
            if (passwordId != (short) 1) {
                switch (passwordId) {
                    case (short) 4:
                        config.wps.setup = 0;
                        break;
                    case (short) 5:
                        config.wps.setup = 2;
                        break;
                    default:
                        config.wps.setup = 0;
                        break;
                }
            }
            config.wps.setup = 1;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Group Owner negotiation initiated on ");
            stringBuilder.append(this.mInterface);
            stringBuilder.append(": ");
            stringBuilder.append(config);
            logd(stringBuilder.toString());
            this.mMonitor.broadcastP2pGoNegotiationRequest(this.mInterface, config);
        } catch (Exception e) {
            Log.e(TAG, "Could not decode device address.", e);
        }
    }

    public void onGoNegotiationCompleted(int status) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Group Owner negotiation completed with status: ");
        stringBuilder.append(status);
        logd(stringBuilder.toString());
        P2pStatus result = halStatusToP2pStatus(status);
        if (result == P2pStatus.SUCCESS) {
            this.mMonitor.broadcastP2pGoNegotiationSuccess(this.mInterface);
        } else {
            this.mMonitor.broadcastP2pGoNegotiationFailure(this.mInterface, result);
        }
    }

    public void onGroupFormationSuccess() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Group formation successful on ");
        stringBuilder.append(this.mInterface);
        logd(stringBuilder.toString());
        this.mMonitor.broadcastP2pGroupFormationSuccess(this.mInterface);
    }

    public void onGroupFormationFailure(String failureReason) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Group formation failed on ");
        stringBuilder.append(this.mInterface);
        stringBuilder.append(": ");
        stringBuilder.append(failureReason);
        logd(stringBuilder.toString());
        this.mMonitor.broadcastP2pGroupFormationFailure(this.mInterface, failureReason);
    }

    public void onGroupStarted(String groupIfName, boolean isGo, ArrayList<Byte> ssid, int frequency, byte[] psk, String passphrase, byte[] goDeviceAddress, boolean isPersistent) {
        if (groupIfName == null) {
            Log.e(TAG, "Missing group interface name.");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Group ");
        stringBuilder.append(groupIfName);
        stringBuilder.append(" started on ");
        stringBuilder.append(this.mInterface);
        logd(stringBuilder.toString());
        WifiP2pGroup group = new WifiP2pGroup();
        group.setInterface(groupIfName);
        try {
            group.setNetworkName(NativeUtil.removeEnclosingQuotes(NativeUtil.encodeSsid(ssid)));
            group.setIsGroupOwner(isGo);
            group.setPassphrase(passphrase);
            if (isPersistent) {
                group.setNetworkId(-2);
            } else {
                group.setNetworkId(-1);
            }
            WifiP2pDevice owner = new WifiP2pDevice();
            try {
                owner.deviceAddress = NativeUtil.macAddressFromByteArray(goDeviceAddress);
                group.setOwner(owner);
                group.setFrequence(frequency);
                this.mMonitor.broadcastP2pGroupStarted(this.mInterface, group);
            } catch (Exception e) {
                Log.e(TAG, "Could not decode Group Owner address.", e);
            }
        } catch (Exception e2) {
            Log.e(TAG, "Could not encode SSID.", e2);
        }
    }

    public void onGroupRemoved(String groupIfName, boolean isGo) {
        if (groupIfName == null) {
            Log.e(TAG, "Missing group name.");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Group ");
        stringBuilder.append(groupIfName);
        stringBuilder.append(" removed from ");
        stringBuilder.append(this.mInterface);
        logd(stringBuilder.toString());
        WifiP2pGroup group = new WifiP2pGroup();
        group.setInterface(groupIfName);
        group.setIsGroupOwner(isGo);
        this.mMonitor.broadcastP2pGroupRemoved(this.mInterface, group);
    }

    public void onInvitationReceived(byte[] srcAddress, byte[] goDeviceAddress, byte[] bssid, int persistentNetworkId, int operatingFrequency) {
        WifiP2pGroup group = new WifiP2pGroup();
        group.setNetworkId(persistentNetworkId);
        WifiP2pDevice client = new WifiP2pDevice();
        try {
            client.deviceAddress = NativeUtil.macAddressFromByteArray(srcAddress);
            group.addClient(client);
            WifiP2pDevice owner = new WifiP2pDevice();
            try {
                owner.deviceAddress = NativeUtil.macAddressFromByteArray(goDeviceAddress);
                group.setOwner(owner);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Invitation received on ");
                stringBuilder.append(this.mInterface);
                stringBuilder.append(": ");
                stringBuilder.append(group);
                logd(stringBuilder.toString());
                this.mMonitor.broadcastP2pInvitationReceived(this.mInterface, group);
            } catch (Exception e) {
                Log.e(TAG, "Could not decode Group Owner MAC address.", e);
            }
        } catch (Exception e2) {
            Log.e(TAG, "Could not decode MAC address.", e2);
        }
    }

    public void onInvitationResult(byte[] bssid, int status) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invitation completed with status: ");
        stringBuilder.append(status);
        logd(stringBuilder.toString());
        this.mMonitor.broadcastP2pInvitationResult(this.mInterface, halStatusToP2pStatus(status));
    }

    public void onProvisionDiscoveryCompleted(byte[] p2pDeviceAddress, boolean isRequest, byte status, short configMethods, String generatedPin) {
        if (status != (byte) 0) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Provision discovery failed: ");
            stringBuilder.append(status);
            Log.e(str, stringBuilder.toString());
            this.mMonitor.broadcastP2pProvisionDiscoveryFailure(this.mInterface);
            return;
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Provision discovery ");
        stringBuilder2.append(isRequest ? "request" : "response");
        stringBuilder2.append(" for WPS Config method: ");
        stringBuilder2.append(configMethods);
        logd(stringBuilder2.toString());
        WifiP2pProvDiscEvent event = new WifiP2pProvDiscEvent();
        event.device = new WifiP2pDevice();
        try {
            event.device.deviceAddress = NativeUtil.macAddressFromByteArray(p2pDeviceAddress);
            if ((configMethods & 128) != 0) {
                if (isRequest) {
                    event.event = 1;
                    this.mMonitor.broadcastP2pProvisionDiscoveryPbcRequest(this.mInterface, event);
                } else {
                    event.event = 2;
                    this.mMonitor.broadcastP2pProvisionDiscoveryPbcResponse(this.mInterface, event);
                }
            } else if (!isRequest && (configMethods & 256) != 0) {
                event.event = 4;
                event.pin = generatedPin;
                this.mMonitor.broadcastP2pProvisionDiscoveryShowPin(this.mInterface, event);
            } else if (!isRequest && (configMethods & 8) != 0) {
                event.event = 3;
                this.mMonitor.broadcastP2pProvisionDiscoveryEnterPin(this.mInterface, event);
            } else if (isRequest && (configMethods & 8) != 0) {
                event.event = 4;
                event.pin = generatedPin;
                this.mMonitor.broadcastP2pProvisionDiscoveryShowPin(this.mInterface, event);
            } else if (!isRequest || (configMethods & 256) == 0) {
                String str2 = TAG;
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("Unsupported config methods: ");
                stringBuilder3.append(configMethods);
                Log.e(str2, stringBuilder3.toString());
            } else {
                event.event = 3;
                this.mMonitor.broadcastP2pProvisionDiscoveryEnterPin(this.mInterface, event);
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not decode MAC address.", e);
        }
    }

    public void onServiceDiscoveryResponse(byte[] srcAddress, short updateIndicator, ArrayList<Byte> tlvs) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Service discovery response received on ");
        stringBuilder.append(this.mInterface);
        logd(stringBuilder.toString());
        try {
            this.mMonitor.broadcastP2pServiceDiscoveryResponse(this.mInterface, WifiP2pServiceResponse.newInstance(NativeUtil.macAddressFromByteArray(srcAddress), NativeUtil.byteArrayFromArrayList(tlvs)));
        } catch (Exception e) {
            Log.e(TAG, "Could not process service discovery response.", e);
        }
    }

    private WifiP2pDevice createStaEventDevice(byte[] srcAddress, byte[] p2pDeviceAddress) {
        byte[] deviceAddressBytes;
        WifiP2pDevice device = new WifiP2pDevice();
        if (Arrays.equals(NativeUtil.ANY_MAC_BYTES, p2pDeviceAddress)) {
            deviceAddressBytes = srcAddress;
        } else {
            deviceAddressBytes = p2pDeviceAddress;
        }
        try {
            device.deviceAddress = NativeUtil.macAddressFromByteArray(deviceAddressBytes);
            return device;
        } catch (Exception e) {
            Log.e(TAG, "Could not decode MAC address", e);
            return null;
        }
    }

    public void onStaAuthorized(byte[] srcAddress, byte[] p2pDeviceAddress) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("STA authorized on ");
        stringBuilder.append(this.mInterface);
        logd(stringBuilder.toString());
        WifiP2pDevice device = createStaEventDevice(srcAddress, p2pDeviceAddress);
        if (device != null) {
            this.mMonitor.broadcastP2pApStaConnected(this.mInterface, device);
        }
    }

    public void onStaDeauthorized(byte[] srcAddress, byte[] p2pDeviceAddress) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("STA deauthorized on ");
        stringBuilder.append(this.mInterface);
        logd(stringBuilder.toString());
        WifiP2pDevice device = createStaEventDevice(srcAddress, p2pDeviceAddress);
        if (device != null) {
            this.mMonitor.broadcastP2pApStaDisconnected(this.mInterface, device);
        }
    }

    private static P2pStatus halStatusToP2pStatus(int status) {
        P2pStatus result = P2pStatus.UNKNOWN;
        switch (status) {
            case 0:
            case 12:
                return P2pStatus.SUCCESS;
            case 1:
                return P2pStatus.INFORMATION_IS_CURRENTLY_UNAVAILABLE;
            case 2:
                return P2pStatus.INCOMPATIBLE_PARAMETERS;
            case 3:
                return P2pStatus.LIMIT_REACHED;
            case 4:
                return P2pStatus.INVALID_PARAMETER;
            case 5:
                return P2pStatus.UNABLE_TO_ACCOMMODATE_REQUEST;
            case 6:
                return P2pStatus.PREVIOUS_PROTOCOL_ERROR;
            case 7:
                return P2pStatus.NO_COMMON_CHANNEL;
            case 8:
                return P2pStatus.UNKNOWN_P2P_GROUP;
            case 9:
                return P2pStatus.BOTH_GO_INTENT_15;
            case 10:
                return P2pStatus.INCOMPATIBLE_PROVISIONING_METHOD;
            case 11:
                return P2pStatus.REJECTED_BY_USER;
            default:
                return result;
        }
    }
}
