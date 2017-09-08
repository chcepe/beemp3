package com.cepe.bee.mp3;

import android.support.v4.app.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import mp3download.musicman.com.simplemp3download.R;

public class AppWallFragment extends Fragment {
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View inflate = inflater.inflate(R.layout.app_wall, container, false);
		WebView webView = (WebView)inflate.findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		final ProgressDialog mProgress = ProgressDialog.show(getActivity(), "Loading", "Please wait for a moment...");
		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			public void onPageFinished(WebView view, String url) {
				if(mProgress.isShowing()) {
					mProgress.dismiss();
				}
			}
		});
		webView.loadUrl(getString(R.string.leadbolt_url));
		return inflate;
	}
}
