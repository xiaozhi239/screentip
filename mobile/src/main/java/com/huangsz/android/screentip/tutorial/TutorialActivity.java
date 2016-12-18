package com.huangsz.android.screentip.tutorial;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.config.WatchFaceConfigActivity;
import com.huangsz.android.screentip.data.persist.SettingPersistenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * When first time uses this app.
 */
public class TutorialActivity extends Activity {

    private ViewPager mViewPager;

    private ViewGroup mDotsViewGroup;

    private List<View> mViewPageItems;

    private List<ImageView> mNavigationDots;

    private Button mStartButton;

    private SettingPersistenceManager mSettingPersistManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        mSettingPersistManager = new SettingPersistenceManager(this);
        // TODO(huangsz) Simplify this, there is no need to launch this activity at all.
        if (mSettingPersistManager.isTutorialPresented()) {
            startConfigActivity();
        }
        mViewPager = (ViewPager) findViewById(R.id.tutorial_viewpager);
        mDotsViewGroup = (ViewGroup) findViewById(R.id.tutorial_dots_viewgroup);
        setUpViewPageItems();
        setUpNavigationDots(mViewPageItems.size());
        mViewPager.setAdapter(new ViewPagerAdapter());
        mViewPager.setOnPageChangeListener(new ViewPageChangeListener());
    }

    private void setUpNavigationDots(int size) {
        mNavigationDots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(25,25));
            imageView.setPadding(10, 0, 10, 0);
            imageView.setBackgroundResource(R.drawable.icon_dot_black);
            if (i != 0) {
                imageView.setVisibility(View.INVISIBLE);
            }
            mNavigationDots.add(imageView);
            mDotsViewGroup.addView(imageView);
        }
    }

    private void setUpViewPageItems() {
        mViewPageItems = new ArrayList<>();
        LayoutInflater inflater = getLayoutInflater();
        mViewPageItems.add(inflater.inflate(R.layout.view_tutorial_page1_intro, null));

        View view = inflater.inflate(R.layout.view_tutorial_page2_setup, null);
        mViewPageItems.add(view);
        mStartButton = (Button) view.findViewById(R.id.tutorial_start_btn);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartButtonPressed();
            }
        });
    }

    private void onStartButtonPressed() {
        mSettingPersistManager.setTutorialPresented(true);
        startConfigActivity();
    }

    private void startConfigActivity() {
        Intent intent = new Intent(TutorialActivity.this, WatchFaceConfigActivity.class);
        startActivity(intent);
        finish();
    }

    // http://www.apkbus.com/android-25078-1-1.html
    private class ViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mViewPageItems.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewPageItems.get(position));
            return mViewPageItems.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewPageItems.get(position));
        }
    }

    private class ViewPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mNavigationDots.get(position)
                    .setVisibility(View.VISIBLE);
            for (int i = 0; i < mNavigationDots.size(); i++) {
                if (position != i) {
                    mNavigationDots.get(i).setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
