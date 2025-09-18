
package com.atakmap.android.plugintemplate;

import android.content.Context;
import android.content.Intent;

import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

import com.atakmap.android.maps.MapActivity;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;

import com.atakmap.android.routes.RouteMapComponent;
import com.atakmap.app.preferences.ToolsPreferenceFragment;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.plugintemplate.plugin.R;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.Pane;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;

public class PluginTemplateMapComponent implements IPlugin {

    private static final String TAG = "PluginTemplateMapComponent";
    private Context pluginContext;
    private PluginTemplateDropDownReceiver ddr;
    private BingMapsRouter router;
    private RouteMapComponent rmc;

    public static PluginTemplateWidget widget;

    IServiceController serviceController;

    IHostUIService uiService;
    ToolbarItem toolbarItem;
    Pane templatePane;
    private MapView mapView;

    private PluginTemplateDropDownReceiver dropDownReceiver;


    public PluginTemplateMapComponent(IServiceController serviceController) {
        try {
            this.serviceController = serviceController;
            final PluginContextProvider ctxProvider = serviceController
                    .getService(PluginContextProvider.class);
            if (ctxProvider != null) {
                pluginContext = ctxProvider.getPluginContext();
                if (pluginContext != null) {
                    pluginContext.setTheme(R.style.ATAKPluginTheme);
                }
            }

            // obtain the UI service
            uiService = serviceController.getService(IHostUIService.class);

            // Get MapView - might be null during initialization
            mapView = MapView.getMapView();

            // initialize the toolbar button for the plugin
            if (pluginContext != null) {
                // create the button
                toolbarItem = new ToolbarItem.Builder(
                        pluginContext.getString(R.string.app_name),
                        MarshalManager.marshal(
                                pluginContext.getResources().getDrawable(R.drawable.ic_launcher_maps),
                                android.graphics.drawable.Drawable.class,
                                gov.tak.api.commons.graphics.Bitmap.class))
                        .setListener(new ToolbarItemAdapter() {
                            @Override
                            public void onClick(ToolbarItem item) {
                                // Send intent to show dropdown instead of pane
                                Intent intent = new Intent(PluginTemplateDropDownReceiver.SHOW_PLUGIN);
                                AtakBroadcast.getInstance().sendBroadcast(intent);
                            }
                        })
                        .build();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onStart() {

            // the plugin is starting, add the button to the toolbar
            if (uiService == null)
                return;

            uiService.addToolbarItem(toolbarItem);

            // Create dropdown receiver in onStart when mapView should be available
            if (dropDownReceiver == null && pluginContext != null) {
                mapView = MapView.getMapView(); // Try to get mapView again
                if (mapView != null) {
                    Log.i(TAG, "Creating dropdown receiver in onStart");
                    dropDownReceiver = new PluginTemplateDropDownReceiver(mapView, pluginContext);

                } else {
                    Log.w(TAG, "MapView still null in onStart, cannot create dropdown receiver");
                }
            }

        ToolsPreferenceFragment.register(
                new ToolsPreferenceFragment.ToolPreference(
                        pluginContext.getString(R.string.preferences_title),
                        pluginContext.getString(R.string.preferences_summary),
                        pluginContext.getString(R.string.key_bingmaps_preferences),
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher_maps),
                        new PluginPreferencesFragment(
                                pluginContext)));


        // Register the dropdown receiver
            if (dropDownReceiver != null) {
                Log.i(TAG, "Registering dropdown receiver");
                DocumentedIntentFilter filter = new DocumentedIntentFilter();
                filter.addAction(PluginTemplateDropDownReceiver.SHOW_PLUGIN,
                        "Show the PluginTemplate dropdown");
                AtakBroadcast.getInstance().registerReceiver(dropDownReceiver,
                        filter);
            } else {
                Log.w(TAG, "Cannot register dropdown receiver - it's null");
            }
        widget = new PluginTemplateWidget(mapView, ddr);

        router = new BingMapsRouter(pluginContext);
        rmc = (RouteMapComponent) ((MapActivity) mapView.getContext()).getMapComponent(RouteMapComponent.class);
        rmc.getRoutePlannerManager().registerPlanner("BingMapsPlugin", router);
    }

    @Override
    public void onStop() {
        ToolsPreferenceFragment.unregister(pluginContext.getString(R.string.key_bingmaps_preferences));

        // the plugin is stopping, remove the button from the toolbar
        if (uiService == null)
            return;

        uiService.removeToolbarItem(toolbarItem);

        // Unregister the dropdown receiver
        if (dropDownReceiver != null) {
            AtakBroadcast.getInstance().unregisterReceiver(dropDownReceiver);
            dropDownReceiver.dispose();
        }

    }
}
