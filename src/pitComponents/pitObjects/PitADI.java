package pitComponents.pitObjects;

import java.awt.image.BufferedImage;
import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import pitComponents.pitHelperComponents.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import windows.PropertiesDialog;

/*@TODO change the way areas are handled, for easy move resize etc */
/** Represents the ADI in the cockpit. ADI stands for Atitude Display Information
 * its the "ball" in the cockpit, and indicates angle of attack, pitch, roll and
 * also has information for landing system.
 */
public class PitADI extends PitObject {
	//we gonna use destloc 1 as ilslimits
	PitArea /*ilslimits, bsrcloc, bdestloc,*/ backsrc, backdest; //when the adi has buffer, it uses these 2
	public boolean blitbackground;
	Dimension buffersize;
	PitFile filename;

	public PitADI(int id) {
		//adi has 2 srcs: srcloc and bsrcloc
		//adi has 3 dests: destloc, bdestloc, ilslimits
		super(id, 2, 3);
		blitbackground = false;
		color = new PitColor[5];
	}

    public Object clone(){
		PitADI adi = new PitADI(0);
		super.cloneMeTo(adi);
		//adi.ilslimits = PitArea.clone(ilslimits);
		adi.backdest = PitArea.clone(backdest);
		adi.backsrc = PitArea.clone(backsrc);
		//adi.bsrcloc = PitArea.clone(bsrcloc);
		//adi.bdestloc = PitArea.clone(bdestloc);
		adi.blitbackground = blitbackground;
		if (buffersize == null){
			adi.buffersize = null;
		}
		else {
			adi.buffersize = (Dimension)buffersize.clone();
		}
		adi.filename = PitFile.clone(filename);
		return adi;
    }


    public void parseData(PitTokenizer st) throws ParseErrorException {
        int i;
        boolean canEnd = true;
        String token;

        //well loop till we get the #end entry(the last one)
        try {
            do {
                st.skipComment();

                if (st.ttype != st.TT_WORD) {
                        throw new Exception("Expecting word token");
                }
                token = st.sval;
                //if no buffer present, we can end, else we have to read 2 #ends
                if (token.equals("#end")) {
                        if (canEnd) {
                                break;
                        }
                        else {
                                canEnd = true; //next #end we exit
                                //next iteration
                                continue;
                        }
                }
                //now we get the = if it exists
                st.nextToken();
                if (st.ttype == '=') {
                    st.nextToken();

                        //parse what we got
                }
                if (token.equals("callbackslot")) {
                        if (st.ttype != st.TT_NUMBER) {
                                throw new Exception(
                                  "Expecting callbackslot = <number>;");
                        }
                        callbackslot = (int) st.nval;
                }
                else if (token.equals("persistant")) {
                        if (st.ttype != st.TT_NUMBER) {
                                throw new Exception("Expecting persistant = <number>;");
                        }
                        persistant = (int) st.nval;
                }
                else if (token.equals("blitbackground")) {
                        blitbackground = true;
                        st.pushBack();
                }
                else if (token.equals("srcloc")) {
                        srcloc[0] = new PitArea(false, true);
                        srcloc[0].parseData(st);
                }
                else if (token.equals("destloc")) {
                        destloc[0] = new PitArea();
                        destloc[0].parseData(st);
                }
                else if (token.equals("buffersize")) {
                    canEnd = false; //this ADI has 2 ends, cause of the buffersize
                    buffersize = new Dimension();

                    //for buffersize, falcon uses XY format
                    if (st.ttype != st.TT_NUMBER) {
                            throw new Exception("Expecting buffersize = X Y;");
                    }
                    buffersize.width = (int) st.nval;
                    st.nextToken();
                    if (st.ttype != st.TT_NUMBER) {
                            throw new Exception("Expecting buffersize = X Y;");
                    }
                    buffersize.height = (int) st.nval;
                }
                else if (token.equals("backsrc")) {
                    backsrc = new PitArea();
                    backsrc.parseData(st);
                }
                else if (token.equals("backdest")) {
                    backdest = new PitArea();
                    backdest.parseData(st);
                }
                else if (token.equals("ilslimits")) {
                    //ilslimits = new PitArea();
                    //ilslimits.parseData(st);
                    destloc[2] = new PitArea();
                    destloc[2].parseData(st);
                }
                else if (token.equals("cyclebits")) {
                    cyclebits = st.parseHex();
                }
                else if (token.equals("bdestloc")) {
                    /*bdestloc = new PitArea();
                    bdestloc.parseData(st);*/
                    destloc[1] = new PitArea();
                    destloc[1].parseData(st);
                }
                else if (token.equals("bsrcloc")) {
                    /*bsrcloc = new PitArea();
                    bsrcloc.parseData(st);*/
                    srcloc[1] = new PitArea(false, true);
                    srcloc[1].parseData(st);                    
                }
                else if (token.startsWith("color")) {
                    //we get the color number
                    int cn = Integer.parseInt(token.substring(5));
                    color[cn] = st.parseColor();
                }
                else if (token.equals("filename")) {
                    filename = new PitFile();
                    filename.parseData(st);
                }

                //get the ; end lets get outta here
                st.nextToken();

                if (st.ttype != ';') {
                        throw new Exception("Missing ;");
                }
            }
            while (true);

            //try to get the label
            if (comments != null){
                label = comments.getLabel();
            }
        }
        catch (Exception e) {
            throw new ParseErrorException("pitAdi.java: parse error " + e);
        }
    }

    public void writeComponent(misc.RAFile arq) {
		int i;

		try {
			if (comments != null){
				comments.write(arq);
			}
			arq.writeln("#" + id + " ADI");

			if (cyclebits == -1){
				arq.writeln("\tcyclebits = -1;");
			}
			else{
				arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
							   ";");
			}
			arq.writeln("\tcallbackslot = " + callbackslot + ";");
			arq.writeln("\tpersistant = " + persistant + ";");
			arq.writeln("\tsrcloc = " + srcloc[0] + ";");
			/*if (ilslimits != null) {
					arq.writeln("\tilslimits = " + ilslimits + ";");
			}*/
			if (destloc[2] != null){
				arq.writeln("\tilslimits = " + destloc[2] + ";");
			}
			arq.writeln("\tdestloc = " + destloc[0] + ";");

			/*if (bsrcloc != null) {
					arq.writeln("\tbsrcloc = " + bsrcloc + ";");
			}
			if (bdestloc != null) {
					arq.writeln("\tbdestloc = " + bdestloc + ";");
			}*/
			if (srcloc[1] != null){
					arq.writeln("\tbsrcloc = " + srcloc[1] + ";");
			}
			if (destloc[1] != null){
					arq.writeln("\tbdestloc = " + destloc[1] + ";");
			}            
			if (blitbackground) {
					arq.writeln("\tblitbackground;");

			}

			if (buffersize != null) {
					arq.writeln("\tfilename = " + filename.getName() + ";");
					arq.writeln("\tbuffersize = " + buffersize.width + " " +
											   buffersize.height + ";");
					arq.writeln("#END");
					arq.writeln("\tbacksrc = " + backsrc + ";");
					arq.writeln("\tbackdest = " + backdest + ";");
			}
			for (i = 0; i < 5; i++) {
				if (color[i] != null)
					arq.writeln("\tcolor" + i + " = " + color[i] + ";");
				}
				arq.writeln("#END");
			}
		catch (IOException io) {
			System.out.println("pitADI.java-> Erro de IO: " + io);
		}
	}
    
    public String toTextFormat() {
        int i;
        String s = new String();
        if (comments != null){
            s += comments;
        }
        s += "#" + id + " ADI"+ ls;
        
        if (cyclebits == -1){
            s += "\tcyclebits = -1;"+ ls;
        } else{
            s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                    ";"+ ls;
        }
        s += "\tcallbackslot = " + callbackslot + ";"+ ls;
        s += "\tpersistant = " + persistant + ";"+ ls;
        s += "\tsrcloc = " + srcloc[0] + ";"+ ls;
/*            if (ilslimits != null) {
                    s += "\tilslimits = " + ilslimits + ";"+ ls;
            }*/
        if (destloc[2] != null){
            s += "\tilslimits = " + destloc[2] + ";"+ ls;
        }
        s += "\tdestloc = " + destloc[0] + ";"+ ls;
        
            /*if (bsrcloc != null) {
                    s += "\tbsrcloc = " + bsrcloc + ";"+ ls;
            }
            if (bdestloc != null) {
                    s += "\tbdestloc = " + bdestloc + ";"+ ls;
            }*/
        if (srcloc[1] != null){
            s += "\tbsrcloc = " + srcloc[1] + ";"+ ls;
        }
        if (destloc[1] != null){
            s += "\tbdestloc = " + destloc[1] + ";"+ ls;
        }
        if (blitbackground) {
            s += "\tblitbackground;"+ ls;
            
        }
        
        if (buffersize != null) {
            s += "\tfilename = " + filename.getName() + ";"+ ls;
            s += "\tbuffersize = " + buffersize.width + " " +
                    buffersize.height + ";"+ ls;
            s += "#END\n"+ ls;
            s += "\tbacksrc = " + backsrc + ";"+ ls;
            s += "\tbackdest = " + backdest + ";"+ ls;
        }
        for (i = 0; i < 5; i++) {
            if (color[i] != null)
                s += "\tcolor" + i + " = " + color[i] + ";"+ ls;
        }
        s += "#END"+ ls;
        
        return s;
    }    
    
    public PropertiesDialog getNewPropertiesDialog(JDialog parent){
        return new AdiPropDialog(parent, this);
    }

    public PropertiesDialog getPropertiesDialog(JFrame parent){
        return new AdiPropDialog(parent, this);
    }
	
    
	/** Draws the ADI. Calls PitObject draw, and then draw the ilslimits over it. */
	public void draw(Graphics2D g){
		// we draw center part of srcloc in destloc
		Point sc = srcloc[0].getCenter();
		Rectangle rD = destloc[0].rectangle;
		//  src rectangle
		Rectangle rS = new Rectangle(sc.x - rD.width / 2, sc.y - rD.height / 2, rD.width, rD.height);
		Image image = Cockpit.template.getImage(PitTemplate.IM_FLOOD_INST);

		g.drawImage(image, 
			rD.x, rD.y, rD.x + rD.width, rD.y + rD.height,
			rS.x, rS.y, rS.x + rS.width, rS.y + rS.height, 
			null
		);
	     
        if (blitbackground){
            if ((srcloc[1] != null) && (destloc[1] != null)){
                drawSrcDst(g, 1, 1);
            }
        }
		
        if (destloc[2] != null){
            destloc[2].draw(g);
        }
    }


}

class AdiPropDialog extends PropertiesDialog {

	PitADI adi;

	LabeledNumField callbackField = new LabeledNumField(this, "callbackslot", 8);
	LabeledHexField cycleField = new LabeledHexField(this, "cyclebits", 8);
	LabeledCBox persistant = new LabeledCBox("persistant");
	LabeledCBox blitback = new LabeledCBox("blitbackground");

	static String areastrs[] = {"srcloc", "destloc","bsrcloc","bdestloc","ilslimits"};
	static boolean areasModifier[] = {false, true, false, true, true };

	ColorField colors[] = new ColorField[5];
	AreasFieldPanel areas[] = new AreasFieldPanel[areastrs.length];

	//buffer stuff
	LabeledCBox hasbuffer = new LabeledCBox(
		"buffer?", "if this ADI has a buffer, check this"
	);
	// if it has a buffer, must have a buffersize and filename also
	LabeledField filename = new LabeledField(this, "filename", 10);
	LabeledDimensionFields buffersize = new LabeledDimensionFields(
		this, "buffersize"
	);
	AreasFieldPanel backsrc = new AreasFieldPanel(this, "backsrc");
	AreasFieldPanel backdest = new AreasFieldPanel(this, "backdst");

	//comments
	CommentsPane comments= new CommentsPane(this);


	/** Creates a dialog with all ADI properties. Also sets some attributes
	* for the dialog, like size, modeal, layout, etc. Can be called to create
	* new ADIs or to update an existing one.
	*
	* @param   parent  the parent window
	* @param   adi the ADI we are creating or updating
	* @param   isNew   if creating a new ADI, use true
	*/
	public AdiPropDialog(JFrame parent, PitADI adi){
		super(parent, adi, "Adi Properties", false);
		setProperties(adi);
	}    


	/** Creates a dialog with all ADI properties. Also sets some attributes
	* for the dialog, like size, modeal, layout, etc. Can be called to create
	* new ADIs or to update an existing one.
	*
	* @param   parent  the parent window
	* @param   adi the ADI we are creating or updating
	* @param   isNew   if creating a new ADI, use true
	*/
	public AdiPropDialog(JDialog parent, PitADI adi){
		super(parent, adi, "Adi Properties", true);
		setProperties(adi);
	}
    
    private void setProperties(PitADI adi){
        this.adi = adi;
        Container c = this.getContentPane();
        this.setSize(450, 450);

        //the main panel has only a JTabbedPane
        //the tabbedPane
        JTabbedPane tp = new JTabbedPane(JTabbedPane.LEFT);
        tp.setBackground(Color.lightGray);
        //add the tab as the center part
        c.add(tp, BorderLayout.CENTER);

        {
            //main properties
            JPanel mainP = new JPanel(new GridLayout(5, 1));

            //ID
            mainP.add(new JLabel("ADI #" + adi.id, JLabel.CENTER));
            //the fields
            mainP.add(callbackField);
            mainP.add(cycleField);
            //persistant?
            mainP.add(persistant);
            //blitback
            mainP.add(blitback);

            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");

        }
        {
            //areas tab
            JPanel areaP = new JPanel(new GridLayout(6, 1));
            areaP.add(new JLabel("(op/trans) - uplx, uply, dorx, dory", JLabel.CENTER));

            //0 and 2 are srcloc
            for (int i=0;i<areastrs.length;i++){
                areas[i] = new AreasFieldPanel(this, areastrs[i], areasModifier[i]);                
                areaP.add(areas[i]);
            }
            tp.add("areas", areaP);
            tp.setToolTipTextAt(1, "srcloc, destloc, bsrcloc, bdestloc, ilslimits");

        }
        {
            //buffer tab
            JPanel bP = new JPanel(new GridLayout(6, 1));
            bP.add(hasbuffer);
            bP.add(buffersize);
            bP.add(filename);
            bP.add(backsrc);
            bP.add(backdest);

            hasbuffer.addActionListener(this);

            tp.add("buffer", bP);
            tp.setToolTipTextAt(2, "buffer data");
        }
        {
            //colors tab
            JPanel colorP = new JPanel(new GridLayout(5, 1));
            colors = new ColorField[adi.color.length];
            for (int i=0;i<colors.length;i++){
                colors[i] = new ColorField(i);
                colorP.add(colors[i]);
            }
            tp.add("colors", colorP);
            tp.setToolTipTextAt(3, "Adi Colors");

        }
        {
            //comments
            tp.add("comments", comments);
            tp.setToolTipTextAt(4, "comments on this object");
        }

        //load all values if we are not a new object
        if (!isNew){
            loadValues();
		}
    }

	protected void setValues() throws Exception {
		adi.cyclebits = cycleField.getValue();
		adi.callbackslot = callbackField.getValue();
		if (persistant.isSelected()) {
			adi.persistant = 1;
		}
		else {
			adi.persistant = 0;
		}
		adi.blitbackground = blitback.isSelected();

		//areas, srcloc and destloc are mandatory
		adi.srcloc[0] = areas[0].getArea();
		adi.destloc[0] = areas[1].getArea();
		if (adi.srcloc[0] == null || adi.destloc[0] == null){
			throw new Exception("srcloc0 and destloc0 must be valid");
		}

		adi.srcloc[1] = areas[2].getArea();
		adi.destloc[1] = areas[3].getArea();
		adi.destloc[2] = areas[4].getArea();

		//has buffer??
		if (hasbuffer.isSelected()) {
			adi.buffersize = buffersize.getFields();
			adi.filename = new PitFile(filename.getText());
			adi.backsrc = backsrc.getArea();
			adi.backdest = backdest.getArea();
		}
		else {
			adi.buffersize = null;
		}

		// colors
		for (int i = 0; i < 5; i++) {
			adi.color[i] = colors[i].getColor();
		}
		// comments
		adi.comments = comments.getComments();
		if (adi.comments != null){
			adi.label = adi.comments.getLabel();
		}
	}
   
    protected void loadValues(){
        //main values
        //{"cyclebits", "callbackslot"};
        {
            cycleField.setValue(adi.cyclebits);
            callbackField.setValue(adi.callbackslot);
            persistant.setSelected(adi.persistant == 1);
        }
        //areas values
        //{"srcloc", "destloc","bsrcloc","bdestloc","ilslimits"};
        {
            areas[0].setFields(adi.srcloc[0]);
            areas[1].setFields(adi.destloc[0]);
            //these are optional
            //if (adi.bsrcloc!= null)
                //areas[2].setFields(adi.bsrcloc);
            if (adi.srcloc[1] != null)
                    areas[2].setFields(adi.srcloc[1]);
            //if (adi.bdestloc != null)
                //areas[3].setFields(adi.bdestloc);
            if (adi.destloc[1] != null)
                    areas[3].setFields(adi.destloc[1]);            
            /*if (adi.ilslimits != null)
                areas[4].setFields(adi.ilslimits);*/
            if (adi.destloc[2] != null)
                areas[4].setFields(adi.destloc[2]);
        }
        // buffer
        {
            if (adi.buffersize == null){
                hasbuffer.setSelected(false);
                backsrc.setEnabled(false);
                backdest.setEnabled(false);
                buffersize.setEnabled(false);
                filename.setEnabled(false);
            }
            else {
                hasbuffer.setSelected(true);
                buffersize.setFields(adi.buffersize);
                filename.setText(adi.filename.getName());
                backsrc.setFields(adi.backsrc);
                backdest.setFields(adi.backdest);
            }
        }
        //colors
        {
            for (int i=0; i<colors.length;i++){
                if (adi.color[i] != null){
                    colors[i].setColor(adi.color[i]);
                }
            }
        }
        //comments
        comments.setComments(adi.comments);
        //this.parent.repaint();
        
    }

	/** called by base class when an action event happens. */
	public boolean processActionEvent(ActionEvent e){
		Object obj = e.getSource();
		if (obj == hasbuffer){
			boolean val = hasbuffer.isSelected();
			hasbuffer.setSelected(val);
			backsrc.setEnabled(val);
			backdest.setEnabled(val);
			buffersize.setEnabled(val);
			filename.setEnabled(val);
			// event processed
			return true;
		}
		return false;
	}
}
