package com.alex.media;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter {

	private Context myCon;
	private Cursor myCur;

	public MusicListAdapter(Context con, Cursor cur) {
		myCon = con;
		myCur = cur;
	}

	@Override
	public int getCount() {
		return myCur.getCount();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(myCon).inflate(R.layout.musiclist,
				null);
		myCur.moveToPosition(position);
		TextView tv_music = (TextView) convertView.findViewById(R.id.music);
		tv_music.setText(myCur.getString(0));
		TextView tv_singer = (TextView) convertView.findViewById(R.id.singer);
		tv_singer.setText(myCur.getString(2));
		TextView tv_time = (TextView) convertView.findViewById(R.id.time);
		tv_time.setText(toTime(myCur.getInt(1)));
		return convertView;
	}

	public String toTime(int time) {

		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

}
