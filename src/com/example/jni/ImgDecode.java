package com.example.jni;

public class ImgDecode {
	static {
		System.loadLibrary("ImgDecode");
	}
	/**
	 * @param inRawData: raw data buffer
	 * @param outImg: image data buffer
	 * @return: image data length
	 */
	public static native int unpackData(byte[] inRawData, byte[] outImg);
}
