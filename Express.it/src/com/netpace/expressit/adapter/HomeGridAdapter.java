package com.netpace.expressit.adapter;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.netpace.expressit.R;
import com.netpace.expressit.model.Media;
import com.netpace.expressit.model.Media.MediaTypeEnum;

public class HomeGridAdapter extends BaseAdapter{

	private Context mContext;
	private	LayoutInflater mInflater;
	private List<Media> mediaArray;
	
	
	public HomeGridAdapter(Context mContext,List<Media> mediaArray) {
		super();
		this.mContext = mContext;
		this.mInflater = (LayoutInflater)
				mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		this.mediaArray = mediaArray;
	}

	private class ViewHolder{
		ImageView imageView;
		ImageView videoThumbView;
	}
	
	@Override
	public int getCount() {
		return mediaArray.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mediaArray.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.home_grid_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView)convertView.findViewById(R.id.grid_item_image);
			holder.videoThumbView = (ImageView)convertView.findViewById(R.id.grid_item_watermark);
			convertView.setTag(holder);
		}else {
            holder = (ViewHolder) convertView.getTag();
        }
		
		Media media = (Media) getItem(position);
		if(media.getMediaType() == MediaTypeEnum.IMAGE){
			Ion.with(holder.imageView)
			.placeholder(R.drawable.ic_launcher_ctv)
			.error(R.drawable.ic_launcher)
			.load("http://thumbs.carbonated.tv/125136_pod.jpg");
//			holder.imageView.setImageResource(R.drawable.ic_launcher_ctv);
			holder.videoThumbView.setVisibility(View.GONE);
		}else{
			Ion.with(holder.imageView)
			.placeholder(R.drawable.ic_launcher_ctv)
			.error(R.drawable.ic_launcher)
			.load("http://thumbs.carbonated.tv/125136_pod.jpg");
//			holder.imageView.setImageResource(R.drawable.ic_launcher_ctv);
		}
		
		return convertView;
	}

	
	public void setMediaArray(List<Media> mediaArray) {
		this.mediaArray = mediaArray;
	}
	
	
	

}
