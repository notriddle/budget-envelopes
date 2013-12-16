/*
 * This file is a part of Budget with Envelopes.
 * Copyright 2013 Michael Howell <michael@notriddle.com>
 *
 * Budget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Budget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Budget. If not, see <http://www.gnu.org/licenses/>.
 */

package com.notriddle.budget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.animation.LinearInterpolator;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Checkable;
import android.widget.FrameLayout;
import java.util.ArrayList;

public class DeleteView extends FrameLayout implements Checkable {
    public static interface OnDeleteListener {
        public void onDelete(DeleteView v);
    };

    private static final int STATE_READY = 0;
    private static final int STATE_IN_SWIPE = 1;
    private static final int STATE_ANIMATING = 2;
    private static final int STATE_DELETING = 3;

    View mInnerView;
    float mSwipeStart;
    long mSwipeStartTime;
    int mSwipeState;
    int mBackground;
    int mTouchSlop;
    int mFlingSlop;
    int mFlingCap;
    int mLongPressTimeout;
    VelocityTracker mVelocityTracker;
    ObjectAnimator mAnim;
    boolean mChecked;
    OnDeleteListener mListener;

    public DeleteView(Context cntx) {
        super(cntx);
        init(cntx);
    }
    public DeleteView(Context cntx, AttributeSet attrs) {
        super(cntx, attrs);
        init(cntx);
    }
    public DeleteView(Context cntx, AttributeSet attrs, int defStyleAttr) {
        super(cntx, attrs, defStyleAttr);
        init(cntx);
    }
    private void init(Context cntx) {
        mInnerView = null;
        mSwipeStart = -1;
        mSwipeStartTime = -1;
        mBackground = 0;
        ViewConfiguration config = ViewConfiguration.get(cntx);
        mTouchSlop = config.getScaledTouchSlop();
        mFlingSlop = config.getScaledMinimumFlingVelocity();
        mFlingCap = config.getScaledMaximumFlingVelocity();
        mLongPressTimeout = config.getLongPressTimeout();
        mSwipeState = STATE_READY;
        mVelocityTracker = null;
        mAnim = null;
        mChecked = false;
        mListener = null;
        setClickable(true);
    }

    @Override public void onFinishInflate() {
        super.onFinishInflate();
        mInnerView = getChildAt(0);
    }

    @Override public boolean isChecked() {
        return mChecked;
    }
    @Override public void setChecked(boolean checked) {
        mChecked = checked;
        if (mInnerView instanceof Checkable) {
            ((Checkable)mInnerView).setChecked(checked);
        }
    }
    @Override public void toggle() {
        setChecked(!mChecked);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                switch (mSwipeState) {
                    case STATE_READY:
                        mSwipeStart = x;
                        mSwipeStartTime = event.getEventTime();
                        mInnerView.setPressed(true);
                        break;
                    case STATE_ANIMATING:
                        cancelAnimation(x);
                        break;
                    case STATE_DELETING:
                        // Do nothing.
                        break;
                    default:
                        throw new Error("Invalid state with ACTION_DOWN.");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mSwipeState) {
                    case STATE_READY:
                        if (Math.abs(x - mSwipeStart) > mTouchSlop) {
                            startSwipe();
                        }
                        break;
                    case STATE_IN_SWIPE:
                        setInnerViewPosition(event.getRawX() - mSwipeStart);
                        mVelocityTracker.addMovement(event);
                        break;
                    case STATE_ANIMATING:
                        cancelAnimation(x);
                        break;
                    case STATE_DELETING:
                        // Do nothing.
                        break;
                    default:
                        throw new Error("Invalid state with ACTION_MOVE.");
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (mSwipeState) {
                    case STATE_READY:
                        mInnerView.setPressed(false);
                        if (event.getEventTime() - mSwipeStartTime > mLongPressTimeout) {
                            performLongClick();
                        } else {
                            performClick();
                        }
                        break;
                    case STATE_IN_SWIPE:
                        startAnimation();
                        break;
                    case STATE_ANIMATING:
                    case STATE_DELETING:
                        // Do nothing.
                        break;
                    default:
                        throw new Error("Invalid state with ACTION_UP.");
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                switch (mSwipeState) {
                    case STATE_READY:
                        mInnerView.setPressed(false);
                        break;
                    case STATE_IN_SWIPE:
                        startAnimation();
                        break;
                    case STATE_ANIMATING:
                    case STATE_DELETING:
                        // Do nothing.
                        break;
                    default:
                        throw new Error("Invalid state with ACTION_CANCEL.");
                }
                break;
            default:
                throw new Error("Invalid MotionEvent!");
        }

        Log.d("Budget", "onTouchEvent(): "+mSwipeState);

        return true;
    }

    private void startSwipe() {
        if (getParent() instanceof ViewGroup) {
            ViewGroup p = (ViewGroup)getParent();
            p.requestDisallowInterceptTouchEvent(true);
        }
        mSwipeState = STATE_IN_SWIPE;
        setBackgroundResource(mBackground);
        mVelocityTracker = VelocityTracker.obtain();
        setClickable(false);
        mInnerView.setPressed(false);
    }

    private void cancelSwipe() {
        if (getParent() instanceof ViewGroup) {
            ViewGroup p = (ViewGroup)getParent();
            p.requestDisallowInterceptTouchEvent(false);
        }
        mSwipeState = STATE_READY;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        if (mAnim != null) {
            ObjectAnimator anim = mAnim;
            mAnim = null;
            anim.cancel();
        }
        setClickable(true);
        setInnerViewPosition(0);
        setBackgroundResource(0);
        setInnerViewHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void performDelete() {
        Log.d("Budget", "performDelete()");
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup)getParent()).requestDisallowInterceptTouchEvent(false);
        }
        mSwipeState = STATE_DELETING;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        setClickable(false);
        final int oldPadding = getPaddingY();
        final ObjectAnimator paddingAnim = ObjectAnimator.ofInt(
            this, "paddingY", oldPadding, 0
        );
        paddingAnim.setDuration(250);
        paddingAnim.start();
        mAnim = ObjectAnimator.ofInt(
            this, "innerViewHeight", mInnerView.getHeight(), 0
        );
        mAnim.setDuration(250);
        mAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                if (mAnim != null) {
                    mListener.onDelete(DeleteView.this);
                    Log.d("Budget", "performDelete(): done");
                } else {
                    Log.d("Budget", "performDelete(): canceled");
                    paddingAnim.cancel();
                    setPaddingY(oldPadding);
                }
            }
        });
        mAnim.start();
    }

    private void startAnimation() {
        mVelocityTracker.computeCurrentVelocity(1);
        float velocityChk = mVelocityTracker.getXVelocity();
        final float position = mInnerView.getTranslationX();
        float velocityChk2 = velocityChk == 0 ? -position : velocityChk;
        if (Math.abs(velocityChk2) > mFlingCap) {
            float velocityChk2Sign = velocityChk2 / Math.abs(velocityChk2);
            velocityChk2 = velocityChk2Sign * mFlingCap;
        }
        final float velocity = velocityChk2;
        final float farPosition = (position > 0 ? 1 : -1) * mInnerView.getWidth();
        final float finalPosition = ((velocity / position) > 0)
                                    ? farPosition : 0;
        final float displacement = finalPosition - position;
        mAnim = ObjectAnimator.ofFloat(
            this, "innerViewPosition",
            position, finalPosition
        );
        mAnim.setDuration((long)Math.min(Math.abs(displacement / velocity), 500));
        mAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                if (mAnim != null) {
                    if (finalPosition == 0) {
                        cancelSwipe();
                    } else {
                        performDelete();
                    }
                }
            }
        });
        mAnim.setInterpolator(new LinearInterpolator());
        mSwipeState = STATE_ANIMATING;
        mAnim.start();
    }

    private void cancelAnimation(float newStartPosition) {
        if (mAnim != null) {
            ObjectAnimator anim = mAnim;
            mAnim = null;
            anim.cancel();
        }
        mVelocityTracker.clear();
        mSwipeStart = newStartPosition - mInnerView.getTranslationX();
        mSwipeState = STATE_IN_SWIPE;
    }

    private void setInnerViewPosition(float position) {
        mInnerView.setTranslationX(position);
        float width = mInnerView.getWidth();
        mInnerView.setAlpha((width-Math.abs(position))/width);
        //Log.d("Budget", "setInnerViewPosition("+position+")");
    }
    private void setInnerViewHeight(int height) {
        mInnerView.getLayoutParams().height = height;
        mInnerView.requestLayout();
        //Log.d("Budget", "setInnerViewHeight("+height+")");
    }

    public void setInnerView(View innerView) {
        if (mInnerView != null) {
            removeView(innerView);
        }
        mInnerView = innerView;
        addView(innerView);
        if (mInnerView instanceof Checkable) {
            ((Checkable)mInnerView).setChecked(mChecked);
        }
        if (mSwipeState != STATE_READY) {
            cancelSwipe();
        }
    }
    public View getInnerView() {
        return mInnerView;
    }

    public int getPaddingY() {
        return getPaddingTop();
    }
    public void setPaddingY(int pad) {
        setPadding(getPaddingLeft(), pad, getPaddingRight(), pad);
    }

    public void setSwipeBackgroundResource(int background) {
        mBackground = background;
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        mListener = listener;
    }
}
