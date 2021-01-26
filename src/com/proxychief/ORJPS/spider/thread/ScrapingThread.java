/* @project Browser
 * @author Nick Lisowski(Proxychief)
 * @date 02/27/2012
 * @time 11:18:33 AM
 */
package com.proxychief.ORJPS.spider.thread;

import com.proxychief.ORJPS.spider.DataList;
import com.congeriem.web.browser.WebBrowser;
import com.congeriem.web.WebPage;
import com.proxychief.ORJPS.spider.thread.Scraper.ScraperWebPage;
import com.proxychief.ORJPS.ui.Gui;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScrapingThread extends Thread {

    private Scraper parent;
    private ScraperWebPage page;
    private boolean paused = false, done = false, destroy = false;
    private static int SCRAPER_COUNT = 0;

    /*
     * Constructor @param tc Parent ThreadController
     */
    public ScrapingThread(Scraper parent) {
        this.parent = parent;
    }

    @Override
    public synchronized void run() {
        this.setName("Scraper " + SCRAPER_COUNT++);
        WebBrowser web = parent.getBrowser();
        while (true) {
            if (paused || destroy) {
                sleepWait(5000);
                continue;
            }
            Optional<WebPage> result = parent.getBrowser().values().stream().filter((ScraperWebPage p) -> !p.isInProgress()).findFirst();
            if (result == null || !result.isPresent()) {
                continue;
            }
            page = (ScraperWebPage) result.get();
            page.setInProgress(true);
            String url = page.getPageUrl().toString();
            parent.onScrapeStart(url);
            parent.onStatus("Loading url %url");
            page.loadPage();
            while (!page.isLoaded() && !page.isBroken()) {
                sleepWait(5);
            }
            parent.onStatus("Scraping url %url");
            String[] data = page.getData();
            if (page.isLoaded() && data != null) {
                String[] urls = page.getLinksRegex();
                parent.procURLS(urls);
                DataList dataList = parent.getDataList();
                dataList.addDataFromStringArray(data, url);
                parent.onStatus("%url had " + urls.length + " URL(s) & " + data.length + " data(s)");
                parent.onScrapeSuccess(url, urls.length, data.length, page.getLength());
                page.setScraped(true);
            }
            if (page.isBroken() || page.isScraped()) {
                page.close();
            }
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(Boolean b) {
        paused = b;
    }

    public boolean isDone() {
        return done;
    }

    public void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }

    public void log(String msg) {
        Gui.print(this.getName(), msg);
    }

    public void sleepWait(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            Logger.getLogger(ScrapingThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String toString() {
        return super.toString();
    }
}
