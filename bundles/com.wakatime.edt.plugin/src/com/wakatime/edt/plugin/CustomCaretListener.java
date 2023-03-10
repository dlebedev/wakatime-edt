package com.wakatime.edt.plugin;

import java.net.URI;

import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;

public class CustomCaretListener
    implements CaretListener
{
    @Override
    public void caretMoved(CaretEvent event)
    {
        var editor = WakaTime.getActiveTextEditor();

        if (editor == null)
            return;

        // log file if one is opened by default
        var plugin = WakaTime.getDefault();
        IEditorInput input = editor.getEditorInput();
        if (input instanceof IURIEditorInput) {
            URI uri = ((IURIEditorInput)input).getURI();
            if (uri != null && uri.getPath() != null) {
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
}
