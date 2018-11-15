package com.huawei.nearbysdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAuthAdapter extends IInterface {

    public static abstract class Stub extends Binder implements IAuthAdapter {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.IAuthAdapter";
        static final int TRANSACTION_hasLoginHwId = 4;
        static final int TRANSACTION_registerAuthentification = 1;
        static final int TRANSACTION_setNickname = 6;
        static final int TRANSACTION_setUserIdFromAdapter = 5;
        static final int TRANSACTION_startAuthentification = 3;
        static final int TRANSACTION_unRegisterAuthentification = 2;

        private static class Proxy implements IAuthAdapter {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public boolean registerAuthentification(int businessId, IAuthListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean z = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unRegisterAuthentification(IAuthListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean z = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long startAuthentification(NearbyDevice device, int mode, byte[] encryptedAesKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    _data.writeByteArray(encryptedAesKey);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasLoginHwId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = false;
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setUserIdFromAdapter(String userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(userId);
                    boolean z = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNickname(String nickname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nickname);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAuthAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAuthAdapter)) {
                return new Proxy(obj);
            }
            return (IAuthAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = DESCRIPTOR;
            if (code != 1598968902) {
                boolean _result;
                switch (code) {
                    case 1:
                        data.enforceInterface(descriptor);
                        boolean _result2 = registerAuthentification(data.readInt(), com.huawei.nearbysdk.IAuthListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 2:
                        data.enforceInterface(descriptor);
                        _result = unRegisterAuthentification(com.huawei.nearbysdk.IAuthListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        NearbyDevice _arg0;
                        data.enforceInterface(descriptor);
                        if (data.readInt() != 0) {
                            _arg0 = (NearbyDevice) NearbyDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        long _result3 = startAuthentification(_arg0, data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeLong(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(descriptor);
                        boolean _result4 = hasLoginHwId();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(descriptor);
                        _result = setUserIdFromAdapter(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 6:
                        data.enforceInterface(descriptor);
                        setNickname(data.readString());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
            reply.writeString(descriptor);
            return true;
        }
    }

    boolean hasLoginHwId() throws RemoteException;

    boolean registerAuthentification(int i, IAuthListener iAuthListener) throws RemoteException;

    void setNickname(String str) throws RemoteException;

    boolean setUserIdFromAdapter(String str) throws RemoteException;

    long startAuthentification(NearbyDevice nearbyDevice, int i, byte[] bArr) throws RemoteException;

    boolean unRegisterAuthentification(IAuthListener iAuthListener) throws RemoteException;
}
