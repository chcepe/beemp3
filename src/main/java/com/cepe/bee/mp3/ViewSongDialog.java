package com.cepe.bee.mp3;

import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import mp3download.musicman.com.simplemp3download.R;

public class ViewSongDialog extends Activity implements OnTouchListener, OnCompletionListener, OnBufferingUpdateListener {
	DownloadsDBAdapter mDbHelper;
	
	public Button  download, share;
	public TextView tv;
	public ImageButton play;
	public SeekBar sb;
	
	int ida;
	public String title, url, source;
	
	private MediaPlayer mediaPlayer;
	private int mediaFileLengthInMilliseconds; 

	private Handler handler = new Handler();
	public Runnable notification;
	public ProgressTask task;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);         
        getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
  
        setContentView(R.layout.viewsong);
        
        //Prepare DBHelper
        mDbHelper = new DownloadsDBAdapter(this);
        mDbHelper.open();
        
        //Get Extras
        title = getIntent().getStringExtra("title");
        url = getIntent().getStringExtra("url");
        source = getIntent().getStringExtra("source");
        
        tv = (TextView)this.findViewById(R.id.songtitle);
        tv.setText(title);
        
        //GET UI ELEMENTS
        share = (Button)this.findViewById(R.id.share);
        play = (ImageButton)this.findViewById(R.id.button_play);
        download = (Button)this.findViewById(R.id.download);
		sb = (SeekBar)findViewById(R.id.progress_bar);

		if(mDbHelper.fetchAllDownloads(url) > 0){
        	Cursor cursor = mDbHelper.fetchAllDownloadsa(url);
        	cursor.moveToNext();
			ida = cursor.getInt(cursor.getColumnIndex(DownloadsDBAdapter.KEY_ROWID));
    		cursor.close();
    		download.setText(getString(R.string.btn_downloadexist));
        }
		
        //Buttons
		share.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {		
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, R.string.checkout);
				i.putExtra(Intent.EXTRA_TEXT, getString(R.string.dlmessage)+ title + " : " +url);
				startActivity(Intent.createChooser(i, getString(R.string.btn_share)));
			}
		});
		
        download.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		download.setEnabled(false);
        		        		
        	    if(mDbHelper.fetchAllDownloads(url) == 0){
					mDbHelper.createDownload(title, url, 1, 0);
					toastmake(getString(R.string.addedqueue));
        		}else if(ida != 0){
					Builder builder = new Builder(ViewSongDialog.this);
					builder.setMessage("Would You Like To Re-Download: "+title);
					builder.setCancelable(false);
					builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
 					        	   mDbHelper.updateDownloaded(ida, "1");
									toastmake("Download Readded To The Queue");
					           }
					       });
					builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();
					           }
					       });
					AlertDialog alert = builder.create();
					alert.show();
        		}
        	}
        });
        
		play.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if(task == null){
					play.setClickable(false);
					toastmake("Song Loading");
					task=new ProgressTask();
					task.execute(); 
				}else{
					if(!mediaPlayer.isPlaying()){
						mediaPlayer.start();
						handler.removeCallbacks(notification);
						primarySeekBarProgressUpdater();
						play.setImageResource(R.drawable.ic_media_pause);
					}else {
						mediaPlayer.pause();
						play.setImageResource(R.drawable.ic_media_play);
					}
				}
        }});
        
        sb.setMax(99);
        sb.setOnTouchListener(this);

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnCompletionListener(this);
		
        //Ads Test
		DisplayMetrics dm = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(dm); 
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    // If we've received a touch notification that the user has touched
	    // outside the app, finish the activity.
	    if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
			/*handler.removeCallbacks(notification);
			try{
				if(mediaPlayer.isPlaying()){
					mediaPlayer.stop();
				}
			}catch(IllegalStateException e){
				
			}
			mediaPlayer.reset();
			
			mediaPlayer.release();
			
			mDbHelper.close();
			
	    	Intent i = new Intent(getBaseContext(), MainActivity.class);
	    	i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); 
	    	ViewSongDialog.this.startActivity(i);
	    	finish();*/
	    	return true;
	    }

	    // Delegate everything else to Activity.
	    return super.onTouchEvent(event);
	}
	public void toastmake(String title){
		Toast.makeText(this, title, Toast.LENGTH_LONG).show();
	}
	
	class ProgressTask extends AsyncTask<Integer, Integer, Void>{
		  @Override
		  protected Void doInBackground(Integer... params) {
				try {
					String location;
					if(source.contains("hulkshare")){
						HttpURLConnection con = (HttpURLConnection)(new URL( url ).openConnection());
						con.setInstanceFollowRedirects( false );
						con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
						con.connect();
						location = con.getHeaderField( "Location" );
					}else{
						location = url;
					}
					mediaPlayer.setDataSource(location); 
					mediaPlayer.prepare(); 
					publishProgress(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
		  }
		  
		  protected void onProgressUpdate(Integer integers) {
			  super.onProgressUpdate();
			  if(integers == 1) {
			    Toast.makeText(ViewSongDialog.this, getString(R.string.buffering), Toast.LENGTH_SHORT).show(); 
			  }
		  }		
		  
		  @Override
		  protected void onPostExecute(Void result) {
			  mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL
			
				if(!mediaPlayer.isPlaying()){
					mediaPlayer.start();
					play.setClickable(true);
					play.setImageResource(R.drawable.ic_media_pause);
				}else {
					toastmake(getString(R.string.failedloading));
					mediaPlayer.pause();
					play.setClickable(false);
					play.setImageResource(R.drawable.ic_media_play);
				}

				handler.removeCallbacks(notification);
				primarySeekBarProgressUpdater();
				
		  }
	}
    
	@Override
	public void onBackPressed() {
		handler.removeCallbacks(notification);
		if(mediaPlayer.isPlaying()){
			mediaPlayer.stop();
		}
		mediaPlayer.reset();
		
		mediaPlayer.release();
		
		mDbHelper.close();
		
    	Intent i = new Intent(getBaseContext(), MainActivity.class);
    	i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); 
    	ViewSongDialog.this.startActivity(i);
    	finish();
	    return;
	}

    private void primarySeekBarProgressUpdater() {
    	sb.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds)*100)); // This math construction give a percentage of "was playing"/"song length"
		if (mediaPlayer.isPlaying()) {
			notification = new Runnable() {
		        public void run() {
		        	primarySeekBarProgressUpdater();
				}
		    };
		    handler.postDelayed(notification, 500);
    	}
		
    }
    
	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId() == R.id.progress_bar){
			if(mediaPlayer.isPlaying()){
		    	SeekBar sb = (SeekBar)v;
				int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
				mediaPlayer.seekTo(playPositionInMillisecconds);
			}
		}
		return false;
	}

	public void onCompletion(MediaPlayer mp) {
		play.setImageResource(R.drawable.ic_media_play);
		
		mediaPlayer.stop();
	}

	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		sb.setSecondaryProgress(percent);
	}
}