/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sxt.superqq.activity;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.sxt.superqq.SuperQQApplication;
import com.sxt.superqq.I;
import com.sxt.superqq.R;
import com.sxt.superqq.bean.UserBean;
import com.sxt.superqq.listener.OnSetAvatarClickListener;
import com.sxt.superqq.utils.NetUtils;

/**
 * 注册页
 * 
 */
public class RegisterActivity extends BaseActivity {
	private EditText userNameEditText;
	private EditText passwordEditText;
	private EditText confirmPwdEditText;
	private EditText metNick;
	private ImageView mivAvatar;
	RegisterActivity mContext;

	OnSetAvatarClickListener mOnSetAvatarListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mContext=this;
		initView();
		setListener();
	}

	private void setListener() {
		setRegisterClickListener();
		setAVatarClickListener();
	}

	/**
	 * 设置选择的头像的单击事件监听
	 */
	private void setAVatarClickListener() {
		//获取RegisterActivity的布局对象
		View layout=View.inflate(this, R.layout.activity_register, null);
		//设置选择头像的事件监听
		mOnSetAvatarListener=new OnSetAvatarClickListener(this,layout);
		findViewById(R.id.iv_avatar).setOnClickListener(mOnSetAvatarListener);
	}

	/**
	 * 设置注册按钮单击事件监听
	 */
	private void setRegisterClickListener() {
		findViewById(R.id.btn_reigster).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String username = userNameEditText.getText()
						.toString().trim();
				final String pwd = passwordEditText.getText()
						.toString().trim();
				String confirm_pwd = confirmPwdEditText.getText()
						.toString().trim();
				if (TextUtils.isEmpty(username)) {
					// 显示账号不能为空
					userNameEditText.setError(getResources().getString(
							R.string.User_name_cannot_be_empty));
					userNameEditText.requestFocus();
					return;
				}
				String nick = metNick.getText().toString().trim();
				if (TextUtils.isEmpty(nick)) {
					metNick.setError("昵称不能为空");
					metNick.requestFocus();
					return;
				}
				if (TextUtils.isEmpty(pwd)) {
					passwordEditText.setError(getResources().getString(
							R.string.Password_cannot_be_empty));
					passwordEditText.requestFocus();
					return;
				}
				if (TextUtils.isEmpty(confirm_pwd)) {
					confirmPwdEditText
							.setError(getResources()
									.getString(
											R.string.Confirm_password_cannot_be_empty));
					confirmPwdEditText.requestFocus();
					return;
				}
				if (!pwd.equals(confirm_pwd)) {
					confirmPwdEditText.setError(getResources()
							.getString(R.string.Two_input_password));
					confirmPwdEditText.requestFocus();
					return;
				}
				UserBean user = new UserBean(username, nick, pwd);
				/*
				 * 向环信服务器和应用服务器注册
				 */
				new RegisterTask(user).execute(I.SERVER_ROOT);

			}
		});
	}

	/**
	 * 初始化view
	 * 
	 */
	private void initView() {
		userNameEditText = (EditText) findViewById(R.id.et_username);
		passwordEditText = (EditText) findViewById(R.id.et_password);
		confirmPwdEditText = (EditText) findViewById(R.id.et_confirm_password);
		metNick=(EditText) findViewById(R.id.et_nick);
		mivAvatar=(ImageView) findViewById(R.id.iv_avatar);
	}

	public void back(View view) {
		finish();
	}

	/**
	 * 向环信服务器和应用服务器注册
	 * 
	 * @author yao
	 * 
	 */
	class RegisterTask extends AsyncTask<String, Void, String> {
		UserBean user;
		ProgressDialog pd;

		public RegisterTask(UserBean user) {
			super();
			this.user = user;
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(mContext);
			pd.setMessage(getResources().getString(R.string.Is_the_registered));
			pd.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String result=null;
			try{
			// 调用sdk注册方法向环信服务器注册
			EMChatManager.getInstance().createAccountOnServer(
				user.getUserName(),user.getPassword());
			//向应用服务器注册
			boolean isSuccess = NetUtils.register(user);
			if(isSuccess){
				String userName=getUserName();
				//上传头像
				boolean uploadAvatar = NetUtils.uploadAvatar(mOnSetAvatarListener.getAvatarFile(RegisterActivity.this,userName));
				result="注册成功";
			}else{
				result="注册失败";
			}
		} catch (final EaseMobException e) {
			int errorCode = e.getErrorCode();
			if (errorCode == EMError.NONETWORK_ERROR) {
				result=getResources().getString(R.string.network_anomalies);
			} else if (errorCode == EMError.USER_ALREADY_EXISTS) {
				result=getResources().getString(R.string.User_already_exists);
			} else if (errorCode == EMError.UNAUTHORIZED) {
				result=getResources().getString(R.string.registration_failed_without_permission);
			} else {
				result=getResources().getString(R.string.Registration_failed)+ e.getMessage();
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return result;
		}
		@Override
		protected void onPostExecute(String result) {
			pd.dismiss();//关闭进度对话框
			Toast.makeText(mContext, result, 2000).show();
			if(null!=result&&result.equals("注册成功")){
				finish();
			}
		}
	}
	
	/**
	 * 供OnSetAvatarClickListener类调用，获取注册窗口中输入的账号
	 * @return
	 */
	public String getUserName(){
		String userName=userNameEditText.getText().toString();
		if(TextUtils.isEmpty(userName)){
			userNameEditText.setError("账号不能为空");
			return null;
		}
		return userName;
	}
	/**
	 * 处理带返回结果的Activity返回的照片
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode!=RESULT_OK){
			return ;
		}
		String userName=getUserName();
		switch (requestCode) {
		case OnSetAvatarClickListener.REQUEST_TAKE_PHOTO:
			if(!TextUtils.isEmpty(userName)){
				Uri uri=Uri.fromFile(mOnSetAvatarListener.getAvatarFile(RegisterActivity.this,userName));
				//启动带裁剪的Activity
				mOnSetAvatarListener.startCropPhoto(uri, 200, 200, OnSetAvatarClickListener.REQUEST_CROP_PHOTO, true);
			}
			break;
		case OnSetAvatarClickListener.REQEUST_CHOOS_PHOTO:
			//启动带裁剪的Activity
			mOnSetAvatarListener.startCropPhoto(data.getData(), 200, 200, OnSetAvatarClickListener.REQUEST_CROP_PHOTO, true);
			break;
		case OnSetAvatarClickListener.REQUEST_CROP_PHOTO://启动系统裁剪照片的Activity
			mOnSetAvatarListener.closePopuAvatar();
			//在ImageView中显示照片，将照片保存至sd卡
			mOnSetAvatarListener.saveCropPhoto(mivAvatar, data, userName);
			break;
		}
	}
}
