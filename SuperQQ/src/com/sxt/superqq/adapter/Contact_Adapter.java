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
package com.sxt.superqq.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.easemob.util.EMLog;
import com.sxt.superqq.Constant;
import com.sxt.superqq.I;
import com.sxt.superqq.R;
import com.sxt.superqq.bean.UserBean;
import com.sxt.superqq.domain.User;
import com.sxt.superqq.utils.ImageLoader;
import com.sxt.superqq.utils.ImageLoader.OnImageLoadListener;

/**
 * 简单的好友Adapter实现
 * 
 */
public class Contact_Adapter extends BaseAdapter implements SectionIndexer {
	private static final String TAG = "ContactAdapter";
	List<String> list;
	// 添加当前适配器显示的集合数据
	List<UserBean> userList;

	// 以下两个集合，弃用
	List<User> user_list;
	List<User> copyUserList;
	private LayoutInflater layoutInflater;
	private SparseIntArray positionOfSection;
	private SparseIntArray sectionOfPosition;
	private int res;
	private MyFilter myFilter;
	private boolean notiyfyByFilter;
	Context context;

	/** 异步加载头像的类 */
	ImageLoader mImageLoader;

	public Contact_Adapter(Context context, int resource, List<UserBean> objects) {
		this.context = context;
		this.res = resource;
		this.userList = objects;
		copyUserList = new ArrayList<User>();
		user_list = new ArrayList<User>();
		// copyUserList.addAll(objects);
		layoutInflater = LayoutInflater.from(context);
		mImageLoader = ImageLoader.getInstance(context);
	}

	/**
	 * 删除指定user的联系人
	 * 
	 * @param user
	 */
	public void remove(UserBean user) {
		userList.remove(user);
		notifyDataSetChanged();
	}

	private static class ViewHolder {
		ImageView avatar;
		TextView unreadMsgView;
		TextView nameTextview;
		TextView tvHeader;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = layoutInflater.inflate(res, null);
			holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			holder.unreadMsgView = (TextView) convertView
					.findViewById(R.id.unread_msg_number);
			holder.nameTextview = (TextView) convertView
					.findViewById(R.id.name);
			holder.tvHeader = (TextView) convertView.findViewById(R.id.header);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		UserBean user = getItem(position);
		if (user != null) {
			Log.i("main", user.toString());
		}
		// 设置nick，demo里不涉及到完整user，用username代替nick显示
		String username = user.getUserName();
		String header = user.getHeader();
		if (position == 0 || header != null
				&& !header.equals(getItem(position - 1).getHeader())) {
			if ("".equals(header)) {
				holder.tvHeader.setVisibility(View.GONE);
			} else {
				holder.tvHeader.setVisibility(View.VISIBLE);
				holder.tvHeader.setText(header);
			}
		} else {
			holder.tvHeader.setVisibility(View.GONE);
		}
		// 显示申请与通知item
		if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
			holder.nameTextview.setText(user.getNick());
			holder.avatar.setImageResource(R.drawable.new_friends_icon);
			if (user.getUnreadMsgCount() > 0) {
				holder.unreadMsgView.setVisibility(View.VISIBLE);
				holder.unreadMsgView.setText(user.getUnreadMsgCount() + "");
			} else {
				holder.unreadMsgView.setVisibility(View.INVISIBLE);
			}
		} else if (username.equals(Constant.GROUP_USERNAME)) {
			// 群聊item
			holder.nameTextview.setText(user.getNick());
			holder.avatar.setImageResource(R.drawable.groups_icon);
		} else {
			holder.nameTextview.setText(username);
			// 设置用户头像
			// UserUtils.setUserAvatar(getContext(), username, holder.avatar);
			if (holder.unreadMsgView != null)
				holder.unreadMsgView.setVisibility(View.INVISIBLE);

			// 获取联系人头像下载的完整地址
			String path = I.DOWNLOAD_AVATAR_URL.replace("&userName=",
				"&userName=" + user.getUserName()) + user.getAvatar();
			// 下载头像并显示
			Bitmap bitmap = mImageLoader.displayImage(path,80,80,new OnImageLoadListener() {
				@Override
				public void onSuccess(String path, Bitmap bitmap) {
					ImageView iv = (ImageView) parent
							.findViewWithTag(path);
					if (iv != null) {
						iv.setImageBitmap(bitmap);
					}
				}

				@Override
				public void error(String errorMsg) {
					// TODO Auto-generated method stub

				}
			});
			if (bitmap == null) {
				holder.avatar.setImageResource(R.drawable.default_avatar);
			} else {
				holder.avatar.setImageBitmap(bitmap);
			}
		}
		return convertView;
	}

	@Override
	public UserBean getItem(int position) {
		return userList.get(position);
	}

	@Override
	public int getCount() {
		return userList.size();
	}

	public int getPositionForSection(int section) {
		return positionOfSection.get(section);
	}

	public int getSectionForPosition(int position) {
		return sectionOfPosition.get(position);
	}

	@Override
	public Object[] getSections() {
		positionOfSection = new SparseIntArray();
		sectionOfPosition = new SparseIntArray();
		int count = getCount();
		list = new ArrayList<String>();
		list.add(context.getString(R.string.search_header));
		positionOfSection.put(0, 0);
		sectionOfPosition.put(0, 0);
		for (int i = 1; i < count; i++) {

			String letter = getItem(i).getHeader();
			System.err.println("contactadapter getsection getHeader:" + letter
					+ " name:" + getItem(i).getUserName());
			int section = list.size() - 1;
			if (list.get(section) != null && !list.get(section).equals(letter)) {
				list.add(letter);
				section++;
				positionOfSection.put(section, i);
			}
			sectionOfPosition.put(i, section);
		}
		return list.toArray(new String[list.size()]);
	}

	public Filter getFilter() {
		if (myFilter == null) {
			myFilter = new MyFilter(user_list);
		}
		return myFilter;
	}

	private class MyFilter extends Filter {
		List<User> mOriginalList = null;

		public MyFilter(List<User> myList) {
			this.mOriginalList = myList;
		}

		@Override
		protected synchronized FilterResults performFiltering(
				CharSequence prefix) {
			FilterResults results = new FilterResults();
			if (mOriginalList == null) {
				mOriginalList = new ArrayList<User>();
			}
			EMLog.d(TAG, "contacts original size: " + mOriginalList.size());
			EMLog.d(TAG, "contacts copy size: " + copyUserList.size());

			if (prefix == null || prefix.length() == 0) {
				results.values = copyUserList;
				results.count = copyUserList.size();
			} else {
				String prefixString = prefix.toString();
				final int count = mOriginalList.size();
				final ArrayList<User> newValues = new ArrayList<User>();
				for (int i = 0; i < count; i++) {
					final User user = mOriginalList.get(i);
					String username = user.getUsername();

					if (username.startsWith(prefixString)) {
						newValues.add(user);
					} else {
						final String[] words = username.split(" ");
						final int wordCount = words.length;

						// Start at index 0, in case valueText starts with
						// space(s)
						for (int k = 0; k < wordCount; k++) {
							if (words[k].startsWith(prefixString)) {
								newValues.add(user);
								break;
							}
						}
					}
				}
				results.values = newValues;
				results.count = newValues.size();
			}
			EMLog.d(TAG, "contacts filter results size: " + results.count);
			return results;
		}

		@Override
		protected synchronized void publishResults(CharSequence constraint,
				FilterResults results) {
			user_list.clear();
			user_list.addAll((List<User>) results.values);
			EMLog.d(TAG, "publish contacts filter results size: "
					+ results.count);
			if (results.count > 0) {
				notiyfyByFilter = true;
				notifyDataSetChanged();
				notiyfyByFilter = false;
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if (!notiyfyByFilter) {
			copyUserList.clear();
			copyUserList.addAll(user_list);
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

}
