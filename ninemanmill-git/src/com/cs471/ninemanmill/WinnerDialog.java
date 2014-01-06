package com.cs471.ninemanmill;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * This class is called when a player has lost/won the game.
 *  
 * Using template from previous project: Mass Effect 3 Coalesced Mod Manager by Michael Perez
 * @author Mike Perez, Sasa Rkman
 *
 */
@SuppressWarnings("serial")
public class WinnerDialog extends JDialog {

		/**
		 * Sets up all of the content panels contained
		 * in the win dialog box.
		 * @param jframe The frame that this dialog should show up on
		 * @param team The player reference 
		 */
		public WinnerDialog(JFrame jframe,int team, String reason) {
			System.gc();
			System.out.println("Garbage collecting.");
			this.setTitle("Player "+team+" wins!");
			this.setResizable(false);
			//this.setPreferredSize(new Dimension(250, 120));
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.setBackground(new Color(108,75,31));
			//mainPanel.setPreferredSize(new Dimension(215,97));

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

			
			//text
			JPanel middlePanel = new JPanel(new BorderLayout());
			middlePanel.setOpaque(false);
			JLabel winnerLabel = new JLabel("Player "+team+" wins!",null,JLabel.CENTER);
			winnerLabel.setFont(winnerLabel.getFont ().deriveFont (28.0f));
			winnerLabel.setForeground(Color.WHITE);
			JLabel reasonLabel = new JLabel(reason, SwingConstants.CENTER);
			reasonLabel.setForeground(Color.WHITE);
			//reasonLabel.set
			middlePanel.add(winnerLabel,BorderLayout.NORTH);
			middlePanel.add(reasonLabel,BorderLayout.CENTER);
			
			//button
			JButton dismiss = new JButton("OK");
			dismiss.setOpaque(true);
			if (System.getProperty("os.name").contains("Mac")){
				//it's running on a mac
				dismiss.setBackground(new Color(108,75,31));
			} else {
				//Other OS are fine
				dismiss.setBackground(new Color(255,146,0));
			}
			dismiss.setFocusPainted(false);
			dismiss.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					WinnerDialog.this.dispose();
				}
			});
			
			
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
			bottomPanel.setBackground(new Color(0,0,0,0));
			bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

			//align the button
			bottomPanel.add(Box.createHorizontalGlue());
			bottomPanel.add(dismiss);
			bottomPanel.add(Box.createHorizontalGlue());

			middlePanel.add(bottomPanel,BorderLayout.SOUTH);
			mainPanel.add(middlePanel,BorderLayout.CENTER);
			
			add(mainPanel);
			
			//setup other stuff
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			//this.setResizable(false);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			ArrayList<Image> icons = new ArrayList<Image>(3);
			try {
				icons.add(new ImageIcon(ImageIO.read(getClass().getResource("resources/icon32.png"))).getImage());
				icons.add(new ImageIcon(ImageIO.read(getClass().getResource("resources/icon16.png"))).getImage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			setIconImages(icons);
			this.pack();
			this.setLocationRelativeTo(jframe);
			this.setVisible(true);
			
			//System.out.println("Winner panel: "+mainPanel.getWidth()+","+mainPanel.getHeight());
		}
	}

