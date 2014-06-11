package pitComponents.pitObjects;

import cockpitEditor.UsPr;
import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import windows.PropertiesDialog;

public class PitDigits extends PitObject {

	public int numdigits = 0;
	private int numDstRead = 0, numSrcRead = 0;
	static int currentDigit = 0;

	public PitDigits(int id) {
		super(id);
		setNumSrcs(10); //10 srcs, one for each digit
	}

	public Object clone(){
		PitDigits d = new PitDigits(0);
		super.cloneMeTo(d);
		d.numdigits = numdigits;
		d.numDstRead = numDstRead;
		d.numSrcRead = numSrcRead;
		d.currentDigit = currentDigit;
		return d;
	}


	public void parseData(PitTokenizer st) throws ParseErrorException {
		String token = null;
		try {
			do {
				//				st.nextToken();
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
					if (numSrcRead + 1 > 10) {
						throw new Exception("more than 10 srclocs");
					}
					srcloc[numSrcRead] = new PitArea();
					srcloc[numSrcRead++].parseData(st);
				}
				else if (token.equals("destloc")) {
					if (numDstRead + 1 > numdigits) {
						throw new ParseErrorException(
								"more dstlocs than numdigits");
					}
					destloc[numDstRead] = new PitArea();
					destloc[numDstRead++].parseData(st);
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
				else if (token.equals("numdigits")) {
					numdigits = (int) st.nval;
					setNumDsts(numdigits);
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
			throw new ParseErrorException("pitDigits.java: " + e);
		}
	}

	public void writeComponent(misc.RAFile arq) {
		int i = 0;
		try {
			if (comments != null){
				comments.write(arq);
			}
			arq.writeln("#" + id + " DIGITS");
			if (cyclebits == -1){
				arq.writeln("\tcyclebits = -1;");
			}
			else{
				arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
						";");
			}
			arq.writeln("\tnumdigits = " + numdigits + ";");
			arq.writeln("\tcallbackslot = " + callbackslot + ";");
			arq.writeln("\tpersistant = " + persistant + ";");
			for (i = 0; i < 10; i++) {
				arq.writeln("\tsrcloc = " + srcloc[i] + ";");
			}
			for (i = 0; i < numdigits; i++) {
				arq.writeln("\tdestloc = " + destloc[i] + ";");
			}
			arq.writeln("#END");
		}
		catch (IOException io) {
			System.out.println("pitButtonview.java-> erro de IO: " + io);

		}
	}

	public String toTextFormat() {
		String s = new String();
		int i = 0;
		if (comments != null){
			s += comments;
		}
		s += "#" + id + " DIGITS"+ ls;
		if (cyclebits == -1){
			s += "\tcyclebits = -1;"+ ls;
		} else{
			s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +
				";"+ ls;
		}
		s += "\tnumdigits = " + numdigits + ";"+ ls;
		s += "\tcallbackslot = " + callbackslot + ";"+ ls;
		s += "\tpersistant = " + persistant + ";"+ ls;
		for (i = 0; i < 10; i++) {
			s += "\tsrcloc = " + srcloc[i] + ";"+ ls;
		}
		for (i = 0; i < numdigits; i++) {
			s += "\tdestloc = " + destloc[i] + ";"+ ls;
		}
		s += "#END"+ ls;
		return s;
	}
	//this is a lil different from the usual draw
	public void draw(Graphics2D g) {        
		for (int i = 0; i < numdigits; i++) {
			// get the correct template image
			Image image;
			if (destloc[i].transparent){
				image = Cockpit.template.getImage(PitTemplate.IM_FLOOD_INST_T);
			}
			else {
				image = Cockpit.template.getImage(PitTemplate.IM_FLOOD_INST);
			}

			//draw different digits to test
			if (image != null) {
			    drawSrcDst(g, currentDigit, i);
			    /*g.drawImage(image, 
				destloc[i].upl.x, destloc[i].upl.y,
				destloc[i].dor.x, destloc[i].dor.y,
				srcloc[currentDigit].upl.x, srcloc[currentDigit].upl.y,
				srcloc[currentDigit].dor.x, srcloc[currentDigit].dor.y,
				null
			    );*/
			    currentDigit = (currentDigit+1) % 10;
			}
		}
	}

	//this is a lil different from the usual draw
	public void drawLabeled(Graphics2D g) {
		draw(g);
		for (int i = 0; i < numdigits; i++) {
			//draw different digits to test
			if (label != null){
				destloc[0].drawTextUnder(label, g);
			}
		}
	}

	protected PropertiesDialog getPropertiesDialog(JFrame parent){
		DigPropDialog dpd = new DigPropDialog(parent, this);
		return dpd;
	}
	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		DigPropDialog dpd = new DigPropDialog(parent, this);
		return dpd;
	}

}

class DigPropDialog extends PropertiesDialog {
	PitDigits dig;
	JTabbedPane tp = new JTabbedPane();

	//main
	LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);
	LabeledNumField callbackslot = new LabeledNumField(this, "callbackslot", 8);
	LabeledCBox persistant = new LabeledCBox("persistant");

	JButton equalSrcs, equalSrcToDest;
	//src areas(10, 1 per digit)
	SrcDestPane src;

	//dest area and number
	SrcDestPane dest;
	JButton upd = new JButton("upd");
	LabeledNumField ndest = new LabeledNumField(parent, "numdig", "number of digits", 8);


	//comments
	CommentsPane comments = new CommentsPane(this);

	public DigPropDialog(JFrame parent, PitDigits dig){
		super(parent, dig, "Digits Properties", false);
		setProperties(dig);
	}    

	public DigPropDialog(JDialog parent, PitDigits dig){
		super(parent, dig, "Digits Properties", true);
		setProperties(dig);
	}

	private void setProperties(PitDigits dig){

		this.dig = dig;
		this.setSize(400,400);
		Container c = this.getContentPane();
		tp.setTabPlacement(tp.LEFT);

		//main tab
		{
			JPanel mainP = new JPanel(new GridLayout(8,1));
			mainP.add(new JLabel("Digits #" + dig.id, JLabel.CENTER));

			mainP.add(callbackslot);
			mainP.add(cyclebits);
			mainP.add(persistant);

			tp.add("main", mainP);
			tp.setToolTipTextAt(0, "main properties");
		}
		//10 src areas
		{
			JPanel srcP = new JPanel(new BorderLayout());

			//the 2 buttons
			JPanel buttonsPanel = new JPanel(new GridLayout(1,2));
			equalSrcs = new JButton("Equal src sizes");
			equalSrcs.setToolTipText("Make all srclocs the same size");
			equalSrcs.addActionListener(this);
			equalSrcToDest = new JButton("Srcs=Dest");
			equalSrcToDest.setToolTipText("Make all srcs size equal to destination size");
			equalSrcToDest.addActionListener(this);
			buttonsPanel.add(equalSrcs);
			buttonsPanel.add(equalSrcToDest);
			srcP.add(buttonsPanel, BorderLayout.NORTH);

			src = new SrcDestPane(parent, "Sources", "s", 10);
			srcP.add(src, BorderLayout.CENTER);

			tp.add("src", srcP);
			tp.setToolTipTextAt(1, "source areas");
		}
		//numdigits destareas
		{
			JPanel dareaP = new JPanel(new BorderLayout());


			//numdigits
			ndest.add(upd);
			upd.setToolTipText("update numdigits");
			upd.addActionListener(this);
			dareaP.add(ndest, BorderLayout.NORTH);

			dest = new SrcDestPane(parent, "Destinations", "d");
			dareaP.add(dest, BorderLayout.CENTER);

			dareaP.add(dest);

			tp.add("dst", dareaP);
			tp.setToolTipTextAt(2, "destination areas");

		}
		//comments
		{
			tp.add("comms", comments);
			tp.setToolTipTextAt(3, "comments");
		}

		c.add(tp, BorderLayout.CENTER);

		if (!isNew)
			loadValues();

	}

	public void loadValues(){
		//main
		callbackslot.setValue(dig.callbackslot);
		cyclebits.setValue(dig.callbackslot);
		persistant.setSelected((dig.persistant == 1));

		//src areas
		for (int i=0;i<10;i++){
			src.setFields(i, dig.srcloc[i]);
		}

		//dest areas
		ndest.setValue(dig.numdigits);
		dest.enableFields(dig.numdigits);
		for (int i=0;i<dig.numdigits;i++){
			dest.setFields(i, dig.destloc[i]);
		}

		//comments
		comments.setComments(dig.comments);
	}

	protected void setValues() throws Exception {
		//update main
		dig.callbackslot = callbackslot.getValue();
		dig.cyclebits = cyclebits.getValue();
		if (persistant.isSelected()) {
			dig.persistant = 1;
		} else {
			dig.persistant = 0;
		}

		//update src
		for (int i = 0; i < 10; i++) {
			dig.srcloc[i] = src.getFields(i);
		}
		//dest
		dig.numdigits = ndest.getValue(); //numdigits
		dig.setNumDsts(dig.numdigits);
		for (int i = 0; i < dig.numdigits; i++) {
			dig.destloc[i] = dest.getFields(i);
		}

		//update comments
		dig.comments = comments.getComments();
		if (dig.comments != null){
			dig.label = dig.comments.getLabel();
		}
	}

	protected boolean processActionEvent(ActionEvent e){
		Object o = e.getSource();
		if (o == equalSrcs){
			//make all src sizes equal the first one
			dig.equalSrclocs(0);
			Cockpit.template.repaint();
			return true;
		}
		else if (o == equalSrcToDest){
			//make all sizes equal to the destination one
			dig.equalSrclocsToDest(0);
			Cockpit.template.repaint();
			return true;
		}
		return false;
	}
}

