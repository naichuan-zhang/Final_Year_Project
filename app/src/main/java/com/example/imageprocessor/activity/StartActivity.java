package com.example.imageprocessor.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.imageprocessor.R;
import com.example.imageprocessor.adapter.StartPagerAdapter;
import com.example.imageprocessor.start.FragmentStartOne;
import com.example.imageprocessor.start.FragmentStartThree;
import com.example.imageprocessor.start.FragmentStartTwo;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private final static String TAG = "StartActivity: ";

    private ViewPager viewPager;
    private StartPagerAdapter pagerAdapter;
    private List<Fragment> fragments;
    private LinearLayout dotLayout;
    private ImageView[] dots;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        initView();
        initData();
        initDots();
        initListeners();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position < 0 || position > fragments.size() - 1
                || currentIndex == position) {
            return;
        }
        dots[position].setImageDrawable(null);
        dots[position].setImageResource(R.drawable.dot_focused);
        dots[currentIndex].setImageDrawable(null);
        dots[currentIndex].setImageResource(R.drawable.dot_normal);
        currentIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void initView() {
        viewPager = findViewById(R.id.start_view_pager);
        dotLayout = findViewById(R.id.dots_layout);
    }

    private void initData() {
        fragments = new ArrayList<>();
        fragments.add(FragmentStartOne.getInstance());
        fragments.add(FragmentStartTwo.getInstance());
        fragments.add(FragmentStartThree.getInstance());
        pagerAdapter = new StartPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(pagerAdapter);
    }

    private void initListeners() {
        viewPager.addOnPageChangeListener(this);
    }

    private void initDots() {
        dots = new ImageView[fragments.size()];
        for (int i = 0; i < fragments.size(); i++) {
            dots[i] = (ImageView) dotLayout.getChildAt(i);
            if (i != 0)
                dots[i].setImageResource(R.drawable.dot_normal);
        }
        currentIndex = 0;
        dots[currentIndex].setImageResource(R.drawable.dot_focused);
    }
}
