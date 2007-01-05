/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 * 
 * Copyright 2006 James Murty
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.jets3t.apps.cockpit.gui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jets3t.apps.cockpit.CockpitPreferences;
import org.jets3t.gui.ErrorDialog;
import org.jets3t.gui.HyperlinkActivatedListener;
import org.jets3t.gui.JHtmlLabel;
import org.jets3t.service.security.EncryptionUtil;

import com.centerkey.utils.BareBonesBrowserLaunch;

/**
 * Dialog box for managing Cockpit Preferences, as reflected in the file <tt>cockpit.properties</tt>. 
 * <p>
 * 
 * @author James Murty
 */
public class PreferencesDialog extends JDialog implements ActionListener, ChangeListener {
    private static final Log log = LogFactory.getLog(PreferencesDialog.class);

    private static PreferencesDialog preferencesDialog = null;
    
    private CockpitPreferences cockpitPreferences = null; 
    
    private Frame ownerFrame = null;
    private HyperlinkActivatedListener hyperlinkListener = null;
    
    private ButtonGroup aclButtonGroup = null;
    private ButtonGroup compressButtonGroup = null;
    private ButtonGroup encryptButtonGroup = null;
    private JPasswordField encryptPasswordField = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    private JTabbedPane tabbedPane = null;
    
    private final Insets insetsDefault = new Insets(3, 5, 3, 5);
    
    /**
     * Creates a modal dialog box with a title.
     * 
     * @param owner
     * the frame within which this dialog will be displayed and centred.
     * @param jets3tHomeDirectory
     */
    private PreferencesDialog(CockpitPreferences cockpitPreferences, Frame owner, 
        HyperlinkActivatedListener hyperlinkListener) 
    {
        super(owner, "Cockpit Preferences", true);
        this.cockpitPreferences = cockpitPreferences;
        this.ownerFrame = owner;
        this.hyperlinkListener = hyperlinkListener;
        this.initGui();
    }
    
    /**
     * Initialises all GUI elements.
     */
    private void initGui() {
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        String introductionText = "<html><center>Configure Cockpit's preferences</center></html>";
        JHtmlLabel introductionLabel = new JHtmlLabel(introductionText, hyperlinkListener);
        introductionLabel.setHorizontalAlignment(JLabel.CENTER);

        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);
        okButton = new JButton("Apply preferences");
        okButton.setActionCommand("ApplyPreferences");
        okButton.addActionListener(this);

        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        buttonsPanel.add(cancelButton, new GridBagConstraints(0, 0, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));
        buttonsPanel.add(okButton, new GridBagConstraints(1, 0, 
            1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insetsDefault, 0, 0));

        // Uploads preferences pane.
        JPanel uploadPrefsPanel = new JPanel(new GridBagLayout());
        int row = 0;
        JHtmlLabel aclPrefsLabel = new JHtmlLabel(
            "ACL Permissions", hyperlinkListener);
        uploadPrefsPanel.add(aclPrefsLabel, new GridBagConstraints(0, row++, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));

        aclButtonGroup = new ButtonGroup();
        JRadioButton aclPrivateButton = new JRadioButton("Private", true);
        aclPrivateButton.setActionCommand(CockpitPreferences.UPLOAD_ACL_PERMISSION_PRIVATE);
        JRadioButton aclPublicReadButton = new JRadioButton("Public read"); 
        aclPublicReadButton.setActionCommand(CockpitPreferences.UPLOAD_ACL_PERMISSION_PUBLIC_READ);
        JRadioButton aclPublicReadWriteButton = new JRadioButton("Public read and write");
        aclPublicReadWriteButton.setActionCommand(CockpitPreferences.UPLOAD_ACL_PERMISSION_PUBLIC_READ_WRITE);
        aclButtonGroup.add(aclPrivateButton);
        aclButtonGroup.add(aclPublicReadButton);
        aclButtonGroup.add(aclPublicReadWriteButton);
        JPanel aclPrefsRadioPanel = new JPanel(new GridBagLayout());
        aclPrefsRadioPanel.add(aclPrivateButton, new GridBagConstraints(0, 0, 
            1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));
        aclPrefsRadioPanel.add(aclPublicReadButton, new GridBagConstraints(1, 0, 
            1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));
        aclPrefsRadioPanel.add(aclPublicReadWriteButton, new GridBagConstraints(2, 0, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));
        uploadPrefsPanel.add(aclPrefsRadioPanel, new GridBagConstraints(0, row++, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));
        
        JHtmlLabel compressionPrefsLabel = new JHtmlLabel(
            "Compress files with GZip?", hyperlinkListener);
        uploadPrefsPanel.add(compressionPrefsLabel, new GridBagConstraints(0, row++, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));

        compressButtonGroup = new ButtonGroup();
        JRadioButton compressNoButton = new JRadioButton("Don't compress", true);
        compressNoButton.setActionCommand("INACTIVE");
        JRadioButton compressYesButton = new JRadioButton("Compress");
        compressYesButton.setActionCommand("ACTIVE");
        compressButtonGroup.add(compressNoButton);
        compressButtonGroup.add(compressYesButton);
        JPanel compressPrefsRadioPanel = new JPanel(new GridBagLayout());
        compressPrefsRadioPanel.add(compressNoButton, new GridBagConstraints(0, 0, 
            1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));
        compressPrefsRadioPanel.add(compressYesButton, new GridBagConstraints(1, 0, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));
        uploadPrefsPanel.add(compressPrefsRadioPanel, new GridBagConstraints(0, row++, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));
        
        JHtmlLabel encryptionPrefsLabel = new JHtmlLabel(
            "<html>Encrypt files?<br><font size=\"-2\">If encryption is turned on you must " +
            "also set the Encryption password</html>", hyperlinkListener);
        uploadPrefsPanel.add(encryptionPrefsLabel, new GridBagConstraints(0, row++, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));
                
        encryptButtonGroup = new ButtonGroup();
        JRadioButton encryptNoButton = new JRadioButton("Don't encrypt", true);
        encryptNoButton.setActionCommand("INACTIVE");
        JRadioButton encryptYesButton = new JRadioButton("Encrypt");
        encryptYesButton.setActionCommand("ACTIVE");
        encryptButtonGroup.add(encryptNoButton);
        encryptButtonGroup.add(encryptYesButton);
        encryptPasswordField = new JPasswordField();
        JPanel encryptPrefsRadioPanel = new JPanel(new GridBagLayout());
        encryptPrefsRadioPanel.add(encryptNoButton, new GridBagConstraints(0, 0, 
            1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));
        encryptPrefsRadioPanel.add(encryptYesButton, new GridBagConstraints(1, 0, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));
        uploadPrefsPanel.add(encryptPrefsRadioPanel, new GridBagConstraints(0, row++, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));        
        
        String encryptAlgorithm = "Unknown";
        try {
            encryptAlgorithm = new EncryptionUtil("").getAlgorithm();
        } catch (Exception e) {
            String message = "Unable to determine default encryption algorithm";
            log.warn(message, e);            
        }
        
        JPanel encryptionPrefsPanel = new JPanel(new GridBagLayout());
        encryptionPrefsPanel.add(new JHtmlLabel("Password", hyperlinkListener), new GridBagConstraints(0, 0, 
            1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));
        encryptionPrefsPanel.add(encryptPasswordField, new GridBagConstraints(0, 1, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));
        encryptionPrefsPanel.add(new JHtmlLabel("Algorithm", hyperlinkListener), new GridBagConstraints(0, 2, 
            1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsDefault, 0, 0));        
        JHtmlLabel encryptAlgorithmLabel = new JHtmlLabel(encryptAlgorithm, hyperlinkListener);
        encryptAlgorithmLabel.setToolTipText("Set by property crypto.algorithm in jets3t.properties");
        encryptionPrefsPanel.add(encryptAlgorithmLabel, new GridBagConstraints(0, 3, 
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));
        // Padding
        encryptionPrefsPanel.add(new JLabel(), new GridBagConstraints(0, 4,  
            1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, insetsDefault, 0, 0));
        
        
        // Tabbed Pane.
        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(this);
        tabbedPane.add(uploadPrefsPanel, "Uploads");
        tabbedPane.add(encryptionPrefsPanel, "Encryption");
        
        row = 0;
        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(introductionLabel, new GridBagConstraints(0, row++, 
            2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));
        this.getContentPane().add(tabbedPane, new GridBagConstraints(0, row++, 
            2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insetsDefault, 0, 0));
        this.getContentPane().add(buttonsPanel, new GridBagConstraints(0, row++, 
            2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));
        
        this.pack();
        this.setLocationRelativeTo(this.getOwner());
    }

    /**
     * Event handler for this dialog.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(okButton)) {
            // Save preferences to CockpitPreferences object.
            cockpitPreferences.setUploadACLPermission(
                aclButtonGroup.getSelection().getActionCommand());            
            cockpitPreferences.setUploadCompressionActive(
                "ACTIVE".equals(compressButtonGroup.getSelection().getActionCommand()));
            cockpitPreferences.setUploadEncryptionActive(
                "ACTIVE".equals(encryptButtonGroup.getSelection().getActionCommand()));
            
            if (cockpitPreferences.isUploadEncryptionActive()
                && encryptPasswordField.getPassword().length == 0) 
            {
                ErrorDialog.showDialog(ownerFrame, hyperlinkListener, 
                    "If encryption is set for Uploads the Encryption password cannot be empty", null);
                return;
            }
            
            cockpitPreferences.setEncryptionPassword(
                new String(encryptPasswordField.getPassword()));
            
            this.hide();
        } else if (e.getSource().equals(cancelButton)) {
            this.hide();
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        // Ignore these events.
    }    
    
    /**
     * Displays the dialog box and waits until the user selects to cancel the dialog or to save
     * the properties.
     */
    public static void showDialog(CockpitPreferences cockpitPreferences, Frame owner, 
        HyperlinkActivatedListener hyperlinkListener)  
    {
        if (preferencesDialog == null) {
            preferencesDialog = new PreferencesDialog(cockpitPreferences, owner, hyperlinkListener);
        }        
        preferencesDialog.show();
    }


    /**
     * Creates stand-alone dialog box for testing only.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        JFrame f = new JFrame();

        HyperlinkActivatedListener listener = new HyperlinkActivatedListener() {
            public void followHyperlink(URL url, String target) {
                BareBonesBrowserLaunch.openURL(url.toString());
            }           
        };
        
        CockpitPreferences cockpitPreferences = new CockpitPreferences();
        
        PreferencesDialog.showDialog(cockpitPreferences, f, listener);
        
        preferencesDialog.dispose();
        f.dispose();
    }

}