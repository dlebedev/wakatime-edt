package com.wakatime.edt.plugin;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;

public class PageEventListener
    implements IPageListener
{

    @Override
    public void pageActivated(IWorkbenchPage page)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void pageClosed(IWorkbenchPage page)
    {
        WindowEventListener.removeListenersFromPage(page);
    }

    @Override
    public void pageOpened(IWorkbenchPage page)
    {
        WindowEventListener.addListenersToPage(page);
    }

}
