package pitComponents.pitDialogComponents;

import javax.swing.*;
import java.awt.event.*;

/** implements a checkbox with a label, extending the JPanel class. */
public class LabeledCBox extends JPanel implements ActionListener{
	private JCheckBox cb = new JCheckBox();
	ActionListener al;

	/** creates a checkbox with the given label. The tip will be the same as label. */
	public LabeledCBox(String label) {
		this(label, label);
	}

	/** creates the checkbox with the given label and tip. */
	public LabeledCBox(String label, String tip) {
		this.add(new JLabel(label));
		this.add(cb);
		this.setToolTipText(tip);
		cb.setToolTipText(tip);
	}

	/** returns if the checkbox is selected. */
	public boolean isSelected(){
		return cb.isSelected();
	}

	/** sets the state of the checkbox. */
	public void setSelected(boolean v){
		cb.setSelected(v);
	}

	public void addActionListener(ActionListener l){
		al = l;
		cb.addActionListener(this);
	}

	public void actionPerformed(ActionEvent a){
		if (a.getSource() == cb){
			ActionEvent b = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
			al.actionPerformed(b);
		}
	}

}