package com.jonas.acase;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import april.yun.ISlidingTabStrip;
import april.yun.other.JTabStyleDelegate;
import com.jonas.acase.fragment.SuperAwesomeCardFragment;

import static april.yun.other.JTabStyleBuilder.STYLE_DEFAULT;
import static april.yun.other.JTabStyleBuilder.STYLE_DOTS;
import static april.yun.other.JTabStyleBuilder.STYLE_ROUND;

public class MainActivity extends FragmentActivity {
    private ISlidingTabStrip tabs_up;
    private ISlidingTabStrip dots;
    private ISlidingTabStrip tabs_buttom;
    private ViewPager pager;
    private MyPagerAdapter adapter;
    private int[] mPressed;
    private int[] mNormal;
    private int[] mSelectors;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabs_up = (ISlidingTabStrip) findViewById(R.id.tabs);
        tabs_buttom = (ISlidingTabStrip) findViewById(R.id.tab_buttom);
        dots = (ISlidingTabStrip) findViewById(R.id.dots);

        setupTabStrips();

        setupViewpager();
    }


    private void setupTabStrips() {

        setupStrip(tabs_up.getTabStyleDelegate(), STYLE_ROUND);
        setupStrip(tabs_buttom.getTabStyleDelegate(), STYLE_DEFAULT);
        setupStrip(dots.getTabStyleDelegate(), STYLE_DOTS);
        tabs_buttom.getTabStyleDelegate().setTabIconGravity(Gravity.TOP);
        tabs_buttom.getTabStyleDelegate().setIndicatorHeight(0);
        tabs_buttom.getTabStyleDelegate().setDividerColor(Color.TRANSPARENT);
        tabs_up.getTabStyleDelegate().setNotDrawIcon(true);
    }


    private void setupStrip(JTabStyleDelegate tabStyleDelegate, int type) {

        tabStyleDelegate.setJTabStyle(type);
        tabStyleDelegate.setShouldExpand(true);
        tabStyleDelegate.setFrameColor(Color.parseColor("#FACDB9"));
        tabStyleDelegate.setTabTextSize(getDimen(R.dimen.tabstrip_textsize));
        //        tabStyleDelegate.setTextColor(Color.parseColor("#FB6522"));
        tabStyleDelegate.setTextColorStateResource(getApplicationContext(), R.drawable.tabstripbg);
        tabStyleDelegate.setDividerColor(Color.parseColor("#FACDB9"));
        tabStyleDelegate.setDividerPadding(0);
        tabStyleDelegate.setUnderlineColor(Color.TRANSPARENT);
        tabStyleDelegate.setIndicatorColor(Color.parseColor("#FACDB9"));
        tabStyleDelegate.setIndicatorHeight(getDimen(R.dimen.sug_event_tabheight));
    }


    private void setupViewpager() {
        mNormal = new int[] { R.drawable.ic_tab_msg, R.drawable.ic_tab_contact, R.drawable.ic_tab_moments,
                R.drawable.ic_tab_profile };
        mPressed = new int[] { R.drawable.ic_tab_msg_h, R.drawable.ic_tab_contact_h,
                R.drawable.ic_tab_moments_h, R.drawable.ic_tab_profile_h };
        mSelectors = new int[] { R.drawable.tab1, R.drawable.tab2, R.drawable.tab3, R.drawable.tab4 };

        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                getResources().getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs_up.setViewPager(pager);
        tabs_buttom.setViewPager(pager);
        dots.setViewPager(pager);

    }


    public class MyPagerAdapter extends FragmentPagerAdapter implements ISlidingTabStrip.IconTabProvider {

        private final String[] TITLES = { "微信", "通讯录", "发现", "我" };


        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override public CharSequence getPageTitle(int position) {
            return TITLES[position % 4];
        }


        @Override public int getCount() {
            return TITLES.length;
        }


        @Override public Fragment getItem(int position) {
            return SuperAwesomeCardFragment.newInstance(position);
        }


        @Override public int[] getPageIconResIds(int position) {
            //return new int[]{mNormal[position%4],mPressed[position%4]};
            return null;
        }


        @Override public int getPageIconResId(int position) {
            //		return mPressed[position];
            return mSelectors[position % 4];
        }
    }

    private int getDimen(int dimen) {
        return (int) getResources().getDimension(dimen);
    }
}
