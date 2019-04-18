/*
 * Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
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
package sb.firefds.pie.firefdskit.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.Date;

public class BackupAdapter extends BaseAdapter {

    private Context context;
    private File[] backups;

    public BackupAdapter(Context context, File[] backups) {
        this.context = context;
        this.backups = backups;
    }

    @Override
    public int getCount() {
        return backups.length;
    }

    @Override
    public Object getItem(int position) {
        return backups[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater
                    .from(context)
                    .inflate(android.R.layout.simple_list_item_2, null);
            ((TextView) convertView
                    .findViewById(android.R.id.text1))
                    .setText(backups[position].getName().replace(".fk", ""));
            ((TextView) convertView.findViewById(android.R.id.text2))
                    .setText(new Date(backups[position].lastModified()).toString());
        }
        return convertView;
    }

}
