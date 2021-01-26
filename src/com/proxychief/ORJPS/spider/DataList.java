/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.proxychief.ORJPS.spider;

import com.proxychief.ORJPS.spider.thread.Scraper;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author Proxychief
 */
public class DataList extends ConcurrentSkipListSet<String> {

    Scraper parent;

    public DataList(Scraper p) {
        parent = p;
    }

    public void addData(String s, String origin) {
        if (this.add(s)) {
            parent.onNewData(s, origin);
        } else {
            parent.onDuplicateData(s, origin);
        }
    }

    public void addDataFromStringArray(String[] data, String origin) {
        if (data != null) {
            for (String s : data) {
                if (s != null) {
                    addData(s, origin);
                }
            }
        }
    }
}
