package com.xair.h264demo.TCP;

import android.content.Intent;
import android.util.Log;

import com.xair.h264demo.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements Runnable {

    private String TAG = "TcpServer";
//    MainActivity mainActivity;
    private int port = 6803;
    private boolean isListen = true;   //线程监听标志位
    int maxBufferSize = 1024*800;
    private byte[] soure = new byte[maxBufferSize];
    int cutposition = 0;



    public ArrayList<ServerSocketThread> SST = new ArrayList<ServerSocketThread>();

    public TCPServer(int port){

        this.port = port;

    }

    //更改监听标志位
    public void setIsListen(boolean b){
        isListen = b;
    }

    public void closeSelf(){
        isListen = false;
        for (ServerSocketThread s : SST){
            s.isRun = false;
        }
        SST.clear();
    }

    private Socket getSocket(ServerSocket serverSocket){
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "run: ----------------------------------------监听超时");
            return null;
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(15000);
            while (isListen){
                Log.i(TAG, "run: 开始监听...");

                Socket socket = getSocket(serverSocket);
                if (socket != null){
                    new ServerSocketThread(socket);
                }
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ServerSocketThread extends Thread{
        Socket socket = null;
        private PrintWriter pw;
        private InputStream is = null;
        private OutputStream os = null;
        private String ip = null;
        private boolean isRun = true;

        ServerSocketThread(Socket socket){
            this.socket = socket;
            ip = socket.getInetAddress().toString();
            Log.i(TAG, "ServerSocketThread:检测到新的客户端联入,ip:" + ip);

            try {
                socket.setSoTimeout(5000);
                os = socket.getOutputStream();
                is = socket.getInputStream();
                pw = new PrintWriter(os,true);
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void send(String msg){
            pw.println(msg);
            pw.flush(); //强制送出数据
        }

        @Override
        public void run() {
            byte buff[]  = new byte[1024*64];
            //String rcvMsg;
            int rcvLen;
            SST.add(this);
            while (isRun && !socket.isClosed() && !socket.isInputShutdown()){
                try {
                    sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if ((rcvLen = is.read(buff)) != -1){
                        Log.i(TAG, "run: ++++++++++++++++" + rcvLen);
                        byte[] data = new byte[rcvLen];
                        System.arraycopy(buff,0,data,0,rcvLen);

                        if ((cutposition + rcvLen) > maxBufferSize)
                            cutposition = 0;
                        System.arraycopy(data, 0, soure, cutposition, rcvLen);
                        cutposition += rcvLen;
                        ///////////////////////////////////
                        int nlastSorftLeng = 0;
                        for (int i = 0; i < cutposition; i++) {
                            int dataLeng = checkHeadLeng(soure, i);
                            if (dataLeng > 0 && (dataLeng + i) <= maxBufferSize){
                                byte[] buffer = new byte[dataLeng+116];
                                System.arraycopy(soure,i,buffer,0,dataLeng + 116);
                                nlastSorftLeng = i + 116 + dataLeng;
                                i += (dataLeng + 116);
                               // MainActivity.bDecFinished = false;
                                outputDecode(buffer);
                                MainActivity.mediaCodecEx.InputDataToDecoder(buffer,dataLeng + 116);
                                Log.i(TAG, "outputDecode:++++++++++++++++ "+buffer.length);

                            }

                        }
                        if (cutposition-nlastSorftLeng > 0) {
                            byte[] newData = new byte[cutposition - nlastSorftLeng];
                            System.arraycopy(soure, nlastSorftLeng, newData, 0, cutposition - nlastSorftLeng);
                            System.arraycopy(newData, 0, soure, 0, cutposition - nlastSorftLeng);
                            cutposition -= nlastSorftLeng;
                        }else {
                            cutposition = 0;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                socket.close();
                SST.clear();
                Log.i(TAG, "run: 断开连接");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void outputDecode(byte[] b){
        Log.i(TAG, "outputDecode: "+b.length);
        Intent intent =new Intent();
        intent.setAction("tcpServerReceiver");
        intent.putExtra("tcpServerReceiver",b);
        MainActivity.context.sendBroadcast(intent);//将消息发送给主界面
    }


    /**
     * Check if is H264 frame head
     *
     * @param buffer
     * @param offset
     * @return whether the src buffer is frame head
     */
    static int checkHeadLeng(byte[] buffer, int offset) {
        // 00 00 00 01
        if (buffer[offset] == 0x74 && buffer[offset + 1] == 0x0
                && buffer[offset + 2] == 0x0 && buffer[offset + 3] == 0x0
                && buffer[offset + 4] == 0x57 && buffer[offset + 5] == 0x59
                &&buffer[offset + 6] == 0x41 && buffer[offset + 7] == 0x56) {
            byte[] l = new byte[4];
            System.arraycopy(buffer,offset+108,l,0,4);

            return ByteArrayToInt(l);
        }else {
            return 0;
        }
    }


    /**
     * Find H264 frame head
     *
     * @param buffer
     * @param len
     * @return the offset of frame head, return 0 if can not find one
     */
//    static int findHead(byte[] buffer, int len) {
//        int i;
//        for (i = HEAD_OFFSET; i < len; i++) {
//            if (checkHeadLeng(buffer, i))
//                break;
//        }
//        if (i == len)
//            return 0;
//        if (i == HEAD_OFFSET)
//            return 0;
//        return i;
//    }

    /**
     * byte转int
     */

    public static int ByteArrayToInt(byte[] bArr) {
        if(bArr.length!=4){
            return -1;
        }
        return (int) ((((bArr[3] & 0xff) << 24)
                | ((bArr[2] & 0xff) << 16)
                | ((bArr[1] & 0xff) << 8)
                | ((bArr[0] & 0xff) << 0)));
    }

}
