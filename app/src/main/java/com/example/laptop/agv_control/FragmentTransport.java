package com.example.laptop.agv_control;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentTransport extends Fragment {
    protected static Handler handler2;
    public FragmentTransport() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //把Fragment填充到ViewPager
        View view = inflater.inflate(R.layout.fragment_transport, container, false);
        Log.v("LALA","hehe");
        Log.e("HEHE", "第二个");
        return view;
    }
}
