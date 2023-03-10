package com.wakatime.edt.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class WakaTime
    extends AbstractUIPlugin
    implements IStartup
{
    // The plug-in ID
    public static final String PLUGIN_ID = "com.wakatime.edt.plugin";

    // The shared instance
    public static WakaTime plugin;
    public static ILog logInstance;
    public static String IDE_NAME;
    public static String ECLIPSE_VERSION;
    public static boolean DEBUG = true;
    public static Boolean READY = false;
    public static final Logger log = new Logger();

    // Listeners
    private static CustomEditorListener editorListener;
    private static IExecutionListener executionListener;
    private WindowsEventService windowsEventService;

    // Constants
    public static final long FREQUENCY = 2; // frequency of heartbeats in minutes
    public static final String VERSION = Platform.getBundle(PLUGIN_ID).getVersion().toString();

    public String lastFile;
    public long lastTime = 0;

    /**
     * The constructor
     */
    public WakaTime()
    {
    }

    @Override
    public void start(BundleContext context) throws Exception
    {
        logInstance = getLog();

        super.start(context);
        plugin = this;

        // discover app name and version
        try
        {
            //ECLIPSE_VERSION = Platform.getBundle("com._1c.g5.v8.dt.product.application").getVersion().toString();
            ECLIPSE_VERSION = System.getProperty("product.version");
            IDE_NAME = "edt";
        }
        catch (Exception e)
        {
            try
            {
                ECLIPSE_VERSION = Platform.getBundle("org.eclipse.platform").getVersion().toString();
                IDE_NAME = "eclipse";
            }
            catch (Exception e2)
            {
                try
                {
                    ECLIPSE_VERSION = Platform.getProduct().getDefiningBundle().getVersion().toString();
                    IDE_NAME = Platform.getProduct().getName();
                }
                catch (Exception e3)
                {
                    ECLIPSE_VERSION = "unknown";
                    IDE_NAME = "edt";
                }
            }
        }
        Logger.debug("Detected " + IDE_NAME + " version: " + ECLIPSE_VERSION);

        windowsEventService = new WindowsEventService();
        windowsEventService.start();
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        plugin = null;
        super.stop(context);

        windowsEventService.stop();
    }

    @Override
    public void earlyStartup()
    {
        final IWorkbench workbench = PlatformUI.getWorkbench();

        // listen for save file events
        ICommandService commandService = workbench.getService(ICommandService.class);
        executionListener = new CustomExecutionListener();
        commandService.addExecutionListener(executionListener);

        workbench.getDisplay().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window == null)
                    return;

                String debug = ConfigFile.get("settings", "debug", false);
                DEBUG = debug != null && debug.trim().equals("true");
                Logger.debug("Initializing WakaTime plugin (https://wakatime.com) v" + VERSION);

                // prompt for apiKey if not set
                String apiKey = ConfigFile.get("settings", "api_key", false);
                if (apiKey == "")
                {
                    promptForApiKey(window);
                }

                checkCLI();

                if (window.getPartService() == null)
                    return;

                // listen for caret movement
//                if (window.getPartService().getActivePartReference() != null
//                    && window.getPartService().getActivePartReference().getPage() != null
//                    && window.getPartService().getActivePartReference().getPage().getActiveEditor() != null)
//                {
//                    IEditorPart part = window.getPartService().getActivePartReference().getPage().getActiveEditor();
//                    if (!(part instanceof AbstractTextEditor))
//                        return;
//                    ((StyledText)part.getAdapter(Control.class)).addCaretListener(new CustomCaretListener());
//                }

                // listen for change of active file
//                if (window.getPartService().getActivePart() == null)
//                    return;
//                if (window.getPartService().getActivePart().getSite() == null)
//                    return;
//                if (window.getPartService().getActivePart().getSite().getPage() == null)
//                    return;
//                if (window.getPartService().getActivePart().getSite().getPage().getActiveEditor() == null)
//                    return;
//                if (window.getPartService()
//                    .getActivePart()
//                    .getSite()
//                    .getPage()
//                    .getActiveEditor()
//                    .getEditorInput() == null)
//                    return;
//
//                // log file if one is opened by default
//                IEditorInput input =
//                    window.getPartService().getActivePart().getSite().getPage().getActiveEditor().getEditorInput();
//                if (input instanceof IURIEditorInput)
//                {
//                    URI uri = ((IURIEditorInput)input).getURI();
//                    if (uri != null && uri.getPath() != null)
//                    {
//                        String currentFile = uri.getPath();
//                        long currentTime = System.currentTimeMillis() / 1000;
//                        if (!currentFile.equals(WakaTime.getDefault().lastFile)
//                            || WakaTime.getDefault().lastTime + WakaTime.FREQUENCY * 60 < currentTime)
//                        {
//                            WakaTime.sendHeartbeat(currentFile, WakaTime.getActiveProject(), false);
//                            WakaTime.getDefault().lastFile = currentFile;
//                            WakaTime.getDefault().lastTime = currentTime;
//                        }
//                    }
//                }

                Logger.debug("Finished initializing WakaTime plugin (https://wakatime.com) v" + VERSION);
            }
        });
    }

    private void checkCLI()
    {
        if (!Dependencies.isCLIInstalled())
        {
            Logger.info("Downloading and installing wakatime-cli...");
            Dependencies.installCLI();
            WakaTime.READY = true;
            Logger.info("Finished downloading and installing wakatime-cli.");
        }
        else if (Dependencies.isCLIOld())
        {
            if (System.getenv("WAKATIME_CLI_LOCATION") != null
                && !System.getenv("WAKATIME_CLI_LOCATION").trim().isEmpty())
            {
                File wakatimeCLI = new File(System.getenv("WAKATIME_CLI_LOCATION"));
                if (wakatimeCLI.exists())
                {
                    Logger.error("$WAKATIME_CLI_LOCATION is out of date, please update it.");
                }
            }
            else
            {
                Logger.info("Upgrading wakatime-cli ...");
                Dependencies.installCLI();
                WakaTime.READY = true;
                Logger.info("Finished upgrading wakatime-cli.");
            }
        }
        else
        {
            WakaTime.READY = true;
            Logger.info("wakatime-cli is up to date.");
        }
        Dependencies.createSymlink(Dependencies.combinePaths(Dependencies.getResourcesLocation(), "wakatime-cli"),
            Dependencies.getCLILocation());
        Logger.debug("wakatime-cli location: " + Dependencies.getCLILocation());
    }

    public static void sendHeartbeat(String file, String project, boolean isWrite)
    {
        if (!WakaTime.READY)
            return;

        final String[] cmds = buildCliCommands(file, project, isWrite);

        Logger.debug(cmds.toString());

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Process proc = Runtime.getRuntime().exec(cmds);
                    if (DEBUG)
                    {
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                        proc.waitFor();
                        String s;
                        while ((s = stdInput.readLine()) != null)
                        {
                            Logger.debug(s);
                        }
                        while ((s = stdError.readLine()) != null)
                        {
                            Logger.debug(s);
                        }
                        Logger.debug("Command finished with return value: " + proc.exitValue());
                    }
                }
                catch (Exception e)
                {
                    Logger.error(e);
                }
            }
        };
        new Thread(r).start();
    }

    public static String[] buildCliCommands(String file, String project, boolean isWrite)
    {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add(Dependencies.getCLILocation());
        cmds.add("--entity");
        cmds.add(WakaTime.fixFilePath(file));
        cmds.add("--plugin");
        cmds.add(IDE_NAME + "/" + ECLIPSE_VERSION + " edt-wakatime/" + VERSION);
        if (project != null)
        {
            cmds.add("--project");
            cmds.add(project);
        }
        if (isWrite)
            cmds.add("--write");
        if (DEBUG)
        {
            Logger.info(cmds.toString());
            cmds.add("--verbose");
        }
        return cmds.toArray(new String[cmds.size()]);
    }

    public static String getActiveProject()
    {
        var editor = getActiveTextEditor();

        if (editor == null)
            return null;

        IEditorInput input = editor.getEditorInput();

        IProject project = null;

        if (input instanceof FileEditorInput)
        {
            project = ((FileEditorInput)input).getFile().getProject();
        }

        if (project == null)
            return null;

        return project.getName();
    }

    public static String fixFilePath(String file)
    {
        return file.replaceFirst("^[\\\\/]([A-Z]:[\\\\/])", "$1");
    }

    public static void promptForApiKey(IWorkbenchWindow window)
    {
        String apiKey = ConfigFile.get("settings", "api_key", false);
        InputDialog dialog = new InputDialog(window.getShell(), "WakaTime API Key",
            "Please enter your api key from http://wakatime.com/settings#apikey", apiKey, null);
        if (dialog.open() == IStatus.OK)
        {
            apiKey = dialog.getValue();
            ConfigFile.set("settings", "api_key", false, apiKey);
        }
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static WakaTime getDefault()
    {
        return plugin;
    }

    public static AbstractTextEditor getActiveTextEditor(IWorkbenchPartReference partRef)
    {
        var part = partRef.getPart(true);

        if (part instanceof AbstractTextEditor)
        {
            return (AbstractTextEditor)part;
        }
        else if (part instanceof FormEditor)
        {
            FormEditor me = (FormEditor)part;
            part = me.getActiveEditor();
            if (part instanceof AbstractTextEditor)
            {
                return (AbstractTextEditor)part;
            }
        }
        return null;
    }

    public static AbstractTextEditor getActiveTextEditor()
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return null;
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null)
            return null;
        if (window.getPartService() == null)
            return null;
        if (window.getPartService().getActivePart() == null)
            return null;
        if (window.getPartService().getActivePart().getSite() == null)
            return null;
        if (window.getPartService().getActivePart().getSite().getPage() == null)
            return null;
        if (window.getPartService().getActivePart().getSite().getPage().getActiveEditor() == null)
            return null;

        IEditorPart editor = window.getPartService().getActivePart().getSite().getPage().getActiveEditor();

        if (editor instanceof FormEditor)
            editor = ((FormEditor)editor).getActiveEditor();

        return (AbstractTextEditor)editor;
    }

}
