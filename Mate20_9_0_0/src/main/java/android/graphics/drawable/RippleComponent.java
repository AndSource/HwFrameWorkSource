package android.graphics.drawable;

import android.animation.Animator;
import android.graphics.Rect;

abstract class RippleComponent {
    protected final Rect mBounds;
    protected float mDensityScale;
    protected boolean mHasMaxRadius;
    protected final RippleDrawable mOwner;
    protected Animator mSoftwareAnimator;
    protected float mTargetRadius;

    public RippleComponent(RippleDrawable owner, Rect bounds) {
        this.mOwner = owner;
        this.mBounds = bounds;
    }

    public void onBoundsChange() {
        if (!this.mHasMaxRadius) {
            this.mTargetRadius = getTargetRadius(this.mBounds);
            onTargetRadiusChanged(this.mTargetRadius);
        }
    }

    public final void setup(float maxRadius, int densityDpi) {
        if (maxRadius >= 0.0f) {
            this.mHasMaxRadius = true;
            this.mTargetRadius = maxRadius;
        } else {
            this.mTargetRadius = getTargetRadius(this.mBounds);
        }
        this.mDensityScale = ((float) densityDpi) * 0.00625f;
        onTargetRadiusChanged(this.mTargetRadius);
    }

    private static float getTargetRadius(Rect bounds) {
        float halfWidth = ((float) bounds.width()) / 2.0f;
        float halfHeight = ((float) bounds.height()) / 2.0f;
        return (float) Math.sqrt((double) ((halfWidth * halfWidth) + (halfHeight * halfHeight)));
    }

    public void getBounds(Rect bounds) {
        int r = (int) Math.ceil((double) this.mTargetRadius);
        bounds.set(-r, -r, r, r);
    }

    protected final void invalidateSelf() {
        this.mOwner.invalidateSelf(false);
    }

    protected final void onHotspotBoundsChanged() {
        if (!this.mHasMaxRadius) {
            this.mTargetRadius = getTargetRadius(this.mBounds);
            onTargetRadiusChanged(this.mTargetRadius);
        }
    }

    protected void onTargetRadiusChanged(float targetRadius) {
    }
}
