
package com.atakmap.android.plugintemplate;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

import com.atakmap.android.maps.MapActivity;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;

import com.atakmap.android.routes.RouteMapComponent;
import com.atakmap.app.preferences.ToolsPreferenceFragment;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.plugintemplate.plugin.R;

public class PluginTemplateMapComponent extends DropDownMapComponent {

    private static final String TAG = "PluginTemplateMapComponent";
    private Context pluginContext;
    private PluginTemplateDropDownReceiver ddr;
    private BingMapsRouter router;
    private RouteMapComponent rmc;

    public static PluginTemplateWidget widget;

    public void onCreate(final Context context, Intent intent,
            final MapView view) {

        context.setTheme(R.style.ATAKPluginTheme);
        super.onCreate(context, intent, view);
        pluginContext = context;

        ddr = new PluginTemplateDropDownReceiver(
                view, context);

        Log.d(TAG, "registering the plugin filter");
        DocumentedIntentFilter ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(PluginTemplateDropDownReceiver.SHOW_PLUGIN);
        registerDropDownReceiver(ddr, ddFilter);

        ToolsPreferenceFragment.register(
                new ToolsPreferenceFragment.ToolPreference(
                        pluginContext.getString(R.string.preferences_title),
                        pluginContext.getString(R.string.preferences_summary),
                        pluginContext.getString(R.string.key_bingmaps_preferences),
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher_maps),
                        new PluginPreferencesFragment(
                                pluginContext)));

        widget = new PluginTemplateWidget(view, ddr);

        router = new BingMapsRouter(context);
        rmc = (RouteMapComponent) ((MapActivity) view.getContext()).getMapComponent(RouteMapComponent.class);
        rmc.getRoutePlannerManager().registerPlanner("BingMapsPlugin", router);
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
        ToolsPreferenceFragment.unregister(pluginContext.getString(R.string.key_bingmaps_preferences));
    }

}
