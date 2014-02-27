package com.alex.media;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.alex.media.ClicksActivity.ListItemClickListener;

public class RecentlyActivity extends Activity {
	private DBHelper dbHelper = null;
	private ListView listview;
	private int[] _ids;
	Cursor cursor = null;
	int[] music_id;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		dbHelper = new DBHelper(this, "music.db", null, 2);
		cursor = dbHelper.queryRecently();
		cursor.moveToFirst();
		int num;
		if (cursor!=null){
			num = cursor.getCount();
		} else{
			return;
		}
		String idString ="";
		if (num>=10){
			for(int i=0;i<10;i++){
				music_id = new int[10];
				music_id[i]=cursor.getInt(cursor.getColumnIndex("music_id"));
				if (i<9){
					idString = idString+music_id[i]+",";
				} else{
					idString = idString+music_id[i];
				}
				cursor.moveToNext();
			} 
		}else if(num>0){
			for(int i=0;i<num;i++){
				music_id = new int[num];
				music_id[i]=cursor.getInt(cursor.getColumnIndex("music_id"));
				if (i<num-1){
					idString = idString+music_id[i]+",";
				} else{
					idString = idString+music_id[i];
				}
				cursor.moveToNext();
			}
		}
		if (cursor!=null){
			cursor.close();
			cursor=null;
		}
		System.out.println(idString);
		listview = new ListView(this);
		Cursor c = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        			new String[]{MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media._ID,
					MediaStore.Audio.Media.DISPLAY_NAME} , MediaStore.Audio.Media._ID + " in ("+ idString + ")", null,null);
		
		  c.moveToFirst();
	        _ids = new int[c.getCount()];
	        for(int i=0;i<c.getCount();i++){
	        	_ids[i] = c.getInt(3);
	        	System.out.println(_ids[i]+":");
	        	c.moveToNext();
	        }
	        listview.setAdapter(new MusicListAdapter(this, c));
	        listview.setOnItemClickListener(new ListItemClickListener());
	        setContentView(listview);
	    }
	   
	    class ListItemClickListener implements OnItemClickListener{

	    	@Override
	    	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
	    		// TODO Auto-generated method stub
	    		Intent intent = new Intent(RecentlyActivity.this,PlayActivity.class);
	    		intent.putExtra("_ids", _ids);
	    		intent.putExtra("position", position);
	    		startActivity(intent);
	    	}
	    	
	    }
}
