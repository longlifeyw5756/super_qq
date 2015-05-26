package com.sxt.superqq.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**MD5即Message-Digest Algorithm 5（信息-摘要算法5）
 * 用于确保信息传输完整一致。是计算机广泛使用的摘要算法、哈希算法
 * 
 * MessageDigest 类为应用程序提供信息摘要算法的功能，如 MD5 或 SHA 算法。 
 * 信息摘要是安全的单向哈希函数，它接收任意大小的数据，
 * 并输出固定长度的哈希值。
 */
public class MD5 {
	/**
	 * 返回str按MD5算法加密后的字符串。
	 * 
	 * @param str
	 * @return
	 */
	public static String getData(String str) {
		// 创建消息摘要对象，返回实现指定摘要算法的 MessageDigest对象。
		StringBuilder builder = null;
		try {
			//利用特定算法返回消息摘要的一个新实例。
			MessageDigest md = MessageDigest.getInstance("md5");
			
			// 将str转换为字节数组，根据该字节数组计算出哈希值
			byte[] result = md.digest(str.getBytes());
			
			//将字节数组进行处理，转换为字符串
			builder = new StringBuilder();
			for (int i = 0; i < result.length; i++) {
				// 将32位的高24位清零，保留低8位，然后转换为16进制的字符串
				String hex = Integer.toHexString(result[i] & 0xff);
				if (hex.length() == 1) {// 若是1位，则前面加0
					builder.append("0").append(hex);
				} else {
					builder.append(hex);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder.toString();
	}
}
