package april.yun.tabstyle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import april.yun.ISlidingTabStrip;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yun.
 * @date 2017/4/21
 * @des [一句话描述]
 * @since [https://github.com/mychoices]
 * <p><a href="https://github.com/mychoices">github</a>
 */
public class DotsTabStyle extends JTabStyle {

    private Paint bgPaint;
    private Paint mIndicatorPaint;
    private float mW;
    private float mH;
    private PointF mCurrentTab;
    private float dosRadio = 10;
    private PointF mNextTab;
    //x:left  y:fight
    private PointF mLinePosition = new PointF(0, 0);

    private float mTabWidth;
    private List<PointF> fake_container = new ArrayList<>();
    private static final String TAG = DotsTabStyle.class.getSimpleName();


    public DotsTabStyle(ISlidingTabStrip slidingTabStrip) {
        super(slidingTabStrip);
        mTabStyleDelegate.setShouldExpand(true);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(mTabStyleDelegate.getDividerColor());

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setColor(mTabStyleDelegate.getIndicatorColor());
        mIndicatorPaint.setStrokeWidth(mTabStyleDelegate.getDividerWidth());
        //dosRadio = mTabStyleDelegate.getDividerWidth();
    }


    @Override public void onSizeChanged(int w, int h, int oldw, int oldh) {
        float pading = dp2dip(0.5f);
        mW = w - pading;
        mH = h - pading;

        fake_container = new ArrayList<>();
        mTabWidth = mW / mTabStrip.getTabCount();
        for (int i = 0; i < mTabStrip.getTabCount(); i++) {
            fake_container.add(new PointF(mTabWidth * i, mTabWidth * (i + 1)));
        }
    }


    @Override
    public void onDraw(Canvas canvas, ViewGroup tabsContainer, float currentPositionOffset, int lastCheckedPosition) {
        Log.d(TAG, "Current: " + mTabStyleDelegate.getCurrentPosition() + "----lastChecked: " +
                lastCheckedPosition);
        // draw indicator line
        // default: line below current tab
        mCurrentTab = fake_container.get(mTabStyleDelegate.getCurrentPosition());
        mLinePosition.x = mCurrentTab.x;
        mLinePosition.y = mCurrentTab.y;
        calcuteIndicatorLinePosition(tabsContainer, currentPositionOffset, lastCheckedPosition);
        //draw indicator
        canvas.drawRoundRect(mLinePosition.x + mTabWidth / 2 - dosRadio, mH / 2 - dosRadio,
                mLinePosition.y - mTabWidth / 2 + dosRadio, mH / 2 + dosRadio, dosRadio, dosRadio,
                mIndicatorPaint);
        //画默认圆
        for (int i = 0; i < fake_container.size(); i++) {
            canvas.drawCircle(mTabWidth / 2 + mTabWidth * i, mH / 2, dosRadio, bgPaint);
        }
    }


     protected void calcuteIndicatorLinePosition(ViewGroup tabsContainer, float currentPositionOffset, int
             lastCheckedPosition) {
        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if (currentPositionOffset > 0f &&
                mTabStyleDelegate.getCurrentPosition() < fake_container.size() - 1) {

            mNextTab = fake_container.get(mTabStyleDelegate.getCurrentPosition() + 1);
            float nextTabLeft = mNextTab.x;
            float nextTabRight = mNextTab.y;
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


    @Override public boolean needChildView() {
        return false;
    }
}
