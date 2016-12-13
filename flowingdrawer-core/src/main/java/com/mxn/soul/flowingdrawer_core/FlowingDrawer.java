
package com.mxn.soul.flowingdrawer_core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * Created by mxn on 2016/10/17.
 * FlowingDrawer
 */
public class FlowingDrawer extends ElasticDrawer {

    static final boolean USE_TRANSLATIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

    public FlowingDrawer(Context context) {
        super(context);
    }

    public FlowingDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowingDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("NewApi")
    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);
    }

    @Override
    protected void drawOverlay(Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();
        final int offsetPixels = (int) mOffsetPixels;
        final float openRatio = Math.abs(mOffsetPixels) / mMenuSize;

        switch (getPosition()) {
            case Position.LEFT:
                mMenuOverlay.setBounds(offsetPixels, 0, width, height);
                break;

            case Position.RIGHT:
                mMenuOverlay.setBounds(0, 0, width + offsetPixels, height);
                break;
        }
        mMenuOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * openRatio));
        mMenuOverlay.draw(canvas);
    }

    @Override
    public void openMenu(boolean animate) {
        int animateTo = 0;
        switch (getPosition()) {
            case Position.LEFT:
                animateTo = mMenuSize;
                break;
            case Position.RIGHT:
                animateTo = -mMenuSize;
                break;
        }
        animateOffsetTo(animateTo, 0, animate);
    }

    @Override
    public void closeMenu(boolean animate) {
        animateOffsetTo(0, 0, animate);
    }


    @SuppressLint("NewApi")
    @Override
    protected void onOffsetPixelsChanged(int offsetPixels) {
        if (USE_TRANSLATIONS) {
            switch (getPosition()) {
                case Position.LEFT:
                    mMenuContainer.setTranslationX(offsetPixels - mMenuSize);
                    break;
                case Position.RIGHT:
                    mMenuContainer.setTranslationX(offsetPixels + mMenuSize);
                    break;

            }
        } else {
            switch (getPosition()) {
                case Position.LEFT:
                    mMenuContainer.offsetLeftAndRight(offsetPixels - mMenuContainer.getRight());
                    break;

                case Position.RIGHT:
                    mMenuContainer.offsetLeftAndRight(offsetPixels - (mMenuContainer.getLeft() - getWidth()));
                    break;
            }
        }

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onOffsetPixelsChanged((int) mOffsetPixels);
    }


    @Override
    protected GradientDrawable.Orientation getDropShadowOrientation() {
        switch (getPosition()) {
            case Position.RIGHT:
                return GradientDrawable.Orientation.RIGHT_LEFT;
            default:
                return GradientDrawable.Orientation.LEFT_RIGHT;
        }
    }

    @Override
    protected void updateDropShadowRect() {
//        final float openRatio = Math.abs(mOffsetPixels) / mMenuSize;
//        final int dropShadowSize = (int) (mShadowSize * openRatio);
//        switch (getPosition()) {
//            case Position.LEFT:
//                mDropShadowRect.top = 0;
//                mDropShadowRect.bottom = getHeight();
//                mDropShadowRect.left = ViewHelper.getRight(mMenuContainer);
//                mDropShadowRect.right = mDropShadowRect.left + dropShadowSize;
//                break;
//
//            case Position.RIGHT:
//                mDropShadowRect.top = 0;
//                mDropShadowRect.bottom = getHeight();
//                mDropShadowRect.right = ViewHelper.getLeft(mMenuContainer);
//                mDropShadowRect.left = mDropShadowRect.right - dropShadowSize;
//                break;
//
//        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void startLayerTranslation() {
        if (USE_TRANSLATIONS && mHardwareLayersEnabled && !mLayerTypeHardware) {
            mLayerTypeHardware = true;
            mMenuContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void stopLayerTranslation() {
        if (mLayerTypeHardware) {
            mLayerTypeHardware = false;
            mMenuContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            throw new IllegalStateException("Must measure with an exact size");
        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mOffsetPixels == -1) openMenu(false);

        int menuWidthMeasureSpec;
        int menuHeightMeasureSpec;
        menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mMenuSize);
        menuHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mMenuContainer.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

        final int contentWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mContentContainer.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);

        setMeasuredDimension(width, height);

        updateTouchAreaSize();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;

        mContentContainer.layout(0, 0, width, height);

        if (USE_TRANSLATIONS) {
            switch (getPosition()) {
                case Position.LEFT:
                    mMenuContainer.layout(0, 0, mMenuSize, height);
                    break;

                case Position.RIGHT:
                    mMenuContainer.layout(width - mMenuSize, 0, width, height);
                    break;
            }

        } else {
            final int offsetPixels = (int) mOffsetPixels;
            final int menuSize = mMenuSize;

            switch (getPosition()) {
                case Position.LEFT:
                    mMenuContainer.layout(-menuSize + offsetPixels, 0, offsetPixels, height);
                    break;
                case Position.RIGHT:
                    mMenuContainer.layout(width + offsetPixels, 0, width + menuSize + offsetPixels, height);
                    break;
            }
        }
    }


    private boolean isContentTouch(int x) {
        boolean contentTouch = false;

        switch (getPosition()) {
            case Position.LEFT:
                contentTouch = ViewHelper.getRight(mMenuContainer) < x;
                break;
            case Position.RIGHT:
                contentTouch = ViewHelper.getLeft(mMenuContainer) > x;
                break;
        }
        return contentTouch;
    }

    protected boolean onDownAllowDrag() {
        switch (getPosition()) {
            case Position.LEFT:
                return (!mMenuVisible && mInitialMotionX <= mTouchSize)
                        || (mMenuVisible && mInitialMotionX <= mOffsetPixels);

            case Position.RIGHT:
                final int width = getWidth();
                final int initialMotionX = (int) mInitialMotionX;

                return (!mMenuVisible && initialMotionX >= width - mTouchSize)
                        || (mMenuVisible && initialMotionX >= width + mOffsetPixels);
        }

        return false;
    }

    protected boolean onMoveAllowDrag(int x, float dx ) {
        if (mMenuVisible && mTouchMode == TOUCH_MODE_FULLSCREEN) {
            return true;
        }

        switch (getPosition()) {
            case Position.LEFT:
                return (!mMenuVisible && mInitialMotionX <= mTouchSize && (dx > 0)) // Drawer closed
                        || (mMenuVisible && x <= mOffsetPixels) ;// Drawer open
            case Position.RIGHT:
                final int width = getWidth();
                return (!mMenuVisible && mInitialMotionX >= width - mTouchSize && (dx < 0))
                        || (mMenuVisible && x >= width - mOffsetPixels) ;

        }

        return false;
    }
    protected void onMoveEvent(float dx) {
        switch (getPosition()) {
            case Position.LEFT:
                setOffsetPixels(Math.min(Math.max(mOffsetPixels + dx, 0), mMenuSize/2));
                break;

            case Position.RIGHT:
                setOffsetPixels(Math.max(Math.min(mOffsetPixels + dx, 0), -mMenuSize));
                break;
        }
    }

    protected void onUpEvent(int x) {
        switch (getPosition()) {
            case Position.LEFT: {
                if (mIsDragging) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getXVelocity(mVelocityTracker);
                    mLastMotionX = x;
                    animateOffsetTo(initialVelocity > 0 ? mMenuSize : 0, initialVelocity, true);
                    // Close the menu when content is clicked while the menu is visible.
                } else if (mMenuVisible) {
                    closeMenu();
                }
                break;
            }

            case Position.RIGHT: {
                if (mIsDragging) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getXVelocity(mVelocityTracker);
                    mLastMotionX = x;
                    animateOffsetTo(initialVelocity > 0 ? 0 : -mMenuSize, initialVelocity, true);
                    // Close the menu when content is clicked while the menu is visible.
                } else if (mMenuVisible) {
                    closeMenu();
                }
                break;
            }
        }
    }

    protected boolean checkTouchSlop(float dx, float dy) {
        return Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy);
    }

    @Override
    protected void stopAnimation() {
        super.stopAnimation();
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }
    private void onPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mActivePointerId = INVALID_POINTER;
            mIsDragging = false;
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }

            if (Math.abs(mOffsetPixels) > mMenuSize / 2) {
                openMenu();
            } else {
                closeMenu();
            }

            return false;
        }

        if (action == MotionEvent.ACTION_DOWN && mMenuVisible && isCloseEnough()) {
            setOffsetPixels(0);
            stopAnimation();
            setDrawerState(STATE_CLOSED);
            mIsDragging = false;
        }

        // Always intercept events over the content while menu is visible.
        if (mMenuVisible) {
            int index = 0;
            if (mActivePointerId != INVALID_POINTER) {
                index = ev.findPointerIndex(mActivePointerId);
                index = index == -1 ? 0 : index;
            }
            final int x = (int) ev.getX(index);
            if (isContentTouch(x)) {
                return true;
            }
        }

        if (!mMenuVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsDragging) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag();
                mActivePointerId = ev.getPointerId(0);

                if (allowDrag) {
                    setDrawerState(mMenuVisible ? STATE_OPEN : STATE_CLOSED);
                    stopAnimation();
                    mIsDragging = false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    closeMenu(true);
                    return false;
                }

                final float x = ev.getX(pointerIndex);
                final float dx = x - mLastMotionX;
                final float y = ev.getY(pointerIndex);
                final float dy = y - mLastMotionY;

                if (checkTouchSlop(dx, dy)) {
                    if (mOnInterceptMoveEventListener != null && (mTouchMode == TOUCH_MODE_FULLSCREEN || mMenuVisible)
                            && canChildrenScroll((int) dx, (int) dy, (int) x, (int) y)) {
                        endDrag(); // Release the velocity tracker
                        requestDisallowInterceptTouchEvent(true);
                        return false;
                    }

                    final boolean allowDrag = onMoveAllowDrag((int) x, dx);

                    if (allowDrag) {
                        stopAnimation();
                        setDrawerState(STATE_DRAGGING);
                        mIsDragging = true;
                        mLastMotionX = x;
                        mLastMotionY = y;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        return mIsDragging;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mMenuVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag();

                mActivePointerId = ev.getPointerId(0);

                if (allowDrag) {
                    stopAnimation();
                    startLayerTranslation();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex == -1) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    closeMenu(true);
                    return false;
                }

//                if (!mIsDragging) {
//                    final float x = ev.getX(pointerIndex);
//                    final float dx = x - mLastMotionX;
//                    final float y = ev.getY(pointerIndex);
//                    final float dy = y - mLastMotionY;
//
//                    if (checkTouchSlop(dx, dy)) {
//                        final boolean allowDrag = onMoveAllowDrag((int) x, dx);
//
//                        if (allowDrag) {
//                            stopAnimation();
//                            setDrawerState(STATE_DRAGGING);
//                            mIsDragging = true;
//                            mLastMotionX = x;
//                            mLastMotionY = y;
//                        } else {
//                            mInitialMotionX = x;
//                            mInitialMotionY = y;
//                        }
//                    }
//                }

                if (mIsDragging) {

                    startLayerTranslation();

                    final float x = ev.getX(pointerIndex);
                    final float dx = x - mLastMotionX;
                    final float y = ev.getY(pointerIndex);

                    mLastMotionX = x;
                    mLastMotionY = y;

                    if (mOffsetPixels + dx < mMenuSize/2) {

                        onMoveEvent(dx);


                    } else {
                        mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                        final int initialVelocity = (int) getXVelocity(mVelocityTracker);
                        mLastMotionX = x;
                        animateOffsetTo(mMenuSize, initialVelocity, true);

                        endDrag();
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                int index = ev.findPointerIndex(mActivePointerId);
                index = index == -1 ? 0 : index;
                final int x = (int) ev.getX(index);
                onUpEvent(x);
                mActivePointerId = INVALID_POINTER;
                mIsDragging = false;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                mLastMotionX = ev.getX(index);
                mLastMotionY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        return true;
    }

}
