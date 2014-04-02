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

package com.netpace.expressit.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ad.mediasharing.ADMediaSharingUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase.MoreItemScrollState;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.netpace.expressit.R;
import com.netpace.expressit.activity.UploadImageStoryActivity;
import com.netpace.expressit.activity.UploadVideoStoryActivity;
import com.netpace.expressit.adapter.HomeGridAdapter;
import com.netpace.expressit.constants.AppConstants;
import com.netpace.expressit.model.Media;
import com.netpace.expressit.utils.URLUtil;
import com.netpace.expressit.utils.Util;

public class MainGridFragment extends Fragment implements OnItemClickListener{

	
	private final static String TAG = MainGridFragment.class.getName();
	
	private List<Media> mediaArray = new ArrayList<Media>();
	private HomeGridAdapter adapter;
	private GridView gridView;
	private PullToRefreshGridView mPullRefreshGridView;
	private ProgressBar progressBar;
		
	public MainGridFragment() {
		super();
	}

	@Override
	public void onResume(){
		super.onResume();
		fetchMediaFromRemote(Boolean.TRUE);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_main_grid, container, false);
        
                
        mPullRefreshGridView = (PullToRefreshGridView) rootView.findViewById(R.id.home_grid_view);
        gridView = mPullRefreshGridView.getRefreshableView();
        progressBar = (ProgressBar) rootView.findViewById(R.id.grid_progress_view);
        
        
        mPullRefreshGridView.setOnRefreshListener(new OnRefreshListener<GridView>() {

			@Override
			public void onRefresh(PullToRefreshBase<GridView> refreshView) {
				fetchMediaFromRemote(Boolean.TRUE);
			}
		});
        
        mPullRefreshGridView.setMoreItemScrollState(MoreItemScrollState.MORE_ITEM_SCROLL_MID_POINT);
        mPullRefreshGridView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				if(mediaArray.size() < AppConstants.MEDIA_THRESHOLD){
					fetchMediaFromRemote(Boolean.FALSE);
				}
			}
		});  
       
        adapter = new HomeGridAdapter(getActivity().getApplicationContext(), mediaArray);
        gridView.setAdapter(adapter);
        
        gridView.setOnItemClickListener(this);
        
        return rootView;
    }
	
	
    
    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_item_media_upload:
			showUploadMediaDailog();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return false;
	}
	
	private void showUploadMediaDailog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		String[] optionArray = {"Image","Video"};
		builder.setTitle("Choose Media").setItems(optionArray, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				switch (arg1) {
				case 0:
					gotoUploadImageStory();
					break;
				case 1:
					gotoUploadVideoStory();
					break;
				default:
					break;
				}
			}
		}).show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Item Selected :: "+position);
		Toast.makeText(getActivity(), "Item Selected :: "+position,
				Toast.LENGTH_SHORT).show();
	}
    
    
    
    public void gotoUploadImageStory() {
        Intent intent = new Intent(getActivity(), UploadImageStoryActivity.class);
        startActivity(intent);
    }
    
    public void gotoUploadVideoStory() {
        Intent intent = new Intent(getActivity(), UploadVideoStoryActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    private String getEmailId() {
    	Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
    	Account[] accounts = AccountManager.get(getActivity().getApplicationContext()).getAccounts();
    	for (Account account : accounts) {
    	    if (emailPattern.matcher(account.name).matches()) {
    	        String possibleEmail = account.name;
    	        return possibleEmail;
    	    }
    	}
    	
    	return null;
    }
    
    
    /*
     * Update GridView Data
     * 
     * @param: Media Array : ArrayList
     * 
     * 
     * */
    
    private void updateGridViewData(List<Media> mArray){
    	progressBar.setVisibility(View.INVISIBLE);
    	adapter.setMediaArray(mArray);
		adapter.notifyDataSetChanged();
		mPullRefreshGridView.onRefreshComplete();
    }
    
    
    /*
     * Fetch Media from remote server
     * 
     * @param: PageNo : Integer
     * @param: Size	  : Integer
     * 
     * @return void
     * */
    Future<Response<JsonArray>> loading;
    
    private void fetchMediaFromRemote(final boolean refresh){

    	if (loading != null && !loading.isDone() && !loading.isCancelled())
             return;
    	if((mediaArray.size() % AppConstants.MEDIA_CHUNK_SIZE) > 0 && refresh == false) return;
    	if(!Util.isNetworkAvailable()){
 			Toast.makeText(getActivity(),R.string.no_internet_available, Toast.LENGTH_SHORT).show();
 			return;
 		}
    	int page = mediaArray.size()  / AppConstants.MEDIA_CHUNK_SIZE;
    	if(refresh)page=0;
    	 
    	loading= ADMediaSharingUtil.getRestClient(getActivity())
    	.load(URLUtil.getURIWithPageNoAndSize(AppConstants.GET_MEDIA_URL, page, AppConstants.MEDIA_CHUNK_SIZE))
    	.asJsonArray()
    	.withResponse()
    	.setCallback(new FutureCallback<Response<JsonArray>>() {
			@Override
			public void onCompleted(Exception arg0, Response<JsonArray> arg1) {
				if(arg1.getResult().size() > 0){
					if(refresh) mediaArray.clear();
					Iterator<JsonElement> element = arg1.getResult().iterator();
					Gson gson = new Gson();
					while (element.hasNext()) {
						JsonElement jsonElement = (JsonElement) element.next();
						Media media =  gson.fromJson(jsonElement, Media.class);
						mediaArray.add(media);
					}
					updateGridViewData(mediaArray);
				}else{
					Log.i(TAG, "Data not found...!!!");
					mPullRefreshGridView.onRefreshComplete();
				}
			}
		});
    }
    
}
