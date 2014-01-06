package com.cs471.ninemanmill;


/**
 * New Game dialog. Comes up when picking a new game.
 * If it is closed via the X button, the UI does not continue forming a new game, instead continuing from where it used to be.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class NewGameDialog extends JDialog {
    //private boolean answer = false;
    private JButton OKButton;
    private int player1 = -1;
    private int player2 = -1;
    private boolean flyMode = true;
    //Internally AI - Extreme is known as impossible. Since you can still beat it with some cheap tactics it was renamed to extreme.
    private String[] players = { "Human", "AI - Easy", "AI - Moderate", "AI - Hard", "AI - Extreme" };
    private JComboBox<String> player1List, player2List;
    //public boolean getAnswer() { return answer; }

    /**
     * Creates a new game dialog box. Allows the player to select an opponent, and select "start game"
     * @param parent The UINew object that will call this
     */
    public NewGameDialog(UINew parent) {
        super(parent, true);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setResizable(false);
        setTitle("New Game");
        JPanel mainPanel = new JPanel(new BorderLayout()); //this would be replaced with the new drawing panel - perhaps make it take a ref to an image
		mainPanel.setBackground(new Color(108,75,31));
		
		
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
		middlePanel.setOpaque(false);
		
        JPanel vsPanel = new JPanel(new BorderLayout());
        vsPanel.setOpaque(false);
        
        JLabel opponentLabel = new JLabel("Select players:", SwingConstants.CENTER);
        opponentLabel.setForeground(Color.WHITE);
        vsPanel.add(opponentLabel,BorderLayout.NORTH);
        
        
        JPanel teamsPanel = new JPanel();
        teamsPanel.setOpaque(false);
        
        player1List = new JComboBox<String>(players);
        player2List = new JComboBox<String>(players);
        teamsPanel.add(player1List);
        JLabel vsLabel = new JLabel("vs");
        vsLabel.setForeground(Color.WHITE);
        teamsPanel.add(vsLabel);
        teamsPanel.add(player2List);
        
        vsPanel.add(teamsPanel,BorderLayout.CENTER);
        middlePanel.add(vsPanel,BorderLayout.NORTH);

        JPanel flyPanel = new JPanel();
        flyPanel.setOpaque(false);
        flyPanel.setLayout(new BoxLayout(flyPanel, BoxLayout.LINE_AXIS));
        flyPanel.add(Box.createHorizontalGlue());
        final JCheckBox flyBox = new JCheckBox("Allow Flying");
        flyBox.setForeground(Color.WHITE);
        flyBox.setSelected(true);
        flyBox.setOpaque(false);
        flyPanel.add(flyBox);
        flyPanel.add(Box.createHorizontalGlue());
        middlePanel.add(flyPanel,BorderLayout.CENTER);

        
        OKButton = new JButton("Start Game");
		if (System.getProperty("os.name").contains("Mac")){
			//it's running on a mac
			OKButton.setBackground(new Color(108,75,31));
		} else {
			//Other OS are fine
			OKButton.setBackground(new Color(255,146,0));
		}
        OKButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				player1 = player1List.getSelectedIndex();
				player2 = player2List.getSelectedIndex();
				flyMode = flyBox.isSelected();
				dispose();
			}
        	
        });
        
        
        JPanel OKPanel = new JPanel();
        OKPanel.setOpaque(false);
        OKPanel.setLayout(new BoxLayout(OKPanel,BoxLayout.LINE_AXIS));

        //align the button
		OKPanel.add(Box.createHorizontalGlue());
		OKPanel.add(OKButton);
		OKPanel.add(Box.createHorizontalGlue());
		middlePanel.add(OKPanel,BorderLayout.SOUTH);
		mainPanel.add(middlePanel,BorderLayout.CENTER);
        
        
        
        getContentPane().add(mainPanel);
        OKButton.requestFocusInWindow();
        pack();
        setLocationRelativeTo(parent);
        
        setVisible(true);
		//System.out.println("New game panel: "+getContentPane().getWidth()+","+getContentPane().getHeight());
    }
    
    /**
     * Gets the selected opponent from this box. As it is a modal box, the item that creates this box will retrieve it once the box is closed.
     * @return new bundle with opponent/flymode setting
     */
    protected NewGameBundle getGameSetup(){
    	return new NewGameBundle(player1,player2,flyMode);
    }

    /**
     * Gets the opponent's name from the given id.
     * @param opponentId difficulty ID of opponent
     * @return Human readable string (from the list of selectable players)
     */
	public String getOpponentString(int opponentId) {
		return players[opponentId];
	}  
}