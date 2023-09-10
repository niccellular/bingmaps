
package com.atakmap.android.plugintemplate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.atakmap.android.maps.MapActivity;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.Marker;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.plugintemplate.plugin.R;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;

import com.atakmap.android.routes.RouteMapComponent;
import com.atakmap.android.rubbersheet.data.RubberImageData;
import com.atakmap.android.rubbersheet.data.create.CreateRubberImageTask;
import com.atakmap.android.rubbersheet.maps.RubberImage;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Locale;

public class PluginTemplateDropDownReceiver extends DropDownReceiver implements
        OnStateListener {

    public static final String TAG = "BingMaps";

    public static final String SHOW_PLUGIN = "com.atakmap.android.plugintemplate.SHOW_PLUGIN";
    private final Context pluginContext;
    private final Context appContext;
    private final MapView mapView;
    private final WebView htmlViewer;
    private final LinearLayout ll_map;
    private int currentDir;

    private boolean mIsDropDownOpen;
    /**************************** CONSTRUCTOR *****************************/

    public PluginTemplateDropDownReceiver(final MapView mapView,
            final Context context) {
        super(mapView);
        this.pluginContext = context;
        this.appContext = mapView.getContext();
        this.mapView = mapView;

        currentDir = 0;

        LayoutInflater inflater = LayoutInflater.from(pluginContext);
        ll_map = (LinearLayout) inflater.inflate(R.layout.main_layout, null);

        // must be created using the application context otherwise this will fail
        this.htmlViewer = new WebView(mapView.getContext());
        this.htmlViewer.setVerticalScrollBarEnabled(true);
        this.htmlViewer.setHorizontalScrollBarEnabled(true);

        WebSettings webSettings = this.htmlViewer.getSettings();

        // do not enable per security guidelines
        //webSettings.setAllowFileAccessFromFileURLs(true);
        //webSettings.setAllowUniversalAccessFromFileURLs(true);

        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        this.htmlViewer.setWebChromeClient(new ChromeClient());

        // cause subsequent calls to loadData not to fail - without this
        // the web view would remain inconsistent on subsequent concurrent opens
        this.htmlViewer.loadUrl("about:blank");
        this.htmlViewer.setWebViewClient(new Client());

        this.htmlViewer.setOnTouchListener(new View.OnTouchListener() {
            private final static long MAX_TOUCH_DURATION = 100;
            private long DownTime;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        DownTime = event.getEventTime();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(event.getEventTime() - DownTime <= MAX_TOUCH_DURATION) {
                            loadBingMaps(context, currentDir);

                            PluginTemplateMapComponent.widget.updateIcon(currentDir);
                            currentDir = (currentDir + 90) % 360;
                        }
                        break;
                    default:
                        break;

                }
                return false;
            }
        });

        htmlViewer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        WebView.enableSlowWholeDocumentDraw();
        ll_map.addView(htmlViewer);
    }

    public static class Client extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "started retrieving: " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverride: " + url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "ended retrieving: " + url);
            super.onPageFinished(view, url);
/*
            Picture picture = view.capturePicture();
            if (picture.getWidth() > 0) {
                Bitmap b = Bitmap.createBitmap(
                        picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                picture.draw(c);
                try {
                    File imgFile = File.createTempFile("birdEye-", null);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.PNG, 0 , bos);
                    byte[] bitmapdata = bos.toByteArray();

                    //write the bytes in file
                    FileOutputStream fos = new FileOutputStream(imgFile);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();

                    //A bounding box contains SouthLatitude, WestLongitude, NorthLatitude, and EastLongitude
                    //"bbox":[28.079514319337,-80.6506253778935,28.0812879965778,-80.6486150622368]
                    double[] bbox = {28.079514319337, -80.6506253778935, 28.0812879965778, -80.6486150622368};
                    double[] lats = new double[2];
                    lats[0] = bbox[0];
                    lats[1] = bbox[2];
                    double[] lngs = new double[2];
                    lngs[0] = bbox[1];
                    lngs[1] = bbox[3];

                    RubberImageData rid = new RubberImageData(imgFile);
                    rid.points = new GeoPoint[]{
                            new GeoPoint(lats[1], lngs[0]), // NW Corner
                            new GeoPoint(lats[1], lngs[1]), // NE Corner
                            new GeoPoint(lats[0], lngs[1]), // SE Corner
                            new GeoPoint(lats[0], lngs[0]), // SW Corner
                    };

                    RubberImage ri = RubberImage.create(MapView.getMapView(), rid);
                    ri.setImage(imgFile);

                    MapView.getMapView().getRootGroup().addItem(ri);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
*/
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d(TAG, "SSL Error");
            handler.proceed(); // Ignore SSL certificate errors
        }
    }

    private static class ChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d(TAG, consoleMessage.message() + " -- From line "
                    + consoleMessage.lineNumber() + " of "
                    + consoleMessage.sourceId());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d(TAG, "loading progress: " + newProgress);
        }
    }

    public void loadBingMaps(Context context, int dir) {

        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(appContext.getApplicationContext());

        String key = sharedPreference.getString("plugin_bingmaps_key_set_apikey", "");
        String mapSize = sharedPreference.getString("plugin_bingmaps_map_size", "2000,16500");
        String radius = sharedPreference.getString("plugin_bingmaps_pli_radius", "1000");
        String zoom = sharedPreference.getString("plugin_bingmaps_zoom_level", "20");
        if (Integer.parseInt(zoom) < 18) zoom = "18";
        if (Integer.parseInt(zoom) > 22) zoom = "22";
        boolean useLabels = sharedPreference.getBoolean("plugin_bingmaps_use_labels", true);
        boolean routeSelf = sharedPreference.getBoolean("plugin_bingmaps_route_self", false);
        boolean routeTwoPoints = sharedPreference.getBoolean("plugin_bingmaps_route_two_points", false);

        // mutually exclusive
        if (routeSelf) routeTwoPoints = false;
        if (routeTwoPoints) routeSelf = false;

        if (key.isEmpty()) {
            Toast toast = Toast.makeText(context, "No API Key set", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        StringBuilder pp = new StringBuilder();
        Marker finishPLI = null;
        Marker startPLI = null;
        if (mapView.getSelfMarker().getGroup() != null) {
            boolean selfMarker;
            Collection<MapItem> nearby = mapView.getSelfMarker().getGroup().deepFindItems(mapView.getCenterPoint().get(), Double.parseDouble(radius));
            for (MapItem mi : nearby) {
                selfMarker = false;
                if (mi instanceof Marker) {
                    int iconStyle = 0;
                    if (((Marker) mi).getType().equals("self")) {
                        iconStyle = 118;
                        selfMarker = true;
                    } else if (((Marker) mi).getType().startsWith("a-u-G")) {
                        // unknown (yellow)
                        iconStyle = 63;
                    } else if (((Marker) mi).getType().startsWith("a-n-G")) {
                        // neutral (green)
                        iconStyle = 26;
                    } else if (((Marker) mi).getType().startsWith("a-f-G")) {
                        // friendly (blue)
                        iconStyle = 20;
                    } else if (((Marker) mi).getType().startsWith("a-h-G")) {
                        // hostile (red)
                        iconStyle = 28;
                    }

                    String label = ((Marker) mi).getTitle();
                    Log.d(TAG, "PLI Label: " + label);
                    if (label.equalsIgnoreCase("finish")) {
                        Log.d(TAG, "Found a Finish PLI");
                        finishPLI = ((Marker) mi);
                    } else if (label.equalsIgnoreCase("start")) {
                        Log.d(TAG, "Found a Start PLI");
                        startPLI = ((Marker) mi);
                    }
                    pp.append(String.format(java.util.Locale.US, "pp=%f,%f;%d;%s&", ((Marker) mi).getPoint().getLatitude(), ((Marker) mi).getPoint().getLongitude(), iconStyle, selfMarker ? mapView.getDeviceCallsign() : label));
                }
            }
        } else {
            pp.append("pp=0,0;;&");
        }

        double lng = mapView.getCenterPoint().get().getLongitude();
        double lat = mapView.getCenterPoint().get().getLatitude();
        String url = String.format(java.util.Locale.US, "https://dev.virtualearth.net/REST/V1/Imagery/Map/%s/%f,%f/%s?dir=%d&ms=%s&%skey=%s", useLabels ? "BirdseyeWithLabels" : "Birdseye", lat, lng, zoom, dir, mapSize, pp.toString(), key);

        if (routeSelf) {
            if (finishPLI != null) {
                url = String.format(java.util.Locale.US, "https://dev.virtualearth.net/REST/V1/Imagery/Map/%s/%f,%f/%s/Routes?wp.1=%f,%f;1;Start&wp.2=%f,%f;7;Finish&travelMode=walking&optmz=distance&dir=%d&ms=%s&%skey=%s", useLabels ? "BirdseyeWithLabels" : "Birdseye", lat, lng, zoom, mapView.getSelfMarker().getPoint().getLatitude(), mapView.getSelfMarker().getPoint().getLongitude(), finishPLI.getPoint().getLatitude(), finishPLI.getPoint().getLongitude(), dir, mapSize, pp.toString(), key);
            } else {
                Toast toast = Toast.makeText(context, "Route Self enabled but no 'Finish' PLI", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }

        if (routeTwoPoints) {
            if (finishPLI != null && startPLI != null) {
                url = String.format(java.util.Locale.US, "https://dev.virtualearth.net/REST/V1/Imagery/Map/%s/%f,%f/%s/Routes?wp.1=%f,%f;1;Start&wp.2=%f,%f;7;Finish&travelMode=walking&optmz=distance&dir=%d&ms=%s&%skey=%s", useLabels ? "BirdseyeWithLabels" : "Birdseye", lat, lng, zoom, startPLI.getPoint().getLatitude(), startPLI.getPoint().getLongitude(), finishPLI.getPoint().getLatitude(), finishPLI.getPoint().getLongitude(), dir, mapSize, pp.toString(), key);
            } else {
                Toast toast = Toast.makeText(context, "Route Two Points enabled but no 'Start' and/or 'Finish' PLI", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }
        Log.i(TAG, url);
        this.htmlViewer.loadUrl(url);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals(SHOW_PLUGIN)) {
            showDropDown(ll_map, HALF_WIDTH, FULL_HEIGHT,
                    FULL_WIDTH, HALF_HEIGHT, false, this);

            this.htmlViewer.loadUrl("about:blank");
            loadBingMaps(context, currentDir);
        }
    }

    @Override
    public void disposeImpl() {
    }

    @Override
    public void onDropDownSelectionRemoved() {
    }

    @Override
    public void onDropDownVisible(boolean v) {
        mIsDropDownOpen = true;
    }

    @Override
    public void onDropDownSizeChanged(double width, double height) {
    }

    @Override
    public void onDropDownClose() {
        mIsDropDownOpen = false;
    }

    public boolean isDropDownOpen() {
        return mIsDropDownOpen;
    }
}
