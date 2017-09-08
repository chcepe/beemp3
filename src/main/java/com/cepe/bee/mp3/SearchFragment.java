package com.cepe.bee.mp3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.startapp.android.publish.StartAppAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mp3download.musicman.com.simplemp3download.R;

public class SearchFragment extends ListFragment {
    public MyAdapter adapter;
    public int fail = 0, ia = 0;
    public View inflate;

    private Context context;

    private StartAppAd startAppAd = new StartAppAd(this.getContext());
    private String cliendID = "dfd268143d94dad03a4242c54646e4a4";
    private String url, searchKeyword;
    private int perPage = 20;
    private int offset = 0;
    private String soundcloudMP3;

    private ListView list;
    private FrameLayout searchLoading;

    private EditText searchEdit;

    private Animation animFadeOut, animFadeIn, bottomDown, bottomUp;
    private boolean isSearch, isFinished, finishLoading = false;
    private int lvSize;

    DownloadsDBAdapter mDbHelper;
    int ida;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();

        /// INFLATER ///
        inflate = inflater.inflate(R.layout.search, container, false);

        /// ADAPTER ///
        adapter = new MyAdapter(getActivity(), 0);
        setListAdapter(adapter);

        /// ANIMATION ///
        bottomDown = AnimationUtils.loadAnimation(context, R.anim.bottom_down_fadeout);
        bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up_fadein);
        animFadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        animFadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

        /// ONBJECTS ///
        searchLoading = (FrameLayout) inflate.findViewById(R.id.searchLoading);
        searchEdit = (EditText) inflate.findViewById(R.id.searchEdit);

        searchEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (searchEdit.getText().toString().equals("")) {
                        toastmake("Nothing Inputed");
                    } else {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);

                        try {
                            searchKeyword = URLEncoder.encode(searchEdit.getText().toString(), "UTF-8").replace("+", "%20");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        isSearch = true;
                        offset = 0;
                        adapter.clearAdapter();
                        try {
                            soundcloud();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        return inflate;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && (getListView().getLastVisiblePosition() - getListView().getHeaderViewsCount() - getListView().getFooterViewsCount()) >= (adapter.getCount() - 1)) {
                    offset = offset + perPage;
                    if (finishLoading == true) {
                        if (isFinished == false) {
                            try {
                                soundcloud();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            isSearch = false;
                        } else {
                            Toast.makeText(context, "No more tracks found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        AdapterItem data = adapter.items.get(position);

        String title = data.title;
        String artwork_url = data.artwork_url;
        String trackID = data.trackID;
        String waveform_url = data.waveform_url;
        String duration = data.duration;
        int likes_count = data.likes_count;
        int playback_count = data.playback_count;
        int i = data.i;

        /// DBHelper ///
        mDbHelper = new DownloadsDBAdapter(context);
        mDbHelper.open();

        if(mDbHelper.fetchAllDownloads(url) > 0){
            Cursor cursor = mDbHelper.fetchAllDownloadsa(url);
            cursor.moveToNext();
            ida = cursor.getInt(cursor.getColumnIndex(DownloadsDBAdapter.KEY_ROWID));
            cursor.close();
        }
        String test = "https://api.soundcloud.com/tracks/"+trackID+"/stream?client_id=dfd268143d94dad03a4242c54646e4a4";
        if(mDbHelper.fetchAllDownloads(test) == 0){
            mDbHelper.createDownload(title, test, 1, 0);
            toastmake(getString(R.string.addedqueue));
        }else if(ida != 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        mDbHelper.close();
    }

    public void soundcloud() throws IOException, NullPointerException{
        showLoading();
        String link = "http://api.soundcloud.com/tracks.json?client_id="+cliendID+"&q="+searchKeyword+"&limit="+perPage+"&offset="+offset;
        toastmake(link);

        finishLoading = false;
        lvSize = adapter.getCount();
        JsonArrayRequest stringRequest = new JsonArrayRequest (link,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray  response) {
                        hideLoading();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);
                                String title = jo.getString("title");
                                String artwork_url = jo.getString("artwork_url");
                                String trackID = jo.getString("uri").substring(jo.getString("uri").lastIndexOf("/") + 1).trim();
                                String waveform_url = jo.getString("waveform_url");
                                String duration = String.format("%d min, %d sec",
                                        TimeUnit.MILLISECONDS.toMinutes(jo.getInt("duration")),
                                        TimeUnit.MILLISECONDS.toSeconds(jo.getInt("duration")) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(jo.getInt("duration")))
                                );
                                int likes_count = jo.getInt("likes_count");
                                int playback_count = jo.getInt("playback_count");
                                adapter.addAdapterItem(new AdapterItem(title, artwork_url, trackID, waveform_url, duration, likes_count, playback_count, i));
                                ia++;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if(adapter.getCount()==0 && isSearch){
                            Toast.makeText(context, "No tracks found.", Toast.LENGTH_SHORT).show();
                            searchEdit.setText("");
                        }
                        else if(!isSearch && lvSize==adapter.getCount()){
                            Toast.makeText(context, "No more tracks found.", Toast.LENGTH_SHORT).show();
                            isFinished = true;
                        }
                        else{
                            isFinished = false;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    public void showLoading(){
        if (searchLoading.getVisibility() != View.VISIBLE) {
            searchLoading.startAnimation(animFadeIn);
            searchLoading.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoading(){
        if (searchLoading.getVisibility() != View.INVISIBLE) {
            finishLoading = true;
            searchLoading.startAnimation(animFadeOut);
            searchLoading.setVisibility(View.INVISIBLE);
        }
    }

    public class MyAdapter extends ArrayAdapter<AdapterItem> {
        private List<AdapterItem> items = new ArrayList<AdapterItem>();

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
            View rowView;
            if(convertView == null) {
                rowView = getActivity().getLayoutInflater().inflate(R.layout.searchitemview, null);
            } else {
                rowView = convertView;
            }

            ImageView imThumb = (ImageView) rowView.findViewById(R.id.imThumb);
            TextView txtTitle = (TextView) rowView.findViewById(R.id.txtTitle);
            TextView txtDuration = (TextView) rowView.findViewById(R.id.txtDuration);
            TextView txtLikes = (TextView) rowView.findViewById(R.id.txtLikes);
            TextView txtPlaybacks = (TextView) rowView.findViewById(R.id.txtPlaybacks);

            Picasso.with(context).load(items.get(position).artwork_url).placeholder(R.drawable.music).error(R.drawable.music).into(imThumb);
            txtTitle.setText(items.get(position).title);
            txtDuration.setText(items.get(position).duration);
            txtLikes.setText(withSuffix(items.get(position).likes_count));
            txtPlaybacks.setText(withSuffix(items.get(position).playback_count));

            return rowView;
        }

        public void clearAdapter(){
            items.clear();
            notifyDataSetChanged();
        }
    }

    class AdapterItem {
        public String title, artwork_url, trackID, waveform_url, duration;
        public int likes_count, playback_count, i;

        public AdapterItem(String title, String artwork_url, String trackID, String waveform_url, String duration, int likes_count, int playback_count, int i) {
            this.title = title;
            this.artwork_url = artwork_url;
            this.trackID = trackID;
            this.waveform_url = waveform_url;
            this.duration = duration;
            this.likes_count = likes_count;
            this.playback_count = playback_count;
            this.i = i;
        }
    }

    public void toastmake(String title){
        Toast.makeText(getActivity(), title, Toast.LENGTH_SHORT).show();
    }
    public static String withSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp-1));
    }
}
