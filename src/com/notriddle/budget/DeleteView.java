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
import android.util.Log;
import android.widget.FrameLayout;

public class DeleteView extends FrameLayout {
    View mInnerView;
    float mSwipeStart;
    boolean mInSwipe;
    int mBackground;
    int mTouchSlop;
    int mFlingSlop;
    int mFlingCap;
    VelocityTracker mVelocityTracker;
    ObjectAnimator mAnim;

    public DeleteView(Context cntx) {
        super(cntx);
        mInnerView = null;
        mSwipeStart = -1;
        mBackground = 0;
        ViewConfiguration config = ViewConfiguration.get(cntx);
        mTouchSlop = config.getScaledTouchSlop();
        mFlingSlop = config.getScaledMinimumFlingVelocity();
        mFlingCap = config.getScaledMaximumFlingVelocity();
        mInSwipe = false;
        mVelocityTracker = null;
        mAnim = null;
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
        onTouchEvent(event);
        return mInSwipe;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (mInSwipe) {
            whileInSwipe(event);
        } else {
            whileNotInSwipe(event);
        }
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup)getParent()).requestDisallowInterceptTouchEvent(mInSwipe);
        }
        return true;
    }

    private void whileNotInSwipe(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d("Budget", "whileNotInSwipe(): ACTION_DOWN");
            mSwipeStart = event.getRawX();
            mVelocityTracker = VelocityTracker.obtain();
            mInSwipe = false;
        } else if (action == MotionEvent.ACTION_MOVE) {
            Log.d("Budget", "whileNotInSwipe(): ACTION_MOVE");
            if (Math.abs(event.getRawX() - mSwipeStart) > mTouchSlop) {
                mInSwipe = true;
                setBackgroundResource(mBackground);
            }
        }
    }

    private void whileInSwipe(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d("Budget", "whileInSwipe(): ACTION_DOWN");
            if (mAnim != null) {
                ObjectAnimator anim = mAnim;
                mAnim = null;
                anim.cancel();
            }
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            } else {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mSwipeStart = event.getRawX() - mInnerView.getTranslationX();
        } else if (action == MotionEvent.ACTION_MOVE) {
            Log.d("Budget", "whileInSwipe(): ACTION_MOVE");
            setInnerViewPosition(event.getRawX() - mSwipeStart);
            mVelocityTracker.addMovement(event);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            Log.d("Budget", "whileInSwipe(): ACTION_UP");
            mVelocityTracker.computeCurrentVelocity(1);
            float velocityChk = mVelocityTracker.getXVelocity();
            final float position = event.getRawX() - mSwipeStart;
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
            mAnim.setDuration((long)Math.min(Math.abs(displacement / velocity), 1000));
            mAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator anim) {
                    if (mAnim != null) {
                        if (finalPosition != 0) {
                            mInnerView.setVisibility(View.GONE);
                        }
                        mInSwipe = false;
                        setBackgroundResource(0);
                        mAnim = null;
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
            });
            mAnim.setInterpolator(new LinearInterpolator());
            mAnim.start();
        }
    }

    private void setInnerViewPosition(float position) {
        mInnerView.setTranslationX(position);
        float width = mInnerView.getWidth();
        mInnerView.setAlpha((width-Math.abs(position))/width);
        //Log.d("Budget", "setInnerViewPosition("+position+")");
    }

    public void setInnerView(View innerView) {
        if (mInnerView != null) {
            removeView(innerView);
        }
        mInnerView = innerView;
        addView(innerView);
    }
    public View getInnerView() {
        return mInnerView;
    }
    public void setSwipeBackgroundResource(int background) {
        mBackground = background;
    }
}
