package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import windows.PropertiesDialog;

public class PitDED extends PitObject {

    /** there are 2 types of DEDs: DED (main) and PFL (faults) */
    protected static final int DED_DED=1, DED_PFL=2;
    /** the ded type, either DED_DED or DED_PFL */
    public int dedtype = DED_DED;

    public PitDED(int id) {
        //single dest
        super(id, 0, 1);
        color = new PitColor[1]; //only one color
    }
    public Object clone(){
        PitDED ded = new PitDED(0);
        super.cloneMeTo(ded);
        ded.dedtype = dedtype;
        return ded;
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
                if (st.ttype == '=') { //skip =
                    st.nextToken();
                }
                if (
                    (!token.equals("dedtype")) &&
                    (!token.endsWith("loc")) &&
                    (st.ttype != st.TT_NUMBER)
                ){
                    throw new ParseErrorException("expecting number token");
                }
                
                if (token.equals("destloc")) {
                    destloc[0] = new PitArea();
                    destloc[0].parseData(st);
                }
                else if (token.equals("callbackslot")) {
                    callbackslot = (int) st.nval;
                }
                else if (token.equals("cyclebits")) {
                    cyclebits = (int) st.parseHex();
                }
                else if (token.startsWith("color")) {
                    color[0] = st.parseColor();
                }
                else if (token.equals("dedtype")) {
                    String dt = st.sval.toLowerCase();
                    if (dt.equals("ded")){
                        dedtype = DED_DED;
                    }
                    else if (dt.equals("pfl")){
                        dedtype = DED_PFL;
                    }
                    else {
                        throw new ParseErrorException("wrong DED type");
                    }
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
            throw new ParseErrorException("pitDED.java: " + e);
        }
        
    }
    
    public void writeComponent(misc.RAFile arq) {
        int i = 0;
        try {
            if (comments != null){
                comments.write(arq);
            }
            arq.writeln("#"+ id +" DED");
            if (cyclebits < 0){
                arq.writeln("\tcyclebits = " + cyclebits + ";");
            }
            else{
                arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                ";");
            }
            arq.writeln("\tcallbackslot = " + callbackslot + ";");
            arq.writeln("\tcolor0 = " + color[0] + ";");
            if (dedtype == DED_DED){
                arq.writeln("\tdedtype = DED;");
            }
            else {
                arq.writeln("\tdedtype = PFL;");
            }
            arq.writeln("\tdestloc = " + destloc[0] + ";");
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitDED.java-> erro de IO: " + io);
        }
    }

    public String toTextFormat(){
        String s = new String();
        int i = 0;
        if (comments != null){
            s += comments;
        }
        s += "#"+ id +" DED"+ ls;
        if (cyclebits < 0){
            s += "\tcyclebits = " + cyclebits + ";"+ ls;
        } else{
            s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                    ";"+ ls;
        }
        s += "\tcallbackslot = " + callbackslot + ";"+ ls;
        s += "\tcolor0 = " + color[0] + ";"+ ls;
        s += "\tdedtype = ";
        if (dedtype == DED_DED){
            s += "DED";
        }
        else {
            s += "PFL";
        }
        s += ";" + ls;        
        s += "\tdestloc = " + destloc[0] + ";"+ ls;
        s += "#END"+ ls;
        
        return s;
    }
    
    
    public void draw(Graphics2D g) {
        //save color
        Color c = g.getColor();
        
        //we only draw a rectangle with the ded word
        destloc[0].draw(g);
        
        //we draw text with DED color if it exists
        if ((this.color != null) && (this.color[0] != null)){
            g.setColor(this.color[0].toColor());            
        }        
        if (dedtype == DED_DED){
            destloc[0].drawText("DED", g);
        }
        else {
            destloc[0].drawText("DED (PFL)", g);
        }
        

        //return to old color
        g.setColor(c);
    }

    public void drawLabeled(Graphics2D g) {
        //save old color
        Color c = g.getColor();
        //we only draw a rectangle with the ded word
        destloc[0].draw(g);

        //we draw text with DED color if it exists
        if ((this.color != null) && (this.color[0] != null)){
            g.setColor(this.color[0].toColor());            
        }
        
        //if we have label, draw it...
        if (label!=null){
            destloc[0].drawText("DED" +  label, g);
        }
        else{
            destloc[0].drawText("DED", g);
        }
        
        //return to old color
        g.setColor(c);
    }

    protected PropertiesDialog getPropertiesDialog(JFrame parent){
        DedPropDialog dpd = new DedPropDialog(parent, this);
        return dpd;
    }
	
    protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
        DedPropDialog dpd = new DedPropDialog(parent, this);
        return dpd;
    }
}

class DedPropDialog extends PropertiesDialog {
    PitDED ded;
    JTabbedPane tp = new JTabbedPane();

    //main
    //LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);
    ColorField color = new ColorField(0);
    
    JPanel dedPanel = new JPanel();
    ButtonGroup dedTypeGroup = new ButtonGroup();
    JRadioButton dedPFL = new JRadioButton("PFL");
    JRadioButton dedDED = new JRadioButton("DED");
    
    LabeledField dedtype = new LabeledField(this, "dedtype", 8);
    //LabeledNumField callbackslot = new LabeledNumField(this, "callbackslot", 8);
    AreasFieldPanel destloc = new AreasFieldPanel(this, "destloc");

    //comments
    CommentsPane comments = new CommentsPane(this);

    
    public DedPropDialog(JFrame parent, PitDED ded){
        super(parent, ded, "DED properties", false);
        setProperties(ded);
    }

    public DedPropDialog(JDialog parent, PitDED ded){
        super(parent, ded, "DED properties", true);
        setProperties(ded);
    }
    
    
    private void setProperties(PitDED ded){

        this.ded = ded;
        this.setSize(400,400);
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("DED #" + ded.id, JLabel.CENTER));

            //mainP.add(callbackslot);
            //mainP.add(cyclebits);

            dedTypeGroup.add(dedPFL);
            dedTypeGroup.add(dedDED);
            dedPanel.add(dedPFL);
            dedPanel.add(dedDED);
            mainP.add(dedPanel);
            mainP.add(color);
            mainP.add(destloc);

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
        //callbackslot.setValue(ded.callbackslot);        
        //cyclebits.setValue(ded.cyclebits);
        if (ded.dedtype == PitDED.DED_DED){
            dedDED.setSelected(true);
        }
        else {
            dedPFL.setSelected(true);
        }
        color.setColor(ded.color[0]);
        destloc.setFields(ded.destloc[0]);


        //comments
        comments.setComments(ded.comments);
    }

    protected void setValues() throws Exception {
		//update main
		{
			//ded.callbackslot = callbackslot.getValue();
			ded.callbackslot = -1;                
			//ded.cyclebits = cyclebits.getValue();
			ded.cyclebits = 0xFFFF;
			ded.dedtype = dedPFL.isSelected() ? PitDED.DED_PFL : PitDED.DED_DED;
			ded.color[0] = color.getColor();
			ded.destloc[0] = destloc.getArea();
		}
		//update comments
		{
			ded.comments = comments.getComments();
			if (ded.comments != null){
				ded.label = ded.comments.getLabel();
			}
		}
    }
}


