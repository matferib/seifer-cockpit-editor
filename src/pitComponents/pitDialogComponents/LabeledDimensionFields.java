package pitComponents.pitDialogComponents;
import javax.swing.*;
import java.awt.*;

/** a panel containing a label and 2 fields for dimension (xy). */
public class LabeledDimensionFields extends JPanel{

    JTextField fields[] = new JTextField[2];
    JLabel label;

    public LabeledDimensionFields(Window parent, String label){
        this(parent, label, label);
    }

    public LabeledDimensionFields(Window parent, String label, String tip) {
        fields[0] = new JTextField(6); //width
        fields[0].setToolTipText("width");
        fields[1] = new JTextField(6); //height
        fields[1].setToolTipText("height");

        add(this.label = new JLabel(label));
        add(fields[0]);
        add(fields[1]);
    }

    public Dimension getFields() throws Exception {
        try {
            int x = Integer.parseInt(fields[0].getText());
            int y = Integer.parseInt(fields[1].getText());
            return new Dimension(x, y);
        }
        catch (Exception e){
            throw new Exception("Invalid values for dimension" + label.getText()); 
        }
    }

    public void setFields(Dimension d){
        fields[0].setText(Integer.toString(d.width));
        fields[1].setText(Integer.toString(d.height));
    }

    public void setEnabled(boolean enabled){
        label.setEnabled(enabled);
        fields[0].setEnabled(enabled);
        fields[1].setEnabled(enabled);
    }

}