package com.alex.media;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ListActivity extends Activity {
    /** Called when the activity is first created. */
	private ListView listview;
	private int[] _ids;
	private ScanSdReceiver scanSdReceiver = null;
	private AlertDialog ad = null;
	private AlertDialog.Builder  builder = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scanSdCard();

        
        listview = new ListView(this);
        Cursor c = this.getContentResolver()
        		.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        				new String[]{MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media._ID,
						MediaStore.Audio.Media.DISPLAY_NAME },
        				null, null, null);
        if (c==null || c.getCount()==0){
        	builder = new AlertDialog.Builder(this);
			builder.setMessage("存储列表为空...").setPositiveButton("确定", null);
			ad = builder.create();
			ad.show();
        }
        c.moveToFirst();
        _ids = new int[c.getCount()];
        for(int i=0;i<c.getCount();i++){
        	_ids[i] = c.getInt(3);
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
    		Intent intent = new Intent(ListActivity.this,PlayActivity.class);
    		intent.putExtra("_ids", _ids);
    		intent.putExtra("position", position);
    		startActivity(intent);
    	}
    	
    }
    
    private void scanSdCard(){
    	IntentFilter intentfilter = new IntentFilter( Intent.ACTION_MEDIA_SCANNER_STARTED);
    	intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
    	intentfilter.addDataScheme("file");
    	scanSdReceiver = new ScanSdReceiver();
    	registerReceiver(scanSdReceiver, intentfilter);
    	sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
    			Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == event.KEYCODE_BACK) {
			if (scanSdReceiver!=null)
				unregisterReceiver(scanSdReceiver);
			this.finish();
			Intent intent = new Intent();
			intent.setClass(this, MainActivity.class);
		}
		return true;
    }


}

