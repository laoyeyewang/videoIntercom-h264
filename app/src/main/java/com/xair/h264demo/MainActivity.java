package com.xair.h264demo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Button;

import com.google.gson.Gson;
import com.xair.h264demo.UDP.UDPClient;
import com.xair.h264demo.UDP.UdpServer;
import com.xair.h264demo.deviceInfo.DeviceInfo;
import com.xair.h264demo.entity.UDPSend;

import org.json.JSONException;
import org.json.JSONObject;


//udp
//6801
//tcp
//8101 192.168.2.140
//6803
//tcp server


public class MainActivity extends Activity {
	//tag 标记
	protected static final String TAG = MainActivity.class.getSimpleName();



	private String h264Path = "/mnt/sdcard/720pq.h264";
	private File h264File = new File(h264Path);
	private InputStream is = null;
	private FileInputStream fs = null;

	static public SurfaceView mSurfaceView;
	private Button mReadButton;
	private MediaCodec mCodec;

	Thread readFileThread;
	boolean isInit = false;
    //AudioPlayer audioPlayer;

	// Video Constants
	private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
	private final static int VIDEO_WIDTH = 640;
	private final static int VIDEO_HEIGHT = 480;
	private final static int TIME_INTERNAL = 50;
	private final static int HEAD_OFFSET = 512;
    private final static int VEDIO_DATALENG = 1312;
	private MyReceiver myReceiver;

	AudioTrack player=null;
	int bufferSize=0;//最小缓冲区大小
    AudioRecord audioRecord=null;
	//int sampleRateInHz = 11025;//采样率
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO; //单声道
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT; //量化位数

    byte[] receveAudio = new byte[VEDIO_DATALENG];
    int audioindex = 0;

	TCPServer tcpServer;

	static public MediaCodecEx mediaCodecEx;


	public static boolean bDecFinished = false;


    int maxBufferSize = 1024*200;
    private byte[] soure = new byte[maxBufferSize];
    int cutposition = 0;






	////////////////////////////
	//Udp客户端向开门发送自己的信息
	public String DeviceType = "VideoTalkFangjian";
	public String ID;
	public String IP;
	public String MASK;
	public String GATE;
	public String DNS;
	public String Louhao = "168";
	public String Danyuan = "03";
	public String Fangjian = "0103";
	public String Name = "android-室内";
	public String Version = "1.1";


	//////////////////////////////
	//socket
	public static Context context;
	ExecutorService exec = Executors.newCachedThreadPool();
	private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
	////////////////////////
	//UDPClient
	private UDPClient udpClient = null;

	//UDPServer
	private UdpServer udpServer = null;





	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		//启动服务
		Intent intent = new Intent(MainActivity.this,UdpReceiver.class);
		startService(intent);
		//注册广播接收器
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.weiyuzk.UdpReceiver");
		registerReceiver(myReceiver, filter);



		readFileThread = new Thread(readFile);
		readFileThread.start();



		// 初始化线程池
//		TCPSend.mThreadPool = Executors.newCachedThreadPool();
//		TCPSend.connectThread();


		//		tcpServer = new TCPServer(6803);
//		exec.execute(tcpServer);



		/////////////////////////////////////
		//获取配置
		ID = DeviceInfo.getUUID();
		printIpAddress();
		DNS = DeviceInfo.getLocalDNS();



/////////////////////////////////////////////////////
		bindReceiver();//注册broadcastReceiver接收器
		context = this;
		///////////////////////////
		//socket
		//客户端
		UDPClientInit();

		//服务端
		UDPServerInit();

        tcpServerData();
		//UDPServerData();

		mediaCodecEx = new MediaCodecEx();
		mediaCodecEx.InitBuffer();
	}

	/**
	 * udp 客户端初始化
	 */

	public void UDPClientInit(){
		udpClient = new UDPClient();
		exec.execute(udpClient);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Gson gson = new Gson();
				UDPSend udpSend = new UDPSend();
				udpSend.setDeviceType(DeviceType);
			    udpSend.setID(ID);
			    udpSend.setIP(IP);
				udpSend.setMASK(MASK);
				udpSend.setGATE(GATE);
				udpSend.setDNS(DNS);
				udpSend.setLouhao(Louhao);
				udpSend.setDanyuan(Danyuan);
				udpSend.setFangjian(Fangjian);
				udpSend.setName(Name);
				udpSend.setVersion(Version);

				String udpSendMessage = gson.toJson(udpSend);//把对象转为JSON格式的字符串
				Log.i(TAG, "run: "+udpSendMessage);

				udpClient.send(udpSendMessage);
			}
		});
		thread.start();
	}

	public void UDPServerInit(){
		udpServer = new UdpServer(40400);
		Thread thread = new Thread(udpServer);
		thread.start();
	}

	/**
	 * 注册broadcastReceiver接收器
	 */

	private void bindReceiver(){
		IntentFilter intentFilterUDPServer = new IntentFilter("udpServer");
		registerReceiver(myBroadcastReceiver,intentFilterUDPServer);

		IntentFilter intentFilter = new IntentFilter("tcpServerReceiver");
		registerReceiver(myBroadcastReceiver,intentFilter);

	}

	/**
	 * broadcastReceiver实现
	 */

	private class MyBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String mAction = intent.getAction();

			if ("udpServer".equals(mAction)) {
				String udpdata = intent.getStringExtra("udpServer");
				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(udpdata);
					Log.i(TAG, "onReceive: +++++++++++++++++++++++++++++++++++++++++++++"+jsonObject);
					if (jsonObject.optString("DeviceType").equals("VideoTalkIpc")){

					}else if(jsonObject.optString("Action").equals("SeekDevice")){
						Log.i(TAG, "onReceive: "+jsonObject);
						Thread thread = new Thread(new Runnable() {
							@Override
							public void run() {
								Gson gson = new Gson();
								UDPSend udpSend = new UDPSend();
								udpSend.setDeviceType(DeviceType);
								udpSend.setID(ID);
								udpSend.setIP(IP);
								udpSend.setMASK(MASK);
								udpSend.setGATE(GATE);
								udpSend.setDNS(DNS);
								udpSend.setLouhao(Louhao);
								udpSend.setDanyuan(Danyuan);
								udpSend.setFangjian(Fangjian);
								udpSend.setName(Name);
								udpSend.setVersion(Version);

								String udpSendMessage = gson.toJson(udpSend);//把对象转为JSON格式的字符串
								Log.i(TAG, "run: "+udpSendMessage);

								udpClient.send(udpSendMessage);
							}
						});
						thread.start();

					}
					Log.i(TAG, "onReceive: "+udpdata);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if ("tcpServerReceiver".equals(mAction)) {
				if (!isInit) {
					//initDecoder();
					mediaCodecEx.InitDecoder(mSurfaceView.getHolder().getSurface(),MIME_TYPE,VIDEO_WIDTH,VIDEO_HEIGHT);
					mediaCodecEx.bShowing = true;
					isInit = true;
				}

//				byte[] msg = intent.getByteArrayExtra("tcpServerReceiver");
//				Log.i(TAG, "onReceive: " + msg);
//				byte[] dataV = new byte[4];
//				System.arraycopy(msg, 100, dataV, 0, 4);
//				Log.i(TAG, "onReceive: " + ByteArrayToInt(dataV));

//				if (ByteArrayToInt(dataV) == 1) {
//					byte[] dataB = new byte[msg.length - 116];
//					System.arraycopy(msg, 116, dataB, 0, msg.length - 116);
//					int datacount = dataB.length;
//					//Log.i(TAG, "onReceive---------------wang: " + datacount);
//					System.out.print(datacount);
//					//onFrame(dataB, 0, datacount);
//					mediaCodecEx.InputDataToDecoder(dataB,datacount);
//
//				} else if (ByteArrayToInt(dataV) == 2) {
//					byte[] dataA = new byte[msg.length - 116];
//					System.arraycopy(msg, 116, dataA, 0, msg.length - 116);
//					int dataleng = dataA.length;
//					System.arraycopy(dataA, 0, receveAudio, dataleng * audioindex, dataleng);
//					audioindex++;
//					if (audioindex == 4) {
//						Log.i(TAG, "onReceive: 声音数据------------------------------------" + receveAudio.length);
//
//						//playVudio(receveAudio);
//
//						//receveAudio = new byte[1312];
//						audioindex = 0;
//					}
//				}
				//bDecFinished = true;
				Log.i(TAG, "onReceive:=============================== ");


			}
		}
	}
















	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		readFileThread.interrupt();
		//结束服务
		stopService(new Intent(MainActivity.this, UdpReceiver.class));
		udpClient.setUdpLife(false);

	}


    public void initDecoder() {

		mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
		MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
				VIDEO_WIDTH, VIDEO_HEIGHT);
		mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, VIDEO_WIDTH * VIDEO_HEIGHT);
		mCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(),
				null, 0);
		mCodec.start();
	}




	int mCount = 0;

	public boolean onFrame(byte[] buf, int offset, int length) {
		Log.e("Media", "onFrame start");
		Log.e("Media", "onFrame Thread:" + Thread.currentThread().getId());
		// Get input buffer index
		ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
		int inputBufferIndex = mCodec.dequeueInputBuffer(10);

		Log.e("Media", "onFrame index:" + inputBufferIndex);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(buf, offset, length);
			mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount
					* TIME_INTERNAL, 0);
			mCount++;
		} else {
			return false;
		}

		// Get output buffer index
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 10);
		while (outputBufferIndex >= 0) {
			mCodec.releaseOutputBuffer(outputBufferIndex, true);
			outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
		}
		Log.e("Media", "onFrame end");
		return true;
	}









	/**
	 * Find H264 frame head
	 *
	 * @param
	 * @param
	 * @return the offset of frame head, return 0 if can not find one
	 */
//	static int findHead(byte[] buffer, int len) {
//		int i;
//		for (i = HEAD_OFFSET; i < len; i++) {
//			if (checkHead(buffer, i))
//				break;
//		}
//		if (i == len)
//			return 0;
//		if (i == HEAD_OFFSET)
//			return 0;
//		return i;
//	}
//
//	/**
//	 * Check if is H264 frame head
//	 *
//	 * @param buffer
//	 * @param offset
//	 * @return whether the src buffer is frame head
//	 */
//	static boolean checkHead(byte[] buffer, int offset) {
//		// 00 00 00 01
//		if (buffer[offset] == 0 && buffer[offset + 1] == 0
//				&& buffer[offset + 2] == 0 && buffer[3] == 1)
//			return true;
//		// 00 00 01
//		if (buffer[offset] == 0 && buffer[offset + 1] == 0
//				&& buffer[offset + 2] == 1)
//			return true;
//		return false;
//	}



	public class MyReceiver extends BroadcastReceiver {
			public void onReceive(Context context, Intent intent){
				try {

                    if (!isInit) {
                        initDecoder();
                        isInit = true;
                    }
					Bundle bundle = intent.getExtras();
					byte[] count = bundle.getByteArray("count");
					Log.i(TAG, "onReceive: " + count.length);
					System.out.print(count.length);
					byte[] dataV = new byte[4];
					System.arraycopy(count,100,dataV,0,4);
					Log.i(TAG, "onReceive: "+ByteArrayToInt(dataV));

					if (ByteArrayToInt(dataV) == 1){
                        byte[] dataB = new byte[count.length-116];
                        System.arraycopy(count,116,dataB,0,count.length-116);
						int datacount = dataB.length;
						Log.i(TAG, "onReceive: " + datacount);
						System.out.print(datacount);
						onFrame(dataB, 0, datacount);
						Log.i(TAG, "onReceive: "+count);
					}else if (ByteArrayToInt(dataV) == 2) {
                       byte[] dataA = new byte[count.length-116];
                        System.arraycopy(count,116,dataA,0,count.length-116);
                        int dataleng = dataA.length;
//						readFileThread = new Thread(readFile);
//						readFileThread.start();

//						System.arraycopy(dataA,0,receveAudio,dataleng*audioindex,dataleng);
//						audioindex++;
//                        if (audioindex == 4){
//							playVudio(receveAudio);
//							//receveAudio = new byte[1312];
//							audioindex = 0;
//						}

                    }

				}catch (Exception e){
					Log.i(TAG, "onReceive: "+e);
				}
			}
	}

















	/**
 *
 * 声音的播放
 */
	Runnable readFile = new Runnable() {
		@Override
		public void run() {
			DataInputStream dis=null;
			try {
				dis = new DataInputStream(new BufferedInputStream(getResources().getAssets().open("welcome.wav")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//最小缓存区
			int bufferSizeInBytes= AudioTrack.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
			//创建AudioTrack对象   依次传入 :流类型、采样率（与采集的要一致）、音频通道（采集是IN 播放时OUT）、量化位数、最小缓冲区、模式
			player=new AudioTrack(AudioManager.STREAM_MUSIC,sampleRateInHz,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
			byte[] data =new byte [bufferSizeInBytes];
			//byte[] data =new byte [320];
			player.play();//开始播放
			while(true)
			{
				int i=0;
				try {
					while(dis.available()>0&&i<data.length)
					{
						data[i]=dis.readByte();
						i++;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				short[] pcm = new short[1312];
				com.example.test.G711Code.G711aDecoder(pcm,data,1312);
				//player.write(data,0,data.length);
				player.write(pcm,0,pcm.length);
				if(i!=bufferSizeInBytes) //表示读取完了
				{
					player.stop();//停止播放
					player.release();//释放资源
					break;
				}
			}
		}
	};
	boolean a  = false;
	int bufferSizeInBytes;
	public void playVudio(byte[] b){
		if (a == false){
			//byte[] data =new byte [320];

			a = true;
		}

		try {
			//最小缓存区
			bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
			//创建AudioTrack对象   依次传入 :流类型、采样率（与采集的要一致）、音频通道（采集是IN 播放时OUT）、量化位数、最小缓冲区、模式
			player=new AudioTrack(AudioManager.STREAM_MUSIC,sampleRateInHz,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);

			player.play();//开始播放
			byte[] data =new byte [bufferSizeInBytes];
			System.arraycopy(b,0,data,0,b.length);
			short[] pcm = new short[bufferSizeInBytes];
			com.example.test.G711Code.G711aDecoder(pcm,data,bufferSizeInBytes);
			//player.write(data,0,data.length);
			player.write(pcm,0,pcm.length);
		}catch (Exception e){
			Log.i(TAG, "playVudio: ++++++++++++++++++++++"+e);
		}


	}


	DatagramSocket dgSocket = null;
	public void UDPServerData(){

		int port = 6801;
		if (dgSocket == null) {
			try {
				dgSocket = new DatagramSocket(null);
				dgSocket.setReuseAddress(true);
				dgSocket.bind(new InetSocketAddress(port));
			} catch (SocketException e) {
				e.printStackTrace();
			}

			//dgSocket.setReceiveBufferSize(512*1024);
		}
		new Thread() {
			@Override
			public void run() {
//                super.run();
				while (true) {
					try {
//						try {
//							Thread.sleep(10);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}

						// byte[] by = new byte[307200];
						byte[] by = new byte[1024*4];
						DatagramPacket packet = new DatagramPacket(by, by.length);
						System.out.print(by.length);
						Log.i("tag", "run: "+by.length);
						// dgSocket.setSoTimeout(500);
						dgSocket.receive(packet);
						int dataL = packet.getLength();
						byte[] data = new  byte[dataL];

						if (!isInit) {
							//initDecoder();
							mediaCodecEx.InitDecoder(mSurfaceView.getHolder().getSurface(),MIME_TYPE,VIDEO_WIDTH,VIDEO_HEIGHT);
							mediaCodecEx.bShowing = true;
							isInit = true;
						}

						System.arraycopy(by,0,data,0,dataL);

						if ((cutposition + dataL) > maxBufferSize) {
							cutposition = 0;
						}
						System.arraycopy(data, 0, soure, cutposition, dataL);
						cutposition = cutposition + dataL;
						Log.i(TAG, "run: ====================="+cutposition);
						///////////////////////////////////
						int nlastSorftLeng = 0;
						long a1= SystemClock.elapsedRealtime();
						for (int i = 0; i < cutposition; i++) {

							int dataLeng = checkHeadLeng(soure, i);
							if (dataLeng > 0 && (dataLeng + i) <= maxBufferSize){
								byte[] buffer = new byte[dataLeng+116];
								System.arraycopy(soure,i,buffer,0,dataLeng + 116);
								nlastSorftLeng = i + 116 + dataLeng;


								if (cutposition >= nlastSorftLeng){
									Log.i(TAG, "run: -------------------------"+cutposition);
								Log.i(TAG, "run: +++++++++++++++++++++++++"+nlastSorftLeng);
									Log.i(TAG, "run: {{{{{{{{{{{{{{{{{{{{"+dataLeng);
								i += (dataLeng + 116);

								byte[] dataV = new byte[4];
								System.arraycopy(data,100,dataV,0,4);
								Log.i(TAG, "onReceive: "+ByteArrayToInt(dataV));

								if (ByteArrayToInt(dataV) == 1){
									byte[] videoB = new byte[dataLeng];
									System.arraycopy(buffer,116,videoB,0,dataLeng);

									//onFrame(videoB,0,dataLeng);
									//MainActivity.mediaCodecEx.InputDataToDecoder(buffer,dataLeng + 116);
									MainActivity.mediaCodecEx.InputDataToDecoder(videoB,dataLeng);
									//Log.i(TAG, "data:++++++++++++++++ ==============="+buffer.length);
								}else {
									byte[] vediobuffer = new byte[dataLeng+116];
									System.arraycopy(soure,i,vediobuffer,0,dataLeng + 116);
								}
								if (cutposition - nlastSorftLeng >= 0) {
									byte[] newData = new byte[cutposition - nlastSorftLeng];
									System.arraycopy(soure, nlastSorftLeng, newData, 0, cutposition - nlastSorftLeng);
									System.arraycopy(newData, 0, soure, 0, cutposition - nlastSorftLeng);
									cutposition -= nlastSorftLeng;
								}else {
									cutposition = 0;
								}
								long a2= SystemClock.elapsedRealtime();
								//Log.i(TAG, "time:++++++++++++++++ ==============="+(a2-a1)+"========="+cutposition);
								}else {
									break;
								}
							}

						}











					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}


	/**
	 * tcp接收视频数据流
	 */

	public void tcpServerData() {
	    new Thread(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                super.run();
                ServerSocket serverSocket=null;
                try{
                    //创建ServerSocket对象监听PORT端口
                    serverSocket = new ServerSocket(6803);
                    //接收tcp连接返回socket对象
                    Socket socket= serverSocket.accept();

                    //获得输入流
					is=socket.getInputStream();
                    ///////////////////////////////////////////////////////////////////////////////////////
                    //获得输出流
                    OutputStream outputStream = socket.getOutputStream();
					byte buff[]  = new byte[1024 * 8];
                    int rcvLen = 0;
                    String s;

                    //读取接收到的数据
                    while((rcvLen = is.read(buff))!=-1)
                    {
                        //outputStream.write(byteBuffer, 0, temp);
						if (!isInit) {
							//initDecoder();
							mediaCodecEx.InitDecoder(mSurfaceView.getHolder().getSurface(),MIME_TYPE,VIDEO_WIDTH,VIDEO_HEIGHT);
							mediaCodecEx.bShowing = true;
							isInit = true;
						}

						//                            if ((rcvLen = is.read(buff)) != -1){

						// rcvMsg = new String(buff,0,rcvLen);
						//Log.i(TAG, "run:收到消息: " + rcvMsg);
						//Log.i(TAG, "run: ++++++++++++++++" + rcvLen);
						byte[] data = new byte[rcvLen];
						System.arraycopy(buff,0,data,0,rcvLen);

						if ((cutposition + rcvLen) > maxBufferSize) {
							cutposition = 0;
						}
						System.arraycopy(data, 0, soure, cutposition, rcvLen);
						cutposition = cutposition + rcvLen;
						Log.i(TAG, "run: ====================="+cutposition);
						///////////////////////////////////
						int nlastSorftLeng = 0;
						long a1= SystemClock.elapsedRealtime();
						for (int i = 0; i < cutposition; i++) {

							int dataLeng = checkHeadLeng(soure, i);
							if (dataLeng > 0 && (dataLeng + i) <= maxBufferSize){
								byte[] buffer = new byte[dataLeng+116];
								System.arraycopy(soure,i,buffer,0,dataLeng + 116);
								nlastSorftLeng = i + 116 + dataLeng;


								if (cutposition >= nlastSorftLeng){
									Log.i(TAG, "run: -------------------------"+cutposition);
									Log.i(TAG, "run: +++++++++++++++++++++++++"+nlastSorftLeng);
									Log.i(TAG, "run: {{{{{{{{{{{{{{{{{{{{"+dataLeng);
									i += (dataLeng + 116);

									byte[] dataV = new byte[4];
									System.arraycopy(buffer,100,dataV,0,4);
									Log.i(TAG, "onReceive: ----------------------"+ByteArrayToInt(dataV));
									Log.i(TAG, "run: ");

									if (ByteArrayToInt(dataV) == 1){

										byte[] videoB = new byte[dataLeng];
										System.arraycopy(buffer,116,videoB,0,dataLeng);
										MainActivity.mediaCodecEx.InputDataToDecoder(videoB,dataLeng);
										//onFrame(videoB,0,dataLeng);

									}else if(ByteArrayToInt(dataV) == 2){
										byte[] vediobuffer = new byte[436];
										//System.arraycopy(soure,i,vediobuffer,0,dataLeng + 116);
										System.arraycopy(soure,0,vediobuffer,0,dataLeng+116);
										System.arraycopy(vediobuffer,116,receveAudio,0+dataLeng * audioindex,dataLeng);
						             audioindex++;
                       			   if (audioindex == 4){
									//playVudio(b);
									   short[] pcm = new short[VEDIO_DATALENG];
									   com.example.test.G711Code.G711aDecoder(pcm,receveAudio,VEDIO_DATALENG);
									   AudioReader.init();
									   AudioReader.playAudioTrack(pcm,0,VEDIO_DATALENG);
									   long a2= SystemClock.elapsedRealtime();
									   Log.i(TAG, "time:++++++++++++++++ ==============="+(a2-a1)+"========="+cutposition);
									audioindex = 0;
										}
									}
									if (cutposition - nlastSorftLeng >= 0) {
										byte[] newData = new byte[cutposition - nlastSorftLeng];
										System.arraycopy(soure, nlastSorftLeng, newData, 0, cutposition - nlastSorftLeng);
										System.arraycopy(newData, 0, soure, 0, cutposition - nlastSorftLeng);
										cutposition -= nlastSorftLeng;
									}else {
										cutposition = 0;
									}

								}else {
									break;
								}
							}

						}


						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}


					}
                    //System.out.println(new String(byteBuffer,0,temp));
                    outputStream.flush();
                    socket.close();
                    serverSocket.close();

                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
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
		if(buffer[offset] != 0x74) {
		    return 0;
        }

		if(buffer[offset + 4] != 0x57) {
		     return 0;
        }

        if (buffer[offset + 5] != 0x59){
		    return 0;
        }

        if (buffer[offset + 6] != 0x41) {
		      return 0;
        }

		if (buffer[offset + 7] != 0x56){
		    return 0;
         }

		byte[] l = new byte[4];
		System.arraycopy(buffer,offset+108,l,0,4);
		return ByteArrayToInt(l);

    }
	static boolean checkHead(byte[] buffer, int offset) {
		// 00 00 00 01
		if(buffer[offset] == 0 && buffer[offset+1] == 0 && buffer[offset+2] == 0 && buffer[offset+3] == 1) {
			return true;
		}
		return false;

	}


	/**
	 * byte转int
	 */

	static public int ByteArrayToInt(byte[] bArr) {
		if(bArr.length!=4){
			return -1;
		}
		return (int) ((((bArr[3] & 0xff) << 24)
				| ((bArr[2] & 0xff) << 16)
				| ((bArr[1] & 0xff) << 8)
				| ((bArr[0] & 0xff) << 0)));
	}

	/**
	 *获取ip、掩码、网关
	 */

	public void printIpAddress() {
		try {
			Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces();
			while (eni.hasMoreElements()) {

				NetworkInterface networkCard = eni.nextElement();
				if (!networkCard.isUp()) { // 判断网卡是否在使用
					continue;
				}

				String DisplayName = networkCard.getDisplayName();

				List<InterfaceAddress> addressList = networkCard.getInterfaceAddresses();
				Iterator<InterfaceAddress> addressIterator = addressList.iterator();
				while (addressIterator.hasNext()) {
					InterfaceAddress interfaceAddress = addressIterator.next();
					InetAddress address = interfaceAddress.getAddress();
					if (!address.isLoopbackAddress()) {
						String hostAddress = address.getHostAddress();

						if (hostAddress.indexOf(":") > 0) {
						} else {
							String maskAddress = calcMaskByPrefixLength(interfaceAddress.getNetworkPrefixLength());
							String gateway = calcSubnetAddress(hostAddress, maskAddress);

							String broadcastAddress = null;
							InetAddress broadcast = interfaceAddress.getBroadcast();
							if (broadcast != null)
								broadcastAddress = broadcast.getHostAddress();

							IP = hostAddress;
							MASK=maskAddress;
							GATE = gateway;

							Log.e("GGG", "DisplayName    =   " + DisplayName);
							Log.e("GGG", "address        =   " + hostAddress);
							Log.e("GGG", "mask           =   " + maskAddress);
							Log.e("GGG", "gateway        =   " + gateway);
							Log.e("GGG", "broadcast      =   " + broadcastAddress + "\n");
							Log.e("GGG", "----- NetworkInterface  Separator ----\n\n");

						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String calcMaskByPrefixLength(int length) {

		int mask = 0xffffffff << (32 - length);
		int partsNum = 4;
		int bitsOfPart = 8;
		int maskParts[] = new int[partsNum];
		int selector = 0x000000ff;

		for (int i = 0; i < maskParts.length; i++) {
			int pos = maskParts.length - 1 - i;
			maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
		}

		String result = "";
		result = result + maskParts[0];
		for (int i = 1; i < maskParts.length; i++) {
			result = result + "." + maskParts[i];
		}
		return result;
	}

	public static String calcSubnetAddress(String ip, String mask) {
		String result = "";
		try {
			// calc sub-net IP
			InetAddress ipAddress = InetAddress.getByName(ip);
			InetAddress maskAddress = InetAddress.getByName(mask);

			byte[] ipRaw = ipAddress.getAddress();
			byte[] maskRaw = maskAddress.getAddress();

			int unsignedByteFilter = 0x000000ff;
			int[] resultRaw = new int[ipRaw.length];
			for (int i = 0; i < resultRaw.length; i++) {
				resultRaw[i] = (ipRaw[i] & maskRaw[i] & unsignedByteFilter);
			}

			// make result string
			result = result + resultRaw[0];
			for (int i = 1; i < resultRaw.length; i++) {
				result = result + "." + resultRaw[i];
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return result;
	}
}



