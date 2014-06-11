package windows;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import pitComponents.pitObjects.PitObject;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitObjects.*;
import pitComponents.pitDialogComponents.*;
import pitComponents.*;
import pitComponents.pitHelperComponents.PitComponentArray;

/** this window is called whenever we create a new object
 *  it has a drop down,
 *  and ok cancel button
 */
public class NewObjectDialog extends JDialog
  implements ActionListener//, ItemListener, ChangeListener
{
    private static final int ADI=0, BV=1, CHEV=2, DED=3, DIAL=4, DIG=5, HSI=6,
     IND=7, KB=8, LIGHT=9, MACHASI=10, MIRROR = 11, TEXT=12;
    private static String objTypes[] =
      {"adi", "buttonview", "chevron/lift line", "ded", "dial", "digits",
      "hsi", "indicator", "kneeboard", "light", "machasi", "mirror", "text"};

    private JFrame parent;
      
    //the combo box with object type
    private static JComboBox combo = new JComboBox(objTypes);

    //the jlabels
    private static JLabel statesLabel = new JLabel("states"),
      initStateLabel = new JLabel("initstate"),
      cycleLabel = new JLabel("Cyclebits"),
      //the src labels
      srcLabels[] = new JLabel[15],
      //the dest labels
      destLabels[] = new JLabel[15];

    //buttons
    private JButton nextBt = new JButton("Next"),
        cancelBt = new JButton("Cancel");

	public NewObjectDialog(JFrame parent){
		super(parent, "Add new object");

		//window stuff
		this.parent = (JFrame)parent;

		nextBt.addActionListener(this);
		cancelBt.addActionListener(this);

		this.setModal(true);
		this.setSize(380, 150);
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		Container c = this.getContentPane();
		BorderLayout bl = new BorderLayout();
		c.setLayout(bl);

		//the north label
		JLabel label = new JLabel("Select a new object type", JLabel.CENTER);
		c.add(label, BorderLayout.NORTH);

		//the combo box
		JPanel p = new JPanel();
		p.add(combo);
		c.add(p, BorderLayout.CENTER);

		//ok cancel in south area
		p = new JPanel();
		p.add(nextBt);
		p.add(cancelBt);
		c.add(p, BorderLayout.SOUTH);        
	}
    
    public void actionPerformed(ActionEvent a){
        PitComponentArray objs = Cockpit.objs;
        PitComponentArray bviews = Cockpit.bviews;
        PitPanel currentPanel = Cockpit.currentPanel;
        
        JButton b = (JButton)a.getSource();
        
        if (b == nextBt){
            int i = combo.getSelectedIndex();
            int id;
            
            switch (i){
                case ADI:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1)
                           return; //!!! will this ever happen?
                    PitADI adi = new PitADI(id);
                    // now we show the new objects properties
                    if (adi.showNew(this)){
                        objs.add(adi, adi.id); //the object is added to the main array
                        currentPanel.addObject(adi);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }
                }
                break;
                case BV:
                {
                    id = bviews.getFreeId(currentPanel.id);
                    if (id == -1)
                           return; //!!! will this ever happen?
                    PitButtonView bv = new PitButtonView(id);
                    //now we show the new objects properties
                    if (bv.showNew(this)){
                        bviews.add(bv, bv.id); //the object is added to the main array
                        currentPanel.addButtonView(bv);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }
                }
                break;
                case CHEV:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1){
						return; //!!! will this ever happen?
					}
                    PitChevLift cl = new PitChevLift(true, id);
                    //now we show the new objects properties
                    if (cl.showNew(this)){
                        objs.add(cl, cl.id); //the object is added to the main array
                        currentPanel.addObject(cl);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }
                }
                break;
                case DED:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1)
                           return; //!!! will this ever happen?
                    PitDED ded = new PitDED(id);
                    //now we show the new objects properties
                    if (ded.showNew(this)){
                        objs.add(ded, ded.id); //the object is added to the main array
                        currentPanel.addObject(ded);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }                    
                }
                break;
                case DIAL:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1)
                           return; //!!! will this ever happen?
                    PitDial dial = new PitDial(id);
                    //now we show the new objects properties
                    if (dial.showNew(this)){
                        objs.add(dial, dial.id); //the object is added to the main array
                        currentPanel.addObject(dial);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }                    
                }
                break;
                case DIG:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1)
                           return; //!!! will this ever happen?
                    PitDigits dig = new PitDigits(id);
                    //now we show the new objects properties
                    if (dig.showNew(this)){
                        objs.add(dig, dig.id); //the object is added to the main array
                        currentPanel.addObject(dig);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }                                        
                }
                break;
                case HSI:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1)
                           return; //!!! will this ever happen?
                    PitHSI hsi = new PitHSI(id);
                    //now we show the new objects properties
                    if (hsi.showNew(this)){
                        objs.add(hsi, hsi.id); //the object is added to the main array
                        currentPanel.addObject(hsi);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }                                        
                }
                break;
                case IND:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1){
						return; //!!! will this ever happen?
					}
                    PitIndicator ind = new PitIndicator(id);
                    //now we show the new objects properties
                    if (ind.showNew(this)){
                        objs.add(ind, ind.id); //the object is added to the main array
                        currentPanel.addObject(ind);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }                                        
                }
                break;
                case KB:
                {
                    id = objs.getFreeId(currentPanel.id);
					if (id == -1){
						return; //!!! will this ever happen?
					}
                    PitKneeboard kv = new PitKneeboard(id);
                    //now we show the new objects properties
                    if (kv.showNew(this)){
                        objs.add(kv, kv.id); //the object is added to the main array
                        currentPanel.addObject(kv);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }                                        
                }
                break;
                case LIGHT:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1){
					   return; //!!! will this ever happen?
					}
                    PitLight light = new PitLight(id);
                    //now we show the new objects properties
                    if (light.showNew(this)){
                        objs.add(light, light.id); //the object is added to the main array
                        currentPanel.addObject(light);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }                                        
                }
                break;
                case MACHASI:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1)
                           return; //!!! will this ever happen?
                    PitMachASI asi = new PitMachASI(id);
                    //now we show the new objects properties
                    if (asi.showNew(this)){
                        objs.add(asi, asi.id); //the object is added to the main array
                        currentPanel.addObject(asi);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }                                        
                }
                break;
				case MIRROR:
				{
					id = objs.getFreeId(currentPanel.id);
					if (id == -1){
						return; //!!! will this ever happen?
					}

					PitMirror m = new PitMirror(id);
					//now we show the new objects properties
					if (m.showNew(this)){
						objs.add(m, m.id); //the object is added to the main array
						currentPanel.addObject(m);
						//now we add to the panels array
						parent.repaint();
						dispose();
					}                                        					
				}
				break;
                case TEXT:
                {
                    id = objs.getFreeId(currentPanel.id);
                    if (id == -1)
                           return; //!!! will this ever happen?
                    PitText text = new PitText(id);
                    //now we show the new objects properties
                    if (text.showNew(this)){
                        objs.add(text, text.id); //the object is added to the main array
                        currentPanel.addObject(text);
                        //now we add to the panels array
                        parent.repaint();
                        dispose();
                    }                                        
                }
                break;                

            }
        }
        else if (b == cancelBt){
            this.dispose();
        }
    }
 }
