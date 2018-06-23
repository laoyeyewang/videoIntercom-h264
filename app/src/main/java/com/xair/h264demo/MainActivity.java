package com.xair.h264demo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xair.h264demo.TCP.TCPSend;
import com.xair.h264demo.TCP.TCPServer;
import com.xair.h264demo.Tool.Audio.AudioReader;
import com.xair.h264demo.Tool.Code.G711Code;
import com.xair.h264demo.Tool.Code.MediaCodecEx;
import com.xair.h264demo.Tool.SQL.RoomDoa;
import com.xair.h264demo.Tool.SQL.RoomInfo;
import com.xair.h264demo.UDP.UDPClient;
import com.xair.h264demo.UDP.UdpReceiver;
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


    //AudioPlayer audioPlayer;

	// Video Constants
	private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
	private final static int VIDEO_WIDTH = 1280;
	private final static int VIDEO_HEIGHT = 720;
	private final static int TIME_INTERNAL = 33;
	private final static int HEAD_OFFSET = 512;
    private final static int VEDIO_DATALENG = 1486;
//	private MyReceiver myReceiver;

	AudioTrack player=null;
	int bufferSize=0;//最小缓冲区大小
    AudioRecord audioRecord=null;
	//int sampleRateInHz = 11025;//采样率
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO; //单声道
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT; //量化位数

    byte[] receveAudio = new byte[VEDIO_DATALENG];
	byte[] vediobuffer = new byte[436];


    int audioindex = 0;

	TCPServer tcpServer;

	static public MediaCodecEx mediaCodecEx;


	public static boolean bDecFinished = false;


    int maxBufferSize = 1024*80;
    private byte[] soure = new byte[maxBufferSize];
    int cutposition = 0;

  //////////////////
	public boolean Isling = false;
	public boolean IsLingStart = false;
	public boolean IsShow = false;
	public int lingCount = 0;



	static public SurfaceView mSurfaceView;
	private Button mReadButton;
	private Button btnRecive;
	private Button btnRefuse;
	private MediaCodec mCodec;
	Thread readFileThread;
	boolean isInit = false;




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
	public String Password = "123456";
	public String Version = "1.1";


	MediaCodec.BufferInfo bufferInfo;



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

///////////////////////////////////
	//SQList
    private RoomDoa roomDoa;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		mReadButton = (Button) findViewById(R.id.btn_opendoor);
		btnRecive = (Button)findViewById(R.id.btn_recive) ;
		btnRefuse = (Button)findViewById(R.id.btn_refuse) ;
		mReadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IsShow = false;
				IsLingStart = true;

                // 初始化线程池
                TCPSend.mThreadPool = Executors.newCachedThreadPool();
                TCPSend.connectThread();


				byte[] headerB = new byte[4144];
				byte[] nCMD = new byte[4];
				nCMD = intToByteArray(13);
				String str = "OPEN_DOOR";
				byte[] b=str.getBytes();
				Log.i(TAG, "run: "+b);
				System.arraycopy(nCMD,0,headerB,0,4);
				System.arraycopy(b,0,headerB,4,b.length);
				TCPSend.sendMsgThread(headerB);


			}
		});

		btnRecive.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				IsShow = true;
				IsLingStart = true;


                // 初始化线程池
                TCPSend.mThreadPool = Executors.newCachedThreadPool();
                TCPSend.connectThread();


				byte[] headerB = new byte[4144];
				byte[] nCMD = new byte[4];
				nCMD = intToByteArray(13);
				String str="TALK_LINK_SEND";
				byte[] b=str.getBytes();
				Log.i(TAG, "run: "+b);
				System.arraycopy(nCMD,0,headerB,0,4);
				System.arraycopy(b,0,headerB,4,b.length);
				TCPSend.sendMsgThread(headerB);

//				RoomInfo roomInfo = new RoomInfo();
//				roomInfo.setDeviceType("1");
//				roomInfo.setID("1");
//				roomInfo.setIP("1");
//				roomInfo.setMASK("1");
//				roomInfo.setGATE("1");
//				roomInfo.setDNS("1");
//				roomInfo.setLouhao("1");
//				roomInfo.setDanyuan("1");
//				roomInfo.setFangjian("1");
//				roomInfo.setName("1");
//				roomInfo.setPassword("1");
//				roomInfo.setVersion("1");
//				roomDoa.insert(roomInfo);
//				Toast.makeText(MainActivity.this,"新增成功",Toast.LENGTH_SHORT).show();


				RoomInfo user = roomDoa.searchUser("1");
				user.setDeviceType(DeviceType);
				user.setID(ID);
				user.setIP(IP);
				user.setMASK(MASK);
				user.setGATE(GATE);
				user.setDNS(DNS);
				user.setLouhao(Louhao);
				user.setDanyuan(Danyuan);
				user.setFangjian(Fangjian);
				user.setName(Name);
				user.setPassword(Password);
				user.setVersion(Version);
				roomDoa.update(user);
				Toast.makeText(MainActivity.this,"修改成功",Toast.LENGTH_SHORT).show();

				RoomInfo roomInfo = roomDoa.searchUser("1");
//				List<RoomInfo> list= roomDoa.search();
				Toast.makeText(MainActivity.this,"查询成功"+roomInfo,Toast.LENGTH_SHORT).show();
				//AudioCapturer.startCapture();

			}
		});

		btnRefuse.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {

				IsLingStart = true;
				IsShow = false;


				// 初始化线程池
				TCPSend.mThreadPool = Executors.newCachedThreadPool();
				TCPSend.connectThread();


				byte[] headerB = new byte[4144];
				byte[] nCMD = new byte[4];
				nCMD = intToByteArray(13);
				String str = "CANCEL_TALK";
				byte[] b=str.getBytes();
				Log.i(TAG, "run: "+b);
				System.arraycopy(nCMD,0,headerB,0,4);
				System.arraycopy(b,0,headerB,4,b.length);
				TCPSend.sendMsgThread(headerB);

				//AudioCapturer.stopCapture();
			}
		});


//		//启动服务
//		Intent intent = new Intent(MainActivity.this,UdpReceiver.class);
//		startService(intent);
//		//注册广播接收器
//		myReceiver = new MyReceiver();
//		IntentFilter filter = new IntentFilter();
//		filter.addAction("com.example.weiyuzk.UdpReceiver");
//		registerReceiver(myReceiver, filter);



//		readFileThread = new Thread(readFile);
//		readFileThread.start();






		//		tcpServer = new TCPServer(6803);
//		exec.execute(tcpServer);


		/////////////////////////
		// 初始化线程池
//		TCPSend.mThreadPool = Executors.newCachedThreadPool();
//		TCPSend.connectThread();




		/////////////////////////////////////
		//获取配置
		//ID = DeviceInfo.getUUID();
		String[] temp =  DeviceInfo.getUUID().split("-");
		ID = temp[temp.length - 1];
		printIpAddress();
		DNS = DeviceInfo.getLocalDNS();
		roomDoa=new RoomDoa(this);


// Get output buffer index
		bufferInfo = new MediaCodec.BufferInfo();

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


		//初始化存储空间
		  initSQl();

		mediaCodecEx = new MediaCodecEx();
		mediaCodecEx.InitBuffer();
	}

	/**
	 *初始化存储空间
	 */

	public void initSQl(){
		RoomInfo roomInfo1 = roomDoa.searchUser("1");
		if(roomInfo1 == null) {
			RoomInfo roomInfo = new RoomInfo();
			roomInfo.setDeviceType("1");
			roomInfo.setID("1");
			roomInfo.setIP("1");
			roomInfo.setMASK("1");
			roomInfo.setGATE("1");
			roomInfo.setDNS("1");
			roomInfo.setLouhao("1");
			roomInfo.setDanyuan("1");
			roomInfo.setFangjian("1");
			roomInfo.setName("1");
			roomInfo.setPassword("1");
			roomInfo.setVersion("1");
			roomDoa.insert(roomInfo);
			Toast.makeText(MainActivity.this, "新增成功", Toast.LENGTH_SHORT).show();
		}
		RoomInfo roomInfo2 = roomDoa.searchUser("2");
		if(roomInfo1 == null) {
			RoomInfo roomInfo = new RoomInfo();
			roomInfo.setDeviceType("1");
			roomInfo.setID("1");
			roomInfo.setIP("1");
			roomInfo.setMASK("1");
			roomInfo.setGATE("1");
			roomInfo.setDNS("1");
			roomInfo.setLouhao("1");
			roomInfo.setDanyuan("1");
			roomInfo.setFangjian("1");
			roomInfo.setName("1");
			roomInfo.setPassword("1");
			roomInfo.setVersion("1");
			roomDoa.insert(roomInfo);
			Toast.makeText(MainActivity.this, "新增成功", Toast.LENGTH_SHORT).show();
		}
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
				udpSend.setPassword(Password);
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
				final String udpdata = intent.getStringExtra("udpServer");
				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(udpdata);
					Log.i(TAG, "onReceive: +++++++++++++++++++++++++++++++++++++++++++++"+jsonObject);
					if (jsonObject.optString("DeviceType").equals("VideoTalkIpc")){
						Thread thread = new Thread(new Runnable() {
							@Override
							public void run() {
//								RoomInfo user = roomDoa.searchUser("2");
//								user.setDeviceType(DeviceType);
//								user.setID(ID);
//								user.setIP(IP);
//								user.setMASK(MASK);
//								user.setGATE(GATE);
//								user.setDNS(DNS);
//								user.setLouhao(Louhao);
//								user.setDanyuan(Danyuan);
//								user.setFangjian(Fangjian);
//								user.setName(Name);
//								user.setPassword(Password);
//								user.setVersion(Version);
//								roomDoa.update(user);
//								Toast.makeText(MainActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
//
//								RoomInfo roomInfo = roomDoa.searchUser("2");
////				List<RoomInfo> list= roomDoa.search();
//								Toast.makeText(MainActivity.this,"查询成功"+roomInfo,Toast.LENGTH_SHORT).show();
							}
						});
						thread.start();

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
								udpSend.setPassword(Password);
								udpSend.setVersion(Version);

								String udpSendMessage = gson.toJson(udpSend);//把对象转为JSON格式的字符串
								Log.i(TAG, "run:---------==========-------- "+udpSendMessage);

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

	public void onFrame(final byte[] buf, final int offset, final int length) {
				try {
//					Log.e("Media", "onFrame start");
//					Log.e("Media", "onFrame Thread:" + Thread.currentThread().getId());
					// Get input buffer index
					ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
					int inputBufferIndex = mCodec.dequeueInputBuffer(0);
//					Log.e("Media", "onFrame Thread:" + inputBufferIndex);
					if (inputBufferIndex >= 0) {
						ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
						inputBuffer.clear();
						inputBuffer.put(buf, offset, length);
						mCodec.queueInputBuffer(inputBufferIndex, 0, length, 1000000 * mCount / 30, 0);
						mCount++;
					}

					int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
					//对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
					if (outputBufferIndex == -3) {
						Log.e("H264", "AMEDIACODEC__INFO_OUTPUT_BUFFERS_CHANGED");
					} else if (outputBufferIndex == -2) {
						Log.e("H264", "AMEDIACODEC__INFO_OUTPUT_FORMAT_CHANGED");
					} else if (outputBufferIndex == -1) {
						Log.e("H264", "AMEDIACODEC__INFO_TRY_AGAIN_LATER");
						//Thread.sleep(1000);
					}
//					mCodec.releaseOutputBuffer(outputBufferIndex, true);
//					if (outputBufferIndex >= 0){
//						mCodec.releaseOutputBuffer(outputBufferIndex, true);
//					}

					while (outputBufferIndex >= 0) {
						mCodec.releaseOutputBuffer(outputBufferIndex, true);
						outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
					}

//			mCodec.stop();
//			mCodec.release();


//			int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
//			Log.e("tag", "===================== "+outputBufferIndex);
//			while (outputBufferIndex >= 0) {
//				Log.e("Media", "11");
//				mCodec.releaseOutputBuffer(outputBufferIndex, true);
//				Log.e("Media", "12");
//				outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
//
//				Log.i(TAG, "outputBufferIndex++++++++++++++++++++++++++++++++++++++"+outputBufferIndex);
//				Log.e("Media", "13");
//			}

				}catch (Exception e){

				}

				//Log.e("Media", "onFrame end");



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



//	public class MyReceiver extends BroadcastReceiver {
//			public void onReceive(Context context, Intent intent){
//				try {
//
//                    if (!isInit) {
//                        initDecoder();
//                        isInit = true;
//                    }
//					Bundle bundle = intent.getExtras();
//					byte[] count = bundle.getByteArray("count");
//					Log.i(TAG, "onReceive: " + count.length);
//					System.out.print(count.length);
//					byte[] dataV = new byte[4];
//					System.arraycopy(count,100,dataV,0,4);
//					Log.i(TAG, "onReceive: "+ByteArrayToInt(dataV));
//
//					if (ByteArrayToInt(dataV) == 1){
//                        byte[] dataB = new byte[count.length-116];
//                        System.arraycopy(count,116,dataB,0,count.length-116);
//						int datacount = dataB.length;
//						Log.i(TAG, "onReceive: " + datacount);
//						System.out.print(datacount);
//						onFrame(dataB, 0, datacount);
//						Log.i(TAG, "onReceive: "+count);
//					}else if (ByteArrayToInt(dataV) == 2) {
//                       byte[] dataA = new byte[count.length-116];
//                        System.arraycopy(count,116,dataA,0,count.length-116);
//                        int dataleng = dataA.length;
////						readFileThread = new Thread(readFile);
////						readFileThread.start();
//
////						System.arraycopy(dataA,0,receveAudio,dataleng*audioindex,dataleng);
////						audioindex++;
////                        if (audioindex == 4){
////							playVudio(receveAudio);
////							//receveAudio = new byte[1312];
////							audioindex = 0;
////						}
//
//                    }
//
//				}catch (Exception e){
//					Log.i(TAG, "onReceive: "+e);
//				}
//			}
//	}

















	/**
 *
 * 声音的播放
 */
	Runnable readFile = new Runnable() {
		@Override
		public void run() {
			DataInputStream dis=null;
			try {
				dis = new DataInputStream(new BufferedInputStream(getResources().getAssets().open("lingling.wav")));
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
				short[] pcm = new short[bufferSizeInBytes];
				G711Code.G711aDecoder(pcm,data,bufferSizeInBytes);
				//player.write(data,0,data.length);
				player.write(pcm,0,pcm.length);
				if(i!=bufferSizeInBytes) //表示读取完了
				{
					if (IsLingStart == false){
						lingCount++;
						if (lingCount>9){
							lingCount = 0;
							IsLingStart = true;
						}
						player.stop();//停止播放
						player.release();//释放资源
						readFileThread = new Thread(readFile);
						readFileThread.start();
						break;

					}else {
						player.stop();//停止播放
						player.release();//释放资源
						break;
					}

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
			G711Code.G711aDecoder(pcm,data,bufferSizeInBytes);
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
						if (Isling == false){

							Isling = true;
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
                //super.run();
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
					byte[] dataV = new byte[4];
					short[] pcm = new short[VEDIO_DATALENG];
                    int rcvLen = 0;
					int dataLeng = 0;
					byte[] data = null;

                    //读取接收到的数据
                    while((rcvLen = is.read(buff))!=-1) {

						if (Isling == false) {
							readFileThread = new Thread(readFile);
							readFileThread.start();
							Isling = true;
						}
						if (IsShow == true) {
							//outputStream.write(byteBuffer, 0, temp);
							if (!isInit) {
								initDecoder();
//							mediaCodecEx.InitDecoder(mSurfaceView.getHolder().getSurface(),MIME_TYPE,VIDEO_WIDTH,VIDEO_HEIGHT);
//							mediaCodecEx.bShowing = true;
								AudioReader.init();
								isInit = true;
							}
							////////////////
							data = new byte[rcvLen];
//
							System.arraycopy(buff, 0, data, 0, rcvLen);
							if ((cutposition + rcvLen) > maxBufferSize) {
								cutposition = 0;
							}
							System.arraycopy(data, 0, soure, cutposition, rcvLen);
							cutposition = cutposition + rcvLen;
							///////////////////////////////////
							int nlastSorftLeng = 0;
							long a1 = SystemClock.elapsedRealtime();
							for (int i = 0; i < cutposition; i++) {
								dataLeng = checkHeadLeng(soure, i);

								//if (dataLeng > 0 && (dataLeng + i) <= maxBufferSize){
								if (dataLeng > 0 && (dataLeng + i) <= cutposition) {
									//////////////////
									byte[] buffer = new byte[dataLeng + 116];

									System.arraycopy(soure, i, buffer, 0, dataLeng + 116);
									nlastSorftLeng = i + 116 + dataLeng;

									if (cutposition >= nlastSorftLeng) {
										i += (dataLeng + 115);

										System.arraycopy(buffer, 100, dataV, 0, 4);

										if (ByteArrayToInt(dataV) == 1) {
											/////////////////////
											byte[] videoB = new byte[dataLeng];

											System.arraycopy(buffer, 116, videoB, 0, dataLeng);
//											MainActivity.mediaCodecEx.InputDataToDecoder(videoB,dataLeng);
//											Log.i(TAG, "当前长度---------------------------------"+ dataLeng+"===="+cutposition+"*******"+videoB.length);

											onFrame(videoB, 0, dataLeng);


										} else if (ByteArrayToInt(dataV) == 2) {
											System.arraycopy(soure, 0, vediobuffer, 0, dataLeng + 116);
											System.arraycopy(vediobuffer, 116, receveAudio, 0 + dataLeng * audioindex, dataLeng);
											audioindex++;
											if (audioindex == 4) {

												vedioRecode(pcm);
												audioindex = 0;
											}
										}
										if (cutposition - nlastSorftLeng >= 0) {
											//////////////////////
											byte[] newData = new byte[cutposition - nlastSorftLeng];

											System.arraycopy(soure, nlastSorftLeng, newData, 0, cutposition - nlastSorftLeng);
											System.arraycopy(newData, 0, soure, 0, cutposition - nlastSorftLeng);
											cutposition -= nlastSorftLeng;
										} else {
											cutposition = 0;
										}
									} else {
										break;
									}
								} else {
									break;
								}
							}

							long a2 = SystemClock.elapsedRealtime();
//							Log.i(TAG, "time:++++++++++++++++ ===============" + (a2 - a1) + "=========" + cutposition);
							try {
									Thread.sleep(5);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}


						}


					}
                    //System.out.println(new String(byteBuffer,0,temp));
                    outputStream.flush();
                    socket.close();
                    serverSocket.close();

                }catch(IOException e){
                    e.printStackTrace();
                }finally {

				}
            }
        }.start();
    }

public void vedioRecode(final short[] pcm){
		new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				G711Code.G711aDecoder(pcm,receveAudio,VEDIO_DATALENG);
				AudioReader.playAudioTrack(pcm,0,VEDIO_DATALENG);
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
	public static byte[] intToByteArray(int value) {
		byte[] src = new byte[4];
		src[3] =  (byte) ((value>>24) & 0xFF);
		src[2] =  (byte) ((value>>16) & 0xFF);
		src[1] =  (byte) ((value>>8) & 0xFF);
		src[0] =  (byte) (value & 0xFF);
		return src;
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



