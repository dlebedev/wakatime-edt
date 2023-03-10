package com.wakatime.edt.plugin;

import java.net.URI;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;

public class CustomExecutionListener
    implements IExecutionListener
{

    @Override
    public void notHandled(String commandId, NotHandledException exception)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void postExecuteFailure(String commandId, ExecutionException exception)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void postExecuteSuccess(String commandId, Object returnValue)
    {
        if (commandId.equals("org.eclipse.ui.file.save"))
        {
            var editor = WakaTime.getActiveTextEditor();

            if (editor == null)
                return;

            // log file save event
            var plugin = WakaTime.getDefault();
            IEditorInput input = editor.getEditorInput();
            if (input instanceof IURIEditorInput)
            {
                URI uri = ((IURIEditorInput)input).getURI();
                if (uri != null && uri.getPath() != null)
                {
                    String currentFile = uri.getPath();
                    long currentTime = System.currentTimeMillis() / 1000;

                    // always log writes
                    WakaTime.sendHeartbeat(currentFile, WakaTime.getActiveProject(), true);
                    plugin.lastFile = currentFile;
                    plugin.lastTime = currentTime;
                }
            }
        }
    }

    @Override
    public void preExecute(String commandId, ExecutionEvent event)
    {
        // TODO Auto-generated method stub
    }
}
