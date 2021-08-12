package cn.qd.willie.mysocket;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
	/**
	 * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
	 */
	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f' };




	/**
	 *
	 * @param file
	 *            需要校验的文件对象。
	 * @return 校验得到的 MD5 值。
	 * @throws IOException
	 *             如果校验过程中发生 IO 错误。
	 */
	public static String getFileMD5StringNIO(File file) throws IOException {
		return getFileMD5StringNIO(file, 1024 * 128);
	}

	/**
	 * MD5。
	 * 
	 * @param file
	 *            需要校验的文件对象。
	 * @param bufSize
	 *            校验时 IO 过程使用的缓存大小。这个值会影响校验过程的时间长短。
	 * @return 校验得到的 MD5 值。
	 * @throws IOException
	 *             如果校验过程中发生 IO 错误。
	 */
	public static String getFileMD5StringNIO(File file, int bufSize) throws IOException {
		MessageDigest messagedigest = null;
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
		if (messagedigest == null) {
			return "";
		}
		FileInputStream fis = new FileInputStream(file);
		FileChannel fChannel = fis.getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(bufSize);
		for (int count = fChannel.read(buffer); count != -1; count = fChannel.read(buffer)) {
			buffer.flip();
			messagedigest.update(buffer);
			if (!buffer.hasRemaining()) {
				buffer.clear();
			}
		}
		fis.close();
		try {
			byte[] digest = messagedigest.digest();
			return bufferToHex(digest);
		} catch (Exception e) {
			throw new IOException(e.toString());
		}
	}

	/**
	 * 计算二进制数据的 MD5 值。
	 * 
	 * @param bytes
	 *            需要校验的二进制数据。
	 * @return 校验得到的 MD5 值。
	 */
	public static String getMD5String(byte[] bytes) {
		MessageDigest messagedigest = null;
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {

		}
		if (messagedigest == null) {
			return "";
		}
		messagedigest.update(bytes);
		try {
			byte[] digest = messagedigest.digest();
			return bufferToHex(digest);
		} catch (Exception e) {
			return "";
		}
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换, >>>
												// 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
		char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	public static String encodeImage(Bitmap bitmap){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//读取图片到ByteArrayOutputStream
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //参数如果为100那么就不压缩
		byte[] bytes = baos.toByteArray();
		return Base64.encodeToString(bytes,Base64.NO_WRAP|Base64.NO_PADDING);
	}

	/**
	 * 将图片转换成Base64编码的字符串
	 * @param path
	 * @return base64编码的字符串
	 */
	public static String imageToBase64(String path){
		if(TextUtils.isEmpty(path)){
			return null;
		}
		InputStream is = null;
		byte[] data = null;
		String result = null;
		try{
			is = new FileInputStream(path);
			//创建一个字符流大小的数组。
			data = new byte[is.available()];
			//写入数组
			is.read(data);
			//用默认的编码格式进行编码
			result = Base64.encodeToString(data,Base64.NO_WRAP);
		}catch (IOException e){
			e.printStackTrace();
		}finally {
			if(null !=is){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return result;
	}

	/**
	 * bitmap转为base64
	 * @param bitmap
	 * @return
	 */
	public static String bitmapToBase64(Bitmap bitmap) {

		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

				baos.flush();
				baos.close();

				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static String decode(String str){
		return new String(Base64.decode(str.getBytes(), Base64.DEFAULT));
	}
}
