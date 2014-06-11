package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import windows.PropertiesDialog;


public class PitLight extends PitObject {

    public int initstate, cursorid;
    private int numSrcRead = 0;


    public PitLight(int id) {
        super(id);
        setNumDsts(1);
    }
    public Object clone(){
        PitLight l = new PitLight(0);
        this.cloneMeTo(l);
        l.initstate = initstate;
        l.numSrcRead = numSrcRead;
        return l;
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
                if ( (!token.endsWith("loc")) && (st.ttype != st.TT_NUMBER)) {
                    throw new ParseErrorException("expecting number token");
                }
                
                if (token.equals("states")) {
                    states = (int) st.nval;
                    setNumSrcs(states);
                }
                else if (token.equals("srcloc")) {
                    if (numSrcRead + 1 > states) {
                        throw new Exception("more srclocs than states");
                    }
                    srcloc[numSrcRead] = new PitArea(false, true);
                    srcloc[numSrcRead++].parseData(st);
                }
                else if (token.equals("destloc")) {
                    destloc[0] = new PitArea();
                    destloc[0].parseData(st);
                }
                else if (token.equals("callbackslot")) {
                    callbackslot = (int) st.nval;
                }
                else if (token.equals("cursorid")) {
                    cursorid = (int) st.nval;
                }
                else if (token.equals("persistant")) {
                    persistant = (int) st.nval;
                }
                else if (token.equals("initstate")) {
                    initstate = (int) st.nval;
                    if (initstate >= states){
                        currentState = 0;
                    }
                    else {
                        currentState = initstate;
                    }
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
            throw new ParseErrorException("pitLight.java: " + e);
        }
        
    }
    
    public void writeComponent(misc.RAFile arq) {
        int i = 0;
        try {
            if (comments != null){
                comments.write(arq);
            }
            arq.writeln("#" + id + " LIGHT");
            if (cyclebits == -1){
                arq.writeln("\tcyclebits = -1;");
            }
            else{
                arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                ";");
            }
            arq.writeln("\tcursorid = " + cursorid + ";");
            arq.writeln("\tstates = " + states + ";");
            arq.writeln("\tcallbackslot = " + callbackslot + ";");
            arq.writeln("\tinitstate = " + initstate + ";");
            arq.writeln("\tpersistant = " + persistant + ";");
            for (i = 0; i < states; i++) {
                arq.writeln("\tsrcloc = " + srcloc[i] + ";");
            }
            arq.writeln("\tdestloc = " + destloc[0] + ";");
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitLight.java-> erro de IO: " + io);
            
        }
    }
    
    public String toTextFormat(){
        String s = new String();

        if (comments != null){
            s += comments;
        }
        s += "#" + id + " LIGHT"+ls;
        if (cyclebits == -1){
            s += "\tcyclebits = -1;"+ls;
        }
        else{
            s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +
            ";"+ls;
        }
        s += "\tcursorid = " + cursorid + ";"+ls;
        s += "\tstates = " + states + ";"+ls;
        s += "\tcallbackslot = " + callbackslot + ";"+ls;
        s += "\tinitstate = " + initstate + ";"+ls;
        s += "\tpersistant = " + persistant + ";"+ls;
        for (int i = 0; i < states; i++) {
            s += "\tsrcloc = " + srcloc[i] + ";"+ls;
        }
        s += "\tdestloc = " + destloc[0] + ";"+ls;
        s += "#END"+ls;
            
        return s;
    }
    
    public void draw(Graphics2D g){
        //draw other state if we must
        if (currentState > 0) {
            super.drawSrcDst(g, currentState, 0);
        }
        else {
            //draws only the rectangle
            destloc[0].draw(g);
        }
        
    }
    
    public void drawLabeled(Graphics2D g){
        //draw other state if we must
        if (currentState > 0) {
            super.drawSrcDstLabeled(g, currentState, 0);
        }
        else {
            //draws only the rectangle
            if (label!= null){
                destloc[0].drawText(label.toString(), g);
            }
            destloc[0].draw(g);
        }
    }
    
    public String check(){
        String s = super.check();
        if (initstate >= states){
            s = s.concat("Light "+ this.id +
            ": initstate is bigger than number of states");
        }
        
        return s;
    }

    protected PropertiesDialog getPropertiesDialog(JFrame parent){
        LightPropDialog lpd = new LightPropDialog(parent, this);
        return lpd;
    }
	
	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
        LightPropDialog lpd = new LightPropDialog(parent, this);
        return lpd;
    }
}

class LightPropDialog extends PropertiesDialog {
    PitLight light;
    JTabbedPane tp = new JTabbedPane();

    //main
    LabeledNumField callbackslot = new LabeledNumField(this, "callbackslot", 5);
    LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);
    LabeledNumField initstate = new LabeledNumField(this, "initstate", 5);
    LabeledNumField cursorid = new LabeledNumField(this, "cursorid", 5);
    LabeledCBox persistantcb = new LabeledCBox("persistant");

    //areas
    JButton makeSameBt;
    LabeledNumField statesField =
      new LabeledNumField(this, "states", "number of states", 8);
    JButton upsrc = new JButton("update");
    AreasFieldPanel dest = new AreasFieldPanel(this, "destloc");
    AreasFieldPanel srcs[] = new AreasFieldPanel[PitButtonView.MAX];

    //comments
    CommentsPane comments = new CommentsPane(this);
    public LightPropDialog(JFrame parent, PitLight light){
        super(parent, light, "Light Properties", false);
        setProperties(light);
    }
    
    public LightPropDialog(JDialog parent, PitLight light){
        super(parent, light, "Light Properties", true);
        setProperties(light);
    }
    
    private void setProperties(PitLight light){
        super.enableStateButtons();
        this.light = light;
        this.setSize(400,400);
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("Light #" + light.id, JLabel.CENTER));
            mainP.add(callbackslot);
            mainP.add(cyclebits);
            mainP.add(cursorid);
            mainP.add(initstate);
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

            //numstates
            statesField.add(upsrc);
            upsrc.addActionListener(this);
            areasP.add(statesField);

            //srclocs
            for (int i=0;i<PitButtonView.MAX;i++){
                srcs[i] = new AreasFieldPanel(this, "src" + i + ":", false);
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
        persistantcb.setSelected(light.persistant == 1);
        cyclebits.setValue(light.cyclebits);
        cursorid.setValue(light.cursorid);
        initstate.setValue(light.initstate);
        callbackslot.setValue(light.callbackslot);

        //areas
        statesField.setValue(light.states);
        dest.setFields(light.destloc[0]);
        for (int i=0;i<PitButtonView.MAX;i++){
            if (i < light.states){
                //enable and setvalue
                srcs[i].setEnabled(true);
                srcs[i].setFields(light.srcloc[i]);
            }
            else {
                srcs[i].setEnabled(false);
            }
        }

        //comments
        comments.setComments(light.comments);
    }

	protected void setValues() throws Exception {
		//update main
		{
			if (persistantcb.isSelected()) {
				light.persistant = 1;
			}
			else {
				light.persistant = 0;
			}
			light.cyclebits = cyclebits.getValue();
			light.callbackslot = callbackslot.getValue();
			light.cursorid = cursorid.getValue();
			light.initstate = initstate.getValue();
		}
		//update areas
		{
			//destloc
			light.destloc[0] = dest.getArea();
			light.states = statesField.getValue();
			light.setNumSrcs(light.states);
			for (int i = 0; i < light.srcloc.length; i++) {
				light.srcloc[i] = srcs[i].getArea();
			}
		}
		//update comments
		{
			light.comments = comments.getComments();
			if (light.comments != null){
				light.label = light.comments.getLabel();
			}
		}
    }
	
	protected boolean processActionEvent(ActionEvent e){
		Object o = e.getSource();

		if (o == makeSameBt){
			PitLight bv = this.light;
			light.equalSrclocsToDest(0);
			//update area src values
			for (int i=0;i<bv.states;i++){
				srcs[i].setFields(light.srcloc[i]);
			}
			return true;
		}        
		else if (o == upsrc){
			int num=0;
			try {
				num = statesField.getValue();
			}
			catch (Exception ex){
				num = 0;
			}

			for (int i = 0; i < PitButtonView.MAX; i++) {
				if (i < num) {
					//enabl
					srcs[i].setEnabled(true);
				}
				else {
					srcs[i].setEnabled(false);
				}
			}
			return true;
		}
		return false;
	}
}

