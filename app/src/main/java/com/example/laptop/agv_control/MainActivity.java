package com.example.laptop.agv_control;


import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * author:MirLee
 * date:2018-9-21
 * program thinking：
 * 第一部分：
 *      包含基本的在轨移动指令，和四个点之间的移动（可以简化，没必要实现全部点的移动，第四个点为待机点
 *      还没有包含充电位置点）。
 * 第二部分：
 *      搬运货物模式，可以简化为从一个待机点运行到另外三个需求点再返回这一个过程（三个需求点为虚拟的，不用标出）
 *      搬货模式现有硬件条件无法满足，待后续加板子，确定通讯协议。
 * 第三部分：
 *      调试模式，包含一个发送和接受界面，用于手动发送一些不常用的命令
 ******************************************************************************************************
 * explain:目前第一部分和第三部分完成，第二部分待硬件部分配合完成;
 *          注意其中Power按钮没有实现，这个是用来走到充电位置的，但目前没有确定充电位置;
 * */
public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,
        ViewPager.OnPageChangeListener{
    //UI Objects
    //private TextView txt_topbar;
    private RadioGroup rg_tab_bar;
    private RadioButton rb_normal;
    private RadioButton rb_transport;
    private RadioButton rb_debug;
    private ViewPager vpager;

    private MyFragmentPagerAdapter mAdapter;
    //几个代表页面的常量
    public static final int PAGE_ONE = 0;
    public static final int PAGE_TWO = 1;
    public static final int PAGE_THREE = 2;

    public static final String TAG = "cn.wch.wchusbdriver";
    protected static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        bindViews();
        rb_normal.setChecked(true);

        MyApp.driver = new CH34xUARTDriver(
                (UsbManager)getSystemService(Context.USB_SERVICE), this,
                ACTION_USB_PERMISSION);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
    }

    /**
     * 将byte[]数组转化为String类型
     * @param arg
     *            需要转换的byte[]数组
     * @param length
     *            需要转换的数组长度
     * @return 转换后的String队形
     */
    public String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }

    /**
     * 将String转化为byte[]数组
     * @param arg
     *            需要转换的String对象
     * @return 转换后的byte[]数组
     */
    public byte[] toByteArray(String arg) {
        if (arg != null) {
            /* 1.先去除String中的' '，然后将String转换为char数组 */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            /* 将char数组中的值转成一个实际的十进制数组 */
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                /* 将 每个char的值每两个组成一个16进制数据 */
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[] {};
    }



    private void bindViews() {
        //txt_topbar = (TextView) findViewById(R.id.txt_topbar);
        rg_tab_bar = findViewById(R.id.rg_tab_bar);
        rb_normal =  findViewById(R.id.rb_normal);
        rb_transport =  findViewById(R.id.rb_transport);
        rb_debug = findViewById(R.id.rb_debug);
        rg_tab_bar.setOnCheckedChangeListener(this);

        vpager =  findViewById(R.id.vpager);
        vpager.setAdapter(mAdapter);
        vpager.setCurrentItem(0);
        vpager.addOnPageChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_normal:
                vpager.setCurrentItem(PAGE_ONE);
                break;
            case R.id.rb_transport:
                vpager.setCurrentItem(PAGE_TWO);
                break;
            case R.id.rb_debug:
                vpager.setCurrentItem(PAGE_THREE);
                break;
        }
    }


    //重写ViewPager页面切换的处理方法
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //state的状态有三个，0表示什么都没做，1正在滑动，2滑动完毕
        if (state == 2) {
            switch (vpager.getCurrentItem()) {
                case PAGE_ONE:
                    rb_normal.setChecked(true);
                    FragmentNormal.handlerFlag = 0;
                    break;
                case PAGE_TWO:
                    rb_transport.setChecked(true);
                    FragmentNormal.handlerFlag = 1;
                    break;
                case PAGE_THREE:
                    rb_debug.setChecked(true);
                    FragmentNormal.handlerFlag = 2;
                    break;
            }
        }
    }
}
