package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import windows.PropertiesDialog;

//HSI is so full of shit!
public class PitHSI
extends PitObject {
    private int numColorsRead = 0; //used for parsing

    public PitHSI(int id) {
        // 2 srclocs and 3 destlocs + 1 warnflag
        super(id, 2, 4);
        color = new PitColor[10];
    }
    public Object clone(){
        PitHSI hsi = new PitHSI(0);
        this.cloneMeTo(hsi);
        //hsi.warnflag = PitArea.clone(warnflag);
        return hsi;
    }
    
    public String check(){
        String s = "";
        
        if (destloc[0].rectangle.width != destloc[0].rectangle.height){
            s = s.concat("Warning: hsi " + id + " destloc0 is not a square\n");
        }
        if (srcloc[0].rectangle.width != srcloc[0].rectangle.height){
            s = s.concat("Warning: hsi " + id + " srcloc0 is not a square\n");            
        }
        
        return s;
    }
    
    public void draw(Graphics2D g){
        if ((srcloc[0] != null) && (destloc[0] != null)){
            drawSrcDst(g, 0, 0);
        }
        if ((srcloc[1] != null) && (destloc[1] != null)){
            drawSrcDst(g, 1, 1);
        }
        if (destloc[2] != null){
            destloc[2].draw(g);
        }

        if (destloc[3] != null){
            destloc[3].draw(g);
            destloc[3].drawText("Warnflag", g);
        }
    }
    
    public void parseData(PitTokenizer st) throws ParseErrorException {
        int numSrcRead = 0, numDstRead = 0;
        
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
                if ( (!token.endsWith("loc")) && (st.ttype != st.TT_NUMBER)) {
                    throw new ParseErrorException("expecting number token");
                }
                
                else if (token.equals("warnflag")) {
                    destloc[3] = new PitArea();
                    destloc[3].parseData(st);
                }
                else if (token.equals("srcloc")) {
                    if (numSrcRead  >= 2) {
                        throw new ParseErrorException("more than 2 srclocs");
                    }
                    srcloc[numSrcRead] = new PitArea(false, true);
                    srcloc[numSrcRead++].parseData(st);
                }
                else if (token.equals("destloc")) {
                    if (numDstRead + 1 > 3) {
                        throw new ParseErrorException("more than 3 dstlocs");
                    }
                    destloc[numDstRead] = new PitArea();
                    destloc[numDstRead++].parseData(st);
                }
                else if (token.equals("callbackslot")) {
                    callbackslot = (int) st.nval;
                }
                else if (token.startsWith("color")) {
                    if (numColorsRead > 9)
                        throw new ParseErrorException("more than 9 colors");
                    color[numColorsRead] = new PitColor();
                    color[numColorsRead] = st.parseColor();
                    numColorsRead++;
                }
                else if (token.equals("persistant")) {
                    persistant = (int) st.nval;
                }
                else if (token.equals("cyclebits")) {
                    cyclebits = st.parseHex();
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
            throw new ParseErrorException("pitMachASI.java: " + e);
        }
        
    }
    
    public void writeComponent(misc.RAFile arq) {
        int i = 0;
        try {
            if (comments != null){
                comments.write(arq);
            }
            arq.writeln("#" + id + " HSI");
            arq.writeln("\tcallbackslot = " + callbackslot + ";");
            if (cyclebits == -1){
                arq.writeln("\tcyclebits = -1;");
            }
            else{
                arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                ";");
            }
            
            arq.writeln("\tpersistant = " + persistant + ";");
            if (destloc[3] != null) {
                arq.writeln("\twarnflag = " + destloc[3] + ";");
            }
            for (i = 0; i < 3; i++) {
                if (destloc[i] != null) {
                    arq.writeln("\tdestloc = " + destloc[i] +
                    ";");
                }
            }
            for (i = 0; i < 2; i++) {
                if (srcloc[i] != null) {
                    arq.writeln("\tsrcloc = " + srcloc[i] +
                    ";");
                }
            }
            for (i = 0; i < color.length; i++) {
                if (color[i] != null) {
                    arq.writeln("\tcolor"+i + " = " + color[i].toString() + ";");
                }
            }
            
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitHSI.java-> erro de IO: " + io);
            
        }
    }
    public String toTextFormat(){
        int i = 0;
        String s= new String();
        if (comments != null){
            s += comments;
        }
        s += "#" + id + " HSI"+ ls;
        s += "\tcallbackslot = " + callbackslot + ";"+ ls;
        if (cyclebits == -1){
            s += "\tcyclebits = -1;"+ ls;
        } else{
            s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                    ";"+ ls;
        }
        
        s += "\tpersistant = " + persistant + ";"+ ls;
        if (destloc[3] != null) {
            s += "\twarnflag = " + destloc[3] + ";"+ ls;
        }
        for (i = 0; i < 3; i++) {
            if (destloc[i] != null) {
                s += "\tdestloc = " + destloc[i] +
                        ";"+ ls;
            }
        }
        for (i = 0; i < 2; i++) {
            if (srcloc[i] != null) {
                s += "\tsrcloc = " + srcloc[i] +
                        ";"+ ls;
            }
        }
        for (i = 0; i < color.length; i++) {
            if (color[i] != null) {
                s += "\tcolor"+i + " = " + color[i].toString() + ";"+ ls;
            }
        }
        
        s += "#END"+ ls;
        return s;
    }
    
	protected PropertiesDialog getPropertiesDialog(JFrame parent){
		HsiPropDialog hpd = new HsiPropDialog(parent, this);
		return hpd;
	}
	
	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		HsiPropDialog hpd = new HsiPropDialog(parent, this);
		return hpd;
	}    
}

class HsiPropDialog extends PropertiesDialog {
    PitHSI hsi;
    JTabbedPane tp = new JTabbedPane();

    //main
    LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);
    LabeledNumField callbackslot = new LabeledNumField(this, "callbackslot", 8);
    LabeledCBox persistant = new LabeledCBox("persistant");

    //areas, 3 dest, 2 src and one warnflag
    AreasFieldPanel src[] = new AreasFieldPanel[2];
    AreasFieldPanel dest[] = new AreasFieldPanel[3];
    AreasFieldPanel warnflag = new AreasFieldPanel(this, "warnflag");

    //colors, 10 colors
    ColorField colors[] = new ColorField[10];
    static String colorLabel[] = {"color0", "color1", "color2", "color3", "color4",
        "color5", "color6", "color7", "color8", "color9"
    };
    static String colorTip[] = {"color0", "color1", "color2", "color3", "color4",
        "color5", "color6", "color7", "color8", "color9"
    };
    //are we using colors?
    JCheckBox useColors = new JCheckBox("Use colors?");

    //comments
    CommentsPane comments = new CommentsPane(this);

    public HsiPropDialog(JFrame parent, PitHSI hsi){
        super(parent, hsi, "HSI Properties", false);
        setProperties(hsi);
    }

    public HsiPropDialog(JDialog parent, PitHSI hsi){
        super(parent, hsi, "HSI Properties", true);
        setProperties(hsi);
    }
    
    private void setProperties(PitHSI hsi){

        this.hsi = hsi;
        this.setSize(400,400);
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("HSI #" + hsi.id, JLabel.CENTER));

            mainP.add(callbackslot);
            mainP.add(cyclebits);
            mainP.add(persistant);

            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");
        }
        //src areas
        {
            JPanel sareaP = new JPanel(new GridLayout(8,1));
            sareaP.add(new JLabel("Sources", JLabel.CENTER));

            src[0] = new AreasFieldPanel(this, "src0", false);
            src[1] = new AreasFieldPanel(this, "src1", false);

            sareaP.add(src[0]);
            sareaP.add(src[1]);

            tp.add("src", sareaP);
            tp.setToolTipTextAt(1, "source areas");
        }
        //dest areas
        {
            JPanel dareaP = new JPanel(new GridLayout(8,1));
            dareaP.add(new JLabel("Destinations", JLabel.CENTER));

            dest[0] = new AreasFieldPanel(this, "dst0");
            dest[1] = new AreasFieldPanel(this, "dst1");
            dest[2] = new AreasFieldPanel(this, "dst2");

            dareaP.add(dest[0]);
            dareaP.add(dest[1]);
            dareaP.add(dest[2]);

            tp.add("dest", dareaP);
            tp.setToolTipTextAt(2, "destination areas");
        }
        //warnflag area
        {
            JPanel wareaP = new JPanel(new GridLayout(8,1));
            wareaP.add(new JLabel("Warnflag", JLabel.CENTER));

            warnflag = new AreasFieldPanel(this, "wfg");

            wareaP.add(warnflag);

            tp.add("warnf", wareaP);
            tp.setToolTipTextAt(2, "warnflag");
        }
        //color fields
        {
            JPanel pan = new JPanel(new GridLayout(colors.length+1, 1));
            pan.add(useColors);
            JScrollPane sp = new JScrollPane(pan);
            for (int i=0;i<10;i++){
                colors[i] = new ColorField(colorLabel[i], colorTip[i]);
                pan.add(colors[i]);   
            }
            tp.add("colors", sp);
            tp.setToolTipTextAt(3, "HSI colors");

        }
        //comments
        {
            tp.add("comms", comments);
            tp.setToolTipTextAt(4, "comments");
        }

        c.add(tp, BorderLayout.CENTER);

        if (!isNew)
            loadValues();

    }

    public void loadValues(){
        //main
        callbackslot.setValue(hsi.callbackslot);
        cyclebits.setValue(hsi.cyclebits);
        persistant.setSelected((hsi.persistant == 1));

        //src areas
        src[0].setFields(hsi.srcloc[0]);
        src[1].setFields(hsi.srcloc[1]);

        //dest areas
        dest[0].setFields(hsi.destloc[0]);
        dest[1].setFields(hsi.destloc[1]);
        dest[2].setFields(hsi.destloc[2]);

        //warnflag areas
        warnflag.setFields(hsi.destloc[3]);

        //colors
        boolean use = true;
        for (int i=0;i<colors.length;i++){
            if (hsi.color[i] == null){
                use = false;
            }
            colors[i].setColor(hsi.color[i]);            
        }
        useColors.setSelected(use);

        //comments
        comments.setComments(hsi.comments);
    }

    protected void setValues() throws Exception {
		//update main
		hsi.callbackslot = callbackslot.getValue();
		hsi.cyclebits = cyclebits.getValue();
		if (persistant.isSelected()) {
			hsi.persistant = 1;
		}
		else {
			hsi.persistant = 0;
		}

		//src areas
		hsi.srcloc[0] = src[0].getArea();
		hsi.srcloc[1] = src[1].getArea();

		//dest
		hsi.destloc[0] = dest[0].getArea();
		hsi.destloc[1] = dest[1].getArea();
		hsi.destloc[2] = dest[2].getArea();

		//warnflag
		hsi.destloc[3] = warnflag.getArea();

		//colors
		for (int i=0;i<colors.length;i++){
			if (useColors.isSelected()){
				hsi.color[i] = colors[i].getColor();
			}
			else {
				hsi.color[i] = null;
			}
		}

		//update comments
		hsi.comments = comments.getComments();
		if (hsi.comments != null){
				hsi.label = hsi.comments.getLabel();
		}
    }
}
