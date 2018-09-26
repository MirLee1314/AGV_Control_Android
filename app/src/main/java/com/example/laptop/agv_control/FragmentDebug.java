package com.example.laptop.agv_control;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.example.laptop.agv_control.FragmentNormal.handler;

public class FragmentDebug extends Fragment{
    MainActivity mainD = new MainActivity();
    protected Button debugSendButton,debugClearButton;
    protected EditText recvText,sendText;
    protected static Handler handler1;
    protected boolean isDebug = false;
    protected String recvDebugBuf;
    Context dContext;

    public FragmentDebug() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.dContext = getActivity();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //把Fragment填充到ViewPager
        View view = inflater.inflate(R.layout.fragment_debug, container, false);
        recvText = view.findViewById(R.id.readValues);
        sendText = view.findViewById(R.id.writeValues);
        debugClearButton = view.findViewById(R.id.clearButton);
        debugSendButton = view.findViewById(R.id.sendButton);

        handler1 = new Handler() {
            public void handleMessage(Message msg) {
                recvDebugBuf = "";
                recvDebugBuf += (String) msg.obj;
                recvText.setText(recvDebugBuf);
            }
        };
        debugSendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                byte[] sendValues = mainD.toByteArray(sendText.getText().toString());
                int retval = MyApp.driver.WriteData(sendValues, sendValues.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                if (retval < 0)
                    Toast.makeText(dContext, "写失败!",
                            Toast.LENGTH_SHORT).show();
            }
        });
        debugClearButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                recvText.setText("");
            }
        });
        Log.v("LALA","hehe");
        Log.e("HEHE", "第三个");
        return view;
    }
}
