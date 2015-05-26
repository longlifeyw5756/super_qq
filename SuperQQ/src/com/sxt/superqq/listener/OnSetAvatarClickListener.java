package com.sxt.superqq.listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.sxt.superqq.R;
import com.sxt.superqq.SuperQQApplication;
import com.sxt.superqq.activity.RegisterActivity;
/**
 *  * 1-显示PopuWindow
	 *2-拍照的事件监听
	 *3-从相册选取的事件监听
	 *4-拍照或相册选取之后，启动裁剪
	 *5-裁剪之后显示头像、保存头像至sd卡
 * @author yao
 */
public class OnSetAvatarClickListener implements OnClickListener {
	/**PopuWindow宿主Acitivty对象*/
	Activity mActivity;
	/**PopuWinow宿主Activity的布局*/
	View mParentLayout;
	/** 显示弹出窗口：拍照、相册选取*/
	PopupWindow mPopuWindow;
	
	/** 启动系统拍照Activity的请求码*/
	public static final int REQUEST_TAKE_PHOTO=1;
	/** 启动从相册选取的Activity的请求码*/
	public static final int REQEUST_CHOOS_PHOTO=2;
	/** 启动系统用于裁剪照片的Activity的请求码*/
	public static final int REQUEST_CROP_PHOTO=3;
	
	/** R.layout.popu_show_avatar */
	View mLayout;
	public OnSetAvatarClickListener(Activity mActivity, View mParentLayout) {
		super();
		this.mActivity = mActivity;
		this.mParentLayout = mParentLayout;
		//创建显示弹出窗口的View对象
		mLayout=View.inflate(mActivity, R.layout.popu_show_avatar, null);
	}

	/*1-显示PopuWindow样式的弹出窗口
	 *2-设置拍照单击事件的监听
	 *3-设置从相册选取的单击事件监听
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_avatar://若点击的是显示头像的view
			createPopAvatar();
			break;
		case R.id.btn_take_picture://若单击的是拍照的view
			takePhoto();//启动拍照的Activity
			break;
		case R.id.btn_choose_photo://若单击的是相册选取的view
			choosePhoto();
			break;
		}
	}

	/**
	 * 启动系统从相册选取图片的Activity
	 */
	private void choosePhoto() {
		Intent intent=new Intent(Intent.ACTION_PICK);
		intent.setDataAndType(Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		mActivity.startActivityForResult(intent, REQEUST_CHOOS_PHOTO);
	}

	/**
	 * 启动拍照的Activity
	 */
	private void takePhoto() {
		//设置启动拍照Activity的action
		Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//设置拍照保存的路径
		File dir=mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		String userName=null;
		if(mActivity instanceof RegisterActivity){
			userName=((RegisterActivity)mActivity).getUserName();
		}else{//登陆成功以后，在修改头像的Acvitity
			//从首选项中获取登陆者的账号
			userName=SuperQQApplication.getInstance().getUserName();
		}
		if(!TextUtils.isEmpty(userName)){
			//创建保存图片的文件对象，不能加文件的扩展名，否则无法启动裁剪的Activity
			File file=new File(dir, userName);
			Uri uri=Uri.fromFile(file);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			//启动系统的拍照Acitvity，并要求返回拍照结果
			mActivity.startActivityForResult(intent, REQUEST_TAKE_PHOTO);
		}
	}

	/**
	 * 弹出窗口，供选择拍照或相册选取
	 */
	private void createPopAvatar() {
		//创建弹出窗口，宽度：占满服务器一行、高度：180px
		mPopuWindow=new PopupWindow(mLayout, getScreenDisplay().widthPixels, (int)(90*getScreenDisplay().density));
		//设置弹出窗口可以触摸
		mPopuWindow.setTouchable(true);
		//设置弹出窗口可以获得焦点
		mPopuWindow.setFocusable(true);
		//设置缺省的背景颜色，必须设置，否则弹出窗口不能关闭
		mPopuWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopuWindow.setAnimationStyle(R.style.styles_show_avatar);
		//从底部弹出窗口
		mPopuWindow.showAtLocation(mParentLayout, Gravity.BOTTOM, 0, 0);
		
		//设置拍照和相册选取两个view的单击事件监听
		mLayout.findViewById(R.id.btn_take_picture).setOnClickListener(this);
		mLayout.findViewById(R.id.btn_choose_photo).setOnClickListener(this);
	}

	/**
	 * 获取表示屏幕尺寸、密度等信息的对象
	 * @return
	 */
	private DisplayMetrics getScreenDisplay() {
		//创建用于获取屏幕尺寸、像素密度的对象
		Display defaultDisplay = mActivity.getWindowManager().getDefaultDisplay();
		//创建用于获取屏幕尺寸、像素密度等信息的对象
		DisplayMetrics outMetrics=new DisplayMetrics();
		defaultDisplay.getMetrics(outMetrics);
		return outMetrics;
	}

	/**
	 * 启动系统裁剪照片的Activity
	 * @param uri：照片的位置的Uri格式
	 * @param outputX：裁剪后的宽度
	 * @param outputY：裁剪后的高度
	 * @param requestCode：请求码
	 * @param isCrop：是否裁剪,true:裁剪
	 */
	public void startCropPhoto(Uri uri,int outputX,int outputY,int requestCode,boolean isCrop){
		Intent intent=null;
		if(isCrop){// 若想裁剪
			intent=new Intent("com.android.camera.action.CROP");
		}else{//不想裁剪
			intent=new Intent(Intent.ACTION_GET_CONTENT);
		}
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		
		intent.putExtra("scale", true);
		
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra("return-data", true);
		intent.putExtra("ouputFormat", Bitmap.CompressFormat.JPEG.toString());
		
		//启动系统裁剪照片的Activity
		mActivity.startActivityForResult(intent, requestCode);
	}
	
	/**
	 * 返回指定账号的头像的路径
	 * @param userName：账号
	 * @return
	 */
	public static File getAvatarFile(Activity activity,String userName){
		File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File file=new File(dir, userName);
		return file;
	}
	
	/**
	 * 显示裁剪后的图片，并保存至sd卡指定位置
	 * @param ivAvatar:显示图片的ImageView
	 * @param data：裁剪图片保存的位置
	 * @param userName：账号
	 */
	public void saveCropPhoto(ImageView ivAvatar,Intent data,String userName){
		Bundle bundle = data.getExtras();
		//获取图片
		Bitmap bitmap=bundle.getParcelable("data");
		if(bitmap==null){
			return ;
		}
		ivAvatar.setImageBitmap(bitmap);//显示头像
		File file = getAvatarFile(mActivity,userName);
		if(file.exists()){//删除裁剪之前，不带扩展名的图片文件
			file.delete();
		}
		//给图片文件加上jpg的扩展名
		String path=file.getAbsolutePath()+".jpg";
		file=new File(path);//保存图片的真正的路径
		FileOutputStream out=null;
		try {//保存图片
			out=new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 关闭PopuWindow
	 */
	public void closePopuAvatar(){
		if(mPopuWindow!=null){
			mPopuWindow.dismiss();
		}
	}
}
