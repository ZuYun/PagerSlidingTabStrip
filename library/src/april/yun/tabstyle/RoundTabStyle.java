package april.yun.tabstyle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import april.yun.ISlidingTabStrip;

/**
 * @author yun.
 * @date 2017/4/21
 * @des [一句话描述]
 * @since [https://github.com/mychoices]
 * <p><a href="https://github.com/mychoices">github</a>
 */
public class RoundTabStyle extends JTabStyle {

    private Paint rectPaint;
    private Paint dividerPaint;
    private float mOutRadio;
    private boolean mDragRight;
    private float mW;
    private float mH;
    private Path mClipath;

    //上一次最后一个tab右侧的位置
    private int preLasTabRight;
    private View mLastTab;
    private static final String TAG = RoundTabStyle.class.getSimpleName();


    public RoundTabStyle(ISlidingTabStrip slidingTabStrip) {
        super(slidingTabStrip);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setStyle(Paint.Style.FILL);
        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setStyle(Paint.Style.STROKE);
    }


    @Override public void afterSetViewPager(LinearLayout tabsContainer) {
        dividerPaint.setStrokeWidth(mTabStyleDelegate.getDividerWidth());
    }


    @Override public void onSizeChanged(int w, int h, int oldw, int oldh) {
        float pading = dp2dip(padingOffect);
        mW = w - pading;
        mH = h - pading;
        mTabCounts = mTabStrip.getTabsContainer().getChildCount();
        mLastTab = mTabStrip.getTabsContainer().getChildAt(mTabCounts - 1);
        //if (mTabStyleDelegate.isNotDrawIcon()) {
        getClipPath(pading, mW);
        mOutRadio = mH / 2;
        //}
    }


    private void getClipPath(float pading, float width) {
        RectF clip = new RectF(pading, pading, width-pading, mH);
        mClipath = new Path();
        mClipath.addRoundRect(clip, mH / 2f, mH / 2f, Path.Direction.CCW);
    }


    @Override
    public void onDraw(Canvas canvas, ViewGroup tabsContainer, float currentPositionOffset, int lastCheckedPosition) {

        if (mTabStyleDelegate.getFrameColor() != Color.TRANSPARENT) {
            //画边框
            dividerPaint.setColor(mTabStyleDelegate.getFrameColor());
            canvas.drawRoundRect(dp2dip(padingOffect), dp2dip(padingOffect), mLastTab.getRight()-dp2dip(padingOffect), this.mH, mOutRadio,
                    mOutRadio, dividerPaint);
        }
        if (mTabStyleDelegate.getIndicatorColor() != Color.TRANSPARENT) {
            if (mTabStyleDelegate.isNotDrawIcon()) {
                //当最后的tab在屏幕之外/之内的时候
                if (preLasTabRight != mLastTab.getRight()) {
                    Log.d(TAG, "reCalculate clip Path");
                    preLasTabRight = mLastTab.getRight();
                    getClipPath(dp2dip(padingOffect), preLasTabRight);
                }
                canvas.save();
                canvas.clipPath(mClipath);
            }
            // draw indicator line
            rectPaint.setColor(mTabStyleDelegate.getIndicatorColor());

            calcuteIndicatorLinePosition(tabsContainer, currentPositionOffset, lastCheckedPosition);

            //draw indicator
            canvas.drawRect(mLinePosition.x, 0, mLinePosition.y, mH,
                    rectPaint);
        }

        if (mTabStyleDelegate.getUnderlineColor() != Color.TRANSPARENT) {
            // draw underline
            rectPaint.setColor(mTabStyleDelegate.getUnderlineColor());
            canvas.drawRect(0, mH - mTabStyleDelegate.getUnderlineHeight(), tabsContainer.getWidth(), mH,
                    rectPaint);
        }

        if (mTabStyleDelegate.getDividerColor() != Color.TRANSPARENT) {
            // draw divider
            dividerPaint.setColor(mTabStyleDelegate.getDividerColor());
            for (int i = 0; i < tabsContainer.getChildCount() - 1; i++) {
                View tab = tabsContainer.getChildAt(i);
                canvas.drawLine(tab.getRight(), mTabStyleDelegate.getDividerPadding(), tab.getRight(),
                        mH - mTabStyleDelegate.getDividerPadding(), dividerPaint);
            }
        }
    }

}
