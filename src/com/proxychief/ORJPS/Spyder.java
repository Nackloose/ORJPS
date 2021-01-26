/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.proxychief.ORJPS;

import com.congeriem.StringUtil;
import com.proxychief.ORJPS.ui.Gui;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Proxychief
 */
public class Spyder {

    public static final double VERSION = 2.0;
    public static long startTime;
    public static Gui gui;

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        startTime = System.currentTimeMillis();
        gui = new Gui();
        gui.setVisible(true);
    }

    public static String getTotalRunTime() {
        return StringUtil.getClockTimeString(System.currentTimeMillis() - startTime);
    }

    public static String getAppTitle() {
        return "ORJPS v" + VERSION ;
    }
}
