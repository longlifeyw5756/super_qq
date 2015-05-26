package com.sxt.superqq.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sxt.superqq.I;
import com.sxt.superqq.bean.ContactBean;
import com.sxt.superqq.bean.UserBean;
import com.sxt.superqq.listener.OnSetAvatarClickListener;

public final class NetUtils {

	/**
	 * 负责向应用服务器注册
	 * 
	 * @param user
	 *            :包含userName、nick和password三个信息的对象
	 * @return:true:注册成功
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws IllegalStateException
	 */
	public static boolean register(UserBean user) throws IllegalStateException,
			ClientProtocolException, IOException {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_REGISTER));
		params.add(new BasicNameValuePair(I.User.USER_NAME, user.getUserName()));
		params.add(new BasicNameValuePair(I.User.NICK, user.getNick()));
		params.add(new BasicNameValuePair(I.User.PASSWORD, user.getPassword()));
		InputStream in = HttpUtils.getInputStream(I.SERVER_ROOT, params,
				HttpUtils.METHOD_GET);
		ObjectMapper om = new ObjectMapper();
		Boolean isSuccess = om.readValue(in, Boolean.class);
		return isSuccess;
	}

	/**
	 * 上传头像
	 * 
	 * @param file
	 *            ：头像保存在sd卡的路径
	 * @param userName
	 *            ：账号
	 * @return
	 * @throws IOException
	 */
	public static boolean uploadAvatar(File file) throws IOException {
		String path = file.getAbsolutePath();
		String userName = path.substring(path.lastIndexOf("/") + 1);
		// 获取sd卡保存的头像的路径
		path = file.getAbsolutePath() + ".jpg";
		file = new File(path);

		// 获取代表sd卡头像文件的输入流
		FileInputStream fis = new FileInputStream(file);

		// 设置上传头像的请求
		/**
		 * 发送服务端的内容 上传头像请求字符串(upload_avatar)的长度： 上传头像请求的字符串内容 userName的字节长度
		 * userName本身的内容
		 * 
		 * 13upload_avatar6aaaaaaxxxxxxxxxxxxxxxxxxxxxxxxx
		 */
		int requestLength = I.REQUEST_UPLOAD_AVATAR.length();
		// 创建字节数组输出流，用于保存以上结构的字节
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		// 上传头像请求字符串(upload_avatar)的长度
		dos.writeInt(requestLength);
		// 将upload_avatar字符串以字节数组保存
		dos.write(I.REQUEST_UPLOAD_AVATAR.getBytes());
		// 保存账号字符串的字节数
		dos.writeInt(userName.length());
		// 将账号以字节数字数组保存
		dos.write(userName.getBytes());

		// 将fis代表的图片输入中的数据保存在dos(baos)
		int len;
		byte[] buffer = new byte[1024 * 5];
		while ((len = fis.read(buffer)) != -1) {
			dos.write(buffer, 0, len);
		}
		// 创建字节数组输入流,输入流的内容是：baos的字节数组内容
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		HttpPost post = new HttpPost(I.SERVER_ROOT);
		HttpClient client = new DefaultHttpClient();
		// 将包含请求、账号等信息的bais设置为post请求的实体类
		HttpEntity entity = new InputStreamEntity(bais, bais.available());
		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == 200) {
			return true;
		}
		return false;
	}

	/**
	 * 登陆应用服务器
	 * 
	 * @param userName
	 *            ：账号
	 * @param password
	 *            ：密码
	 * @return true:登陆成功
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws IllegalStateException
	 */
	public static UserBean login(String userName, String password)
			throws IllegalStateException, ClientProtocolException, IOException {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_LOGIN));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.User.PASSWORD, password));
		InputStream in = HttpUtils.getInputStream(I.SERVER_ROOT, params,
				HttpUtils.METHOD_GET);
		ObjectMapper om = new ObjectMapper();
		UserBean user = om.readValue(in, UserBean.class);
		return user;
	}

	/**
	 * 从服务端下载指定账号的头像
	 * @param userName：账号
	 */
	public static boolean downloadAvatar(Activity activity, String userName) {
		//获取sd卡保存的头像
		File file = OnSetAvatarClickListener.getAvatarFile(activity, userName);
		if (!file.exists()) {//若该头像文件不存在
			ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(new BasicNameValuePair(I.KEY_REQUEST,
					I.REQUEST_DOWNLOAD_AVATAR));
			params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
			FileOutputStream fos = null;
			try {
				InputStream in = HttpUtils.getInputStream(I.SERVER_ROOT,
						params, HttpUtils.METHOD_GET);
				file = OnSetAvatarClickListener.getAvatarFile(activity,
						userName);
				file = new File(file.getAbsolutePath() + ".jpg");
				fos= new FileOutputStream(file);
				int len;
				byte[] buffer=new byte[1024*5];
				while((len=in.read(buffer))!=-1){
					fos.write(buffer, 0, len);
				}
				return true;
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(fos!=null){
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 向应用服务器添加联系人信息，返回ContacBean对象
	 * @param userName
	 * @param name
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static ContactBean addContact(String userName,String name) {
		
		ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_ADD_CONTACT));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.Contact.NAME, name));
		ContactBean contact;
		try {
			InputStream in = HttpUtils.getInputStream(I.SERVER_ROOT, params, HttpUtils.METHOD_GET);
			
			ObjectMapper om=new ObjectMapper();
			contact = om.readValue(in, ContactBean.class);
			if(contact.getResult().equals("ok")){
//				Log.i("main","新添好友:"+contact.toString());
			}
			return contact;
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			HttpUtils.closeClient();
		}
		return null;
	}
	
	/**
	 * 从服务端下载指定账号的用户
	 * @param userName
	 * @return
	 */
	public static UserBean findUserByUserName(String userName){
		
		ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_USER));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		try {
			InputStream in = HttpUtils.getInputStream(I.SERVER_ROOT, params, HttpUtils.METHOD_GET);
			ObjectMapper om=new ObjectMapper();
			UserBean user = om.readValue(in, UserBean.class);
			return user;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			HttpUtils.closeClient();
		}
		return null;
	}
	
	/**
	 * 向服务端发送删除指定id的联系人数据
	 * @param myuid:当前用户的账号
	 * @param cuid：联系人的账号
	 * @return
	 */
	public static boolean deleteContact(int myuid,int cuid){
		
		ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_DELETE_CONTACT));
		params.add(new BasicNameValuePair(I.Contact.MYUID, ""+myuid));
		params.add(new BasicNameValuePair(I.Contact.CUID, ""+cuid));
		try {
			InputStream in = HttpUtils.getInputStream(I.SERVER_ROOT, params, HttpUtils.METHOD_GET);
			ObjectMapper om=new ObjectMapper();
			Boolean isDeleted = om.readValue(in, Boolean.class);
			return isDeleted;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			HttpUtils.closeClient();
		}
		return false;
	}
}
