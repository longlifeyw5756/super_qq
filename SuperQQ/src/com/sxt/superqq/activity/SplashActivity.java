package com.sxt.superqq.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.sxt.superqq.DemoHXSDKHelper;
import com.sxt.superqq.I;
import com.sxt.superqq.R;
import com.sxt.superqq.SuperQQApplication;
import com.sxt.superqq.bean.UserBean;
import com.sxt.superqq.db.UserDao;
import com.sxt.superqq.task.DownloadContactsTask;

/**
 * 开屏页
 *
 */
public class SplashActivity extends BaseActivity {
	private LinearLayout rootLayout;
	private TextView versionText;
	
	private static final int sleepTime = 2000;

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.activity_splash);
		super.onCreate(arg0);

		rootLayout = (LinearLayout) findViewById(R.id.splash_root);
		versionText = (TextView) findViewById(R.id.tv_version);

		versionText.setText(getVersion());
		AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
		animation.setDuration(1500);
		rootLayout.startAnimation(animation);
	}

	@Override
	protected void onStart() {
		super.onStart();

		new Thread(new Runnable() {
			public void run() {
				if (DemoHXSDKHelper.getInstance().isLogined()) {
					// ** 免登陆情况 加载所有本地群和会话
					//不是必须的，不加sdk也会自动异步去加载(不会重复加载)；
					//加上的话保证进了主页面会话和群组都已经load完毕
					long start = System.currentTimeMillis();
					//从本地加载所有的群组
					EMGroupManager.getInstance().loadAllGroups();
					//从本地加载所有的会话
					EMChatManager.getInstance().loadAllConversations();
					long costTime = System.currentTimeMillis() - start;
					//等待sleeptime时长
					if (sleepTime - costTime > 0) {
						try {
							Thread.sleep(sleepTime - costTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					String userName = SuperQQApplication.getInstance().getUserName();
					if(SuperQQApplication.getInstance().getContacts().size()==0){
                		//若是第一次登陆,则下载头一页20个userName的联系人数据
                		new DownloadContactsTask(SplashActivity.this, 
                			userName, 0, 20).execute(I.SERVER_ROOT);
                	}
					
					//从手机数据库：user.db中读取当前用户，并保存在SuperQQApplication中
					UserDao dao=new UserDao(SplashActivity.this);
					//从数据库中回去欧洲当前用户信息->user
					UserBean user = dao.findUserByUserName(userName);
					//将user保存在内存中
					SuperQQApplication.getInstance().setUser(user);
					
					//进入主页面
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
					finish();
				}else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
					}
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
					finish();
				}
			}
		}).start();

	}
	
	/**
	 * 获取当前应用程序的版本号
	 */
	private String getVersion() {
		String st = getResources().getString(R.string.Version_number_is_wrong);
		PackageManager pm = getPackageManager();
		try {
			PackageInfo packinfo = pm.getPackageInfo(getPackageName(), 0);
			String version = packinfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return st;
		}
	}
}
