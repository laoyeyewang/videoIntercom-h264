package com.xair.h264demo.UDP;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UdpReceiver extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread() {
            @Override
            public void run() {
//                super.run();
                while (true) {
                    try {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        DatagramSocket dgSocket = null;
                        int port = 6801;
                        if (dgSocket == null) {
                            dgSocket = new DatagramSocket(null);
                            dgSocket.setReuseAddress(true);
                            dgSocket.bind(new InetSocketAddress(port));
                            dgSocket.setReceiveBufferSize(512*1024);
                        }
                       // byte[] by = new byte[307200];
                        byte[] by = new byte[260000];
                        DatagramPacket packet = new DatagramPacket(by, by.length);
                        System.out.print(by.length);
                        Log.i("tag", "run: "+by.length);
                       // dgSocket.setSoTimeout(500);
                        dgSocket.receive(packet);
                        int dataL = packet.getLength();
                        byte[] data = new  byte[dataL];

                        if (dataL <= 260000) {
                            System.arraycopy(by,0,data,0,dataL);
                            //Log.i("tag", "run: "+l);
                            // Log.i("tag", "run: "+ dgSocket.getReceiveBufferSize());
                            //发送广播
                            Intent intent = new Intent();
                            intent.putExtra("count", data);
                            intent.setAction("com.example.weiyuzk.UdpReceiver");
                            sendBroadcast(intent);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}



















