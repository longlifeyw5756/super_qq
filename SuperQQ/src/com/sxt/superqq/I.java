package com.sxt.superqq;

public interface I {

	String SERVER_ROOT="http://10.0.2.2:8080/SuperQQ-Server/Server";
	public static class User{
		public static final String ID="id";
		/** 应用服务器数据库中用户的id*/
		public static final String UID="uid";
		public static final String USER_NAME="userName";
		public static final String NICK="nick";
		public static final String AVATAR="avatar";
		public static final String PASSWORD="password";
		public static final String LATITUDE="latitude";
		public static final String LONGITUDE="longitude";
		public static final String HEADER="heard";
		public static final String UN_READ_MSG_COUNT="unreadMsgCount";
	}
	
	public static class Contact extends User{
		
		public static final String NAME="name";
		public static final String MYUID="myuid";
		public static final String CUID="cuid";
		public static final String IS_GET_MY_LOCATION="isGetMyLocation";
		public static final String IS_SHOW_MY_LOCATION="isShowMyLocation";
		
	}
	
	public static final String PAGE_ID="page_id";
	public static final String PAGE_SIZE="page_size";
	
	String KEY_REQUEST="request";
    /**
     * 客户端发送的注册请求
     */
    String REQUEST_REGISTER="register";
    
    String REQUEST_UPLOAD_AVATAR="upload_avatar";
    
    String REQUEST_LOGIN="login";
    
    String REQUEST_DOWNLOAD_AVATAR="download_avatar";
    
    String REQUEST_DOWNLOAD_CONTACTS="download_contacts";
    
    String REQUEST_DOWNLOAD_CONTACTLIST="download_contactlist";
    
    String REQUEST_DELETE_CONTACT="delete_contact";
    
    /** 下载头像的接口*/
    String DOWNLOAD_AVATAR_URL=SERVER_ROOT+
        "?request="+REQUEST_DOWNLOAD_AVATAR+"&userName=&avatar=";
    
    /** 添加联系人的请求*/
    String REQUEST_ADD_CONTACT="add_contact";
    
    String REQUEST_FIND_USER="find_user";
}
