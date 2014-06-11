package pitComponents.pitDialogComponents;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;

/** a jpanel containing a label and a field where only numbers can be input. */
public class LabeledNumField extends JPanel implements ActionListener {
	
	JLabel label;
	Window parent;
	JTextField value;
	JButton bt;
	ActionListener alistener;

	/**
	* Creates a labeled num field with the given label, labelsize and a button. A 
	* button will be created only if btext != null. LabeledNumfields are just like
	* a labeled field, except that it only accepts number as inputs.
	*
	*  @param parent the window parent of this dialog
	*  @param label the field label
	*  @param tip the tooltip of the component
	*  @param size the field size, for input
	*  @param btext the button text
	*/
	public LabeledNumField(Window parent, String label, String tip, int size, 
		String btext, String btip) {
		parent = parent;
		this.label = new JLabel(label);
		this.add(new JLabel(label));
		value = new JTextField(size);
		this.add(value);
		value.setToolTipText(tip + ", number format");
		if (btext != null){
			bt = new JButton(btext);
			this.add(bt);
			bt.addActionListener(this);
			bt.setToolTipText(btip);
		}
    }

	public LabeledNumField(Window parent, String label, String tip, int size, String btext){
		this(parent, label, tip, size, btext, btext);
	}


	public void addActionListener(ActionListener l){
		this.alistener = l;
	}

	public void actionPerformed(ActionEvent ae){
		if (alistener != null){
			ActionEvent nae = new ActionEvent(this, ae.ACTION_PERFORMED, ae.getActionCommand());
			alistener.actionPerformed(nae);
		}
	}

	public LabeledNumField(Window parent, String label, int size) {
		this(parent, label, label, size, null);
	}

	public LabeledNumField(Window parent, String label, String tip, int size) {
		this(parent, label, tip, size, null);
	}
	//end constructors

	/** gets the value in this field. An exception is raised if field is blank,
	* number format is invalid.
	* 
	* @throws if the field value is invalid(not a number or blank).
	* @return the integer value of the field
	*/
	public int getValue() throws Exception {
		try {
			String s = value.getText();
			//we throw if field is empty
			if (s.length() == 0){
				throw new Exception("Blank field") ;
			}
			return Integer.parseInt(s);
		}
		catch (Exception e){
			throw new Exception("Invalid number value: " + label.getText());
		}
	}

	/** indicates if the field is value or not. An invalid numField is either
	* blank or contains non number characters
	*
	* @return true if field is valid, false otherwise
	**/
	public boolean isFieldValid(){
		try {
			String s = value.getText();
			if (s.length() == 0){
				throw new Exception("Blank field") ;
			}
			return true;
		}
		catch (Exception e){
			return false;
		}        
	}

	public void setValue(int i){
		value.setText(Integer.toString(i));
	}

	public void setEnabled(boolean enabled){
		label.setEnabled(enabled);
		value.setEnabled(enabled);
	}


}