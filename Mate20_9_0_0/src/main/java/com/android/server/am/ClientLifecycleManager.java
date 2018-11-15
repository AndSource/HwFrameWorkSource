package com.android.server.am;

import android.app.IApplicationThread;
import android.app.servertransaction.ActivityLifecycleItem;
import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.ClientTransactionItem;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

class ClientLifecycleManager {
    ClientLifecycleManager() {
    }

    void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
        IApplicationThread client = transaction.getClient();
        transaction.schedule();
        if (!(client instanceof Binder)) {
            transaction.recycle();
        }
    }

    void scheduleTransaction(IApplicationThread client, IBinder activityToken, ActivityLifecycleItem stateRequest) throws RemoteException {
        scheduleTransaction(transactionWithState(client, activityToken, stateRequest));
    }

    void scheduleTransaction(IApplicationThread client, IBinder activityToken, ClientTransactionItem callback) throws RemoteException {
        scheduleTransaction(transactionWithCallback(client, activityToken, callback));
    }

    void scheduleTransaction(IApplicationThread client, ClientTransactionItem callback) throws RemoteException {
        scheduleTransaction(transactionWithCallback(client, null, callback));
    }

    private static ClientTransaction transactionWithState(IApplicationThread client, IBinder activityToken, ActivityLifecycleItem stateRequest) {
        ClientTransaction clientTransaction = ClientTransaction.obtain(client, activityToken);
        clientTransaction.setLifecycleStateRequest(stateRequest);
        return clientTransaction;
    }

    private static ClientTransaction transactionWithCallback(IApplicationThread client, IBinder activityToken, ClientTransactionItem callback) {
        ClientTransaction clientTransaction = ClientTransaction.obtain(client, activityToken);
        clientTransaction.addCallback(callback);
        return clientTransaction;
    }
}
