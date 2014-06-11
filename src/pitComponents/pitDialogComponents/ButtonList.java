/*
 * ButtonList.java
 *
 * Created on February 20, 2005, 1:03 PM
 */

package pitComponents.pitDialogComponents;

import pitComponents.Cockpit;
import javax.swing.*;
import pitComponents.*;

/**
 *
 * @author mribeiro
 * Shows a list of buttons this cockpit has
 */
public class ButtonList extends JScrollPane{
    
    private JList l;
    
    /** Creates a new instance of ButtonList */
    public ButtonList() {
        reload();
    }
    
    public PitButton getSelectedButton(){
        int si = l.getSelectedIndex();
        
        if (si == -1){
            return null;
        }
        else {
            return (PitButton)Cockpit.buttons.elementAt(si);
        }
    }
    
    public void reload(){
        l = new JList(Cockpit.buttons.getObjects());
        l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setViewportView(l);
        repaint();
    }
}
