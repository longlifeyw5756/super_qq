package com.sxt.superqq.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

public final class EmotionUtils {
	/**
	 * 将text中包含ee_数字的文本转换为表情图片
	 * 
	 * @param text
	 *            :ee_01 helloee_02okee_08hiee_02
	 * @return
	 */
	public static CharSequence replace(Context context, CharSequence text) {
		// 定义监测ee_xx的正则表达式的模板
		Pattern pattern = Pattern.compile("ee_[\\d]{1,2}");
		// 监测text，结果：matcher
		Matcher matcher = pattern.matcher(text);
		// 创建用于插入表情图片的对象
		SpannableString ss = new SpannableString(text);
		/** 查找key在text出现位置的起始点 */
		int start = 0;
		while (matcher.find()) {// 若有符合条件的文本
			// 获得一个符合条件的文本
			String key = matcher.group();
			Resources res = context.getResources();
			// 获取key表示的图片的资源id
			int id = res.getIdentifier(key, "drawable",
					context.getPackageName());
			// 拿到key表示的表情图片的位图格式
			Bitmap bitmap = BitmapFactory.decodeResource(res, id);
			// 创建用于插入图片的对象
			ImageSpan imageSpan = new ImageSpan(bitmap);
			// 查找key在text中出现的位置,startIndex是key在text中出现的位置
			int startIndex = text.toString().indexOf(key, start);
			ss.setSpan(imageSpan, startIndex, startIndex + key.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			// 累加start的值
			start = startIndex + key.length();
		}
		return ss;
	}
}
