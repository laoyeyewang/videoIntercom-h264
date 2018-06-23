package com.xair.h264demo.TCP;

import android.os.Message;
import android.util.Log;

import com.xair.h264demo.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import static android.os.SystemClock.sleep;

public class TCPSend {
    //tag 标记
    protected static final String TAG = MainActivity.class.getSimpleName();

    // Socket变量
    private static Socket socket;
    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    public static ExecutorService mThreadPool;
    // 输出流对象
    static OutputStream outputStream;
    // 输入流对象
    static InputStream is;
    // 输入流读取器对象
    static InputStreamReader isr;
    static BufferedReader br;

    public static String response;

    private static String ip = "192.168.2.199";

    /*
     * 连接服务
     *
     * */
    public static void connectThread() {
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建Socket对象 & 指定服务端的IP 及 端口号
                    socket = new Socket(ip, 8101);

                    // 判断客户端和服务器是否连接成功
                    System.out.println(socket.isConnected());
                    if (socket.isConnected() == true) {
                        // Toast.makeText(MainActivity.this, "连接成功 ", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "run: 连接成功");



                        receviedMsgThread();
                    } else {
                        Log.i(TAG, "run: 连接失败");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /*
     * 发送消息
     * */
    public static void sendMsgThread(final byte[] msg) {
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket != null && socket.isConnected()) {
                        // 步骤1：从Socket 获得输出流对象OutputStream
                        // 该对象作用：发送数据
                        outputStream = socket.getOutputStream();
                        // 步骤2：写入需要发送的数据到输出流对象中
                        outputStream.write(msg);
                        // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                        // 步骤3：发送数据到服务端
                        outputStream.flush();
                        Log.i(TAG, "run: 发送成功---------------------+++++++++++++++++++++++++++++++===============================" + msg);
                        // Toast.makeText(MainActivity.this,"发送成功 ",Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
     * 接收消息
     * */
    private static void receviedMsgThread() {
        byte[] b = new byte[4144];
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    sleep(100);
                    try {
                        if (socket.isConnected() == true && socket != null) {
                            // 步骤1：创建输入流对象InputStream
                            //shu ru
                            is = socket.getInputStream();

                            // 步骤2：创建输入流读取器对象 并传入输入流对象
                            // 该对象作用：获取服务器返回的数据
                            isr = new InputStreamReader(is);
                            br = new BufferedReader(isr);
//                            Log.i(TAG, "br ------------------------------------"+br);
                            // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据
                            response = br.readLine();
                             Log.i(TAG, "tcpsend*****************************: " + response);
                            if (response != null) {
                                //Message msg = Message.obtain();
                                //msg.what = 0;
                                //mMainHandler.sendMessage(msg);
                                disconnectThread();
                                Log.i(TAG, "tcpsend*****************************: " + "断开连接");
                            }
                        }
                        //   Toast.makeText(MainActivity.this, "发送成功 " + response, Toast.LENGTH_LONG).show();
                        // 步骤4:通知主线程,将接收的消息显示到界面
                    } catch (IOException e) {
                        e.printStackTrace();
                        disconnectThread();
                    }
                }
            }
        });
    }


    /*
     * 断开链接
     * */
    private static void disconnectThread() {
        try {
            // 断开 客户端发送到服务器 的连接，即关闭输出流对象OutputStream
            outputStream.close();

            // 断开 服务器发送到客户端 的连接，即关闭输入流读取器对象BufferedReader
            br.close();

            // 最终关闭整个Socket连接
            socket.close();

            // 判断客户端和服务器是否已经断开连接
            System.out.println(socket.isConnected());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
