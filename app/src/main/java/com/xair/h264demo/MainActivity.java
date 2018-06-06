package com.xair.h264demo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;




//6801



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

	// Video Constants
	private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
	private final static int VIDEO_WIDTH = 1280;
	private final static int VIDEO_HEIGHT = 720;
	private final static int TIME_INTERNAL = 2;
	private final static int HEAD_OFFSET = 0;

	private MyReceiver myReceiver;


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

	public void initDecoder() {

		mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
		MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
				VIDEO_WIDTH, VIDEO_HEIGHT);
		mCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(),
				null, 0);
		mCodec.start();
	}

	int mCount = 0;

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
					byte[] dataB = new byte[count.length-112];
					System.arraycopy(count,112,dataB,0,count.length-112);
					byte[] dataV = new byte[4];
					System.arraycopy(count,96,dataV,0,4);
					Log.i(TAG, "onReceive: "+ByteArrayToInt(dataV));
					if (ByteArrayToInt(dataV) == 1){
						int datacount = dataB.length;
						Log.i(TAG, "onReceive: " + datacount);
						System.out.print(datacount);
						onFrame(dataB, 0, datacount);
						Log.i(TAG, "onReceive: "+count);
					}

				}catch (Exception e){
					Log.i(TAG, "onReceive: "+e);
				}
			}
	}

	public int ByteArrayToInt(byte[] bArr) {
		if(bArr.length!=4){
			return -1;
		}
		return (int) ((((bArr[3] & 0xff) << 24)
				| ((bArr[2] & 0xff) << 16)
				| ((bArr[1] & 0xff) << 8)
				| ((bArr[0] & 0xff) << 0)));
	}
}
