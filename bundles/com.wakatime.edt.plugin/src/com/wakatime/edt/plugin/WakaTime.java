package com.wakatime.edt.plugin;

import org.eclipse.core.runtime.ILog;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class WakaTime
    extends AbstractUIPlugin
    implements IStartup
{

    private static BundleContext context;

    // The plug-in ID
    public static final String PLUGIN_ID = "com.wakatime.edt.plugin";

    // The shared instance
    public static WakaTime plugin;
    public static ILog logInstance;
    public static String IDE_NAME;
    public static String ECLIPSE_VERSION;
    public static boolean DEBUG = false;
    public static Boolean READY = false;
    public static final Logger log = new Logger();

    static BundleContext getContext()
    {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception
    {
        WakaTime.context = bundleContext;
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception
    {
        WakaTime.context = null;
    }

    @Override
    public void earlyStartup()
    {
        // TODO Auto-generated method stub

    }

}
