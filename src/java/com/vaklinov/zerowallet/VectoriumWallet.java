/************************************************************************************************
 *  _________          _     ____          _           __        __    _ _      _   _   _ ___
 * |__  / ___|__ _ ___| |__ / ___|_      _(_)_ __   __ \ \      / /_ _| | | ___| |_| | | |_ _|
 *   / / |   / _` / __| '_ \\___ \ \ /\ / / | '_ \ / _` \ \ /\ / / _` | | |/ _ \ __| | | || |
 *  / /| |__| (_| \__ \ | | |___) \ V  V /| | | | | (_| |\ V  V / (_| | | |  __/ |_| |_| || |
 * /____\____\__,_|___/_| |_|____/ \_/\_/ |_|_| |_|\__, | \_/\_/ \__,_|_|_|\___|\__|\___/|___|
 *                                                 |___/
 *
 * Copyright (c) 2016 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.vectoriumwallet;


import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.vaklinov.vectoriumwallet.OSUtil.OS_TYPE;
import com.vaklinov.vectoriumwallet.VectoriumClientCaller.NetworkAndBlockchainInfo;
import com.vaklinov.vectoriumwallet.VectoriumClientCaller.WalletCallException;
import com.vaklinov.vectoriumwallet.VectoriumInstallationObserver.DAEMON_STATUS;
import com.vaklinov.vectoriumwallet.VectoriumInstallationObserver.DaemonInfo;
import com.vaklinov.vectoriumwallet.VectoriumInstallationObserver.InstallationDetectionException;


/**
 * Main Vectorium Window.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class VectoriumWallet
    extends JFrame
{
	private static final long serialVersionUID = -8056343020670619627L;

	private VectoriumInstallationObserver installationObserver;
    private VectoriumClientCaller         clientCaller;
    private StatusUpdateErrorReporter errorReporter;

    private WalletOperations walletOps;

    private JMenuItem menuItemExit;
    private JMenuItem menuItemAbout;
    private JMenuItem menuItemEncrypt;
    private JMenuItem menuItemBackup;
    private JMenuItem menuItemExportKeys;
    private JMenuItem menuItemImportKeys;
    private JMenuItem menuItemShowPrivateKey;
    private JMenuItem menuItemImportOnePrivateKey;

    private DashboardPanel   dashboard;
    private AddressesPanel   addresses;
    private SendCashPanel    sendPanel;
    
    private static WalletPreferences preferences;
    
    JTabbedPane tabs;

    public VectoriumWallet(StartupProgressDialog progressDialog)
        throws IOException, InterruptedException, WalletCallException
    {
        super("Vectorium Swing Wallet - 0.74 (beta)");
        
        if (progressDialog != null)
        {
        	progressDialog.setProgressText("Starting GUI wallet...");
        }
        
        ClassLoader cl = this.getClass().getClassLoader();

        this.setIconImage(new ImageIcon(cl.getResource("images/vectorium.png")).getImage());

        Container contentPane = this.getContentPane();

        errorReporter = new StatusUpdateErrorReporter(this);
        installationObserver = new VectoriumInstallationObserver(preferences.commandLineToolsDir());
        clientCaller = new VectoriumClientCaller(preferences.commandLineToolsDir());

        // Build content
        tabs = new JTabbedPane();
        tabs.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
        tabs.addTab("Overview ",
        		    new ImageIcon(cl.getResource("images/overview.png")),
        		    dashboard = new DashboardPanel(this, installationObserver, clientCaller, errorReporter));
        tabs.addTab("Own addresses ",
        		    new ImageIcon(cl.getResource("images/own-addresses.png")),
        		    addresses = new AddressesPanel(clientCaller, errorReporter));
        tabs.addTab("Send cash ",
        		    new ImageIcon(cl.getResource("images/send.png")),
        		    sendPanel = new SendCashPanel(clientCaller, errorReporter));
        tabs.addTab("Address book ",
    		        new ImageIcon(cl.getResource("images/address-book.png")),
    		        new AddressBookPanel(sendPanel, tabs));
        tabs.addTab("Preferences",
	        		new ImageIcon(cl.getResource("images/preferences.png")),
    				new PreferencesPanel(preferences));
        contentPane.add(tabs);

        this.walletOps = new WalletOperations(
            	this, tabs, dashboard, addresses, sendPanel, installationObserver, clientCaller, errorReporter);

        this.setSize(new Dimension(960, 427));

        // Build menu
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("Main");
        file.setMnemonic(KeyEvent.VK_M);
        int accelaratorKeyMask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask();
        file.add(menuItemAbout = new JMenuItem("About...", KeyEvent.VK_T));
        menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, accelaratorKeyMask));
        file.addSeparator();
        file.add(menuItemExit = new JMenuItem("Quit", KeyEvent.VK_Q));
        menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, accelaratorKeyMask));
        mb.add(file);

        JMenu wallet = new JMenu("Wallet");
        wallet.setMnemonic(KeyEvent.VK_W);
        wallet.add(menuItemBackup = new JMenuItem("Backup...", KeyEvent.VK_B));
        menuItemBackup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, accelaratorKeyMask));
        wallet.add(menuItemEncrypt = new JMenuItem("Encrypt...", KeyEvent.VK_E));
        menuItemEncrypt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, accelaratorKeyMask));
        wallet.add(menuItemExportKeys = new JMenuItem("Export private keys...", KeyEvent.VK_K));
        menuItemExportKeys.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, accelaratorKeyMask));
        wallet.add(menuItemImportKeys = new JMenuItem("Import private keys...", KeyEvent.VK_I));
        menuItemImportKeys.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, accelaratorKeyMask));
        wallet.add(menuItemShowPrivateKey = new JMenuItem("Show private key...", KeyEvent.VK_P));
        menuItemShowPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, accelaratorKeyMask));
        wallet.add(menuItemImportOnePrivateKey = new JMenuItem("Import one private key...", KeyEvent.VK_N));
        menuItemImportOnePrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelaratorKeyMask));        
        mb.add(wallet);

        // Some day the extras menu will be populated with less essential functions
        //JMenu extras = new JMenu("Extras");
        //extras.setMnemonic(KeyEvent.VK_ NOT R);
        //extras.add(menuItemAddressBook = new JMenuItem("Address book...", KeyEvent.VK_D));
        //menuItemAddressBook.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, accelaratorKeyMask));        
        //mb.add(extras);

        // TODO: Temporarily disable encryption until further notice - Oct 24 2016
        menuItemEncrypt.setEnabled(false);
                        
        this.setJMenuBar(mb);

        // Add listeners etc.
        menuItemExit.addActionListener(
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    VectoriumWallet.this.exitProgram();
                }
            }
        );

        menuItemAbout.addActionListener(
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	try
                	{
                		AboutDialog ad = new AboutDialog(VectoriumWallet.this);
                		ad.setVisible(true);
                	} catch (UnsupportedEncodingException uee)
                	{
                		Log.error("Unexpected error: ", uee);
                		VectoriumWallet.this.errorReporter.reportError(uee);
                	}
                }
            }
        );

        menuItemBackup.addActionListener(   
        	new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    VectoriumWallet.this.walletOps.backupWallet();
                }
            }
        );
        
        menuItemEncrypt.addActionListener(
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    VectoriumWallet.this.walletOps.encryptWallet();
                }
            }
        );

        menuItemExportKeys.addActionListener(   
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    VectoriumWallet.this.walletOps.exportWalletPrivateKeys();
                }
            }
       );
        
       menuItemImportKeys.addActionListener(   
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    VectoriumWallet.this.walletOps.importWalletPrivateKeys();
                }
            }
       );
       
       menuItemShowPrivateKey.addActionListener(   
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    VectoriumWallet.this.walletOps.showPrivateKey();
                }
            }
       );
       
       menuItemImportOnePrivateKey.addActionListener(   
           new ActionListener()
           {
               @Override
               public void actionPerformed(ActionEvent e)
               {
                   VectoriumWallet.this.walletOps.importSinglePrivateKey();
               }
           }
       );
       
        // Close operation
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                VectoriumWallet.this.exitProgram();
            }
        });

        // Show initial message
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    String userDir = OSUtil.getSettingsDirectory();
                    File warningFlagFile = new File(userDir + File.separator + "initialInfoShown.flag");
                    if (warningFlagFile.exists())
                    {
                        return;
                    } else
                    {
                        warningFlagFile.createNewFile();
                    }

                } catch (IOException ioe)
                {
                    /* TODO: report exceptions to the user */
                	Log.error("Unexpected error: ", ioe);
                }

                JOptionPane.showMessageDialog(
                    VectoriumWallet.this.getRootPane().getParent(),
                    "The Vectorium GUI Wallet is currently considered experimental. Use of this software\n" +
                    "comes at your own risk! \n" +
                    "\n\n" +
                    "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                    "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                    "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                    "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                    "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                    "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\n" +
                    "THE SOFTWARE.\n\n" +
                    "(This message will be shown only once)",
                    "Disclaimer", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Finally dispose of the progress dialog
        if (progressDialog != null)
        {
        	progressDialog.doDispose();
        }
    }

    public void exitProgram()
    {
    	Log.info("Exiting ...");

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        this.dashboard.stopThreadsAndTimers();
        this.addresses.stopThreadsAndTimers();
        this.sendPanel.stopThreadsAndTimers();

//        Integer blockchainProgress = this.dashboard.getBlockchainPercentage();
//        
//        if ((blockchainProgress != null) && (blockchainProgress >= 100))
//        {
//	        this.dashboard.waitForEndOfThreads(3000);
//	        this.addresses.waitForEndOfThreads(3000);
//	        this.sendPanel.waitForEndOfThreads(3000);
//        }
        
        VectoriumWallet.this.setVisible(false);
        VectoriumWallet.this.dispose();

        System.exit(0);
    }

    public static void main(String argv[])
        throws IOException
    {
        try
        {
        	OS_TYPE os = OSUtil.getOSType();
        	
        	preferences = new WalletPreferences(os);
        	
        	Log.info("Starting Vectorium Swing Wallet ...");
        	Log.info("OS: " + System.getProperty("os.name") + " = " + os);
        	Log.info("Current directory: " + new File(".").getCanonicalPath());
        	Log.info("Class path: " + System.getProperty("java.class.path"));
        	Log.info("Environment PATH: " + System.getenv("PATH"));

        	if (os == OS_TYPE.WINDOWS)
        	{
        		// Custom Windows L&F and font settings
        		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

        		// This font looks good but on Windows 7 it misses some chars like the stars...
        		//FontUIResource font = new FontUIResource("Lucida Sans Unicode", Font.PLAIN, 11);
        		//UIManager.put("Table.font", font);
        	} else
        		if (os == OS_TYPE.MAC_OS)
        		{
        			UIManager.setLookAndFeel("com.apple.laf.AquaLookAndFeel");
        		} else
        		{            
        			for (LookAndFeelInfo ui : UIManager.getInstalledLookAndFeels())
        			{
        				Log.info("Available look and feel: " + ui.getName() + " " + ui.getClassName());
        				if (ui.getName().equals("Nimbus"))
        				{
        					UIManager.setLookAndFeel(ui.getClassName());
        					break;
        				};
        			}
        		}
            
            // If vectoriumd is currently not running, do a startup of the daemon as a child process
            // It may be started but not ready - then also show dialog
            VectoriumInstallationObserver initialInstallationObserver = 
            	new VectoriumInstallationObserver(preferences.commandLineToolsDir());
            DaemonInfo vectoriumdInfo = initialInstallationObserver.getDaemonInfo();
            initialInstallationObserver = null;
            
            VectoriumClientCaller initialClientCaller = new VectoriumClientCaller(preferences.commandLineToolsDir());
            boolean daemonStartInProgress = false;
            try
            {
            	if (vectoriumdInfo.status == DAEMON_STATUS.RUNNING)
            	{
            		NetworkAndBlockchainInfo info = initialClientCaller.getNetworkAndBlockchainInfo();
            		// If more than 20 minutes behind in the blockchain - startup in progress
            		if ((System.currentTimeMillis() - info.lastBlockDate.getTime()) > (20 * 60 * 1000))
            		{
            			Log.info("Current blockchain synchronization date is"  + 
            		                       new Date(info.lastBlockDate.getTime()));
            			daemonStartInProgress = true;
            		}
            	}
            } catch (WalletCallException wce)
            {
                if ((wce.getMessage().indexOf("{\"code\":-28") != -1) || // Started but not ready
                	(wce.getMessage().indexOf("error code: -28") != -1))
                {
                	Log.info("vectoriumd is currently starting...");
                	daemonStartInProgress = true;
                }
            }
            
            StartupProgressDialog startupBar = null;
            if ((vectoriumdInfo.status != DAEMON_STATUS.RUNNING) || (daemonStartInProgress))
            {
            	Log.info(
            		"vectoriumd is not runing at the moment or has not started/synchronized 100% - showing splash...");
	            startupBar = new StartupProgressDialog(initialClientCaller);
	            startupBar.setVisible(true);
	            startupBar.waitForStartup();
            }
            initialClientCaller = null;
            
            // Main GUI is created here
            VectoriumWallet ui = new VectoriumWallet(startupBar);
            ui.setVisible(true);

        } catch (InstallationDetectionException ide)
        {
        	Log.error("Unexpected error: ", ide);
            JOptionPane.showMessageDialog(
                null,
                "This program was started in directory: " + OSUtil.getProgramDirectory() + "\n" +
                ide.getMessage() + "\n" +
                "See the console output for more detailed error information!",
                "Installation error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (WalletCallException wce)
        {
        	Log.error("Unexpected error: ", wce);

            if ((wce.getMessage().indexOf("{\"code\":-28,\"message\"") != -1) ||
            	(wce.getMessage().indexOf("error code: -28") != -1))
            {
                JOptionPane.showMessageDialog(
                        null,
                        "It appears that vectoriumd has been started but is not ready to accept wallet\n" +
                        "connections. It is still loading the wallet and blockchain. Please try to \n" +
                        "start the GUI wallet later...",
                        "Wallet communication error",
                        JOptionPane.ERROR_MESSAGE);
            } else
            {
                JOptionPane.showMessageDialog(
                    null,
                    "There was a problem communicating with the Vectorium daemon/wallet. \n" +
                    "Please ensure that the Vectorium server vectoriumd is started (e.g. via \n" + 
                    "command  \"vectoriumd --daemon\"). Error message is: \n" +
                     wce.getMessage() +
                    "See the console output for more detailed error information!",
                    "Wallet communication error",
                    JOptionPane.ERROR_MESSAGE);
            }

            System.exit(2);
        } catch (Exception e)
        {
        	Log.error("Unexpected error: ", e);
            JOptionPane.showMessageDialog(
                null,
                "A general unexpected critical error has occurred: \n" + e.getMessage() + "\n" +
                "See the console output for more detailed error information!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(3);
        }  catch (Error err)
        {
        	// Last resort catch for unexpected problems - just to inform the user
        	Log.error("Unexpected unrecovverable error: ", err);
            JOptionPane.showMessageDialog(
                null,
                "A general unexpected critical/unrecoverable error has occurred: \n" + err.getMessage() + "\n" +
                "See the console output for more detailed error information!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(4);
        }
    }
}
