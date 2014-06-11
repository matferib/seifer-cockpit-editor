/*
 * ResolutionDialog.java
 *
 * Created on April 19, 2005, 12:18 PM
 */

package windows;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import pitComponents.pitDialogComponents.*;

/**
 * Some people use non regular resolution, like widescreens. I dont know if falcon
 * allows that, but my program will surely will. =)
 * @author mribeiro
 */
public class ResolutionDialog extends JDialog implements ActionListener{
    //the resolution fields
    private LabeledFloatFields resFields = 
        new LabeledFloatFields(this, "Resolution", 2, 7);
    
    /** the pre defined resolutions */
    private String preResLabels[] = {"800x600", "1024x768", "1280,960", "1600x1200"};
    /** the values of each button */
    private Dimension preResVals[] = {new Dimension(800, 600), new Dimension(1024, 768), 
        new Dimension(1280, 960), new Dimension(1600, 1200)};
    /** the radio buttons representing preResLabels resolutions */
    private JRadioButton preResBts[] = new JRadioButton[preResLabels.length];
    /** the buttongroup encompassing all radios */
    private ButtonGroup preResGroup = new ButtonGroup();

    //the buttons
    private JButton okBt = new JButton("ok"), cancelBt= new JButton("cancel");

    //the resolution
    private Dimension resolution = null;

    /** Creates a new instance of ResolutionDialog */
    public ResolutionDialog(JFrame parent) {
        super(parent);
        setProperties();
    }

    public ResolutionDialog(JDialog parent) {
        super(parent);
        setProperties();
    }

    /** Sets the window properties, such as buttons, size modal etc... */
    private void setProperties(){
        //the predefined res buttons
        for (int i=0;i<preResLabels.length;i++){
            preResBts[i] = new JRadioButton(preResLabels[i]);
            preResBts[i].addActionListener(this);
            preResGroup.add(preResBts[i]);
            
        }
        
        setSize(300, 180);
        this.setLocationRelativeTo(null);
        setTitle("Select Resolution(width x height)"); 
        setModal(true);
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());
                
        
        //center panel: pre def resolutions and text field
        JPanel cp = new JPanel(new BorderLayout());
        JPanel preResP = new JPanel();
        for (int i=0;i<preResBts.length;i++){
            preResP.add(preResBts[i]);
        }
        cp.add(preResP, BorderLayout.CENTER);
        cp.add(resFields, BorderLayout.SOUTH);
        c.add(cp, BorderLayout.CENTER);
        
        //south panel, buttons
        JPanel sp = new JPanel();
        sp.add(okBt); sp.add(cancelBt);
        c.add(sp, BorderLayout.SOUTH);
        
        okBt.addActionListener(this);
        cancelBt.addActionListener(this);
        okBt.setForeground(Color.green);
        cancelBt.setForeground(Color.red);
    }

    public void actionPerformed(ActionEvent ae){
        Object o = ae.getSource();

        if (o == okBt){
            int valid = resFields.getLength();
            if (valid != 2){
                JOptionPane.showMessageDialog(this, "Invalid resolution!");
                return;
            }
            int values[];
            try {
                values = resFields.getFieldsAsInteger();
            }
            catch (Exception e){
                JOptionPane.showMessageDialog(this, "Invalid resolution!");
                return;                
            }
            resolution = new Dimension(values[0], values[1]);
            this.setVisible(false);
        }
        else if (o == cancelBt){
            resolution = null;
            this.setVisible(false);
        }
        else {
            //check pre res buttons
            for (int i=0;i<preResBts.length;i++){
                if (o == preResBts[i]){
                    resFields.setFields(
                        new int[]
                            {preResVals[i].width, preResVals[i].height}
                    );
                    this.repaint();
                    break;
                }
            }
        }
    }

    public Dimension getResolution(){
        this.setVisible(true);
        return resolution;
    }
}
