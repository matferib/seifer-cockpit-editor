package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import windows.PropertiesDialog;

public class PitMachASI
  extends PitObject {

    public int needleradius;
    float endangle, endlength, minval, maxval, startangle, arclength;

    public PitMachASI(int id) {
        super(id, 0, 1);
        color = new PitColor[2];
    }

    public Object clone() {

        PitMachASI ma = new PitMachASI(0);
        this.cloneMeTo(ma);
        ma.needleradius = this.needleradius;
        ma.endangle = this.endangle;
        ma.endlength = this.endlength;
        ma.minval = this.minval;
        ma.startangle = this.startangle;
        ma.arclength = this.arclength;
        return ma;
    }


    public void parseData(PitTokenizer st) throws ParseErrorException {
        int numColorsRead = 0;
        
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
                
                else if (token.equals("destloc")) {
                    destloc[0] = new PitArea();
                    destloc[0].parseData(st);
                }
                else if (token.equals("srcloc")) {
                    setNumSrcs(1);
                    srcloc[0] = new PitArea();
                    srcloc[0].parseData(st);
                }
                
                else if (token.equals("callbackslot")) {
                    callbackslot = (int) st.nval;
                }
                else if (token.equals("persistant")) {
                    persistant = (int) st.nval;
                }
                else if (token.equals("cyclebits")) {
                    cyclebits = st.parseHex();
                }
                else if (token.startsWith("color")) {
                    if (numColorsRead + 1 > 2) {
                        throw new ParseErrorException("more than 2 colors read");
                    }
                    color[numColorsRead++] = st.parseColor();
                }
                else if (token.startsWith("needleradius")) {
                    needleradius = (int) st.nval;
                }
                else if (token.startsWith("minval")) {
                    minval = (float) st.nval;
                }
                else if (token.startsWith("maxval")) {
                    maxval = (float) st.nval;
                }
                //				else if (token.equals("renderneedle")) {
                //					renderneedle = (int) st.nval;
                //				}
                else if (token.startsWith("startangle")) {
                    startangle = (float) st.nval;
                }
                
                else if (token.startsWith("arclength")) {
                    arclength = (float) st.nval;
                }
                else if (token.startsWith("endlength")) {
                    endlength = (float) st.nval;
                }
                else if (token.startsWith("endangle")) {
                    endangle = (float) st.nval;
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
        try {
            if (comments != null){
                comments.write(arq);
            }
            arq.writeln("#" + id + " MACHASI");
            arq.writeln("\tcallbackslot = " + callbackslot + ";");
            if (cyclebits == -1){
                arq.writeln("\tcyclebits = -1;");
            }
            else{
                arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                ";");
            }
            arq.writeln("\tminval = " + minval + ";");
            arq.writeln("\tmaxval = " + maxval + ";");
            arq.writeln("\tstartangle = " + startangle + ";");
            arq.writeln("\tarclength = " + arclength + ";");
            arq.writeln("\tneedleradius = " + needleradius + ";");
            arq.writeln("\tcolor0 = " + color[0] + ";");
            arq.writeln("\tcolor1 = " + color[1] + ";");
            arq.writeln("\tendlength = " + endlength + ";");
            arq.writeln("\tendangle = " + endangle + ";");
            arq.writeln("\tdestloc = " + destloc[0] + ";");
            arq.writeln("\tpersistant = " + persistant + ";");
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitSound.java-> erro de IO: " + io);
        }
    }
    
    public String toTextFormat(){
        String s = new String();
        if (comments != null){
            s += comments;
        }
        s += "#" + id + " MACHASI" + ls;
        s += "\tcallbackslot = " + callbackslot + ";" + ls;
        if (cyclebits == -1){
            s += "\tcyclebits = -1;" + ls;
        }
        else{
            s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +";" + ls;
        }
        s += "\tminval = " + minval + ";" + ls;
        s +="\tmaxval = " + maxval + ";" + ls;
        s +="\tstartangle = " + startangle + ";" + ls;
        s +="\tarclength = " + arclength + ";" + ls;
        s +="\tneedleradius = " + needleradius + ";" + ls;
        s +="\tcolor0 = " + color[0] + ";" + ls;
        s +="\tcolor1 = " + color[1] + ";" + ls;
        s +="\tendlength = " + endlength + ";" + ls;
        s +="\tendangle = " + endangle + ";" + ls;
        s +="\tdestloc = " + destloc[0] + ";" + ls;
        s +="\tpersistant = " + persistant + ";" +ls;
        s +="#END" + ls;
        
        return s;        
    }
    
    //well draw the dial needle
    public void draw(Graphics2D g) {
        //dest coords
        int uplx = destloc[0].upl.x;
        int uply = destloc[0].upl.y;
        int dorx = destloc[0].dor.x + 1;
        int dory = destloc[0].dor.y + 1;
        
        //the needle
        Polygon needle = new Polygon();
        needle.addPoint( (uplx + dorx) / 2, (uply + dory) / 2 + 4);
        needle.addPoint( (uplx + dorx) / 2, (uply + dory) / 2 - 4);
        needle.addPoint(uplx + 5, (uply + dory) / 2);
        
        //draw needle
        g.fill(needle);
        
        //the circle
        g.drawArc(uplx, uply,
        destloc[0].rectangle.width, destloc[0].rectangle.height,
        0, 360);
    }
    //well draw the dial needle
    public void drawLabeled(Graphics2D g) {
        //dest coords
        int uplx = destloc[0].upl.x;
        int uply = destloc[0].upl.y;
        int dorx = destloc[0].dor.x + 1;
        int dory = destloc[0].dor.y + 1;
        
        //the needle
        Polygon needle = new Polygon();
        needle.addPoint( (uplx + dorx) / 2, (uply + dory) / 2 + 4);
        needle.addPoint( (uplx + dorx) / 2, (uply + dory) / 2 - 4);
        needle.addPoint(uplx + 5, (uply + dory) / 2);
        
        //draw needle
        g.fill(needle);
        
        //the circle
        g.drawArc(uplx, uply,
        destloc[0].rectangle.width, destloc[0].rectangle.height,
        0, 360);
        if (label != null){
            destloc[0].drawText(label, g);
        }
    }

	protected PropertiesDialog getPropertiesDialog(JFrame parent) {
		MachPropDialog mpd = new MachPropDialog(parent, this);
		return mpd;
	}

	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		MachPropDialog mpd = new MachPropDialog(parent, this);
		return mpd;
	}

}

class MachPropDialog extends PropertiesDialog {
    PitMachASI asi;
    JTabbedPane tp = new JTabbedPane();

    //main
    LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);
    LabeledNumField callbackslot = new LabeledNumField(this, "callbackslot", 8);
    LabeledCBox persistant = new LabeledCBox("persistant");

    //areas
    AreasFieldPanel dest = new AreasFieldPanel(this, "destloc");

    //points
    LabeledFloatFields minval =  new LabeledFloatFields(this, "minval", 1, 8);
    LabeledFloatFields maxval =  new LabeledFloatFields(this, "maxval", 1, 8);
    LabeledFloatFields startangle =  new LabeledFloatFields(this, "startangle", 1, 8);
    LabeledFloatFields endangle =  new LabeledFloatFields(this, "endangle", 1, 8);
    LabeledFloatFields arclength =  new LabeledFloatFields(this, "arclength", 1, 8);
    LabeledFloatFields needleradius = new LabeledFloatFields(this, "needleradius", 1, 5);
    LabeledFloatFields endlength = new LabeledFloatFields(this, "endlength", 1, 5);

    //colors
    ColorField c0 = new ColorField("color0", "needle color");
    ColorField c1 = new ColorField("color1", "needle shade color");

    //comments
    CommentsPane comments = new CommentsPane(this);
    public MachPropDialog(JFrame parent, PitMachASI asi){
        super(parent, asi, "MachASI properties", false);
        setProperties(asi);
    }

    public MachPropDialog(JDialog parent, PitMachASI asi){
        super(parent, asi, "MachASI properties", true);
        setProperties(asi);
    }
    
    private void setProperties(PitMachASI asi){
        this.asi = asi;
        this.setSize(400,400);
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("MACHASI #" + asi.id, JLabel.CENTER));

            mainP.add(callbackslot);
            mainP.add(cyclebits);
            mainP.add(persistant);

            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");
        }
        //areas
        {
            JPanel areaP = new JPanel(new GridLayout(8,1));

            areaP.add(dest);

            tp.add("areas", areaP);
            tp.setToolTipTextAt(1, "areas");
        }
        //points
        {
            JPanel pp = new JPanel(new GridLayout(8,1));

            pp.add(minval);
            pp.add(maxval);
            pp.add(startangle);
            pp.add(endangle);
            pp.add(needleradius);
            pp.add(endlength);
            pp.add(arclength);

            tp.add("points", pp);
            tp.setToolTipTextAt(2, "points");
        }
        //colors
        {
            JPanel cp = new JPanel(new GridLayout(8,1));

            cp.add(c0);
            cp.add(c1);

            tp.add("colors", cp);
            tp.setToolTipTextAt(3, "colors for this MachASI");

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
        callbackslot.setValue(asi.callbackslot);
        cyclebits.setValue(asi.cyclebits);
        persistant.setSelected((asi.persistant == 1));

        //areas
        dest.setFields(asi.destloc[0]);

        //points
        startangle.setFields(new float[]{asi.startangle});
        endangle.setFields(new float[]{asi.endangle});
        maxval.setFields(new float[]{asi.maxval});
        minval.setFields(new float[]{asi.minval});
        needleradius.setFields(new float[]{asi.needleradius});
        endlength.setFields(new float[]{asi.endlength});
        arclength.setFields(new float[]{asi.arclength});

        //colors
        c0.setColor(asi.color[0]);
        c1.setColor(asi.color[1]);

        //comments
        comments.setComments(asi.comments);
    }

    protected void setValues() throws Exception {
		//update main
		asi.callbackslot = callbackslot.getValue();
		asi.cyclebits = cyclebits.getValue();
		if (persistant.isSelected()) {
			asi.persistant = 1;
		}
		else {
			asi.persistant = 0;
		}

		//update areas
		asi.destloc[0] = dest.getArea();

		//update points
		asi.arclength = arclength.getField(0);
		asi.startangle = startangle.getField(0);
		asi.endangle = endangle.getField(0);
		asi.needleradius = (int)needleradius.getField(0);
		asi.minval = minval.getField(0);
		asi.maxval = maxval.getField(0);
		asi.endlength = endlength.getField(0);

		//update colors
		asi.color[0] = c0.getColor();
		asi.color[1] = c1.getColor();

		//update comments
		asi.comments = comments.getComments();
		if (asi.comments != null){
			asi.label = asi.comments.getLabel();
		}
    }
}


