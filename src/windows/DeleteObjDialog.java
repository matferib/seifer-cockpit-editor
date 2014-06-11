/*
 * ExtendedJOP.java
 *
 * Created on April 1, 2005, 10:01 PM
 */

package windows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author mribeiro
 * Just like an option pane, but creates a dialog for deleting objects... I needed
 * this because of check box with delete from panel as well...
 */
public class DeleteObjDialog extends JDialog implements ActionListener {
    private JCheckBox cb = new JCheckBox("Delete from .dat as well", true);
    private JButton ok = new JButton("Confirm"),
                    cancel = new JButton("Cancel");
    private int res = CANCEL;

    /** cancel option */
    static final public int CANCEL = 0;
    /** del from panel and dat */
    static final public int DELALL = 1;
    /** del from panel only */
    static final public int DELPAN = 2;
    
    /** Creates a new instance of ExtendedJOP */
    public DeleteObjDialog(JFrame parent) {
        super(parent);
        this.setModal(true);
        this.setTitle("Delete");
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());

        JLabel label = new JLabel("Confirm delete?", JLabel.CENTER);
        c.add(label, BorderLayout.NORTH);
        
        c.add(cb, BorderLayout.CENTER);

        JPanel p = new JPanel();
        p.add(ok); p.add(cancel);
        ok.setForeground(Color.green);
        cancel.setForeground(Color.red);
        c.add(p, BorderLayout.SOUTH);
        
        this.setSize(400, 200);
    }

    static int showDeleteObjDialog(JFrame parent){
        DeleteObjDialog d = new DeleteObjDialog(parent);
        d.setVisible(true);
        return d.res;
    }

    public void actionPerformed(ActionEvent ae){
        Object o  = (JButton)ae.getSource();
        if (o == ok){
            res = (cb.isSelected())?DELALL:DELPAN;
            this.setVisible(false);
        }
        else if (o == cancel){
            res = CANCEL;
            this.setVisible(false);
        }
    }
}
