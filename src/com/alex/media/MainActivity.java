package com.alex.media;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity{
	private ListView listview=null;
	private Intent intent = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		String[] strs = {"全部音乐","最近播放列表","最经常播放列表"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,strs);
		listview = new ListView(this);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				switch (position) {
				case 0:
			        intent = new Intent();
					intent.setClass(MainActivity.this, ListActivity.class);
					startActivity(intent);
					break;

				case 1:
					intent = new Intent();
					intent.setClass(MainActivity.this, RecentlyActivity.class);
					startActivity(intent);
					break;
				case 2:
					intent = new Intent();
					intent.setClass(MainActivity.this, ClicksActivity.class);
					startActivity(intent);
					break;
				}
				
			}
		});
		this.setContentView(listview);
	}
	
}
