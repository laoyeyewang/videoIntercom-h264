package com.xair.h264demo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

import android.annotation.TargetApi;
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
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;



//udp
//6801
//tcp
//8101 192.168.2.140


public class MainActivity extends Activity {
	//tag 标记
	protected static final String TAG = MainActivity.class.getSimpleName();


	private String h264Path = "/mnt/sdcard/720pq.h264";
	private File h264File = new File(h264Path);
	private InputStream is = null;
	private FileInputStream fs = null;

	private SurfaceView mSurfaceView;
	private Button mReadButton;
	private MediaCodec mCodec;

	Thread readFileThread;
	boolean isInit = false;
    //AudioPlayer audioPlayer;

	// Video Constants
	private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
	private final static int VIDEO_WIDTH = 1280;
	private final static int VIDEO_HEIGHT = 720;
	private final static int TIME_INTERNAL = 2;
	private final static int HEAD_OFFSET = 0;

	private MyReceiver myReceiver;

	AudioTrack player=null;
	int bufferSize=0;//最小缓冲区大小
    AudioRecord audioRecord=null;
	//int sampleRateInHz = 11025;//采样率
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO; //单声道
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT; //量化位数

    byte[] receveAudio = new byte[1312];
    int audioindex = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//启动服务
		Intent intent = new Intent(MainActivity.this,UdpReceiver.class);
		startService(intent);
		//注册广播接收器
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.weiyuzk.UdpReceiver");
		registerReceiver(myReceiver, filter);

		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);

//		readFileThread = new Thread(readFile);
//		readFileThread.start();
		// 初始化线程池
		TCPSend.mThreadPool = Executors.newCachedThreadPool();
		TCPSend.connectThread();

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
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initDecoder() {

		mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
		MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
				VIDEO_WIDTH, VIDEO_HEIGHT);
		mCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(),
				null, 0);
		mCodec.start();
	}

	int mCount = 0;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean onFrame(byte[] buf, int offset, int length) {

		// Get input buffer index
		ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
		int inputBufferIndex = mCodec.dequeueInputBuffer(100);
//		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(buf, offset, length);

			mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount
					* TIME_INTERNAL, 0);
			mCount++;
//		} else {
//			return false;
//		}

		// Get output buffer index
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 100);
		while (outputBufferIndex >= 0) {
			mCodec.releaseOutputBuffer(outputBufferIndex, true);
			outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 100);
		}
		Log.e("Media", "onFrame end");
		return true;
	}

	/**
	 * Find H264 frame head
	 * 
	 * @param buffer
	 * @param len
	 * @return the offset of frame head, return 0 if can not find one
	 */
	static int findHead(byte[] buffer, int len) {
		int i;
		for (i = HEAD_OFFSET; i < len; i++) {
			if (checkHead(buffer, i))
				break;
		}
		if (i == len)
			return 0;
		if (i == HEAD_OFFSET)
			return 0;
		return i;
	}

	/**
	 * Check if is H264 frame head
	 * 
	 * @param buffer
	 * @param offset
	 * @return whether the src buffer is frame head
	 */
	static boolean checkHead(byte[] buffer, int offset) {
		// 00 00 00 01
		if (buffer[offset] == 0 && buffer[offset + 1] == 0
				&& buffer[offset + 2] == 0 && buffer[3] == 1)
			return true;
		// 00 00 01
		if (buffer[offset] == 0 && buffer[offset + 1] == 0
				&& buffer[offset + 2] == 1)
			return true;
		return false;
	}


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
					System.arraycopy(count,96,dataV,0,4);
					Log.i(TAG, "onReceive: "+ByteArrayToInt(dataV));
					if (ByteArrayToInt(dataV) == 1){
                        byte[] dataB = new byte[count.length-112];
                        System.arraycopy(count,112,dataB,0,count.length-112);
						int datacount = dataB.length;
						Log.i(TAG, "onReceive: " + datacount);
						System.out.print(datacount);
						onFrame(dataB, 0, datacount);
						Log.i(TAG, "onReceive: "+count);
					}else if (ByteArrayToInt(dataV) == 2) {
                       byte[] dataA = new byte[count.length-112];
                        System.arraycopy(count,112,dataA,0,count.length-112);
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
	 * byte转int
	 */

	public int ByteArrayToInt(byte[] bArr) {
		if(bArr.length!=4){
			return -1;
		}
		return (int) ((((bArr[3] & 0xff) << 24)
				| ((bArr[2] & 0xff) << 16)
				| ((bArr[1] & 0xff) << 8)
				| ((bArr[0] & 0xff) << 0)));
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
	public void playVudio(byte[] b){
		//最小缓存区
		int bufferSizeInBytes= AudioTrack.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
		//创建AudioTrack对象   依次传入 :流类型、采样率（与采集的要一致）、音频通道（采集是IN 播放时OUT）、量化位数、最小缓冲区、模式
		player=new AudioTrack(AudioManager.STREAM_MUSIC,sampleRateInHz,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
		byte[] data =new byte [bufferSizeInBytes];
		//byte[] data =new byte [320];
		player.play();//开始播放
			System.arraycopy(b,0,data,0,b.length);
			short[] pcm = new short[1312];
			com.example.test.G711Code.G711aDecoder(pcm,data,1312);
			//player.write(data,0,data.length);
			player.write(pcm,0,pcm.length);
			player.stop();
//			if(i!=bufferSizeInBytes) //表示读取完了
//			{
//				player.stop();//停止播放
//				player.release();//释放资源
//				break;
//			}
//		}
	}
    /**
     *
     * 录音
     */
	public void startAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig, audioFormat);//计算最小缓冲区
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRateInHz,channelConfig, audioFormat, bufferSize);//创建AudioRecorder对象
                byte[] buffer = new byte[bufferSize];
                audioRecord.startRecording();//开始录音
                int bufferReadResult = audioRecord.read(buffer,0,bufferSize);
                Log.i(TAG, "run: "+bufferReadResult);
            }
        }).start();
    }
}

