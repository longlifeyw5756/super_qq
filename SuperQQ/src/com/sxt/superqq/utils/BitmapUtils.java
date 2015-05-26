package com.sxt.superqq.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;

public final class BitmapUtils {
	/**
	 * 按指定尺寸转换图片
	 * @param data：图片的二进制数据
	 * @param width：图片的预期宽度
	 * @param height：图片的预期高度
	 * @return Bitmap类型
	 */
	public static Bitmap getBitmap(byte[] data,int width,int height){
		Options options=new Options();
		options.inJustDecodeBounds=true;
		//只获取图片的宽和高
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		int scaleX=options.outWidth/width;
		int scaleY=options.outHeight/height;
		int scale=scaleX;
		if(scale<scaleY){
			scale=scaleY;
		}
		options.inJustDecodeBounds=false;
		options.inSampleSize=scale;
		Bitmap bitmap=BitmapFactory.decodeByteArray(data, 0, data.length,options);
		return bitmap;
	}
	/**
	 * 从本地文件读取图片
	 * @param path：图片文件的本地路径
	 * @return 图片的Bitmap类型
	 */
	public static Bitmap getBitmap(String path){
		File file=new File(path);
		if(!file.exists()){
			return null;
		}
		if(file.length()==0){
			file.delete();
			return null;
		}
		Bitmap bitmap= BitmapFactory.decodeFile(path);
		return bitmap;
	}
	/**
	 *  将图片保存至本地
	 * @param bitmap：图片
	 * @param path：保存的路径
	 * @throws IOException
	 */
	public static void saveBitmap(Bitmap bitmap,String path) throws IOException{
		File file=new File(path);
		if(!file.getParentFile().exists()){
			return ;
		}
		FileOutputStream out=new FileOutputStream(file);
		bitmap.compress(CompressFormat.JPEG, 100, out);
	}
}
