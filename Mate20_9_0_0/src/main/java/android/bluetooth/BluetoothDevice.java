package android.bluetooth;

import android.annotation.SystemApi;
import android.bluetooth.IBluetoothManagerCallback.Stub;
import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public final class BluetoothDevice implements Parcelable {
    @SystemApi
    public static final int ACCESS_ALLOWED = 1;
    @SystemApi
    public static final int ACCESS_REJECTED = 2;
    @SystemApi
    public static final int ACCESS_UNKNOWN = 0;
    public static final String ACTION_ACL_CONNECTED = "android.bluetooth.device.action.ACL_CONNECTED";
    public static final String ACTION_ACL_DISCONNECTED = "android.bluetooth.device.action.ACL_DISCONNECTED";
    public static final String ACTION_ACL_DISCONNECT_REQUESTED = "android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED";
    public static final String ACTION_ALIAS_CHANGED = "android.bluetooth.device.action.ALIAS_CHANGED";
    public static final String ACTION_BATTERY_LEVEL_CHANGED = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED";
    public static final String ACTION_BOND_STATE_CHANGED = "android.bluetooth.device.action.BOND_STATE_CHANGED";
    public static final String ACTION_CLASS_CHANGED = "android.bluetooth.device.action.CLASS_CHANGED";
    public static final String ACTION_CONNECTION_ACCESS_CANCEL = "android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL";
    public static final String ACTION_CONNECTION_ACCESS_REPLY = "android.bluetooth.device.action.CONNECTION_ACCESS_REPLY";
    public static final String ACTION_CONNECTION_ACCESS_REQUEST = "android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST";
    public static final String ACTION_DISAPPEARED = "android.bluetooth.device.action.DISAPPEARED";
    public static final String ACTION_FOUND = "android.bluetooth.device.action.FOUND";
    public static final String ACTION_MAS_INSTANCE = "android.bluetooth.device.action.MAS_INSTANCE";
    public static final String ACTION_NAME_CHANGED = "android.bluetooth.device.action.NAME_CHANGED";
    public static final String ACTION_NAME_FAILED = "android.bluetooth.device.action.NAME_FAILED";
    public static final String ACTION_PAIRING_CANCEL = "android.bluetooth.device.action.PAIRING_CANCEL";
    public static final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
    public static final String ACTION_SDP_RECORD = "android.bluetooth.device.action.SDP_RECORD";
    public static final String ACTION_UUID = "android.bluetooth.device.action.UUID";
    public static final int BATTERY_LEVEL_UNKNOWN = -1;
    public static final int BOND_BONDED = 12;
    public static final int BOND_BONDING = 11;
    public static final int BOND_NONE = 10;
    public static final int BOND_SUCCESS = 0;
    public static final int CONNECTION_ACCESS_NO = 2;
    public static final int CONNECTION_ACCESS_YES = 1;
    private static final int CONNECTION_STATE_CONNECTED = 1;
    private static final int CONNECTION_STATE_DISCONNECTED = 0;
    private static final int CONNECTION_STATE_ENCRYPTED_BREDR = 2;
    private static final int CONNECTION_STATE_ENCRYPTED_LE = 4;
    public static final Creator<BluetoothDevice> CREATOR = new Creator<BluetoothDevice>() {
        public BluetoothDevice createFromParcel(Parcel in) {
            return new BluetoothDevice(in.readString());
        }

        public BluetoothDevice[] newArray(int size) {
            return new BluetoothDevice[size];
        }
    };
    private static final boolean DBG = false;
    public static final int DEVICE_TYPE_CLASSIC = 1;
    public static final int DEVICE_TYPE_DUAL = 3;
    public static final int DEVICE_TYPE_LE = 2;
    public static final int DEVICE_TYPE_UNKNOWN = 0;
    public static final int ERROR = Integer.MIN_VALUE;
    public static final String EXTRA_ACCESS_REQUEST_TYPE = "android.bluetooth.device.extra.ACCESS_REQUEST_TYPE";
    public static final String EXTRA_ALWAYS_ALLOWED = "android.bluetooth.device.extra.ALWAYS_ALLOWED";
    public static final String EXTRA_BATTERY_LEVEL = "android.bluetooth.device.extra.BATTERY_LEVEL";
    public static final String EXTRA_BOND_STATE = "android.bluetooth.device.extra.BOND_STATE";
    public static final String EXTRA_CLASS = "android.bluetooth.device.extra.CLASS";
    public static final String EXTRA_CLASS_NAME = "android.bluetooth.device.extra.CLASS_NAME";
    public static final String EXTRA_CONNECTION_ACCESS_RESULT = "android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT";
    public static final String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
    public static final String EXTRA_MAS_INSTANCE = "android.bluetooth.device.extra.MAS_INSTANCE";
    public static final String EXTRA_NAME = "android.bluetooth.device.extra.NAME";
    public static final String EXTRA_PACKAGE_NAME = "android.bluetooth.device.extra.PACKAGE_NAME";
    public static final String EXTRA_PAIRING_KEY = "android.bluetooth.device.extra.PAIRING_KEY";
    public static final String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
    public static final String EXTRA_PREVIOUS_BOND_STATE = "android.bluetooth.device.extra.PREVIOUS_BOND_STATE";
    public static final String EXTRA_REASON = "android.bluetooth.device.extra.REASON";
    public static final String EXTRA_RSSI = "android.bluetooth.device.extra.RSSI";
    public static final String EXTRA_SDP_RECORD = "android.bluetooth.device.extra.SDP_RECORD";
    public static final String EXTRA_SDP_SEARCH_STATUS = "android.bluetooth.device.extra.SDP_SEARCH_STATUS";
    public static final String EXTRA_UUID = "android.bluetooth.device.extra.UUID";
    public static final int PAIRING_VARIANT_CONSENT = 3;
    public static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
    public static final int PAIRING_VARIANT_DISPLAY_PIN = 5;
    public static final int PAIRING_VARIANT_OOB_CONSENT = 6;
    public static final int PAIRING_VARIANT_PASSKEY = 1;
    public static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    public static final int PAIRING_VARIANT_PIN = 0;
    public static final int PAIRING_VARIANT_PIN_16_DIGITS = 7;
    public static final int PHY_LE_1M = 1;
    public static final int PHY_LE_1M_MASK = 1;
    public static final int PHY_LE_2M = 2;
    public static final int PHY_LE_2M_MASK = 2;
    public static final int PHY_LE_CODED = 3;
    public static final int PHY_LE_CODED_MASK = 4;
    public static final int PHY_OPTION_NO_PREFERRED = 0;
    public static final int PHY_OPTION_S2 = 1;
    public static final int PHY_OPTION_S8 = 2;
    public static final int REQUEST_TYPE_MESSAGE_ACCESS = 3;
    public static final int REQUEST_TYPE_PHONEBOOK_ACCESS = 2;
    public static final int REQUEST_TYPE_PROFILE_CONNECTION = 1;
    public static final int REQUEST_TYPE_SIM_ACCESS = 4;
    private static final String TAG = "BluetoothDevice";
    public static final int TRANSPORT_AUTO = 0;
    public static final int TRANSPORT_BREDR = 1;
    public static final int TRANSPORT_LE = 2;
    public static final int TRANSPORT_NEARBY_FASTCONNECT = 12;
    public static final int UNBOND_REASON_AUTH_CANCELED = 3;
    public static final int UNBOND_REASON_AUTH_FAILED = 1;
    public static final int UNBOND_REASON_AUTH_REJECTED = 2;
    public static final int UNBOND_REASON_AUTH_TIMEOUT = 6;
    public static final int UNBOND_REASON_DISCOVERY_IN_PROGRESS = 5;
    public static final int UNBOND_REASON_REMOTE_AUTH_CANCELED = 8;
    public static final int UNBOND_REASON_REMOTE_DEVICE_DOWN = 4;
    public static final int UNBOND_REASON_REMOVED = 9;
    public static final int UNBOND_REASON_REPEATED_ATTEMPTS = 7;
    private static volatile IBluetooth sService;
    static IBluetoothManagerCallback sStateChangeCallback = new Stub() {
        public void onBluetoothServiceUp(IBluetooth bluetoothService) throws RemoteException {
            synchronized (BluetoothDevice.class) {
                if (BluetoothDevice.sService == null) {
                    BluetoothDevice.sService = bluetoothService;
                }
            }
        }

        public void onBluetoothServiceDown() throws RemoteException {
            synchronized (BluetoothDevice.class) {
                BluetoothDevice.sService = null;
            }
        }

        public void onBrEdrDown() {
        }
    };
    private final String mAddress;

    static IBluetooth getService() {
        synchronized (BluetoothDevice.class) {
            if (sService == null) {
                sService = BluetoothAdapter.getDefaultAdapter().getBluetoothService(sStateChangeCallback);
            }
        }
        return sService;
    }

    BluetoothDevice(String address) {
        getService();
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            this.mAddress = address;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(address);
        stringBuilder.append(" is not a valid Bluetooth address");
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public boolean equals(Object o) {
        if (o instanceof BluetoothDevice) {
            return this.mAddress.equals(((BluetoothDevice) o).getAddress());
        }
        return false;
    }

    public int hashCode() {
        return this.mAddress.hashCode();
    }

    public String toString() {
        return this.mAddress;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mAddress);
    }

    public String getAddress() {
        return this.mAddress;
    }

    public String getPartAddress() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getAddress().substring(0, getAddress().length() / 2));
        stringBuilder.append(":**:**:**");
        return stringBuilder.toString();
    }

    public String getName() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get Remote Device name");
            return null;
        }
        try {
            String name = service.getRemoteName(this);
            if (name != null) {
                return name.replaceAll("[\\t\\n\\r]+", " ");
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public int getType() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get Remote Device type");
            return 0;
        }
        try {
            return service.getRemoteType(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        }
    }

    public String getAlias() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get Remote Device Alias");
            return null;
        }
        try {
            return service.getRemoteAlias(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public boolean setAlias(String alias) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot set Remote Device name");
            return false;
        }
        try {
            return service.setRemoteAlias(this, alias);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public String getAliasName() {
        String name = getAlias();
        if (name == null) {
            return getName();
        }
        return name;
    }

    public int getBatteryLevel() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "Bluetooth disabled. Cannot get remote device battery level");
            return -1;
        }
        try {
            return service.getBatteryLevel(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return -1;
        }
    }

    public boolean createBond() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot create bond to Remote Device");
            return false;
        }
        try {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("createBond() for device ");
            stringBuilder.append(getPartAddress());
            stringBuilder.append(" called by pid: ");
            stringBuilder.append(Process.myPid());
            stringBuilder.append(" tid: ");
            stringBuilder.append(Process.myTid());
            Log.i(str, stringBuilder.toString());
            return service.createBond(this, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean createBond(int transport) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot create bond to Remote Device");
            return false;
        } else if (transport < 0 || transport > 2) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(transport);
            stringBuilder.append(" is not a valid Bluetooth transport");
            throw new IllegalArgumentException(stringBuilder.toString());
        } else {
            try {
                String str = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("createBond() for device ");
                stringBuilder2.append(getPartAddress());
                stringBuilder2.append(" called by pid: ");
                stringBuilder2.append(Process.myPid());
                stringBuilder2.append(" tid: ");
                stringBuilder2.append(Process.myTid());
                Log.i(str, stringBuilder2.toString());
                return service.createBond(this, transport);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                return false;
            }
        }
    }

    public boolean createBondOutOfBand(int transport, OobData oobData) {
        IBluetooth service = sService;
        if (service == null) {
            Log.w(TAG, "BT not enabled, createBondOutOfBand failed");
            return false;
        }
        try {
            return service.createBondOutOfBand(this, transport, oobData);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean isBondingInitiatedLocally() {
        IBluetooth service = sService;
        if (service == null) {
            Log.w(TAG, "BT not enabled, isBondingInitiatedLocally failed");
            return false;
        }
        try {
            return service.isBondingInitiatedLocally(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean setDeviceOutOfBandData(byte[] hash, byte[] randomizer) {
        return false;
    }

    @SystemApi
    public boolean cancelBondProcess() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot cancel Remote Device bond");
            return false;
        }
        try {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("cancelBondProcess() for device ");
            stringBuilder.append(getAddress());
            stringBuilder.append(" called by pid: ");
            stringBuilder.append(Process.myPid());
            stringBuilder.append(" tid: ");
            stringBuilder.append(Process.myTid());
            Log.i(str, stringBuilder.toString());
            return service.cancelBondProcess(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @SystemApi
    public boolean removeBond() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot remove Remote Device bond");
            return false;
        }
        try {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("removeBond() for device ");
            stringBuilder.append(getAddress());
            stringBuilder.append(" called by pid: ");
            stringBuilder.append(Process.myPid());
            stringBuilder.append(" tid: ");
            stringBuilder.append(Process.myTid());
            Log.i(str, stringBuilder.toString());
            return service.removeBond(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public int getBondState() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get bond state");
            return 10;
        }
        try {
            return service.getBondState(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 10;
        }
    }

    @SystemApi
    public boolean isConnected() {
        IBluetooth service = sService;
        boolean z = false;
        if (service == null) {
            return false;
        }
        try {
            if (service.getConnectionState(this) != 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @SystemApi
    public boolean isEncrypted() {
        IBluetooth service = sService;
        boolean z = false;
        if (service == null) {
            return false;
        }
        try {
            if (service.getConnectionState(this) > 1) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public BluetoothClass getBluetoothClass() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get Bluetooth Class");
            return null;
        }
        try {
            int classInt = service.getRemoteClass(this);
            if (classInt == -16777216) {
                return null;
            }
            return new BluetoothClass(classInt);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public ParcelUuid[] getUuids() {
        IBluetooth service = sService;
        if (service == null || !isBluetoothEnabled()) {
            Log.e(TAG, "BT not enabled. Cannot get remote device Uuids");
            return null;
        }
        try {
            return service.getRemoteUuids(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public boolean fetchUuidsWithSdp() {
        IBluetooth service = sService;
        if (service == null || !isBluetoothEnabled()) {
            Log.e(TAG, "BT not enabled. Cannot fetchUuidsWithSdp");
            return false;
        }
        try {
            return service.fetchRemoteUuids(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean sdpSearch(ParcelUuid uuid) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot query remote device sdp records");
            return false;
        }
        try {
            return service.sdpSearch(this, uuid);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean setPin(byte[] pin) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot set Remote Device pin");
            return false;
        }
        try {
            return service.setPin(this, true, pin.length, pin);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean setPasskey(int passkey) {
        return false;
    }

    public boolean setPairingConfirmation(boolean confirm) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot set pairing confirmation");
            return false;
        }
        try {
            return service.setPairingConfirmation(this, confirm);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean setRemoteOutOfBandData() {
        return false;
    }

    public boolean cancelPairingUserInput() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot create pairing user input");
            return false;
        }
        try {
            return service.cancelBondProcess(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean isBluetoothDock() {
        return false;
    }

    boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            return false;
        }
        return true;
    }

    public int getPhonebookAccessPermission() {
        IBluetooth service = sService;
        if (service == null) {
            return 0;
        }
        try {
            return service.getPhonebookAccessPermission(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        }
    }

    @SystemApi
    public boolean setPhonebookAccessPermission(int value) {
        IBluetooth service = sService;
        if (service == null) {
            return false;
        }
        try {
            return service.setPhonebookAccessPermission(this, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public int getMessageAccessPermission() {
        IBluetooth service = sService;
        if (service == null) {
            return 0;
        }
        try {
            return service.getMessageAccessPermission(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        }
    }

    public boolean setMessageAccessPermission(int value) {
        IBluetooth service = sService;
        if (service == null) {
            return false;
        }
        try {
            return service.setMessageAccessPermission(this, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public int getSimAccessPermission() {
        IBluetooth service = sService;
        if (service == null) {
            return 0;
        }
        try {
            return service.getSimAccessPermission(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        }
    }

    public boolean setSimAccessPermission(int value) {
        IBluetooth service = sService;
        if (service == null) {
            return false;
        }
        try {
            return service.setSimAccessPermission(this, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public BluetoothSocket createRfcommSocket(int channel) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, true, true, this, channel, null);
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createL2capSocket(int channel) throws IOException {
        return new BluetoothSocket(3, -1, true, true, this, channel, null);
    }

    public BluetoothSocket createInsecureL2capSocket(int channel) throws IOException {
        return new BluetoothSocket(3, -1, false, false, this, channel, null);
    }

    public BluetoothSocket createRfcommSocketToServiceRecord(UUID uuid) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, true, true, this, -1, new ParcelUuid(uuid));
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createInsecureRfcommSocketToServiceRecord(UUID uuid) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, false, false, this, -1, new ParcelUuid(uuid));
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createInsecureRfcommSocket(int port) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, false, false, this, port, null);
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createScoSocket() throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(2, -1, true, true, this, -1, null);
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public static byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        try {
            byte[] pinBytes = pin.getBytes("UTF-8");
            if (pinBytes.length <= 0 || pinBytes.length > 16) {
                return null;
            }
            return pinBytes;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 not supported?!?");
            return null;
        }
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback) {
        Log.i(TAG, "connectGatt");
        return connectGatt(context, autoConnect, callback, 0);
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport) {
        return connectGatt(context, autoConnect, callback, transport, 1);
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport, int phy) {
        return connectGatt(context, autoConnect, callback, transport, phy, null);
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport, int phy, Handler handler) {
        return connectGatt(context, autoConnect, callback, transport, false, phy, handler);
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport, boolean opportunistic, int phy, Handler handler) {
        RemoteException e;
        Handler handler2;
        BluetoothGattCallback bluetoothGattCallback = callback;
        if (bluetoothGattCallback != null) {
            try {
                IBluetoothGatt iGatt = BluetoothAdapter.getDefaultAdapter().getBluetoothManager().getBluetoothGatt();
                if (iGatt == null) {
                    return null;
                }
                BluetoothGatt gatt = new BluetoothGatt(iGatt, this, transport, opportunistic, phy);
                try {
                    gatt.connect(Boolean.valueOf(autoConnect), bluetoothGattCallback, handler);
                    return gatt;
                } catch (RemoteException e2) {
                    e = e2;
                    Log.e(TAG, "", e);
                    return null;
                }
            } catch (RemoteException e3) {
                e = e3;
                handler2 = handler;
                Log.e(TAG, "", e);
                return null;
            }
        }
        handler2 = handler;
        throw new NullPointerException("callback is null");
    }

    public BluetoothSocket createL2capCocSocket(int transport, int psm) throws IOException {
        if (!isBluetoothEnabled()) {
            Log.e(TAG, "createL2capCocSocket: Bluetooth is not enabled");
            throw new IOException();
        } else if (transport == 2) {
            return new BluetoothSocket(4, -1, true, true, this, psm, null);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unsupported transport: ");
            stringBuilder.append(transport);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public BluetoothSocket createInsecureL2capCocSocket(int transport, int psm) throws IOException {
        if (!isBluetoothEnabled()) {
            Log.e(TAG, "createInsecureL2capCocSocket: Bluetooth is not enabled");
            throw new IOException();
        } else if (transport == 2) {
            return new BluetoothSocket(4, -1, false, false, this, psm, null);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unsupported transport: ");
            stringBuilder.append(transport);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public BluetoothGatt fastConnectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("fastConnectGatt: ");
        stringBuilder.append(transport);
        Log.i(str, stringBuilder.toString());
        return connectGatt(context, autoConnect, callback, transport | 12, 1);
    }
}
