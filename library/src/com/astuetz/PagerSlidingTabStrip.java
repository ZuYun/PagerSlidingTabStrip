/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.astuetz;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PagerSlidingTabStrip extends HorizontalScrollView {

    private float mW;
    private float mH;
    private Path mClipath;
    private boolean mCapsuleStyle = true;
    private ColorStateList mTabTextColorStateList;
    private float mOutRadio;
    /**
     * 顶部指示器模式
     */
    public static final int MODE_TOP = 0;
    /**
     * 底部选项卡模式
     */
    public static final int MODE_BUTTOM = 1;
    private int mTabMode = MODE_TOP;
    private static final String TAG = PagerSlidingTabStrip.class.getSimpleName();
    private int mLastCheckedPosition = -1;
    private int mState = -1;
    private boolean mDragRight;
    private RectF mClipRectf;
    private int tabBackgroundResId;

    @IntDef({MODE_TOP, MODE_BUTTOM})
    public @interface TabMode {}

    public interface IconTabProvider {
        /**
         * 如果 返回 null 則調用getPageIconResId
         *
         * @param position
         *         1,简单的背景图片
         *         2，0为checked pressed背景  1为normal背景
         */
        public int[] getPageIconResIds(int position);

        /**
         * 兩個都實現的話 默認使用getPageIconResIds
         *
         * @param position
         * @return
         */
        public int getPageIconResId(int position);
    }

    private RadioGroup.LayoutParams defaultTabLayoutParams;
    private RadioGroup.LayoutParams expandedTabLayoutParams;

    private final PageListener pageListener = new PageListener();
    public OnPageChangeListener delegatePageListener;

    private RadioGroup tabsContainer;
    private ViewPager pager;

    private int mTabCount;

    private int currentPosition = 0;
    private float currentPositionOffset = 0f;

    private Paint rectPaint;
    private Paint dividerPaint;

    private int indicatorColor = 0xFF666666;
    private int underlineColor = 0x1A000000;
    private int dividerColor = 0x1A000000;

    private boolean shouldExpand = false;
    private boolean textAllCaps = true;

    private int scrollOffset = 52;
    private int indicatorHeight = 8;
    private int underlineHeight = 2;
    private int dividerPadding = 12;
    private int tabPadding = 24;
    private int dividerWidth = 1;

    private int tabTextSize = 12;
    private int tabTextColor = 0xFF666666;
    private Typeface tabTypeface = null;
    private int tabTypefaceStyle = Typeface.BOLD;

    private int lastScrollX = 0;

    private Locale locale;
    private List<TextPaint> mTextPaints = new ArrayList<TextPaint>();
    // @formatter:off
    private static final int[] ATTRS = new int[]{android.R.attr.textSize, android.R.attr.textColor};

    public PagerSlidingTabStrip(Context context){
        this(context, null);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        tabsContainer = new RadioGroup(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new RadioGroup.LayoutParams(-1, -1));
        addView(tabsContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        scrollOffset = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
        indicatorHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
        underlineHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
        dividerPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
        tabPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
        dividerWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
        tabTextSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

        // get system attrs (android:textSize and android:textColor)

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
        tabTextColor = a.getColor(1, tabTextColor);

        a.recycle();

        // get custom attrs

        //		a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);

        //		indicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, indicatorColor);
        //		underlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsUnderlineColor, underlineColor);
        //		dividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor, dividerColor);
        //		indicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight, indicatorHeight);
        //		underlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight, underlineHeight);
        //		dividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerPadding, dividerPadding);
        //		tabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight, tabPadding);
        //		tabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground, tabBackgroundResId);
        //		shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand, shouldExpand);
        //		scrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsScrollOffset, scrollOffset);
        //		textAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, textAllCaps);

        //		a.recycle();

        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Style.FILL);

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(dividerWidth);

        defaultTabLayoutParams = new RadioGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        expandedTabLayoutParams = new RadioGroup.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

        if(locale == null) {
            locale = getResources().getConfiguration().locale;
        }
    }

    public void setViewPager(ViewPager pager){
        this.pager = pager;

        if(pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        pager.addOnPageChangeListener(pageListener);

        notifyDataSetChanged();
    }

    public void setOnPageChangeListener(OnPageChangeListener listener){
        this.delegatePageListener = listener;
    }

    public void notifyDataSetChanged(){

        tabsContainer.removeAllViews();
        mTabCount = pager.getAdapter().getCount();
        for(int i = 0; i<mTabCount; i++) {

            if(pager.getAdapter() instanceof IconTabProvider) {
                if(( (IconTabProvider)pager.getAdapter() ).getPageIconResIds(i) != null) {
                    addIconTab(i, pager.getAdapter().getPageTitle(i).toString(),
                            ( (IconTabProvider)pager.getAdapter() ).getPageIconResIds(i));
                }else {
                    addIconTab(i, pager.getAdapter().getPageTitle(i).toString(),
                            ( (IconTabProvider)pager.getAdapter() ).getPageIconResId(i));
                }
            }else {
                addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
            }
        }
        updateTabStyles();
        currentPosition = pager.getCurrentItem();
        check(currentPosition);
    }

    private void addTextTab(final int position, String title){
        addIconTab(position, title, 0);
    }

    private void addIconTab(final int position, String title, @NonNull @Size(min = 1) int... resId){
        if(TextUtils.isEmpty(title)) {
            Log.e(TAG, "title is null ");
            return;
        }
        CheckedTextView tab = new CheckedTextView(getContext());
        tab.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
        tab.setGravity(Gravity.CENTER);
        if(!mCapsuleStyle) {
            if(mTabMode == MODE_TOP) {
                if(resId.length>1) {
                    tab.setBackground(getListDrable(resId));
                }else {
                    tab.setBackgroundResource(resId[0]);
                }
            }else {
                tab.setCompoundDrawablePadding(0);
                tab.setSingleLine();
                if(resId.length>1) {
                    tab.setCompoundDrawablesWithIntrinsicBounds(null, getListDrable(resId), null, null);
                }else {
                    tab.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), resId[0]),
                            null, null);
                }
            }
        }
        tab.setText(title);
        addTab(position, tab);
        if(currentPosition == 0) {
            pageListener.onPageSelected(0);
        }
    }

    private StateListDrawable getListDrable(@NonNull @Size(min = 2) int... resId){
        StateListDrawable listDrawable = new StateListDrawable();
        listDrawable
                .addState(new int[]{android.R.attr.state_checked}, ContextCompat.getDrawable(getContext(), resId[0]));
        listDrawable
                .addState(new int[]{android.R.attr.state_pressed}, ContextCompat.getDrawable(getContext(), resId[0]));
        listDrawable.addState(new int[]{}, ContextCompat.getDrawable(getContext(), resId[1]));
        return listDrawable;
    }

    private void addTab(final int position, View tab){
        //        tab.setFocusable(true);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                pager.setCurrentItem(position);
            }
        });
        tab.setPadding(tabPadding, 0, tabPadding, 0);
        tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
    }

    private void updateTabStyles(){

        for(int i = 0; i<mTabCount; i++) {

            View v = tabsContainer.getChildAt(i);
            if(v instanceof TextView) {
                TextView tab = (TextView)v;
                tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
                tab.setTypeface(tabTypeface, tabTypefaceStyle);
                if(mTabTextColorStateList == null) {
                    tab.setTextColor(tabTextColor);
                }else {
                    tab.setTextColor(mTabTextColorStateList);
                }
                // setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
                // pre-ICS-build
                if(textAllCaps) {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        tab.setAllCaps(true);
                    }else {
                        tab.setText(tab.getText().toString().toUpperCase(locale));
                    }
                }
            }
        }

    }

    private void scrollToChild(int position, int offset){

        if(mTabCount == 0) {
            return;
        }
        int newScrollX = tabsContainer.getChildAt(position).getLeft()+offset;

        if(position>0 || offset>0) {
            newScrollX -= scrollOffset;
        }
        if(newScrollX != lastScrollX) {
            lastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        float pading = dp2dip(0.5f);
        mW = w-pading;
        mH = h-pading;
        if(mCapsuleStyle) {
            //            tabsContainer.measure(0,0);
            mW = tabsContainer.getMeasuredWidth()-pading;
            mClipRectf = new RectF(pading, pading, mW, mH);
            mClipath = new Path();
            mClipath.addRoundRect(mClipRectf, mH/2f, mH/2f, Path.Direction.CCW);
            mOutRadio = mH/2;
        }

        if(mTabMode == MODE_BUTTOM) {
            indicatorHeight = (int)mH;
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        //        if (isInEditMode() || mTabCount == 0 || mTabMode != MODE_TOP) {
        if(isInEditMode() || mTabCount == 0) {
            return;
        }

        // draw indicator line
        rectPaint.setColor(indicatorColor);

        // default: line below current tab
        View currentTab = tabsContainer.getChildAt(currentPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();

        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if(currentPositionOffset>0f && currentPosition<mTabCount-1) {

            View nextTab = tabsContainer.getChildAt(currentPosition+1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();
            if(mState == ViewPager.SCROLL_STATE_DRAGGING || mState == ViewPager.SCROLL_STATE_IDLE) {
                if(mLastCheckedPosition == currentPosition) {
                    mDragRight = true;
                    Log.d(TAG, "往右 ------>> ");
                }else {
                    mDragRight = false;
                    Log.d(TAG, "往左 <<------");
                }
            }
            //            lineLeft = ( currentPositionOffset*nextTabLeft+( 1f-currentPositionOffset )*lineLeft );
            //            lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
            if(mDragRight) {
                //                ------>>
                if(currentPositionOffset>=0.5) {
                    lineLeft = ( 2*( nextTabLeft-lineLeft )*currentPositionOffset+2*lineLeft-nextTabLeft );
                }
                lineRight = ( currentPositionOffset*nextTabRight+( 1f-currentPositionOffset )*lineRight );
            }else {
                //                <<------
                lineLeft = ( currentPositionOffset*nextTabLeft+( 1f-currentPositionOffset )*lineLeft );
                if(currentPositionOffset<=0.5) {
                    lineRight = ( 2*( nextTabRight-lineRight )*currentPositionOffset+lineRight );
                }else {
                    lineRight = nextTabRight;
                }
            }
        }

        if(mCapsuleStyle) {
            underlineColor = Color.TRANSPARENT;
            indicatorHeight = (int)mH;
            //画边框
            rectPaint.setStyle(Style.STROKE);
            rectPaint.setStrokeWidth(dp2dip(1));
            canvas.drawRoundRect(mClipRectf, mOutRadio, mOutRadio, rectPaint);
            canvas.save();
            canvas.clipPath(mClipath);
        }
        rectPaint.setStyle(Style.FILL);
        //画滚动 高亮的 指示线
        canvas.drawRect(lineLeft, mH-indicatorHeight, lineRight, mH, rectPaint);

        // draw underline 默认底线
        rectPaint.setColor(underlineColor);
        canvas.drawRect(0, mH-underlineHeight, tabsContainer.getWidth(), mH, rectPaint);
        //        canvas.restore();

        // draw divider
        dividerPaint.setColor(dividerColor);
        for(int i = 0; i<mTabCount-1; i++) {
            View tab = tabsContainer.getChildAt(i);
            canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), mH-dividerPadding, dividerPaint);
        }
    }

    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){

            currentPosition = position;
            currentPositionOffset = positionOffset;

            scrollToChild(position, (int)( positionOffset*tabsContainer.getChildAt(position).getWidth() ));

            invalidate();

            if(delegatePageListener != null) {
                delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state){
            mState = state;
            if(state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(pager.getCurrentItem(), 0);
            }

            if(delegatePageListener != null) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position){
            check(position);
            if(delegatePageListener != null) {
                delegatePageListener.onPageSelected(position);
            }
        }

    }

    private void check(int position){
        if(mLastCheckedPosition != -1) {
            ( (Checkable)tabsContainer.getChildAt(mLastCheckedPosition) ).setChecked(false);
        }
        mLastCheckedPosition = position;
        ( (Checkable)tabsContainer.getChildAt(position) ).setChecked(true);
    }

    public void setIndicatorColor(int indicatorColor){
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId){
        this.indicatorColor = ContextCompat.getColor(getContext(), resId);
        invalidate();
    }

    public int getIndicatorColor(){
        return this.indicatorColor;
    }

    public void setIndicatorHeight(int indicatorLineHeightPx){
        this.indicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public int getIndicatorHeight(){
        return indicatorHeight;
    }

    public void setUnderlineColor(int underlineColor){
        this.underlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineColorResource(int resId){
        this.underlineColor = getResources().getColor(resId);
        invalidate();
    }

    public int getUnderlineColor(){
        return underlineColor;
    }

    public void setDividerColor(int dividerColor){
        this.dividerColor = dividerColor;
        invalidate();
    }

    public void setDividerColorResource(int resId){
        this.dividerColor = getResources().getColor(resId);
        invalidate();
    }

    public int getDividerColor(){
        return dividerColor;
    }

    public void setUnderlineHeight(int underlineHeightPx){
        this.underlineHeight = underlineHeightPx;
        invalidate();
    }

    public int getUnderlineHeight(){
        return underlineHeight;
    }

    public void setDividerPadding(int dividerPaddingPx){
        this.dividerPadding = dividerPaddingPx;
        invalidate();
    }

    public int getDividerPadding(){
        return dividerPadding;
    }

    public void setScrollOffset(int scrollOffsetPx){
        this.scrollOffset = scrollOffsetPx;
        invalidate();
    }

    public int getScrollOffset(){
        return scrollOffset;
    }

    public void setShouldExpand(boolean shouldExpand){
        this.shouldExpand = shouldExpand;
        requestLayout();
    }

    public boolean getShouldExpand(){
        return shouldExpand;
    }

    public boolean isTextAllCaps(){
        return textAllCaps;
    }

    public void setAllCaps(boolean textAllCaps){
        this.textAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizePx){
        this.tabTextSize = textSizePx;
        updateTabStyles();
    }

    public int getTextSize(){
        return tabTextSize;
    }

    public void setTextColor(int textColor){
        this.tabTextColor = textColor;
        updateTabStyles();
    }

    public void setTextColorResource(int resId){
        this.tabTextColor = getResources().getColor(resId);
        updateTabStyles();
    }

    public void setTextColorStateResource(int resId){
        mTabTextColorStateList = ContextCompat.getColorStateList(getContext(), resId);
        updateTabStyles();
    }

    public int getTextColor(){
        return tabTextColor;
    }

    public void setTypeface(Typeface typeface, int style){
        this.tabTypeface = typeface;
        this.tabTypefaceStyle = style;
        updateTabStyles();
    }

    public void setTabBackground(int resId){
        this.tabBackgroundResId = resId;
    }

    public int getTabBackground(){
        return tabBackgroundResId;
    }

    public void setTabPaddingLeftRight(int paddingPx){
        this.tabPadding = paddingPx;
        updateTabStyles();
    }

    public int getTabPaddingLeftRight(){
        return tabPadding;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state){
        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState(){
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    public void setCapsuleStyle(boolean capsuleStyle){
        mCapsuleStyle = capsuleStyle;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState){
            super(superState);
        }

        private SavedState(Parcel in){
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags){
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in){
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size){
                return new SavedState[size];
            }
        };
    }

    public int getTabMode(){
        return mTabMode;
    }

    public void setTabMode(@TabMode int tabMode){
        mTabMode = tabMode;
        if(mTabMode == MODE_BUTTOM) {
            setPadding(0, 8, 0, 8);
        }
    }


    private float dp2dip(float dp){
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

}
