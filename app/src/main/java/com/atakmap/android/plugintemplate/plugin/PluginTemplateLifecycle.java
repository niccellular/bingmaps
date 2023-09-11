
package com.atakmap.android.plugintemplate.plugin;

import com.atak.plugins.impl.AbstractPlugin;
import com.atak.plugins.impl.PluginContextProvider;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import com.atakmap.android.plugintemplate.PluginTemplateMapComponent;
import com.atakmap.coremap.log.Log;

public class PluginTemplateLifecycle extends AbstractPlugin implements IPlugin {

    private final static String TAG = "PluginTemplateLifecycle";

    public PluginTemplateLifecycle(IServiceController serviceController) {
        super(serviceController, new PluginTemplateTool(serviceController.getService(PluginContextProvider.class).getPluginContext()), new PluginTemplateMapComponent());
        Log.d(TAG, "PluginTemplateLifcycle");
    }
}
