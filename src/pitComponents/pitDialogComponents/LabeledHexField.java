package pitComponents.pitDialogComponents;

import javax.swing.*;
import java.awt.*;

public class LabeledHexField extends JPanel {
    JTextField value;
    JLabel label;
    Window parent;

    public LabeledHexField(Window parent, String label, int size) {
        this(parent, label, label, size);
    }

    public LabeledHexField(Window parent, String label, String tip, int size) {
        this.parent = parent;
        this.label = new JLabel(label);
        this.add(new JLabel(label));
        value = new JTextField(size);
        this.add(value);
        value.setToolTipText(tip + ", in hex");
    }

    public long getValue() throws Exception{
        String s = value.getText();
        int indx = s.indexOf("x");
        try {
            if (indx == -1)
                return Long.parseLong(s, 16);
            else {
                return Long.parseLong(s.substring(indx + 1), 16);
            }
        }
        catch (Exception e){
            throw new Exception("Error getting hex field " + label.getText());
        }
    }

    public void setValue(long l){
        if (l > 0){
            value.setText("0x" + Long.toHexString(l));
		}
        else {
            value.setText(Long.toString(l));
		}
    }

}