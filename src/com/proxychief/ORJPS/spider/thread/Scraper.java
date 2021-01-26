/* @project Browser
 * @author Nick Lisowski(Proxychief)
 * @date 02/27/2012
 * @time 10:47:11 AM
 */
package com.proxychief.ORJPS.spider.thread;

import com.congeriem.log.Log;
import com.congeriem.web.WebPage;
import com.proxychief.ORJPS.spider.DataList;
import com.congeriem.web.browser.WebBrowser;
import com.congeriem.web.browser.factory.WebPageFactory;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class Scraper {

    public abstract void onScrapeStart(String url);

    public abstract void onScrapeSuccess(String url, int urlCount, int dataCount, long length);

    public abstract void onFailure(String url);

    public abstract void onNewURL(String url);

    public abstract void onDuplicateURL(String url);

    public abstract void onNewData(String data, String originURL);

    public abstract void onDuplicateData(String data, String originUrl);

    public abstract void onStatus(String statusLine);

    private final WebBrowser browser;
    private final DataList dataList = new DataList(this);
    private final ArrayDeque<ScrapingThread> workingThreads = new ArrayDeque();
    private final ConcurrentSkipListSet<String> urlSet = new ConcurrentSkipListSet<>();
    private boolean pauseThreads = false, stop = false;
    private int maxThreads = 4;

    /*
     * Constructor @param ...
     */
    public Scraper() {
        //initilize a WebBrowser object with a custom factory
        //for building ScraperWebPages, so the webpages returned by the
        //WebBrowser object itself can contain additional functionality
        //like whether or not they have been scraped, etc.
        browser = new WebBrowser(new WebPageFactory<ScraperWebPage>() {
            @Override
            public ScraperWebPage produce(final WebBrowser browser, String url) {
                return new ScraperWebPage(browser, url) {
                    //this function will be called internally in the
                    //ScraperWebPage(extending WebPage) whenever the WebPage 
                    //object itself encounters an error loading a page,
                    //even after multiple attempts and fallback methods.
                    @Override
                    public void onFailure(String url) {
                        //every scraper page that is made should pass its failure
                        //onto its parent WebBrowser object in order for the 
                        //WebBrowser object we are using to recieve our invidual
                        //pages errors, so we can pass them all on from there
                        browser.onPageFailure(url);
                    }
                };
            }
        }) {
            //this function will be called internally in the WebBrowser object
            //whenever any page gives an error back to it.
            @Override
            public void onPageFailure(String url) {
                //here we simply pass off any errors given by a pages failure
                //from inside the scraper, off to the abstract scraper fuctions
                //themselves, allowing the implementer of the scraper object
                //do decide how to display to the user that a failure has occured
                onFailure(url);
            }
        };
        Log.log("Initalized", "Scraper");
    }

    protected void procURLS(String... urls) {
        for (String url : urlSet) {
            if (urlSet.add(url)) {
                onNewURL(url);
                browser.open(url);
            } else {
                onDuplicateURL(url);
            }
        }
    }

    public void start(String... urls) {
        procURLS(urls);
    }

    public void pause() {
        pauseThreads = !pauseThreads;
        workingThreads.forEach((ScrapingThread t) -> t.setPaused(pauseThreads));
        if (pauseThreads) {
            Log.log("ThreadController", "Paused");
        } else {
            Log.log("ThreadController", "Un-Paused");
        }
    }

    public void stop() {
        stop = true;
    }

    public boolean paused() {
        return pauseThreads;
    }

    public WebBrowser getBrowser() {
        return browser;
    }

    public DataList getDataList() {
        return dataList;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }


    public int getMaxThreadCount() {
        return maxThreads;
    }

    public int size() {
        return workingThreads.size();
    }

    public class ThreadController extends Thread {

        int pauseLoop = 0;
        boolean running = true;
        Scraper p;

        public ThreadController(Scraper c) {
            p = c;
            Log.log("Initlized", "ScraperThreadController");
        }

        @Override
        public void run() {
            while (!stop) {
                if (!pauseThreads) {
                    onStatus("Scraping...");
                    while (workingThreads.size() < maxThreads) {
                        workingThreads.add(new ScrapingThread(Scraper.this));
                    }
                    try {
                        sleep(500);
                    } catch (InterruptedException ex) {
                    }
                } else {
                    onStatus("Paused...");
                    pauseLoop++;
                    if (pauseLoop > 20) {
                        Log.log("ScraperThreadController", "Paused");
                        pauseLoop = 0;
                    }
                }
            }
            //cleanup and stop everything
            onStatus("Stopped.");
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public abstract class ScraperWebPage extends WebPage {

        private boolean scraped = false;
        private String[] data = new String[0];

        public ScraperWebPage(String url) {
            super(url);
        }

        public ScraperWebPage(WebBrowser parent, String url) {
            super(parent, url);
        }

        public boolean isScraped() {
            return scraped;
        }

        public void setScraped(boolean scraped) {
            this.scraped = scraped;
        }

        public String[] getData() {
            return data;
        }

        public void setData(String[] data) {
            this.data = data;
        }

    }
}
