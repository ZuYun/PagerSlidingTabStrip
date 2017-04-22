package april.yun.tabstyle;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import april.yun.ISlidingTabStrip;
import april.yun.other.JTabStyleDelegate;

/**
 * @author yun.
 * @date 2017/4/21
 * @des [一句话描述]
 * @since [https://github.com/mychoices]
 * <p><a href="https://github.com/mychoices">github</a>
 */
public abstract class JTabStyle {
    protected final JTabStyleDelegate mTabStyleDelegate;
    protected ISlidingTabStrip mTabStrip;
    protected boolean mDragRight;
    protected View mCurrentTab;
    protected View mNextTab;
    protected int mTabCounts;
    //x:left  y:fight
    protected PointF mLinePosition = new PointF(0, 0);


    JTabStyle(ISlidingTabStrip slidingTabStrip) {
        mTabStyleDelegate = slidingTabStrip.getTabStyleDelegate();
        mTabStrip = slidingTabStrip;
    }


    public abstract void onSizeChanged(int w, int h, int oldw, int oldh);

    public abstract void onDraw(Canvas canvas, ViewGroup tabsContainer, float currentPositionOffset, int lastCheckedPosition);


    public float dp2dip(float dp) {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }


    public boolean needChildView() {
        return true;
    }


    protected void calcuteIndicatorLinePosition(ViewGroup tabsContainer, float currentPositionOffset, int lastCheckedPosition) {
        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if (currentPositionOffset > 0f &&
                mTabStyleDelegate.getCurrentPosition() < tabsContainer.getChildCount() - 1) {

            mNextTab = tabsContainer.getChildAt(mTabStyleDelegate.getCurrentPosition() + 1);
            final float nextTabLeft = mNextTab.getLeft();
            final float nextTabRight = mNextTab.getRight();
            //moveStyle_normal(currentPositionOffset, nextTabLeft, nextTabRight);

            moveStyle_sticky(currentPositionOffset, lastCheckedPosition, nextTabLeft, nextTabRight);
        }
    }


    protected void moveStyle_normal(float currentPositionOffset, float nextTabLeft, float nextTabRight) {
        mLinePosition.x = (currentPositionOffset * nextTabLeft +
                (1f - currentPositionOffset) * mLinePosition.x);
        mLinePosition.y = (currentPositionOffset * nextTabRight +
                (1f - currentPositionOffset) * mLinePosition.y);
    }


    protected void moveStyle_sticky(float currentPositionOffset, int lastCheckedPosition, float nextTabLeft, float nextTabRight) {
        if (mTabStrip.getState() == ViewPager.SCROLL_STATE_DRAGGING ||
                mTabStrip.getState() == ViewPager.SCROLL_STATE_IDLE) {
            if (lastCheckedPosition == mTabStyleDelegate.getCurrentPosition()) {
                mDragRight = true;
                //Log.d(TAG, "往右 ------>> ");
            }
            else {
                mDragRight = false;
                //Log.d(TAG, "往左 <<------");
            }
        }
        if (mDragRight) {
            //                ------>>
            if (currentPositionOffset >= 0.5) {
                mLinePosition.x = (
                        2 * (nextTabLeft - mLinePosition.x) * currentPositionOffset + 2 * mLinePosition.x -
                                nextTabLeft);
            }
            mLinePosition.y = (currentPositionOffset * nextTabRight +
                    (1f - currentPositionOffset) * mLinePosition.y);
        }
        else {
            //                <<------
            mLinePosition.x = (currentPositionOffset * nextTabLeft +
                    (1f - currentPositionOffset) * mLinePosition.x);
            if (currentPositionOffset <= 0.5) {
                mLinePosition.y = (2 * (nextTabRight - mLinePosition.y) * currentPositionOffset +
                        mLinePosition.y);
            }
            else {
                mLinePosition.y = nextTabRight;
            }
        }
    }
}

