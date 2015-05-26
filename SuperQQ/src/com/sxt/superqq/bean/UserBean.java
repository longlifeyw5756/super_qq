package com.sxt.superqq.bean;

import java.io.Serializable;
/** 表示注册用户
 *  存放数据库中user表的一条记录*/
public class UserBean implements Serializable{
	private int id;//user表的主键
	private String userName;//环信账号
	private String nick;//昵称
	private String password;//密码
	private String avatar;//头像在服务端硬盘的地址
	private double latitude;//用户手机位置的纬度
	private double longitude;//用户手机位置的经度
	private int unreadMsgCount;//未读信息的条数
	/**
	 * 手机中联系人定位的属性
	 */
	private String header;
	
	private String result;//返回客户端请求是否成功，成功：ok
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public int getUnreadMsgCount() {
		return unreadMsgCount;
	}
	public void setUnreadMsgCount(int unreadMsgCount) {
		this.unreadMsgCount = unreadMsgCount;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public UserBean() {
		// TODO Auto-generated constructor stub
	}
	public UserBean(String userName, String nick, String password) {
		super();
		this.userName = userName;
		this.nick = nick;
		this.password = password;
	}
	
	public UserBean(int id, String userName, String nick, String password,
			String avatar, double latitude, double longitude,
			int unreadMsgCount, String header, String result) {
		super();
		this.id = id;
		this.userName = userName;
		this.nick = nick;
		this.password = password;
		this.avatar = avatar;
		this.latitude = latitude;
		this.longitude = longitude;
		this.unreadMsgCount = unreadMsgCount;
		this.header = header;
		this.result = result;
	}

	
	
	@Override
	public String toString() {
		return "UserBean [id=" + id + ", userName=" + userName + ", nick="
				+ nick + ", password=" + password + ", avatar=" + avatar
				+ ", latitude=" + latitude + ", longitude=" + longitude
				+ ", unreadMsgCount=" + unreadMsgCount + ", header=" + header
				+ ", result=" + result + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserBean other = (UserBean) obj;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}
	
}
