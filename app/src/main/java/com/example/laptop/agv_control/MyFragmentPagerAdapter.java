package com.example.laptop.agv_control;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

public class MyFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private final int PAGER_COUNT = 3;
    private FragmentNormal myFragment1 = null;
    private FragmentTransport myFragment2 = null;
    private FragmentDebug myFragment3 = null;


    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        myFragment1 = new FragmentNormal();
        myFragment2 = new FragmentTransport();
        myFragment3 = new FragmentDebug();
    }


    @Override
    public int getCount() {
        return PAGER_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup vg, int position) {
        return super.instantiateItem(vg, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        System.out.println("position Destory" + position);
        //注释掉后切换界面后，就不会销毁其他页面,不然会出现断开串口连接情况
        //super.destroyItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case MainActivity.PAGE_ONE:
                fragment = myFragment1;
                break;
            case MainActivity.PAGE_TWO:
                fragment = myFragment2;
                break;
            case MainActivity.PAGE_THREE:
                fragment = myFragment3;
                break;
        }
        return fragment;
    }
}
