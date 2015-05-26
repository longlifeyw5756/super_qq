package com.sxt.superqq.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import com.sxt.superqq.I;
import com.sxt.superqq.SuperQQApplication;
import com.sxt.superqq.bean.ContactBean;
import com.sxt.superqq.bean.UserBean;
import com.sxt.superqq.utils.HttpUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 负责从服务端下载以下联系人数据： 
 * HashMap<Integer,ContactBean> contacts; 
 * ArrayList<UserBean>
 * contaList;
 * 
 * @author yao 本类被LoginActivity和SplashActivity、ContactListFragment调用。
 */
public class DownloadContactsTask extends AsyncTask<String, Void, Boolean> {
	Activity activity;
	String userName;
	int pageId;// 下载的页号
	int pageSize;// 每页的记录数

	public DownloadContactsTask(Activity activity, String userName, int pageId,
			int pageSize) {
		super();
		this.activity = activity;
		this.userName = userName;
		this.pageId = pageId;
		this.pageSize = pageSize;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String url = params[0];// 获取下载地址

		boolean isSuccess = downloContacts(url);
		if (isSuccess) {
			isSuccess = downloadContactList(url);
		}
		return isSuccess;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(result){
			//向ContactListFragment发送粘滞广播，通知刷新联系人页面
			Intent intent=new Intent("update_contacts");
			activity.sendStickyBroadcast(intent);
		}
	}
	/**
	 * 下载类型是ArrayList<UserBean>的联系人数据
	 * 
	 * @param url
	 */
	private boolean downloadContactList(String url) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,
				I.REQUEST_DOWNLOAD_CONTACTLIST));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.PAGE_ID, pageId + ""));
		params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize + ""));
		try {
			InputStream in = HttpUtils.getInputStream(url, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			UserBean[] contactArray = om.readValue(in, UserBean[].class);
			List<UserBean> contacts = Arrays.asList(contactArray);
			// 取出SuperQQApplication.contacList集合
			ArrayList<UserBean> contactList = SuperQQApplication.getInstance()
					.getContactList();
			// 将新下载的联系人集合添加到已下载的联系人集合中
			contactList.addAll(contacts);
//			for (UserBean user : contactList) {
//				Log.i("main", user.toString());
//			}
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
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}

	/**
	 * 下载类型为HashMap<Integer,ContactBean>的联系人数据
	 * 
	 * @param url
	 */
	private boolean downloContacts(String url) {
		ArrayList<BasicNameValuePair> paramList = new ArrayList<BasicNameValuePair>();
		paramList.add(new BasicNameValuePair(I.KEY_REQUEST,
				I.REQUEST_DOWNLOAD_CONTACTS));
		paramList.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		paramList.add(new BasicNameValuePair(I.PAGE_ID, pageId + ""));
		paramList.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize + ""));

		try {
			InputStream in = HttpUtils.getInputStream(url, paramList,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			ContactBean[] contactArray = om.readValue(in, ContactBean[].class);
			// 存放当前用户的联系人的集合，键是cuid
			HashMap<Integer, ContactBean> contactMap = new HashMap<Integer, ContactBean>();
			for (ContactBean contact : contactArray) {
				contactMap.put(contact.getCuid(), contact);
				Log.i("main", contact.toString());
			}
			// 取出SuperQQApplication.contacts集合
			HashMap<Integer, ContactBean> contacts = SuperQQApplication
					.getInstance().getContacts();
			// 将下载的联系人集合添加到contacts集合
			contacts.putAll(contactMap);
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
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}

}
