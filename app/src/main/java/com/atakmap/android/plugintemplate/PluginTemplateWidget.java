package com.atakmap.android.plugintemplate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.MotionEvent;

import com.atakmap.android.dropdown.DropDownManager;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;

import com.atakmap.android.widgets.LinearLayoutWidget;
import com.atakmap.android.widgets.MapWidget;
import com.atakmap.android.widgets.RootLayoutWidget;
import com.atakmap.coremap.maps.assets.Icon;
import com.atakmap.android.widgets.MarkerIconWidget;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.plugintemplate.plugin.R;

public class PluginTemplateWidget extends MarkerIconWidget
        implements MapWidget.OnClickListener {
    private final static int ICON_WIDTH = 32;
    private final static int ICON_HEIGHT = 32;

    private PluginTemplateDropDownReceiver ddr;
    public static final String TAG = "BingMapsWidget";

    public PluginTemplateWidget(MapView mapView, PluginTemplateDropDownReceiver ddr) {
        this.ddr = ddr;
        setName("BingMaps Direction");
        RootLayoutWidget root = (RootLayoutWidget) mapView.getComponentExtra("rootLayoutWidget");
        LinearLayoutWidget brLayout = root.getLayout(RootLayoutWidget.BOTTOM_RIGHT);
        brLayout.addWidget(this);

        updateIcon(0);
    }

    public void updateIcon(int dir) {
        int drawableId = 0;

        switch (dir) {
            default:
            case 0:
                drawableId = R.drawable.north32;
                break;
            case 90:
                drawableId = R.drawable.east32;
                break;
            case 180:
                drawableId = R.drawable.south32;
                break;
            case 270:
                drawableId = R.drawable.west32;
                break;
        }

        setIcon(drawableId);
    }

    private void setIcon(int drawableId) {

        String imageUri = "android.resource://com.atakmap.android.plugintemplate.plugin/" + drawableId;

        Log.d(TAG, "imageURi " + imageUri);
        Icon.Builder builder = new Icon.Builder();
        builder.setAnchor(0, 0);
        builder.setColor(Icon.STATE_DEFAULT, Color.WHITE);
        builder.setSize(ICON_WIDTH, ICON_HEIGHT);
        builder.setImageUri(Icon.STATE_DEFAULT, imageUri);

        Icon icon = builder.build();
        setIcon(icon);
    }

    @Override
    public void onMapWidgetClick(MapWidget mapWidget, MotionEvent motionEvent) {
        if (mapWidget == this) {
            if (!ddr.isDropDownOpen()) {
                Intent intent = new Intent();
                intent.setAction(ddr.SHOW_PLUGIN);
                AtakBroadcast.getInstance().sendBroadcast(intent);
            } else {
                DropDownManager.getInstance().unHidePane();
            }
        }
    }
}
