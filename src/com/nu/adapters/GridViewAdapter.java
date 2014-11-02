package com.nu.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;

import android.graphics.BitmapFactory;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GridViewAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<String> imageSourceList = new ArrayList<String>();

	public GridViewAdapter(Context c, ArrayList<String> imageSource) {
		mContext = c;
		imageSourceList = imageSource;
	}

	public int getCount() {
		return imageSourceList.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(185, 185));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}

		imageView.setImageBitmap(BitmapFactory.decodeFile(imageSourceList.get(position)));
		return imageView;
	}
}
