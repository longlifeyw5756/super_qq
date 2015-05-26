package com.sxt.superqq.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.easemob.util.HanziToPinyin;
import com.squareup.picasso.Picasso;
import com.sxt.superqq.Constant;
import com.sxt.superqq.SuperQQApplication;
import com.sxt.superqq.R;
import com.sxt.superqq.bean.UserBean;
import com.sxt.superqq.domain.User;

public class UserUtils {
    /**
     * 根据username获取相应user，由于demo没有真实的用户数据，这里给的模拟的数据；
     * @param username
     * @return
     */
    public static User getUserInfo(String username){
        User user = SuperQQApplication.getInstance().getContact_List().get(username);
        if(user == null){
            user = new User(username);
        }
            
        if(user != null){
            //demo没有这些数据，临时填充
            user.setNick(username);
//            user.setAvatar("http://downloads.easemob.com/downloads/57.png");
        }
        return user;
    }
    
    /**
     * 设置用户头像
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
        User user = getUserInfo(username);
        if(user != null){
            Picasso.with(context).load(user.getAvatar()).placeholder(R.drawable.default_avatar).into(imageView);
        }else{
            Picasso.with(context).load(R.drawable.default_avatar).into(imageView);
        }
    }
    
	/**
	 * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
	 * 
	 * @param userName
	 * @param user
	 */
	public static void setUserHearder(String userName, UserBean user) {
		String headerName = null;
		if (!TextUtils.isEmpty(user.getNick())) {
			headerName = user.getNick();
		} else {
			headerName = user.getUserName();
		}
		if (userName.equals(Constant.NEW_FRIENDS_USERNAME)
			  || userName.equals(Constant.GROUP_USERNAME)) {
			user.setHeader("");
		} else if (Character.isDigit(headerName.charAt(0))) {
			//若headerName是整数
			user.setHeader("#");
		} else {
			//将headerName首字母转换为汉语拼音
			user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1).toUpperCase());
			char header = user.getHeader().toLowerCase().charAt(0);
			if (header < 'a' || header > 'z') {
				user.setHeader("#");
			}
		}
	}
	
}
