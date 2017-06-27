/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api.sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ivsign.android.IDCReader.IDCReaderSDK;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android_serialport_api.SerialPort;

public class TestActivity extends Activity implements SoundPool.OnLoadCompleteListener {
	public static final String	TAG							= "TestActivity";

	public static final String	SDPATH						= Environment.getExternalStorageDirectory().getPath();

	public static final byte[]	mSearchCardCmd				= {(byte) 0x17,(byte) 0x17,(byte) 0x00,(byte) 0x0C,(byte) 0x6A,(byte) 0xAA,(byte) 0xAA,(byte) 0xAA,(byte) 0x96,(byte) 0x69,(byte) 0x00,(byte) 0x03,(byte) 0x20,(byte) 0x01,(byte) 0x22,(byte) 0x0D};
	public static final byte[]	mSelectCardCmd				= {(byte) 0x17,(byte) 0x17,(byte) 0x00,(byte) 0x0C,(byte) 0x6A,(byte) 0xAA,(byte) 0xAA,(byte) 0xAA,(byte) 0x96,(byte) 0x69,(byte) 0x00,(byte) 0x03,(byte) 0x20,(byte) 0x02,(byte) 0x21,(byte) 0x0D};
	public static final byte[]	mReadCardCmd				= {(byte) 0x17,(byte) 0x17,(byte) 0x00,(byte) 0x0C,(byte) 0x6A,(byte) 0xAA,(byte) 0xAA,(byte) 0xAA,(byte) 0x96,(byte) 0x69,(byte) 0x00,(byte) 0x03,(byte) 0x30,(byte) 0x01,(byte) 0x32,(byte) 0x0D};
	//public static final byte[]	mReadCardFingerCmd			= {(byte) 0x17,(byte) 0x17,(byte) 0x00,(byte) 0x0C,(byte) 0x6A,(byte) 0xAA,(byte) 0xAA,(byte) 0xAA,(byte) 0x96,(byte) 0x69,(byte) 0x00,(byte) 0x03,(byte) 0x30,(byte) 0x10,(byte) 0x23,(byte) 0x0D};
	public static final byte[]	mStartModuleCmd				= {(byte) 0x17,(byte) 0x17,(byte) 0x00,(byte) 0x05,(byte) 0x9A,(byte) 0x02,(byte) 0x05,(byte) 0x01,(byte) 0x06};
	public static final byte[]	mShutModuleCmd				= {(byte) 0x17,(byte) 0x17,(byte) 0x00,(byte) 0x05,(byte) 0x9A,(byte) 0x02,(byte) 0x05,(byte) 0x00,(byte) 0x06};
	//public static final byte[]	mPowerLevelCmd				= {(byte) 0x17,(byte) 0x17,(byte) 0x00,(byte) 0x04,(byte) 0x9A,(byte) 0x01,(byte) 0x02,(byte) 0x05};

	//public static final byte[]	mSearchCardCmd1				= {(byte) 0xAA,(byte) 0xAA,(byte) 0xAA,(byte) 0x96,(byte) 0x69,(byte) 0x00,(byte) 0x03,(byte) 0x20,(byte) 0x01,(byte) 0x22};
	//public static final byte[]	mSelectCardCmd1				= {(byte) 0xAA,(byte) 0xAA,(byte) 0xAA,(byte) 0x96,(byte) 0x69,(byte) 0x00,(byte) 0x03,(byte) 0x20,(byte) 0x02,(byte) 0x21};
	//public static final byte[]	mReadCardCmd1				= {(byte) 0xAA,(byte) 0xAA,(byte) 0xAA,(byte) 0x96,(byte) 0x69,(byte) 0x00,(byte) 0x03,(byte) 0x30,(byte) 0x01,(byte) 0x32};
	//public static final byte[]	mResetModuleCmd1			= {(byte) 0xAA,(byte) 0xAA,(byte) 0xAA,(byte) 0x96,(byte) 0x69,(byte) 0x00,(byte) 0x03,(byte) 0x10,(byte) 0xFF,(byte) 0xEC};

	public static final int		RET_SEARCH_CARD_OK			= 1;
	public static final int		RET_SEARCH_CARD_FAIL		= 2;
	public static final int		RET_SELECT_CARD_OK			= 3;
	public static final int		RET_SELECT_CARD_FAIL		= 4;
	public static final int		RET_READ_CARD_OK			= 5;
	public static final int		RET_READ_CARD_FAIL			= 6;
	public static final int		RET_MODULE_OPEN_OK			= 7;
	public static final int		RET_MODULE_OPEN_FAIL		= 8;
	public static final int		RET_POWER_LEVEL_OK			= 9;
	public static final int		RET_POWER_LEVEL_FAIL		= 10;
	public static final int		RET_CMD_ERR					= 11;

	public static final int		MESSAGE_REC_DATA_TIMEOUT	= 7;
	public static final int		MESSAGE_REC_DATA_OK			= 8;
	public static final int		MESSAGE_REC_DATA_FAIL		= 9;
	public static final int		MESSAGE_REC_DATA_NOT_READY	= 10;

	private int					rec_data_state				= 0;
	private int					rec_index_pos				= 0;
	private int					rec_cmd_len					= 0;
	private Myttt				ttt;
	private int					level						= 0;
	private int					level_min					= 100;
	private boolean				isload						= false;

	ImageView					mCardImg;

	private TextView			name, sex, mingzu, year, month, day, address, cid, jiguan, qixiao, staus;

	IDCReaderSDK				mIDCReaderSDK				= new IDCReaderSDK();
	SoundPool					soundPool;
	int							soundId;

	byte[]						mBuffer;
	// 2327
	byte[]						mIDCBuffer					= new byte[4096];

	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		isload = true;
	}

	void findView() {
		mCardImg = (ImageView) findViewById(R.id.imageView1);

		name = (TextView) findViewById(R.id.textView6);
		sex = (TextView) findViewById(R.id.textView11);
		mingzu = (TextView) findViewById(R.id.textView16);
		year = (TextView) findViewById(R.id.textView12);
		month = (TextView) findViewById(R.id.textView13);
		day = (TextView) findViewById(R.id.textView17);
		address = (TextView) findViewById(R.id.textView15);
		cid = (TextView) findViewById(R.id.textView10);
		jiguan = (TextView) findViewById(R.id.textView20);
		qixiao = (TextView) findViewById(R.id.textView21);
		staus = (TextView) findViewById(R.id.textView22);

		staus.setText("未检测到设备信息");
	}

	void setInfo(byte[] info) {
		// 声音
		if (isload) {
			soundPool.play(soundId, 1.0f, 0.5f, 1, 0, 1.5f);
		}
		// 解码图片：
		mIDCReaderSDK.DoDecodeCardInfo(info);

		String data1 = mIDCReaderSDK.GetPeopleBirthday();

		String data2 = mIDCReaderSDK.GetStartDate();
		data2 = data2.substring(0, 4) + "." + data2.substring(4, 6) + "." + data2.substring(6, 8) + "";

		String data3 = mIDCReaderSDK.GetEndDate();
		data3 = data3.substring(0, 4) + "." + data3.substring(4, 6) + "." + data3.substring(6, 8) + "";

		name.setText(mIDCReaderSDK.GetPeopleName());
		sex.setText(mIDCReaderSDK.GetPeopleSex());
		mingzu.setText(mIDCReaderSDK.GetPeopleNation());
		year.setText(data1.substring(0, 4));
		month.setText(data1.substring(4, 6));
		day.setText(data1.substring(6, 8));
		address.setText(mIDCReaderSDK.GetPeopleAddress());
		qixiao.setText(mIDCReaderSDK.GetDepartment());
		jiguan.setText(data2 + "-" + data3);

		cid.setText(mIDCReaderSDK.GetPeopleIDCode());

		Bitmap bitmap = BitmapFactory.decodeFile(SDPATH + "/wltlib/zp.bmp");
		mCardImg.setImageBitmap(bitmap);
	}

	class RfidReadThread extends Thread {

		@Override
		public void run() {
			while (!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[32];
					if (mInputStream == null)
						return;
					size = mInputStream.read(buffer);
					Log.d("read", "size"+size);
					
					 StringBuffer bb=new StringBuffer(); for (byte b : buffer)
					  { bb.append(byteToHex(b)); } Log.e("read",
					  "test:"+size+" = " +bb);
					 

					if (size > 0) {
						onRfidDataReceived(buffer, size);
					}
					/*byte[] b= new byte[1024];
			        int count = 0;
			     if(mInputStream.available()>0 == false){
			      continue;
			     }else{
			      Thread.sleep(200);
			      }
			     count = mInputStream.read(b);*/
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	protected SerialPort mRfidPort;
	protected OutputStream mRfidOutputStream;
	protected InputStream mInputStream;
	protected RfidReadThread mRfidReadThread;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		File file = new File("/sys/devices/soc.0/78d9000.usb/backboard_power_enable");
		if (file.exists()) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(file));
				
				out.write("enable");
				out.close();
			} catch (IOException e) {
				Log.e(TAG, "enable failed", e);
			}
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			mRfidPort = new SerialPort(new File("/dev/ttyHSL1"), 115200, 0);
			mRfidOutputStream = mRfidPort.getOutputStream();
			mInputStream = mRfidPort.getInputStream();
			
			mRfidReadThread = new RfidReadThread();
			mRfidReadThread.start();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setContentView(R.layout.main2);
		new Thread() {
			public void run() {
				CopyAssets("wltlib", SDPATH + "/wltlib");
			};
		}.start();
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(this);
		soundId = soundPool.load(this, R.raw.bb, 1);
		findView();
		startRfidSendingThread(mStartModuleCmd, mStartModuleCmd.length);
	}

	@Override
	protected void onDestroy() {
		startRfidSendingThread(mShutModuleCmd, mShutModuleCmd.length);
		soundPool.release();
		soundPool = null;
		if (mRfidReadThread != null)
			mRfidReadThread.interrupt();
		if (mRfidPort != null) {
			mRfidPort.close();
			mRfidPort = null;
		}
		File file = new File("/sys/devices/soc.0/78d9000.usb/backboard_power_enable");
		if (file.exists()) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("/sys/devices/soc.0/78d9000.usb/backboard_power_enable"));
				out.write("disable");
				out.close();
			} catch (IOException e) {
				Log.e(TAG, "disable failed", e);
			}
		}
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (ttt != null) {
			ttt.interrupt();
		}
	}

	private void startRfidSendingThread(final byte[] buffer, final int len) {
		mBuffer = new byte[len];
		for (int i = 0; i < len; i++) {
			mBuffer[i] = buffer[i];
		}
		if (mRfidPort != null) {
			if (mRfidOutputStream != null) {
				try {
					mRfidOutputStream.write(mBuffer);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	protected void onRfidDataReceived(byte[] buffer, int size) {
		// 接收来自mcu的数据(led)
		final int len = size;
		final byte[] tempbuffer = buffer;

		runOnUiThread(new Runnable() {
			public void run() {
				if (rec_data_state != RET_READ_CARD_OK) {
					rec_cmd_len = Check_Data(tempbuffer, len);
					Log.d("usloy", "received len = " + rec_cmd_len);

					// switch(rec_data_state){
					if (rec_cmd_len != 0) {
						// Check_Cmd(temp, len, cmd_len);

						rec_data_state = Check_Cmd(tempbuffer, len, rec_cmd_len);
						switch (rec_data_state) {
							case 0 :
								Log.e("usloy", "cmd not inited = 0");
								break;
							case RET_MODULE_OPEN_OK :
								ttt = new Myttt();
								ttt.start();
								// startRfidSendingThread(mSearchCardCmd,
								// mSearchCardCmd.length);
								Log.d("usloy", "cmd module ok");
								staus.setText("打开设备成功");
								break;
							case RET_POWER_LEVEL_OK :
								if (level_min <= 2) {
									ttt.interrupt();
									staus.setText("设备电量不足：" + level_min);
								} else {
									startRfidSendingThread(mSearchCardCmd, mSearchCardCmd.length);
								}
								Log.d("usloy", "cmd level ok");
								break;
							case RET_POWER_LEVEL_FAIL :
								Log.d("usloy", "cmd level fail");
								staus.setText("读取电量失败");
								break;
							case RET_SEARCH_CARD_OK :

								startRfidSendingThread(mSelectCardCmd, mSelectCardCmd.length);
								Log.d("usloy", "cmd search ok");

								// getWindow().setTitle("寻卡成功");
								staus.setText("寻卡成功");
								break;
							case RET_SEARCH_CARD_FAIL :
								Log.e("usloy", "cmd search fail");
								// printByte("Rfid received", tempbuffer, len);
								// getWindow().setTitle("寻卡中...");
								staus.setText("寻卡中...");
								break;
							case RET_SELECT_CARD_OK :

								// startRfidSendingThread(mReadCardCmd,
								// mReadCardCmd.length);
								startRfidSendingThread(mReadCardCmd, mReadCardCmd.length);
								Log.d("usloy", "cmd select ok");
								// getWindow().setTitle("选卡成功");
								staus.setText("选卡成功");
								break;
							case RET_SELECT_CARD_FAIL :

								Log.e("usloy", "cmd select fail");
								// getWindow().setTitle("选卡失败");
								staus.setText("选卡失败");
								break;
							case RET_READ_CARD_OK :
								Log.d("usloy", "cmd read ok");
								System.arraycopy(tempbuffer, 0, mIDCBuffer, rec_index_pos, len);
								rec_index_pos += len;
								// getWindow().setTitle("读卡成功");
								staus.setText("读卡成功");
								break;
							case RET_READ_CARD_FAIL :

								Log.e("usloy", "cmd read fail");
								// getWindow().setTitle("读卡失败");
								staus.setText("读卡失败");
								break;
							case RET_CMD_ERR :

								Log.e("usloy", "cmd err");
								// getWindow().setTitle("发送错误指令");
								staus.setText("发送错误指令");
								break;
							default :

								Log.e("usloy", "cmd default");
								// getWindow().setTitle("正在读卡...");
								staus.setText("正在读卡...");

								break;
						}
					}
				} else {
					System.arraycopy(tempbuffer, 0, mIDCBuffer, rec_index_pos, len);
					rec_index_pos += len;
					Log.e("usloy", "rec_index_pos = " + rec_index_pos);
					Log.e("usloy", "rec_cmd_len = " + (rec_cmd_len + 7));
					if (rec_index_pos >= rec_cmd_len + 7) {
						setInfo(mIDCBuffer);
						rec_index_pos = 0;
						Log.e("usloy", "rec_data ok");
						rec_data_state = 0;
					}
					if (rec_index_pos > 2327) {
						rec_index_pos = 0;
					}
				}
			}
		});
	}

	public Boolean Check_Header(byte[] data, int len) {
		if (len < 5) {
			return false;
		}
		switch (data[3]) {
			case 0x0c :
				if ("9b".equals(String.format("%02x", data[4]))) {
					Log.d("Check_Header", "电量数据");
					if (data[5] == (byte) 0x01 && data[6] == (byte) 0x02) {
						return true;
					}
				}
				break;
			case 0x05 :
				if (data[4] == (byte) 0x9B) {
					Log.d("Check_Header", "上电数据");
					if (data[5] == (byte) 0x02 && data[6] == (byte) 0x05 && data[7] == (byte) 0x01) {
						return true;
					}
				}
				break;
			case 0x06 :
				if (data[4] == (byte) 0x6B) {
					Log.d("Check_Header", "二代证数据");
					if (data[5] == (byte) 0xAA && data[6] == (byte) 0xAA && data[7] == (byte) 0xAA && data[8] == (byte) 0x96 && data[9] == (byte) 0x69) {
						return true;
					}
				}
				break;
			default :
				Log.i("Check_Header", "无效数据");
				break;
		}
		return false;
	}

	// return data len when ret = 0 mean fail
	public int Check_Data(byte[] data, int len) {
		int cmd_data_len = 0;
		if (Check_Header(data, len)) {
			if (len > 12) {
				cmd_data_len = ((int) data[10] << 8) + (int) data[11];
				return cmd_data_len;
			} else if (len > 5) {
				return -1;
			} else {
				Log.d("Check_Data", "len");
				return 0;
			}
		} else {
			Log.d("Check_Data", "Check_Header");
			return 0;
		}
	}

	public int Check_Cmd(byte[] data, int len, int cmd_len) {
		if (len == 16) {
			if (data[11] != 0x00) {
				level = data[9] & 0xff;
				if (level < level_min) {
					level_min = level;
				}
			} else {
				level = -1;
			}
			return RET_POWER_LEVEL_OK;
		} else if (len == 9 | cmd_len == -1) {
			return RET_MODULE_OPEN_OK;
		} else if (len < 14 | cmd_len == 0) {
			return 0;
		}

		int ret = 0;

		switch (data[14]) {
			case (byte) 0x9F :
				ret = RET_SEARCH_CARD_OK;
				break;
			case (byte) 0x80 :
				ret = RET_SEARCH_CARD_FAIL;
				break;
			case (byte) 0x90 :
				if (data[11] == (byte) 0x0c) {
					ret = RET_SELECT_CARD_OK;
				} else {// == 0x08
					ret = RET_READ_CARD_OK;
				}
				break;
			case (byte) 0x41 :
				ret = RET_READ_CARD_FAIL;
				break;
			case (byte) 0x10 :
				ret = RET_CMD_ERR;
				break;
			case (byte) 0x81 :
				ret = RET_SELECT_CARD_FAIL;
				break;
			default :
				ret = 0;
				break;
		}
		return ret;
	}

	// 循环读卡
	class Myttt extends Thread {
		public void run() {
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while (!isInterrupted()) {
				if (!new File(SDPATH + "/wltlib").exists()) {

					CopyAssets("wltlib", SDPATH + "/wltlib");
				} else if (new File(SDPATH + "/wltlib").listFiles().length < 4) {

					CopyAssets("wltlib", SDPATH + "/wltlib");
				} else {
					// startRfidSendingThread(mPowerLevelCmd,
					// mPowerLevelCmd.length);
					startRfidSendingThread(mSearchCardCmd, mSearchCardCmd.length);
				}

				try {
					sleep(500);
				} catch (InterruptedException e) {
					break;
				}
			}
		};
	};

	private void CopyAssets(String assetDir, String dir) {
		String[] files;
		try {
			files = this.getResources().getAssets().list(assetDir);
		} catch (IOException e1) {
			return;
		}
		File mWorkingPath = new File(dir);
		// if this directory does not exists, make one.
		if (!mWorkingPath.exists()) {
			if (!mWorkingPath.mkdirs()) {

			}
		}

		for (int i = 0; i < files.length; i++) {
			try {
				String fileName = files[i];
				// we make sure file name not contains '.' to be a folder.
				if (!fileName.contains(".")) {
					if (0 == assetDir.length()) {
						CopyAssets(fileName, dir + fileName + "/");
					} else {
						CopyAssets(assetDir + "/" + fileName, dir + fileName + "/");
					}
					continue;
				}
				File outFile = new File(mWorkingPath, fileName);
				if (outFile.exists())
					outFile.delete();
				InputStream in = null;
				if (0 != assetDir.length())
					in = getAssets().open(assetDir + "/" + fileName);
				else
					in = getAssets().open(fileName);
				OutputStream out = new FileOutputStream(outFile);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String byteToHex(final byte b) {
		StringBuffer buf = new StringBuffer();
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int high = ((b & 0xf0) >> 4);
		int low = (b & 0x0f);
		buf.append(hexChars[high]);
		buf.append(hexChars[low]);
		return buf.toString();
	}

	
}
