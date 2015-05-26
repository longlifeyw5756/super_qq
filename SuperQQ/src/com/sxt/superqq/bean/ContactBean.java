package com.sxt.superqq.bean;

import java.io.Serializable;
/**
 * 联系人实体类
 * 表示数据表contact中一条记录*/
public class ContactBean implements Serializable {

	private int myuid;//用户在user表中的id
	private int cuid;//myuid联系人在user表中的id
	/*允许myuid代表的用户查看cuid代表的用户的位置
	 */
	private boolean isGetMyLocation;
	//允许显示位置
	private boolean isShowMyLocation;
	private String result;//返回客户端结果，成功：ok
	
	public int getMyuid() {
		return myuid;
	}
	public void setMyuid(int myuid) {
		this.myuid = myuid;
	}
	public int getCuid() {
		return cuid;
	}
	public void setCuid(int cuid) {
		this.cuid = cuid;
	}
	public boolean isGetMyLocation() {
		return isGetMyLocation;
	}
	public void setGetMyLocation(boolean isGetMyLocation) {
		this.isGetMyLocation = isGetMyLocation;
	}
	public boolean isShowMyLocation() {
		return isShowMyLocation;
	}
	public void setShowMyLocation(boolean isShowMyLocation) {
		this.isShowMyLocation = isShowMyLocation;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public ContactBean() {
		// TODO Auto-generated constructor stub
	}
	public ContactBean(int myuid, int cuid, boolean isGetMyLocation,
			boolean isShowMyLocation) {
		super();
		this.myuid = myuid;
		this.cuid = cuid;
		this.isGetMyLocation = isGetMyLocation;
		this.isShowMyLocation = isShowMyLocation;
	}
	@Override
	public String toString() {
		return "ContactBean [myuid=" + myuid + ", cuid=" + cuid
				+ ", isGetMyLocation=" + isGetMyLocation
				+ ", isShowMyLocation=" + isShowMyLocation + ", result="
				+ result + "]";
	}
}
