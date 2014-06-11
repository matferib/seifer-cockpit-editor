package pitComponents.pitObjects;

import cockpitEditor.UsPr;
import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.swing.*;
import windows.PropertiesDialog;

public class PitDial extends PitObject {

	public int numendpoints;
	public float radius[];
	private int numRadRead = 0; //just for control
	public float values[], points[];
	public int renderneedle = 0;
	public int currentPoint = 0; //the position we are drawing

	public PitDial(int id) {
		super(id, 1, 1);
		radius = new float[3];
		color = new PitColor[3];
	}

    public Object clone(){
        PitDial dial = new PitDial(0);
        super.cloneMeTo(dial);
        dial.numendpoints = numendpoints;
        dial.radius = (float[])radius.clone();
        dial.numRadRead = numRadRead;
        dial.values = (float[])values.clone();
        dial.points = (float[])points.clone();
        dial.renderneedle = renderneedle;
        dial.currentPoint = currentPoint;
        return dial;
    }

    public String check(){
        String s="";

        if (points.length == 0){
            s = s.concat("WARNING: no points for needle " + id + "\n");
        }
        if  (values.length == 0){
            s = s.concat("WARNING: no values for needle " + id + "\n");
        }
        if  ((renderneedle == 1) && 
            ((srcloc[0].upl.y < Cockpit.resolution.height)&&
            (srcloc[0].upl.x < Cockpit.resolution.width))){
            //according to aeyes, srcloc must be outside cockpit resolution
            s = s.concat("WARNING: srcloc for dial "+ id +" is inside panel resolution\n");
        }
        return s;
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
                if ( (!token.endsWith("loc")) && (st.ttype != st.TT_NUMBER)) {
                    throw new ParseErrorException("expecting number token");
                }
                
                if (token.equals("srcloc")) {
                    srcloc[0] = new PitArea(false, true);
                    srcloc[0].parseData(st);
                }
                else if (token.equals("destloc")) {
                    destloc[0] = new PitArea(false, true);
                    destloc[0].parseData(st);
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
                else if (token.equals("numendpoints")) {
                    numendpoints = (int) st.nval;
                    values = new float[numendpoints];
                    points = new float[numendpoints];
                    
                    for (int i = 0; i < numendpoints; i++) {
                        values[i] = -2000; //flag to indicate its not used
                        points[i] = -2;
                    }
                }
                else if (token.startsWith("color")) {
                    int which = Integer.parseInt(token.substring(5));
                    color[which] = new PitColor();
                    color[which] = st.parseColor();
                }
                else if (token.startsWith("radius")) {
                    radius[numRadRead++] = (float) st.nval;
                }
                else if (token.equals("renderneedle")) {
                    renderneedle = (int) st.nval;
                }
                else if (token.equals("values")) {
                    values[0] = (float) st.nval;
                    for (int i = 1; i < numendpoints; i++) {
                        st.nextToken();
                        if (st.ttype != st.TT_NUMBER) {
                            throw new Exception("Expecting values");
                        }
                        values[i] = (float) st.nval;
                    }
                }
                else if (token.equals("points")) {
                    points[0] = (float) st.nval;
                    for (int i = 1; i < numendpoints; i++) {
                        st.nextToken();
                        if (st.ttype != st.TT_NUMBER) {
                            throw new Exception("Expecting points");
                        }
                        points[i] = (float) st.nval;
                    }
                }
                else {
                    //found junk
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
            throw new ParseErrorException("pitDial.java: " + e);
        }
        
    }
    
    public void writeComponent(misc.RAFile arq) {
        int i = 0;
        try {
            if (comments != null){
                comments.write(arq);
            }
            arq.writeln("#" + id + " DIAL");
            if (cyclebits == -1){
                arq.writeln("\tcyclebits = -1;");
            }
            else{
                arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                ";");
            }
            arq.writeln("\tnumendpoints = " + numendpoints + ";");
            arq.writeBytes("\tpoints =");
            for (i = 0; i < numendpoints; i++) {
                arq.writeBytes(" " + points[i]);
            }
            arq.writeln(";");
            arq.writeBytes("\tvalues =");
            for (i = 0; i < numendpoints; i++) {
                arq.writeBytes(" " + values[i]);
            }
            arq.writeln(";");
            for (i = 0; i < numRadRead; i++) {
                arq.writeln("\tradius" + i + " = " + radius[i] + ";");
            }
            for (i = 0; i < 3; i++) {
                if (color[i] != null)
                    arq.writeln("\tcolor" + i + " = " + color[i] + ";");
            }
            if (renderneedle == 1){
                arq.writeln("\trenderneedle = 1;");
                arq.writeln("\tsrcloc = " + srcloc[0] + ";");
            }
            else{
                //this is NO bug!!! falcon is stupid, if the needle is not rendered
                //the srcloc values must be equal to destloc
                arq.writeln("\tsrcloc = " + destloc[0] + ";");
            }
            arq.writeln("\tdestloc = " + destloc[0] + ";");
            arq.writeln("\tcallbackslot = " + callbackslot + ";");
            arq.writeln("\tpersistant = " + persistant + ";");
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitDial.java-> erro de IO: " + io);
            
        }
    }

    public String toTextFormat() {
        String s = new String();
        int i = 0;
        if (comments != null){
            s += comments;
        }
        s += "#" + id + " DIAL"+ ls;
        if (cyclebits == -1){
            s += "\tcyclebits = -1;"+ ls;
        } else{
            s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                    ";"+ ls;
        }
        s += "\tnumendpoints = " + numendpoints + ";"+ ls;
        s += "\tpoints =";
        for (i = 0; i < numendpoints; i++) {
            s += " " + points[i];
        }
        s += ";"+ ls;
        s += "\tvalues =";
        for (i = 0; i < numendpoints; i++) {
            s += " " + values[i];
        }
        s += ";"+ ls;
        for (i = 0; i < numRadRead; i++) {
            s += "\tradius" + i + " = " + radius[i] + ";"+ ls;
        }
        for (i = 0; i < 3; i++) {
            if (color[i] != null)
                s += "\tcolor" + i + " = " + color[i] + ";"+ ls;
        }
        if (renderneedle == 1){
            s += "\trenderneedle = 1;"+ ls;
            s += "\tsrcloc = " + srcloc[0] + ";"+ ls;
        } else{
            //this is NO bug!!! falcon is stupid, if the needle is not rendered
            //the srcloc values must be equal to destloc
            s += "\tsrcloc = " + destloc[0] + ";"+ ls;
        }
        s += "\tdestloc = " + destloc[0] + ";"+ ls;
        s += "\tcallbackslot = " + callbackslot + ";"+ ls;
        s += "\tpersistant = " + persistant + ";"+ ls;
        s += "#END"+ ls;
        
        return s;
    }
       
    //well draw the dial needle
    public void draw(Graphics2D g) {        
        drawNeedle(g);

        //the circle
        g.drawArc(destloc[0].upl.x, destloc[0].upl.y,
        destloc[0].rectangle.width, destloc[0].rectangle.height,
        0, 360);
    }
    
    //well draw the dial needle
    public void drawLabeled(Graphics2D g) {
        drawNeedle(g);
        
        //the circle
        g.drawArc(destloc[0].upl.x, destloc[0].upl.y,
        destloc[0].rectangle.width, destloc[0].rectangle.height,
        0, 360);
        if (label != null){
            destloc[0].drawText(label, g);
        }
    }
      
	/** draws the dial needle. Its composed of 2 triangles. The radius values are:
	 * - radius0: radius of the back of the arrow
	 * - radius1: radius of the front of the arrow
	 * - radius2: distance from center to the side of arrow, 45 degree inclination
	 */
    private void drawNeedle(Graphics2D g){
		final double COSSIN45 = 0.7071;
        if ((points.length == 0) || (values.length == 0)){
            return;
        }

        //dest coords
        int uplx = destloc[0].upl.x;
        int uply = destloc[0].upl.y;
        int dorx = destloc[0].dor.x + 1;
        int dory = destloc[0].dor.y + 1;
        //src coords
        int suplx = srcloc[0].upl.x;
        int suply = srcloc[0].upl.y;
        int sdorx = srcloc[0].dor.x + 1;
        int sdory = srcloc[0].dor.y + 1;
        int srcw = sdorx - suplx; //srcwidth
        int srch = sdory - suply; //srcheight
        //we have to translate all points 90 degree ccw(Pi / 2), and falcon uses CCW rotation
        AffineTransform a = AffineTransform.getRotateInstance(
          - points[currentPoint] + (Math.PI / 2.0), (uplx + dorx) / 2, (uply + dory) / 2
		);
        a.translate(uplx, uply);

        //rendered needles are always transparent
        Image image =  Cockpit.template.getImage(PitTemplate.IM_FLOOD_INST_T);
        
        if ((renderneedle == 1) && (image != null)){
            Rectangle r = destloc[0].rectangle;
            Rectangle r2 = srcloc[0].rectangle;
            BufferedImage sub = ((BufferedImage)image).getSubimage(
                    r2.x, r2.y, r2.width, r2.height);
            Image sub2 = sub.getScaledInstance(
                    r.width, r.height, BufferedImage.SCALE_FAST);
            g.drawImage(sub2, a, null);
        }
        else {
            //we need the corners at 0,0
            int cx= (uplx  + dorx)/2 - uplx, cy = (uply + dory)/2- uply;

            //the needle pointing up, well transform it later
            Polygon needle = new Polygon();
            needle.addPoint( cx, cy - (int)radius[1]); //up
            needle.addPoint( cx, cy - (int)radius[0]); // down
            needle.addPoint( cx + (int)(radius[2] * COSSIN45), cy - (int)(radius[2] * COSSIN45)); //left
            Polygon needleShade = new Polygon();
            needleShade.addPoint(cx,cy - (int)radius[1]); //up
            needleShade.addPoint(cx, cy- (int)radius[0]); //center
            needleShade.addPoint(cx - (int)(radius[2] * COSSIN45), cy - (int)(radius[2] * COSSIN45)); //right

            //draw needle
            Color c = g.getColor(); //save color
            Shape needleS = a.createTransformedShape(needle);
            Shape needleSh = a.createTransformedShape(needleShade);

            if (color[2] != null){
                g.setColor(this.color[2].toColor());
			}
            g.fill(needleSh);
            if (color[1] != null){
                g.setColor(this.color[1].toColor());
			}
            g.fill(needleS);

            g.setColor(c);
        }

    }


    /** Override next state, as dials dont have states, but current point
     */        
    public void nextState(){
        currentPoint++;
        if (currentPoint >= numendpoints){
            currentPoint = 0;
        }
    }

    /** Override previous state, as dials dont have states, but current point
     */        
    public void previousState(){
        currentPoint--;
        if (currentPoint < 0){
            currentPoint = numendpoints - 1;
        }        
    }         

    /** Override reset state, as dials dont have states, but current point
     */        
    public void resetState(){
        currentPoint = 0;
    }             
    
	protected PropertiesDialog getPropertiesDialog(JFrame parent){
		DialPropDialog dpd = new DialPropDialog(parent, this);
		return dpd;
	}

	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		DialPropDialog dpd = new DialPropDialog(parent, this);
		return dpd;
	}
}

class DialPropDialog extends PropertiesDialog {
    JTabbedPane tp = new JTabbedPane();

    PitDial dial;
    
    //main
    LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);
    LabeledNumField callbackslot = new LabeledNumField(this, "callbackslot", 8);
    LabeledCBox persistant = new LabeledCBox("persistant");

    //areas
    LabeledCBox rneedle = new LabeledCBox("renderneedle");
    AreasFieldPanel src = new AreasFieldPanel(this, "srcloc", false);
    AreasFieldPanel dest = new AreasFieldPanel(this, "destloc", false);

    //points
    LabeledFloatFields values= new LabeledFloatFields(this, "values", PitDial.MAX, 8);
    LabeledFloatFields points= new LabeledFloatFields(this, "angles", PitDial.MAX, 8);
	
	LabeledFloatFields radius = new LabeledFloatFields(this, "radius", 3, 5);
    
    //colors
    ColorField c0 = new ColorField("color0", "unknown");
    ColorField c1 = new ColorField("color1", "needle color");
    ColorField c2 = new ColorField("color2", "needle shading");

    //comments
    CommentsPane comments = new CommentsPane(this);

    public DialPropDialog(JFrame parent, PitDial dial){
        super(parent, dial, "Dial Properties", false);
        setProperties(dial);
    }
    
    public DialPropDialog(JDialog parent, PitDial dial){
        super(parent, dial, "Dial Properties", true);
        setProperties(dial);
    }
    
    private void setProperties(PitDial dial){
        enableStateButtons();
        this.dial = dial;
        this.setSize(400,400);
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("Dial #" + dial.id, JLabel.CENTER));

            mainP.add(callbackslot);
            mainP.add(cyclebits);
            mainP.add(persistant);

            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");
        }
        //areas
        {
            JPanel areaP = new JPanel(new GridLayout(8,1));

            areaP.add(rneedle);
            rneedle.addActionListener(this);
            areaP.add(src);
            areaP.add(dest);
            areaP.add(radius);

            src.setEnabled(false);

            tp.add("areas", areaP);
            tp.setToolTipTextAt(1, "areas");
        }
        //points and values
        {
            JPanel pointsAndValuesPanel = new JPanel(new BorderLayout());
            pointsAndValuesPanel.add(new JLabel("Values and angles", JLabel.CENTER), BorderLayout.NORTH);

			points.setToolTipText("angle in degrees for the corresponding value");
            JPanel centerPanel = new JPanel(new GridLayout(8, 1));
			centerPanel.add(values);
			centerPanel.add(points);

            JScrollPane centerPaneScroll = new JScrollPane(centerPanel);
            pointsAndValuesPanel.add(centerPaneScroll, BorderLayout.CENTER);

            tp.add("points", pointsAndValuesPanel);
            tp.setToolTipTextAt(2, "Values and angles");
        }
        //colors
        {
            JPanel cp = new JPanel(new GridLayout(8,1));

            cp.add(c0);
            cp.add(c1);
            cp.add(c2);

            tp.add("colors", cp);
            tp.setToolTipTextAt(3, "colors for this dial");

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
        callbackslot.setValue(dial.callbackslot);
        cyclebits.setValue(dial.cyclebits);
        persistant.setSelected((dial.persistant == 1));

        //areas
        src.setFields(dial.srcloc[0]);
        dest.setFields(dial.destloc[0]);
        if (dial.renderneedle == 1){
            rneedle.setSelected(true);
            src.setEnabled(true);
            radius.setEnabled(false);
        }
        else {
            rneedle.setSelected(false);
            src.setEnabled(false);
            radius.setEnabled(true);
            radius.setFields(dial.radius);
        }

        //points
        //endpoints.setValue(dial.numendpoints);
        //lets convert these to 180 degrees
        float ptArray[] = new float[dial.points.length];
        for (int i=0;i<ptArray.length;i++){
            //falcon uses a clockwise 90 degree translation and CCW rotation
            //ptArray[i] = (float)(-dial.points[i] * 180.0f / Math.PI) + 90.0f;
			ptArray[i] = (float)(dial.points[i] * 180.0f / Math.PI);
        }
        points.setFields(ptArray);
        values.setFields(dial.values);

        //colors
        c0.setColor(dial.color[0]);
        c1.setColor(dial.color[1]);
        c2.setColor(dial.color[2]);

        //comments
        comments.setComments(dial.comments);
    }
    
    protected void setValues() throws Exception {
		//update main
		dial.callbackslot = callbackslot.getValue();
		dial.cyclebits = cyclebits.getValue();
		if (persistant.isSelected()) {
			dial.persistant = 1;
		}
		else {
			dial.persistant = 0;
		}

		//update areas
		dial.destloc[0] = dest.getArea();
		if (rneedle.isSelected()){
			dial.renderneedle = 1;
			dial.srcloc[0] = src.getArea();
		}
		else{
			dial.renderneedle = 0;
			dial.srcloc[0] = dial.destloc[0];
			dial.radius = radius.getFields();
		}

		//update points
		//discover the number of valid points: minimum between points and values
		dial.currentPoint = 0;
		int valid=0;
		{
			int v1 = points.getLength();
			int v2 = values.getLength();
			valid = (v1 < v2)?v1:v2;
		}
		dial.numendpoints = valid;
		//we read the points in degrees, and convert to radians, in falcon format
		float ptInDegrees[] = points.getFields(valid);
		float ptInRadians[] = new float[ptInDegrees.length];
		for (int i=0;i<valid;i++){
			//here we compensate the clockwise rotation and CCW rotation
			//ptInRadians[i] = (float)((-(ptInDegrees[i] - 90.0f))*Math.PI/180.0f);
			ptInRadians[i] = (float)((ptInDegrees[i])*Math.PI/180.0f);
		}
		dial.values = values.getFields(valid);
		dial.points = ptInRadians;

		//update colors
		dial.color[0] = c0.getColor();
		dial.color[1] = c1.getColor();
		dial.color[2] = c2.getColor();

		//update comments
		dial.comments = comments.getComments();
		if (dial.comments != null){
			dial.label = dial.comments.getLabel();
		}
    }

	protected boolean processActionEvent(ActionEvent e){
		Object o = e.getSource();
		if (o == rneedle){
			src.setEnabled(rneedle.isSelected());
			radius.setEnabled(!rneedle.isSelected());
			return true;
		}
		return false;
    }
}


