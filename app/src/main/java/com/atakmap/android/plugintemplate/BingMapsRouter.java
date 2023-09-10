package com.atakmap.android.plugintemplate;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePlannerInterface;
import com.atakmap.android.routes.RoutePlannerOptionsView;
import com.atakmap.android.plugintemplate.plugin.R;

public class BingMapsRouter implements RoutePlannerInterface {

    public final static String TAG = "BingMapsRouter";
    public final Context context;

    public BingMapsRouter(Context context) {
        super();
        this.context = context;
    }

    @Override
    public String getDescriptiveName() {
        return "BingMaps Online Router";
    }

    @Override
    public boolean isNetworkRequired() {
        return true;
    }

    @Override
    public RouteGenerationTask getRouteGenerationTask(RouteGenerationTask.RouteGenerationEventListener routeGenerationEventListener) {
        return new BingMapsRoutingTask(routeGenerationEventListener, context);
    }

    @Override
    public RoutePlannerOptionsView getOptionsView(AlertDialog alertDialog) {
        RoutePlannerOptionsView routePlannerOptionsView;
        try {
            routePlannerOptionsView = (RoutePlannerOptionsView) LayoutInflater.from(alertDialog.getContext()).inflate(R.layout.bingmaps_planner_option, null);
        } catch (Exception e) {
            routePlannerOptionsView = new RoutePlannerOptionsView(alertDialog.getContext());
        }

        return routePlannerOptionsView;
    }

    @Override
    public RoutePlannerOptionsView getNavigationOptions(AlertDialog alertDialog) {
        return null;
    }

    @Override
    public boolean isRerouteCapable() {
        return false;
    }

    @Override
    public boolean canRouteAroundRegions() {
        return false;
    }
}
