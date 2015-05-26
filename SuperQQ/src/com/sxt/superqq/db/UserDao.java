package com.sxt.superqq.db;

import com.sxt.superqq.I;
import com.sxt.superqq.bean.UserBean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDao extends SQLiteOpenHelper {
	private static final String DB_NAME="users.db";
	private static final String TABLE_NAME="user";
	
	public UserDao(Context context) {
		super(context, DB_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql="create table if not exists "+TABLE_NAME
			+"("+I.User.ID+" integer primary key autoincrement,"
			+I.User.UID+" int unique,"
			+I.User.AVATAR+" varchar,"
			+I.User.HEADER+" varchar,"
			+I.User.LATITUDE+" double,"
			+I.User.LONGITUDE+" double,"
			+I.User.NICK+" varchar,"
			+I.User.PASSWORD+" varchar,"
			+I.User.UN_READ_MSG_COUNT+" int,"
			+I.User.USER_NAME+" varchar)";
		db.execSQL(sql);//建表
			
	}

	/**
	 * 向user表添加一条记录
	 * @param user
	 * @return
	 */
	public boolean addUser(UserBean user){
		ContentValues values=new ContentValues();
		values.put(I.User.AVATAR, user.getAvatar());
		values.put(I.User.HEADER, user.getHeader());
		values.put(I.User.LATITUDE, user.getLatitude());
		values.put(I.User.LONGITUDE, user.getLongitude());
		values.put(I.User.NICK, user.getNick());
		values.put(I.User.PASSWORD, user.getPassword());
		values.put(I.User.UID, user.getId());
		values.put(I.User.UN_READ_MSG_COUNT, user.getUnreadMsgCount());
		values.put(I.User.USER_NAME, user.getUserName());
		SQLiteDatabase db = getWritableDatabase();
		long rowId = db.insert(TABLE_NAME, null, values);
		return rowId>0;
	}
	
	/**
	 * 修改User记录
	 * @param user
	 * @return
	 */
	public boolean updateUser(UserBean user){
		ContentValues values=new ContentValues();
		values.put(I.User.AVATAR, user.getAvatar());
		values.put(I.User.HEADER, user.getHeader());
		values.put(I.User.LATITUDE, user.getLatitude());
		values.put(I.User.LONGITUDE, user.getLongitude());
		values.put(I.User.NICK, user.getNick());
		values.put(I.User.PASSWORD, user.getPassword());
		values.put(I.User.UID, user.getId());
		values.put(I.User.UN_READ_MSG_COUNT, user.getUnreadMsgCount());
		values.put(I.User.USER_NAME, user.getUserName());
		SQLiteDatabase db = getWritableDatabase();
		int count = db.update(TABLE_NAME, values, I.User.USER_NAME+"=?", new String[]{user.getUserName()});
		return count>0;
	}
	
	/**
	 * 通过账号查询曾登陆过的用户
	 * @param userName
	 * @return
	 */
	public UserBean findUserByUserName(String userName){
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, null, I.User.USER_NAME+"=?", new String[]{userName}, 
			null, null, null);
		if(c.moveToNext()){
			int uid=c.getInt(c.getColumnIndex(I.User.UID));
			String nick=c.getString(c.getColumnIndex(I.User.NICK));
			String password=c.getString(c.getColumnIndex(I.User.PASSWORD));
			String avatar=c.getString(c.getColumnIndex(I.User.AVATAR));
			double latitude=c.getDouble(c.getColumnIndex(I.User.LATITUDE));
			double longitude=c.getDouble(c.getColumnIndex(I.User.LONGITUDE));
			int unreadMsgCount=c.getInt(c.getColumnIndex(I.User.UN_READ_MSG_COUNT));
			String header=c.getString(c.getColumnIndex(I.User.HEADER));
			UserBean user=new UserBean(uid, userName, nick, password, avatar, latitude, longitude, unreadMsgCount, header, "ok");
			return user;
		}
		return null;
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
