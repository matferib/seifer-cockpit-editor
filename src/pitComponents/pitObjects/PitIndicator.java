package pitComponents.pitObjects;

import cockpitEditor.UsPr;
import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import windows.PropertiesDialog;

public class PitIndicator extends PitObject {

	public int numtapes = 0, calibrationval;
	public int minpos[], maxpos[];
	public float minval, maxval;
	public String orientation;
	// the possible positions for a scrolling tape
	// these are the state possible values
	private final int POS_MIDDLE = 0, POS_MAX = 1, POS_MIN = 2;

	public PitIndicator(int id) {
		super(id);
		orientation = "horizontal";
		//indicators have 3 states: normal, max and minumum position
		super.setStates(3);
		super.currentState = POS_MIDDLE;
	}

	public Object clone(){
		PitIndicator i = new PitIndicator(0);
		this.cloneMeTo(i);
		i.numtapes = this.numtapes;
		i.calibrationval = this.calibrationval;
		i.minpos = (int[])this.minpos.clone();
		i.maxpos = (int[])this.maxpos.clone();
		i.minval = this.minval;
		i.maxval = this.maxval;
		i.orientation = new String(orientation);
		return i;
	}


	public void parseData(PitTokenizer st) throws ParseErrorException {
		int numSrcRead = 0, numDstRead = 0, numMaxRead = 0, numMinRead = 0;
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
					(!token.equals("orientation")) &&
					(!token.endsWith("loc")) && (st.ttype != st.TT_NUMBER)) 
				{
					throw new ParseErrorException("expecting number token");
				}

				if (token.equals("numtapes")) {
					numtapes = (int) st.nval;
					if (numtapes > MAX){
						System.out.println("Warning: more than " + MAX + "numtapes");
					}
					setNumSrcs(numtapes);
					setNumDsts(numtapes);
					maxpos = new int[numtapes];
					minpos = new int[numtapes];
					for (int i = 0; i < numtapes; i++) {
						maxpos[i] = minpos[i] = -2;
					}
				}
				else if (token.equals("callbackslot")) {
					callbackslot = (int) st.nval;
				}
				else if (token.equals("minval")) {
					minval = (float) st.nval;
				}
				else if (token.equals("maxval")) {
					maxval = (float) st.nval;
				}
				else if (token.equals("orientation")) {
					orientation = st.sval;
				}
				else if (token.equals("persistant")) {
					persistant = (int) st.nval;
				}
				else if (token.equals("cyclebits")) {
					cyclebits = st.parseHex();
				}
				else if (token.equals("srcloc")) {
					if (numSrcRead + 1 > numtapes) {
						throw new ParseErrorException(
								"more srclocs than numtapes");
					}
					srcloc[numSrcRead] = new PitArea(false, true);
					srcloc[numSrcRead++].parseData(st);
				}
				else if (token.equals("destloc")) {
					if (numDstRead + 1 > numtapes) {
						throw new ParseErrorException(
								"more destlocs than numtapes");
					}
					destloc[numDstRead] = new PitArea(false, true);
					destloc[numDstRead++].parseData(st);
				}
				else if (token.equals("maxpos")) {
					if (numMaxRead + 1 > numtapes) {
						throw new ParseErrorException(
								"more srclocs than numtapes");
					}
					maxpos[numMaxRead++] = (int) st.nval;
				}
				else if (token.equals("minpos")) {
					if (numMinRead + 1 > numtapes) {
						throw new ParseErrorException(
								"more srclocs than numtapes");
					}
					minpos[numMinRead++] = (int) st.nval;
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
			throw new ParseErrorException("pitLight.java: " + e);
		}
	}

	public void writeComponent(misc.RAFile arq) {
		int i = 0;
		try {
			if (comments != null){
				comments.write(arq);
			}
			arq.writeln("#" + id + " INDICATOR");
			if (cyclebits == -1){
				arq.writeln("\tcyclebits = -1;");
			}
			else{
				arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
						";");
			}
			arq.writeln("\tminval = " + minval + ";");
			arq.writeln("\tmaxval = " + maxval + ";");
			arq.writeln("\tnumtapes = " + numtapes + ";");
			arq.writeln("\tcallbackslot = " + callbackslot + ";");
			arq.writeln("\tpersistant = " + persistant + ";");
			for (i = 0; i < numtapes; i++) {
				arq.writeln("\tminpos = " + minpos[i] + ";");
				arq.writeln("\tmaxpos = " + maxpos[i] + ";");
				arq.writeln("\tsrcloc = " + srcloc[i] + ";");
				arq.writeln("\tdestloc = " + destloc[i] + ";");
			}
			arq.writeln("\torientation = " + orientation + ";");
			arq.writeln("#END");
		}
		catch (IOException io) {
			System.out.println("pitLight.java-> erro de IO: " + io);

		}
	}

	public String toTextFormat() {
		String s = new String();
		if (comments != null){
			s += comments;
		}
		s += "#" + id + " INDICATOR"+ ls;
		if (cyclebits == -1){
			s += "\tcyclebits = -1;"+ ls;
		} else{
			s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +
				";"+ ls;
		}
		s += "\tminval = " + minval + ";"+ ls;
		s += "\tmaxval = " + maxval + ";"+ ls;
		s += "\tnumtapes = " + numtapes + ";"+ ls;
		s += "\tcallbackslot = " + callbackslot + ";"+ ls;
		s += "\tpersistant = " + persistant + ";"+ ls;
		for (int i = 0; i < numtapes; i++) {
			s += "\tminpos = " + minpos[i] + ";"+ ls;
			s += "\tmaxpos = " + maxpos[i] + ";"+ ls;
			s += "\tsrcloc = " + srcloc[i] + ";"+ ls;
			s += "\tdestloc = " + destloc[i] + ";"+ ls;
		}
		s += "\torientation = " + orientation + ";"+ ls;
		s += "#END"+ ls;
		return s;
	}    

	public void draw(Graphics2D g) {
	    for (int i = 0; i < numtapes; i++) {
		// template image to use
		Image image;
		if (destloc[i].transparent) {
			image = Cockpit.template.getImage(PitTemplate.IM_FLOOD_INST_T);
		}
		else {
			image = Cockpit.template.getImage(PitTemplate.IM_FLOOD_INST);
		}

		if (image != null) {
		    //these are the values declared in the dat
		    Rectangle rDst = destloc[i].rectangle;
		    Rectangle rSrc = srcloc[i].rectangle;

		    //this is the rectangle we will draw in screen
		    Rectangle rD;
		    //this is the piece we take from template
		    Rectangle rS;

		    if (orientation.equals("vertical")){
			//this is no bug, we use src width for vertical inds
			rD = new Rectangle(rDst.x, rDst.y, rSrc.width, rDst.height);
			//we use the dest height as reference
			int h = rDst.height;                    

			if (currentState == POS_MIDDLE){
				//we get the middle vertical point from source 
				//(rSrc.y + (rSrc.height / 2))
				//and translate half of dest height, - (h / 2)
				int my = (rSrc.y + (rSrc.height / 2)) - (h / 2);                                          
				rS = new Rectangle(rSrc.x, my, rSrc.width, h);
			}
			else if (currentState == POS_MIN){
				rS = new Rectangle(rSrc.x, (rSrc.y + rSrc.height - h), rSrc.width, h);                        
			}
			else { // POS_MAX
				rS = new Rectangle(rSrc.x, rSrc.y, rSrc.width, h);                        
			}
		    }
		    else {
			//this is no bug, we use src height for horizontal inds
			rD = new Rectangle(rDst.x, rDst.y, rDst.width, rSrc.height);

			//we use the dest width as reference
			int w = rDst.width;

			if (currentState == POS_MIDDLE){
				//we get the middle horizontal point
				// and translate half width
				int mx = (rSrc.x + (rSrc.width / 2)) - (w / 2);
				rS = new Rectangle(mx, rSrc.y, w, rSrc.height); 
			}
			else if (currentState == POS_MIN){
				rS = new Rectangle(rSrc.x, rSrc.y, w, rSrc.height);                        
			}
			else { // POS_MAX
				rS = new Rectangle((rSrc.x+rSrc.width-w), rSrc.y, w, rSrc.height);
			}
		    }
		    //according to fixitman, falcon overrides desloc width using 
		    //srcloc width value(for vertical inds), and similar to 
		    //horizonatal ones
		    g.drawImage(image, 
			rD.x, rD.y, rD.x + rD.width, rD.y + rD.height,
			rS.x, rS.y, rS.x + rS.width, rS.y + rS.height, 
			null
		    );
		}
		if (drawRectangle){
			destloc[i].draw(g);
		}
	    }
	}

	/** Override parents draw because of minpos and maxpos mark*/
	public void drawTemplate(Graphics2D g) {
		if (srcloc == null){
			return;
		}

		// we draw the srcloc and the min and maxpos mark
		// save old color
		Color c = g.getColor();
		for (int i = 0; i < srcloc.length; i++) {
			if (srcloc[i] != null){
				int x1 = srcloc[i].upl.x;
				int x2 = srcloc[i].dor.x;
				int y1 = srcloc[i].upl.y;
				int y2 = srcloc[i].dor.y;
				g.setColor(c);
				srcloc[i].draw(g);
				if (orientation.equals("vertical")){
					g.setColor(UsPr.minPosColor);
					g.drawLine(x1, minpos[i], x2, minpos[i]);
					g.setColor(UsPr.maxPosColor);
					g.drawLine(x1, maxpos[i], x2, maxpos[i]);
				}
				else {
					g.setColor(UsPr.minPosColor);
					g.drawLine(minpos[i], y1, minpos[i], y2);
					g.setColor(UsPr.maxPosColor);
					g.drawLine(maxpos[i], y1, maxpos[i], y2);
				}
			}
		}
		// return to default color
		g.setColor(c);
	}

	/** computes min and max position for a tape automatically based
	 * on srcloc 
	 */
	private void computeMinAndMax(int i){
		//check first for valid tape
		if (i >= numtapes){
			return;
		}

		// we have to keep minpos and maxpos relative to each other
		// because it dictates direction tape will roll (up or down)
		boolean maxSmaller = (maxpos[i] < minpos[i]);

		if (orientation.equals("vertical")){
			// maxpos is the upper one
			int dh_2 = (destloc[i].rectangle.height / 2);
			maxpos[i] = srcloc[i].upl.y + dh_2;
			minpos[i] = srcloc[i].dor.y - dh_2;
		}
		else {
			// maxpos is the left one
			int dw_2 = (destloc[i].rectangle.width / 2);
			maxpos[i] = srcloc[i].upl.x + dw_2;
			minpos[i] = srcloc[i].dor.x - dw_2;                
		}

		// swap values if min is the smaller one
		if (!maxSmaller){
			int temp = maxpos[i];
			maxpos[i] = minpos[i];
			minpos[i] = temp;
		}
	}

	/** computes minpos and maxpos automatically for the indicator */
	public void computeMinAndMax(){
		for (int i=0;i<numtapes;i++){
			computeMinAndMax(i);
		}
	}

	// used by public fucntion of same name, applied on single tape
	private void computeSrclocFromMinAndMax(int i){        
		// check number of tapes
		if (i >= numtapes){
			return;
		}

		// check direction tape scrolls
		boolean maxSmaller = (maxpos[i] < minpos[i]);
		int smallest = (maxSmaller) ? maxpos[i] : minpos[i];
		int biggest = (maxSmaller) ? minpos[i] : maxpos[i];

		if (orientation.equals("vertical")){
			// maxpos is the upper one
			int dh_2 = (destloc[i].rectangle.height / 2);
			srcloc[i].upl.y = smallest - dh_2;
			srcloc[i].dor.y = biggest + dh_2;
		}
		else {
			// maxpos is the left one
			int dw_2 = (destloc[i].rectangle.width / 2);
			srcloc[i].upl.x = smallest - dw_2;
			srcloc[i].dor.x = biggest + dw_2;                
		}        
	}

	/** computes srcloc min and max position from minpos and maxpos */
	public void computeSrclocFromMinAndMax(){
		for (int i=0;i<numtapes;i++){
			computeSrclocFromMinAndMax(i);
		}
	}

	public PropertiesDialog getPropertiesDialog(JFrame parent){
		IndPropDialog ipd = new IndPropDialog(parent, this);
		return ipd;
	}
	public PropertiesDialog getNewPropertiesDialog(JDialog parent){
		IndPropDialog ipd = new IndPropDialog(parent, this);
		return ipd;
	}

}

class IndPropDialog extends PropertiesDialog {
	PitIndicator ind;
	JTabbedPane tp = new JTabbedPane();

	//main
	LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);
	LabeledNumField callbackslot = new LabeledNumField(this, "callbackslot", 8);
	LabeledCBox persistant = new LabeledCBox("persistant");
	LabeledFloatFields minval = new LabeledFloatFields(this, "minval", 1, 5);
	LabeledFloatFields maxval = new LabeledFloatFields(this, "maxval", 1, 5);
	LabeledDropDown orientation = new LabeledDropDown(
		"orientation", new String [] {"vertical", "horizontal"}
	);

	//areas: numtapes src and destlocs
	LabeledNumField numtapes = new LabeledNumField(
		this, "numtapes", "number of tapes", 5
	);
	AreasFieldPanel src[];
	AreasFieldPanel dest[];
	LabeledNumField minpos[], maxpos[];
	JButton updNumtapes = new JButton("update");

	//auto min/max button
	JButton autoMM = new JButton("Min/Max");
	JButton autoSrc = new JButton("AutoSrc");

	//comments
	CommentsPane comments = new CommentsPane(this);

	public IndPropDialog(JFrame parent, PitIndicator ind){
		super(parent, ind, "Indicator Properties", false);
		setProperties(ind);
	}    

	public IndPropDialog(JDialog parent, PitIndicator ind){
		super(parent, ind, "Indicator Properties", true);
		setProperties(ind);
	}

	private void setProperties(PitIndicator ind){
		this.ind = ind;
		this.setSize(600,400);
		Container c = this.getContentPane();
		tp.setTabPlacement(tp.LEFT);
		// position change
		super.enableStateButtons();

		// adds auto minpos maxpos to south panel
		autoMM.setToolTipText("Computes minpos and maxpos automatically for all tapes in this indicator");
		autoSrc.setToolTipText("Computes srcloc automatically based on minpos and maxpos");
		super.southPanel.add(autoMM);
		super.southPanel.add(autoSrc);
		autoMM.addActionListener(this);
		autoSrc.addActionListener(this);

		//main tab
		{
			JPanel mainP = new JPanel(new GridLayout(8,1));
			mainP.add(new JLabel("INDICATOR #" + ind.id, JLabel.CENTER));

			mainP.add(callbackslot);
			mainP.add(cyclebits);
			mainP.add(persistant);
			mainP.add(minval);
			mainP.add(maxval);
			mainP.add(orientation);

			tp.add("main", mainP);
			tp.setToolTipTextAt(0, "main properties");
		}
		
		//areas
		{
			JPanel areasP = new JPanel(new GridLayout(PitIndicator.MAX*4+1,1));

			//numtapes field and button
			updNumtapes.setToolTipText(
				"click to update the area fields, max: " + ind.MAX
			);
			updNumtapes.addActionListener(this);
			numtapes.add(updNumtapes);
			areasP.add(numtapes);

			src = new AreasFieldPanel[ind.MAX];
			dest = new AreasFieldPanel[ind.MAX];
			minpos = new LabeledNumField[ind.MAX];
			maxpos = new LabeledNumField[ind.MAX];
			for (int i = 0; i < ind.MAX; i++) {
				src[i] = new AreasFieldPanel(this, "src" + i + ":", false);
				dest[i] = new AreasFieldPanel(this, "dst" + i + ":", false);
				// we place max above min because max is usually the upper point
				maxpos[i] = new LabeledNumField(
					this, 
					"maxpos" + i + ":", 
					"maxpos is the place middle of tape will reach at maxval for the given tape", 
					6
				);
				minpos[i] = new LabeledNumField(
					this, 
					"minpos" + i + ":", 
					"minpos is the place middle of tape will reach at minval for the given tape", 
					6
				);
				areasP.add(src[i]);
				areasP.add(dest[i]);                
				areasP.add(maxpos[i]);
				areasP.add(minpos[i]);
			}

			JScrollPane areasSP = new JScrollPane(areasP);
			tp.add("areas", areasSP);
			tp.setToolTipTextAt(1, "numtapes and areas");
		}
		
		//comments
		{
			tp.add("comms", comments);
			tp.setToolTipTextAt(2, "comments");
		}

		c.add(tp, BorderLayout.CENTER);

		if (!isNew){
			loadValues();
		}

	}

	public void loadValues(){
		//main
		callbackslot.setValue(ind.callbackslot);
		cyclebits.setValue(ind.cyclebits);
		persistant.setSelected((ind.persistant == 1));
		minval.setFields(new float[]{ind.minval});
		maxval.setFields(new float[]{ind.maxval});
		if (ind.orientation.equalsIgnoreCase("vertical")){
			orientation.setSelectedIndex(0);
		}
		else {
			orientation.setSelectedIndex(1);
		}

		//areas
		numtapes.setValue(ind.numtapes);
		for (int i=0;i<ind.MAX;i++){
			if (i<ind.numtapes){
				//enable fields
				src[i].setEnabled(true);
				dest[i].setEnabled(true);
				maxpos[i].setEnabled(true);
				minpos[i].setEnabled(true);

				//set the values
				src[i].setFields(ind.srcloc[i]);
				dest[i].setFields(ind.destloc[i]);
				maxpos[i].setValue(ind.maxpos[i]);
				minpos[i].setValue(ind.minpos[i]);
			}
			else {
				src[i].setEnabled(false);
				dest[i].setEnabled(false);
				maxpos[i].setEnabled(false);
				minpos[i].setEnabled(false);
			}
		}

		//comments
		comments.setComments(ind.comments);
	}

	protected void setValues() throws Exception {
		//update main
		ind.callbackslot = callbackslot.getValue();
		ind.cyclebits = cyclebits.getValue();
		ind.minval = minval.getField(0);
		ind.maxval = maxval.getField(0);
		if (persistant.isSelected()) {
			ind.persistant = 1;
		}
		else {
			ind.persistant = 0;
		}
		ind.orientation = orientation.getSelectedItem();

		//areas
		ind.numtapes = numtapes.getValue();
		ind.setNumSrcs(ind.numtapes);
		ind.setNumDsts(ind.numtapes);
		ind.minpos = new int[ind.numtapes];
		ind.maxpos = new int[ind.numtapes];

		for (int i = 0; i < ind.numtapes; i++) {
			ind.srcloc[i] = src[i].getArea();
			ind.destloc[i] = dest[i].getArea();
			ind.minpos[i] = minpos[i].getValue();
			ind.maxpos[i] = maxpos[i].getValue();
		}

		//update comments
		ind.comments = comments.getComments();
		if (ind.comments != null){
			ind.label = ind.comments.getLabel();
		}
	}

	public boolean processActionEvent(ActionEvent e){
		Object o = e.getSource();

		if (o == updNumtapes){
			//update number of tapes

			int val = 0;
			try {
				val = numtapes.getValue();
			}
			catch (Exception e2){
				JOptionPane.showMessageDialog(
					this, "Invalid numtapes value",
					"Error", JOptionPane.OK_OPTION
				);
				return true;
			}

			// disable tapes above number and enable others
			for (int i = 0; i < ind.MAX; i++) {
				if (i < val) {
					//enable fields
					src[i].setEnabled(true);
					dest[i].setEnabled(true);
					maxpos[i].setEnabled(true);
					minpos[i].setEnabled(true);

				}
				else {
					src[i].setEnabled(false);
					dest[i].setEnabled(false);
					maxpos[i].setEnabled(false);
					minpos[i].setEnabled(false);
				}
			}
			return true;
		}
		else if (o == autoMM){
			// auto min max button
			ind.computeMinAndMax();
			//update values in fields
			for (int i = 0; i < ind.numtapes; i++) {
				minpos[i].setValue(ind.minpos[i]);
				maxpos[i].setValue(ind.maxpos[i]);
			}            
			parent.repaint();
			Cockpit.template.repaint();
			return true;
		}
		else if (o == autoSrc){
			// compute srcloc automatically
			ind.computeSrclocFromMinAndMax();
			//update values in fields
			for (int i = 0; i < ind.numtapes; i++) {
				src[i].setFields(ind.srcloc[i]);
			}            
			parent.repaint();
			Cockpit.template.repaint();
			return true;
		}
		return false;
	}
}

