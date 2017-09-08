package com.cepe.bee.mp3;

import android.support.annotation.NonNull;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class Utils {
    static String getString(URLConnection conn) throws IOException {
        return getString(conn.getInputStream());
    }

    @NonNull
    public static String getString(InputStream inputStream) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        while (true) {
            String line = rd.readLine();
            if (line == null) {
                String response = sb.toString();
                rd.close();
                return response;
            }
            sb.append(line).append('\n');
        }
    }

    static String getMediaUrlFromSongUrl(String source, String url) throws IOException {
        if (source.contains("4song")) {
            URLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            con.setRequestProperty("Referer", "http://www.4songs.pk/search?q=hey+mama");
            con.connect();
            String location = ((Element) Jsoup.parse(getString(con), "http://4songs.pk").select("audio").get(0)).attr("src");
            Log.d("utils", "location found for 4song " + location);
            return location;
        } else if (source.contains("hulkshare") || source.contains("zingd")) {
            HttpURLConnection con2 = (HttpURLConnection) new URL(url).openConnection();
            con2.setInstanceFollowRedirects(false);
            con2.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
            con2.connect();
            return con2.getHeaderField("Location");
        } else if (source.contains("mp3skull")) {
            return ((Element) Jsoup.parse(getDataFromUrl(url, Constants.TIMEOUT_MILLIS_LONG)).select("input[type=hidden]").get(0)).attr("value");
        } else {
            return url;
        }
    }

    @NonNull
    static HttpURLConnection getHttpURLConnection(String link, int ms) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(link).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
        conn.setDoOutput(true);
        conn.setConnectTimeout(ms);
        return conn;
    }

    static String getDataFromUrl(String link, int ms) throws IOException {
        return getString(getHttpURLConnection(link, ms));
    }
}
