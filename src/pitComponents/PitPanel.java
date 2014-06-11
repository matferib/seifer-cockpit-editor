package pitComponents;

import cockpitEditor.UsPrListener;
import pitComponents.pitObjects.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import cockpitEditor.UsPr;
import windows.PropertiesDialog;
import actions.*;
import windows.NewMainWindow;


/* Represents the panel, emcompassing everything graphic related to
 * it. Graphically, its a JPanel (Drawing panel)
 */
public class PitPanel extends JPanel implements MouseListener, MouseMotionListener, UsPrListener {
	//mouse stuff
	private int operation;
	private Point lastp = new Point();
	/** the Point where mouse was pressed. Used for actions */
	private Point pressPoint; 
	private boolean canDrag= false;    ///< indicates we can drag and drop
	/** indicates this is first call to drag. Used to save action targets */
	private boolean dragStart = false;
	
	private boolean isClick= false; //<because the way java is working now
	private boolean isInsideArea = false; //< used to update mouse position
    
    
	/**
	 * If a pit has a background image, it has a buffer.
	 */
	protected PitBuffer buffer;

	/** 
	 * Comments this panel has
	 */
	public PitComment comments;

	/** some constants, adjacent panel values */
	public static int UP=0, UPR=1, RIGHT=2, DOWNR = 3,
				 DOWN=4, DOWNL = 5, LEFT=6, UPL=7;

	/** panel values, -1 means not used */
	public int masktop, cursorid, id, fonts[] = {-1, -1, -1};
	
	/** each type of font in panel, used for font array index */
	protected static final String fontsStr[] = {
	     "dedfont", "mfdfont", "hudfont"
	};
	protected static final int
	     PANEL_DEDFONT = 0, 
	     PANEL_MFDFONT = 1, 
	     PANEL_HUDFONT = 2;

	/** each panel holds objects and buttonviews, they are held in vectors.
	 * Also, we have the surfaces.
	 */
	public Vector objectsVector = new Vector(3), 
				 buttonviewsVector = new Vector(3),
				 Vector = new Vector(3), 
				 minisurfaces = new Vector(3);

	/** more panel properties */
	public PitArea mousebounds;
	public PanelRWR rwr;
	public PanelMFD mfds[] = new PanelMFD[4];
	public PanelHud hud;
	public int adjpanels[] = new int[8], offset[] = new int[2];
	protected float pantilt[] = new float[2]; //changed to private to be read only
	public boolean dogeometry;
	private int numSurfsAdded = 0;

	/** represents the tilt value in pixel values, calculated from the tilt 
	 * each time its achanged
	 **/
	protected int horizonPosition = 0;

	//OF specific options
	public int litpalette = 0;

	//we need this only to check if the file is ok, elsewhere we use
	//the Vector.size() function
	int numbuttonviews = 0, numobjects = 0;

	/** panels need to be loaded before drawing the first time, this variable
	 * tells if the panel is loaded or not
	 */
	public boolean loaded = false;

	/**
	 * Creates the panel, without the id. Useful when the panel is created manually
	 * and not by the parser. The ID must be filled though, for a valid panel.
	 */
	public PitPanel(){
	    dogeometry = false;
	    setOpaque(false);
	    addMouseListener(this);
	    addMouseMotionListener(this);
	    setDoubleBuffered(false);
	    Dimension d = new Dimension((int)(Cockpit.resolution.width), (int)(Cockpit.resolution.height));    
	    setPreferredSize(d);
	    setSize(d);
	    UsPr.addUsPrListener(this);
	}

	/**
	 * Creates the new panel, using an id. Called by the parser. Calls the default
	 * constructor also.
	 */
	public PitPanel(int id) {
		this();
		this.id = id;
	}


	/**
	 * Called by the cockpit object, after all elements have been read. This is
	 * needed because when we read the panel, the buffer does not exist yet(ie
	 * panel buffer == null. So after all buffers have been read, we call this
	 * function. Beware that just the buffer is linked, but images are not loaded,
	 * so as to avoid program hanging for long time during startup.
	 */
	public void linkBuffer(){
		//if we have a surface, we get the buffer from it
		Vector minis = minisurfaces;
		if ((minis!=null) && (minis.size() != 0)){
			//get the surface id
			int sid = ((MiniSurface)minis.get(0)).sid;
			//get buffer from the surface
			PitSurface ps = (PitSurface)Cockpit.surfs.get(sid);
			if (ps != null){
				//this can happen if the file is wrong...
				buffer = ps.buffer;
			}
			else {
				buffer = null;
			}
		}
	}

	/** computes the horizon position, in pixels, given the current tilt value. Used for 
	 * horizon line drawing. 
	 */
	public void computeHorizon(){
	    int hp = (int)(
		(Cockpit.resolution.height/2.0) + 
		Cockpit.resolution.height * Math.tan(-Math.toRadians(pantilt[1]))
		/ (Math.tan(Math.toRadians(UsPr.vFov / 2.0)) * 2.0)
		//masktop adjustment
		- ((Cockpit.resolution.height - masktop) / 2)
	    );

	    //we dont draw below screen resolution
	    int limit = (masktop > Cockpit.resolution.height) ? (Cockpit.resolution.height):(masktop);
	    horizonPosition = (hp > limit) ? (Cockpit.resolution.height):(hp);
	}

	/** calculates the tilt automatically, based on the guncross position, if
	 * it exists. The formula is the oposite of the one used for drawing.
	 */
	public void autoTilt(){
	    //no hud, no deal
	    if (this.hud == null){
		    return;
	    }

	    //cockpit y res
	    double sy = Cockpit.resolution.height;
	    //guncross y
	    double gc = this.hud.getGC().y;
	    //auxiliary var
	    double z = -(2.0 * Math.tan(Math.toRadians(UsPr.vFov/2.0))) / sy;
	    //masktop diff
	    double m = sy - masktop;

	    this.pantilt[1] = (float)Math.toDegrees(Math.atan(z*(gc + (m - sy)/2.0)));
	    //updates the tilt value
	    computeHorizon();
	}

	/** loads the panel data, like images, buffers, surfaces and ids, and computes tilt. 
		* Calls buffer load 
	  */
	public void load(){
		if (buffer == null){
			loaded = true;
			//repaint();
			return;
		}
		buffer.load();
		int size = (buffer == null) ? 0 : minisurfaces.size();
		for (int i=0;i<size;i++){
			((MiniSurface)minisurfaces.get(i)).load();
		}
		loaded = true;
		repaint();
	}

	/** unloads a panel. Means it will be loaded before next draw */
	public void unload(){
		loaded = false;
	}

	public void parseData(PitTokenizer st) throws ParseErrorException {
		int numSrfRead = 0;

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
				else if (token.equals("dogeometry")) {
					dogeometry = true;
					st.nextToken();
					if (st.ttype != ';') {
						throw new Exception("dogeometry expecting ;");
					}
					continue; //end this iteration
				}

				st.nextToken();
				if (st.ttype == '=') { //skip =
					st.nextToken();
				}

				//for those empty lines like buttonviews = ;
				if (st.ttype == ';'){
					continue;
				}

				if ( (!token.equals("orientation")) &&
						(!token.endsWith("loc")) && (st.ttype != st.TT_NUMBER)) {
					throw new ParseErrorException("expecting number token");
						}

				if (token.equals("adjpanels")) {
					for (int i = 0; i < 8; i++) {
						if (st.ttype != st.TT_NUMBER) {
							throw new Exception("adjpanels expecting number");
						}
						adjpanels[i] = (int) st.nval;
						st.nextToken();
					}
					st.pushBack();
				}
				else if (token.equals("buttonviews")) {
					int i=0;
					while (true) {
						if (st.ttype == ';')
							break;
						else if (st.ttype != st.TT_NUMBER) {
							throw new Exception("buttonview expecting number");
						}
						int j =(int) st.nval;
						Object o = Cockpit.bviews.get(j);
						if (o == null){
							System.out.println("Panel: " + id + ", null buttonview "
									+ j + "\n");
						}
						else
							buttonviewsVector.add(o);
						st.nextToken();
						i=0;
					}
					st.pushBack();
				}
				else if (token.equals("cursorid")) {
					cursorid = (int) st.nval;
				}
				else if (token.equals("hud")) {
					hud = new PanelHud();
					hud.parseData(st);
				}
				else if (token.equals("hudfont")) {
					fonts[PANEL_HUDFONT] = (int) st.nval;
				}
				else if (token.equals("dedfont")) {
					fonts[PANEL_DEDFONT] = (int) st.nval;
				}                                
				else if (token.equals("mfdfont")) {
					fonts[PANEL_MFDFONT] = (int) st.nval;
				}
				else if (token.equals("masktop")) {
					masktop = (int) st.nval;
					computeHorizon();
				}
				else if (token.startsWith("mfd")) {
					//CAUTION: this has to be place after mfdfont
					int w;
					if (token.equals("mfdleft"))
						w = PanelMFD.MFDL;
					else if (token.equals("mfdright"))
						w = PanelMFD.MFDR;
					else if (token.endsWith("mfd3"))
						w = PanelMFD.MFD3;
					else if (token.endsWith("mfd4"))
						w = PanelMFD.MFD4;
					else
						throw new Exception("invalid mfd");
					mfds[w] = new PanelMFD(w);
					mfds[w].parseData(st);
				}
				else if (token.equals("mousebounds")) {
					mousebounds = new PitArea();
					mousebounds.parseData(st);
				}
				else if (token.equals("numbuttonviews")) {
					numbuttonviews = (int) st.nval;
					//buttonviewsVector = new Vector(numbuttonviews);
				}
				else if (token.equals("numobjects")) {
					numobjects = (int) st.nval;
					//objectsVector = new Vector(numobjects);
				}
				else if (token.equals("numsurfaces")) {
					int numsurfaces = (int) st.nval;
					//minisurfaces = new Vector(numsurfaces);
				}
				else if (token.equals("objects")) {
					int i=0;
					while (true) {
						if (st.ttype == ';')
							break;
						else if (st.ttype != st.TT_NUMBER) {
							throw new Exception("button expecting number");
						}
						int j = (int) st.nval;
						Object o = Cockpit.objs.get(j);
						if (o == null){
							System.out.println("Panel: " + id + ", null object: "
									+ j + "\n");
						}
						else{
							objectsVector.add(o);
						}
						st.nextToken();
					}
					st.pushBack();
				}
				else if (token.startsWith("osb")) {
					int w;
					if (token.equals("osbleft"))
						w = PanelMFD.MFDL;
					else if (token.equals("osbright"))
						w = PanelMFD.MFDR;
					else if (token.endsWith("osb3"))
						w = PanelMFD.MFD3;
					else if (token.endsWith("osb4"))
						w = PanelMFD.MFD4;
					else
						throw new Exception("invalid osb");
					mfds[w].parseOSB(st);
				}
				else if (token.equals("offset")) {
					offset[0] = (int) st.nval;
					st.nextToken();
					if (st.ttype != st.TT_NUMBER) {
						throw new Exception("offset expecting number");
					}
					offset[1] = (int) st.nval;
				}
				else if (token.equals("pantilt")) {
					pantilt[0] = (float) st.nval;
					st.nextToken();
					if (st.ttype != st.TT_NUMBER) {
						throw new Exception("osb expecting number");
					}
					pantilt[1] = (float) st.nval;
					computeHorizon();
				}
				else if (token.equals("rwr")) {
					rwr = new PanelRWR();
					rwr.parseData(st);
				}
				else if (token.equals("surfaces")) {
					MiniSurface ms = new MiniSurface();
					ms.parseData(st);
					minisurfaces.add(ms);
				}
				//OF options
				//if we read any of them, we set dat to open falcon
				else if (token.equals("litpalette")){
					UsPr.datFormat = UsPr.DAT_OF;
					litpalette = (int)st.nval;
				}

				//get the ;
				st.nextToken();
				if (st.ttype != ';') {
					throw new Exception("expecting ;");
				}

			}
			while (true);           
		}
		catch (Exception e) {
			throw new ParseErrorException("pitPanel.java: " + e);
		}
	}

	/**
	 * Writes the buffer embeded in this panel. Each panel may or may not have
	 * a buffer.
	 *
	 * @param arq the file we write data to. Must be opened and in the right
	 * position
	 *
	 * @throws Exception if an IO error happens
	 */
	public void writeBuffer(misc.RAFile arq) throws Exception{
		if (buffer != null){
			buffer.writeComponent(arq);    
		}        
	}

	/**
	 * Writes all the surfaces in this panel. The surfaces are drawn only if
	 * the buffer is not null.
	 *
	 * @param arq the file we write data to. Must be opened and in the right
	 * position
	 *
	 * @throws Exception if an IO error happens
	 */
	public void writeSurfaces(misc.RAFile arq) throws Exception{
		if (buffer == null)
			return;

		int numSurfs = minisurfaces.size();
		int[] sw = new int[numSurfs]; //surfaces written
		for (int j=0;j<numSurfs;j++){
			sw[j] = -1; //no surface used yet
		}

		//we need the filename of the surfaces
		String filename = buffer.getFilename();

		//for each minisurface, we write the correponding surface if we did not 
		//write it yet
		for (int i=0; i<numSurfs;i++){
			//the surface id
			int sid = ((MiniSurface)minisurfaces.get(i)).sid;

			//is the surface already written?
			boolean written = false;
			for (int j=0;(j<numSurfs);j++){
				if (sw[j] == -1){
					sw[j] = sid;
					break;
				}
				else if (sid == sw[j]){
					written = true;
					break;
				}
			}

			if (!written){
				PitSurface ps = ((PitSurface)Cockpit.surfs.get(sid));
				//we write the surface using the buffer filename
				ps.writeComponent(arq, filename);
			}
		}

	}

	public void writeComponent(misc.RAFile arq) throws Exception {
		int i;

		try {
			if (comments != null){
				comments.write(arq);
			}
			this.correct();
			arq.writeln("#" + id + " PANEL");
			arq.writeln("\tnumsurfaces = " + minisurfaces.size() + ";");
			arq.writeln("\tnumobjects = " + objectsVector.size() + ";");
			if (objectsVector.size() != 0) {
				for (i = 0; i < objectsVector.size(); i++) {
					if (((i % 15) == 0)){
						if (i != 0){
							arq.writeln(";");
						}
						arq.writeBytes("\tobjects = ");
					}
					PitObject po = ((PitObject)(objectsVector.get(i)));
					if (po == null){
						System.out.println("panel: " + this.id +
								", null object, position: " + i + "\n");
					}
					else
						arq.writeBytes(" " + po.id);
				}
				arq.writeln(";");
			}

			arq.writeln("\tnumbuttonviews = " + buttonviewsVector.size() + ";");
			if (buttonviewsVector.size() != 0) {
				for (i = 0; i < buttonviewsVector.size(); i++) {
					if (((i % 15) == 0)){
						if (i != 0){
							arq.writeln(";");
						}
						arq.writeBytes("\tbuttonviews = ");
					}

					PitButtonView bv = (PitButtonView)buttonviewsVector.get(i);
					if (bv == null){
						System.out.println("panel: " + this.id +
								", null buttonview, position " + i + "\n");
					}
					else
						arq.writeBytes(" " + bv.id);


				}
				arq.writeln(";");
			}

			if (dogeometry) {
				arq.writeln("\tdogeometry;");
			}
			for (i=0; i<4;i++){
				if (mfds[i] != null) {
					mfds[i].writeComponent(arq);
				}
			}
			if (rwr != null) {
				rwr.writeComponent(arq);
			}
			if (hud != null) {
				hud.writeComponent(arq);
			}
			if (mousebounds != null) {
				arq.writeln("\tmousebounds = " + mousebounds +
						";");
			}
			arq.writeBytes("\tadjpanels =");
			for (i = 0; i < 8; i++) {
				arq.writeBytes(" " + adjpanels[i]);
			}
			arq.writeln(";");
			//surfaces
			if (buffer != null){
				for (i = 0; i < minisurfaces.size(); i++) {
					arq.writeln("\tsurfaces = " + minisurfaces.get(i) + ";");
				}
			}
			arq.writeln("\toffset = " + offset[0] + " " + offset[1] + ";");
			arq.writeln("\tpantilt = " + pantilt[0] + " " + pantilt[1] + ";");
			if (fonts[PANEL_HUDFONT] >= 0) {
				arq.writeln("\t" + fontsStr[PANEL_HUDFONT] + " = " + fonts[PANEL_HUDFONT] + ";");
			}
			if (fonts[PANEL_MFDFONT] >= 0) {
				arq.writeln("\t" + fontsStr[PANEL_MFDFONT] + " = " + fonts[PANEL_MFDFONT] + ";");
			}
			if (fonts[PANEL_DEDFONT] >= 0) {
				arq.writeln("\t" + fontsStr[PANEL_DEDFONT] + " = " + fonts[PANEL_DEDFONT] + ";");
			}                        
			arq.writeln("\tmasktop = " + masktop + ";");                       
			arq.writeln("\tcursorid = " + cursorid + ";");

			//OF specific options
			if (UsPr.datFormat == UsPr.DAT_OF){
				arq.writeln("\tlitpalette = " + litpalette + ";");
			}

			arq.writeln("#END");
		}
		catch (IOException io) {
			System.out.println("pitPanel.java-> erro de IO: " + io);
		}
	}

	/* end control vars ****************/

	/* end setget *******/


	/** sends object to the pit background.
	 * this is useful for flood lighting, which must be at the background. Falcon
	 * draws objects in the order they appear in the object list of the panel.
	 * this makes sense only for buttons and objects(no RWR, MFSs nor huds)
	 *
	 */
	public void sendToBg(PitObject o){
	    if (o instanceof PitButtonView){
		buttonviewsVector.remove(o);
		buttonviewsVector.add(0, o);
	    } 
	    else if (o instanceof PitObject){
		objectsVector.remove(o);
		objectsVector.add(0, o);
	    }
	    repaint();
	}
	
	
	/** sends object to the pit foreground
	 * this is the reverse of the @link{sendSelectedToBg}
	 */
	public void sendToFg(PitObject o){
	    if (o instanceof PitButtonView){
		buttonviewsVector.remove(o);
		buttonviewsVector.add(o);
	    } 
	    else if (o instanceof PitObject){
		objectsVector.remove(o);
		objectsVector.add(o);
	    }
	    repaint();
	}


	/** 
	 * Centers view on the selected object, or the first one of many selected
	 */
	public void centerOn(PitObject po){
	    double zoom = UsPr.getZoom();
	    // no selected objects, return

	    //get first selected object
	    //object has no destloc
	    if ((po == null) || (po.destloc == null)){
		return;
	    }
	    //use destloc 0 if for some weird reason none is selected        
	    int dest = (po.currentSelectedDest == -1)?0:po.currentSelectedDest;
	    if (po.destloc[dest] == null){
		return;
	    }

	    //get area center and apply zoom factor
	    Point point = po.destloc[dest].getCenter();
	    point.x *= zoom;
	    point.y *= zoom;

	    //sets the view position according to the center point
	    JViewport v = (JViewport)this.getParent();        
	    point.x -= (v.getWidth() / 2);
	    point.y -= (v.getHeight() / 2);

	    if (point.x < 0)
		    point.x = 0;
	    if (point.y < 0)
		    point.y = 0;

	    v.setViewPosition(point);
	}	

	/** uspr listener for zoom */
	public void zoomChanged(double zoom){
	    changeZoom(zoom, null);
	}
	
	/** loads the zoom from the user preferences and repaint panel
	 * @param zoom new zoom factor
	 * @param po object to center on
	 */
	public void changeZoom(double zoom, PitObject po){
	    Dimension d = new Dimension(
		(int)(Cockpit.resolution.width * zoom),
		(int)(Cockpit.resolution.height * zoom)
	    );
	    this.setPreferredSize(d);
	    this.setSize(d);

	    if (po != null){
		centerOn(po);
	    }
	    repaint();
	}

	/**
	 * Deletes the object from the panel. All references to it are removed 
	 * from panel. Cockpit object is responsible for deleting his own
	 * @param @object being deleted
	 */ 
	public void deleteObject(PitObject o){
	    if (o instanceof PanelMFD){
		    PanelMFD mfd = (PanelMFD)o;
		    mfds[mfd.id] = null;
	    } 
	    else if (o instanceof PanelHud){
		    hud = null;
	    } 
	    else if (o instanceof PanelRWR){
		    rwr = null;
	    } 
	    else if (o instanceof PitButtonView){
		    buttonviewsVector.remove(o);
	    } 
	    else if (o instanceof PitObject){
		    objectsVector.remove(o);
	    }		
	}

	/**
	 * Deletes all references from this pit. Should be called before deleting
	 * a panel.
	 */
	public void deleteAll(){
	    objectsVector.clear();
	    buttonviewsVector.clear();
	    minisurfaces.clear();
	    //should delete surfaces also, but who cares
	}

	/* end current ***********************************************/

	//panel walk
	public int getAdj(int which){
	    return adjpanels[which];
	}


	/* Add new object functions ****************************/
	public void addMFD(Rectangle r, int which, boolean withOsbs) {
	    //we create the panel only if it doesnt exist
	    if (mfds[which] == null){
		try {
		    mfds[which] = new PanelMFD(which, r, withOsbs);		    
		}
		catch (Exception e){
		}
	    }
	}

	public void addHud(Rectangle r){
		//we create only if it doesnt exist
		if (hud == null){
			hud = new PanelHud(r);
		}
	}

	public void addRWR(Rectangle r){
	    if (rwr == null){
		rwr = new PanelRWR(r);
	    }
	}

	public void addButtonView(PitButtonView bv){
	    buttonviewsVector.add(bv);
	}

	public void addObject(PitObject obj){
	    objectsVector.add(obj);        
	}
	/* *****************************************************/

	/** Gets the list of surfaces Id, used for checking duplicating surfaces
	 * 
	 * @return a vector of surface id
	 */
	public int[] getSurfacesId(){
	    int ms = minisurfaces.size();
	    int ret[] = new int[ms];
	    for (int i=0;i<ms;i++){
		ret[i] = ((MiniSurface)minisurfaces.get(i)).sid;
	    }
	    return ret;
	}

	/** some cockpits have duplicate surfaces. This function is called by a clean
	 * save, when it finds duplicate surfaces, so this panel creates its own
	 * surface instead of using a dup one
	 *
	 * @param sid the duplicated surface id
	 * @throws if the given surface id cant be found
	 */
	public void createNewSurfaceFrom(int sid) throws Exception {
	    //first, we find the minisurface holding the surface
	    MiniSurface ms = null;
	    int mss = minisurfaces.size();
	    for (int i=0;i<mss;i++){
		ms = (MiniSurface)minisurfaces.get(i);
		if (ms.sid == sid){
		    break;
		}
	    }

	    //surface id not found in any of the minis
	    if (ms == null){
		throw new Exception("Surface " + sid + " not found in panel " + id);
	    }

	    //here we clone the given surface
	    //first find a free Id given panel id        
	    int newSurfId = Cockpit.surfs.getFreeId(id + 1);
	    //get the original surface
	    PitSurface ps = (PitSurface)Cockpit.surfs.get(sid);
	    //create the new surface
	    PitSurface nps = new PitSurface(newSurfId);
	    //copy attributes

	    //make the mini point to the new clone surface
	    ms.sid = newSurfId;

	}

	/** Checks to see if all surfaces exists. Also sees if all panel references
	 * are valid ones, else, correct them to -1.
	 * 
	 * In the future, must check if all
	 * objects are in place.
	 *
	 * @return a string with all errors found
	 */
	public String check(){
		String s = "";
		//minisurfaces check
		int ns = minisurfaces.size();
		for (int i=0;i<ns;i++){
			int sid = (((MiniSurface)minisurfaces.elementAt(i)).sid);
			s = s.concat(((PitSurface)Cockpit.surfs.get(sid)).check());
		}

		//reference check
		for (int i=0;i<8;i++){
			if ( (adjpanels[i] != -1) && 
					(Cockpit.panels.get(adjpanels[i]) == null) ) {
				adjpanels[i] = -1;
				s = s.concat("Warning: invalid reference in panel " + id + 
						"to panel " + adjpanels[i] + "corrected to -1\n");
					}
		}

		if ((masktop <= 0) || (masktop > Cockpit.resolution.height) ){
			//invalid masktop
			s = s.concat("WARNING: panel " + id + ", invalid masktop " + masktop);
		}


		return s;
	}

	/**
	 * Corrects the panel. Similar to check, but corrects wrong entries.
	 * Every panel should call this prior to saving.
	 */
	private void correct(){
		//minisurfaces check

		//check for null surfaces
		for (int i=0;i<minisurfaces.size();i++){
			int sid = (((MiniSurface)minisurfaces.elementAt(i)).sid);
			PitSurface ps = (PitSurface)Cockpit.surfs.get(sid);
			if (ps == null){
				minisurfaces.remove(i);
				i--;
			}
		}

		int ns = minisurfaces.size();
		if (ns == 0){
			if (this.buffer != null){
				this.buffer.unload();
			}
			this.buffer = null;
		}

		//reference check
		for (int i=0;i<8;i++){
			if ( (adjpanels[i] != -1) && 
					(Cockpit.panels.get(adjpanels[i]) == null) ) {
				adjpanels[i] = -1;
					}
		}        
	}

	/** Shows this panel properties, just like an object. Called by the panel 
	* window when shift P is pressed. Panel properties range from id to adjacent
	* panels, objects etc.
	*
	* @param parent the frame calling
	*/
	public void showProperties(JFrame parent){
		PanelPropertiesDialog ppd = new PanelPropertiesDialog(parent, this, false);
		ppd.setVisible(true);
	}

	/**
	* Shows the object list of this panel, so user can select from a list.
	* Useful when objects are on top of each other.
	*/
	public void showObjectsList(JFrame f){
		PanelObjectList pol = new PanelObjectList(this, f);
		pol.showObjects();
	}

	/** Shows new panel dialog, just like an object.
	*
	* @param parent the frame calling
	*/     
	public boolean showNew(JFrame parent){
		PanelPropertiesDialog ppd = new PanelPropertiesDialog(parent, this, true);
		ppd.setVisible(true);
		boolean ret = ppd.getFieldsOk();
		ppd.dispose();
		return ret;
	}
	
	/** draws the panel. This is where the drawing happens, its called
	* by the java panel whenever a drawing is required from system
	*/
	protected void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		//no graphics context, no draw
		if (g2 == null) {
			return;            
		}

		//zoom factor
		double zoom = UsPr.getZoom();
		g2.scale(zoom, zoom);

		//we draw sky if user selects daylight
		if ((UsPr.drawHorizon) && (UsPr.isDayTime())) {
			//attemp to draw horizon line
			//Im assuming a 80 degree vertical FoV            
			int y;
			if (this.pantilt[1] == 90){
				y = 0;
			}
			else if (this.pantilt[1] == -90){
				y = Cockpit.resolution.height;
			}
			else {
				y = this.horizonPosition;
			}

			//sky, we dont draw below masktop, though the horizon position takes
			//that into account
			g2.setColor(UsPr.skyColor);
			g2.fillRect(0, 0, Cockpit.resolution.width, y);
			//ground, we dont draw below masktop
			int limit = ((Cockpit.resolution.height) > masktop) ?
				(masktop - y):(Cockpit.resolution.height - y);
			g2.setColor(UsPr.terrainColor);
			g2.fillRect(0, y, Cockpit.resolution.width, limit);
		}
		else {
			//draw gray bg
			g2.setColor(Color.GRAY);
			g2.fillRect(0, 0, Cockpit.resolution.width, Cockpit.resolution.height);
		}

		//lets set the colors, according to the user preferences
		Color lightColor, otherColor, panelObjColor, bvColor, curObColor, maskColor;
		if (UsPr.drawAlpha){
			panelObjColor = UsPr.panelObjColorAlpha;
			otherColor = UsPr.otherColorAlpha;
			bvColor = UsPr.bvColorAlpha;
			lightColor = UsPr.lightColorAlpha;
			curObColor = UsPr.curObColorAlpha;
			maskColor = UsPr.masktopColorAlpha;
		}
		else {
			panelObjColor = UsPr.panelObjColor;
			otherColor =  UsPr.otherColor;
			bvColor =  UsPr.bvColor;
			lightColor =  UsPr.lightColor;
			curObColor =  UsPr.curObColor;
			maskColor = UsPr.masktopColor;
		}


		g2.setStroke(UsPr.regularStroke);

		int size; //used for array sizes

		//load if we are not loaded
		if (!loaded){
		    load();
		}

		//draw the background image
		//if buffer != null, we must have valid surfaces
		if (buffer != null){
			//load if not loaded
			if (!buffer.loaded){
				buffer.load();
			}
			
			//we draw all mini surfaces
			size=(minisurfaces == null)?0:minisurfaces.size();
			for (int i=0; i<size;i++){
				MiniSurface ms = (MiniSurface)minisurfaces.get(i);
				if (!ms.loaded){
					ms.load();
				}

				Rectangle r = ms.area.rectangle;
				Rectangle r2 = ms.ps.srcloc.rectangle;

				// get image from buffer, according to its type
				Image image = buffer.getImage(
				    (UsPr.drawBuffersTrans && ms.area.transparent) ? 
				    PitBuffer.IM_TRANSPARENT :
				    PitBuffer.IM_OPAQUE
				);
				if (image != null){
				    g.drawImage(image,
					r.x, r.y, r.x + r.width, r.y + r.height,
					r2.x, r2.y, r2.x + r2.width, r2.y + r2.height,
					this
				    );
				}
			}
		}

		//draw the masktop line
		g2.setColor(maskColor);
		g2.drawLine(0, masktop, Cockpit.resolution.width, masktop);

		//we draw rwr, MFD and hud green cause theyre
		//not inside the objects array
		g2.setColor(panelObjColor);
		if (rwr != null){
		    rwr.draw(g2);
		}

		if (hud != null) {
		    hud.draw(g2);
		}

		//draw MFDs
		for (int i=0;i<4;i++){
		    if (mfds[i] != null){
			mfds[i].draw(g2);
		    }
		}

		Vector objects = objectsVector;

		if (objects == null)
			size = 0;
		else
			size = objects.size();
		if (UsPr.drawLabel){
			//draw objects
			for (int i = 0; i < size; i++) {
				PitObject po = (PitObject)objects.get(i);
				if (po instanceof PitLight) {
					g2.setColor(lightColor);
					po.drawLabeled(g2);
				}
				else {
					g2.setColor(otherColor);
					po.drawLabeled(g2);
				}
			}

		}
		else {
			//draw objects
			for (int i = 0; i < size; i++) {
				PitObject po = (PitObject)objects.get(i);
				if (po instanceof PitLight) {
					g2.setColor(lightColor);
					po.draw(g2);
				}
				else {
					g2.setColor(otherColor);
					po.draw(g2);
				}
			}
		}

		Vector bviews = buttonviewsVector;
		//bv are cyan
		g2.setColor(bvColor);
		if (bviews == null)
			size = 0;
		else
			size = bviews.size();

		if (UsPr.drawLabel){
			//draw buttonviews labeled
			for (int i = 0; i < size; i++) {
				( (PitButtonView)bviews.get(i)).
					drawLabeled(g2);
			}
		}
		else {
			//draw buttonviews
			for (int i = 0; i < size; i++) {
				( (PitButtonView) bviews.get(i)).
					draw(g2);
			}
		}

		//draw all selected objects rectangle
		size = Cockpit.getSelectedObjectsSize();
		g2.setColor(curObColor);
		for (int i=0;i<size;i++){
			PitObject po = Cockpit.getSelectedObjectAt(i);
			//draw object again, so it overwrites other objects
			g2.setStroke(UsPr.regularStroke);
			po.draw(g2);
			//draw its current dest outline
			g2.setStroke(UsPr.selectedStroke);
			po.drawCurrentDestOutline(g2);
		}
	}
	
	/* mouse events *********************************/
	public void mouseClicked(MouseEvent e){
	    NewMainWindow.setActive(NewMainWindow.ACTIVE_PANEL);
	    //System.out.println("click");
	    Point p = e.getPoint();
	    double zoom = UsPr.getZoom();

	    //hack, sun screwed mouse clicks in 1.5
	    //a click happens only when mouse press and mouse release 
	    // happens at the same spot
	    if (!isClick){
		return;
	    }

	    //if ctrl is held, we add the object to selected objects vector
	    boolean ctrl = ((e.getModifiersEx() & e.CTRL_DOWN_MASK) != 0);

	    Point clickPoint =  new Point((int)(p.x / zoom),(int)(p.y / zoom));
	    PitObject po = getXY(clickPoint); //gets the object at point

	    if (po == null) {
		//clicked an empty space
		if (!ctrl){
		    //we are not holding ctrl, unselect all
		    Cockpit.removeAllSelectedObjects();
		}
		//if ctrl do nothing
	    }
		else {
			//we clicked an object                       
			if (ctrl){
				//and we are holding ctrl
				//gets object index		    
				if (Cockpit.isObjectSelected(po)){
					//object was selected, unselect it
					Cockpit.removeFromSelectedObjects(po);
				} 
				else {
					//object was not selected, select it
					Cockpit.addSelectedObject(po);
				}
			}
			else {
				//we are not holding ctrl
				//we change state if the object was selected before and we clicked
				//it again
				if (Cockpit.isObjectSelected(po)){
				po.nextState();
				repaint();
				}
				else {
				Cockpit.setSelectedObject(po);
				}
			}
			//we only center the template, because we are changing the panel,
			//so we dont user to click the object and sunddenly it appears elsewhere
			Cockpit.centerOnSelectedObject(false, true);
		}
	}

	public void mouseEntered(MouseEvent e){
		isInsideArea = true;
	}

	public void mouseExited(MouseEvent e){
		isInsideArea = false;
	}

	public void mousePressed(MouseEvent e){
	    // any drags starts here. So its safe to set it here
	    dragStart = true;
	    double zoom = UsPr.getZoom();
	    isClick = true;
	    Point p = e.getPoint();
	    pressPoint = p;
	    //set last point coordinates
	    lastp.x =(int)(p.x / zoom);
	    lastp.y = (int)(p.y / zoom);

	    int size = Cockpit.getSelectedObjectsSize();
	    if (size == 0){
		return;
	    }

	    PitObject po = getXY(lastp); //gets the object

	    if (!Cockpit.isObjectSelected(po)){
		//if we pressed button on an unselected object, dont drag!
		canDrag = false;
		return;
	    }

	    //here we pressed on a valid object, so we save the points
	    canDrag = true;

	    int modifiers = e.getModifiersEx();
	    if ((modifiers & e.BUTTON3_DOWN_MASK) != 0){
		//right button
		operation = -1;
	    }
	    else {
		//normal button
		if (((modifiers & e.SHIFT_DOWN_MASK) != 0) || (size > 1)){
			//if shift is held, we always move. Also, if there are multiple
			//objects selected, we move all of them
			operation = 0;
		}
		//else if ctrl is held, we resize
		else if ((modifiers & e.CTRL_DOWN_MASK) != 0){
			operation = po.getResizeCorner(lastp);
		}
		//else, it depends on where we click
		else {
			operation = po.getOperation(lastp);
		}            
	    }
	}

	public void mouseReleased(MouseEvent e){
	    Point releasePoint = e.getPoint();
	    int size= Cockpit.getSelectedObjectsSize();
	    if ((!canDrag) || (size <= 0)){
		    return;
	    }
	    //Cockpit.endAction();

	    //calculate the offset
	    /*
	    Point offset = new Point(
		(int)((releasePoint.x - pressPoint.x) / UsPr.getZoom()), 
		(int)((releasePoint.y - pressPoint.y) / UsPr.getZoom())
	    );

	    //the targets of the action

	    ActionTarget[] targets = new ActionTarget[size];
	    PitObject o;
	    //move all performed
	    if (operation == -1){                        
		//move all destlocs from selected            
		for (int i=0;i<size;i++){
		    o = (PitObject)parent.selectedObjects.get(i);
		    ActionTarget t = new ActionTarget(o, o.destloc);
		    targets[i] = t;
		}
		MoveAction ma = new MoveAction(targets, offset);
		Cockpit.addAction(ma);
	    }
	    else if (operation == 0) {
		    //move only selected area
		    for (int i=0;i<size;i++){
			    o = (PitObject)parent.selectedObjects.get(i);
			    PitArea a[] = new PitArea[1];
			    a[0] = o.destloc[o.currentSelectedDest];
			    ActionTarget t = new ActionTarget(o, a);
			    targets[i] = t;
		    }
		    MoveAction ma = new MoveAction(targets, offset);
		    Cockpit.addAction(ma);
	    }
	    else {

	    }*/

	}

	public void mouseDragged(MouseEvent e){
	    double zoom = UsPr.getZoom();
	    isClick = false;

	    int size= Cockpit.getSelectedObjectsSize();
	    if ((!canDrag) || (size <= 0)){
		    return;
	    }

	    //if starting drag, need to save action targets
	    if (dragStart){
			dragStart = false;
			ActionTargets at = new ActionTargets();
			for (int i=0;i<size;++i){
				at.addObject(Cockpit.getSelectedObjectAt(i));
			}
			//Cockpit.startAction(at);
	    }

	    Point newp = e.getPoint();
	    newp.x /= zoom;
	    newp.y /= zoom;
	    PitObject o;

	    Point offset = new Point(newp.x - lastp.x, newp.y - lastp.y);
	    if ((operation == -1) || (operation == 0)){
			Cockpit.moveSelectedObjects(offset, true, (operation == -1) ? true : false);
	    }
	    else {
			Cockpit.resizeSelectedObject(offset, operation,  true);
	    }
	    lastp = newp;
	}

	public void mouseMoved(MouseEvent e){
	    double zoom = UsPr.getZoom();
	    PitObject po;
	    if (isInsideArea){
		//adjust point
		Point  p = e.getPoint();
		p.x /= zoom;
		p.y /= zoom;

		//show mouse coords
		//parent.bb.setMousePosition(p);

		//if we are near a border, change cursor
		int size= Cockpit.getSelectedObjectsSize();
			if (size == 1){
				po = Cockpit.getSelectedObjectAt(0);
				setCursor(po.getCursor(p));
			}
			else {
				setCursor(Cursor.getDefaultCursor());
			}
	    }
	}
	/***************end mouse */
	
	/** returns the first object found at the mouse click
	 * we run the list in reverse order, to take the objects draw on top
	 *
	 * @return the object at XY location, which is on top
	 */
	public PitObject getXY(Point p) {
		PitObject po = null, pres = null;

		//highest priority: currentobject
		int size = Cockpit.getSelectedObjectsSize();
		for (int i=size-1;i>=0;i--){
			po = Cockpit.getSelectedObjectAt(i);
			if (po.pointIsInside(p)){
				return po;
			}
		}

		// high priority: buttonviews
		size = buttonviewsVector.size();
		for (int i = size-1; i >= 0; i--) {
			po = (PitObject) buttonviewsVector.get(i);
			if (po.pointIsInside(p)) {
				return po;
			}
		}

		//second: objects
		size = objectsVector.size();
		for (int i = size-1; i >= 0; i--) {
			po = (PitObject)objectsVector.get(i);
			if (po.pointIsInside(p)) {
				/*                if (po instanceof PitLight) {
													pres = po; //lights are the last we want to return
				}
				else {*/
				return po;
				//                }
			}
		}

		//third panel objects(rwr, mfds and huds)
		for (int i=3;i>=0;i--){
			if (mfds[i] != null){
				if (mfds[i].pointIsInside(p))
					return mfds[i];
			}
		}
		if ((hud!=null) && (hud.pointIsInside(p))) {
			return hud;
		}
		if ((rwr!=null)&&(rwr.pointIsInside(p))){
			return rwr;
		}
		return null;
		//fourth: lights, because they can occupy the whole screen
		//      return pres;
	}   
	
}

/** Panels use some metadata to use the surfaces. A panel entry has the surface
 * id, an unknown number, [transparent], and the area the surface will be pasted
 * to. These entries we call minisurfaces.
 */
class MiniSurface {

	/** this is the destination area of a surface */
	public PitArea area;
	/** the surface id */
	public int sid;
	/** unknown */
	public int num;
	/** the surface, loaded when load is called */
	public PitSurface ps;
	/** indicates the minisurface is loaded */
	public boolean loaded = false;

	/** constructs a minisurface, passing the surfaces as argument, so it can get
	 * its own surface when load is called
	 */
	public MiniSurface() {
		area = new PitArea();
		int num = 0;
	}

	/**
	 * Sets this minisurfaces manually, instead of reading from a file. Used when
	 * editing surfaces properties
	 *
	 */
	public void setProperties(int sid, Rectangle area){
		this.sid = sid;
		this.area = new PitArea(area);
		this.area.transparent = true;
		this.area.opaque = false;
	}

	public String toString() {
		return sid + " " + num + " " + area;
	}

	public void parseData(StreamTokenizer st) throws Exception {
		sid = (int) st.nval;
		st.nextToken();
		if (st.ttype != st.TT_NUMBER) {
			throw new Exception("surfaces expecting number");
		}
		num = (int) st.nval;
		st.nextToken();
		area.parseData(st);
	}

	/**
	 * When the minis are read, we dont have the surfaces yet. This is an 
	 * optimization, must be called prior to drawing the surface, so the minis
	 * can get their surfaces.
	 *
	 */
	public void load(){
		ps = (PitSurface)Cockpit.surfs.get(sid);
		loaded = true;
	}

	/** unloads the surface. Means it will be loaded before next use. */
	public void unload(){
		loaded = false;
	}

}


/** this nested class represents a dialog with all objects in the panel shown as
 * a list. Its used so the user can select objects from here.
 */
class PanelObjectList extends JDialog implements ActionListener, ListSelectionListener {
	JList list;
	Vector objects = new Vector();
	PitPanel panel;
	JButton closeBt = new JButton("Close");

	public PanelObjectList(PitPanel p, JFrame parent){
		super(parent);
		panel = p;
		//add buttonviews
		int size = p.buttonviewsVector.size();
		for (int i=0;i<size;i++){
			objects.add(p.buttonviewsVector.get(i));
		}
		//add objects
		size = p.objectsVector.size();
		for (int i=0;i<size;i++){
			objects.add(p.objectsVector.get(i));
		}

		//add huds, mfds and rwr
		if (panel.hud != null){
			objects.add(panel.hud);
		}
		if (panel.rwr != null){
			objects.add(panel.rwr);
		}

		for (int i=0;i<4;i++){
			if (panel.mfds[i] != null){
				objects.add(panel.mfds[i]);
			}
		}

		//sort the objects vector
		sortObjects();

		//create the list
		list = new JList(objects);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new javax.swing.ListCellRenderer() {
			JLabel l = new JLabel();
			public Component getListCellRendererComponent(
				JList list,
				Object value,            // value to display
				int index,               // cell index
				boolean isSelected,      // is the cell selected
				boolean cellHasFocus)    // the list and the cell have the focus
			{                
				String s = value.toString();
				l.setText(s);
				if (value instanceof PitButtonView){
					l.setForeground(UsPr.bvColor);
				}
				else if (value instanceof PitLight){
					l.setForeground(UsPr.lightColor);
				}
				else if ((value instanceof PanelMFD) || (value instanceof PanelRWR) ||
					(value instanceof PanelHud)){
					l.setForeground(UsPr.panelObjColor);
					}
				else {
					l.setForeground(UsPr.otherColor);
				}

				//setIcon((s.length() > 10) ? longIcon : shortIcon);
				if (isSelected) {
					l.setBackground(list.getSelectionBackground());
				} else {
					l.setBackground(Color.GRAY);
				}
				l.setEnabled(list.isEnabled());
				l.setFont(list.getFont());
				l.setOpaque(true);
				return l;
			}            
		});

		//creates the window
		setProperties();
	}

	private void setProperties(){
		setSize(new Dimension(300, 400));
		Container c = this.getContentPane();
		c.setLayout(new BorderLayout());
		c.add(new JLabel("Pick an object", JLabel.CENTER), BorderLayout.NORTH);
		JScrollPane sp = new JScrollPane(list);
		c.add(sp, BorderLayout.CENTER);
		closeBt.setBackground(Color.RED);
		c.add(closeBt, BorderLayout.SOUTH);
		setModal(true);
		closeBt.addActionListener(this);
		list.addListSelectionListener(this);
	}

	/** order the objects in the vector, by name and id */
	private void sortObjects(){
		int position = 1;
		int size = objects.size();
		int res, ind, ind2;
		PitObject po, poAt;
		String spo, spoAt;

		for (position=1;position < size; position++){          
			po = (PitObject)objects.get(position);
			spo = po.toString();
			if ( (ind = spo.indexOf(' ')) >= 0 ){
				spo = spo.substring(0, ind);
			}
			for (int i=0;i<position;i++){
				poAt = (PitObject)objects.get(i);
				spoAt = poAt.toString();
				if ( (ind2 = spoAt.indexOf(' ')) >= 0 ){
					spoAt = spoAt.substring(0, ind2);
				}

				res = spo.compareTo(spoAt);
				if ((res < 0) || ((res == 0) & (po.id < poAt.id))){
					//we move the object to this position
					objects.remove(position);
					objects.insertElementAt(po, i);
					break;
				}
			}
		}
	}

	public void showObjects(){
		setVisible(true);
	}

	public void actionPerformed(ActionEvent ae){
	    Object o = ae.getSource();
	    if (o==closeBt){
		//close the window
		this.dispose();
	    }
	}

	public void valueChanged(ListSelectionEvent e){
	    Cockpit.setSelectedObject((PitObject)objects.get(list.getSelectedIndex()));
	    Cockpit.centerOnSelectedObject(true, true);
	}

}

class PanelPropertiesDialog extends PropertiesDialog {

	PitPanel panel;

	/** 
	 * We only instanciate this for new panels 
	 */
	LabeledNumField panelId;

	AdjPanelsPane adjPanels; //ajacent panels
	AreasFieldPanel mousebounds = new AreasFieldPanel(this, "mousebounds", false);

	LabeledCBox dogeometry = new LabeledCBox("dogeometry", "Draw a 3d model externally?");

	LabeledField bgFile = new LabeledField(this, "Image", 
			"background filename", 10, "Browse");

	/* Xis says a masktop can never be 0, always in the range of [1,MAXY],
	 * the masktop is the area your 2d pit covers the 3d world, so there is less
	 * rendering 
	 */
	LabeledNumField masktop = new LabeledNumField(this, "masktop", 6),
	    cursorid = new LabeledNumField(this, "cursorid", 4);    

	LabeledFloatFields offset = new LabeledFloatFields(this, "offset", 2, 5);
	String pantiltLabels[] = {"pan(X)", "tilt(Y)"};
	LabeledFloatFields pantilt = new LabeledFloatFields(this, "pantilt", pantiltLabels, 2, 5);

	LabeledNumField fonts[] = new LabeledNumField[3];

	//comments
	CommentsPane comments = new CommentsPane(this);

	//OF specific stuff
	LabeledNumField litPalette = new LabeledNumField(this, "litpalette", 10);

	/** Creates a dialog with all panel properties. Also sets some attributes
	 * for the dialog, like size, modal, layout, etc. Called when creating a 
	 * new panel.
	 *
	 * @param   panel the panel we are creating or updating
	 * @param   parent the parent window
	 * @param   isNew are we creating a new panel or editing an existing one?
	 */
	public PanelPropertiesDialog(JFrame parent, PitPanel panel, boolean isNew){
		super(parent, null, "Panel Properties", isNew);

		this.isNew = isNew; //we are trying to create a panel
		setProperties(panel);
	}

	private void setProperties(PitPanel panel){    
		this.panel = panel;
		Container c = this.getContentPane();

		//the main panel has only a JTabbedPane
		//the tabbedPane
		JTabbedPane tp = new JTabbedPane(JTabbedPane.LEFT);
		tp.setBackground(Color.lightGray);
		//add the tab as the center part
		c.add(tp, BorderLayout.CENTER);

		{
			//main properties
			JPanel mainP = new JPanel(new GridLayout(10, 1));

			//ID
			if (isNew){
			    panelId = new LabeledNumField(this, "PanelID", 4);
			    mainP.add(panelId);
			}
			else{
			    mainP.add(new JLabel("Panel #" + panel.id, JLabel.CENTER));
			}
			//cursorid
			mainP.add(cursorid);            
			//dogeometry
			mainP.add(dogeometry);
			//offset
			mainP.add(offset);
			//pantilt
			mainP.add(pantilt);
			//masktop
			mainP.add(masktop);
			//background (not yet)
			//mainP.add(bgFile);
			//fonts
			for (int i=0;i<fonts.length;i++){
				fonts[i] = new LabeledNumField(
					this, 
					PitPanel.fontsStr[i],
					"choose font index for " + PitPanel.fontsStr[i] + 
					" (use -1 for font given in manager section)",
					5
				);
				mainP.add(fonts[i]);
			}
			tp.add("main", mainP);
			tp.setToolTipTextAt(0, "main properties");
		}
		{
			//buffer data
			JPanel bufferPanel = new JPanel(new BorderLayout());

			bgFile.addActionListener(this);
			bufferPanel.add(bgFile);

			tp.add("Buffer", bufferPanel);
			tp.setToolTipTextAt(1, "Buffers, surfaces, bg images");
		}
		{
			//adjacent panels
			adjPanels = new AdjPanelsPane(this);
			adjPanels.addActionListener(this);


			tp.add("adjPan", adjPanels);
			tp.setToolTipTextAt(2, "Adjacent panels");


		}
		{
			//OF specific options
			JPanel ofPanel = new JPanel();
			ofPanel.add(litPalette);
			tp.add("OF", ofPanel);
			tp.setToolTipTextAt(3, "Open falcon specific options");
			if (UsPr.datFormat != UsPr.DAT_OF){
				tp.setEnabledAt(3, false);
			}
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
	
	protected void loadValues(){
		//main values
		{
			cursorid.setValue(panel.cursorid);
			dogeometry.setSelected(panel.dogeometry);
			offset.setFields(panel.offset);
			pantilt.setFields(panel.pantilt);
			masktop.setValue(panel.masktop);
			for (int i=0;i<fonts.length;i++){
			    fonts[i].setValue(panel.fonts[i]);
			}
		}
		//adjpanels
		{
			adjPanels.setAdjPanels(panel.adjpanels);
		}
		// buffer
		{
			PitBuffer buffer = panel.buffer;
			if (buffer != null){
				bgFile.setText(panel.buffer.getFilename());
			}
		}
		//OF options
		{
			if (UsPr.datFormat == UsPr.DAT_OF){
				litPalette.setValue(panel.litpalette);
			}
		}
		//comments
		comments.setComments(panel.comments);


	}

	protected void setValues() throws Exception {
		{
			//main properties
			if (isNew){
				int pid = panelId.getValue();
				if (Cockpit.panels.get(pid) != null){
					//id already exists, showmessage and raise exception
					JOptionPane.showMessageDialog(
						parent, "ID " + pid + " already used",
						"Error", JOptionPane.ERROR_MESSAGE
					);
					throw new Exception("Invalid ID");
				}
				panel.id = pid;
			}

			panel.cursorid = cursorid.getValue();
			panel.dogeometry = dogeometry.isSelected();
			panel.offset = offset.getFieldsAsInteger();
			panel.pantilt = pantilt.getFields();

			//fonts
			for (int i=0;i<fonts.length;i++){
				panel.fonts[i] = fonts[i].getValue();
			}

			int mtop = masktop.getValue();
			/*                if ((mtop <= 0) || (mtop > Cockpit.resolution.height) ){
			//invalid masktop
			JOptionPane.showMessageDialog(
			parent, "Invalid masktop value: " + mtop,
			"Error", JOptionPane.ERROR_MESSAGE);
			throw new Exception("Invalid masktop");
			}*/
			panel.masktop = mtop;
			//compute real tilt
			panel.computeHorizon();                
		}
		{
			//ok, so if the person choose empty, we null buffer. Else
			// we use the given image. But we compare if old name = new name
			//buffer
			String filename;
			try{
				filename = bgFile.getText();
			}
			catch (Exception e){
				filename = null;
			}


			if ( (filename == null) || (filename.equals("")) ){
				//here we null the buffer and minis
				if (panel.buffer != null){
					panel.buffer.unload();
				}
				panel.buffer = null;
				//delete old surfaces
				if (panel.minisurfaces != null){
					for (int i=0;i<panel.minisurfaces.size();i++){
						int sid = ((MiniSurface)panel.minisurfaces.get(i)).sid;
						Cockpit.surfs.delete(sid);
					}
					panel.minisurfaces.clear();
				}

				panel.load();
			}
			else {
				//we have a filename

				//if buffer ! null, see if need reload(image file != new file)                    
				//else buffer null, create a new one
				if (panel.buffer != null){
					String oldFilename = panel.buffer.getFilename();
					if (!oldFilename.equalsIgnoreCase(filename)){
						panel.buffer.unload();

						//delete old surfaces
						if (panel.minisurfaces != null){
							for (int i=0;i<panel.minisurfaces.size();i++){
								int sid = ((MiniSurface)panel.minisurfaces.get(i)).sid;
								Cockpit.surfs.delete(sid);
							}
							panel.minisurfaces.clear();
						}

						//update buffer and surface
						panel.buffer.setFilename(filename);
						int sid = Cockpit.surfs.getFreeId(panel.id);
						PitSurface newsurf = new PitSurface(sid);
						newsurf.setArea(
								new Rectangle(0, 0, 
									Cockpit.resolution.width,
									Cockpit.resolution.height));
						Cockpit.surfs.add(newsurf, sid);

						//and its reference, the mini
						MiniSurface ms = new MiniSurface();
						ms.setProperties(sid, new Rectangle(0, 0, 
									Cockpit.resolution.width,
									Cockpit.resolution.height));
						panel.minisurfaces.add(ms);
						panel.load();
					}
				}
				else {
					//buffer was null, create a new one
					//create the buffer, load the data
					panel.buffer = new PitBuffer(1);
					panel.buffer.setFilename(filename);

					//create the surface
					int sid = Cockpit.surfs.getFreeId(panel.id);
					PitSurface newsurf = new PitSurface(sid);
					newsurf.setArea(
							new Rectangle(0, 0, 
								Cockpit.resolution.width,
								Cockpit.resolution.height));
					newsurf.setFileName(filename);
					Cockpit.surfs.add(newsurf, sid);

					//and its reference, aka minisurface
					MiniSurface ms = new MiniSurface();
					ms.setProperties(sid, new Rectangle(0, 0, 
								Cockpit.resolution.width,
								Cockpit.resolution.height));
					panel.minisurfaces.add(ms);
					panel.load();
				}
			}
		}
		{
			//adjpanels
			panel.adjpanels = adjPanels.getAdjPanels();

		}
		{
			//comments
			panel.comments = comments.getComments();
		}
		{
			//OF values
			if (UsPr.datFormat == UsPr.DAT_OF){
				panel.litpalette = litPalette.getValue();
			}
		}
	}

	protected boolean processActionEvent(ActionEvent e){
		Object obj = e.getSource();

		if (obj == bgFile){
			//open the dialog so one can choose the file
			JFileChooser chooser = new JFileChooser(Cockpit.cwd);

			GifFilter gf = new GifFilter();
			chooser.setFileFilter(gf);
			chooser.showOpenDialog(this);
			File f;
			if ((f = chooser.getSelectedFile()) != null){
				String name = f.getName();
				bgFile.setText(name.toLowerCase());
			}
			return true;
		}
		else if (obj == adjPanels){
			//get the id of the panel
			String s = e.getActionCommand();
			int pid;

			try {
				pid = Integer.parseInt(s);
			}
			catch (Exception ex){
				pid = -1;
			}

			//does the panel exist?
			if (Cockpit.panels.get(pid) == null){
				JOptionPane.showMessageDialog(
						parent, 
						"Invalid panel", "Error", 
						JOptionPane.ERROR_MESSAGE);                
				return true;
			}

			//lets get the window first		
			Cockpit.setCurrentPanel(pid);
			setVisible(false);
			return true;
		}
		return false;
	}
}


