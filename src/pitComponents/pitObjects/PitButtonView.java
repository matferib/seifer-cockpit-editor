package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;


import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import windows.PropertiesDialog;
import pitComponents.pitHelperComponents.PitComponentArray;

/** a buttonview in 2d cockpit represents a cockpit button which can be viewed
* from different angles. Although the button is the same, the views are different.
*/
public class PitButtonView extends PitObject {

    public int parentbutton = -1; //-1 = flag to indicate it has not been used yet
    private int numSrcRead = 0;

    public PitButtonView(int id) {
        super(id);
        setNumDsts(1);        
    }
    
    public Object clone(){
        PitButtonView bv = new PitButtonView(0);
        super.cloneMeTo(bv);
        bv.parentbutton = parentbutton;
        bv.numSrcRead = numSrcRead;
        return bv;
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

                //get the first value after the =
                st.nextToken();
                if (st.ttype == '=') {
                    //skip =
                    st.nextToken();
                }
                if ( (!token.endsWith("loc")) && (st.ttype != st.TT_NUMBER)) {
                    throw new ParseErrorException("expecting number token");
                }

                //parse data!
                if (token.equals("states")) {
                    states = (int) st.nval;
                    currentState = 0;
                    setNumSrcs(states);
                }
                else if (token.equals("srcloc")) {
                    if (numSrcRead + 1 > states) {
                            throw new Exception("more srcloc than states");
                    }

                    srcloc[numSrcRead] = new PitArea(false, true);
                    srcloc[numSrcRead++].parseData(st);
                }
                else if (token.equals("persistant")) {
                        persistant = (int) st.nval;
                }
                else if (token.equals("parentbutton")) {
                        parentbutton = (int) st.nval;
                }
                else if (token.equals("destloc")) {
                        destloc[0] = new PitArea();
                        destloc[0].parseData(st);
                }

                //get the ;
                st.nextToken();
                if (st.ttype != ';') {
                        throw new Exception("expecting ;");
                }
            }
            while (true);
            if (comments != null){
                label = comments.getLabel();
            }
        }
        catch (Exception e) {
            throw new ParseErrorException("pitButtonView.java: " + e);
        }
    }

    public void writeComponent(misc.RAFile arq) {
        int i = 0;
        try {
            if (comments!=null){
                comments.write(arq);
            }
            arq.writeln("#" + id + " BUTTONVIEW");
            arq.writeln("\tstates = " + states + ";");
            for (i = 0; i < states; i++) {
                    arq.writeln("\tsrcloc = " + srcloc[i] + ";");
            }
            arq.writeln("\tparentbutton = " + parentbutton + ";");

            //buttons must be opaque by default... if you want them transparent
            //click transparent, should i do this???
//            if (destloc[0].transparent){
//                destloc[0].transparent = false;
//                destloc[0].opaque = true;
//            }
            arq.writeln("\tdestloc = " + destloc[0] + ";");

            if (persistant != -2) {
                    arq.writeln("\tpersistant = " + persistant + ";");
            }
            else {
                    arq.writeln("\tpersistant = 0;");
            }
            arq.writeln("#END");
        }
        catch (IOException io) {
                System.out.println("pitButtonview.java-> erro de IO: " + io);
        }
    }

    public String toTextFormat(){
        String s = new String();
        int i = 0;
        if (comments!=null){
            s += comments;
        }
        s += "#" + id + " BUTTONVIEW"+ ls;
        s += "\tstates = " + states + ";"+ ls;
        for (i = 0; i < states; i++) {
            s += "\tsrcloc = " + srcloc[i] + ";"+ ls;
        }
        s += "\tparentbutton = " + parentbutton + ";"+ ls;
        
        s += "\tdestloc = " + destloc[0] + ";"+ ls;
        
        if (persistant != -2) {
            s += "\tpersistant = " + persistant + ";"+ ls;
        } else {
            s += "\tpersistant = 0;"+ ls;
        }
        s += "#END"+ ls;
        return s;
    }
    
    //default: draw the button location
    public void draw(Graphics2D g) {
        //draw other state if we must
        if ((currentState > 0)){
            if ((srcloc != null) && (srcloc[currentState] != null)){
                super.drawSrcDst(g, currentState, 0);
            }
            else {
                currentState = 0;
            }
        }
        else {
            //draws only the rectangle
            destloc[0].draw(g);
        }
    }

    //default: draw the button location
    public void drawLabeled(Graphics2D g) {
        draw(g);
        if (label!=null)
            destloc[0].drawText(label.toString(), g);
    }

	public PropertiesDialog getPropertiesDialog(JFrame parent){
		return new BVPropDialog(parent, this);
	}

	public PropertiesDialog getNewPropertiesDialog(JDialog parent){
		return new BVPropDialog(parent, this);
	}
}

class BVPropDialog extends PropertiesDialog {
    PitButtonView bv;
    JTabbedPane tp = new JTabbedPane();

    //main
    LabeledNumField parentField =
      new LabeledNumField(this, "parentbutton", "the parent button", 8, 
            "list", "select the parent button from button list");
    LabeledCBox persistantcb = new LabeledCBox("persistant");

    //areas
    JButton makeSameBt;
    AreasFieldPanel dest = new AreasFieldPanel(this, "destloc");
    AreasFieldPanel srcs[] = new AreasFieldPanel[PitButtonView.MAX];

    //comments
    CommentsPane comments = new CommentsPane(this);

    
    public BVPropDialog(JFrame parent, PitButtonView bv){
        super(parent, bv, "ButtonView Properties", false);
        setProperties(bv);
    }

    public BVPropDialog(JDialog parent, PitButtonView bv){
        super(parent, bv, "ButtonView Properties", true);
        setProperties(bv);
    }
    
    
    private void setProperties(PitButtonView bv){
        super.enableStateButtons();
        this.bv = bv;
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("Buttonview #" + bv.id, JLabel.CENTER));
            mainP.add(parentField);
            parentField.addActionListener(this);
            mainP.add(persistantcb);
            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");
        }
        //areas tab
        {
            JPanel areasP = new JPanel(new GridLayout(PitButtonView.MAX+3, 1));

            //button to make all panel areas the same as the template one
            makeSameBt = new JButton("equal areas");
            makeSameBt.setToolTipText(
                    "Make all areas the same size as the destination area");
            makeSameBt.addActionListener(this);
            areasP.add(makeSameBt);
            
            //destloc
            areasP.add(dest);

            //srclocs
            JLabel label = new JLabel("Srclocs", JLabel.CENTER);
            areasP.add(label);
            for (int i=0;i<PitButtonView.MAX;i++){
                srcs[i] = new AreasFieldPanel(this, "src" + i + ":", false);
                srcs[i].setEnabled(true);
                areasP.add(srcs[i]);
            }

            JScrollPane areasSP = new JScrollPane(areasP);
            areasSP.setHorizontalScrollBarPolicy(areasSP.HORIZONTAL_SCROLLBAR_NEVER);
            tp.add("areas", areasSP);
            tp.setToolTipTextAt(1, "srclocs and destloc");
        }
        //comments
        {
            tp.add("comms", comments);
            tp.setToolTipTextAt(2, "comments");
        }

        c.add(tp, BorderLayout.CENTER);

        if (!isNew)
            loadValues();

    }

    public void loadValues(){
        //main
        parentField.setValue(bv.parentbutton);
        persistantcb.setSelected(bv.persistant == 1);

        //areas
        //statesField.setValue(bv.states);
        dest.setFields(bv.destloc[0]);
        for (int i=0;i<PitButtonView.MAX;i++){
            if (i < bv.states){
                //enable and setvalue
                //srcs[i].setEnabled(true);
                srcs[i].setFields(bv.srcloc[i]);
            }
            else {
                //srcs[i].setEnabled(false);
            }
        }

        //comments
        comments.setComments(bv.comments);
    }

	protected void setValues() throws Exception {
		//update main
		{
			int oldparent = bv.parentbutton;
			bv.parentbutton = parentField.getValue();

			//check if parent is valid
			PitComponentArray buttons = Cockpit.buttons;
			PitButton button = (PitButton)buttons.get(bv.parentbutton);
			if (button == null){
				//invalid parent
				bv.parentbutton = oldparent;
				JOptionPane.showMessageDialog(this, "Invalid parent", "Error", JOptionPane.ERROR_MESSAGE);
				throw new Exception("Invalid parent button");
			}


			//increase new parent refcount and decrease old one
			//we only dec oldparent if we are not new
			if (oldparent != -1){
				button = (PitButton)buttons.get(oldparent);
				button.numbuttonviews--;
			}
			button.numbuttonviews++;

			if (persistantcb.isSelected()) {
				bv.persistant = 1;
			}
			else {
				bv.persistant = 0;
			}
		}
		//update areas
		{
			//destloc
			bv.destloc[0] = dest.getArea();
			//bv.states = statesField.getValue();
			//bv.setNumSrcs(bv.states);
			//se how many sources are valid
			int numStates = 0;
			for (int i = 0; i < bv.MAX; i++) {
				try {
					if (srcs[i].getArea() == null){
						break;
					}
					numStates++;
				}
				catch (Exception e){
					break;
				}
			}

			//get the valid sources
			bv.states = numStates;
			bv.setNumSrcs(numStates);
			for (int i = 0; i < numStates; i++) {
					bv.srcloc[i] = srcs[i].getArea();
			}                               
		}
		//update comments
		{
			bv.comments = comments.getComments();
			if (bv.comments != null){
				bv.label = bv.comments.getLabel();
			}
		}
	}

	protected boolean checkValues(){
		try {
			//update main
			{
				parentField.getValue();

				//check if parent is valid
				PitComponentArray buttons = Cockpit.buttons;
				PitButton button = (PitButton)buttons.get(bv.parentbutton);
				if (button == null){
					JOptionPane.showMessageDialog(this, "Invalid parent", "Error", JOptionPane.ERROR_MESSAGE);
					throw new Exception("Invalid parent button");
				}
			}
			//update comments
			{
				comments.getComments();
			}
			return true;
		}
		catch (Exception e){
			return false;
		}
	}

    protected boolean processActionEvent(ActionEvent e){
        Object o = e.getSource();

        if (o == makeSameBt){
            PitButtonView bv = this.bv;
            bv.equalSrclocsToDest(0);
            //update area src values
            for (int i=0;i<bv.states;i++){
                srcs[i].setFields(bv.srcloc[i]);
            }
			return true;
        }
        else if (o == parentField){
            //show buttons dialog
            PitButton pb = PitButton.getButtonFromList(this);
            if (pb != null){
                parentField.setValue(pb.id);
            }
			return true;
        }
		return false;
    }
}





















