package pitComponents.pitDialogComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Graphically, this class is a panel with a label, a text field and optionally,
 * a button. You can also choose tips for the field.
 */
public class LabeledField extends JPanel implements ActionListener {
	private Window parent;
	private JButton button; //optional
	private JTextField field;
	ActionListener alistener;
	JLabel label;

	/** Builds a panel with the given label, a field and a tip. The tip is the
	* same as the label.
	*
	* @param parent parent window
	* @label the field label. Usually its something meaningfull like masktop. 
	* Its also the tip at the field.
	* @size the field size, in characters
	*/
	public LabeledField(Window parent, String label, int size) {
		this(parent, label, label, size);
	}
	
	/** Builds a panel with the given label, a field and a tip. The size of the field
	* is given in characters length.
	*
	* @param parent parent window
	* @label the field label. Usually its something meaningfull like masktop
	* @size the field size, in characters
	*/
	public LabeledField(Window parent, String label, String tip, int size) {
		this.parent = parent;
		this.setToolTipText(tip);
		this.add(this.label = new JLabel(label));
		this.add(field = new JTextField(size));
	}

	/** Builds a panel with the given label, a field(with tip), and a button.
	*  The size of the field is given in characters length. 
	*
	* @param parent parent window
	* @label the field label. Usually its something meaningfull like masktop
	* @size the field size, in characters
	*/
	public LabeledField(Window parent, String label, String tip, int size, String blb) {
		this.parent = parent;
		this.setToolTipText(tip);
		this.add(this.label = new JLabel(label));
		this.add(field = new JTextField(size));

		button = new JButton(blb);
		button.addActionListener(this);
		this.add(button);
	}

	/** gets the content of the field in string format. */
	public String getText(){
		return field.getText();
	}

	/** sets the contents of the field */
	public void setText(String text){
		field.setText(text);
	}

	public void setEnabled(boolean enabled){
		label.setEnabled(enabled);
		field.setEnabled(enabled);
	}

	public void addActionListener(ActionListener alistener){
		this.alistener = alistener;
	}

	public void actionPerformed(ActionEvent ae){
		if (ae.getSource() == button){
			//button has been pressed
			ActionEvent ae2 = new ActionEvent(this, 
				   ActionEvent.ACTION_PERFORMED, "Button pressed");
			alistener.actionPerformed(ae2);
		}
	}

}