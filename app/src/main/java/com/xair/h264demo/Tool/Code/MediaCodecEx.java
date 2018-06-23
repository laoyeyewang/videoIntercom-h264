package com.xair.h264demo.Tool.Code;

import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.os.SystemClock;
import android.media.MediaFormat;

import android.util.Log;
import android.view.Surface;

public class MediaCodecEx extends Object {

	class ShowThread extends Thread {
		
		public ShowThread() {
		}

		@Override
		public void run() {
			int nSleepTimer = 40;
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(nSleepTimer);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//if (bShowing == true)
				//	break;
				if (bShowing == false)
					break;
				if (bInitedDec == false)
					break;
				long nStart = SystemClock.elapsedRealtime();
				if(_decoder==null)
					break;
				// int outputBufferIndex =
				// _decoder.dequeueOutputBuffer(bufferInfo,0);
				 ByteBuffer[] outputBuffers = _decoder.getOutputBuffers();
				 if(outputBuffers==null)
					 break;
				 MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
				int outputBufferIndex = _decoder.dequeueOutputBuffer(bufferInfo, 0);
				if (outputBufferIndex < 5)
					nSleepTimer = 33;
				if (outputBufferIndex > 10)
					nSleepTimer = 25;
				while (outputBufferIndex >= 0) {
					if (bShowing == false)
						break;
					if (bInitedDec == false)
						break;
					// MediaFormat _mediaFormat=_decoder.getOutputFormat();
					// ByteBuffer[] outputBuffers = _decoder.getOutputBuffers();
					// ByteBuffer outputBuffer =
					// outputBuffers[outputBufferIndex];
					//Log.d(TAG, "offerDecoder OutputBufSize:" + bufferInfo.size + " bytes written");
					// If a valid surface was specified when configuring the
					// codec,
					// passing true renders this output buffer to the surface.

					_decoder.releaseOutputBuffer(outputBufferIndex, true);
					if (bShowing == false)
						break;
					outputBufferIndex = _decoder.dequeueOutputBuffer(bufferInfo,0);
					if (outputBufferIndex < 5)
						nSleepTimer = 33;
					if (outputBufferIndex > 5)
						nSleepTimer = 25;
					if (outputBufferIndex > 10)
						nSleepTimer = 15;
					try {
						Thread.sleep(nSleepTimer);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				nSleepTimer = 40 - (int) (SystemClock.elapsedRealtime() - nStart);
				if (nSleepTimer < 0)
					nSleepTimer = 1;
				if (nSleepTimer > 40)
					nSleepTimer = 40;

			}

		}
	}
	private static final String TAG = "TvWallServer";
	private static final int FRAME_RATE = 25;
	public MediaCodec _decoder = null;
	public Surface _surface = null;
	public String _mime = "video/avc";
	public int _width = 1280;
	public int _height = 720;
	private int mFrameIndex = 0;

	int nTimes = 0;
	int mTrans = 0;
	String PathFileName = "";
	boolean bThreadDecodeRuning = false;
	byte[] NalBufEx; // 40k
	byte[] SockBuf;
	public boolean bInitedDec = false;
	public boolean bShowing = false;
	///////////////////////////////
	///////////////////////////////
	// 用来记录，当前解码通道，连接的是哪个流媒体服务器的那个通道
	public int nStreamServerNum = 0;
	public int nStreamServerGetChl = 0;
	public int nDecodeChl = 0;// 全局解码通道
	public boolean bPreviewed;// 是否正在预览,如果是，2个开启的流不能使用同一个预览窗体
	////////////////////////////////
	int NalBufUsed = 0;
	int SockBufUsed = 0;
	int nalLen = 0;
	boolean bFirst = true;
	boolean bFindPPS = true;
	/////////////////////////// 文件读取
	int nOneFrameMaxSize = 200000;
	int nReadFileSizeOneTime = 50120;

	////////////////////////// 异步显示线程
	ShowThread showThread;

	boolean bInitedDecBuffer = false;

	@SuppressLint("NewApi")
	public boolean DeInitDecoder() 
	{
		
		bInitedDec = false;
		showThread.interrupt();
		showThread=null;
		_decoder.stop();
		_decoder.release();
		_decoder=null;
		NalBufUsed = 0;
		SockBufUsed = 0;
		nalLen = 0;
		bFirst = true;
		bFindPPS = true;
		nTimes = 0;
		mTrans = 0x0F0F0F0F;
		return true;
	}

	public void InitBuffer() {
		if (!bInitedDecBuffer) {
			NalBufEx = new byte[nOneFrameMaxSize]; // 200k
			SockBuf = new byte[nReadFileSizeOneTime];
			mTrans = 0x0F0F0F0F;
			bThreadDecodeRuning = false;
			bInitedDecBuffer = true;
		}
		// 下面代码，相当于清空了帧缓冲
		NalBufUsed = 0;
		SockBufUsed = 0;
		nalLen = 0;
		bFirst = true;
		bFindPPS = true;
	}

	@SuppressLint("NewApi")
	public boolean InitDecoder(Surface surface, String mime, int width, int height) {
		if (surface == null)
			return false;
		_surface = surface;
		if (mime.length() > 0)
			_mime = mime;

		if (width > 0)
			_width = width;
		if (height > 0)
			_height = height;
		Log.d(TAG, "setupDecoder surface:" + _surface + " mime:" + _mime + " w:" + _width + " h:" + _height);

		MediaFormat mediaFormat = MediaFormat.createVideoFormat(_mime, _width, _height);
		_decoder = MediaCodec.createDecoderByType(_mime);
		if (_decoder == null) {
			Log.e("DecodeActivity", "createDecoderByType fail!");
			return false;
		}
		_decoder.configure(mediaFormat, _surface, null, 0);
		_decoder.start();
		showThread = new ShowThread();
		showThread.start();
		bInitedDec = true;
		return true;
	}

	public boolean InitDecoderEx() {
		InitDecoder(_surface, _mime, _width, _height);
		return true;
	}

	@SuppressLint("NewApi")
	public void InputDataToDecoder(byte[] input, int length) {
		if (bInitedDec==false) {
			if (_surface != null && _width > 0 && _height > 0 && _mime.length() > 0) {
				if (!InitDecoder(_surface, _mime, _width, _height))
					return;
			}
		}
		//String str = "InputDataToDecoder：" + length + "Data:" + input[0] + input[1] + input[2] + input[3];
		//OutputDebugString(str);
		if (_surface == null)
			return;
		if (_decoder == null)
			return;
		if (bShowing == false)
			return;
	//	OutputDebugString("InputDataToDecoder try decodeing");
		try {
			ByteBuffer[] inputBuffers = _decoder.getInputBuffers();
			if(inputBuffers==null) {
				return;
			}
			 int inputBufferIndex = _decoder.dequeueInputBuffer(-1);
			//int inputBufferIndex = _decoder.dequeueInputBuffer(150);// 缓冲150ms
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				if(inputBuffer==null)
					return;
				long timestamp = mFrameIndex++ * 1000000 / FRAME_RATE;
				inputBuffer.clear();
				inputBuffer.put(input, 0, length);
				_decoder.queueInputBuffer(inputBufferIndex, 0, length, timestamp, 0);
				inputBuffer.clear();
				Log.d(TAG, "offerDecoder timestamp: " + timestamp + " inputSize: " + length + " bytes");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	////////////////// 数据送入///////////////////////////////////////////////////////////////////
	////////////////// 数据送入///////////////////////////////////////////////////////////////////
	/////////////////// 数据送入//////////////////////////////////////////////////////////////////
	////////////////// 数据送入///////////////////////////////////////////////////////////////////
	/////////////// 数据送入//////////////////////////////////////////////////////////////////////
	// 外部输入并且解码，有可能不是一帧 不带线程终止判断
	public void InputVideoEx(byte[] Buffer, int nBufSize) {
		if (bShowing == false)
			return;
		if (nBufSize < 1)
			return;
		for (int i = 0; i < nBufSize; i++) {
			if (Buffer[i] < 0)
				Buffer[i] &= 0xff;
		}

		String str = "InputVideo nBufSize:" + nBufSize;
		//////////////////////////////////////////////////////////////////////
		SockBufUsed = 0;
		// OutputDebugString(str);
		while (nBufSize - SockBufUsed > 0) {
			nalLen = MergeBuffer(NalBufEx, NalBufUsed, Buffer, SockBufUsed, nBufSize - SockBufUsed);
			NalBufUsed += nalLen;
			SockBufUsed += nalLen;
			while (mTrans == 1) {
				mTrans = 0xFFFFFFFF;
				if (bFirst == true) // the first start flag
				{
					bFirst = false;
				} else // a complete NAL data, include 0x00000001 trail.
				{
					if (bFindPPS == true) // true
					{
						if ((NalBufEx[4] & 0x1F) == 7) {
							bFindPPS = false;
						} else {
							NalBufEx[0] = 0;
							NalBufEx[1] = 0;
							NalBufEx[2] = 0;
							NalBufEx[3] = 1;
							NalBufUsed = 4;
							break;
						}
					}
					InputDataToDecoder(NalBufEx, NalBufUsed - 4);
				}
				NalBufEx[0] = 0;
				NalBufEx[1] = 0;
				NalBufEx[2] = 0;
				NalBufEx[3] = 1;
				NalBufUsed = 4;
			}
		}
	}

	int MergeBuffer(byte[] NalBuf, int NalBufUsed, byte[] SockBuf, int SockBufUsed, int SockRemain) {
		int i = 0;
		byte Temp;
		for (i = 0; i < SockRemain; i++) {
			Temp = SockBuf[i + SockBufUsed];
			NalBuf[i + NalBufUsed] = Temp;
			mTrans <<= 8;
			mTrans |= Temp;
			if (mTrans == 1) // 找到一个开始字
			{
				i++;
				break;
			}
		}
		return i;
	}

	public boolean OutputDebugString(String str) {
		System.out.println(str);
		return true;
	}

}
