package com.huawei.nearbysdk.closeRange;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICloseRangeEventListener extends IInterface {

    public static abstract class Stub extends Binder implements ICloseRangeEventListener {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.closeRange.ICloseRangeEventListener";
        static final int TRANSACTION_onEvent = 1;

        private static class Proxy implements ICloseRangeEventListener {
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

            public void onEvent(CloseRangeResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
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

        public static ICloseRangeEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICloseRangeEventListener)) {
                return new Proxy(obj);
            }
            return (ICloseRangeEventListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = DESCRIPTOR;
            if (code == 1) {
                CloseRangeResult _arg0;
                data.enforceInterface(descriptor);
                if (data.readInt() != 0) {
                    _arg0 = (CloseRangeResult) CloseRangeResult.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onEvent(_arg0);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(descriptor);
                return true;
            }
        }
    }

    void onEvent(CloseRangeResult closeRangeResult) throws RemoteException;
}
