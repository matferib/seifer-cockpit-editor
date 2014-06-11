package pitComponents;

import pitComponents.pitDialogComponents.AreasFieldPanel;
import pitComponents.pitDialogComponents.LabeledCBox;
import pitComponents.pitDialogComponents.LabeledNumField;
import pitComponents.pitHelperComponents.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import pitComponents.pitDialogComponents.ColorField;
import windows.PropertiesDialog;

/** The manager holds information about diffferent things in the 2d cockpit. 
* It counts how many buttons the pit has, surfaces, objects etc. 
* Those values are used for memory allocation, so it is important they are kept right. 
* Manager also holds data for the 1 view (hud), altpanel, 3d lod to draw outside and others.
* 
* This class does not extend PitObject, because its not drawn. Manager entries
* are updated while saving, so the program does not keep track of it. Is safer
* to do it this way.
*/
public class PitManager {
	//some constants
	final int ADI = 1, AREA = 2, BUFFER = 3, BUTTON = 4, BUTTONVIEW = 5,
		CHEVLIFT = 6,
		DED = 7, DIAL = 8, DIGITS = 9, FILE = 10, HSI = 11, INDICATOR = 12,
		KNEEVIEW = 13,
		LIGHT = 14, MACHASI = 15, MANAGER = 16, PANEL = 17, SOUND = 18,
		SURFACE = 19, TEXT = 20,
		OBJECT = 21
	;
    
	/** Version string was added so that exe could keep different cockpit features and 
	* remain compatible with previous versions. Its composed of 2 numbers,
	* major and minor. FF5 is version 1.0.
	*/
	private int majorVersion, minorVersion;
	
	/** Cockpit interior light color, aka floodlight. Used in freefalcon version, this applies lighting
	* to cockpit panels and instruments. Initialized with falcon default color.
	*/
	private PitColor floodlight = new PitColor(new Color(0.35f, 0.15f, 0.15f));

	/** Cockpit instrument light color. These are applied to the brighter colors (last 48 colors of palette).
	* Initialized with falcon default color
	*/
	private PitColor instlight = new PitColor(new Color(1.0f, 0.15f, 0.15f));
			
    public PitComment comments;
    private int id;
    private int mouseBorder, hudfont, dedfont, mfdfont, popupfont, saboxfont,
			kneefont, generalfont, labelfont;
    public int numsurfaces, numpanels, numcursors, numobjects, numbuttons,
			numbuttonviews, numsounds, altpanel = 1100;
    private PitArea hud;
    private boolean doGeometry;
    private File cwd;
    private JTextArea mensagens;
    private int cockpit2d[]; //the 2 numbers are parent LOD for dogfight and TE/CA
    
    /**
     * Creates the manager, with the given id. Usually, its 0.
     *
     * @param id the manager id, usually 0.
     */
    public PitManager(int id) {
        this.id = id;
        numsurfaces = numpanels = numcursors = numobjects = numbuttons =
        numbuttonviews =
        numsounds = mouseBorder = hudfont = dedfont = mfdfont = popupfont =
        saboxfont =
        kneefont = generalfont = labelfont = altpanel = -2;
    }
    
	/** shows manager properties. User can change only those that are static
	*/
	public void showProperties(JFrame parent){
		ManagerPropertiesDialog mpd = new ManagerPropertiesDialog(parent, this);
		mpd.setVisible(true);
	}
    
	/* Checks if the manager properties are right. When saved, many properties
	* are automatically corrected.
	*
	* @return the String with errors found
	*/
	public String check(){
        String s = new String();
        
        //check allocation values
        if (Cockpit.buttons.getNumComponents() != numbuttons) {
            s = s.concat("manager says " + numbuttons +
                               " buttons, but found " +
                               Cockpit.buttons.getNumComponents() + "\n");

        }
        else if (Cockpit.bviews.getNumComponents() != numbuttonviews) {
            s = s.concat ("manager says " + numbuttons +
                               " buttonsviews, but found " +
                               Cockpit.bviews.getNumComponents()+ "\n");
        }
        else if (Cockpit.surfs.getNumComponents() != numsurfaces) {
            s = s.concat("manager says " + numsurfaces +
                               " surfaces, but found " +
                               Cockpit.surfs.getNumComponents()+ "\n");
        }
        else if (Cockpit.panels.getNumComponents() != numpanels) {
            s = s.concat("manager says " + numbuttons +
                               " panels, but found "
                               + Cockpit.panels.getNumComponents()+ "\n");
        }
        else if (Cockpit.sounds.getNumComponents() != numsounds) {
            s = s.concat("manager says " + numbuttons +
                               " sounds, but found " +
                               Cockpit.sounds.getNumComponents()+ "\n");
        }
        else if (Cockpit.objs.getNumComponents() != numobjects) {
            s = s.concat("WARNING: manager says " + numobjects +
                               " objects, but found " +
                               Cockpit.objs.getNumComponents()+ "\n");
        }
        
        return s;
    }
    
    /**
     * Parses the manager from a file. The tokenizer must be pointing prior to it.
     *
     * @parameter st the tokenizer
     * @throws ParseErrorException if it finds a syntax error or any other io error
     */
    public void parseData(PitTokenizer st) throws ParseErrorException {
        
		String token = null;
		int numToken = 0, n1 = 0, n2 = 0, n3 = 0, n4 = 0;
        
		//well loop till we find an error or we get the #end
		try {
			// version string MUST come before all others if it exists
			// its like version = X.Y;
			// .Y is optional
			st.skipComment();
			if (st.ttype != st.TT_WORD) {
				throw new Exception("Expecting word token");
			}
			token = st.sval; //get the value name
			if (token.equals("version")){
				st.nextToken();
				if (st.ttype != '=') {
					throw new Exception("Expecting =");
				}
				st.nextToken(); //skip the =
				double version = st.nval;
				majorVersion = (int)Math.floor(version);
				// WARNING: this will only work for at most 3 digits of minor version
				// but its a lot easier to do than base convertion from 2 to 10
				
				minorVersion = (int)((version - majorVersion) * 1000);
				if (minorVersion != 0){
					// remove zeros to the right
					while ((minorVersion % 10) == 0){
						minorVersion /= 10;
					}				
				}
				st.nextToken();
				if (st.ttype != ';'){
					throw new Exception("Expecting ;");
				}
			}
			else {
				// no version string
				st.pushBack();
			}
			
            do {
                // st.nextToken();
                st.skipComment();
                if (st.ttype != st.TT_WORD) {
                    throw new Exception("Expecting word token");
                }
                token = st.sval; //get the value name
                if (token.equals("#end")) {
                    break; //end the loop
                }
                
                st.nextToken(); //gets the =, optional sometimes, so we wont raise
                
                if (st.ttype == '=') {
                    st.nextToken(); //skip the =
                }
                numToken = (int) st.nval;
                
                if (token.equals("dogeometry")) {
                    doGeometry = true;
                    st.pushBack(); //puts the ; token back
                }
                else if (token.equals("altpanel")) {
                    altpanel = numToken;
                }
                else if (token.equals("numsurfaces")) {
                    numsurfaces = numToken;
                }
                else if (token.equals("numpanels")) {
                    numpanels = numToken;
                }
                else if (token.equals("numcursors")) {
                    numcursors = numToken;
                }
                else if (token.equals("numobjects")) {
                    numobjects = numToken;
                }
                else if (token.equals("numbuttons")) {
                    numbuttons = numToken;
                }
                else if (token.equals("numbuttonviews")) {
                    numbuttonviews = numToken;
                }
                else if (token.equals("numsounds")) {
                    numsounds = numToken;
                }
                else if (token.equals("mouseborder")) {
                    mouseBorder = numToken;
                }
                else if (token.equals("hudfont")) {
                    hudfont = numToken;
                }
                else if (token.equals("dedfont")) {
                    dedfont = numToken;
                }
                else if (token.equals("mfdfont")) {
                    mfdfont = numToken;
                }
                else if (token.equals("popupfont")) {
                    popupfont = numToken;
                }
                else if (token.equals("saboxfont")) {
                    saboxfont = numToken;
                }
                else if (token.equals("kneefont")) {
                    kneefont = numToken;
                }
                else if (token.equals("generalfont")) {
                    generalfont = numToken;
                }
                else if (token.equals("labelfont")) {
                    labelfont = numToken;
                }
                else if (token.equals("hud")) {
                    hud = new PitArea();
                    hud.parseData(st);
                }
                else if (token.equals("templatefile")) {
                    Cockpit.template = new PitTemplate();
                    Cockpit.template.parseData(st);
                }
                else if (token.equals("cockpit2d")) {
                    cockpit2d = new int[2];
                    cockpit2d[0] = (int) st.nval;
                    st.nextToken();
                    cockpit2d[1] = (int) st.nval;
                }
				else if (token.equals("floodlight")){
					// get pitcolor in string format
					floodlight = st.parseColor();
				}
				else if (token.equals("instlight")){
					// get instrument color in string format
					instlight = st.parseColor();
				}
                else {
                    throw new ParseErrorException(
                    "pitManager.java: Invalid token " + token);
                }
                
                st.nextToken();
                if (st.ttype != ';') {
                    throw new ParseErrorException(
                    "pitManager.java: Error: missing ;");
                }
            }
            while (true);
        }
        catch (Exception e) {
            //here we raise the exception
            throw new ParseErrorException(e.toString());
        }
    }
    
    /**
     * Saves the manager to the given file. All values are upadted on demand.
     *
     * @param arq the file being written to. Must be opened and in the 
     * right position
     */
    public void writeComponent(misc.RAFile arq) {
        try {
            //write comments
            if (comments != null){
                comments.write(arq);
            }
            arq.writeln("#" + id + " MANAGER");
			
			if (majorVersion >= 1){
				arq.writeln("\tversion = " + majorVersion + '.' + minorVersion + ';');
			}
            
            arq.writeln("\tnumsurfaces = " + Cockpit.surfs.getNumComponents() + ";");
            arq.writeln("\tnumpanels = " + Cockpit.panels.getNumComponents() + ";");
            arq.writeln("\tnumcursors = " + numcursors + ";");
            arq.writeln("\tnumobjects = " + Cockpit.objs.getNumComponents() + ";");
            arq.writeln("\tnumbuttons = " + Cockpit.buttons.getNumComponents() + ";");
            arq.writeln("\tnumbuttonviews = " + Cockpit.bviews.getNumComponents() + ";");
            arq.writeln("\tnumsounds = " + Cockpit.sounds.getNumComponents() + ";");

            if (mouseBorder != -2) {
                arq.writeln("\tmouseBorder = " + mouseBorder + ";");
            }
            if (hudfont != -2) {
                arq.writeln("\thudfont = " + hudfont + ";");
            }
            if (dedfont != -2) {
                arq.writeln("\tdedfont = " + dedfont + ";");
            }
            if (mfdfont != -2) {
                arq.writeln("\tmfdfont = " + mfdfont + ";");
            }
            if (popupfont != -2) {
                arq.writeln("\tpopupfont = " + popupfont + ";");
            }
            if (saboxfont != -2) {
                arq.writeln("\tsaboxfont = " + saboxfont + ";");
            }
            if (kneefont != -2) {
                arq.writeln("\tkneefont = " + kneefont + ";");
            }
            if (generalfont != -2) {
                arq.writeln("\tgeneralfont = " + generalfont + ";");
            }
            if (labelfont != -2) {
                arq.writeln("\tlabelfont = " + labelfont + ";");
            }
            if (hud != null) {
                arq.writeln("\thud = " + hud + ";");
            }
            if (Cockpit.template != null) {
                Cockpit.template.writeComponents(arq);
            }
			if (floodlight != null){
				arq.writeln("\tfloodlight = " + floodlight + ";");
			}			
			if (instlight != null){
				arq.writeln("\tinstlight = " + instlight + ";");
			}
			if (cockpit2d != null) {
                arq.writeln("\tcockpit2d " + cockpit2d[0] + " " +
                cockpit2d[1] + ";");
            }
            if (doGeometry) {
                arq.writeln("\tdogeometry;");
            }
            if (altpanel != -2){
                arq.writeln("\taltpanel = " + altpanel + ";");
            }
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitManager.java-> erro de IO: " + io);
        }
    }  

	/** gets the floodlight in color format. Its stored internally as a PitColor. */
	public Color getFloodlightColor(){
		return floodlight.toColor();
	}
	
	public void setFloodLightColor(Color c){
		floodlight.setColor(c);
	}

	/** gets the floodlight in color format. Its stored internally as a PitColor. */
	public Color getInstlightColor(){
		return instlight.toColor();
	}	
	
	public void setInstLightColor(Color c){
		instlight.setColor(c);
	}
	
	/** gets mouse border. */
	public int getMouseBorder(){
		return mouseBorder;
	}

	/** sets the mouse border for manager. */
	public void setMouseBorder(int mb){
		mouseBorder = mb;
	}
	
	/** gets major version. */
	public int getMajorVersion(){
		return majorVersion;
	}
	
	/** sets major version for this cockpit file. */
	public void setMajorVersion(int major){
		majorVersion = major;
	}
	
	/** gets minor version. */
	public int getMinorVersion(){
		return minorVersion;
	}
	
	/** sets the minor version for this cockpit file. */
	public void setMinorVersion(int min){
		minorVersion = min;
	}
	
	/** gets do geometry value. */
	public boolean getDoGeometry(){
		return doGeometry;
	}
	
	/** sets do gemotry. */
	public void setDoGeometry(boolean dg){
		doGeometry = dg;
	}
	
	/** gets the 1 view hud coodinate. */
	public PitArea get1ViewHud(){
		return hud;
	}
	
	/** sets hud area in 1 view. */
	public void set1ViewHud(PitArea nh){
		hud = nh;
	}
}

/** cockpit manager properties dialog. Called from main menu. */
class ManagerPropertiesDialog extends PropertiesDialog {
	/** the cockpit manager. */
	private PitManager pm;
	
	/** labeled fields: mouse border, major and minor versions. */
	LabeledNumField 
		mouseBorder = new LabeledNumField(this, "Mouse Border", 3),
		majorVersion = new LabeledNumField(this, "Version", 2),
		minorVersion = new LabeledNumField(this, ". ", 2)
	;
	
	ColorField instLight = new ColorField("Instruments light color");
	ColorField floodLight = new ColorField("Flood light color");
	
	/** 1 view hud field. */
	AreasFieldPanel hud = new AreasFieldPanel(this, "1-view HUD", false);
	
	/** labeled checkbox */
	LabeledCBox doGeometry = new LabeledCBox("do geometry", "draws 3D pit externally");
	
    public ManagerPropertiesDialog(JFrame parent, PitManager pm){
		super(parent, null, "Manager Properties");
		setProperties(pm);
	}
	
	public ManagerPropertiesDialog(JDialog parent, PitManager pm){
		super(parent, null, "ManagerProperties");
		setProperties(pm);
	}
	
	/** sets properties common to all constructors.
	* Manager properties dialog is composed of 3 tabs.
	* <li> First is informational (number of objects) - read only
	* <li> Second is general.
	* <li> Third is font indexes.
	*/
	private void setProperties(PitManager pm){
		this.pm = pm;
		setSize(500, 500);
		JTabbedPane tp = new JTabbedPane(JTabbedPane.LEFT);
		// info panel
		{
			JPanel p = new JPanel();
			tp.add("Info", p);
			tp.setToolTipTextAt(0, "Cockpit information");
		}
		// general panel
		{
			JPanel p = new JPanel(new GridLayout(8, 1));
			{
				JPanel versionPanel = new JPanel();
				versionPanel.add(majorVersion);
				versionPanel.add(minorVersion);				
				p.add(versionPanel);				
			}
			p.add(mouseBorder);
			p.add(hud);
			p.add(instLight);
			p.add(floodLight);
			p.add(doGeometry);
			tp.add("General", p);
			tp.setToolTipTextAt(1, "General data");
		}
		// font panel
		{
			JPanel p = new JPanel();
			tp.add("Fonts", p);
			tp.setToolTipTextAt(2, "Fonts");
		}
		Container c = this.getContentPane();
		c.add(tp);
		
		loadValues();
	}
    
	/** fill the value fields with manager data. */
	protected void loadValues(){
		try {
			// info
			// general
			mouseBorder.setValue(pm.getMouseBorder());
			majorVersion.setValue(pm.getMajorVersion());
			minorVersion.setValue(pm.getMinorVersion());
			if (pm.getInstlightColor() != null){
				instLight.setColor(new PitColor(pm.getInstlightColor()));
			}
			if (pm.getFloodlightColor() != null){
				floodLight.setColor(new PitColor(pm.getFloodlightColor()));
			}
			doGeometry.setSelected(pm.getDoGeometry());
			hud.setFields(pm.get1ViewHud());
			// fonts
		}
		catch (Exception e){
		}
	}
	
	protected void setValues() throws Exception {
		// info
		// general
		pm.setMouseBorder(mouseBorder.getValue());
		pm.setMajorVersion(majorVersion.getValue());
		pm.setMinorVersion(minorVersion.getValue());
		pm.setInstLightColor(instLight.getColor().toColor());
		pm.setFloodLightColor(floodLight.getColor().toColor());
		pm.setDoGeometry(doGeometry.isSelected());
		pm.set1ViewHud(hud.getArea());
	}
}

