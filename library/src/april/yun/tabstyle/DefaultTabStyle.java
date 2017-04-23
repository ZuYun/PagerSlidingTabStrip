package april.yun.tabstyle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
public class DefaultTabStyle extends JTabStyle {

    private Paint rectPaint;
    private Paint dividerPaint;
    private float mOutRadio = 0;
    private boolean mDragRight;
    private float mH;
    private View mLastTab;


    public DefaultTabStyle(ISlidingTabStrip slidingTabStrip) {
        super(slidingTabStrip);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setStyle(Paint.Style.FILL);

        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setStyle(Paint.Style.STROKE);
    }


    @Override public void afterSetViewPager(LinearLayout tabsContainer) {
        dividerPaint.setStrokeWidth(mTabStyleDelegate.getDividerWidth());
        mOutRadio = mTabStyleDelegate.getCornerRadio();
    }


    @Override public void onSizeChanged(int w, int h, int oldw, int oldh) {
        float pading = dp2dip(padingOffect);
        mH = h - pading;
        mTabCounts = mTabStrip.getTabsContainer().getChildCount();
        mLastTab = mTabStrip.getTabsContainer().getChildAt(mTabCounts - 1);
    }


    @Override
    public void onDraw(Canvas canvas, ViewGroup tabsContainer, float currentPositionOffset, int lastCheckedPosition) {
        if (mTabStyleDelegate.getFrameColor() != Color.TRANSPARENT) {
            //画边框
            dividerPaint.setColor(mTabStyleDelegate.getFrameColor());
            canvas.drawRoundRect(dp2dip(padingOffect), dp2dip(padingOffect),
                    mLastTab.getRight() - dp2dip(padingOffect), this.mH, mOutRadio, mOutRadio, dividerPaint);
        }

        if (mTabStyleDelegate.getIndicatorColor() != Color.TRANSPARENT) {
            // draw indicator line
            rectPaint.setColor(mTabStyleDelegate.getIndicatorColor());
            calcuteIndicatorLinePosition(tabsContainer, currentPositionOffset, lastCheckedPosition);
            //draw indicator
            if (mTabStyleDelegate.getIndicatorHeight() >= mH / 2) {
                //画在中间
                int halfIndHeight = mTabStyleDelegate.getIndicatorHeight() / 2;
                float indPading = mH / 2 - halfIndHeight;

                canvas.drawRoundRect(mLinePosition.x + indPading, indPading, mLinePosition.y - indPading,
                        mH - indPading, mOutRadio, mOutRadio, rectPaint);
            }
            else {
                //画在底部
                //draw indicator
                canvas.drawRoundRect(mLinePosition.x, mH - mTabStyleDelegate.getIndicatorHeight(),
                        mLinePosition.y, mH, mOutRadio, mOutRadio, rectPaint);
            }
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
