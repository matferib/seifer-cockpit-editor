package pitComponents.pitObjects;

import pitComponents.pitHelperComponents.*;
import pitComponents.*;
import pitComponents.pitObjects.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import windows.PropertiesDialog;

//this is not a real object, but share lots of it
public class PanelMFD extends PitObject {
    public static final int MFDL=0, MFDR=1, MFD3=2, MFD4=3;

    /** default values for osbs*/
    private static float osbValues[][] = new float[][] {
	{-0.7f, 0.95f},
	{-0.355f, 0.95f}, 
	{0.0f, 0.95f},
	{0.34f, 0.95f},
	{0.68f, 0.95f},
	{0.95f,  0.735f},
	{0.95f, 0.39f},
	{0.95f, 0.03f},
	{0.95f, -0.315f},
	{0.95f, -0.66f},
	{0.695f, -0.85f},
	{0.35f, -0.85f},
	{0.01f, -0.85f},
	{-0.33f, -0.85f},
	{-0.66f, -0.85f},
	{-0.95f, -0.66f},
	{-0.95f, -0.315f},
	{-0.95f, 0.03f},
	{-0.95f, 0.39f},
	{-0.95f, 0.735f}
    };
    
    /** Osbs coordinates come in pairs of float(y,x). 
     * The MFD center is considered 0.0, 0.0. The mfd limits are 1.0, and -1.0.
     * Y+ is up, instead of down
     * In editor, osbs have 2 representation: the osbs (float) and 
     * destloc[1]-[20]. They must be kept synchronized all the time
     * When osbs is NULL, MFD does not have OSBs
     */
    public float[][] osbs = null;
    
    /** mfd sufix: right, left, 3, 4 */
    public String mySuf;

    /** creates a new MFD with no OSBs */
    public PanelMFD(int which) throws Exception {
        //we dont have an ID... so well use it to say which MFD we are
        //and 0 srclocs and 21 destlocs (1 for MFD and 20 for osbs)
        super(which, 0, 21);
        switch (which){
            case (MFDL): mySuf = "left";
            break;
            case (MFDR): mySuf = "right";
            break;
            case (MFD3): mySuf = "3";
            break;
            case (MFD4): mySuf = "4";
            break;
            default:
                //panel does not exist
                throw new Exception("PanelMFD.java: panel "+ which +" does not exist");
        }
    }

    /** creates the MFD based on a rectangle 
     * @param withOsbs create osbs with default position
     */
    public PanelMFD(int which, Rectangle r, boolean withOsbs) throws Exception {
        this(which);
        destloc[0] = new PitArea(r);
	if (withOsbs){
	    createOSBs();
	}
    }
    
    
    /* OSB related functions */
    /** create osbs with default positions */
    public void createOSBs(){
	if (osbs != null){
	    return;
	}
	//osbs 20 XY
        osbs = new float[20][2];
        for (int i=0; i<20; i++){
            osbs[i][0] = osbValues[i][0];
	    osbs[i][1] = osbValues[i][1];
        }
	// create the area for OSBs
	OSBsFloatToArea();
    }
    
  
    /** converts a osbs to a pit area 
     * @param which 1-20, indicating which osb
     */
    private void OSBFloatToArea(int which){	
	if ((which <1) || (which > 20) || (osbs == null)){
	    return;
	}
	// osb dimensions, based on MFD size
	int owidth = this.destloc[0].rectangle.width/15;
	int oheight = this.destloc[0].rectangle.height/15;
	// adjustment (since point is center)
	int xoff = -owidth/2;
	int yoff = -oheight/2;
	// get osb xy center
	Point p  = this.destloc[0].getCoordinate(osbs[which-1][0], osbs[which-1][1]);

	// this rectangle will be osb area
	Rectangle r = new Rectangle();
	// osb coordinates
	r.x = p.x + xoff;
	r.width = owidth;
	r.y = p.y + yoff;
	r.height = oheight;
	// create the destloc
	destloc[which] = new PitArea(r);
	destloc[which].setResizable(false);
    }
    
    /** converts all OSBs from float to areas */
    protected void OSBsFloatToArea(){
        if (osbs == null){
	    return;
	}
	for (int i = 1; i <= 20; ++i){
	    OSBFloatToArea(i);
	}
    }
    
    /** converts osb from area to float */
    private void OSBAreaToFloat(int which){
	if ((which <1) || (which > 20) || (osbs == null) || (destloc[which] == null)){
	    return;
	}
	// osb and mfd center 
	Point oc = destloc[which].getCenter();
	Point mc = destloc[0].getCenter();
	osbs[which-1][0] = (oc.x - mc.x) / (destloc[0].rectangle.width / 2.0f);
	osbs[which-1][1] = -((oc.y - mc.y) / (destloc[0].rectangle.height / 2.0f));
    }

    /** converts all osbs from area to floats */
    protected void OSBsAreaToFloat(){
	if (osbs == null){
	    return;
	}
	for (int i=1;i<=20;++i){
	    OSBAreaToFloat(i);
	}
    }   
    /* end osb functions */
    
    public Object clone(){
        PanelMFD mfd = null;
        try {
            mfd = new PanelMFD(this.id);
	    mfd.osbs = (float[][])osbs.clone();
        }
        catch (Exception e){
            
        }
        
        this.cloneMeTo(mfd);
        return mfd;
    }


    public void parseData(PitTokenizer st) throws ParseErrorException{
        destloc[0] = new PitArea();
        destloc[0].parseData(st);
    }
    
    public void parseOSB(StreamTokenizer st) throws Exception{
        osbs = new float[20][2];
        for (int i = 0; i < 20; ++i) {
            if (st.ttype != st.TT_NUMBER) {
                throw new Exception("PanelMFD.java: osb expecting number");
            }
            osbs[i][0] = (float)st.nval;
	    st.nextToken();
	    osbs[i][1] = (float)st.nval;
            st.nextToken();
        }
	// converts osb from float to areas
	OSBsFloatToArea();
        st.pushBack();
    }
    
    public String toTextFormat() {
        String s = new String();
        s += "\tmfd" + mySuf + " = " + destloc[0] + ";"+ ls;
        if (osbs != null){
            s += "\tosb" + mySuf + " =";
            for (int i = 0; i < 20; i++) {
                s += " " + osbs[i][0] + " " + osbs[i][1];
            }
            s += ";"+ ls;
        }
        return s;
    }
    
    public void writeComponent(misc.RAFile arq) throws Exception{
        arq.writeln("\tmfd" + mySuf + " = " + destloc[0] + ";");
        if (osbs != null){
            arq.writeBytes("\tosb" + mySuf + " =");
            for (int i = 0; i < 20; i++) {
                arq.writeBytes(" " + osbs[i][0] + " " + osbs[i][1]);
            }
            arq.writeln(";");
        }
    }

	public void draw(Graphics2D g){
		destloc[0].draw(g);
		destloc[0].drawText("mfd" + mySuf, g);
		if (osbs != null){
			for (int i=1;i<21;++i){
				destloc[i].draw(g);
				destloc[i].drawText(Integer.toString(i), g);
			}
		}
	}

	
    protected PropertiesDialog getPropertiesDialog(JFrame parent){
        MfdPropDialog p = new MfdPropDialog(parent, this);
        return p;
    }

    protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
        MfdPropDialog p = new MfdPropDialog(parent, this);
        return p;
    }

    /* move and resize:
     * These functions are different, since we need to remake osbs
     */
    
    /** resize only works on MFD, not osbs
     * once MFD is resized, all osbs need to be resized
     */
    public boolean resize(int xinc, int yinc, int target){
	// resize MFD
	if (destloc[0].resize(xinc, yinc, target, Cockpit.resolution)){
	    // redo all osbs if MFD resized
	    OSBsFloatToArea();
	    return true;
	}
	else {
	    return false;
	}
	
    }
    
    /** move: of mfd is moved, all osbs need to be moved also */
    public boolean move(int xoff, int yoff) {
	if (currentSelectedDest == 0){
	    return moveAll(xoff, yoff);
	}
	else{
	    // move osb and save its new float representation
	    if (super.move(xoff, yoff)){
		OSBAreaToFloat(currentSelectedDest);
		return true;
	    }
	    else {
		return false;
	    }
	}
    }
    
    /** move all areas for MFD 
     * move MFD and OSBs
     */
    public boolean moveAll(int xoff, int yoff) {
	if (
	    (destloc[0].move(xoff, yoff, Cockpit.resolution)) &&
	    (osbs != null)
	){
	    // move all
	    for (int i=1;i<=20;i++){
		destloc[i].move(xoff, yoff, Cockpit.resolution);
	    }
	    return true;
	    // no need to redo areas here, since MFD is moved also
	    // and relative position is the same
	}
	else {
	    return false;
	}
    }    
}

class MfdPropDialog extends PropertiesDialog {
    private PanelMFD mfd; //the object we create

    // area fields text tips
    private static String fieldTips[] = {
      "Up Left X", "Up Left Y", "Down Right X", "Down Right Y"},
      osbTips[] = {"x","y"};

    //the textfields
    private JTextField
      //the dest text fields
      destFields[] = new JTextField[4],
      osbFields[][] = new JTextField[20][2];
   
    public MfdPropDialog(JFrame parent, PanelMFD mfd){
        super(parent, mfd, "MFD properties", false);
        setProperties(mfd);
    }
    
    public MfdPropDialog(JDialog parent, PanelMFD mfd){
        super(parent, mfd, "MFD properties", true);
        setProperties(mfd);
    }
    
    public void setProperties(PanelMFD mfd){
        //window stuff
        this.mfd = mfd;
        this.setSize(380, 350);
        Container c = this.getContentPane();
        BorderLayout bl = new BorderLayout();
        c.setLayout(bl);

        //the main panel has only a JTabbedPane
        //the tabbedPane
        JTabbedPane tp = new JTabbedPane(JTabbedPane.LEFT);
        tp.setBackground(Color.lightGray);

        {
            //destP: the panel of this tab
            JPanel destP = new JPanel();

            //Title
            JPanel p2 = new JPanel(); //dest
            p2.setLayout(new GridLayout(2, 1));
            p2.add(new JLabel("Destination for " + mfd.mySuf + " mfd"));
            p2.add(new JLabel("format: uplX  uplY  drX  drY"));
            destP.add(p2);

            //fields
            p2 = new JPanel();
            p2.setLayout(new FlowLayout(FlowLayout.LEFT));
            for (int j = 0; j < 4; j++) {
                destFields[j] = new JTextField();
                destFields[j].setColumns(4);
                destFields[j].setToolTipText(fieldTips[j]);
                p2.add(destFields[j]);
            }
            destP.add(p2);

            //add the panel to the tabbed pane
            tp.addTab("Dest", destP);
            tp.setToolTipTextAt(0, "destination area");
        }
        {
            //osbs
            JPanel osbsPanel = new JPanel(new BorderLayout());

            //title
            osbsPanel.add(new JLabel("Osbs for " + mfd.mySuf + " MFD"),
                         BorderLayout.NORTH);

			JPanel ofPanel = new JPanel(new GridLayout(20,1));
			for (int i=0;i<20;i++){
				JPanel auxPanel = new JPanel();
				if ((i+1)<10){
                    auxPanel.add(new JLabel("osb0"+ (i+1) + "-"));
				}
				else {
					auxPanel.add(new JLabel("osb"+ (i+1) + "-"));
				}

				if (mfd.osbs != null){
					//the fields
					osbFields[i][0] = new JTextField(6);
					osbFields[i][1] = new JTextField(6);
					osbFields[i][0].setToolTipText(osbTips[0]);
					osbFields[i][1].setToolTipText(osbTips[1]);
					//seems falcon use X Y for OSBs
					osbFields[i][0].setText(Float.toString(mfd.osbs[i][0]));
					osbFields[i][1].setText(Float.toString(mfd.osbs[i][1]));

					auxPanel.add(new JLabel("x:"));
					auxPanel.add(osbFields[i][0]);
					auxPanel.add(new JLabel("y:"));
					auxPanel.add(osbFields[i][1]);
					ofPanel.add(auxPanel);
				}
			}

            JScrollPane osbFieldsPane = new JScrollPane(ofPanel);
            osbFieldsPane.setHorizontalScrollBarPolicy(
              JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            osbsPanel.add(osbFieldsPane, BorderLayout.CENTER);
            tp.add("osbs", osbsPanel);
            tp.setToolTipTextAt(1, "osbs data");
        }

        //add the tabbed pane
        c.add(tp, BorderLayout.CENTER);

        //create/cancel buttons
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(c.getWidth(), 40));

        updateBt.addActionListener(this);
        p.add(updateBt);
        updateBt.setForeground(Color.green);

        closeBt.addActionListener(this);
        closeBt.setForeground(Color.red);
        p.add(closeBt);
        c.add(p, BorderLayout.SOUTH);
		
		loadValues();
    }

	protected void loadValues(){
		//sets the fields text
		destFields[0].setText(Integer.toString(mfd.destloc[0].upl.x));
		destFields[1].setText(Integer.toString(mfd.destloc[0].upl.y));
		destFields[2].setText(Integer.toString(mfd.destloc[0].dor.x));
		destFields[3].setText(Integer.toString(mfd.destloc[0].dor.y));
		
		for (int i=0;i<20;i++){
			if (mfd.osbs != null){
				//the fields
				osbFields[i][0].setText(Float.toString(mfd.osbs[i][0]));
				osbFields[i][1].setText(Float.toString(mfd.osbs[i][1]));
			}
		}		
	}
	
	protected void setValues() throws Exception {
		Rectangle r = new Rectangle();
		r.x = Integer.parseInt(destFields[0].getText());
		r.y = Integer.parseInt(destFields[1].getText());
		r.width = Integer.parseInt(destFields[2].getText()) - r.x + 1;
		r.height = Integer.parseInt(destFields[3].getText()) - r.y + 1;
		if (r.width <= 0){
			throw new Exception("Negative width");
		}
		else if (r.height <= 0){
			throw new Exception("Negative height");
		}
		mfd.destloc[0] = new PitArea(r);

		for (int i=0;i<20;i++){
			//read the osbs (XY)
			mfd.osbs[i][0] = Float.parseFloat(osbFields[i][0].getText());
			mfd.osbs[i][1] = Float.parseFloat(osbFields[i][1].getText());
		}
		mfd.OSBsFloatToArea();
	}
}