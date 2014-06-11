/*
 * LabeledFloatFields.java
 *
 * Created on April 10, 2005, 5:33 PM
 */

package pitComponents.pitDialogComponents;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/** Represents a panel with fields for input of many values. Values can be retrieved
* as integers, or as floating points. Not all values have to be retrieved at once.
* There is a function to check how many fields hold valid data
*/
public class LabeledFloatFields extends JPanel {

	/** the window parent of this component */
	private Window parent;
	/** label will be shown at the left of the fields */
	private JLabel label;
	/** this is the fields array, holding the values */
	private JTextField floatFields[];

	/** Builds the float fields in the given parent, with a label, and number of
	* fields determined by the caller. Each field is of size fsize
	* @param parent the window holding this
	* @param label will be show left of the fields
	* @param numFields number of fields this panel has
	* @param fsize the size of each field
	*/
	public LabeledFloatFields(Window parent, String label, int numFields, int fsize) {
		super();
		//this.parent = parent;

		this.label = new JLabel(label);
		add(this.label);

		floatFields = new JTextField[numFields];
		for (int i=0;i<numFields;i++){
			floatFields[i] = new JTextField(fsize);
			add(floatFields[i]);
			floatFields[i].setToolTipText(label +" "+i);
		}
		this.setEnabled(true);
		this.setVisible(true);
	}

	/** Builds the float fields in the given parent, with a label, and number of
	* fields determined by the caller. Each field is of size fsize. Allows the caller
	* to specify the tooltip at the fields
	*
	* @param parent the window holding this
	* @param label will be show left of the fields
	* @param numFields number of fields this panel has
	* @param fsize the size of each field
	*/
	public LabeledFloatFields(Window parent, String label, String tips[], int numFields, int fsize) {
		super();
		//this.parent = parent;

		this.label = new JLabel(label);
		add(this.label);

		floatFields = new JTextField[numFields];
		for (int i=0;i<numFields;i++){
			floatFields[i] = new JTextField(fsize);
			add(floatFields[i]);
			if (i < tips.length){
				floatFields[i].setToolTipText(tips[i]);
			}
			else{
				floatFields[i].setToolTipText(label + " " + i);
			}
		}
		this.setEnabled(true);
		this.setVisible(true);
	}
	/** gets a field from the fields.
	  @param which which field to get, 0 is the first one
	  @throws if field is invalid or the data in it is
	  @return a float with the value of the field
	 */
	public float getField(int which) throws Exception {
		try {
			return Float.parseFloat(floatFields[which].getText());
		}
		catch (Exception e){
			throw new Exception("Error getting float field " + which + ","+ label.getText());
		}
	}

	/** Returns the number of valid fields(interface fields contaning number that
	 * are not empty)
	 * @return the number of valid fields
	 */
	public int getLength(){
		int ret;
		String s;
		for (ret=0;(ret<floatFields.length);ret++){
			s = floatFields[ret].getText();
			try{
				Float.parseFloat(s);
			}
			catch (Exception e){
				return ret;
			}

			if ((s.trim().equals(""))){
				return ret;
			}
		}
		return ret;
	}

	/**
	 * Gets a number of fields equal to the argument. Call this to get the first
	 * fields only, as an array of floats.
	 *
	 * @param numFields the number of fields desired
	 * @return a float array, of numFields size
	 */
	public float[] getFields(int numFields) throws Exception{
		float[] out = new float[numFields];

		try {
			for (int i = 0; i < numFields; i++) {
				out[i] = Float.parseFloat(floatFields[i].getText());
			}
			return out;
		}
		catch (Exception e){
			throw new Exception("Error getting float field " + label);
		}
	}

	/**
	 * Gets the fields as an array of floats.
	 *
	 * @return a float array, with all fields
	 */
	public float[] getFields() throws Exception{
		float[] out = new float[floatFields.length];
		try {
			for (int i = 0; i < floatFields.length; i++) {
				out[i] = Float.parseFloat(floatFields[i].getText());
			}
			return out;
		}
		catch (Exception e){
			throw new Exception("Error getting float field " + label.getText());
		}
	}

	/**
	 * Gets the fields as an array of integers instead of floats
	 *
	 * @return a float array, with all fields
	 */
	public int[] getFieldsAsInteger() throws Exception{
		int[] out = new int[floatFields.length];
		try {
			for (int i = 0; i < floatFields.length; i++) {
				out[i] = (int)Float.parseFloat(floatFields[i].getText());
			}
			return out;
		}
		catch (Exception e){
			throw new Exception("Error getting int field " + label.getText());
		}
	}


	/** Sets the field values with the array of floats, passed as arguments
	 *
	 * @param values the array of values
	 */
	public void setFields(float []values){
		int l = values.length;
		if (l> floatFields.length){
			JOptionPane.showMessageDialog(
			parent, "Error setting float field " + label.getText(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		for (int i=0;i<l;i++){
			floatFields[i].setText(Float.toString(values[i]));
		}
	}

	/** Sets the field values with the array of integers, passed as arguments
	 *
	 * @param values the array of values
	 */
	public void setFields(int []values){
		int l = values.length;
		if (l> floatFields.length){
			JOptionPane.showMessageDialog(
			parent, "Error setting float field " + label.getText(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		for (int i=0;i<l;i++){
			floatFields[i].setText(Integer.toString(values[i]));
		}
	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		for (int i=0;i<floatFields.length;i++){
			floatFields[i].setEnabled(enabled);
		}
	}
}
