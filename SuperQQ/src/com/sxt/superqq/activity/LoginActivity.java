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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.sxt.superqq.Constant;
import com.sxt.superqq.I;
import com.sxt.superqq.SuperQQApplication;
import com.sxt.superqq.R;
import com.sxt.superqq.bean.UserBean;
import com.sxt.superqq.db.EMUserDao;
import com.sxt.superqq.db.UserDao;
import com.sxt.superqq.domain.User;
import com.sxt.superqq.task.DownloadContactsTask;
import com.sxt.superqq.utils.CommonUtils;
import com.sxt.superqq.utils.MD5;
import com.sxt.superqq.utils.NetUtils;

/**
 * 登陆页面
 * 
 */
public class LoginActivity extends BaseActivity {
	public static final int REQUEST_CODE_SETNICK = 1;
	private EditText usernameEditText;
	private EditText passwordEditText;

	private boolean progressShow;
	private boolean autoLogin = false;
	
	private String currentUsername;
	private String currentPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 如果用户名密码都有，直接进入主页面
//		if (DemoHXSDKHelper.getInstance().isLogined()) {
//			autoLogin = true;
//			startActivity(new Intent(LoginActivity.this, MainActivity.class));
//
//			return;
//		}
		setContentView(R.layout.activity_login);

		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		setListener();
		
		if (SuperQQApplication.getInstance().getUserName() != null) {
			usernameEditText.setText(SuperQQApplication.getInstance().getUserName());
		}
	}

	/**
	 * 设置各种事件监听
	 */
	private void setListener() {
	    setUsernameTextChangedListener();
	    setLoginClickListener();
	    //设置注册按钮的事件监听
    }

	/**
	 * 设置登陆按钮的事件监听
	 */
    private void setLoginClickListener() {
        findViewById(R.id.btnLogin).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonUtils.isNetWorkConnected(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
                    return;
                }
                currentUsername = usernameEditText.getText().toString().trim();
                currentPassword = passwordEditText.getText().toString().trim();
                
                /*
                 * 账号和密码的非空验证
                 */
                if(TextUtils.isEmpty(currentUsername)){
                    Toast.makeText(LoginActivity.this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(currentPassword)){
                    Toast.makeText(LoginActivity.this, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                //显示进度对话框
                progressShow = true;
                final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
                pd.setCanceledOnTouchOutside(false);
                pd.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        progressShow = false;
                    }
                });
                pd.setMessage(getString(R.string.Is_landing));
                pd.show();

                final long start = System.currentTimeMillis();
                // 调用sdk登陆方法登陆环信聊天服务器
                EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        
                        if (!progressShow) {
                            return;
                        }
                        
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.setMessage(getString(R.string.list_is_for));
                            }
                        });
                        try {
                        	
                        	//登陆应用服务器
                        	boolean isSuccess=loginAppServer(currentUsername,currentPassword);
                        	if(!isSuccess){
                        		runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(LoginActivity.this, "登陆失败", 2000).show();
										return ;
									}
								});
                        	}
                        	final String userName=SuperQQApplication.getInstance().getUserName();
                        	//下载登陆者的头像
                        	isSuccess = NetUtils.downloadAvatar(LoginActivity.this, userName);
                        	
                        	if(SuperQQApplication.getInstance().getContacts().size()==0){
                        		//若是第一次登陆,则下载头一页20个userName的联系人数据
                        		new DownloadContactsTask(LoginActivity.this, 
                        			userName, 0, 20).execute(I.SERVER_ROOT);
                        	}
                        	if(!isSuccess){
                        		runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(LoginActivity.this, userName+"的头像下载失败", 3000).show();
									}
								});
                        	}
                            // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
                            // ** manually load all local groups and
                            // conversations in case we are auto login
                            EMGroupManager.getInstance().loadAllGroups();
                            EMChatManager.getInstance().loadAllConversations();
                            //处理好友和群组
                            processContactsAndGroups();
                        } catch (Exception e) {
                            e.printStackTrace();
                            //取好友或者群聊失败，不让进入主页面
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.dismiss();
                                    SuperQQApplication.getInstance().logout(null);
                                    Toast.makeText(getApplicationContext(), R.string.login_failure_failed, 1).show();
                                }
                            });
                            return;
                        }
                        //更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                        boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(SuperQQApplication.currentUserNick.trim());
                        if (!updatenick) {
                            Log.e("LoginActivity", "update current user nick fail");
                        }
                        if (!LoginActivity.this.isFinishing())
                            pd.dismiss();
                        // 进入主页面
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }

					@Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(final int code, final String message) {
                        if (!progressShow) {
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                                Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
//                Intent intent = new Intent(LoginActivity.this, com.sxt.superqq.activity.AlertDialogActivity.class);
//                intent.putExtra("editTextShow", true);
//                intent.putExtra("titleIsCancel", true);
//                intent.putExtra("msg", getResources().getString(R.string.please_set_the_current));
//                intent.putExtra("edit_text", currentUsername);
//                startActivityForResult(intent, REQUEST_CODE_SETNICK);
            }
        });
    }

	/**
	 * 设置账号编辑框文字改变的事件监听
	 */
    private void setUsernameTextChangedListener() {
        // 如果用户名改变，清空密码
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEditText.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_SETNICK) {
				SuperQQApplication.currentUserNick = data.getStringExtra("edittext");

				progressShow = true;
				final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
				pd.setCanceledOnTouchOutside(false);
				pd.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						progressShow = false;
					}
				});
				pd.setMessage(getString(R.string.Is_landing));
				pd.show();

				final long start = System.currentTimeMillis();
				// 调用sdk登陆方法登陆环信聊天服务器
				EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {

					@Override
					public void onSuccess() {
						
						if (!progressShow) {
							return;
						}
						// 登陆成功，保存用户名密码
						SuperQQApplication.getInstance().setUserName(currentUsername);
						SuperQQApplication.getInstance().setPassword(currentPassword);
						
						runOnUiThread(new Runnable() {
							public void run() {
								pd.setMessage(getString(R.string.list_is_for));
							}
						});
						try {
							// ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
							// ** manually load all local groups and
							// conversations in case we are auto login
							EMGroupManager.getInstance().loadAllGroups();
							EMChatManager.getInstance().loadAllConversations();
							//处理好友和群组
							processContactsAndGroups();
						} catch (Exception e) {
							e.printStackTrace();
							//取好友或者群聊失败，不让进入主页面
							runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.dismiss();
                                    SuperQQApplication.getInstance().logout(null);
                                    Toast.makeText(getApplicationContext(), R.string.login_failure_failed, 1).show();
                                }
                            });
							return;
						}
						//更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
						boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(SuperQQApplication.currentUserNick.trim());
						if (!updatenick) {
							Log.e("LoginActivity", "update current user nick fail");
						}
						if (!LoginActivity.this.isFinishing())
							pd.dismiss();
						// 进入主页面
						startActivity(new Intent(LoginActivity.this, MainActivity.class));
						finish();
					}

                   

					@Override
					public void onProgress(int progress, String status) {
					}

					@Override
					public void onError(final int code, final String message) {
						if (!progressShow) {
							return;
						}
						runOnUiThread(new Runnable() {
							public void run() {
								pd.dismiss();
								Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message, Toast.LENGTH_SHORT).show();
							}
						});
					}
				});

			}
		}
	}

	 private void processContactsAndGroups() throws EaseMobException {
         // demo中简单的处理成每次登陆都去获取好友username，开发者自己根据情况而定
         List<String> usernames = EMContactManager.getInstance().getContactUserNames();
         EMLog.d("roster", "contacts size: " + usernames.size());
         Map<String, User> userlist = new HashMap<String, User>();
         for (String username : usernames) {
             User user = new User();
             user.setUsername(username);
             setUserHearder(username, user);
             userlist.put(username, user);
         }
         // 添加user"申请与通知"
         User newFriends = new User();
         newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
         String strChat = getResources().getString(R.string.Application_and_notify);
         newFriends.setNick(strChat);
         
         userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
         // 添加"群聊"
         User groupUser = new User();
         String strGroup = getResources().getString(R.string.group_chat);
         groupUser.setUsername(Constant.GROUP_USERNAME);
         groupUser.setNick(strGroup);
         groupUser.setHeader("");
         userlist.put(Constant.GROUP_USERNAME, groupUser);

         // 存入内存
         SuperQQApplication.getInstance().setContact_List(userlist);
         System.out.println("----------------"+userlist.values().toString());
         // 存入db
         EMUserDao dao = new EMUserDao(LoginActivity.this);
         List<User> users = new ArrayList<User>(userlist.values());
         dao.saveContactList(users);
         
         //获取黑名单列表
         List<String> blackList = EMContactManager.getInstance().getBlackListUsernamesFromServer();
         //保存黑名单
         EMContactManager.getInstance().saveBlackList(blackList);

         // 获取群聊列表(群聊里只有groupid和groupname等简单信息，不包含members),sdk会把群组存入到内存和db中
         EMGroupManager.getInstance().getGroupsFromServer();
     }
	
	/**
	 * 注册
	 * 
	 * @param view
	 */
	public void register(View view) {
		startActivityForResult(new Intent(this, RegisterActivity.class), 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (autoLogin) {
			return;
		}
	}
	
	/**
	 * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
	 * 
	 * @param username
	 * @param user
	 */
	protected void setUserHearder(String username, User user) {
		String headerName = null;
		if (!TextUtils.isEmpty(user.getNick())) {
			headerName = user.getNick();
		} else {
			headerName = user.getUsername();
		}
		if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
			user.setHeader("");
		} else if (Character.isDigit(headerName.charAt(0))) {
			user.setHeader("#");
		} else {
			user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1).toUpperCase());
			char header = user.getHeader().toLowerCase().charAt(0);
			if (header < 'a' || header > 'z') {
				user.setHeader("#");
			}
		}
	}
	
	/**
	 * 向应用服务端登陆
	 * @param currentUsername：输入的账号
	 * @param currentPassword：输入的密码
	 * @return
	 */
	private boolean loginAppServer(String currentUsername,
		String currentPassword) {
		UserDao dao=new UserDao(this);
		UserBean user = dao.findUserByUserName(currentUsername);
		if(user!=null){//若数据库中有曾登陆过的用户信息
			if(user.getPassword().equals(MD5.getData(currentPassword))){
				saveUser(user);
				return true;
			}
		}
		//从应用服务器登陆
		try {
			user = NetUtils.login(currentUsername, currentPassword);
			saveUser(user);
			user.setPassword(MD5.getData(user.getPassword()));
			dao.addUser(user);
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
		}
		return false;
	}

	/**
	 * 将账号和密码还有user对象保存
	 * @param user
	 */
	private void saveUser(UserBean user) {
		// 登陆成功，保存用户名密码至首选项
      SuperQQApplication.getInstance().setUserName(currentUsername);
      SuperQQApplication.getInstance().setPassword(currentPassword);
      SuperQQApplication.getInstance().setUser(user);
	}
	 
}
