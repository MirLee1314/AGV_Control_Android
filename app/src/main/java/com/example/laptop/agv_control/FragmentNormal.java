package com.example.laptop.agv_control;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentNormal extends Fragment {
    private Context mContext;
    final MainActivity main = new MainActivity();
    final FragmentDebug debug = new FragmentDebug();

    protected boolean isOpen;
    protected boolean isConfig = false;
    protected static Handler handler;
    protected static byte handlerFlag =0;
    /**
     * 按钮变量
     * */
    protected Button openButton,configButton;
    protected Button startButton,endButton,powerButton;
    protected Button forwardButton,turnButton,stopButton,turnLeftButton,turnRightButton;
    protected Button debugButton;
    protected TextView debugTextViewText;
    protected Spinner curPlace,speedSelect;
    /**
     * 用来判断状态的变量
     * */
    protected int nCurPlace=1;
    protected byte[] turnCommandBufWorking,forwardCommandBufWorking;
    protected long startTime = 0;
    protected byte stopLineNum = 0;
    /**
     * 按键按下标志
     **/
    protected boolean debugButtonFlag = false;
    protected boolean startButtonFlag = false;
    protected boolean endButtonFlag = false;
    /**
     *AGV控制命令常量和变量
     **/
    protected String recvBuf = "";
    protected byte[] sendBuf;
    protected String speedCommand;
    protected final String STOP_LINE_COMMAND = "57 ";
    protected final String STATUS_QUERY = "50 ";
    protected final String ELECTIC_QUANTITY_OK = "51 ";
    protected final String FORWARD_COMMAND = "01 ";
    protected final String TURN_BACK_COMMAND = "02 ";
    protected final String STOP_COMMAND = "03 ";
    protected final String TURN_RIGHT_COMMAND = "05 ";
    protected final String TURN_LEFT_COMMAND = "04 ";
    /**
     * 串口配置参数变量
     * */
    private int speed; //速度
    private int baudRate;//波特率
    private byte stopBit;//停止位
    private byte dataBit;//数据位
    private byte parity;//奇偶校验位
    private byte flowControl;//停止位

    public FragmentNormal() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //把Fragment填充到ViewPager
         View view = inflater.inflate(R.layout.fragment_normal, container, false);

        configButton = view.findViewById(R.id.Config);
        openButton = view.findViewById(R.id.open_device);
        startButton = view.findViewById(R.id.PlaceOne);
        endButton = view.findViewById(R.id.PlaceTwo);
        powerButton = view.findViewById(R.id.PlaceThree);
        forwardButton = view.findViewById(R.id.Forward);
        turnButton = view.findViewById(R.id.turn);
        stopButton = view.findViewById(R.id.stop);
        turnLeftButton = view.findViewById(R.id.turnLeft);
        turnRightButton = view.findViewById(R.id.turnRight);
        debugButton = view.findViewById(R.id.debug);
        debugTextViewText = view.findViewById(R.id.debugTextView);
        //下拉列表
        curPlace = view.findViewById(R.id.curPlaceSelect);
        speedSelect = view.findViewById(R.id.speedSelect);

        if (!MyApp.driver.UsbFeatureSupported())// 判断系统是否支持USB HOST
        {
            Dialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle("提示")
                    .setMessage("您的手机不支持USB HOST，请更换其他手机再试！")
                    .setPositiveButton("确认",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    System.exit(0);
                                }
                            }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        /**
         * 打开串口前，把相关按钮设置为不可选状态
         * */
        isOpen = false;
        configButton.setEnabled(false);
        startButton.setEnabled(false);
        endButton.setEnabled(false);
        powerButton.setEnabled(false);
        forwardButton.setEnabled(false);
        turnButton.setEnabled(false);
        stopButton.setEnabled(false);
        turnRightButton.setEnabled(false);
        turnLeftButton.setEnabled(false);
        handler();
        initButtonListener();
        initValue();

        Log.v("LALA","hehe");
        Log.e("HEHE", "第一个");
        return view;
    }
    protected void handler()
    {
        /**
         * 接收数据
         * */
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //debugTextViewText.apend("");//在之前的内容后面追加内容
                recvBuf = "";
                recvBuf += (String) msg.obj;
                debugTextViewText.setText(recvBuf);
                if(debugButtonFlag)
                {
                    Toast.makeText(mContext," 收到", Toast.LENGTH_SHORT).show();
                    debugButtonFlag = false;
                }
                /**
                 * 点1到3运行到点4
                 * */
                else if(startButtonFlag && recvBuf.equals(STOP_LINE_COMMAND))
                {
                    stopLineNum++;
                    if(nCurPlace == 1)
                    {
                        if(stopLineNum <= 8) {
                            //延时
                            startTime = System.currentTimeMillis();
                            while ((System.currentTimeMillis() - startTime) <= 5000) ;
                            //经过中间位置后后，发送前进
                            forwardCommandBufWorking = main.toByteArray(FORWARD_COMMAND);
                            MyApp.driver.WriteData(forwardCommandBufWorking, forwardCommandBufWorking.length);
                        }
                        else
                        {
                            startButtonFlag = false;
                            stopLineNum = 0;
                            nCurPlace = 4;
                            Toast.makeText(mContext,"到达位置四", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(nCurPlace == 2)
                    {
                        if(stopLineNum <=5)
                        {
                            //延时
                            startTime = System.currentTimeMillis();
                            while ((System.currentTimeMillis() - startTime) <= 5000) ;
                            //经过中间位置后后，发送前进
                            forwardCommandBufWorking = main.toByteArray(FORWARD_COMMAND);
                            MyApp.driver.WriteData(forwardCommandBufWorking, forwardCommandBufWorking.length);
                        }
                        else
                        {
                            startButtonFlag = false;
                            stopLineNum = 0;
                            nCurPlace = 4;
                            Toast.makeText(mContext,"到达位置四", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(nCurPlace == 3)
                    {
                        if(stopLineNum <=5)
                        {
                            //延时
                            startTime = System.currentTimeMillis();
                            while ((System.currentTimeMillis() - startTime) <= 5000) ;
                            //经过中间位置后后，发送前进
                            forwardCommandBufWorking = main.toByteArray(FORWARD_COMMAND);
                            MyApp.driver.WriteData(forwardCommandBufWorking, forwardCommandBufWorking.length);
                        }
                        else
                        {
                            startButtonFlag = false;
                            stopLineNum = 0;
                            nCurPlace = 4;
                            Toast.makeText(mContext,"到达位置四", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                /**
                 * 点四运行到点一
                 * 其他运行过程类似
                 * */
                else if(endButtonFlag && recvBuf.equals(STOP_LINE_COMMAND))
                {
                    /**
                     * 中途会遇到八个停止点，需要计数
                     * */
                    stopLineNum++;
                    if(stopLineNum <=8)
                    {
                        //延时
                        startTime = System.currentTimeMillis();
                        while ((System.currentTimeMillis() - startTime) <= 5000) ;
                        //经过中间位置后后，发送前进
                        forwardCommandBufWorking = main.toByteArray(FORWARD_COMMAND);
                        MyApp.driver.WriteData(forwardCommandBufWorking, forwardCommandBufWorking.length);
                    }
                    else
                    {
                        endButtonFlag = false;
                        stopLineNum = 0;
                        Toast.makeText(mContext,"到达位置一", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    protected void initButtonListener()
    {
        /**
         * 测试用的按钮
         * 以及计划实现发送和接受界面
         * 测试返回多个指令会怎样？
         * */
        debugButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendBuf = main.toByteArray("50 ");
                MyApp.driver.WriteData(sendBuf,sendBuf.length);
                debugButtonFlag = true;
            }
        });

        //打开流程主要步骤为ResumeUsbList，UartInit
        openButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!isOpen) {
                    if (!MyApp.driver.ResumeUsbList())// ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
                    {
                        Toast.makeText(mContext, "打开设备失败!",
                                Toast.LENGTH_SHORT).show();
                        MyApp.driver.CloseDevice();
                    } else {
                        if (!MyApp.driver.UartInit()) {//对串口设备进行初始化操作
                            Toast.makeText(mContext, "设备初始化失败!",
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(mContext, "打开" +
                                            "设备失败!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(mContext, "打开设备成功!",
                                Toast.LENGTH_SHORT).show();
                        isOpen = true;
                        openButton.setText("Close");
                        configButton.setEnabled(true);

                        new FragmentNormal.readThread().start();//开启读线程读取串口接收的数据
                    }
                } else {
                    MyApp.driver.CloseDevice();
                    openButton.setText("Open");
                    isOpen = false;
                    configButton.setEnabled(false);
                    startButton.setEnabled(false);
                    endButton.setEnabled(false);
                    powerButton.setEnabled(false);
                    forwardButton.setEnabled(false);
                    turnButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    turnRightButton.setEnabled(false);
                    turnLeftButton.setEnabled(false);
                }
            }
        });

        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (MyApp.driver.SetConfig(baudRate, dataBit, stopBit, parity,//配置串口波特率，函数说明可参照编程手册
                        flowControl)) {
                    Toast.makeText(mContext, "串口设置成功!",
                            Toast.LENGTH_SHORT).show();

                    //debugButton.setEnabled(true);
                    forwardButton.setEnabled(true);
                    turnButton.setEnabled(true);
                    stopButton.setEnabled(true);
                    turnLeftButton.setEnabled(true);
                    turnRightButton.setEnabled(true);
                    startButton.setEnabled(true);
                    isConfig = true;

                } else {
                    Toast.makeText(mContext, "串口设置失败!",
                            Toast.LENGTH_SHORT).show();
                    isConfig = false;
                }
            }
        });
        /**
         * 在轨前进按钮
         * */
        forwardButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                byte[] forwardCommandBuf = main.toByteArray(FORWARD_COMMAND);
                int retval = MyApp.driver.WriteData(forwardCommandBuf, forwardCommandBuf.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                if (retval < 0)
                    Toast.makeText(mContext, "前进失败!",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "前进成功!",
                            Toast.LENGTH_SHORT).show();
            }
        });
        /**
         * 在轨调头按钮
         * */
        turnButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                byte[] turnCommandBuf = main.toByteArray(TURN_BACK_COMMAND);
                int retval = MyApp.driver.WriteData(turnCommandBuf, turnCommandBuf.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                if (retval < 0)
                    Toast.makeText(mContext, "调头失败!",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "调头成功!",
                            Toast.LENGTH_SHORT).show();
            }
        });
        /**
         * 在轨停止按钮
         * */
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                byte[] stopCommandBuf = main.toByteArray(STOP_COMMAND);
                int retval = MyApp.driver.WriteData(stopCommandBuf,stopCommandBuf.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                if (retval < 0)
                    Toast.makeText(mContext, "停止失败!",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "停止成功!",
                            Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * 在轨进入左岔路按钮
         * */
        turnLeftButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                byte[] turnLeftCommandBuf = main.toByteArray(TURN_LEFT_COMMAND);
                int retval = MyApp.driver.WriteData(turnLeftCommandBuf,turnLeftCommandBuf.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                if (retval < 0)
                    Toast.makeText(mContext, "发送失败!",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "即将左拐!",
                            Toast.LENGTH_SHORT).show();
            }
        });
        /**
         * 在轨进入右岔路按钮
         * */
        turnRightButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                byte[] turnRightCommandBuf = main.toByteArray(TURN_RIGHT_COMMAND);
                int retval = MyApp.driver.WriteData(turnRightCommandBuf,turnRightCommandBuf.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                if (retval < 0)
                    Toast.makeText(mContext, "发送失败!",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "即将右拐!",
                            Toast.LENGTH_SHORT).show();
            }
        });
        /**
         * 功能：从点一到点三其中一点运行到点四
         * 配合选择初始位置,使用一个按钮完成三个动作
         * */
        startButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    startButtonFlag = true;
                    //调头命令
                    turnCommandBufWorking = main.toByteArray(TURN_BACK_COMMAND);
                    MyApp.driver.WriteData(turnCommandBufWorking, turnCommandBufWorking.length);
                    //延时
                    startTime = System.currentTimeMillis();
                    while ((System.currentTimeMillis() - startTime) <= 5000) ;
                    //前进命令
                    forwardCommandBufWorking = main.toByteArray(FORWARD_COMMAND);
                    MyApp.driver.WriteData(forwardCommandBufWorking, forwardCommandBufWorking.length);
            }
        });

        /**
         * 功能：从点四运行到点一
         * 不用选择位置，小车的位置需要在点四
         * */
        endButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //调头命令
                turnCommandBufWorking = main.toByteArray(TURN_BACK_COMMAND);
                MyApp.driver.WriteData(turnCommandBufWorking, turnCommandBufWorking.length);
                //延时
                startTime = System.currentTimeMillis();
                while ((System.currentTimeMillis() - startTime) <= 5000) ;
                //前进命令
                forwardCommandBufWorking = main.toByteArray(FORWARD_COMMAND);
                MyApp.driver.WriteData(forwardCommandBufWorking, forwardCommandBufWorking.length);
                //下面的操作需要不断监听停止线，放在handler中
                endButtonFlag = true;
            }
        });
    }
/**
 * 初始化串口参数，初始化下拉列表
 * */
     protected void initValue()
    {
        /* by default it is 9600 */
        baudRate = 9600;
        /* stop bits */
        /* default is stop bit 1 */
        stopBit = 1;
        /* data bits */
        /* default data bit is 8 bit */
        dataBit = 8;
        /* parity */
        /* default is none */
        parity = 0;
        /* flow control */
        /* default flow control is is none */
        flowControl = 0;
        //注册下列表
        ArrayAdapter<CharSequence> curPlaceAdapter = ArrayAdapter
                .createFromResource(mContext, R.array.curPlace_data,
                        R.layout.my_spinner_textview);
        curPlaceAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
        curPlace.setAdapter(curPlaceAdapter);
        curPlace.setGravity(0x10);
        curPlace.setSelection(0);
        nCurPlace = 1;
        /* set the adapter listeners for speed */
        curPlace.setOnItemSelectedListener(new MyPlaceSelectListener());

        ArrayAdapter<CharSequence> speedAdapter = ArrayAdapter
                .createFromResource(mContext, R.array.speed_data,
                        R.layout.my_spinner_textview);
        speedAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
        speedSelect.setAdapter(speedAdapter);
        speedSelect.setGravity(0x10);
        speedSelect.setSelection(4);
        /* by default it is 5 */
        speed = 5;
        /* set the adapter listeners for speed */
        speedSelect.setOnItemSelectedListener(new MySpeedSelectListener());
    }

    //下拉位置列表监听
    public class MyPlaceSelectListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            nCurPlace = Integer.parseInt(parent.getItemAtPosition(position)
                    .toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
    //下拉速度列表监听
    public class MySpeedSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            speed = Integer.parseInt(parent.getItemAtPosition(position)
                    .toString());

            switch (speed)
            {
                case 1:
                    speedCommand = "41 ";
                    break;
                case 2:
                    speedCommand = "42 ";
                    break;
                case 3:
                    speedCommand = "43 ";
                    break;
                case 4:
                    speedCommand = "44 ";
                    break;
                case 5:
                    speedCommand = "45 ";
                    break;
                case 6:
                    speedCommand = "46 ";
                    break;
            }
            /** 速度初始化
             * */
            if(isConfig){
                byte[] speedCommandBuf = main.toByteArray(speedCommand);
                int retval = MyApp.driver.WriteData(speedCommandBuf, speedCommandBuf.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                if (retval < 0)
                    Toast.makeText(mContext, "速度初始失败!",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "速度初始成功!",
                            Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class readThread extends Thread {

        public void run() {
            byte[] buffer = new byte[64];
            while (true) {
                Message msg = Message.obtain();
                if (!isOpen) {
                    break;
                }
                int length = MyApp.driver.ReadData(buffer, 64);
                if (length > 0) {
                    String recv = main.toHexString(buffer, length);
                    //String recv = buffer.toString();
                    msg.obj = recv;
                    /**
                     * 切换不同Fragment时，发送不同handler
                     * */
                    if(handlerFlag == 0){
                        handler.sendMessage(msg);
                    }
                    else if(handlerFlag == 1){

                    }
                    else if(handlerFlag == 2)
                    {
                        FragmentDebug.handler1.sendMessage(msg);
                    }
                }
            }
        }
    }
}
