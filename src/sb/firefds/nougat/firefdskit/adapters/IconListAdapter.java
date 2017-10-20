/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Modified for Samsung Roms by Mohamed Karami (Wanam@Xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sb.firefds.nougat.firefdskit.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import sb.firefds.nougat.firefdskit.adapters.BaseListAdapterFilter.IBaseListAdapterFilterable;

public class IconListAdapter extends ArrayAdapter<IIconListAdapterItem> implements
		IBaseListAdapterFilterable<IIconListAdapterItem> {
	private Context mContext;
	private List<IIconListAdapterItem> mData = null;
	private List<IIconListAdapterItem> mFilteredData = null;
	private android.widget.Filter mFilter;

	public IconListAdapter(Context context, List<IIconListAdapterItem> objects) {
		super(context, android.R.layout.simple_list_item_1, objects);

		mContext = context;
		mData = new ArrayList<IIconListAdapterItem>(objects);
		mFilteredData = new ArrayList<IIconListAdapterItem>(objects);
	}

	static class ViewHolder {
		TextView text;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

			holder = new ViewHolder();
			holder.text = (TextView) row.findViewById(android.R.id.text1);
			holder.text.setCompoundDrawablePadding(20);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		IIconListAdapterItem item = mFilteredData.get(position);

		holder.text.setText(item.getText());
		holder.text.setTextColor(Color.BLACK);
		holder.text.setCompoundDrawablesWithIntrinsicBounds(item.getIconLeft(), null, item.getIconRight(), null);

		return row;
	}

	@Override
	public android.widget.Filter getFilter() {
		if (mFilter == null)
			mFilter = new BaseListAdapterFilter<IIconListAdapterItem>(this);

		return mFilter;
	}

	@Override
	public List<IIconListAdapterItem> getOriginalData() {
		return mData;
	}

	@Override
	public List<IIconListAdapterItem> getFilteredData() {
		return mFilteredData;
	}

	@Override
	public void onFilterPublishResults(List<IIconListAdapterItem> results) {
		mFilteredData = results;
		clear();
		for (int i = 0; i < mFilteredData.size(); i++) {
			IIconListAdapterItem item = mFilteredData.get(i);
			add(item);
		}
	}
}