/*
 * UserPreference.java
 *
 * Created on February 10, 2005, 11:33 AM
 */
package cockpitEditor;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import pitComponents.pitDialogComponents.*;
import pitComponents.*;
import misc.RAFile;
import java.util.Vector;

/**
 *
 * @author mribeiro
 * Represents all user preferences, like colors, priority objects will be draw etc
 * I plan to do it writable to a file soon.
 * 
 */
public class UsPr {
    /** listeners of user preferences */
    private static Vector listeners = new Vector();
    
	/** this is the current working directory when user press open button in main window */
	public static File cwd;

	/** the maximum number of actions queued */
	public static int maxActions = 10;

	/** 
	 * 0 is completely transparent, 255 is opaque 
	 */
	protected static int alpha = 130;

	/** the vertical FoV angle, used for horizon drawing */
	public static double vFov = 46.4;

	/* These variables control how the objects should be draw*/
	/** Draw the pit showing labels */
	public static boolean drawLabel = false;
	/** Draw the pit using transparent borders */
	public static boolean drawAlpha = false;
	/** transparent buffers are slower than normal,
	 * Here we always draw then transparent
	 */
	public static boolean drawBuffersTrans = true;
	/** Draws the sky and terrain, with horizon line */
	public static boolean drawHorizon = true;

	/** here are the types of dat we have now, user can pick any one */
	//WARNING dont change the order here, else UsPR will pick wrong values
	public static final int DAT_NONE = 0, DAT_COBRA=1, DAT_OF = 2;
	/** unfortunately, we reached a point where we are no longer compatible with
	 * other exes. This variable indicates which options we are using(BMS/cobra) */
	public static int datFormat = DAT_NONE;    

	/** ambient light to be simulated. One for each RGB factor */
	private static float ambLight[] = {1.0f, 1.0f, 1.0f};


	/**
	 * The size of the stroke. Objects are outlined and this variable is the size
	 * of the stroke
	 */
	public static BasicStroke regularStroke = new BasicStroke((float)0.1);
	public static BasicStroke selectedStroke = new BasicStroke((float)0.2);

	/** 
	 * colors used by the system to outline objects. Alpha colors are used when
	 * user toggle to alpha mode(transparent). Other are all objects which are not,
	 * light, buttonview(bv), current object(curOb), and panel object(panelObj)
	 */
	public static Color panelObjColorAlpha = new Color(0, 255, 0, alpha),
				 bvColorAlpha = new Color(60, 220, 220, alpha),
				 lightColorAlpha = new Color(255, 0, 0, alpha),
				 curObColorAlpha = new Color(255, 154, 0, alpha),
				 masktopColorAlpha = new Color(120, 120, 120, alpha),
				 minPosColorAlpha = new Color(255, 30, 30, alpha),
				 maxPosColorAlpha = new Color(30, 255, 30, alpha),
				 otherColorAlpha = new Color(255, 255, 100, alpha),
				 panelObjColor = new Color(0, 255, 0),
				 bvColor = new Color(60, 220, 220),
				 lightColor = new Color(255, 0, 0),
				 curObColor = new Color(255, 154, 0),
				 masktopColor = new Color(120, 120, 120),
				 skyColor = new Color(93, 103, 200),
				 terrainColor = new Color(166, 107, 3),
				 minPosColor = new Color(255, 30, 30),
				 maxPosColor = new Color(30, 255, 30),
				 otherColor = new Color(255, 255, 100)
					 ;

	/** template colors, why is this separate from above colors??? */
	public static Color templateColor = new Color(255, 154, 0), 
				 templateColorAlpha = new Color(255, 154, 0, alpha);

	/**
	 * the zoom factors, used to scale the graphics context, modifying panel
	 * preferredSize and adjust mouse clicks. Cool stuff
	 */
	private static double zoomFactors[] = { 0.25f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 4.0f, 6.0f };

	/** indicates which zoom factor we are using. Must be between 0 and 
	 * zoomFactors.length-1.  By default, starts with 3, meaning 1.0f zoom
	 */
	private static int zoomInd = 3;     

	/** Creates a new instance of UserPreference */
	public UsPr() {
	}

	/**
	 * Resets the preferences to defaul values. Useful if the user mess around and
	 * wnats to reset stuff
	 */
	public static void resetDefaults(){
		alpha = 130;
		panelObjColorAlpha = new Color(0, 255, 0, alpha);
		otherColorAlpha = new Color(255, 255, 100, alpha);
		bvColorAlpha = new Color(60, 220, 220, alpha);
		lightColorAlpha = new Color(255, 0, 0, alpha);
		curObColorAlpha = new Color(255, 154, 0, alpha);
		panelObjColor = new Color(0, 255, 0);
		otherColor = new Color(255, 255, 100);
		bvColor = new Color(60, 220, 220);
		lightColor = new Color(255, 0, 0);
		curObColor = new Color(255, 154, 0);
	}

	/**
	 * loads the user preferences from the given file. If any error occurs, loads
	 * default preferences for that part.
	 *
	 * @param arq the file used for reading the preferences
	 */
	public static void loadUserPreferences(File f){
	    try {

	    }
	    // if load fails, reset default and erase save file
	    catch (Exception e){

	    }
	}

	/**
	 * saves user preferences to the given file. If the given
	 * file does not exist, a user dialog must be shown indicating
	 * the error. 
	 *
	 * @param arq the file user for wrinting the preferences to
	 */
	public static void saveUserPreferences(File f){
	    // basic check
	    try {
		//open file for writing
		RAFile raf = new RAFile(f, "w"); 

		//write UsPr attributes. the comment explains each one
		raf.writeln("// max number of actions queued for undo");
		raf.writeln("maxActions = " + maxActions);

		raf.writeln("// alpha values for contour in transparent mode. 0 iscompletely transparent while 255 is opaque (solid)");
		raf.writeln("alpha = " + alpha);

		// raf.writeln("// vFov: this is the vertical field of view falcon uses");
		// skip vFov as its internal only

		raf.writeln("// draw object labels? (L key toggles)");
		raf.writeln("drawLabel = " + drawLabel);

		raf.writeln("// draw objects contour transparently? (A key)");
		raf.writeln("drawAlpha = " + drawAlpha);

		// raf.writeln("// drawBuffer buffers transparently ()");
		// skip drawBuffer, always true

		// raf.writeln("// draw horizon line");
		// skipt it, always draw

		String datFormatS;
		if (datFormat == DAT_COBRA){
			datFormatS = "cobra";
		}
		else if (datFormat == DAT_OF){
			datFormatS = "openfalcon";
		}
		else {
			datFormatS = "sp3";
		}
		raf.writeln("// dat file format (sp3, cobra, openfalcon)");
		raf.writeln("datFormat = " + datFormatS);

		// skip image type

		raf.writeln("// stroke size: the size of contour. This affects mainly zoom in if its less than 1.0 (1 pix)");
		raf.writeln("// regular are for non selected objects, selected guess");
		raf.writeln("regularStroke = " + regularStroke);
		raf.writeln("selectedStroke = " + selectedStroke);

		raf.writeln("// object colors: define colors of contours (RGB format)");
		raf.writeln("bviewColor = " + bvColor.getRed() + ", " + bvColor.getGreen() + ", " + bvColor.getBlue() + " // buttonviews");
		raf.writeln("lightColor = " + lightColor.getRed() + ", " + lightColor.getGreen() + ", " + lightColor.getBlue() + " // lights");
		raf.writeln("otherColor = " +
				otherColor.getRed() + ", " + otherColor.getGreen() + ", " + otherColor.getBlue() + " // other objects (tapes, asi, etc) color"
				);
		raf.writeln("panelObjColor = " +
				panelObjColor.getRed() + ", " + panelObjColor.getGreen() + ", " + panelObjColor.getBlue() + " // panel objects (rwr, mdf and hud) color"		
				);
		raf.writeln("selectedColor = " + curObColor.getRed() + ", " + curObColor.getGreen() + ", " + curObColor.getBlue() + " // selected object");
		raf.writeln("masktopColor = " + masktopColor.getRed() + ", " + masktopColor.getGreen() + ", " + masktopColor.getBlue() + " // masktop line");
		raf.writeln("minPosColor = " + 
				minPosColor.getRed() + ", " + minPosColor.getGreen() + ", " + minPosColor.getBlue() + " // indicators min pos color"
				);
		raf.writeln("maxPosColor = " + 
				maxPosColor.getRed() + ", " + maxPosColor.getGreen() + ", " + maxPosColor.getBlue() + " // indicators max pos color"
				);
		raf.writeln("templateColor = " +
				templateColor.getRed() + ", " + templateColor.getGreen() + ", " + templateColor.getBlue() + " // template objects color"
				);

		// skip zoom, always start at 1.0	

		raf.writeln("ckptDir = " + cwd.getName() + "// default directory for the open button");

		// close os
		raf.close();

	    }
	    catch (FileNotFoundException fnfe){
		//user dialog showing file was not found
		System.err.println("Error! file " + f.getName() + " not found");
	    }
	    catch (IOException ioe){
		//user dialog showing error on write and file 
		System.err.println("Error writing saved file " + f.getName());
	    }	

	}

	/**
	 * Shows the user preferences window
	 */
	public static void showUserPreferences(JFrame parent){
	    UsPrDialog upd = new UsPrDialog(parent);
	    upd.setVisible(true);
	}

	/**
	 * Gets zoom value
	 */
	public static double getZoom(){
	    return zoomFactors[zoomInd];
	}

	/** Zoom in, increase detail */
	public static void zoomIn(){
	    if (zoomInd < (zoomFactors.length - 1)){
		zoomInd++;
		// notify listeners
		for (int i=0;i<listeners.size();++i){
		    ((UsPrListener)(listeners.get(i))).zoomChanged(getZoom());
		}
	    }
	}

	/** zoom out, show more */
	public static void zoomOut(){
	    if (zoomInd > 0){
		zoomInd--;
		// notify listeners
		for (int i=0;i<listeners.size();++i){
		    ((UsPrListener)(listeners.get(i))).zoomChanged(getZoom());
		}		
	    }
	}

	/** no zoom */
	public static void zoomOff(){
		zoomInd = 3;
	}

	/** returns ambient light, RGB float format */
	public static float[] getAmbientLightF(){
		return ambLight;
	}

	/** toggles night mode.This enters and leaves night mode 
	 */
	public static void toggleNightMode(){
		// ambient light switch
		for (int i=0;i<3;i++){
			ambLight[i] = (ambLight[i] != 0.3f) ? 0.3f : 1.0f;
		}

		if (ambLight[0] > 0.3f){
			// turns lights off
			Cockpit.setFloodMode(Cockpit.FLOOD_OFF);
			Cockpit.setInstMode(Cockpit.INST_OFF);
		}
		else {
			// lights on, inst HIGH
			Cockpit.setFloodMode(Cockpit.FLOOD_LOW);
			Cockpit.setInstMode(Cockpit.INST_HIGH);
		}
	
		// call reload as images need to be updated
		Cockpit.reloadImages();
	}

	/** returns if its daytime or night time */
	public static boolean isDayTime(){
		return (ambLight[0] > 0.3f);
	}
	
	//////////////////////	
	// USRPRF LISTENERS //
	//////////////////////
	/** adds a listener to UsPr listeners */
	public static void addUsPrListener(UsPrListener ul){
	    listeners.add(ul);
	}
	/** removes a listener from UsPr listeners */
	public static void removeUsPrListener(UsPrListener ul){
	    listeners.remove(ul);
	}
	/** removes all listeners */
	public static void removeAllUsPrListeners(){
	    listeners.clear();
	}
}

/**
 * The dialog with all user preferences. This dialog has a save button, an update 
 * and a reset default button. Tere are 2 tabs, one for the colors and another
 * one for the priorities objects will be drawn.
 */
class UsPrDialog extends JDialog 
	implements ActionListener, ChangeListener{
	//buttons
	JButton applyBt = new JButton("Apply");
	JButton saveBt = new JButton("Save");
	JButton rdBt = new JButton("Reset Defaults");
	JButton closeBt = new JButton("Close");

	/** the alpha factor, transparency. 0 is translucid, 255 is opaque*/
	JSlider alphaFactor = new JSlider(JSlider.HORIZONTAL, 0, 255, 130);
	//the color fields
	/** objects colorfield */
	ColorField obCF = new ColorField("Objects Color", 
			"Objects color, does not include buttonviews, nor light");
	/** buttonviews colorfield */
	ColorField bvCF = new ColorField("Buttonviews Color", 
			"Buttonviews color");
	/** panel objs colors, mfd, hud, rwr */
	ColorField pnCF = new ColorField("MFD, HUD and RWR Color", 
			"MFD, HUD and RWR color");
	/** current object color */
	ColorField crCF = new ColorField("Current object Color", 
			"The selected object color");
	/** lights color */
	ColorField ltCF = new ColorField("Lights Color", 
			"Lights color");

	/** dat format */
	String datFormatStrings[] = {"default", "cobra", "open falcon"};
	JComboBox datFormat = new JComboBox(datFormatStrings);    

	public UsPrDialog(JFrame parent){
	    super(parent);
	    setProperties();
	    this.setLocationRelativeTo(parent);
	}

	public UsPrDialog(JDialog parent){
	    super(parent);
	    setProperties();
	    this.setLocationRelativeTo(parent);
	}

	private void setProperties(){
	    this.setTitle("User preferences");
	    this.setSize(500, 400);
	    this.setModal(true);
	    Container c = this.getContentPane();
	    c.setLayout(new BorderLayout());

	    //dat format option, top
	    JPanel datPanel = new JPanel();
	    datPanel.add(new JLabel("Dat format"));
	    datPanel.add(datFormat);
	    c.add(datPanel, BorderLayout.NORTH);

	    //the buttons at the bottom
	    JPanel bp = new JPanel();
	    bp.add(applyBt);
	    bp.add(saveBt);
	    saveBt.setEnabled(true);
	    bp.add(rdBt);
	    bp.add(closeBt);
	    closeBt.setForeground(Color.RED);
	    closeBt.addActionListener(this);
	    saveBt.addActionListener(this);
	    rdBt.addActionListener(this);
	    applyBt.addActionListener(this);
	    applyBt.setToolTipText("apply changes");
	    saveBt.setToolTipText("Saves your preferences to a file, " +
			    "theyll be used again when program is restarted");
	    rdBt.setToolTipText("Restore default settings");
	    c.add(bp, BorderLayout.SOUTH);

	    //the tabbed pane
	    JTabbedPane tp = new JTabbedPane(JTabbedPane.LEFT);
	    //colors
	    {
		    JPanel cp = new JPanel(new BorderLayout());

		    //slider
		    JPanel sp = new JPanel();
		    alphaFactor.setToolTipText(
				    "The transparency factor when A key is pressed");
		    alphaFactor.addChangeListener(this);
		    sp.add(new JLabel("Transparency(- +):"));
		    sp.add(alphaFactor);
		    cp.add(sp, BorderLayout.NORTH);

		    //colors
		    JPanel cp2 = new JPanel(new GridLayout(5, 1));
		    cp2.add(crCF);
		    cp2.add(bvCF);
		    cp2.add(ltCF);
		    cp2.add(obCF);
		    cp2.add(pnCF);

		    cp.add(cp2, BorderLayout.CENTER);
		    tp.add(cp, 0);
		    tp.setTitleAt(0, "Colors");
	    }

	    //priorities
	    {

	    }

	    c.add(tp, BorderLayout.CENTER);

	    load();
	}

	public void actionPerformed(ActionEvent e){
		JButton bt = (JButton)e.getSource();

		if (bt == applyBt){
			//update stuff
			update();
		}
		else if (bt == saveBt){
			//update and save stuff
		}
		else if (bt == rdBt){
			//restore default and update stuff
		}
		else if (bt == closeBt){
			this.dispose();
		}
	}

	public void stateChanged(ChangeEvent ce){
	}

	/**
	 * loads the current settings, called whenever this dialog is called.
	 */
	private void load(){
		alphaFactor.setValue(255 - UsPr.alpha);
		crCF.setColor(new PitColor(UsPr.curObColor));
		ltCF.setColor(new PitColor(UsPr.lightColor));
		bvCF.setColor(new PitColor(UsPr.bvColor));
		pnCF.setColor(new PitColor(UsPr.panelObjColor));
		obCF.setColor(new PitColor(UsPr.otherColor));

		//dat format
		datFormat.setSelectedIndex(UsPr.datFormat);
	}

	/**
	 * Updates the variables, according to user setting, but do not save them
	 */
	private void update(){
		//dat format
		UsPr.datFormat = datFormat.getSelectedIndex();

		//lets get the transparency factor
		int tf = 255 - alphaFactor.getValue();
		UsPr.alpha = tf;

		//lets get the colors now
		Color c = null;

		try {
			//current obj
			c = crCF.getColor().toColor();
			UsPr.curObColor = c;
			UsPr.curObColorAlpha = 
				new Color(c.getRed(), c.getGreen(), c.getBlue(), tf);
		}
		catch (Exception e){

		}


		try {
			//buttonviews
			c = bvCF.getColor().toColor();
			UsPr.bvColor = c;
			UsPr.bvColorAlpha = 
				new Color(c.getRed(), c.getGreen(), c.getBlue(), tf);
		}
		catch (Exception e){

		}

		try {
			//panel  objects
			c = pnCF.getColor().toColor();
			UsPr.panelObjColor = c;
			UsPr.panelObjColorAlpha = new Color(c.getRed(), c.getGreen(), c.getBlue(), tf);
		}
		catch (Exception e){

		}

		try {
			//lights
			c = ltCF.getColor().toColor();
			UsPr.lightColor = c;
			UsPr.lightColorAlpha = new Color(c.getRed(), c.getGreen(), c.getBlue(), tf);
		}
		catch (Exception  e){

		}

		try {
			//objects which are not panel
			c = obCF.getColor().toColor();
			UsPr.otherColor = c;
			UsPr.otherColorAlpha = 
				new Color(c.getRed(), c.getGreen(), c.getBlue(), tf);
		}
		catch (Exception e){

		}
	}
}

