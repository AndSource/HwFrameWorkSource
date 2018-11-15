package com.huawei.odmf.core;

import android.util.ArrayMap;
import com.huawei.odmf.predicate.SaveRequest;
import com.huawei.odmf.user.api.AllChangeToTarget;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.utils.LOG;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

class NotifyManager {
    private HandleMessageThread messageThread = new HandleMessageThread();
    private final Queue<MessageNode> messagesQueue = new LinkedList();

    private class HandleMessageThread extends Thread {
        private HandleMessageThread() {
        }

        public void run() {
            while (true) {
                MessageNode node;
                synchronized (NotifyManager.this.messagesQueue) {
                    while (NotifyManager.this.messagesQueue.isEmpty()) {
                        try {
                            NotifyManager.this.messagesQueue.wait();
                        } catch (InterruptedException e) {
                            LOG.logW("The message handle thread interrupted by another thread.");
                        }
                    }
                    node = (MessageNode) NotifyManager.this.messagesQueue.poll();
                }
                if (node != null) {
                    try {
                        if (node.isClearEntityMsg()) {
                            NotifyManager.this.sendMessageToObjectContext(node.getChangeContext(), node.getEntityName(), node.getPsUri(), node.getNotifyTarget());
                            node.releaseReferences();
                        } else {
                            NotifyManager.this.sendMessageToObjectContext(new AllChangeToThread(node.getInsertedObjects(), node.getUpdatedObjects(), node.getDeletedObjects(), node.getChangeContext(), node.getPsUri()), node.getNotifyTarget());
                            node.releaseReferences();
                        }
                    } catch (RuntimeException e2) {
                        LOG.logE("A RuntimeException occurred during send message : " + e2.getMessage());
                    }
                }
            }
        }
    }

    private static class MessageNode {
        private ObjectContext changeContext;
        private List<ManagedObject> deletedObjects = new ArrayList();
        private String entityName;
        private List<ManagedObject> insertedObjects = new ArrayList();
        private boolean isClearEntityMsg = false;
        private List<ObjectContext> notifyTarget = new ArrayList();
        private String psUri;
        private List<ManagedObject> updatedObjects = new ArrayList();

        MessageNode(List<ManagedObject> insertedObjects, List<ManagedObject> updatedObjects, List<ManagedObject> deletedObjects, ObjectContext changeContext, String psUri, List<ObjectContext> notifyTarget) {
            this.insertedObjects = new ArrayList(insertedObjects);
            this.updatedObjects = new ArrayList(updatedObjects);
            this.deletedObjects = new ArrayList(deletedObjects);
            this.changeContext = changeContext;
            this.psUri = psUri;
            this.notifyTarget = notifyTarget;
        }

        MessageNode(ObjectContext changeContext, String entityName, String psUri, boolean isClearEntityMsg, List<ObjectContext> notifyTarget) {
            this.changeContext = changeContext;
            this.entityName = entityName;
            this.psUri = psUri;
            this.isClearEntityMsg = isClearEntityMsg;
            this.notifyTarget = notifyTarget;
        }

        public List<ManagedObject> getInsertedObjects() {
            return this.insertedObjects;
        }

        public List<ManagedObject> getUpdatedObjects() {
            return this.updatedObjects;
        }

        public List<ManagedObject> getDeletedObjects() {
            return this.deletedObjects;
        }

        public ObjectContext getChangeContext() {
            return this.changeContext;
        }

        public String getPsUri() {
            return this.psUri;
        }

        public String getEntityName() {
            return this.entityName;
        }

        public boolean isClearEntityMsg() {
            return this.isClearEntityMsg;
        }

        public List<ObjectContext> getNotifyTarget() {
            return this.notifyTarget;
        }

        public void releaseReferences() {
            this.notifyTarget.clear();
            this.notifyTarget = null;
            this.insertedObjects.clear();
            this.insertedObjects = null;
            this.updatedObjects.clear();
            this.updatedObjects = null;
            this.deletedObjects.clear();
            this.deletedObjects = null;
            this.changeContext = null;
            this.psUri = null;
            this.entityName = null;
        }
    }

    public NotifyManager() {
        this.messageThread.start();
    }

    public List<ObjectContext> hasListeners(String uriString) {
        List<ObjectContext> notifyTarget = new ArrayList();
        for (Entry<ObjectContext, PersistentStore> entry : PersistentStoreCoordinator.getDefault().getMapContextToPersistentStore().entrySet()) {
            AObjectContext objContext = (AObjectContext) entry.getKey();
            NotifyClient notifyClient = objContext.getNotifyClient();
            if (((PersistentStore) entry.getValue()).getUriString().equals(uriString) && (notifyClient.hasPsListener() || notifyClient.hasObjectContextListener() || notifyClient.hasEntityListener() || notifyClient.hasManageObjListener())) {
                notifyTarget.add(objContext);
            }
        }
        return notifyTarget;
    }

    private void sendMessageToObjectContext(ObjectContext changeContext, String entityName, String uriString, List<ObjectContext> notifyTarget) {
        int length = notifyTarget.size();
        for (int i = 0; i < length; i++) {
            NotifyClient notifyClient = ((AObjectContext) notifyTarget.get(i)).getNotifyClient();
            if (notifyClient.hasEntityListener(entityName) || notifyClient.hasPsListener()) {
                notifyClient.sendMessage(2, new AllChangeToThread(changeContext, entityName, uriString));
            }
        }
    }

    void sendMessageToObjectContext(AllChangeToThread allChange, List<ObjectContext> notifyTarget) {
        if (allChange == null) {
            LOG.logI("allChange is empty,do nothing");
            return;
        }
        ArrayMap<String, AllChangeToTarget> persistStoreMap = allChange.getPsMap();
        ArrayMap<ObjectContext, AllChangeToTarget> objectContextMap = allChange.getObjContextMap();
        ArrayMap<String, AllChangeToTarget> entityMap = allChange.getEntityMap();
        ArrayMap<ObjectId, AllChangeToTarget> manageObjAllChange = allChange.getManageObjMap();
        ObjectContext changeContext = allChange.getChangeContext();
        int length = notifyTarget.size();
        for (int i = 0; i < length; i++) {
            AObjectContext objContext = (AObjectContext) notifyTarget.get(i);
            AllChangeToThread allChangeToThread = new AllChangeToThread(changeContext);
            NotifyClient notifyClient = objContext.getNotifyClient();
            boolean hasPsListener = false;
            for (String uriString : persistStoreMap.keySet()) {
                if (notifyClient.hasPsListener(uriString)) {
                    allChangeToThread.getPsMap().put(uriString, persistStoreMap.get(uriString));
                    hasPsListener = true;
                }
            }
            boolean hasObjContextListener = false;
            for (ObjectContext ctx : objectContextMap.keySet()) {
                if (notifyClient.hasObjectContextListener(ctx)) {
                    allChangeToThread.getObjContextMap().put(ctx, objectContextMap.get(ctx));
                    hasObjContextListener = true;
                }
            }
            boolean hasEntityListener = false;
            for (String entityName : entityMap.keySet()) {
                if (notifyClient.hasEntityListener(entityName)) {
                    allChangeToThread.getEntityMap().put(entityName, entityMap.get(entityName));
                    hasEntityListener = true;
                }
            }
            boolean hasManageObjListener = false;
            for (ObjectId id : manageObjAllChange.keySet()) {
                if (notifyClient.hasManageObjListener(id)) {
                    allChangeToThread.getManageObjMap().put(id, manageObjAllChange.get(id));
                    hasManageObjListener = true;
                }
            }
            if (hasPsListener || hasObjContextListener || hasEntityListener || hasManageObjListener) {
                notifyClient.sendMessage(1, allChangeToThread);
            }
        }
    }

    void addMessageToQueue(SaveRequest saveRequest, ObjectContext context, String psUri, List<ObjectContext> notifyTarget) {
        synchronized (this.messagesQueue) {
            this.messagesQueue.add(new MessageNode(saveRequest.getInsertedObjects(), saveRequest.getUpdatedObjects(), saveRequest.getDeletedObjects(), context, psUri, notifyTarget));
            this.messagesQueue.notifyAll();
        }
    }

    void addMessageToQueue(ObjectContext context, String entityName, String psUri, List<ObjectContext> notifyTarget) {
        synchronized (this.messagesQueue) {
            this.messagesQueue.add(new MessageNode(context, entityName, psUri, true, notifyTarget));
            this.messagesQueue.notifyAll();
        }
    }
}
