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
package com.sxt.superqq.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;
import com.sxt.superqq.Constant;
import com.sxt.superqq.R;
import com.sxt.superqq.SuperQQApplication;
import com.sxt.superqq.activity.AddContactActivity;
import com.sxt.superqq.activity.ChatActivity;
import com.sxt.superqq.activity.GroupsActivity;
import com.sxt.superqq.activity.MainActivity;
import com.sxt.superqq.activity.NewFriendsMsgActivity;
import com.sxt.superqq.adapter.Contact_Adapter;
import com.sxt.superqq.bean.UserBean;
import com.sxt.superqq.db.EMUserDao;
import com.sxt.superqq.db.InviteMessgeDao;
import com.sxt.superqq.domain.User;
import com.sxt.superqq.utils.UserUtils;
import com.sxt.superqq.widget.Sidebar;

/**
 * 联系人列表页
 * 
 */
public class ContactlistFragment extends Fragment {
	private Contact_Adapter mAdapter;
	// private List<User> contactList;
	private List<UserBean> mContactList;
	private ListView listView;
	private boolean hidden;
	private Sidebar sidebar;
	private InputMethodManager inputMethodManager;
	private List<String> blackList;
	ImageButton clearSearch;
	EditText query;

	/** 接收联系人数据改变的广播接收者 */
	private ContactChangedReceiver mReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contact_list, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("isConflict", false))
			return;
		inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		listView = (ListView) getView().findViewById(R.id.list);
		sidebar = (Sidebar) getView().findViewById(R.id.sidebar);
		sidebar.setListView(listView);
		// 黑名单列表
		blackList = EMContactManager.getInstance().getBlackListUsernames();
		// contactList = new ArrayList<User>();
		// 搜索框
		query = (EditText) getView().findViewById(R.id.query);
		query.setHint(R.string.search);
		clearSearch = (ImageButton) getView().findViewById(R.id.search_clear);
		query.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mAdapter.getFilter().filter(s);
				if (s.length() > 0) {
					clearSearch.setVisibility(View.VISIBLE);
				} else {
					clearSearch.setVisibility(View.INVISIBLE);

				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		clearSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				query.getText().clear();
				hideSoftKeyboard();
			}
		});

		mContactList = new ArrayList<UserBean>();
		// 设置adapter
		mAdapter = new Contact_Adapter(getActivity(), R.layout.row_contact,
				mContactList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String username = mAdapter.getItem(position).getUserName();
				if (Constant.NEW_FRIENDS_USERNAME.equals(username)) {
					// 进入申请与通知页面
					User user = SuperQQApplication.getInstance()
							.getContact_List()
							.get(Constant.NEW_FRIENDS_USERNAME);
					user.setUnreadMsgCount(0);
					startActivity(new Intent(getActivity(),
							NewFriendsMsgActivity.class));
				} else if (Constant.GROUP_USERNAME.equals(username)) {
					// 进入群聊列表页面
					startActivity(new Intent(getActivity(),
							GroupsActivity.class));
				} else {
					// demo中直接进入聊天页面，实际一般是进入用户详情页
					startActivity(new Intent(getActivity(), ChatActivity.class)
							.putExtra("userId", mAdapter.getItem(position)
									.getUserName()));
				}
			}
		});
		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 隐藏软键盘
				if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getActivity().getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(
								getActivity().getCurrentFocus()
										.getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});

		ImageView addContactView = (ImageView) getView().findViewById(
				R.id.iv_new_contact);
		// 进入添加好友页
		addContactView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(),
						AddContactActivity.class));
			}
		});
		registerForContextMenu(listView);

		regsiterContactReceiver();
	}

	/**
	 * 注册联系人数据改变的广播接收者
	 */
	private void regsiterContactReceiver() {
		mReceiver = new ContactChangedReceiver();
		IntentFilter filter = new IntentFilter("update_contacts");
		getActivity().registerReceiver(mReceiver, filter);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// 长按前两个不弹menu
		if (((AdapterContextMenuInfo) menuInfo).position > 1) {
			getActivity().getMenuInflater().inflate(
					R.menu.context_contact_list, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_contact) {
			UserBean tobeDeleteUser = mAdapter
					.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
			// 删除此联系人
			deleteContact(tobeDeleteUser);
			// 删除相关的邀请消息
			InviteMessgeDao dao = new InviteMessgeDao(getActivity());
			dao.deleteMessage(tobeDeleteUser.getUserName());
			return true;
		} else if (item.getItemId() == R.id.add_to_blacklist) {
			UserBean user = mAdapter.getItem(((AdapterContextMenuInfo) item
					.getMenuInfo()).position);
			moveToBlacklist(user.getUserName());
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {
			refresh();
		}
	}

	/**
	 * 删除联系人
	 * 
	 * @param toDeleteUser
	 */
	public void deleteContact(final UserBean tobeDeleteUser) {
		String st1 = getResources().getString(R.string.deleting);
		final String st2 = getResources().getString(R.string.Delete_failed);
		final ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					EMContactManager.getInstance().deleteContact(
							tobeDeleteUser.getUserName());
					// 删除db和内存中此用户的数据
					EMUserDao dao = new EMUserDao(getActivity());
					dao.deleteContact(tobeDeleteUser.getUserName());
					SuperQQApplication.getInstance().getContact_List()
							.remove(tobeDeleteUser.getUserName());
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							mAdapter.remove(tobeDeleteUser);

						}
					});
				} catch (final Exception e) {
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st2 + e.getMessage(),
									1).show();
						}
					});

				}

			}
		}).start();

	}

	/**
	 * 把user移入到黑名单
	 */
	private void moveToBlacklist(final String username) {
		final ProgressDialog pd = new ProgressDialog(getActivity());
		String st1 = getResources().getString(R.string.Is_moved_into_blacklist);
		final String st2 = getResources().getString(
				R.string.Move_into_blacklist_success);
		final String st3 = getResources().getString(
				R.string.Move_into_blacklist_failure);
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					// 加入到黑名单
					EMContactManager.getInstance().addUserToBlackList(username,
							false);
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st2, 0).show();
							refresh();
						}
					});
				} catch (EaseMobException e) {
					e.printStackTrace();
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st3, 0).show();
						}
					});
				}
			}
		}).start();

	}

	// 刷新ui
	public void refresh() {
		try {
			// 可能会在子线程中调到这方法
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					// initContactList();
					mAdapter.notifyDataSetChanged();

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化联系人数据，添加群聊和申请与通知两个对象
	 */
	private void initContactList() {
		// 添加user"申请与通知"
		UserBean newFriends = new UserBean();
		newFriends.setUserName(Constant.NEW_FRIENDS_USERNAME);
		String strChat = getResources().getString(
				R.string.Application_and_notify);
		newFriends.setNick(strChat);

		// 添加"群聊"
		UserBean groupUser = new UserBean();
		String strGroup = getResources().getString(R.string.group_chat);
		groupUser.setUserName(Constant.GROUP_USERNAME);
		groupUser.setNick(strGroup);
		groupUser.setHeader("");
		mContactList.add(0, newFriends);
		mContactList.add(0, groupUser);
	}

	void hideSoftKeyboard() {
		if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getActivity().getCurrentFocus() != null)
				inputMethodManager.hideSoftInputFromWindow(getActivity()
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (((MainActivity) getActivity()).isConflict) {
			outState.putBoolean("isConflict", true);
		} else if (((MainActivity) getActivity()).getCurrentAccountRemoved()) {
			outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
		}

	}

	/**
	 * 接收联系人数据改变的广播接收者类
	 * 
	 * @author yao
	 * 
	 */
	class ContactChangedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			mContactList.clear();
			// 获取设置contactlist
			initContactList();
			// 1.获取下载的联系人集合:ArrayList<UserBean>
			ArrayList<UserBean> contactList = SuperQQApplication.getInstance()
					.getContactList();
			if(!mContactList.containsAll(contactList)){
				// 2.添加到mContactList
				mContactList.addAll(contactList);
			}
			// 3.设置集合中每个元素的header值
			for (UserBean user : contactList) {
				UserUtils.setUserHearder(user.getUserName(), user);
			}
			if(mContactList.size()>2){
				// 4.mContactList升序排序，按user的header属性排序
				Collections.sort(mContactList, new Comparator<UserBean>() {

					@Override
					public int compare(UserBean user1, UserBean user2) {
						if(user1.getId()>0&&user2.getId()>0 ){
							return user1.getHeader().compareTo(user2.getHeader());
						}
						return 0;
					}
				});
			}
			
			mAdapter.notifyDataSetChanged();
		}
	}
}
