/*
 * PitMirror.java
 *
 * Created on 1 de Setembro de 2007, 22:21
 */
package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import pitComponents.pitDialogComponents.AreasFieldPanel;
import pitComponents.pitDialogComponents.CommentsPane;
import pitComponents.pitDialogComponents.LabeledHexField;
import windows.PropertiesDialog;

/**
 * Represents a cockpit mirror
 * @author MatheusRibeiro
 */
public class PitMirror extends PitObject {
  
	/** Creates a new instance of PitMirror */
	public PitMirror(int id){
		super(id, 1, 1);
	}
  
	// PitObject abstract interface implementation
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
				if (token.equals("srcloc")) {
					srcloc[0] = new PitArea(false, true);
					srcloc[0].parseData(st);
				}
				else if (token.equals("destloc")){
					destloc[0] = new PitArea();
					destloc[0].parseData(st);
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
			if (comments != null){
				label = comments.getLabel();
			}
		}
		catch (Exception e) {
			throw new ParseErrorException("PitMirror.java: " + e);
		}		
	}
	
	public void writeComponent(misc.RAFile arq) throws Exception {
        try {
			arq.writeBytes(ls + toTextFormat() + ls);
        }
        catch (IOException io) {
            System.out.println("pitSound.java-> erro de IO: " + io);
        }		
	}
	
	protected PropertiesDialog getPropertiesDialog(JFrame parent){
		MirrorPropDialog mpd = new MirrorPropDialog(parent, this);
		return mpd;	
	}

	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		MirrorPropDialog mpd = new MirrorPropDialog(parent, this);
		return mpd;
	}
	
	public Object clone(){
		PitMirror m = new PitMirror(0);
		this.cloneMeTo(m);
		return m;
	}
	
	public String toTextFormat(){
        String s = new String();
        if (comments != null){
            s += comments;
        }
        s += "#" + id + " MIRROR" + ls;
        if (cyclebits == -1){
            s += "\tcyclebits = -1;" + ls;
        }
        else{
            s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +";" + ls;
        }
		s +="\tsrcloc = " + srcloc[0] + ";" + ls;		
        s +="\tdestloc = " + destloc[0] + ";" + ls;
        s +="#END" + ls;
        
        return s;   		
	}
}

class MirrorPropDialog extends PropertiesDialog implements ActionListener{
    PitMirror m;
    JTabbedPane tp = new JTabbedPane();

    //main
    LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);

    //areas
    AreasFieldPanel dest = new AreasFieldPanel(this, "destloc");
    AreasFieldPanel src = new AreasFieldPanel(this, "srcloc");
	
    //comments
    CommentsPane comments = new CommentsPane(this);
    public MirrorPropDialog(JFrame parent, PitMirror m){
        super(parent, m, "Mirror properties", false);
        setProperties(m);
    }

    public MirrorPropDialog(JDialog parent, PitMirror m){
        super(parent, m, "Mirror properties", true);
        setProperties(m);
    }
    
    private void setProperties(PitMirror m){
        this.m = m;
        this.setSize(400,400);
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("MIRROR #" + m.id, JLabel.CENTER));
            mainP.add(cyclebits);

            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");
        }
        //areas
        {
            JPanel areaP = new JPanel(new GridLayout(8,1));
            areaP.add(src);
			areaP.add(dest);

            tp.add("areas", areaP);
            tp.setToolTipTextAt(1, "areas");
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
        cyclebits.setValue(m.cyclebits);

        //areas
        dest.setFields(m.destloc[0]);
		src.setFields(m.srcloc[0]);
		
        //comments
        comments.setComments(m.comments);
    }

    protected void setValues() throws Exception {
		//update main
		m.cyclebits = cyclebits.getValue();

		//update areas
		m.destloc[0] = dest.getArea();
		m.srcloc[0] = src.getArea();

		//update comments
		m.comments = comments.getComments();
		if (m.comments != null){
			m.label = m.comments.getLabel();
		}
    }
}


