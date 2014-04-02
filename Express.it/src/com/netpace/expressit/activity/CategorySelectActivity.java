/*******************************************************************************
 * Copyright 2013 Adnan Urooj (Deminem)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 * 	
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.netpace.expressit.activity;

import static com.netpace.expressit.constants.AppConstants.SELECTED_CATEGORY_ID;
import static com.netpace.expressit.constants.AppConstants.SELECTED_CATEGORY_STR;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netpace.expressit.R;

public class CategorySelectActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);
        
        ListView listview = (ListView) findViewById(R.id.category_listView);
        final CategoryListAdapter listAdapter = new CategoryListAdapter(this);
        
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setAdapter(listAdapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				final String selectedItem = (String) parent.getItemAtPosition(position);
				final int categoryId = listAdapter.getCategoryId(position);
				
				Intent intent = new Intent();
				intent.putExtra(SELECTED_CATEGORY_STR, selectedItem);
				intent.putExtra(SELECTED_CATEGORY_ID, categoryId);
				setResult(RESULT_OK, intent);        
				finish();
			}
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    
    /**
     * Category List Adapater
     * 
     * @author Adnan
     *
     */
    public class CategoryListAdapter extends BaseAdapter {

        private String[] categories = {"News", "Entertainment", "Technology", "Viral", "Lifestyle", "Shows"};
        private int[] categoryIcons = {R.drawable.ic_news, R.drawable.ic_entertainment, 
        		R.drawable.ic_technology, R.drawable.ic_viral, R.drawable.ic_viral, R.drawable.ic_viral};
        
        private int[] categoryIds = {1, 2, 6, 4, 10, 7};
        
        private Context mContext;

        private LayoutInflater mLayoutInflater;
        
        public CategoryListAdapter(Context context) {                    

           mContext = context;
           mLayoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
		@Override
		public int getCount() {
			return categories.length;
		}

		@Override
		public Object getItem(int position) {
			return categories[ position ];
		}

		@Override
		public long getItemId(int position) {
			return categories[ position ].hashCode();
		}

		public int getCategoryId(int position) {
			return categoryIds[ position ];
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final RelativeLayout itemView;
			if (convertView == null) {
				itemView = (RelativeLayout) mLayoutInflater.inflate(
						R.layout.category_list_item, parent, false);
				
				final TextView textView = (TextView) itemView.findViewById(R.id.category_title);
				final ImageView imageView = (ImageView) itemView.findViewById(R.id.category_icon);
				
				textView.setText(categories[position]);
				imageView.setImageResource(categoryIcons[position]);
				textView.setOnLongClickListener(new View.OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						itemView.setBackgroundResource(R.drawable.list_item_selector);
						
						return true;
					}
				});
				textView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						itemView.setBackgroundResource(R.drawable.list_item_selector);
					}
				});
			} else {
				itemView = (RelativeLayout) convertView;
			}
			
			return itemView;
		}
    }
}

