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
	private ImageButton playBtn = null;//���š���ͣ
	//private Button stopBtn = null;//ֹͣ
	private ImageButton latestBtn = null;//��һ��
	private ImageButton nextButton = null;//��һ��
	private ImageButton forwardBtn = null;//���
	private ImageButton rewindBtn = null;//����
	private TextView lrcText = null;//����ı�
	private TextView playtime = null;//�Ѳ���ʱ��
	private TextView durationTime = null;//����ʱ��
	private SeekBar seekbar = null;//��������
	private SeekBar soundBar = null;//��������
	private Handler handler = null;//���ڽ�����
	private Handler fHandler = null;//���ڿ��
    private int currentPosition;//��ǰ����λ��
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
		
		
		
		
		
		/*����ʱ��*/
		playtime = (TextView)findViewById(R.id.playtime);//�Ѿ����ŵ�ʱ��
		durationTime = (TextView)findViewById(R.id.duration);//������ʱ��
		
		
		/*���š���ͣ��ֹͣ��ť����*/
		playBtn = (ImageButton)findViewById(R.id.playBtn);//��ʼ����
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
		
		
		/*stopBtn = (Button)findViewById(R.id.stopBtn);//ֹͣ����
		stopBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stop();
			}
		});
		*/
		
		
		
		/*��һ�ס���һ��*/
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
		
		/*���������*/
		forwardBtn = (ImageButton)findViewById(R.id.forwardBtn);//���
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
		
		
		/*SeekBar������*/
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
		
		/*����������*/
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
		setup();//׼������
		play();//��ʼ����
		
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
		mp = new MediaPlayer();//������ý�����
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
					seekbar.setMax(mp.getDuration());//���ò��Ž��������ֵ
					handler.sendEmptyMessage(1);//��handler������Ϣ���������Ž�����
					playtime.setText(toTime(mp.getCurrentPosition()));//��ʼ������ʱ��
					durationTime.setText(toTime(mp.getDuration()));//���ø���ʱ��
					mp.seekTo(currentPosition);//��ʼ��MediaPlayer����λ��
					/*��������������*/
					mAudioManager = (AudioManager) PlayActivity.this.getSystemService(PlayActivity.this.AUDIO_SERVICE);
					int maxSound = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
					
					/*��õ�ǰ��������*/
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
	public void onCompletion(MediaPlayer mp) {//ѭ������
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
		//���ݿ����
		dbHelper = new DBHelper(this, "music.db", null, 2);
		Cursor c = dbHelper.query(pos);
		Date currentTime = new Date();   
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
		String dateString = formatter.format(currentTime); 
		if (c==null||c.getCount()==0){//�����ѯ���Ϊ��			
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
	
	Runnable forward = new Runnable() {//���
		
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
	
	Runnable rewind = new Runnable() {//����
		
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
    		lrcText.setText("����ļ�������...");
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
					if (data.charAt(3)==':'&&data.charAt(6)=='.'){//�Ӹ�����Ŀ�ʼ
						data = data.replace("[", "");
						data = data.replace("]", "@");
						data = data.replace(".", ":");
						String lrc[] = data.split("@");
						String lrcContent= null;
						if (lrc.length==2){
						lrcContent = lrc[lrc.length-1];//���
						}else{
						lrcContent = "";
						}
						String lrcTime[] = lrc[0].split(":");
						
						int m = Integer.parseInt(lrcTime[0]);//��
						int s = Integer.parseInt(lrcTime[1]);//��
						int ms = Integer.parseInt(lrcTime[2]);//����
						
						int begintime = (m*60 + s) * 1000 + ms;//ת���ɺ���
						LRCbean lrcbean = new LRCbean();
						lrcbean.setBeginTime(begintime);//���ø�ʿ�ʼʱ��
						lrcbean.setLrcBody(lrcContent);//���ø�ʵ�����
						lrc_read.put(begintime,lrcbean);
					}
				}
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//����ÿ������Ҫ��ʱ��
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
