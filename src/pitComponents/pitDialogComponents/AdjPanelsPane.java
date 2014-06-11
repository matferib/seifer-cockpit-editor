/*
 * AdjPanelsPane.java
 *
 * Created on 19 de Janeiro de 2005, 12:39
 */

package pitComponents.pitDialogComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import pitComponents.*;


/**
 * Graphical representation of the adjacent panels of a single panel. Used by
 * the panel property dialog. Basically, its like a #, with the middle square
 * empty. All other squares are filled with a number, representing the adjacent
 * panel. Also have the option to go to that panel.
 *
 * @author  matheus
 */
public class AdjPanelsPane extends JPanel
implements ActionListener {

    ActionListener al; 
    private int[] adjPanels;  //8 positions
    private JButton[] gotoBts = new JButton[8], eraseBts = new JButton[8];
    private JTextField adjFields[] = new JTextField[9];
    private Window parent;
    
    private static String gotoTip = "Go to this panel", 
        eraseTip = "Empty this slot";
    
    private static int N=0, NE=1, E=2, SE=3, S=4, SW=5, W=6, NW=7;

    /* ok this map is used as follows:
     * the adjpanels starts(position 0) at north position. But the gfx representation
     * is different, as it starts from NW. So this map translates a gfx position
     * into the adj position
     */
    private static int map[] = {NW, N, NE, W, E, SW, S, SE};
    
    private static String positionLabels[] = {
        "NORTHWEST", "NORTH", "NORTHEAST",
        "WEST", "EAST", 
        "SOUTHWEST", "SOUTH", "SOUTHEAST"};
    
    /** Creates a new instance of AdjPanelsPane */
    public AdjPanelsPane(Window parent) {
       setLayout(new GridLayout(3, 3));
       this.parent = parent;
        
        for (int i=0;i<8;i++){
            //int mp = map[i]; //mapped position
            
            if (i == 4){
                //fill center place
                this.add(new JPanel());
            }

            //slot panel
            JPanel p = new JPanel(new BorderLayout());
            
            //the label
            p.add(new JLabel(positionLabels[i], JLabel.CENTER), BorderLayout.NORTH);
            
            gotoBts[i] = new JButton("Goto");
            gotoBts[i].setBackground(Color.BLUE);
            gotoBts[i].setToolTipText(gotoTip);
            eraseBts[i] = new JButton("Empty");
            eraseBts[i].setBackground(Color.RED);
            eraseBts[i].setToolTipText(eraseTip);
            
            eraseBts[i].addActionListener(this);
            gotoBts[i].addActionListener(this);
            
            adjFields[i] = new JTextField(6);
            JPanel ap = new JPanel();
            ap.add(adjFields[i]);
            ap.add(gotoBts[i]);
            ap.add(eraseBts[i]);
            
            p.add(ap, BorderLayout.CENTER);
            
            this.add(p);
        }
    }
    
    public void setAdjPanels(int[] adjPanels){
        if (adjPanels.length != 8){
            //fill all textfields with -1
            for (int i=0; i<8; i++){
                adjFields[i].setText(Integer.toString(-1));
            }
        }
        else {
            //fill all textfields
            for (int i=0; i<8; i++){
                int mp = map[i];
                adjFields[i].setText(Integer.toString(adjPanels[mp]));
            }
        }        
    }
    
    public int[] getAdjPanels() throws Exception {
        adjPanels = new int[8];
        
        for (int i=0;i<8;i++){
            int mp = map[i];
            try {
                adjPanels[mp] = Integer.parseInt(adjFields[i].getText());
            }
            catch (Exception e){
                /*JOptionPane.showMessageDialog(
                    parent, 
                    "Invalid adjacent panel field", "Error", 
                    JOptionPane.ERROR_MESSAGE);
                throw e;*/
                adjPanels[mp] = -1;

            }
            
        }
        return adjPanels;
    }

    public void addActionListener(ActionListener al){
        //we add the action listener to all goto buttons
        this.al = al;
    }
    
    public void actionPerformed(ActionEvent ae){
        //check empty buttons
        for (int i=0;i<8;i++){
            if (ae.getSource() == eraseBts[i]){
                adjFields[i].setText("-1");
            }
            else if ((ae.getSource() == gotoBts[i]) && (al != null)){
                ActionEvent ae2 = new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED, adjFields[i].getText());
                al.actionPerformed(ae2);
            }
        }
    }
}
