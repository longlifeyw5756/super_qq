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
package com.sxt.superqq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.Context;

import com.easemob.EMCallBack;
import com.sxt.superqq.bean.ContactBean;
import com.sxt.superqq.bean.UserBean;
import com.sxt.superqq.domain.User;
/**
 * 保存了当前项目的一些全局的变量，例如：
 * 登陆者的账号、密码、昵称
 * 所有联系人的信息。
 * @author yao
 *
 */
public class SuperQQApplication extends Application {

	public static Context applicationContext;
	private static SuperQQApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";
	
	/**
	 * 当前用户nickname,为了苹果推送不是userid而是昵称
	 */
	public static String currentUserNick = "";
	public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();

	@Override
	public void onCreate() {
		super.onCreate();
        applicationContext = this;
        instance = this;

        /**
         * this function will initialize the HuanXin SDK
         * 
         * @return boolean true if caller can continue to call HuanXin related APIs after calling onInit, otherwise false.
         * 
         * 环信初始化SDK帮助函数
         * 返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
         * 
         * for example:
         * 例子：
         * 
         * public class DemoHXSDKHelper extends HXSDKHelper
         * 
         * HXHelper = new DemoHXSDKHelper();
         * if(HXHelper.onInit(context)){
         *     // do HuanXin related work
         * }
         */
        hxSDKHelper.onInit(applicationContext);
	}

	public static SuperQQApplication getInstance() {
		return instance;
	}
 
	/**
	 * 获取内存中好友user list
	 *
	 * @return
	 */
	public Map<String, User> getContact_List() {
	    return hxSDKHelper.getContactList();
	}

	/**
	 * 设置好友user list到内存中
	 *
	 * @param contactList
	 */
	public void setContact_List(Map<String, User> contactList) {
	    hxSDKHelper.setContactList(contactList);
	}

	/**
	 * 获取当前登陆用户名
	 *
	 * @return
	 */
	public String getUserName() {
	    return hxSDKHelper.getHXId();
	}

	/**
	 * 获取密码
	 *
	 * @return
	 */
	public String getPassword() {
		return hxSDKHelper.getPassword();
	}

	/**
	 * 设置用户名
	 *
	 * @param user
	 */
	public void setUserName(String username) {
	    hxSDKHelper.setHXId(username);
	}

	/**
	 * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
	 * 内部的自动登录需要的密码，已经加密存储了
	 *
	 * @param pwd
	 */
	public void setPassword(String pwd) {
	    hxSDKHelper.setPassword(pwd);
	}

	/**
	 * 退出登录,清空数据
	 */
	public void logout(final EMCallBack emCallBack) {
		// 先调用sdk logout，在清理app中自己的数据
	    hxSDKHelper.logout(emCallBack);
	}
	/** 保存最近登陆成功的用户信息*/
	private UserBean user;

	public UserBean getUser() {
		return user;
	}

	public void setUser(UserBean user) {
		this.user = user;
	}
	
	//contacts存放当前账号所有联系人的信息
	private HashMap<Integer, ContactBean> contacts=new HashMap<Integer, ContactBean>();

	public HashMap<Integer, ContactBean> getContacts() {
		return contacts;
	}

	public void setContacts(HashMap<Integer, ContactBean> contacts) {
		this.contacts = contacts;
	}
	
	//存放联系人的具体个人信息
	private ArrayList<UserBean> contactList=new ArrayList<UserBean>();

	public void setContactList(ArrayList<UserBean> contactList) {
		this.contactList = contactList;
	}
	
	public ArrayList<UserBean> getContactList(){
		return this.contactList;
	}
}
