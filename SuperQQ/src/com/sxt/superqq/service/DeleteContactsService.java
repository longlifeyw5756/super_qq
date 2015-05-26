package com.sxt.superqq.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.sxt.superqq.bean.ContactBean;
import com.sxt.superqq.utils.NetUtils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * 后台服务,用于向服务端发送删除联系人数据的请求。
 * @author yao
 *
 */
public class DeleteContactsService extends IntentService {

	public DeleteContactsService() {
		super("DeleteContactsService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ArrayList<ContactBean> contacts=(ArrayList<ContactBean>) intent.getSerializableExtra("delete_contacts");
		for (ContactBean contact : contacts) {
			boolean isDeleted = NetUtils.deleteContact(contact.getMyuid(), contact.getCuid());
			Log.i("main","成功删除"+contact.toString()+":"+isDeleted);
		}
	}

}
