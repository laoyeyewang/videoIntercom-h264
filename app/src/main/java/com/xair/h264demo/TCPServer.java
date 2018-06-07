package com.xair.h264demo;

import android.content.Intent;
import android.util.Log;

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
//    private  boolean isEnable;
//    private final WebConfig webConfig;//配置信息类
//    private final ExecutorService threadPool;//线程池
//    private  ServerSocket socket;
//
//    MainActivity mainActivity;
//
//
//    public TCPServer(WebConfig webConfig) {
//        this.webConfig = webConfig;
//        threadPool = Executors.newCachedThreadPool();
//        mainActivity = new MainActivity();
//    }
//
//    /**
//     * 开启server
//     */
//    public void startServerAsync() {
//        isEnable=true;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                doProcSync();
//            }
//        }).start();
//    }
//
//    /**
//     * 关闭server
//     */
//    public void stopServerAsync() throws IOException {
//        if (!isEnable){
//            return;
//        }
//        isEnable=true;
//        socket.close();
//        socket=null;
//    }
//
//    private void doProcSync() {
//        try {
//            InetSocketAddress socketAddress=new InetSocketAddress(webConfig.getPort());
//            socket=new ServerSocket();
//            socket.bind(socketAddress);
//            while (isEnable){
//                final Socket remotePeer= socket.accept();
//                threadPool.submit(new Runnable() {
//                    @Override
//                    public void run() {
//                        //Log.e("remotePeer..............."+remotePeer.getRemoteSocketAddress().toString());
//                        onAcceptRemotePeer(remotePeer);
//                    }
//                });
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private byte[] onAcceptRemotePeer(Socket remotePeer) {
//        byte[] buffer = new byte[1024*300];
//        try {
//            remotePeer.getOutputStream().write("connected successful".getBytes());//告诉客户端连接成功
//            // 从Socket当中得到InputStream对象
//            InputStream inputStream = remotePeer.getInputStream();
//            int temp = 0;//
//            // 从InputStream当中读取客户端所发送的数据
//            while ((temp = inputStream.read(buffer)) != -1) {
//               // LLogger.e(new String(buffer, 0, temp,"UTF-8"));
//              //  remotePeer.getOutputStream().write(buffer,0,temp);//把客户端传来的消息发送回去
//                Log.i("tag", "onAcceptRemotePeer: "+buffer);
//                mainActivity.tcpdata(buffer);
//
//            }
//            return buffer;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return buffer;
//    }
//
//    /**
//     * byte转int
//     */
//    public int ByteArrayToInt(byte[] bArr) {
//        if(bArr.length!=4){
//            return -1;
//        }
//        return (int) ((((bArr[3] & 0xff) << 24)
//                | ((bArr[2] & 0xff) << 16)
//                | ((bArr[1] & 0xff) << 8)
//                | ((bArr[0] & 0xff) << 0)));
//    }





    private String TAG = "TcpServer";
    private int port = 6803;
    private boolean isListen = true;   //线程监听标志位
    int maxBufferSize = 1024*800;
    private byte[] soure = new byte[maxBufferSize];
    int cutposition = 0;
    private final static int HEAD_OFFSET = 0;

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
            Log.i(TAG, "run: 监听超时");
            return null;
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(5000);
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

                       // rcvMsg = new String(buff,0,rcvLen);
                        //Log.i(TAG, "run:收到消息: " + rcvMsg);
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
                                i += (dataLeng + 116 - 1);
                               outputDecode(buffer);
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




//                        if (rcvMsg.equals("QuitServer")){
//                            isRun = false;
//                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            try {
//                socket.close();
//                SST.clear();
//                Log.i(TAG, "run: 断开连接");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
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




//    int nSolved=0;
//				while(nSolved<nCurPos){
//        char *pBuffer=(char *)Buf+nSolved;
//        IV_BROADCAST_IPC_HEADER *header=(IV_BROADCAST_IPC_HEADER*)pBuffer;
//        if(header->nStructSize==sizeof(IV_BROADCAST_IPC_HEADER)){
//        int dwBufSize=header->nDataLen;
//        if((nSolved+dwBufSize+sizeof(IV_BROADCAST_IPC_HEADER))>nCurPos)
//        break;
//        char *pData=(char *)pBuffer+sizeof(IV_BROADCAST_IPC_HEADER);
//        if(header->nDataType==IV_IPC_DATA_TYPE_VIDEO)
//        {
//        if(nDataBianhao!=header->nDataBianhao)
//        nDataBianhao=header->nDataBianhao;
//        else
//        break;
//        //解码显示
//        int i=0;
//        int nServerList=0;
//        int nServerChl=0;
//        if(!bVideoTalkDecInited){
//        //////////////////////根据导入的数据流判断是否是265数据
//        //////////////////////////////////////////////////
//        SoftDecoder_SetDecodeChlType(nSoftDecodeChl,SOFT_DECODE_HS_H264);
//        //如果使用自己来解码，打开通道
//        //打开数据openstream
//        SoftDecoder_SetStreamOpenMode(nSoftDecodeChl,STREAME_REALTIME);//STREAME_FILE //STREAME_REALTIME
//        bool bRet1=false;
//        //如果没有窗体句柄，很有可能是在进行YUV输出，需要最大可能增大实时性
//        bRet1=SoftDecoder_OpenStream(nSoftDecodeChl,"",0,0,false);
//        bool bRet2=false;
//        bRet2=SoftDecoder_Play(nSoftDecodeChl,hVideoTalkWnd);
//        if(bRet1&&bRet2)
//        bVideoTalkDecInited=true;
//        }
//        nFrameType=PktIFrames;
//        SoftDecoder_InputData(nSoftDecodeChl, (unsigned char *)pData,dwBufSize,nFrameType);
//        nSolved+=(dwBufSize+sizeof(IV_BROADCAST_IPC_HEADER));
//        continue;
//        }
//        else if(header->nDataType==IV_IPC_DATA_TYPE_AUDIO)
//        {
//        nFrameType=PktAudioFrames;
//        SoftDecoder_InputData(nSoftDecodeChl, (unsigned char *)pData,dwBufSize,nFrameType);
//        nSolved+=(dwBufSize+sizeof(IV_BROADCAST_IPC_HEADER));
//        continue;
//        }
//        }
//        nSolved++;
//        }//while
//        if((nCurPos-nSolved)>0){
//        memmove(Buf,Buf+nSolved,nCurPos-nSolved);
//        }
//        nCurPos-=nSolved;
//        if(nCurPos<0)
//        nCurPos=0;
//        数字矩阵  10:10:24
//        网络接收部分的代码参考：
//
//        if((nMaxReadLen-nCurPos)<1)
//        nCurPos=0;
//        err = IVComInf_Recv(hLinkSocket,(char *)Buf+nCurPos,nMaxReadLen-nCurPos,1500,1);
//        数字矩阵  10:10:42
//        其中：
//        int            nMaxReadLen=300*1024;
//        char	        *Buf=new char[nMaxReadLen];