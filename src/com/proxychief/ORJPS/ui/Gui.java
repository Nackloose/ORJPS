/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * Gui.java
 *
 * Created on Nov 16, 2011, 10:12:47 AM
 */
package com.proxychief.ORJPS.ui;

import com.congeriem.StringUtil;
import com.proxychief.ORJPS.Spyder;
import com.proxychief.ORJPS.spider.thread.Scraper;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Proxychief
 */
public class Gui extends javax.swing.JFrame {

    public static ImageIcon ICON_GOOD, ICON_GOOD_HOVER, ICON_IN_PROGRESS, ICON_IN_PROGRESS_HOVER, ICON_ERROR, ICON_ERROR_HOVER, ICON_HALT,
            ICON_HALT_HOVER, ICON_BOX_CHECKED, ICON_BOX_INPROGRESS, ICON_BOX_UNCHECKED;
    private static DefaultListModel outputModel, urlsToScrapeModel;
    private static DefaultTableModel urlListModel, proxyListModel;
    private calculatePerSec calc = new calculatePerSec();
    private static final HashMap urlList = new HashMap();
    private static final HashMap proxyList = new HashMap();
    public String currentURL = "", dataCurrent = "", status = "Not running...", lastWebCrawlerStatus;
    int currentTabIndex = 0, urlTotal = 0, uniqueURLCount = 0, dataTotal = 0, dataUniqueTotal = 0,
            networkConnectionsTotal = 0, failuresTotal = 0, successTotal = 0, webCrawlerUniqueUrlsSec = 0,
            webCrawlerUniqueProxiesSec = 0, webCrawlerUrlsSec = 0, webCrawlerProxiesSec = 0, webCrawlerUrlScrapeSec = 0,
            secsCrawling = 0;
    long lastURLScroll = 0;
    long networkByesTotal = 0;
    int totalCheckThreads = 0;
    JPopupMenu urlListBoxRightClick = new JPopupMenu();
    Scraper scraper = new Scraper() {

        @Override
        public void onScrapeStart(String url) {
            currentURL = url;
            networkConnectionsTotal++;
            setUrlInProgress(url);
        }

        @Override
        public void onScrapeSuccess(String url, int urlCount, int proxyCount, long bytes) {
            setUrlCompleted(url, urlCount, proxyCount);
            successTotal++;
            networkByesTotal += bytes;
        }

        @Override
        public void onFailure(String url) {
            setUrlFailed(url);
            failuresTotal++;
        }

        @Override
        public void onNewURL(String url) {
            addNewUrl(url);
            uniqueURLCount++;
            urlTotal++;
        }

        @Override
        public void onDuplicateURL(String url) {
            urlTotal++;
        }

        @Override
        public void onNewData(String data, String originURL) {
            dataCurrent = data;
            dataUniqueTotal++;
            dataTotal++;
        }

        @Override
        public void onDuplicateData(String data, String originUrl) {
            dataTotal++;
        }

        @Override
        public void onStatus(String statusLine) {
            status = statusLine;
        }
    };
    public ImageIcon lastIcon;
    public Spyder parent;

    /**
     * Creates new form Gui
     */
    public Gui() throws UnsupportedLookAndFeelException {
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        System.setProperty("sun.awt.noerasebackground", "true");
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        try {
            UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ICON_GOOD = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/Good.png")), 4);
        ICON_GOOD_HOVER = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/Good_Hover.png")), 4);
        ICON_IN_PROGRESS = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/InProgress.png")), 4);
        ICON_IN_PROGRESS_HOVER = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/InProgress_Hover.png")), 4);
        ICON_HALT = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/Error_Halt.png")), 4);
        ICON_HALT_HOVER = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/Error_Halt_Hover.png")), 4);
        ICON_ERROR = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/Bad.png")), 16);
        ICON_BOX_CHECKED = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/Box_Checked.png")), 16);
        ICON_BOX_INPROGRESS = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/Box_InProgress.png")), 16);
        ICON_BOX_UNCHECKED = scaleIcon(new ImageIcon(getClass().getResource("/com/proxychief/ORJPS/res/Box_Unchecked.png")), 16);
        outputModel = new DefaultListModel();
        urlsToScrapeModel = new DefaultListModel();
        urlListBoxRightClick.add("Open url in browser");
        urlListModel = new DefaultTableModel(new Object[][]{}, new Object[]{"", "URL", "Urls Scraped", "Proxies Scraped"}) {
            boolean[] canEdit = new boolean[]{
                false, false, false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                Class<?> type = super.getColumnClass(columnIndex);
                if (columnIndex == 0) {
                    type = Icon.class;
                } else {
                    type = super.getColumnClass(columnIndex);
                }
                return type;
            }
        };
        proxyListModel = new DefaultTableModel(new Object[][]{}, new Object[]{"", "IP:Port", "Location", "Type", "Ping", "Speed"}) {
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false, false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                Class<?> type = super.getColumnClass(columnIndex);
                if (columnIndex == 0) {
                    type = Icon.class;
                } else {
                    type = super.getColumnClass(columnIndex);
                }
                return type;
            }
        };
        initComponents();
        urlListBox.getColumnModel().getColumn(0).setCellRenderer(iconHeaderRenderer);
        urlListBox.getColumnModel().getColumn(0).setMinWidth(24);
        urlListBox.getColumnModel().getColumn(0).setMaxWidth(24);
        urlListBox.getColumnModel().getColumn(1).setMinWidth(800);
        urlListBox.getColumnModel().getColumn(2).setMinWidth(100);
        urlListBox.getColumnModel().getColumn(3).setMinWidth(100);
        urlListBox.setRowHeight(24);
        urlListBox.getColumnModel().getColumn(0).setResizable(false);
        urlListBox.getColumnModel().getColumn(2).setResizable(false);
        urlListBox.getColumnModel().getColumn(3).setResizable(false);
        proxyListBox.getColumnModel().getColumn(0).setCellRenderer(iconHeaderRenderer);
        proxyListBox.getColumnModel().getColumn(0).setMinWidth(24);
        proxyListBox.getColumnModel().getColumn(0).setMaxWidth(24);
        proxyListBox.getColumnModel().getColumn(1).setMinWidth(250);
        proxyListBox.getColumnModel().getColumn(1).setMaxWidth(250);
        proxyListBox.getColumnModel().getColumn(2).setMinWidth(75);
        proxyListBox.getColumnModel().getColumn(2).setMaxWidth(75);
        proxyListBox.getColumnModel().getColumn(3).setMinWidth(150);
        proxyListBox.getColumnModel().getColumn(3).setMaxWidth(150);
        proxyListBox.setRowHeight(24);
        proxyListBox.getColumnModel().getColumn(0).setResizable(false);
        proxyListBox.getColumnModel().getColumn(2).setResizable(false);
        proxyListBox.getColumnModel().getColumn(3).setResizable(false);
        urlsToScrapeModel.addElement("http://www.forumproxyleecher.com/liststat.php");
        urlListBox.setComponentPopupMenu(urlListBoxRightClick);
        setTitle(Spyder.getAppTitle());
        log(Spyder.getAppTitle());
        new Timer().scheduleAtFixedRate(updateWebCrawlerStatusText, 0, 100);
    }

    public void scrollUrlListBox(int row) {
        urlListBox.scrollRectToVisible(urlListBox.getCellRect(row, 0, false));
    }

    private void log(String msg) {
        print(this.getClass().getName(), msg);
    }

    private void log(String msg, Object... o) {
        log(String.format(msg, o));
    }

    public static void print(String className, String msg) {
        if (outputModel != null) {
            outputModel.addElement("[" + className + "]:" + msg);
            outputBox.ensureIndexIsVisible(outputModel.getSize() - 1);
        }
    }

    public ImageIcon scaleIcon(ImageIcon ii, int divideBy) {
        return new ImageIcon(ii.getImage().getScaledInstance((ii.getIconWidth() / divideBy), (ii.getIconHeight() / divideBy), Image.SCALE_SMOOTH));
    }

    public void addNewUrl(String url) {
        if (urlListModel != null) {
            urlListModel.addRow(new Object[]{Gui.ICON_BOX_UNCHECKED, url, 0, 0});
            urlList.put(url, urlList.size());
        }
    }

    public void addNewProxy(String proxy) {
        if (proxyListModel != null) {
            proxyListModel.addRow(new Object[]{Gui.ICON_BOX_UNCHECKED, proxy});
            proxyList.put(proxy, proxyList.size());
        }
    }

    public void setUrlInProgress(String url) {
        if (urlListModel != null) {
            int urlIndex = Integer.parseInt(urlList.get(url).toString());
            if (urlIndex < 0) {
                urlIndex = 0;
            }
            urlListModel.setValueAt(Gui.ICON_BOX_INPROGRESS, urlIndex, 0);
            if ((System.currentTimeMillis() - lastURLScroll) / 1000 > 3) {
                urlListBox.scrollRectToVisible(urlListBox.getCellRect(urlIndex + 6, 0, false));
                lastURLScroll = System.currentTimeMillis();
            }
        }
    }

    public void setUrlCompleted(String url, int urlCount, int proxyCount) {
        if (urlListModel != null) {
            int urlIndex = Integer.parseInt(urlList.get(url).toString());
            if (urlIndex < 0) {
                urlIndex = 0;
            }
            urlListModel.setValueAt(Gui.ICON_BOX_CHECKED, urlIndex, 0);
            urlListModel.setValueAt(urlCount, urlIndex, 2);
            urlListModel.setValueAt(proxyCount, urlIndex, 3);
        }
    }

    public void setUrlFailed(String url) {
        if (urlListModel != null) {
            int urlIndex = Integer.parseInt(urlList.get(url).toString());
            if (urlIndex < 0) {
                urlIndex = 0;
            }
            urlListModel.setValueAt(Gui.ICON_ERROR, urlIndex, 0);
        }
    }

    public String[] getUrlsToScrape() {
        return StringUtil.objectArrayToStringArray(urlsToScrapeModel.toArray());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabs = new javax.swing.JTabbedPane();
        thirdPanel = new javax.swing.JPanel();
        crawlTime = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        secondPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        webCrawlerThreadSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        webCrawlerThreadSpeed = new javax.swing.JProgressBar();
        webCrawlerThreadPerformance = new javax.swing.JProgressBar();
        jPanel2 = new javax.swing.JPanel();
        webCrawlerStopButton = new javax.swing.JButton();
        webCrawlerPauseButton = new javax.swing.JButton();
        webCrawlerStartButton = new javax.swing.JButton();
        firstPanel = new javax.swing.JPanel();
        urlScrollPane = new javax.swing.JScrollPane();
        urlsToScrapeList = new javax.swing.JList();
        urlsToScrapeInput = new javax.swing.JTextField();
        urlsToScrapeButton = new javax.swing.JButton();
        dataLabel = new javax.swing.JLabel();
        siteIndexPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        urlListBox = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        proxyListBox = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        outputBox = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        statsPanel = new javax.swing.JPanel();
        webCrawlerStatSeven = new javax.swing.JLabel();
        webCrawlerStatFive = new javax.swing.JLabel();
        webCrawlerStatSix = new javax.swing.JLabel();
        webCrawlerStatThree = new javax.swing.JLabel();
        webCrawlerStatFour = new javax.swing.JLabel();
        webCrawlerStatOne = new javax.swing.JLabel();
        webCrawlerStatTwo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(647, 597));

        tabs.setDoubleBuffered(true);
        tabs.setMinimumSize(new java.awt.Dimension(250, 250));
        tabs.setPreferredSize(new java.awt.Dimension(2, 2));
        tabs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Gui.this.mouseReleased(evt);
            }
        });

        thirdPanel.setPreferredSize(new java.awt.Dimension(0, 0));

        crawlTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        crawlTime.setText("0:0:0:0");

        jLabel5.setText("Total Scrape Time:");

        secondPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webCrawlerThreadSpinnerMouseWheelMoved(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 48)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Threads");
        jLabel10.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabel10.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webCrawlerThreadSpinnerMouseWheelMoved(evt);
            }
        });

        webCrawlerThreadSpinner.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        webCrawlerThreadSpinner.setMinimumSize(new java.awt.Dimension(0, 0));
        webCrawlerThreadSpinner.setPreferredSize(null);
        webCrawlerThreadSpinner.setValue(4);
        webCrawlerThreadSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                webCrawlerThreadSpinnerStateChanged(evt);
            }
        });
        webCrawlerThreadSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webCrawlerThreadSpinnerMouseWheelMoved(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("I like my internet:");
        jLabel3.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabel3.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webCrawlerThreadSpinnerMouseWheelMoved(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("NOM NOM DATA");
        jLabel4.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabel4.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webCrawlerThreadSpinnerMouseWheelMoved(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("vs");
        jLabel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabel1.setPreferredSize(null);
        jLabel1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webCrawlerThreadSpinnerMouseWheelMoved(evt);
            }
        });

        webCrawlerThreadSpeed.setValue(16);
        webCrawlerThreadSpeed.setMinimumSize(new java.awt.Dimension(0, 0));
        webCrawlerThreadSpeed.setPreferredSize(null);
        webCrawlerThreadSpeed.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webCrawlerThreadSpinnerMouseWheelMoved(evt);
            }
        });

        webCrawlerThreadPerformance.setValue(80);
        webCrawlerThreadPerformance.setMinimumSize(new java.awt.Dimension(0, 0));
        webCrawlerThreadPerformance.setPreferredSize(null);
        webCrawlerThreadPerformance.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webCrawlerThreadSpinnerMouseWheelMoved(evt);
            }
        });

        webCrawlerStopButton.setText("Stop");
        webCrawlerStopButton.setEnabled(false);
        webCrawlerStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webCrawlerStopButton(evt);
            }
        });

        webCrawlerPauseButton.setText("Pause");
        webCrawlerPauseButton.setEnabled(false);
        webCrawlerPauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webCrawlerPauseButton(evt);
            }
        });

        webCrawlerStartButton.setText("Start");
        webCrawlerStartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webCrawlerStartButton(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(webCrawlerPauseButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerStopButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerStartButton)
                .addGap(65, 65, 65))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(webCrawlerPauseButton)
                .addComponent(webCrawlerStopButton)
                .addComponent(webCrawlerStartButton))
        );

        javax.swing.GroupLayout secondPanelLayout = new javax.swing.GroupLayout(secondPanel);
        secondPanel.setLayout(secondPanelLayout);
        secondPanelLayout.setHorizontalGroup(
            secondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(secondPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(secondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(webCrawlerThreadSpeed, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(webCrawlerThreadPerformance, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(webCrawlerThreadSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        secondPanelLayout.setVerticalGroup(
            secondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(secondPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerThreadSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerThreadPerformance, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerThreadSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        urlsToScrapeList.setModel(urlsToScrapeModel);
        urlsToScrapeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        urlsToScrapeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                urlsToScrapeListValueChanged(evt);
            }
        });
        urlScrollPane.setViewportView(urlsToScrapeList);

        urlsToScrapeInput.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                urlsToScrapeInputFocusGained(evt);
            }
        });
        urlsToScrapeInput.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                urlsToScrapeInputCaretPositionChanged(evt);
            }
        });
        urlsToScrapeInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urlsToScrapeInputKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                urlsToScrapeInputKeyPressed(evt);
            }
        });

        urlsToScrapeButton.setText("Add");
        urlsToScrapeButton.setEnabled(false);
        urlsToScrapeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlsToScrapeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout firstPanelLayout = new javax.swing.GroupLayout(firstPanel);
        firstPanel.setLayout(firstPanelLayout);
        firstPanelLayout.setHorizontalGroup(
            firstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, firstPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(firstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(urlScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, firstPanelLayout.createSequentialGroup()
                        .addComponent(urlsToScrapeInput)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(urlsToScrapeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        firstPanelLayout.setVerticalGroup(
            firstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(firstPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(firstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlsToScrapeInput, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(urlsToScrapeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(urlScrollPane)
                .addContainerGap())
        );

        dataLabel.setText("Total Data: ");

        javax.swing.GroupLayout thirdPanelLayout = new javax.swing.GroupLayout(thirdPanel);
        thirdPanel.setLayout(thirdPanelLayout);
        thirdPanelLayout.setHorizontalGroup(
            thirdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(thirdPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(thirdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dataLabel)
                    .addGroup(thirdPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(crawlTime, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(secondPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(firstPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        thirdPanelLayout.setVerticalGroup(
            thirdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(thirdPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(thirdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(thirdPanelLayout.createSequentialGroup()
                        .addGroup(thirdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(crawlTime))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dataLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(secondPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(firstPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        tabs.addTab("Scrape Control", thirdPanel);

        jScrollPane4.setDoubleBuffered(true);

        urlListBox.setModel(urlListModel);
        urlListBox.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        urlListBox.setDoubleBuffered(true);
        urlListBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                urlListBoxMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(urlListBox);
        urlListBox.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        javax.swing.GroupLayout siteIndexPanelLayout = new javax.swing.GroupLayout(siteIndexPanel);
        siteIndexPanel.setLayout(siteIndexPanelLayout);
        siteIndexPanelLayout.setHorizontalGroup(
            siteIndexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 615, Short.MAX_VALUE)
            .addGroup(siteIndexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(siteIndexPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        siteIndexPanelLayout.setVerticalGroup(
            siteIndexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 395, Short.MAX_VALUE)
            .addGroup(siteIndexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(siteIndexPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        tabs.addTab("Url List", siteIndexPanel);

        jScrollPane5.setDoubleBuffered(true);

        proxyListBox.setModel(proxyListModel);
        proxyListBox.setDoubleBuffered(true);
        proxyListBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                proxyListBoxMouseReleased(evt);
            }
        });
        jScrollPane5.setViewportView(proxyListBox);

        jButton1.setText("Export");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabs.addTab("Proxy List", jPanel1);

        jScrollPane1.setDoubleBuffered(true);

        outputBox.setForeground(new java.awt.Color(0, 255, 255));
        outputBox.setModel(outputModel);
        outputBox.setDoubleBuffered(true);
        jScrollPane1.setViewportView(outputBox);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
        );

        tabs.addTab("Output", jPanel4);

        jLabel6.setText("Online Reprogrammable Java Page Scraper");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addContainerGap(362, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addContainerGap(366, Short.MAX_VALUE))
        );

        tabs.addTab("About", jPanel3);

        webCrawlerStatSeven.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        webCrawlerStatSeven.setText("jLabel4");

        webCrawlerStatFive.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        webCrawlerStatFive.setText("jLabel4");

        webCrawlerStatSix.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        webCrawlerStatSix.setText("jLabel4");

        webCrawlerStatThree.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        webCrawlerStatThree.setText("jLabel4");

        webCrawlerStatFour.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        webCrawlerStatFour.setText("jLabel4");

        webCrawlerStatOne.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        webCrawlerStatOne.setText("jLabel4");

        webCrawlerStatTwo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        webCrawlerStatTwo.setText("jLabel4");

        javax.swing.GroupLayout statsPanelLayout = new javax.swing.GroupLayout(statsPanel);
        statsPanel.setLayout(statsPanelLayout);
        statsPanelLayout.setHorizontalGroup(
            statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(webCrawlerStatSeven, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(webCrawlerStatSix, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(webCrawlerStatFive, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(webCrawlerStatThree, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(webCrawlerStatOne, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(webCrawlerStatTwo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(webCrawlerStatFour, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        statsPanelLayout.setVerticalGroup(
            statsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(webCrawlerStatOne)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerStatTwo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerStatThree)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerStatFour)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerStatFive)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerStatSix)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webCrawlerStatSeven)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
                    .addComponent(statsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void webCrawlerPauseButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webCrawlerPauseButton
        scraper.pause();
        if (scraper.paused()) {
            webCrawlerPauseButton.setText("Un-Pause");
            lastWebCrawlerStatus = status;
            status = "Paused";
        } else {
            webCrawlerPauseButton.setText("Pause");
            status = lastWebCrawlerStatus;
        }
        calc.pause();
        webCrawlerPauseButton.setEnabled(true);
        webCrawlerStartButton.setEnabled(false);
        webCrawlerStopButton.setEnabled(false);
    }//GEN-LAST:event_webCrawlerPauseButton

    private void webCrawlerStopButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webCrawlerStopButton
        scraper.stop();
        calc.cancel();
        status = "Stopped!";
        webCrawlerStartButton.setEnabled(true);
        webCrawlerPauseButton.setEnabled(false);
        webCrawlerStopButton.setEnabled(false);
    }//GEN-LAST:event_webCrawlerStopButton

    private void webCrawlerStartButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webCrawlerStartButton
        if (getUrlsToScrape().length == 0) {
            status = "Please put in a url!";
            return;
        }
        calc = new calculatePerSec();
        new Timer().scheduleAtFixedRate(calc, 0, 1000);
        status = "Starting scraping method...";
        webCrawlerStartButton.setEnabled(false);
        webCrawlerPauseButton.setEnabled(true);
        webCrawlerStopButton.setEnabled(true);
        scraper.start(getUrlsToScrape());
    }//GEN-LAST:event_webCrawlerStartButton

   private void mouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseReleased
   }//GEN-LAST:event_mouseReleased

   private void urlsToScrapeListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_urlsToScrapeListValueChanged
       urlsToScrapeInput.setText(urlsToScrapeList.getModel().getElementAt(urlsToScrapeList.getSelectedIndex()).toString());
       urlsToScrapeButton.setEnabled(false);
   }//GEN-LAST:event_urlsToScrapeListValueChanged

   private void urlsToScrapeInputCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_urlsToScrapeInputCaretPositionChanged
   }//GEN-LAST:event_urlsToScrapeInputCaretPositionChanged

   private void urlsToScrapeInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_urlsToScrapeInputKeyPressed
       if (StringUtil.isStringUrl(urlsToScrapeInput.getText())) {
           urlsToScrapeButton.setEnabled(true);
       } else {
           urlsToScrapeButton.setEnabled(false);
       }
   }//GEN-LAST:event_urlsToScrapeInputKeyPressed

   private void urlsToScrapeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlsToScrapeButtonActionPerformed
       urlsToScrapeModel.addElement(urlsToScrapeInput.getText());
       urlsToScrapeInput.setText("");
   }//GEN-LAST:event_urlsToScrapeButtonActionPerformed

   private void urlsToScrapeInputFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_urlsToScrapeInputFocusGained
       if (urlsToScrapeModel.contains(urlsToScrapeInput.getText())) {
           urlsToScrapeInput.setText("");
       }
   }//GEN-LAST:event_urlsToScrapeInputFocusGained

    private void webCrawlerThreadSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_webCrawlerThreadSpinnerMouseWheelMoved
        int spinner = ((Integer) webCrawlerThreadSpinner.getValue());
        updateThreadCount(spinner + evt.getWheelRotation());
    }//GEN-LAST:event_webCrawlerThreadSpinnerMouseWheelMoved

    private void webCrawlerThreadSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_webCrawlerThreadSpinnerStateChanged
        int val = ((Integer) webCrawlerThreadSpinner.getValue());
        updateThreadCount(val);
    }//GEN-LAST:event_webCrawlerThreadSpinnerStateChanged

    private void proxyListBoxMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_proxyListBoxMouseReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_proxyListBoxMouseReleased

    private void urlListBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlListBoxMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_urlListBoxMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    public void updateThreadCount(int val) {
        if (val > 20) {
            webCrawlerThreadSpinner.setValue(20);
            val = 20;
        } else if (val <= 1) {
            val = 1;
        }
        scraper.setMaxThreads(val);
        if (Integer.parseInt(webCrawlerThreadSpinner.getValue().toString()) != val) {
            webCrawlerThreadSpinner.setValue(val);
        }
        if (val == 1) {
            val = 0;
        }
        webCrawlerThreadPerformance.setValue((20 - val) * 5);
        webCrawlerThreadSpeed.setValue(val * 5);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel crawlTime;
    private javax.swing.JLabel dataLabel;
    private javax.swing.JPanel firstPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private static javax.swing.JList outputBox;
    private javax.swing.JTable proxyListBox;
    private javax.swing.JPanel secondPanel;
    private javax.swing.JPanel siteIndexPanel;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JPanel thirdPanel;
    public javax.swing.JTable urlListBox;
    private javax.swing.JScrollPane urlScrollPane;
    private javax.swing.JButton urlsToScrapeButton;
    private javax.swing.JTextField urlsToScrapeInput;
    private javax.swing.JList urlsToScrapeList;
    private javax.swing.JButton webCrawlerPauseButton;
    private javax.swing.JButton webCrawlerStartButton;
    private javax.swing.JLabel webCrawlerStatFive;
    private javax.swing.JLabel webCrawlerStatFour;
    private javax.swing.JLabel webCrawlerStatOne;
    private javax.swing.JLabel webCrawlerStatSeven;
    private javax.swing.JLabel webCrawlerStatSix;
    private javax.swing.JLabel webCrawlerStatThree;
    private javax.swing.JLabel webCrawlerStatTwo;
    private javax.swing.JButton webCrawlerStopButton;
    private javax.swing.JProgressBar webCrawlerThreadPerformance;
    private javax.swing.JProgressBar webCrawlerThreadSpeed;
    private javax.swing.JSpinner webCrawlerThreadSpinner;
    // End of variables declaration//GEN-END:variables
    TableCellRenderer iconHeaderRenderer = new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            // Inherit the colors and font from the header component
            if (table != null) {
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            if (value instanceof Icon) {
                setIcon(((Icon) value));
            } else {
                setText((value == null) ? "" : value.toString());
                setIcon(null);
            }
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    };
    long lastDataCalc = 0, lastDataSize = 0;
    TimerTask updateWebCrawlerStatusText = new TimerTask() {
        @Override
        public void run() {
            crawlTime.setText(StringUtil.getClockTimeString(secsCrawling * 1000));
            setTitle(Spyder.getAppTitle() + " | Running for " + Spyder.getTotalRunTime());
            if (currentURL != null) {
                String theUrl = currentURL;
                if (theUrl.length() > 50) {
                    theUrl = theUrl.substring(0, 50) + "...";
                }
                status = status.replace("%url", theUrl);
                status = status.replace("%proxy", dataCurrent);
                webCrawlerStatOne.setText(status);
                webCrawlerStatTwo.setText("Scraped " + StringUtil.formatNumber(networkConnectionsTotal) + " urls, " + StringUtil.formatNumber(failuresTotal) + " in error, " + StringUtil.formatNumber(successTotal) + " sucessfully, " + StringUtil.formatNumber((uniqueURLCount - networkConnectionsTotal)) + " remaining.");
                webCrawlerStatThree.setText("Scraped " + StringUtil.formatNumber(urlTotal) + " url" + (dataTotal == 1 ? "" : "s") + ", " + StringUtil.formatNumber(uniqueURLCount) + " unique.");
                webCrawlerStatFour.setText("Scraped " + StringUtil.formatNumber(dataTotal) + " prox" + (dataTotal == 1 ? "y" : "ies") + ", " + StringUtil.formatNumber(dataUniqueTotal) + " unique.");
                webCrawlerStatFive.setText("Threads: [" + scraper.size()+ "\\" + scraper.getMaxThreadCount() + "]");
                webCrawlerStatSix.setText("Total: [Urls/Sec: " + StringUtil.formatNumber(webCrawlerUrlsSec) + " | Urls/Min: " + StringUtil.formatNumber(webCrawlerUrlsSec * 60) + " | Proxies/Sec: " + StringUtil.formatNumber(webCrawlerProxiesSec) + " | Proxies/Min: " + StringUtil.formatNumber(webCrawlerProxiesSec * 60) + "]");
                webCrawlerStatSeven.setText("Unique: [Urls/Sec: " + StringUtil.formatNumber(webCrawlerUniqueUrlsSec) + " | Urls/Min: " + StringUtil.formatNumber(webCrawlerUniqueUrlsSec * 60) + " | Proxies/Sec: " + StringUtil.formatNumber(webCrawlerUniqueProxiesSec) + " | Proxies/Min: " + StringUtil.formatNumber(webCrawlerUniqueProxiesSec * 60) + "]");
                long bytess = 0;
                if (networkByesTotal > 0) {
                    bytess = networkByesTotal / secsCrawling;
                }
                dataLabel.setText("Total Data: " + readableFileSize(networkByesTotal) + " in @ avg " + readableFileSize(bytess) + "/s");
            }
        }
    };

    public class calculatePerSec extends TimerTask {

        Boolean paused = false;

        @Override
        public void run() {
            if (!paused) {
                secsCrawling++;
                webCrawlerUniqueProxiesSec = dataUniqueTotal / secsCrawling;
                webCrawlerUniqueUrlsSec = uniqueURLCount / secsCrawling;
                webCrawlerProxiesSec = dataTotal / secsCrawling;
                webCrawlerUrlsSec = urlTotal / secsCrawling;
                webCrawlerUrlScrapeSec = networkConnectionsTotal / secsCrawling;
            }
        }

        public void pause() {
            paused = !paused;
        }
    }

    public String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
