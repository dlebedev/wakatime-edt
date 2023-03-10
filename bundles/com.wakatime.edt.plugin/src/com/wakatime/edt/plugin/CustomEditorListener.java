package com.wakatime.edt.plugin;

import java.net.URI;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class CustomEditorListener
    implements IPartListener2
{
    @Override
    public void partActivated(IWorkbenchPartReference partRef)
    {

        var editor = WakaTime.getActiveTextEditor(partRef);

        if (editor == null)
            return;

        // log new active file
        var plugin = WakaTime.getDefault();
        IEditorInput input = editor.getEditorInput();
        if (input instanceof IURIEditorInput)
        {
            URI uri = ((IURIEditorInput)input).getURI();
            if (uri != null && uri.getPath() != null)
            {
                String currentFile = uri.getPath();
                long currentTime = System.currentTimeMillis() / 1000;
                if (!currentFile.equals(plugin.lastFile) || plugin.lastTime + WakaTime.FREQUENCY * 60 < currentTime)
                {
                    WakaTime.sendHeartbeat(currentFile, WakaTime.getActiveProject(), false);
                    plugin.lastFile = currentFile;
                    plugin.lastTime = currentTime;
                }
            }
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef)
    {

        // listen for caret movement
        try
        {
            var part = partRef.getPart(true);

            if (part instanceof FormEditor)
            {
                FormEditor me = (FormEditor)part;
                part = me.getActiveEditor();
            }

            if (part instanceof AbstractTextEditor)
            {
                ((StyledText)part.getAdapter(Control.class)).addCaretListener(new CustomCaretListener());
            }
        }
        catch (Exception e)
        {
        }
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef)
    {
        // TODO Auto-generated method stub
    }

}
