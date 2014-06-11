package pitComponents.pitDialogComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import pitComponents.*;

public class ColorField extends JPanel implements ActionListener{

    private PitColor color; //the color of this field, we have no color till we get data
    private JTextField value;
    private JButton colorb, updateb;

    //constructors
    public ColorField(String label, String tip){
        add(new JLabel(label + ":")); //label

        value = new JTextField(8); //the text field(falcon format)
        add(value);
        value.setToolTipText("Falcon color format for " + tip);

        updateb = new JButton("proof");
        updateb.addActionListener(this);
        updateb.setToolTipText("see the color");
        add(updateb);

        colorb = new JButton(""); //the color button
        colorb.addActionListener(this);
        colorb.setToolTipText("Click to choose a color");
        add(colorb);

    }
    
    public ColorField(String label){
        this(label, label);
    }
    
    public ColorField(int labelID) {
        this("color"+ labelID+":");
    }
    //end contructor

    //outside call this
    public void setColor(PitColor c){
        if (c == null){
            color = null;
            return;
        }
        color = c;
        value.setText(c.toString());
        colorb.setBackground(c.toColor());
    }

    //we use this internally
    private void setColor(Color c){
        if (c == null){
            color = null;
            return;
        }
        color = new PitColor(c);
        value.setText(color.toString());
        colorb.setBackground(c);
    }

    /** Returns the color of this field. If color is not defined, return null */
    public PitColor getColor(){
        return color;
    }

    //choose color button
    public void actionPerformed(ActionEvent e){
        JButton b = (JButton)e.getSource();
        if (b == colorb){
            Color c = null;
            if (color != null){
                c = JColorChooser.showDialog(this, "Choose Color", color.toColor());
			}
            else {
                c = JColorChooser.showDialog(this, "Choose Color", Color.BLACK);
			}
			
            if (c != null){
                setColor(c);
            }
        }
        else if (b == updateb){
            if (color == null){
                color = new PitColor(value.getText());
            }
            else{
                color.setAll(value.getText());
            }
            setColor(color);
        }
    }
}