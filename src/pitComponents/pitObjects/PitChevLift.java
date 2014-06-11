package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import windows.PropertiesDialog;

/**
 * Represents both the chevrons and lift lines. The ehLift variable identifies
 * which type of component this is. If ehLift is true, than its a liftline.
 * Else, its a chevron.
 */
public class PitChevLift extends PitObject {

    float pantilt[] = new float[2];
	int pantiltlabel[] = new int[2];
    boolean ehLift;

    /**
     * Creates a new chevron/liftline, depending on the ehL parameter. Chevrons
     * and lift lines have the same properties. The only differ in the way they
     * are drawn. Chevrons are >> and liftlines are straight lines.
     *
     * @param ehL   true if its a liftline, false if chevron
     * @param id    the id of this object
     */
    public PitChevLift(boolean ehL, int id) {
        //no src, no dest
        super(id, 0, 0);
        ehLift = ehL;
    }

    public Object clone(){
        PitChevLift cl = new PitChevLift(this.ehLift, 0);
        super.cloneMeTo(cl);
        cl.pantilt = pantilt.clone();
        cl.pantiltlabel = pantiltlabel.clone();
        return cl;
    }


    public void parseData(PitTokenizer st) throws ParseErrorException {
        String token = null;
        float n0, n1;
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
                n0 = (float) st.nval;
                st.nextToken();
                if (st.ttype != st.TT_NUMBER) {
                    throw new ParseErrorException("expecting number token");
                }
                n1 = (float) st.nval;
                
                if (token.equals("pantiltlabel")) {
                    pantiltlabel[0] = (int)n0;
                    pantiltlabel[1] = (int)n1;
                }
                else if (token.equals("pantilt")) {
                    pantilt[0] = n0;
                    pantilt[1] = n1;
                    
                }
                
                //get the ;
                st.nextToken();
                if (st.ttype != ';') {
                    throw new Exception("expecting ;");
                }
                
            }
            while (true);
            //try to get the label
            if (comments != null){
                label = comments.getLabel();
            }
            
            
        }
        catch (Exception e) {
            throw new ParseErrorException("pitChevLift.java: " + e);
        }
        
    }
    
    public void writeComponent(misc.RAFile arq) {
        int i = 0;
        try {
            if (comments!=null){
                comments.write(arq);
            }
            if (ehLift) {
                arq.writeln("#" + id + " LIFTLINE");
            }
            else {
                arq.writeln("#" + id + " CHEVRON");
            }
            if (pantilt != null) {
                arq.writeln("\tpantilt = " + pantilt[0] + " " + pantilt[1] +
                ";");
            }
            if (pantiltlabel != null) {
                arq.writeln("\tpantiltlabel = " + pantiltlabel[0] + " " +
                pantiltlabel[1] + ";");
            }
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitChevLift.java-> erro de IO: " + io);
            
        }
    }

    public String toTextFormat() {
        int i = 0;
        String s = new String();
        if (comments!=null){
            s += comments;
        }
        if (ehLift) {
            s += "#" + id + " LIFTLINE"+ ls;
        } else {
            s += "#" + id + " CHEVRON"+ ls;
        }
        if (pantilt != null) {
            s += "\tpantilt = " + pantilt[0] + " " + pantilt[1] +
                    ";"+ ls;
        }
        if (pantiltlabel != null) {
            s += "\tpantiltlabel = " + pantiltlabel[0] + " " +
                    pantiltlabel[1] + ";"+ ls;
        }
        s += "#END"+ ls;
        return s;
    }
    
	//we dont draw these, yet
	public void draw(Graphics2D g) {
	}
	public void drawLabeled(Graphics2D g) {
	}
	public void drawTemplate(Graphics2D g){
	}
	public void drawTemplateLabeled(Graphics2D g){
	}


	protected PropertiesDialog getPropertiesDialog(JFrame parent){
		CLPropDialog clp = new CLPropDialog(parent, this);
		return clp;
	}

	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		CLPropDialog clp = new CLPropDialog(parent, this);
		return clp;
	}

}

class CLPropDialog extends PropertiesDialog {
	
    private static int CHEV = 0, LIFT = 1;
    private static final String names[] = {"Chevron", "Liftline"};
	private static final String pantiltTips[] = {"pan (floating point)", "tilt (floating point)"};
	private static final String pantiltlabelTips[] = {"pan label (integer)", "tilt label (integer)"};

    PitChevLift cl;
    JTabbedPane tp = new JTabbedPane();

    
    //main, the pantilt and the pantiltlabel, and the combo
    LabeledDropDown ehLift = 
        new LabeledDropDown("Object type", names);
    LabeledFloatFields pantilt = new LabeledFloatFields(this, "pantilt", pantiltTips, 2, 5);
    LabeledFloatFields pantiltLabel = new LabeledFloatFields(this, "pantiltlabel", pantiltlabelTips, 2, 5);

    //comments
    CommentsPane comments = new CommentsPane(this);

    public CLPropDialog(JDialog parent, PitChevLift cl){
        super(parent, cl, "Chevron/LiftLine Properties", true);
        setProperties(cl);
    }

    public CLPropDialog(JFrame parent, PitChevLift cl){
        super(parent, cl, "Chevron/LiftLine Properties", false);
        setProperties(cl);
    }
    
    
    private void setProperties(PitChevLift cl){

        this.cl = cl;
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));

            mainP.add(ehLift);
            mainP.add(pantilt);
            mainP.add(pantiltLabel);

            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");
        }
        //comments
        {
            tp.add("comms", comments);
            tp.setToolTipTextAt(1, "comments");
        }

        c.add(tp, BorderLayout.CENTER);

        if (!isNew)
            loadValues();

    }

    public void loadValues(){
        //main
        pantilt.setFields(cl.pantilt);
        pantiltLabel.setFields(cl.pantiltlabel);
        if (cl.ehLift){
            ehLift.setSelectedIndex(LIFT);
        }
        else{
            ehLift.setSelectedIndex(CHEV);            
        }

        //comments
        comments.setComments(cl.comments);
    }

    protected void setValues() throws Exception {
		//update main
		{
			if (ehLift.getSelectedIndex() == CHEV)
				cl.ehLift = false;
			else
				cl.ehLift = true;
			cl.pantilt = pantilt.getFields();
			cl.pantiltlabel = pantiltLabel.getFieldsAsInteger();                
		}
		//update comments
		{
			cl.comments = comments.getComments();
			if (cl.comments != null){
				cl.label = cl.comments.getLabel();
			}
		}
		closeBt.setEnabled(true);
    }
}



