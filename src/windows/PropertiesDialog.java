/*
* PropertiesDialog.java
*
* Created on November 19, 2004, 1:10 PM
*/

package windows;

import javax.swing.*;
import pitComponents.*;
import pitComponents.pitObjects.*;
import java.awt.event.*;
import java.awt.*;

/**
* Base of all properties dialogs.
* By default, has an ok and a cancel button, which have the dialog as the listener.
* If the dialog is used for a new object, instead of update close, it has create/ cancel buttons.
* Can be used without a pitObject.
*/
abstract public class PropertiesDialog extends JDialog implements ActionListener {
	protected Window parent; //parent dialog
	protected boolean isNew = false; //true when creating a new object
	private PitObject po;
	/** indicates all fields are ok and object can be created. */
	private boolean fieldsOk = false;

	/** These are the buttons on the bottom */
	protected JButton updateBt = new JButton("Update"),
		closeBt = new JButton("Close"),
		createBt = new JButton("Create"),
		cancelBt = new JButton("cancel"),
		nextStBt = new JButton(">>"),
		prevStBt = new JButton("<<"),
		resetStBt = new JButton("Reset");
    
	/** south panel, contaning buttons */
	protected JPanel southPanel = new JPanel();
      
	/** Creates a new instance of PropertiesDialog. Used when the owner is a
	*frame, not a dialog.
	*
	* @param   parent  this dialog owner
	* @param   po      the pit object this dialog represents (null if none).
	* @param   title   this dialog title
	* @param   isNew   true if we are creating a new pitObject
	*/
	protected PropertiesDialog(JFrame parent, PitObject po, String title, boolean isNew) {
		super(parent, title);
		setProperties(parent, po, isNew);
	}

	/** Creates a new instance of PropertiesDialog, to show object properties.
	* Calls the constructor with isNew set to false. Used when the owner is not
	* a dialog.
	*
	* @param   parent  this dialog owner
	* @param   po      the pit object this dialog represents (null if none)	 
	* @param   title   this dialog title
	* @see PropertiesDialog(JDialog, String, PitObject, boolean)
	*/
    protected PropertiesDialog(JFrame parent, PitObject po, String title){
		this(parent, po, title, false);
		setProperties(parent, po, false);
    }
    
	/** Creates a new instance of PropertiesDialog.
	*
	* @param   parent  this dialog owner
	* @param   po      the pit object this dialog represents (null if none)	 
	* @param   title   this dialog title
	* @param   isNew   true if we are creating a new pitObject
	*/
	protected PropertiesDialog(JDialog parent, PitObject po, String title, boolean isNew) {
		super(parent, title);
		setProperties(parent, po, isNew);
	}

	/** Creates a new instance of PropertiesDialog, to show object properties.
	* Calls the constructor with isNew set to false.
	*
	* @param   parent  this dialog owner
	* @param   po      the pit object this dialog represents (null if none)	 
	* @param   title   this dialog title
	* @see PropertiesDialog(JDialog, String, PitObject, boolean)
	*/
    protected PropertiesDialog(JDialog parent, PitObject po, String title){
        this(parent, po, title, false);
        setProperties(parent, po, false);
    }
    
	/**
	* Sets the common properties of all objects.
	*
	* @param parent the parent window
	* @param po the pit object represented (null if none).
	* @param isNew indicates this is a create new window
	*/
	private void setProperties(Window parent, PitObject po, boolean isNew){
		this.parent = parent;
		this.po = po;
		this.isNew = isNew;
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setModal(true);
		this.setSize(400, 400);
		int count = (parent==null)?0:parent.getComponentCount();

		if (count == 0){
			this.setLocationRelativeTo(null);
		}
		else {
			this.setLocationRelativeTo(parent.getComponent(0));
		}

		Container c = this.getContentPane();
		BorderLayout bl = new BorderLayout();
		c.setLayout(bl);        

		//(create/cancel) or (update/close) button
		if (isNew){
			createBt.addActionListener(this);
			createBt.setForeground(Color.green);
			southPanel.add(createBt);                

			cancelBt.addActionListener(this);
			cancelBt.setForeground(Color.red);
			southPanel.add(cancelBt);

		}
		else {
			updateBt.addActionListener(this);
			southPanel.add(updateBt);
			updateBt.setForeground(Color.green);

			closeBt.addActionListener(this);
			closeBt.setForeground(Color.red);
			southPanel.add(closeBt);

		}
		//reset state, next state and previous state buttons        
		prevStBt.setToolTipText("previous state, for objects with states");
		prevStBt.setVisible(false);
		southPanel.add(prevStBt);

		resetStBt.setToolTipText("reset state, for objects with states");
		southPanel.add(resetStBt);
		resetStBt.setVisible(false);

		nextStBt.setVisible(false);
		nextStBt.setToolTipText("next state, for objects with states");        
		southPanel.add(nextStBt);

		//add the buttons in the bottom
		c.add(southPanel, BorderLayout.SOUTH);                  
	}

	/** Turn all state buttons(next, prev, reset) visible. Used by objects 
	* with states
	*/
	protected void enableStateButtons(){
		prevStBt.addActionListener(this);
		resetStBt.addActionListener(this);
		nextStBt.addActionListener(this);
		prevStBt.setVisible(true);
		resetStBt.setVisible(true);
		nextStBt.setVisible(true);
	}
	
	/** returns if the creation was successful. */
	public boolean getFieldsOk(){
		return fieldsOk;
	}
	
	/** child classes must implement this if they want to process action events.
	* @return false if event is not processed, true otherwise. In this case, action performed
	* wont process the event. 
	*/
	protected boolean processActionEvent(ActionEvent ae){
		return false;
	}

	/** child classes implement this to apply all values from field to the pit object.
	* @throw if values are incorrect
	*/
	abstract protected void setValues() throws Exception;
	
	/** child classes implement this function to be called after loading. */
	//abstract protected void loadValues();
	
	/** answers the actions performed on the local buttons. */
	public void actionPerformed(ActionEvent ae){
		// see if event is treated at higher level
		if (processActionEvent(ae)){
			// even processed
			return;
		}
		
		// process event
		Object o = ae.getSource();
		if (o == cancelBt){
			fieldsOk = false;
			setVisible(false);
		}
		else if (o == closeBt){
			setVisible(false);
		}
		else if (o == createBt){
			try {
				setValues();
				fieldsOk = true;
				setVisible(false);
			}
			catch (Exception e){
				fieldsOk = false;
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (o == updateBt){
			try {
				setValues();
				parent.repaint();
				closeBt.setEnabled(true);
			}
			catch (Exception e){
				closeBt.setEnabled(false);
				JOptionPane.showMessageDialog(this, e.getCause(), "Error", JOptionPane.ERROR_MESSAGE);				
			}
		}
		else if (o == nextStBt){
			po.nextState();
			parent.repaint();
			Cockpit.template.repaint();
		}
		else if (o == prevStBt){
			po.previousState();
			parent.repaint();			
			Cockpit.template.repaint();
		}
		else if (o == resetStBt){
			po.resetState();
			parent.repaint();
			Cockpit.template.repaint();			
		}
	}
}
