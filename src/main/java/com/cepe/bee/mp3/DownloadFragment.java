package com.cepe.bee.mp3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.support.v4.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import mp3download.musicman.com.simplemp3download.R;

public class DownloadFragment extends ListFragment {
	private List<AdapterItem> items = new ArrayList<AdapterItem>();
	private DownloadsDBAdapter mDbHelper;

	public TextView text;
	public Button clearall;
	
	MyReceiver myReceiver;
	DownloadService mService;
    boolean mBound = false;
    public MyAdapter adapter;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View inflate = inflater.inflate(R.layout.download, container, false);

		mDbHelper = new DownloadsDBAdapter(getActivity());
        mDbHelper.open();

        this.text = (TextView)inflate.findViewById(R.id.totaldls);    
        this.clearall = (Button)inflate.findViewById(R.id.clearall);      
        
        //Total Downloads
        int totaldownloading = mDbHelper.fetchtotaler();
        if(totaldownloading >= 1){
        	text.setText(getString(R.string.totaldl)+": "+totaldownloading);
        }else{
        	text.setText(getString(R.string.totaldl)+": 0");
        }        
        
        //Clear All
        this.clearall.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
                mDbHelper.open();
        		mDbHelper.deleteDownloads();
        		
        		refreshlist();
        		
                int totaldownloading = mDbHelper.fetchtotaler();
                if(totaldownloading >= 1){
                	text.setText(getString(R.string.totaldl)+": "+totaldownloading);
                }else{
                	text.setText(getString(R.string.totaldl)+": 0");
                }
                
        		toastmake(getString(R.string.toast_clear));
        	}
        });
        refreshlist();
        
        return inflate;
    }
	
	@Override
	public void onStart() {
        super.onStart();
        
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.MY_ACTION);
        getActivity().registerReceiver(myReceiver, intentFilter);
        
        // Bind to LocalService
        Intent intent = new Intent(getActivity(), DownloadService.class);
        getActivity();
		getActivity().bindService(intent, mConnection, FragmentActivity.BIND_AUTO_CREATE);
    }
    
    @Override
	public void onStop() {
        super.onStop();
        mDbHelper.close(); 
        getActivity().unregisterReceiver(myReceiver);
        // Unbind from the service
        if (mBound) {
        	mService.bound=0;
        	getActivity().unbindService(mConnection);
            mBound = false;
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        	mService.bound=1;
        }
        
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mService = null;
        }
    };
    
	
	public class MyAdapter extends ArrayAdapter<AdapterItem> {       
		public MyAdapter(Context context, int textviewid) {
            super(context, textviewid);
            
		}
		
	    public void addAdapterItem(AdapterItem item) {
	        items.add(item);
	    }

	    @Override
		public int getCount() {
	      return items.size();
	    }

        @Override
        public AdapterItem getItem(int position) {
                return ((null != items) ? items.get(position) : null);
        }
        
	    @Override
		public long getItemId(int position) {
	            return position;
	    }

	    @Override
		public View getView(int position, View convertView, ViewGroup parent){
	    	View rowView = null;
	    	if(items.get(position).third == 100){
	    		rowView = getActivity().getLayoutInflater().inflate(R.layout.download_divider, null);
	    		TextView progress = (TextView)rowView.findViewById(R.id.txtTitle);
	    		progress.setText(items.get(position).second);
	    	}else{

	    		int a = items.get(position).third;
	    		
	    		if(a == 2){
		    		rowView = getActivity().getLayoutInflater().inflate(R.layout.download_list_progress, null);
		    		TextView summary = (TextView)rowView.findViewById(R.id.title);
		    		summary.setText(items.get(position).second);
	    		}else{
		    		rowView = getActivity().getLayoutInflater().inflate(R.layout.download_list, null);
		    		TextView summary = (TextView)rowView.findViewById(R.id.title);
		    		summary.setText(items.get(position).second);
		    		
		    		TextView progress = (TextView)rowView.findViewById(R.id.progress);
		    		if(a == 1){
		        		progress.setText("Pending Download");
		    		}else if(a == 0){
		    			progress.setText("Finished Downloading");
		    		}else if(a == -1){
		        		progress.setText("Failed Downloading");
		    		}
	    		}
	    		mDbHelper.updatelvid(items.get(position).first, position);
	    	}
	    	
	    	return rowView;
	    }
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, final long id) {https://www.facebook.com/people/Christian-Lou-Cepe/100001578010660
		super.onListItemClick(l, v, position, id);
		
		if(items.get(position).third != 100){
			final long ida = items.get(position).first;
			
			try{
			Cursor cursor = mDbHelper.fetchId(items.get(position).first);
			
			final String title = cursor.getString(cursor.getColumnIndex(DownloadsDBAdapter.KEY_TITLE));
			final double downloaded = items.get(position).third;
			final double dltotal = cursor.getDouble(cursor.getColumnIndex(DownloadsDBAdapter.KEY_SIZE));
			final String filepath = Environment.getExternalStorageDirectory()+"/music/"+getString(R.string.app_name)+"/"+title.replaceAll("[^a-zA-Z0-9\\s]", "")+"-["+getString(R.string.app_name)+"].mp3";
			final int sid = cursor.getInt(cursor.getColumnIndex(DownloadsDBAdapter.KEY_SID));
		
			cursor.close();
			
			if(downloaded == -1){
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Download Option")
				       .setCancelable(false)
				       .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                mDbHelper.deleteDownload(ida);
				                refreshlist();
				                toastmake(getString(R.string.toast_deleted));
				           }
				       })
				       .setNeutralButton("Retry", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   mDbHelper.updateDownloaded(ida, "1");
				        	   refreshlist();
				        	   toastmake(getString(R.string.toast_retry));
				           }
				       })
				       .setPositiveButton(getString(R.string.btn_cancel_name), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				
			}else if(downloaded == 0){
				final CharSequence[] items = {"Open MP3", "Set As Ringtone", "Redownload", "Delete", getString(R.string.btn_cancel_name)};
	
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Pick An Option");
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	if(items[item] == "Open MP3"){
				    	    try {
				    	        //launch intent
				    	        Intent i = new Intent(Intent.ACTION_VIEW);
				    	        Uri uri = Uri.fromFile(new File(filepath)); 
				    	        String url = uri.toString();
	
				    	        //grab mime
				    	        String newMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				    	                MimeTypeMap.getFileExtensionFromUrl(url));
	
				    	        i.setDataAndType(uri, newMimeType);
				    	        startActivity(i);
				    	    } catch (Exception e) {
				    	    	toastmake("Error Loading MP3");
				    	        e.printStackTrace();
				    	    }
				    	}else if(items[item] == "Set As Ringtone"){
				    		File k = new File(filepath); // path is a file to /sdcard/media/ringtone
	
				    		ContentValues values = new ContentValues();
				    		values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
				    		values.put(MediaStore.MediaColumns.TITLE, title);
				    		values.put(MediaStore.MediaColumns.SIZE, (dltotal*1024));
				    		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
				    		values.put(MediaStore.Audio.Media.ARTIST, getString(R.string.app_name));
				    		values.put(MediaStore.Audio.Media.DURATION, 230);
				    		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
				    		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
				    		values.put(MediaStore.Audio.Media.IS_ALARM, false);
				    		values.put(MediaStore.Audio.Media.IS_MUSIC, false);
	
				    		//Insert it into the database
				    		Uri uri = MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath());
				    		Uri newUri = getActivity().getContentResolver().insert(uri, values);
	
				    		RingtoneManager.setActualDefaultRingtoneUri(
				    		  getActivity(),
				    		  RingtoneManager.TYPE_RINGTONE,
				    		  newUri);
				    		
				    	}else if(items[item] == "Delete"){
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							builder.setMessage("Are you sure you want to delete: " + title + " ?")
							       .setCancelable(false)
							       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							        	    File file = new File(filepath);
							        	    file.delete();
							        	    mDbHelper.deleteDownload(ida);
							        	    refreshlist();
							                toastmake("MP3 Deleted");
							           }
							       })
							       .setNegativeButton("No", new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							        	   toastmake("MP3 Not Deleted");
							               dialog.cancel();
							           }
							       });
							AlertDialog alert = builder.create();
							alert.show();
				    	}else if(items[item] == "Redownload"){
				    		mDbHelper.updateDownloaded(ida, "1"); 
				    		refreshlist();
				    		toastmake("Set To Retry");
				    	}
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}else if(downloaded == 1){
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Download Option")
				       .setCancelable(false)
				       .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                mDbHelper.deleteDownload(ida);
				                refreshlist();
				                toastmake("MP3 Download Deleted");
				           }
				       })
				       
				       .setPositiveButton(getString(R.string.btn_cancel_name), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}else if(downloaded == 2){
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Download Option")
				       .setCancelable(false)
				       .setNegativeButton("Stop Download", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   if(sid == 1){
									 mService.dltask1.cancel(true);
								}else if(sid == 2){
									 mService.dltask2.cancel(true);
								}else if(sid == 3){
									 mService.dltask3.cancel(true);
								}
					   			
								mDbHelper.updateDownloaded(ida, "-1");
					   			
								refreshlist();
					    		
				                toastmake("MP3 Download Stopped");
				           }
				       })
				       .setPositiveButton(getString(R.string.btn_cancel_name), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}
			}catch(IllegalStateException e){
				mDbHelper = new DownloadsDBAdapter(getActivity());
		        mDbHelper.open();
			}
		}
	}
	   
    public void refreshlist(){
    	items.clear();
    	adapter = new MyAdapter(getActivity(), 0);
    	//Current Downloading
    	Cursor a = mDbHelper.getEachDownload(2);
    	if(a.getCount() != 0){
    		adapter.addAdapterItem(new AdapterItem(102109, "Currently Downloading", 100));
    		a.moveToFirst();
        	for (int i = 0; i < a.getCount(); i++){
        		a.moveToPosition(i);
        		int v = (int) a.getDouble(a.getColumnIndex(DownloadsDBAdapter.KEY_ROWID));
        		int x = (int) a.getDouble(a.getColumnIndex(DownloadsDBAdapter.KEY_DOWNLOADED));
        		String c = a.getString(a.getColumnIndex(DownloadsDBAdapter.KEY_TITLE));

        		adapter.addAdapterItem(new AdapterItem(v, c, x));
        	}
    	}
    	a.close();
    	
    	//Pending Download
    	Cursor a1 = mDbHelper.getEachDownload(1);
    	if(a1.getCount() != 0){
    		adapter.addAdapterItem(new AdapterItem(102110, "Pending Download", 100));
			
        	for (int i = 0; i < a1.getCount(); i++){
        		a1.moveToPosition(i);
        		int v = (int) a1.getDouble(a.getColumnIndex(DownloadsDBAdapter.KEY_ROWID));
        		int x = (int) a1.getDouble(a.getColumnIndex(DownloadsDBAdapter.KEY_DOWNLOADED));
        		String c = a1.getString(a.getColumnIndex(DownloadsDBAdapter.KEY_TITLE));

        		adapter.addAdapterItem(new AdapterItem(v, c, x));
        	}
    	}
    	a1.close();
    	
    	//Finished Download
    	Cursor a11 = mDbHelper.getEachDownload(0);
    	if(a11.getCount() != 0){
    		adapter.addAdapterItem(new AdapterItem(102111, "Finished Downloads", 100));
			
        	for (int i = 0; i < a11.getCount(); i++){
        		a11.moveToPosition(i);
        		int v = (int) a11.getDouble(a.getColumnIndex(DownloadsDBAdapter.KEY_ROWID));
        		int x = (int) a11.getDouble(a.getColumnIndex(DownloadsDBAdapter.KEY_DOWNLOADED));
        		String c = a11.getString(a.getColumnIndex(DownloadsDBAdapter.KEY_TITLE));

        		adapter.addAdapterItem(new AdapterItem(v, c, x));
        	}
    	}
    	a11.close();
    	
    	//Failed Download
    	Cursor a111 = mDbHelper.getEachDownload(-1);
    	if(a111.getCount() != 0){
    		adapter.addAdapterItem(new AdapterItem(102112, "Failed Download", 100));
			
        	for (int i = 0; i < a111.getCount(); i++){
        		a111.moveToPosition(i);
        		int v = (int) a111.getDouble(a.getColumnIndex(DownloadsDBAdapter.KEY_ROWID));
        		int x = (int) a111.getDouble(a.getColumnIndex(DownloadsDBAdapter.KEY_DOWNLOADED));
        		String c = a111.getString(a.getColumnIndex(DownloadsDBAdapter.KEY_TITLE));

        		adapter.addAdapterItem(new AdapterItem(v, c, x));
        	}
    	}
    	a111.close();

		setListAdapter(adapter);
    }
	public void updateprogress(int id, int pos){
		try{
			ListView listview = getListView();   
			View view = listview.getChildAt(id);
			
			ProgressBar progress = (ProgressBar)view.findViewById(R.id.progressBar1);
			progress.setProgress(pos);
			adapter.notify();
			adapter.notifyDataSetChanged();
		}catch(NullPointerException e){
			
		}
	}
	
    private class MyReceiver extends BroadcastReceiver {
    	 @Override
    	 public void onReceive(Context arg0, Intent arg1) {
    		 try{
	    		 if(mService.http != 0){
	    			 if(arg1.hasExtra("yes")){
	    				 refreshlist();
	    			 }else{
		    			//refreshlist();
					    int id = Integer.parseInt(arg1.getStringExtra("id"));
						Cursor cursor = mDbHelper.fetchId(id);
						
						final int lvid = cursor.getInt(cursor.getColumnIndex(DownloadsDBAdapter.KEY_LISTVIEW));
						
					    int pos = Integer.parseInt(arg1.getStringExtra("pos"));
					    updateprogress(lvid, pos);
	    			 }
	    		 }else{
	    			 try{
	    				mDbHelper.open();
	    				refreshlist();
	    			 }catch(RuntimeException e){
	    				 
	    			 }catch(Exception e){
	    				 
	    			 }
		    	 }
    		 }catch(NullPointerException e){
    			 
    		 }catch(Exception e){
    			 
    		 }
    	 }
    }
    
	public void toastmake(String title){
		Toast.makeText(getActivity(), title, Toast.LENGTH_SHORT).show();
	}	
	
	class AdapterItem {
		public int first;
		public String second;
		public int third;

		public AdapterItem(int first, String second, int third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}
	}
}
