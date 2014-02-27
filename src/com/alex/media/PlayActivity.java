package com.alex.media;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import com.alex.media.LRCbean;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayActivity extends Activity implements MediaPlayer.OnCompletionListener{
	private int[] _ids;
	private int position;
	private MediaPlayer mp=null;
	private AudioManager mAudioManager = null;
	private Uri uri;
	private ImageButton playBtn = null;//播放、暂停
	//private Button stopBtn = null;//停止
	private ImageButton latestBtn = null;//上一首
	private ImageButton nextButton = null;//下一首
	private ImageButton forwardBtn = null;//快进
	private ImageButton rewindBtn = null;//快退
	private TextView lrcText = null;//歌词文本
	private TextView playtime = null;//已播放时间
	private TextView durationTime = null;//歌曲时间
	private SeekBar seekbar = null;//歌曲进度
	private SeekBar soundBar = null;//音量调节
	private Handler handler = null;//用于进度条
	private Handler fHandler = null;//用于快进
    private int currentPosition;//当前播放位置
    private DBHelper dbHelper = null;
	
	
	private TreeMap<Integer, LRCbean> lrc_map = new TreeMap<Integer, LRCbean>();
	private Cursor myCur;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		_ids = bundle.getIntArray("_ids");
		position = bundle.getInt("position");
		fHandler = new Handler();
		fHandler.removeCallbacks(forward);
		lrcText = (TextView)findViewById(R.id.lrc);
		
		
		
		
		
		/*歌曲时间*/
		playtime = (TextView)findViewById(R.id.playtime);//已经播放的时间
		durationTime = (TextView)findViewById(R.id.duration);//歌曲总时间
		
		
		/*播放、暂停、停止按钮设置*/
		playBtn = (ImageButton)findViewById(R.id.playBtn);//开始播放
		playBtn.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				if (mp.isPlaying()){
					pause();
					playBtn.setBackgroundResource(R.drawable.play_selecor);
				} else{
					play();
					playBtn.setBackgroundResource(R.drawable.pause_selecor);
					
				}
			}
		});
		
		
		/*stopBtn = (Button)findViewById(R.id.stopBtn);//停止播放
		stopBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stop();
			}
		});
		*/
		
		
		
		/*上一首、下一首*/
		latestBtn = (ImageButton)findViewById(R.id.latestBtn);
		latestBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int num = _ids.length;
				if(position==0){
					position=num-1;
				}else{
					position-=1;
				}
				System.out.println(position);
				int pos = _ids[position];
			
				lrc_map.clear();
				setup();
				play();
				
				
			}
		});
		
		nextButton = (ImageButton)findViewById(R.id.nextBtn);
		nextButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int num = _ids.length;
				if (position==num-1){
					position=0;
				}else{
					position+=1;
				}
				int pos = _ids[position];
				lrc_map.clear();
				setup();
				play();
			}
		});
		
		/*快进、快退*/
		forwardBtn = (ImageButton)findViewById(R.id.forwardBtn);//快进
		forwardBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					fHandler.post(forward);
					mp.pause();
					break;

				case MotionEvent.ACTION_UP:
					fHandler.removeCallbacks(forward);
					mp.start();
					playBtn.setBackgroundResource(R.drawable.pause_selecor);
					break;
				}
				return false;
			}
		});
		
		rewindBtn = (ImageButton)findViewById(R.id.rewindBtn);
		rewindBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					fHandler.post(rewind);
					mp.pause();
					break;

				case MotionEvent.ACTION_UP:
					fHandler.removeCallbacks(rewind);
					mp.start();
					playBtn.setBackgroundResource(R.drawable.pause_selecor);
					break;
				}
				return false;
			}
		});
		
		
		/*SeekBar进度条*/
		seekbar = (SeekBar)findViewById(R.id.seekbar);
		
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mp.start();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mp.pause();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					mp.seekTo(progress);
				}
			}
		});
		
		/*音量控制条*/
		soundBar = (SeekBar)findViewById(R.id.sound);
		soundBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser){
					int ScurrentPosition = soundBar.getProgress();
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, ScurrentPosition, 0);
					
				}
			}
		});
		setup();//准备播放
		play();//开始播放
		
	}
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == event.KEYCODE_BACK) {
			if (mp != null) {
				mp.reset();
				mp.release();
				mp = null;
			}
		}
		fHandler.removeCallbacks(forward);
		fHandler.removeCallbacks(rewind);
		fHandler=null;
		handler.removeMessages(1);
		handler=null;
		dbHelper.close();
		Intent intent = new Intent(this, ListActivity.class);
		startActivity(intent);
		finish();
		return true;
	}
	
	


	private void loadClip(){
		if (mp != null) {
			mp.reset();
			mp.release();
			mp = null;
		}
		mp = new MediaPlayer();//创建多媒体对象
		mp.setOnCompletionListener(this);
		int pos = _ids[position];
		DBOperate(pos);
	    uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				"" + pos);
	    try {
			mp.setDataSource(this, uri);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void setup(){
		refreshView();
		loadClip();
		init();
		try {
			mp.prepare();
			mp.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(final MediaPlayer mp) {
					seekbar.setMax(mp.getDuration());//设置播放进度条最大值
					handler.sendEmptyMessage(1);//向handler发送消息，启动播放进度条
					playtime.setText(toTime(mp.getCurrentPosition()));//初始化播放时间
					durationTime.setText(toTime(mp.getDuration()));//设置歌曲时间
					mp.seekTo(currentPosition);//初始化MediaPlayer播放位置
					/*获得音乐最大音量*/
					mAudioManager = (AudioManager) PlayActivity.this.getSystemService(PlayActivity.this.AUDIO_SERVICE);
					int maxSound = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
					
					/*获得当前音乐音量*/
					int currentSound = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
					
					soundBar.setMax(maxSound);
					soundBar.setProgress(currentSound);

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void onCompletion(MediaPlayer mp) {//循环播放
		int num = _ids.length;
		if (position==num-1){
			position=0;
		}else{
			position+=1;
		}
		System.out.println(position);
		int pos = _ids[position];
		lrc_map.clear();
		refreshView();
		setup();
		play();
	}
	
	private void play(){
		fHandler.removeCallbacks(forward);
		fHandler.removeCallbacks(rewind);
		mp.start();
		playBtn.setBackgroundResource(R.drawable.pause_selecor);
		
	}
	
	private void pause(){
		fHandler.removeCallbacks(forward);
		fHandler.removeCallbacks(rewind);
		mp.pause();
		
	}
	
	private void stop(){
		mp.stop();
		fHandler.removeCallbacks(forward);
		fHandler.removeCallbacks(rewind);
		playBtn.setBackgroundResource(R.drawable.play_selecor);
		try {
			mp.prepare();
			mp.seekTo(0);
			seekbar.setProgress(mp.getCurrentPosition());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	 
	private void init(){
		 handler = new Handler(){
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					if(mp!=null)
						currentPosition = mp.getCurrentPosition();
					seekbar.setProgress(currentPosition);
					playtime.setText(toTime(currentPosition));
					handler.sendEmptyMessage(1);
					Iterator<Integer> iterator=lrc_map.keySet().iterator();
					while(iterator.hasNext()){
						Object o = iterator.next();
			        	LRCbean val = lrc_map.get(o);
			        	if (val!=null){
			        		
				        	if (mp.getCurrentPosition()>val.getBeginTime()
				        			&&mp.getCurrentPosition()<val.getBeginTime()+val.getLineTime()){
				        		lrcText.setText(val.getLrcBody());
				        		System.out.println("123");
				        		break;
				        	}
			        	}
					}
					break;

				default:
					break;
				}
				
			}
		};
	}
	
	public String toTime(int time) {

		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}
	
	private void DBOperate(int pos){
		//数据库操作
		dbHelper = new DBHelper(this, "music.db", null, 2);
		Cursor c = dbHelper.query(pos);
		Date currentTime = new Date();   
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
		String dateString = formatter.format(currentTime); 
		if (c==null||c.getCount()==0){//如果查询结果为空			
			ContentValues values = new ContentValues();
			values.put("music_id", pos);
			values.put("clicks", 1);
			values.put("latest", dateString);
			dbHelper.insert(values);
		} else{
			c.moveToNext();
			int clicks = c.getInt(2);
			clicks++;
			ContentValues values = new ContentValues();
			values.put("clicks", clicks);
			values.put("latest", dateString);
			dbHelper.update(values, pos);
			c.close();
		}
	}
	
	Runnable forward = new Runnable() {//快进
		
		@Override
		public void run() {
			if(currentPosition<=mp.getDuration()){
				currentPosition+=5000;
				mp.seekTo(currentPosition);
				fHandler.postDelayed(forward, 500);
			}else{
				fHandler.removeCallbacks(forward);
			}
			
		}
	};
	
	Runnable rewind = new Runnable() {//快退
		
		@Override
		public void run() {
			if (currentPosition>=0){
				currentPosition-=5000;
				mp.seekTo(currentPosition);
				fHandler.postDelayed(rewind, 500);
			}else{
				fHandler.removeCallbacks(rewind);
			}
		}
	};
	
	
	
	private void read(String path){
    	TreeMap<Integer, LRCbean> lrc_read = new TreeMap<Integer, LRCbean>();
    	String data = "";
    	BufferedReader br = null;
    	File file = new File(path);
    	if (!file.exists()){
    		lrcText.setText("歌词文件不存在...");
    		return;
    	}
    	FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(
					stream, "GB2312"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			while((data=br.readLine())!=null){
				if (data.length()>6){
					if (data.charAt(3)==':'&&data.charAt(6)=='.'){//从歌词正文开始
						data = data.replace("[", "");
						data = data.replace("]", "@");
						data = data.replace(".", ":");
						String lrc[] = data.split("@");
						String lrcContent= null;
						if (lrc.length==2){
						lrcContent = lrc[lrc.length-1];//歌词
						}else{
						lrcContent = "";
						}
						String lrcTime[] = lrc[0].split(":");
						
						int m = Integer.parseInt(lrcTime[0]);//分
						int s = Integer.parseInt(lrcTime[1]);//秒
						int ms = Integer.parseInt(lrcTime[2]);//毫秒
						
						int begintime = (m*60 + s) * 1000 + ms;//转换成毫秒
						LRCbean lrcbean = new LRCbean();
						lrcbean.setBeginTime(begintime);//设置歌词开始时间
						lrcbean.setLrcBody(lrcContent);//设置歌词的主体
						lrc_read.put(begintime,lrcbean);
					}
				}
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//计算每句歌词需要的时间
		lrc_map.clear();
		data = "";
		Iterator<Integer> iterator = lrc_read.keySet().iterator();
		LRCbean oldval = null;
		int i = 0;
		while (iterator.hasNext()){
			Object ob = iterator.next();
			LRCbean val = lrc_read.get(ob);
			if (oldval==null){
				oldval = val;
			} else{
				LRCbean item1 = new LRCbean();
				item1 = oldval;
				item1.setLineTime(val.getBeginTime()-oldval.getBeginTime());
				lrc_map.put(new Integer(i), item1);
				i++;
				oldval = val;
			}
		}
		
    }
	
	public void refreshView() {
		myCur = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.DISPLAY_NAME }, "_id=?",
				new String[] { _ids[position] + "" }, null);
		myCur.moveToFirst();
		
		String name = myCur.getString(4).substring(0,
				myCur.getString(4).lastIndexOf("."));
		System.out.println(name);
		read("/sdcard/" + name + ".lrc");
	}

	 
			


}
