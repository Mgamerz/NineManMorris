package com.cs471.ninemanmill;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

/**
 * This class is called when the user selections Options/Preferences from the context menu of the program and allows them to;
 *  - Turn music on/off
 *  - Turn SFX on/off
 *  
 *  It uses the ini4j library to store data in a simple format.
 *  
 *  Using template from previous project: Mass Effect 3 Coalesced Mod Manager by Michael Perez
 * @author Mike Perez, Sasa Rkman
 *
 */
@SuppressWarnings("serial")
public class PreferencesDialog extends JDialog {
	UINew parent;
	final JCheckBox cbPlayMusic = new JCheckBox("Play Background Music");
	final JCheckBox cbPlaySFX = new JCheckBox("Play Sound Effects");
	Wini ini; //Ini handler
	
	Image background;
	
	/**
	 * Sets up all the content panels to be placed
	 * inside the preferences dialog box
	 * @param parent The reference to the parent frame
	 */
	public PreferencesDialog(UINew parent) {
		this.parent = parent;
		this.setTitle("Preferences");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		//this.setPreferredSize(new Dimension(250, 120));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.pack();
		this.setLocationRelativeTo(parent);
		
		try {
			File settings = new File(NineManMill.settingsfile);
			if (!settings.exists()) {
				//If settings file does not exist, make one
				settings.createNewFile();
			}
			//load settings file
			ini = new Wini(settings);
		} catch (InvalidFileFormatException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			//maybe delete settings here
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//add closing adapter - called when window dies
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
            	try {
					ini.store();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
		
		this.setVisible(true);

		}

	
		/**
		 * Configures the dialog box and sets up the interaction.
		 */
		private void setupWindow() {
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.setBackground(new Color(108,75,31));
			mainPanel.setMaximumSize(new Dimension(250, 120));
			mainPanel.setMinimumSize(new Dimension(250, 120));
			
			//spacing
			JPanel upperPanel = new JPanel();
			JPanel leftPanel = new JPanel();
			JPanel rightPanel = new JPanel();
			JPanel lowerPanel = new JPanel();
			
			upperPanel.setOpaque(false);
			leftPanel.setOpaque(false);
			rightPanel.setOpaque(false);
			lowerPanel.setOpaque(false);
			
			upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.PAGE_AXIS));
			lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.PAGE_AXIS));
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
			rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
			upperPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			lowerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			leftPanel.add(Box.createRigidArea(new Dimension(10, 0)));
			rightPanel.add(Box.createRigidArea(new Dimension(10, 0)));

			mainPanel.add(upperPanel,BorderLayout.NORTH);
			mainPanel.add(lowerPanel,BorderLayout.SOUTH);
			mainPanel.add(leftPanel,BorderLayout.WEST);
			mainPanel.add(rightPanel,BorderLayout.EAST);
			
			JPanel middlePanel = new JPanel(new BorderLayout());
			
			JPanel soundPanel = new JPanel(new BorderLayout());
			//final JCheckBox cbPlayMusic = new JCheckBox("Play Background Music");
			cbPlayMusic.setSelected(NineManMill.PLAY_MUSIC);
			cbPlayMusic.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					//Checkbox was changed: Save changes to wini
					try{
						ini.put("Settings", "playmusic", cbPlayMusic.isSelected() ? "1" : "0");
						NineManMill.PLAY_MUSIC = cbPlayMusic.isSelected();
						if (parent!=null && parent.gamestate != null){
							if (NineManMill.PLAY_MUSIC){
								parent.gamestate.playBackgroundMusic();
							} else{
								//turn it off
								parent.gamestate.stopBackgroundMusic();
							}
						}
						ini.store();
						//if unchecked, stop music playing
						//TODO
						
					} catch (IOException e1) {
						System.err.println("ERROR: Error with I/O when operating on settings file. Settings may not save.");
						e1.printStackTrace();
					}
				}
			});
			
			//final JCheckBox cbPlaySFX = new JCheckBox("Play Sound Effects");
			cbPlaySFX.setSelected(NineManMill.PLAY_SFX);
			cbPlaySFX.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					//Checkbox was changed: Save changes to wini
					try{
						ini.put("Settings", "playsfx", cbPlaySFX.isSelected() ? "1" : "0");
						NineManMill.PLAY_SFX = cbPlaySFX.isSelected();
						ini.store();
					} catch (IOException e1) {
						System.err.println("ERROR: Error with I/O when operating on settings file. Settings may not save.");
						e1.printStackTrace();
					}
				}
			});
			
			// Sasa Stuff
			cbPlaySFX.setBackground(new Color(108,75,31));
			cbPlaySFX.setFocusPainted(false);
			cbPlaySFX.setForeground(Color.WHITE);
			cbPlayMusic.setBackground(new Color(108,75,31));
			cbPlayMusic.setFocusPainted(false);
			cbPlayMusic.setForeground(Color.WHITE);
			soundPanel.setBackground(new Color(108,75,31));
			// End Sasa
			
			soundPanel.add(cbPlaySFX, BorderLayout.NORTH);
			soundPanel.add(cbPlayMusic, BorderLayout.SOUTH);
			middlePanel.add(soundPanel,BorderLayout.CENTER);
			middlePanel.setOpaque(false);
			

			JPanel OKPanel = new JPanel();
			OKPanel.setLayout(new BoxLayout(OKPanel, BoxLayout.LINE_AXIS));

			//align the button
			//button
			JButton dismiss = new JButton("OK");
			dismiss.setOpaque(true);
			if (System.getProperty("os.name").contains("Mac")){
				//it's running on a mac
				dismiss.setBackground(new Color(108,75,31));
			} else {
				//other OS are fine
				dismiss.setBackground(new Color(255,146,0));
			}
			dismiss.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					PreferencesDialog.this.dispose();
				}
				
			});
			
			OKPanel.setBackground(new Color(0,0,0,0));
			
			OKPanel.add(Box.createHorizontalGlue());
			OKPanel.add(dismiss);
			OKPanel.add(Box.createHorizontalGlue());
			
			middlePanel.add(OKPanel, BorderLayout.SOUTH);
			mainPanel.add(middlePanel,BorderLayout.CENTER);
	
			getContentPane().add(mainPanel);
		}
	}

