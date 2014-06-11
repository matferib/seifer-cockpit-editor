package pitComponents;

import java.io.*;
import pitComponents.pitObjects.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import cockpitEditor.UsPr;
import windows.PropertiesDialog;
import pitComponents.pitHelperComponents.PitComponentArray;

public class PitButton {
    /// indicates this button can be created, used only for creating new buttons
    public boolean canBeCreated = false;
    
    ///the cockpit object
    static Cockpit cockpit;
    ///the button id, each button has one
    public int id;
    public int states, delay, cursorid, initstate, sound[] = new int[4], 
        callbackslot, numbuttonviews;
    private int numbvsFound; //to check
    private int numSdRead = 0;
    public PitComment comments;
    public String label;
    
    //OF options
    public static final int MAX_DATA = 10;
    int numData = 0;
    int data[] = new int[MAX_DATA];
    
    public PitButton(int id) {
        this.id = id;
        
        states = 0;
        delay = 0;
        cursorid = 0;
        initstate = 0;
        
        callbackslot = 0;
        numbuttonviews = 0;
    }
    
    public void parseData(PitTokenizer st) throws ParseErrorException {
        String token = null;
        try {
            do {
                st.skipComment();
                if (st.ttype != st.TT_WORD) {
                    throw new ParseErrorException("expecting word token");
                }
                
                token = st.sval;
                if (token.equals("#end")) {
                    break;
                }
                
                st.nextToken();
                if (st.ttype == '=') {
                    //skip =
                    st.nextToken();
                }
                if (st.ttype != st.TT_NUMBER) {
                    throw new ParseErrorException("expecting number token");
                }
                
                if (token.equals("states")) {
                    states = (int) st.nval;
                    //					sound = new int[states];
                    //					for (int i=0;i<states;i++)
                    //						sound[i] = -2; // not used
                }
                else if (token.equals("delay")) {
                    delay = (int) st.nval;
                }
                else if (token.equals("cursorid")) {
                    cursorid = (int) st.nval;
                }
                else if (token.equals("initstate")) {
                    initstate = (int) st.nval;
                }
                else if (token.equals("callbackslot")) {
                    callbackslot = (int) st.nval;
                }
                else if (token.equals("numbuttonviews")) {
                    numbuttonviews = (int) st.nval;
                }
                else if (token.startsWith("sound")) {
                    //check to see if we got more sounds than states
                    //					if (numSdRead+1 > states)
                    //						throw new Exception("more sounds than states");
                    sound[numSdRead++] = (int) st.nval;
                }
                //OF options
                //whenever we read OF format, we set options to OF
                else if (token.startsWith("data")) {
                    if (numData == MAX_DATA){
                        throw new Exception("maximum number of data reached");
                    }
                    UsPr.datFormat = UsPr.DAT_OF;
                    data[numData++] = (int) st.nval;
                }
                
                //get the ;
                st.nextToken();
                if (st.ttype != ';') {
                    throw new Exception("expecting ;");
                }
                
            }
            while (true);
            
            //button label
            if (comments != null){
                label = comments.getLabel();
            }
            
        }
        catch (Exception e) {
            throw new ParseErrorException("pitButton.java: " + e);
        }
    }
    
    public void writeComponent(misc.RAFile arq) {
        try {
            //write comments
            if (comments != null){
                comments.write(arq);
            }
            arq.writeln("#" + id + " BUTTON");
            arq.writeln("\tstates = " + states + ";");
            arq.writeln("\tdelay = " + delay + ";");
            arq.writeln("\tcursorid = " + cursorid + ";");
            arq.writeln("\tinitstate = " + initstate + ";");
            //sound comes here
            for  (int i=0;i<numSdRead;i++) {
                arq.writeln("\tsound" + (i+1) + " = " + sound[i] + ";");
            }
            arq.writeln("\tcallbackslot = " + callbackslot + ";");
            arq.writeln("\tnumbuttonviews = " + numbuttonviews + ";");
            
            //OF options
            if (UsPr.datFormat == UsPr.DAT_OF){
                for (int i=0;i<numData;i++){
                    arq.writeln("\tdata" + (i+1) + " = " + data[i] + ";");
                }
            }
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitBuffer.java-> erro de IO: " + io);
            
        }
    }
    
    /**
     * A better way to represent buttons. Human readable, used by the button lists.
     */
    public String toString(){
        String s = new String("Button " + id + ", clbk " + callbackslot);
        //if the button has a label, we use it here
        if (label != null){
            s = s.concat(", " +  label);
        }
        return s;
    }

    
    /** Checks the buttons reference count. Reference count are the number of
     * times the button is used by buttonviews. For example, if buttonviews 230,
     * 240, and 262 uses button 335, its refcount is 3.
     *
     * @return a String with the erros found
     */
    public static String check(){
        String s = new String();
        
        //checks all buttons
        PitComponentArray buttons = cockpit.buttons;
        PitComponentArray bviews = cockpit.bviews;
        int nbuttons = buttons.getNumComponents();
        int nbviews = bviews.getNumComponents();
        //zero all bvs found
        for (int i=0;i<nbuttons;i++){
            PitButton b = (PitButton)buttons.elementAt(i);
            b.numbvsFound = 0;
        }
        
        //scan bvs for buttons
        for (int i=0;i<nbviews;i++){
            PitButtonView bview = (PitButtonView)bviews.elementAt(i);
            PitButton b = (PitButton)buttons.get(bview.parentbutton);
            if (b == null){
                //invalid parent
                s.concat("Buttonview " + bview.id + " has invalid parent(" + bview.parentbutton +")\n");
            }
            else {
                b.numbvsFound++;
            }
        }
        
        //check all buttons again
        for (int i=0;i<nbuttons;i++){
            PitButton b = (PitButton)buttons.elementAt(i);
            if (b.numbuttonviews != b.numbvsFound){
                s.concat("Button "+ b.id + " has " + b.numbuttonviews + " declared, but found " + b.numbvsFound + "\n");
            }
            if ((b.states != 0) && (b.initstate >= b.states)){
                s.concat("Button " + b.id + " has " + b.states + ", but initstate is " + b.initstate + "\n");
            }
            
            //check initstate and num of states
            if (b.initstate >= b.states){
                s.concat("Button " + b.id + " initstate is " + b.initstate +", but has only " + b.states + "states\n");
            }
        }
    
        return s;
    }
    
    /** Corrects all refcounts. Same as check, but correct errors =)
     */
    public static void correct(){       
        //checks all buttons
        PitComponentArray buttons = cockpit.buttons;
        PitComponentArray bviews = cockpit.bviews;
        int nbuttons = buttons.getNumComponents();
        int nbviews = bviews.getNumComponents();
        //zero all butoons bvs found
        for (int i=0;i<nbuttons;i++){
            PitButton b = (PitButton)buttons.elementAt(i);
            b.numbvsFound = 0;
        }
        
        //scan bvs for buttons
        for (int i=0;i<nbviews;i++){
            PitButtonView bview = (PitButtonView)bviews.elementAt(i);
            PitButton b = (PitButton)buttons.get(bview.parentbutton);
            if (b != null){
                b.numbvsFound++;
            }
        }
        
        //check all buttons again
        for (int i=0;i<nbuttons;i++){
            PitButton b = (PitButton)buttons.elementAt(i);
            if (b.numbuttonviews != b.numbvsFound){
                b.numbuttonviews = b.numbvsFound;
            }
            if  (b.states == 0){
                b.initstate = 0;
            }
            else if ((b.initstate >= b.states)){
                b.initstate = b.states - 1;
            }

        }   
    }
    
    /** When a button is to be created, this function gets called
     *
     * @param parent the dialog parent of this
     */
    public void showNew(JDialog parent){
        ButtonPropDialog bpd = new ButtonPropDialog(parent, this, true); 
        bpd.setVisible(true);
    }
    
    /** Shows the button properties
     *
     * @param parent the parent window
     */
    public void showProperties(JFrame parent){
        ButtonPropDialog bpd = new ButtonPropDialog(parent, this, false);
        bpd.setVisible(true);
    }

    /** Shows the button properties
     *
     * @param parent the parent window
     */
    public void showProperties(JDialog parent){
        ButtonPropDialog bpd = new ButtonPropDialog(parent, this, false);
        bpd.setVisible(true);
    }

    /** shows buttons dialog, a buttons manager */
    public static void showButtonList(JFrame parent){
        ButtonListDialog bld = new ButtonListDialog(parent, true);
	bld.setVisible(true);
    }
        
    /** shows all buttons in a dialog, and get the selected one */
    public static PitButton getButtonFromList(JDialog parent){
        ButtonListDialog bld = new ButtonListDialog(parent);
		bld.setVisible(true);
		return bld.selectedBt;
    }
    
    /** shows all buttons in a dialog, and get the selected one */    
    public static PitButton getButtonFromList(JFrame parent){
        ButtonListDialog bld = new ButtonListDialog(parent);
		bld.setVisible(true);
        return bld.selectedBt;
    }

}

/** a dialog with all buttons shown as a list */
class ButtonListDialog extends JDialog 
        implements ActionListener, WindowListener {
    ButtonList bl = new ButtonList();
    JButton okBt, cancelBt, newBt, viewBt, deleteBt, childBt;
    PitButton selectedBt = null;

    public ButtonListDialog(JFrame parent, boolean showNewBt){
        super(parent);
        setProperties(showNewBt);
	//setLocationRelativeTo(parent);	
    }    

    public ButtonListDialog(JDialog parent, boolean showNewBt){
        super(parent);
        setProperties(showNewBt);
	//setLocationRelativeTo(parent);	
    }        
    
    public ButtonListDialog(JFrame parent){
        super(parent);
        setProperties(false);
	//setLocationRelativeTo(parent);	
    }

    public ButtonListDialog(JDialog parent){
        super(parent);
        setProperties(false);
	//setLocationRelativeTo(parent);
    }
    
    private void setProperties(boolean showNewBt){
	//show buttons dialog
	Container c = getContentPane();
	c.setLayout(new BorderLayout());
	setTitle("Please choose a button");

	c.add(bl, BorderLayout.CENTER);

	JPanel p = new JPanel();

	//buttons
	if (showNewBt){
	    newBt = new JButton("new");
	    newBt.setForeground(Color.yellow);
	    childBt = new JButton("create bv");
	    childBt.setForeground(Color.green);
	    childBt.setToolTipText("Creates a buttonview from this parent button");
	    viewBt = new JButton("view");
	    viewBt.setForeground(Color.blue);
	    deleteBt = new JButton("delete");
	    deleteBt.setForeground(Color.red);
	    cancelBt = new JButton("close");
	    childBt.addActionListener(this);
	    viewBt.addActionListener(this);
	    deleteBt.addActionListener(this);
	    newBt.addActionListener(this);
	    cancelBt.addActionListener(this);
	    p.add(newBt);
	    p.add(childBt);                
	    p.add(viewBt);
	    p.add(deleteBt);
	    p.add(cancelBt);                
	}
	else {
	    okBt = new JButton("ok");
	    okBt.setForeground(Color.green);
	    viewBt = new JButton("view");
	    viewBt.setForeground(Color.blue);
	    cancelBt = new JButton("cancel");
	    cancelBt.setForeground(Color.red);
	    okBt.addActionListener(this);
	    cancelBt.addActionListener(this);
	    viewBt.addActionListener(this);
	    p.add(okBt);
	    p.add(viewBt);
	    p.add(cancelBt);
	}
	c.add(p, BorderLayout.SOUTH);

	setModal(true);
	setSize(400, 400);
	setLocationRelativeTo(getParent());
    }
    
    public void actionPerformed(ActionEvent ae){
        Object o = ae.getSource();
        if (o == okBt){
            selectedBt = bl.getSelectedButton();
            this.setVisible(false);
        }
        else if (o == cancelBt){
            selectedBt = null;
            this.setVisible(false);
        }
        else if (o == viewBt){
            selectedBt = bl.getSelectedButton();
            if (selectedBt != null){
                selectedBt.showProperties(this);
            }
        }
        else if (o == newBt){
            //get and id for the button, based on the first id
            PitButton bt = (PitButton)Cockpit.buttons.elementAt(0);
            int tryid;
            if (bt == null){
                tryid = 1000;
            }
            else {
                tryid = bt.id;
            }
            tryid = Cockpit.buttons.getFreeId(tryid+1);
            bt = new PitButton(tryid);
            bt.showNew(this);
            if (bt.canBeCreated){
                Cockpit.buttons.add(bt, bt.id);
                bl.reload();
            }
        }
        else if (o == childBt){
            //we create a child buttonview from the selected button in the current panel
            //usual checks
            PitButton bt = bl.getSelectedButton();
            if ((Cockpit.currentPanel == null) || (bt == null)){
                return;
            }
            
            int id = Cockpit.currentPanel.id; 
            id = Cockpit.bviews.getFreeId(id);
            PitButtonView bv = new PitButtonView(id);
            bv.parentbutton = bt.id;
            bv.persistant = 0;
            bv.destloc[0] = new PitArea(new Rectangle(100, 100));
            bv.setNumSrcs(bt.states);
            bv.states = bt.states;
            bv.comments = PitComment.clone(bt.comments);
	    bv.label = bv.comments == null ? null : bv.comments.getLabel();
            for (int i=0;i<bt.states;i++){
                bv.srcloc[i] = new PitArea(new Rectangle(100, 100));
            }            
            Cockpit.bviews.add(bv, bv.id);
            Cockpit.currentPanel.buttonviewsVector.add(bv);
            Cockpit.setSelectedObject(bv);
            Cockpit.centerOnSelectedObject(true, true);
        }
        else if (o == deleteBt){
            selectedBt = bl.getSelectedButton();
            if (selectedBt != null){
                if (JOptionPane.showConfirmDialog(this, 
                        "You are about to delete a button, this can be dangerous. Are you sure?", 
                        "Button delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                    
                    Cockpit.buttons.delete(selectedBt.id);
                    bl.repaint();
                }
            }
        }

    }
    
    //window events
    public void windowActivated(WindowEvent e){}
    public void windowClosed(WindowEvent e){}    
    public void windowClosing(WindowEvent e){
        this.selectedBt = null;
    }
    public void windowDeactivated(WindowEvent e){}
    public void windowDeiconified(WindowEvent e){}
    public void windowIconified(WindowEvent e){}
    public void windowOpened(WindowEvent e){}
}

class ButtonPropDialog extends PropertiesDialog {
    PitButton bt;
    JTabbedPane tp = new JTabbedPane(JTabbedPane.LEFT);
    LabeledNumField id = new LabeledNumField(this, "button", 
            "button id", 3);
    LabeledNumField states = new LabeledNumField(this, "states", 
            "number of states this button has", 3);
    LabeledNumField delay = new LabeledNumField(this, "delay", 
            "delay for the button, -1 if it stays depressed, 0 if it goes back to original position", 3);
    LabeledNumField cursorid = new LabeledNumField(this, "cursorid", 
            "the cursor when mouse is over the button", 3);
    LabeledNumField initstate = new LabeledNumField(this, "initstate", 
            "the position of the button when you enter the game. Its srcloc is never drawn", 3);
    LabeledNumField callbackslot = new LabeledNumField(this, "callbackslot", 
            "what the button does, consult callback entries", 3);
    
    //OF stuff
    String dataLabels[] = new String[PitButton.MAX_DATA];
    LabeledNumField data[] = new LabeledNumField[PitButton.MAX_DATA];
    
    JLabel numbvs = new JLabel();
    CommentsPane comments = new CommentsPane(this);
    
    public ButtonPropDialog(JFrame parent, PitButton bt, boolean isNew){
        super(parent, null, "Button Properties", isNew);
        setProperties(bt);
    }

    public ButtonPropDialog(JDialog parent, PitButton bt, boolean isNew){
        super(parent, null, "Button Properties", isNew);
        setProperties(bt);
    }
    
    private void setProperties(PitButton bt){
        this.bt = bt;
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            if (!isNew){
                mainP.add(new JLabel("Button #" + bt.id, JLabel.CENTER));
            }
            else {
                mainP.add(id);
                id.setValue(bt.id);
            }
            mainP.add(states);
            mainP.add(delay);
            mainP.add(cursorid);
            mainP.add(initstate);
            mainP.add(callbackslot);
            mainP.add(numbvs);
            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");
        }
        //sounds
        {
            JPanel sp = new JPanel();
            tp.add("sounds", sp);
            tp.setToolTipTextAt(1, "sounds");
        }
        //OF tab
        {
            JPanel op = new JPanel();
            op.setLayout(new BorderLayout());
            //title
            op.add(new JLabel("Data"), BorderLayout.NORTH);
            //data panel
            JPanel dp = new JPanel(new GridLayout(PitButton.MAX_DATA, 1));
            for (int i=0;i<PitButton.MAX_DATA;i++){
                dataLabels[i] = "data" + (i+1);
                data[i] = new LabeledNumField(parent, dataLabels[i], 5);
                dp.add(data[i]);
            }
            JScrollPane dps = new JScrollPane(dp);
            op.add(dps, BorderLayout.CENTER);
            tp.add("OF", op);
            tp.setToolTipTextAt(2, "Open Falcon options");
            if (UsPr.datFormat != UsPr.DAT_OF){
                tp.setEnabledAt(2, false);
            }
        }
        //comments
        {
            tp.add("comms", comments);
            tp.setToolTipTextAt(3, "comments");
        }
        c.add(tp, BorderLayout.CENTER);

        if (!isNew){
            loadValues();
		}
    }
    
    protected void loadValues(){
        //main
        {
            states.setValue(bt.states);
            delay.setValue(bt.delay);
            cursorid.setValue(bt.cursorid);
            initstate.setValue(bt.initstate);
            callbackslot.setValue(bt.callbackslot);
        }
        //sounds
        {
            
        }
        //OF options
        {
            //data array
            if (UsPr.datFormat == UsPr.DAT_OF){
                for (int i=0; i< bt.numData;i++){
                    data[i].setValue(bt.data[i]);
                }
            }
        }
        //comments
        {
            comments.setComments(bt.comments);
        }
	}
    
	protected void setValues() throws Exception {
		//update main
		{
			if (isNew){
				int tid = id.getValue();
				if (Cockpit.buttons.get(tid) != null){
					int sid = Cockpit.buttons.getFreeId(tid);
					JOptionPane.showMessageDialog(this, 
						"Id already exists, I suggest " + sid,
						"Error", JOptionPane.ERROR_MESSAGE
					);
					throw new Exception();
				}
				bt.id = tid;
			}
			bt.states = states.getValue();
			bt.delay = delay.getValue();
			bt.cursorid = cursorid.getValue();
			bt.initstate = initstate.getValue();
			bt.callbackslot = callbackslot.getValue();
		}
		//sounds
		{
		}
		//OF options
		{
			bt.numData = 0;
			for (int i=0;i<PitButton.MAX_DATA;i++){
				if (data[i].isFieldValid()){
					bt.data[bt.numData++] = data[i].getValue();
				}
				else {
					break;
				}
			}
		}
		//comments
		{
			bt.comments = comments.getComments();
			bt.label = (bt.comments == null) ? null : bt.comments.getLabel();
		}
	}
}


