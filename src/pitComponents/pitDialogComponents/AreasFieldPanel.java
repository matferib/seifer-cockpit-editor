package pitComponents.pitDialogComponents;

import pitComponents.pitHelperComponents.*;
import java.awt.*;
import javax.swing.*;

/** Represents an area in falcon format. An area consists of four values,
 * and some areas also have a opaque/transparent in front of it. Example:
 * transparent 0 0 50 50. One thing peculiar to falcon areas is that they are
 * inverted in x y, so an area 0 20 9 29 means a rectangle with upper left 
 * coordinates (20, 0) and down right (29, 9), which is a 10x10 rectangle.
 */
public class AreasFieldPanel extends JPanel {
    //parent
    Window parent;
    JLabel label;

    //radios
    private JRadioButton op = new JRadioButton(), tr = new JRadioButton();
    //the textfields
    private JTextField fields[] = new JTextField[4];
	private boolean hasOpTr; //< can this area have opaque/transp modifier

    private static String fieldTips[] = {
      "Up Left X", "Up Left Y", "Down Right X", "Down Right Y"};

    /** Creates a input representing an area. Areas are 4 values. The fields are
     * ordered in the normal way, not falcon, which means XY format.
     *
     * @param parent the parent window
     * @param label the field name
     * @param hasOpTr true if we want to show a opaque/transparent radio
     */
    public AreasFieldPanel(Window parent, String label, boolean hasOpTr) {
        this.parent = parent;
        setLayout(new FlowLayout(FlowLayout.CENTER));
		this.hasOpTr = hasOpTr;
		
        if (hasOpTr){
            //the radiobuttons
            ButtonGroup g = new ButtonGroup();
            op.setToolTipText("Opaque");
            tr.setToolTipText("Transparent");
			op.setSelected(true);
            g.add(op);
            g.add(tr);
            add(op);
            add(tr);
        }

        add(this.label = new JLabel(label));
        for (int j = 0; j < 4; j++) {
            fields[j] = new JTextField();
            fields[j].setColumns(4);
            fields[j].setToolTipText(fieldTips[j]);
            add(fields[j]);
        }        
    }      
    
    /** Calls constructor with hasOpTr set to true. See 
     * @link{FieldsPanel(Window, String, boolean)} for more information.
     *
     * @param parent the parent window
     * @param label the field name
     */
    public AreasFieldPanel(Window parent, String label) {
        this(parent, label, true);
    }

    public void setEnabled(boolean enabled){
        label.setEnabled(enabled);
        op.setEnabled(enabled);
        tr.setEnabled(enabled);
        for (int j = 0; j < 4; j++) {
            fields[j].setEnabled(enabled);
        }
    }


    public void setFields(PitArea a){
        if (a == null){
            return;    
        }
        else {
            fields[0].setText(Integer.toString(a.upl.x));
            fields[1].setText(Integer.toString(a.upl.y));
            fields[2].setText(Integer.toString(a.dor.x));
            fields[3].setText(Integer.toString(a.dor.y));
            
            if (a.transparent){
                tr.setSelected(true);
            }
            if (a.opaque){
                op.setSelected(true);
            }
        }
    }

    public PitArea getArea() throws Exception{
        //throws only if parse fails
        //if empty, returns null
        if (isEmpty()){
            return null;
		}
        
		Rectangle r = new Rectangle();
		r.x = getField(0);
		r.y = getField(1);
		int dorx = getField(2);
		int dory = getField(3);
		r.width = dorx - r.x + 1;
		r.height = dory - r.y + 1;
		PitArea a = new PitArea(r);
		a.transparent = tr.isSelected();
		a.opaque = op.isSelected();
		a.setCanHaveModifier(hasOpTr);
		return a;
    }

    private int getField(int j) throws Exception{
        if (fields[j].getText().length() == 0){
            throw new Exception("error getting field " + j + " from " + label.getText());
		}
        return Integer.parseInt(fields[j].getText());
    }
    
    //returns true if all fields are empty, wow nice code =)
    private boolean isEmpty(){
		boolean allFilled = true;
        for (int i=0;i<4;i++){
			if (fields[i].getText().length() > 0){
				return false;
			}
		}
        return true;
    }

}
