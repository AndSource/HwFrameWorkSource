package com.android.server.accessibility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import android.util.MathUtils;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.usb.descriptors.UsbACInterface;
import java.util.Queue;

class MagnificationGestureHandler extends BaseEventStreamTransformation {
    private static final boolean DEBUG_ALL = false;
    private static final boolean DEBUG_DETECTING = false;
    private static final boolean DEBUG_EVENT_STREAM = false;
    private static final boolean DEBUG_PANNING_SCALING = false;
    private static final boolean DEBUG_STATE_TRANSITIONS = false;
    private static final String LOG_TAG = "MagnificationGestureHandler";
    private static final float MAX_SCALE = 5.0f;
    private static final float MIN_SCALE = 2.0f;
    private static final String SHOW_ROUNDED_CORNERS = "show_rounded_corners";
    private static final String mNotchProp = SystemProperties.get("ro.config.hw_notch_size", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    private final Context mContext;
    @VisibleForTesting
    State mCurrentState;
    private final Queue<MotionEvent> mDebugInputEventHistory;
    private final Queue<MotionEvent> mDebugOutputEventHistory;
    @VisibleForTesting
    final DelegatingState mDelegatingState;
    final boolean mDetectShortcutTrigger;
    final boolean mDetectTripleTap;
    @VisibleForTesting
    final DetectingState mDetectingState;
    private boolean mHasNotchInScreen = false;
    @VisibleForTesting
    final MagnificationController mMagnificationController;
    @VisibleForTesting
    final PanningScalingState mPanningScalingState;
    @VisibleForTesting
    State mPreviousState;
    private final ScreenStateReceiver mScreenStateReceiver;
    private PointerCoords[] mTempPointerCoords;
    private PointerProperties[] mTempPointerProperties;
    @VisibleForTesting
    final ViewportDraggingState mViewportDraggingState;

    private static final class MotionEventInfo {
        private static final int MAX_POOL_SIZE = 10;
        private static final Object sLock = new Object();
        private static MotionEventInfo sPool;
        private static int sPoolSize;
        public MotionEvent event;
        private boolean mInPool;
        private MotionEventInfo mNext;
        public int policyFlags;
        public MotionEvent rawEvent;

        private MotionEventInfo() {
        }

        public static MotionEventInfo obtain(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            MotionEventInfo info;
            synchronized (sLock) {
                info = obtainInternal();
                info.initialize(event, rawEvent, policyFlags);
            }
            return info;
        }

        private static MotionEventInfo obtainInternal() {
            if (sPoolSize <= 0) {
                return new MotionEventInfo();
            }
            sPoolSize--;
            MotionEventInfo info = sPool;
            sPool = info.mNext;
            info.mNext = null;
            info.mInPool = false;
            return info;
        }

        private void initialize(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            this.event = MotionEvent.obtain(event);
            this.rawEvent = MotionEvent.obtain(rawEvent);
            this.policyFlags = policyFlags;
        }

        public void recycle() {
            synchronized (sLock) {
                if (this.mInPool) {
                    throw new IllegalStateException("Already recycled.");
                }
                clear();
                if (sPoolSize < 10) {
                    sPoolSize++;
                    this.mNext = sPool;
                    sPool = this;
                    this.mInPool = true;
                }
            }
        }

        private void clear() {
            this.event = MagnificationGestureHandler.recycleAndNullify(this.event);
            this.rawEvent = MagnificationGestureHandler.recycleAndNullify(this.rawEvent);
            this.policyFlags = 0;
        }

        static int countOf(MotionEventInfo info, int eventType) {
            int i = 0;
            if (info == null) {
                return 0;
            }
            if (info.event.getAction() == eventType) {
                i = 1;
            }
            return i + countOf(info.mNext, eventType);
        }

        public static String toString(MotionEventInfo info) {
            if (info == null) {
                return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(MotionEvent.actionToString(info.event.getAction()).replace("ACTION_", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
            stringBuilder.append(" ");
            stringBuilder.append(toString(info.mNext));
            return stringBuilder.toString();
        }
    }

    private static class ScreenStateReceiver extends BroadcastReceiver {
        private final Context mContext;
        private final MagnificationGestureHandler mGestureHandler;

        public ScreenStateReceiver(Context context, MagnificationGestureHandler gestureHandler) {
            this.mContext = context;
            this.mGestureHandler = gestureHandler;
        }

        public void register() {
            this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.SCREEN_OFF"));
        }

        public void unregister() {
            this.mContext.unregisterReceiver(this);
        }

        public void onReceive(Context context, Intent intent) {
            this.mGestureHandler.mDetectingState.setShortcutTriggered(false);
        }
    }

    interface State {
        void onMotionEvent(MotionEvent motionEvent, MotionEvent motionEvent2, int i);

        void clear() {
        }

        String name() {
            return getClass().getSimpleName();
        }

        static String nameOf(State s) {
            return s != null ? s.name() : "null";
        }
    }

    final class DelegatingState implements State {
        public long mLastDelegatedDownEventTime;

        DelegatingState() {
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            int actionMasked = event.getActionMasked();
            if (actionMasked != 3) {
                switch (actionMasked) {
                    case 0:
                        MagnificationGestureHandler.this.transitionTo(MagnificationGestureHandler.this.mDelegatingState);
                        this.mLastDelegatedDownEventTime = event.getDownTime();
                        break;
                    case 1:
                        break;
                }
            }
            MagnificationGestureHandler.this.transitionTo(MagnificationGestureHandler.this.mDetectingState);
            if (MagnificationGestureHandler.this.getNext() != null) {
                event.setDownTime(this.mLastDelegatedDownEventTime);
                MagnificationGestureHandler.this.dispatchTransformedEvent(event, rawEvent, policyFlags);
            }
        }
    }

    final class DetectingState implements State, Callback {
        private static final int MESSAGE_ON_TRIPLE_TAP_AND_HOLD = 1;
        private static final int MESSAGE_TRANSITION_TO_DELEGATING_STATE = 2;
        private MotionEventInfo mDelayedEventQueue;
        @VisibleForTesting
        Handler mHandler = new Handler(Looper.getMainLooper(), this);
        MotionEvent mLastDown;
        private MotionEvent mLastUp;
        final int mLongTapMinDelay = ViewConfiguration.getLongPressTimeout();
        final int mMultiTapMaxDelay;
        final int mMultiTapMaxDistance;
        private MotionEvent mPreLastDown;
        private MotionEvent mPreLastUp;
        @VisibleForTesting
        boolean mShortcutTriggered;
        final int mSwipeMinDistance;

        public DetectingState(Context context) {
            this.mMultiTapMaxDelay = ViewConfiguration.getDoubleTapTimeout() + context.getResources().getInteger(17694863);
            this.mSwipeMinDistance = ViewConfiguration.get(context).getScaledTouchSlop();
            this.mMultiTapMaxDistance = ViewConfiguration.get(context).getScaledDoubleTapSlop();
        }

        public boolean handleMessage(Message message) {
            int type = message.what;
            switch (type) {
                case 1:
                    MotionEvent down = message.obj;
                    transitionToViewportDraggingStateAndClear(down);
                    down.recycle();
                    break;
                case 2:
                    transitionToDelegatingStateAndClear();
                    break;
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unknown message type: ");
                    stringBuilder.append(type);
                    throw new IllegalArgumentException(stringBuilder.toString());
            }
            return true;
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            cacheDelayedMotionEvent(event, rawEvent, policyFlags);
            int actionMasked = event.getActionMasked();
            if (actionMasked != 5) {
                switch (actionMasked) {
                    case 0:
                        this.mHandler.removeMessages(2);
                        if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(event.getX(), event.getY())) {
                            transitionToDelegatingStateAndClear();
                            return;
                        } else if (isMultiTapTriggered(2)) {
                            afterLongTapTimeoutTransitionToDraggingState(event);
                            return;
                        } else if (MagnificationGestureHandler.this.mDetectTripleTap || MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                            afterMultiTapTimeoutTransitionToDelegatingState();
                            return;
                        } else {
                            transitionToDelegatingStateAndClear();
                            return;
                        }
                    case 1:
                        this.mHandler.removeMessages(1);
                        if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(event.getX(), event.getY())) {
                            transitionToDelegatingStateAndClear();
                            return;
                        } else if (isMultiTapTriggered(3)) {
                            onTripleTap(event);
                            return;
                        } else if (!isFingerDown()) {
                            return;
                        } else {
                            if (timeBetween(this.mLastDown, this.mLastUp) >= ((long) this.mLongTapMinDelay) || GestureUtils.distance(this.mLastDown, this.mLastUp) >= ((double) this.mSwipeMinDistance)) {
                                transitionToDelegatingStateAndClear();
                                return;
                            }
                            return;
                        }
                    case 2:
                        if (isFingerDown() && GestureUtils.distance(this.mLastDown, event) > ((double) this.mSwipeMinDistance)) {
                            if (isMultiTapTriggered(2)) {
                                transitionToViewportDraggingStateAndClear(event);
                                return;
                            } else {
                                transitionToDelegatingStateAndClear();
                                return;
                            }
                        }
                        return;
                    default:
                        return;
                }
            } else if (MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                MagnificationGestureHandler.this.transitionTo(MagnificationGestureHandler.this.mPanningScalingState);
                clear();
            } else {
                transitionToDelegatingStateAndClear();
            }
        }

        public boolean isMultiTapTriggered(int numTaps) {
            boolean z = false;
            if (this.mShortcutTriggered) {
                if (tapCount() + 2 >= numTaps) {
                    z = true;
                }
                return z;
            }
            if (MagnificationGestureHandler.this.mDetectTripleTap && tapCount() >= numTaps && isMultiTap(this.mPreLastDown, this.mLastDown) && isMultiTap(this.mPreLastUp, this.mLastUp)) {
                z = true;
            }
            return z;
        }

        private boolean isMultiTap(MotionEvent first, MotionEvent second) {
            return GestureUtils.isMultiTap(first, second, this.mMultiTapMaxDelay, this.mMultiTapMaxDistance);
        }

        public boolean isFingerDown() {
            return this.mLastDown != null;
        }

        private long timeBetween(MotionEvent a, MotionEvent b) {
            if (a == null && b == null) {
                return 0;
            }
            return Math.abs(timeOf(a) - timeOf(b));
        }

        private long timeOf(MotionEvent event) {
            return event != null ? event.getEventTime() : Long.MIN_VALUE;
        }

        public int tapCount() {
            return MotionEventInfo.countOf(this.mDelayedEventQueue, 1);
        }

        public void afterMultiTapTimeoutTransitionToDelegatingState() {
            this.mHandler.sendEmptyMessageDelayed(2, (long) this.mMultiTapMaxDelay);
        }

        public void afterLongTapTimeoutTransitionToDraggingState(MotionEvent event) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, MotionEvent.obtain(event)), (long) ViewConfiguration.getLongPressTimeout());
        }

        public void clear() {
            setShortcutTriggered(false);
            removePendingDelayedMessages();
            clearDelayedMotionEvents();
        }

        private void removePendingDelayedMessages() {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
        }

        private void cacheDelayedMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            if (event.getActionMasked() == 0) {
                this.mPreLastDown = this.mLastDown;
                this.mLastDown = MotionEvent.obtain(event);
            } else if (event.getActionMasked() == 1) {
                this.mPreLastUp = this.mLastUp;
                this.mLastUp = MotionEvent.obtain(event);
            }
            MotionEventInfo info = MotionEventInfo.obtain(event, rawEvent, policyFlags);
            if (this.mDelayedEventQueue == null) {
                this.mDelayedEventQueue = info;
                return;
            }
            MotionEventInfo tail = this.mDelayedEventQueue;
            while (tail.mNext != null) {
                tail = tail.mNext;
            }
            tail.mNext = info;
        }

        private void sendDelayedMotionEvents() {
            while (this.mDelayedEventQueue != null) {
                MotionEventInfo info = this.mDelayedEventQueue;
                this.mDelayedEventQueue = info.mNext;
                MagnificationGestureHandler.this.handleEventWith(MagnificationGestureHandler.this.mDelegatingState, info.event, info.rawEvent, info.policyFlags);
                info.recycle();
            }
        }

        private void clearDelayedMotionEvents() {
            while (this.mDelayedEventQueue != null) {
                MotionEventInfo info = this.mDelayedEventQueue;
                this.mDelayedEventQueue = info.mNext;
                info.recycle();
            }
            this.mPreLastDown = null;
            this.mPreLastUp = null;
            this.mLastDown = null;
            this.mLastUp = null;
        }

        void transitionToDelegatingStateAndClear() {
            MagnificationGestureHandler.this.transitionTo(MagnificationGestureHandler.this.mDelegatingState);
            sendDelayedMotionEvents();
            removePendingDelayedMessages();
        }

        private void onTripleTap(MotionEvent up) {
            clear();
            String str;
            StringBuilder stringBuilder;
            if (MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                MagnificationGestureHandler.this.zoomOff();
                str = MagnificationGestureHandler.LOG_TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("zoomOff,mHasNotchInScreen:");
                stringBuilder.append(MagnificationGestureHandler.this.mHasNotchInScreen);
                Log.i(str, stringBuilder.toString());
                if (MagnificationGestureHandler.this.mHasNotchInScreen) {
                    Global.putInt(MagnificationGestureHandler.this.mContext.getContentResolver(), MagnificationGestureHandler.SHOW_ROUNDED_CORNERS, 1);
                    return;
                }
                return;
            }
            MagnificationGestureHandler.this.zoomOn(up.getX(), up.getY());
            str = MagnificationGestureHandler.LOG_TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("zoomOn,mHasNotchInScreen:");
            stringBuilder.append(MagnificationGestureHandler.this.mHasNotchInScreen);
            Log.i(str, stringBuilder.toString());
            if (MagnificationGestureHandler.this.mHasNotchInScreen) {
                Global.putInt(MagnificationGestureHandler.this.mContext.getContentResolver(), MagnificationGestureHandler.SHOW_ROUNDED_CORNERS, 0);
            }
        }

        void transitionToViewportDraggingStateAndClear(MotionEvent down) {
            clear();
            MagnificationGestureHandler.this.mViewportDraggingState.mZoomedInBeforeDrag = MagnificationGestureHandler.this.mMagnificationController.isMagnifying();
            MagnificationGestureHandler.this.zoomOn(down.getX(), down.getY());
            MagnificationGestureHandler.this.transitionTo(MagnificationGestureHandler.this.mViewportDraggingState);
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("DetectingState{tapCount()=");
            stringBuilder.append(tapCount());
            stringBuilder.append(", mShortcutTriggered=");
            stringBuilder.append(this.mShortcutTriggered);
            stringBuilder.append(", mDelayedEventQueue=");
            stringBuilder.append(MotionEventInfo.toString(this.mDelayedEventQueue));
            stringBuilder.append('}');
            return stringBuilder.toString();
        }

        void toggleShortcutTriggered() {
            setShortcutTriggered(this.mShortcutTriggered ^ 1);
        }

        void setShortcutTriggered(boolean state) {
            if (this.mShortcutTriggered != state) {
                this.mShortcutTriggered = state;
                MagnificationGestureHandler.this.mMagnificationController.setForceShowMagnifiableBounds(state);
            }
        }
    }

    final class PanningScalingState extends SimpleOnGestureListener implements OnScaleGestureListener, State {
        float mInitialScaleFactor = -1.0f;
        private final ScaleGestureDetector mScaleGestureDetector;
        boolean mScaling;
        final float mScalingThreshold;
        private final GestureDetector mScrollGestureDetector;

        public PanningScalingState(Context context) {
            TypedValue scaleValue = new TypedValue();
            context.getResources().getValue(17104974, scaleValue, false);
            this.mScalingThreshold = scaleValue.getFloat();
            this.mScaleGestureDetector = new ScaleGestureDetector(context, this, Handler.getMain());
            this.mScaleGestureDetector.setQuickScaleEnabled(false);
            this.mScrollGestureDetector = new GestureDetector(context, this, Handler.getMain());
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            int action = event.getActionMasked();
            if (action == 6 && event.getPointerCount() == 2 && MagnificationGestureHandler.this.mPreviousState == MagnificationGestureHandler.this.mViewportDraggingState) {
                persistScaleAndTransitionTo(MagnificationGestureHandler.this.mViewportDraggingState);
            } else if (action == 1 || action == 3) {
                persistScaleAndTransitionTo(MagnificationGestureHandler.this.mDetectingState);
            }
        }

        public void persistScaleAndTransitionTo(State state) {
            MagnificationGestureHandler.this.mMagnificationController.persistScale();
            clear();
            MagnificationGestureHandler.this.transitionTo(state);
        }

        public boolean onScroll(MotionEvent first, MotionEvent second, float distanceX, float distanceY) {
            if (MagnificationGestureHandler.this.mCurrentState != MagnificationGestureHandler.this.mPanningScalingState) {
                return true;
            }
            MagnificationGestureHandler.this.mMagnificationController.offsetMagnifiedRegion(distanceX, distanceY, 0);
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            boolean z = true;
            if (this.mScaling) {
                float scale;
                float scale2;
                float initialScale = MagnificationGestureHandler.this.mMagnificationController.getScale();
                float targetScale = detector.getScaleFactor() * initialScale;
                if (targetScale > 5.0f && targetScale > initialScale) {
                    scale = 5.0f;
                } else if (targetScale >= MagnificationGestureHandler.MIN_SCALE || targetScale >= initialScale) {
                    scale2 = targetScale;
                    MagnificationGestureHandler.this.mMagnificationController.setScale(scale2, detector.getFocusX(), detector.getFocusY(), false, 0);
                    return true;
                } else {
                    scale = MagnificationGestureHandler.MIN_SCALE;
                }
                scale2 = scale;
                MagnificationGestureHandler.this.mMagnificationController.setScale(scale2, detector.getFocusX(), detector.getFocusY(), false, 0);
                return true;
            } else if (this.mInitialScaleFactor < 0.0f) {
                this.mInitialScaleFactor = detector.getScaleFactor();
                return false;
            } else {
                if (Math.abs(detector.getScaleFactor() - this.mInitialScaleFactor) <= this.mScalingThreshold) {
                    z = false;
                }
                this.mScaling = z;
                return this.mScaling;
            }
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return MagnificationGestureHandler.this.mCurrentState == MagnificationGestureHandler.this.mPanningScalingState;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            clear();
        }

        public void clear() {
            this.mInitialScaleFactor = -1.0f;
            this.mScaling = false;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("PanningScalingState{mInitialScaleFactor=");
            stringBuilder.append(this.mInitialScaleFactor);
            stringBuilder.append(", mScaling=");
            stringBuilder.append(this.mScaling);
            stringBuilder.append('}');
            return stringBuilder.toString();
        }
    }

    final class ViewportDraggingState implements State {
        private boolean mLastMoveOutsideMagnifiedRegion;
        boolean mZoomedInBeforeDrag;

        ViewportDraggingState() {
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            int action = event.getActionMasked();
            switch (action) {
                case 0:
                case 6:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unexpected event type: ");
                    stringBuilder.append(MotionEvent.actionToString(action));
                    throw new IllegalArgumentException(stringBuilder.toString());
                case 1:
                case 3:
                    if (!this.mZoomedInBeforeDrag) {
                        MagnificationGestureHandler.this.zoomOff();
                    }
                    clear();
                    MagnificationGestureHandler.this.transitionTo(MagnificationGestureHandler.this.mDetectingState);
                    return;
                case 2:
                    if (event.getPointerCount() == 1) {
                        float eventX = event.getX();
                        float eventY = event.getY();
                        if (MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(eventX, eventY)) {
                            MagnificationGestureHandler.this.mMagnificationController.setCenter(eventX, eventY, this.mLastMoveOutsideMagnifiedRegion, 0);
                            this.mLastMoveOutsideMagnifiedRegion = false;
                            return;
                        }
                        this.mLastMoveOutsideMagnifiedRegion = true;
                        return;
                    }
                    throw new IllegalStateException("Should have one pointer down.");
                case 5:
                    clear();
                    MagnificationGestureHandler.this.transitionTo(MagnificationGestureHandler.this.mPanningScalingState);
                    return;
                default:
                    return;
            }
        }

        public void clear() {
            this.mLastMoveOutsideMagnifiedRegion = false;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ViewportDraggingState{mZoomedInBeforeDrag=");
            stringBuilder.append(this.mZoomedInBeforeDrag);
            stringBuilder.append(", mLastMoveOutsideMagnifiedRegion=");
            stringBuilder.append(this.mLastMoveOutsideMagnifiedRegion);
            stringBuilder.append('}');
            return stringBuilder.toString();
        }
    }

    public MagnificationGestureHandler(Context context, MagnificationController magnificationController, boolean detectTripleTap, boolean detectShortcutTrigger) {
        this.mContext = context;
        this.mMagnificationController = magnificationController;
        this.mDelegatingState = new DelegatingState();
        this.mDetectingState = new DetectingState(context);
        this.mViewportDraggingState = new ViewportDraggingState();
        this.mPanningScalingState = new PanningScalingState(context);
        this.mDetectTripleTap = detectTripleTap;
        this.mDetectShortcutTrigger = detectShortcutTrigger;
        if (this.mDetectShortcutTrigger) {
            this.mScreenStateReceiver = new ScreenStateReceiver(context, this);
            this.mScreenStateReceiver.register();
        } else {
            this.mScreenStateReceiver = null;
        }
        this.mDebugInputEventHistory = null;
        this.mDebugOutputEventHistory = null;
        transitionTo(this.mDetectingState);
        this.mHasNotchInScreen = TextUtils.isEmpty(mNotchProp) ^ 1;
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        onMotionEventInternal(event, rawEvent, policyFlags);
    }

    private void onMotionEventInternal(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if ((this.mDetectTripleTap || this.mDetectShortcutTrigger) && event.isFromSource(UsbACInterface.FORMAT_II_AC3)) {
            handleEventWith(this.mCurrentState, event, rawEvent, policyFlags);
        } else {
            dispatchTransformedEvent(event, rawEvent, policyFlags);
        }
    }

    private void handleEventWith(State stateHandler, MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        this.mPanningScalingState.mScrollGestureDetector.onTouchEvent(event);
        this.mPanningScalingState.mScaleGestureDetector.onTouchEvent(event);
        stateHandler.onMotionEvent(event, rawEvent, policyFlags);
    }

    public void clearEvents(int inputSource) {
        if (inputSource == UsbACInterface.FORMAT_II_AC3) {
            clearAndTransitionToStateDetecting();
        }
        super.clearEvents(inputSource);
    }

    public void onDestroy() {
        if (this.mScreenStateReceiver != null) {
            this.mScreenStateReceiver.unregister();
        }
        clearAndTransitionToStateDetecting();
    }

    void notifyShortcutTriggered() {
        if (!this.mDetectShortcutTrigger) {
            return;
        }
        if (this.mMagnificationController.resetIfNeeded(true)) {
            clearAndTransitionToStateDetecting();
        } else {
            this.mDetectingState.toggleShortcutTriggered();
        }
    }

    void clearAndTransitionToStateDetecting() {
        this.mCurrentState = this.mDetectingState;
        this.mDetectingState.clear();
        this.mViewportDraggingState.clear();
        this.mPanningScalingState.clear();
    }

    private void dispatchTransformedEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        MotionEvent event2 = event;
        if (this.mMagnificationController.isMagnifying() && event2.isFromSource(UsbACInterface.FORMAT_II_AC3) && this.mMagnificationController.magnificationRegionContains(event.getX(), event.getY())) {
            float scale = this.mMagnificationController.getScale();
            float scaledOffsetX = this.mMagnificationController.getOffsetX();
            float scaledOffsetY = this.mMagnificationController.getOffsetY();
            int pointerCount = event.getPointerCount();
            PointerCoords[] coords = getTempPointerCoordsWithMinSize(pointerCount);
            PointerProperties[] properties = getTempPointerPropertiesWithMinSize(pointerCount);
            for (int i = 0; i < pointerCount; i++) {
                event2.getPointerCoords(i, coords[i]);
                coords[i].x = (coords[i].x - scaledOffsetX) / scale;
                coords[i].y = (coords[i].y - scaledOffsetY) / scale;
                event2.getPointerProperties(i, properties[i]);
            }
            event2 = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), pointerCount, properties, coords, 0, 0, 1.0f, 1.0f, event.getDeviceId(), 0, event.getSource(), event.getFlags());
        }
        super.onMotionEvent(event2, rawEvent, policyFlags);
    }

    private static void storeEventInto(Queue<MotionEvent> queue, MotionEvent event) {
        queue.add(MotionEvent.obtain(event));
        while (!queue.isEmpty() && event.getEventTime() - ((MotionEvent) queue.peek()).getEventTime() > 5000) {
            ((MotionEvent) queue.remove()).recycle();
        }
    }

    private PointerCoords[] getTempPointerCoordsWithMinSize(int size) {
        int oldSize = this.mTempPointerCoords != null ? this.mTempPointerCoords.length : 0;
        if (oldSize < size) {
            PointerCoords[] oldTempPointerCoords = this.mTempPointerCoords;
            this.mTempPointerCoords = new PointerCoords[size];
            if (oldTempPointerCoords != null) {
                System.arraycopy(oldTempPointerCoords, 0, this.mTempPointerCoords, 0, oldSize);
            }
        }
        for (int i = oldSize; i < size; i++) {
            this.mTempPointerCoords[i] = new PointerCoords();
        }
        return this.mTempPointerCoords;
    }

    private PointerProperties[] getTempPointerPropertiesWithMinSize(int size) {
        int oldSize;
        if (this.mTempPointerProperties != null) {
            oldSize = this.mTempPointerProperties.length;
        } else {
            oldSize = 0;
        }
        if (oldSize < size) {
            PointerProperties[] oldTempPointerProperties = this.mTempPointerProperties;
            this.mTempPointerProperties = new PointerProperties[size];
            if (oldTempPointerProperties != null) {
                System.arraycopy(oldTempPointerProperties, 0, this.mTempPointerProperties, 0, oldSize);
            }
        }
        for (int i = oldSize; i < size; i++) {
            this.mTempPointerProperties[i] = new PointerProperties();
        }
        return this.mTempPointerProperties;
    }

    private void transitionTo(State state) {
        this.mPreviousState = this.mCurrentState;
        this.mCurrentState = state;
    }

    private void zoomOn(float centerX, float centerY) {
        this.mMagnificationController.setScaleAndCenter(MathUtils.constrain(this.mMagnificationController.getPersistedScale(), MIN_SCALE, 5.0f), centerX, centerY, true, 0);
    }

    private void zoomOff() {
        this.mMagnificationController.reset(true);
    }

    private static MotionEvent recycleAndNullify(MotionEvent event) {
        if (event != null) {
            event.recycle();
        }
        return null;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MagnificationGesture{mDetectingState=");
        stringBuilder.append(this.mDetectingState);
        stringBuilder.append(", mDelegatingState=");
        stringBuilder.append(this.mDelegatingState);
        stringBuilder.append(", mMagnifiedInteractionState=");
        stringBuilder.append(this.mPanningScalingState);
        stringBuilder.append(", mViewportDraggingState=");
        stringBuilder.append(this.mViewportDraggingState);
        stringBuilder.append(", mDetectTripleTap=");
        stringBuilder.append(this.mDetectTripleTap);
        stringBuilder.append(", mDetectShortcutTrigger=");
        stringBuilder.append(this.mDetectShortcutTrigger);
        stringBuilder.append(", mCurrentState=");
        stringBuilder.append(State.nameOf(this.mCurrentState));
        stringBuilder.append(", mPreviousState=");
        stringBuilder.append(State.nameOf(this.mPreviousState));
        stringBuilder.append(", mMagnificationController=");
        stringBuilder.append(this.mMagnificationController);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
