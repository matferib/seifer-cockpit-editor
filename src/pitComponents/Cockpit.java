package pitComponents;

import actions.ActionInterface;
import actions.CantRedoException;
import actions.CantUndoException;
import pitComponents.pitObjects.*;
import pitComponents.pitHelperComponents.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import pitComponents.pitHelperComponents.PitComponentArray;
import cockpitEditor.UsPr;

/**
 * The representation of the cockpit. Contains all data related to the cockpit,
 * such as the objects vector, buttonviews vector, and general information,
 * like resolution.
 */
public class Cockpit {
	/** listener of cockpit loading events */
	private static Vector<CockpitLoaderListener> loaderListeners = new Vector<CockpitLoaderListener>();
	/** listeners for cockpit change events */
	private static Vector<CockpitListener> pitListeners = new Vector<CockpitListener>();
	
	/** when editing a panel, you can have one or more objects selected. This
	 * vector holds those objetcs. They are draw in selected object color in UI.
	 */
	private static Vector<PitObject> selectedObjects = new Vector<PitObject>();	

    
	/** indicates the pit we are working on is new, ie, has not been saved yet */
	public static boolean isNew = true;
	/** the working directory, like c:\games\falcon4\art\ckptart\ */
	public static File cwd = null;
	/** Cockpit file, like 16_ckpit.dat */
	private static File file = null;
	/** Pit resolution */
	public static Dimension resolution = null;
	/** Manager */
	public static PitManager pm = null;
	/** Buttons */
	public static PitComponentArray buttons = null;
	/** Buttonviews */
	public static PitComponentArray bviews = null;
	/** Panels */
	public static PitComponentArray panels = null;
	/** surfaces */
	public static PitComponentArray surfs = null;
	/** Objects(no buttonviews nor buttons) */
	public static PitComponentArray objs = null;
	/** Sounds */
	public static PitComponentArray sounds = null;

	/** flood light possible states */
	static public final int FLOOD_OFF = 0;
	static public final int FLOOD_LOW = 1;
	static public final int FLOOD_HIGH = 2;
	/** Cockpit flood light state. Flood iluminates all colors of palette  */
	private static int floodState = FLOOD_OFF;


	/** instrument light possible states. 
	 * Illuminates only the last colors of palette for all objects, except lights, 
	 * which use full brightness for last ones.
	 */
	static public final int INST_OFF = 0;
	static public final int INST_LOW = 1;
	static public final int INST_HIGH = 2;
	private static int instState = INST_OFF;
	
	/** buffers */
	//public Vector buffers = new Vector(20);
	/** template object */
	public static PitTemplate template = null;
	/** The current panel being edited. */    
	public static PitPanel currentPanel = null;

	/** the actions list, for undo redo. We always add objects to the end of the
	 * list, and remove the first one, ie the first one is the oldest.
	 */
	private static LinkedList actionList = new LinkedList();
	/** pointers to actions */
	private static ActionInterface actionToUndo = null, actionToRedo = null;

	/** Is the cockpit loaded? */
	public static boolean loaded = false;

	/** Flushes everything
	*/
	public static void flush(){
	    // notifiy listeners
		for (CockpitLoaderListener cll : loaderListeners){
			cll.cockpitUnloaded();
		}	    	    
		cwd = null;
		file = null;
		resolution = null;
		pm = null;
		buttons = null;
		bviews = null;
		panels = null;
		surfs = null;
		objs = null;
		sounds = null;
		template = null;
		currentPanel= null;
		loaded = false;
	}
	
	/** setup containers before reading */
	private static void setup(){
	    buttons = new PitComponentArray();
	    bviews = new PitComponentArray();
	    panels = new PitComponentArray();
	    surfs = new PitComponentArray();
	    objs = new PitComponentArray();
	    sounds = new PitComponentArray();
	}
	
	/** flushes and setup */
	private static void flushAndSetup(){
	    flush();
	    setup();
	}

	/** Saves the cockpit being edited. The old file is saved with a .bak
	 * suffix.
	 *
	 * @throws  Exception   if an error happens.
	 */
	public static void save() throws Exception{
		//get prefix
		String filename = file.getName();
		int dotIndex = filename.indexOf('.');
		String prefix = filename.substring(0, dotIndex);

		//backup file
		String backupName = prefix + ".bak";
		File backupFile = new File(Cockpit.cwd, backupName);

		//save file and destination file
		String saveFileName = prefix + ".new";
		File saveFile = new File(Cockpit.cwd, saveFileName);
		File destFile = file;

		if (saveFile.exists()){
			if (!saveFile.delete()){
				System.out.println("WARNING: delete of old temporary "+ saveFile + " failed");                
			}
		}
		//saveFile.createNewFile();

		//throws if save is not successful
		misc.RAFile arq = new misc.RAFile(saveFile, "rw");

		//we save the manager
		pm.writeComponent(arq);

		//first the sounds
		for (int i=0;i<sounds.getNumComponents();i++){
			((PitSound)sounds.elementAt(i)).writeComponent(arq);
		}

		//we save the buttons, but first corrects all refcounts
		PitButton.correct();
		for (int i=0;i<buttons.getNumComponents();i++){
			PitButton bt = ((PitButton)buttons.elementAt(i));
			bt.writeComponent(arq);

		}

		//buttonviews
		for (int i=0; i<bviews.getNumComponents();i++){
			PitButtonView bv = (PitButtonView)bviews.elementAt(i);
			bv.writeComponent(arq);
		}

		//objects
		for (int i=0;i<objs.getNumComponents();i++){
			((PitObject)objs.elementAt(i)).writeComponent(arq);
		}

		//now we save the panels
		for (int i=0;i<panels.getNumComponents();i++){
			((PitPanel)panels.elementAt(i)).writeComponent(arq);
		}

		//this map is used to check for shared surfaces between panels
		Map usedSurfs = new HashMap(Cockpit.surfs.getNumComponents());

		//we loop again over panels, saving now buffers and surfaces for
		//each panel
		int numPanels = panels.getNumComponents();
		for (int i=0;i<numPanels;i++){
			//            System.out.println(Integer.toString(i));
			PitPanel pp = ((PitPanel)panels.elementAt(i));

			//here we get the surfaces used by this panel
			int surfs[] = pp.getSurfacesId();
			boolean isShared = false;
			for (int j=0;j<surfs.length;j++){
				Integer sId = new Integer(surfs[j]);
				if (usedSurfs.containsKey(sId)){
					isShared = true;
					break;
				}
			}            

			if (isShared){
				//do nothing, skip this panel
				arq.writeln("//Starting panel " + pp.id);
				arq.writeln("//Panel uses shared surface, skipping...");
				arq.writeln("//--------------------------");

			}
			else {
				//else we write the surfaces and add them to the map
				arq.writeln("");
				arq.writeln("//Starting panel " + pp.id);
				pp.writeBuffer(arq); //write panel buffer
				pp.writeSurfaces(arq); //write panel surfaces
				arq.writeln("//--------------------------");

				Integer pId = new Integer(pp.id);
				//add the surfaces to the map
				for (int j=0;j<surfs.length;j++){
					Integer sId = new Integer(surfs[j]);
					usedSurfs.put(sId, pId);
				}
			}
		}
		arq.close();

		//all went well, now we create backup of old one
		//if it exists, we create another backup
		if (backupFile.exists()){
			//try to find a backup file
			int bakId = 1;
			do {
				backupName = prefix + "-" + bakId + ".bak";
				bakId++;
				backupFile = new File(Cockpit.cwd, backupName);
			} while (backupFile.exists() && (bakId < 100));

			//if we cant find a backup name, issue warning
			if (bakId == 100){
				System.out.println(
						"Backup overflow, delete you backup files. Using " +
						prefix + "bak");
				backupName = prefix + ".bak";
				backupFile = new File(Cockpit.cwd, backupName);
				//as we are using an existing fil, we must delete it
				if (!backupFile.delete()){
					System.out.println("WARNING: delete of " + backupFile + " failed");
				}
			}            
		}

		//rename the current file to backup
		if (!destFile.renameTo(backupFile)){
			System.out.println("WARNING: could not rename "+ destFile +" to " + backupFile);
			//try deleting the destination if it still exists for some weird reason
			if (destFile.exists()){
				if (!destFile.delete()){
					System.out.println("WARNING: could not delete " + destFile + " file");
				}
			}
		}

		if (!saveFile.renameTo(destFile)){
			System.out.println("WARNING: could not move " + saveFile + " to " + destFile);
		}

	}

	/** Checks the pit, in several ways. Now, what it does:
	 * - checks if the numbers declared in the manager are right.
	 * - checks all buttons reference count
	 * - checks if all images exist
	 * - check for duplicate surface entries
	 * - calls the check function of all objects(but theyre not implemented
	 *      yet
	 *
	 * @return   the list of errors, if any.
	 */
	public static Vector checkPit(){
		Vector v = new Vector();

		//check manager
		String s = pm.check();
		if (s.length() != 0){
			v.add(s);
		}

		//check template
		s = template.check();
		if (s.length() != 0){
			v.add(s);
		}        
		//check buttons
		s = PitButton.check();
		if (s.length() != 0){
			v.add(s);
		}

		//check buttonviews
		for (int i=0;i<bviews.getNumComponents();i++){
			s = ( ((PitButtonView)(bviews.elementAt(i))).check());
			if (s.length() != 0){
				v.add(s);
			}
		}
		//check objects
		for (int i=0;i<objs.getNumComponents();i++){
			s= ((((PitObject)(objs.elementAt(i))).check()));
			if (s.length() != 0){
				v.add(s);
			}
		}


		//surfaces map, for duplicate check
		Map surfaceMap = new HashMap(Cockpit.pm.numsurfaces);

		//for each panel
		int ps = panels.getNumComponents();
		for (int i=0;i<ps;i++){
			//get panel
			PitPanel p = ((PitPanel)panels.elementAt(i));

			//here we get all surfaces from this panel
			//trying to see if they were already used
			int ids[] = p.getSurfacesId();

			//for each surface the panel has
			for (int j=0;j<ids.length;j++){
				Integer idInt = new Integer(ids[j]);
				if (surfaceMap.containsKey(idInt)){
					//if surface already added to map,
					//it is used by other panel, and we show error
					s = "Surface " + ids[j] + " is referenced in panels " +
						surfaceMap.get(idInt) + " and " + p.id;
					v.add(s);
				}
				else {
					//else we add it to our map of surfaces
					surfaceMap.put(idInt, new Integer(p.id));
				}                
			}

			s = ((p.check()));
			if (s.length() != 0){
				v.add(s);
			}
		}

		//check for duplicate surface entries

		return v;
	}

	/** removes all non used objects, generating a clean file 
	 * We dont discard buttons(not buttonviews), as they are useful even when
	 * not used. We look at duplicate surfaces too
	 */
	public static void clearCockpit(){
		//the new arrays
		PitComponentArray bviews = new PitComponentArray();
		PitComponentArray objects = new PitComponentArray();

		//surfaces map, for duplicate check
		Map surfaceMap = new HashMap(Cockpit.pm.numsurfaces);

		int numPanels = Cockpit.panels.getNumComponents();
		//for each panel
		for (int i=0;i<numPanels;i++){
			//get the panel
			PitPanel p = (PitPanel)Cockpit.panels.elementAt(i);

			/*
			//here we get all surfaces from this panel
			//trying to see if they were already used
			int ids[] = p.getSurfacesId();

			//for each surface the panel has
			for (int j=0;j<ids.length;j++){
			Integer idInt = new Integer(ids[j]);
			if (surfaceMap.containsKey(idInt)){
			//if surface already added to map,
			//it is used by other panel, so we create a new one
			//for this panel
			try {
			p.createNewSurfaceFrom(ids[j]);
			}
			catch (Exception e){
			System.out.println(e);
			}
			}
			else {
			//else we add it to our map of surfaces
			surfaceMap.put(idInt, new Integer(p.id));
			}                
			}
			 */

			//get the panel objects
			Vector v = p.objectsVector;
			int arraySize = v.size();
			//save them
			for (int j=0;j<arraySize;j++){
				PitObject po = (PitObject)v.get(j);
				objects.add(po, po.id);
			}
			//get panel buttonviews
			v = p.buttonviewsVector;
			arraySize = v.size();
			//save them
			for (int j=0;j<arraySize;j++){
				PitObject po = (PitObject)v.get(j);
				bviews.add(po, po.id);
			}
		}

		//discard old ones
		Cockpit.bviews = bviews;
		Cockpit.objs = objects;
		//        Cockpit.surfs = surfaces;
	}
	/* End save and check ***********************************/


	/** Returns a string array, containing the panel names.
	 *
	 * @return  a String array, with all panel names
	 */
	public static String[] getPanelNames() {
		String pstring[] = new String[panels.getNumComponents()];
		for (int i = 0; i < pstring.length; i++) {
			pstring[i] = Integer.toString(((PitPanel) (panels.elementAt(i))).id);
		}
		return pstring;
	}

	/** this functions parses a string, adding the objects to an existing cockpit
	 * Its called when we get data from clipboard, and parses the objects on the
	 * fly. The object IDs are all changed, to match the destination panel
	 *
	 * @param buffer we read from it
	 * @return the number of objects successfully pasted
	 */
	public static int readString(String buffer){
		//usual checks first
		if ((currentPanel == null) || (buffer == null)){
			return 0;
		}
		//start the tokenizer
		PitTokenizer st = new PitTokenizer(new StringReader(buffer));
		st.normalState();
		PitComment comments;
		int indTralha, bvId, obId, btId, sdId;
		int numObjectsAdded = 0;
		//begin parsing
		try {
			do {
				//st.commentState();
				comments = new PitComment();
				comments.readComments(st);

				//we ignore id and get our own
				//we get a freeid for the object
				bvId = bviews.getFreeId(currentPanel.id);
				obId = objs.getFreeId(currentPanel.id);
				btId = buttons.getFreeId(currentPanel.id);
				sdId = sounds.getFreeId(currentPanel.id);
				if ((bvId < 0) || (obId < 0) || (btId < 0) || (sdId < 0)){
					throw new ParseErrorException("Cant get object ID!");
				}

				//reads the component type
				st.nextToken();

				//here we break
				if (st.ttype == st.TT_EOF){
					break;
				} else if (st.ttype != st.TT_WORD) {
					throw new ParseErrorException("Expecting component type");
				}

				//now we parse what we got
				if (st.sval.equals("adi")) {
					PitADI a = new PitADI(obId);
					a.comments = comments;
					a.parseData(st);
					objs.add(a, obId);
					currentPanel.objectsVector.add(a);
				} else if (st.sval.equals("button")) {
					PitButton bt = new PitButton(btId);
					bt.comments = comments;
					bt.parseData(st);
					buttons.add(bt, btId);
				} else if (st.sval.equals("buttonview")) {
					PitButtonView bv = new PitButtonView(bvId);
					bv.comments = comments;
					bv.parseData(st);
					bviews.add(bv, bvId);
					currentPanel.buttonviewsVector.add(bv);
				} else if (st.sval.equals("chevron")) {
					PitChevLift ch = new PitChevLift(false, obId);
					ch.comments = comments;
					ch.parseData(st);
					objs.add(ch, obId);
					currentPanel.objectsVector.add(ch);
				} else if (st.sval.equals("ded")) {
					PitDED d = new PitDED(obId);
					d.comments = comments;
					d.parseData(st);
					objs.add(d, obId);
					currentPanel.objectsVector.add(d);
				} else if (st.sval.equals("dial")) {
					PitDial d = new PitDial(obId);
					d.comments = comments;
					d.parseData(st);
					objs.add(d, obId);
					currentPanel.objectsVector.add(d);
				} else if (st.sval.equals("liftline")) {
					PitChevLift ll = new PitChevLift(true, obId);
					ll.comments = comments;
					ll.parseData(st);
					objs.add(ll, obId);
					currentPanel.objectsVector.add(ll);
				} else if (st.sval.equals("digits")) {
					PitDigits d = new PitDigits(obId);
					d.comments = comments;
					d.parseData(st);
					objs.add(d, obId);
					currentPanel.objectsVector.add(d);
				} else if (st.sval.equals("hsi")) {
					PitHSI h = new PitHSI(obId);
					h.comments = comments;
					h.parseData(st);
					objs.add(h, obId);
					currentPanel.objectsVector.add(h);
				} else if (st.sval.equals("indicator")) {
					PitIndicator in = new PitIndicator(obId);
					in.comments = comments;
					in.parseData(st);
					objs.add(in, obId);
					currentPanel.objectsVector.add(in);
				} else if (st.sval.equals("kneeview")) {
					PitKneeboard kv = new PitKneeboard(obId);
					kv.comments = comments;
					kv.parseData(st);
					objs.add(kv, obId);
					currentPanel.objectsVector.add(kv);
				} else if (st.sval.equals("light")) {
					PitLight l = new PitLight(obId);
					l.comments = comments;
					l.parseData(st);
					objs.add(l, obId);
					currentPanel.objectsVector.add(l);
				} else if (st.sval.equals("machasi")) {
					PitMachASI m = new PitMachASI(obId);
					m.comments = comments;
					m.parseData(st);
					objs.add(m, obId);
					currentPanel.objectsVector.add(m);
				} else if (st.sval.equals("sound")) {
					PitSound s = new PitSound(sdId);
					s.comments = comments;
					s.parseData(st);
					sounds.add(s, sdId);
				} else if (st.sval.equals("text")) {
					PitText t = new PitText(obId);
					t.comments = comments;
					t.parseData(st);
					objs.add(t, obId);
					currentPanel.objectsVector.add(t);
				} else {
					throw new ParseErrorException("Object cant be pasted");
				}
				numObjectsAdded++;
			}
			while (true);
			Cockpit.currentPanel.repaint();
			Cockpit.template.repaint();
		}
		catch (Exception e){
			Cockpit.currentPanel.repaint();
			Cockpit.template.repaint();
			return numObjectsAdded;
		}

		return numObjectsAdded;
	}

	/** Creates a new cockpit object. Program does so by receiving user input on
	 * cockpit resolution, cockpit directory and reading the template file.
	 *
	 * @param res cockpit resolution
	 * @param dir cockpit directory
	 * @param tFile cockpit template dat file
	 */
	public static void createNew(Dimension res, File dir, File datTemplate) 
		throws Exception {

		if (res == null){
			throw new Exception("Invalid resolution(null)");
		}
		else if (dir == null){
			throw new Exception("Invalid directory(null)");
		}

		//read the template file
		Cockpit.readFile(datTemplate, true, res);
		
		// set cwd
		//the file prefix: 8, 10, 12, 16 ...
		String prefix = Integer.toString(res.width / 100);
		file = new File(dir, prefix + "_ckpit.dat");
		//cockpit directory
		cwd = dir;
	}

	/** Parses the cockpit dat file. If the cockpit is not new, also sets the 
	 * cwd and file variables, indicating the dat and the filename. For new cockpits
	 * those vars are set in the createNew function
	 *
	 * @param   file    the cockpit data file
	 * @param isNew indicates this pit is a new cockpit or not
	 * @param resolution the cockpit resolution, null if autodetect from filename
	 * @throws  ParseErrorException if the parser finds syntax error.
	 */
	public static void readFile(File file, boolean isNew, Dimension res)
		throws ParseErrorException 
	{
		flushAndSetup();
		PitTokenizer st = null;

		//lets set our cwd and file if we are not new
		if (!isNew){
			Cockpit.cwd = file.getParentFile();
			Cockpit.file = file;
		}

		PitComment.numCommentsRead = 0;
		//now we get the resolution
		try {
			if (res == null){
				if (file.getName().startsWith("8_")) {
					resolution = new Dimension(800, 600);
				}
				else if (file.getName().startsWith("10_")) {
					resolution = new Dimension(1024, 768);
				}
				else if (file.getName().startsWith("12_")) {
					resolution = new Dimension(1280, 960);
				}
				else if (file.getName().startsWith("16_")) {
					resolution = new Dimension(1600, 1200);
				}
				else {
					throw new Exception("Cant find file resolution");
				}
			}
			else {
				//special resolution
				resolution = res;
			}
		}
		catch (Exception e) {
			throw new ParseErrorException("cockpitObject.java: " + e);
		}
		BufferedReader bufReader = null;
		try {
			bufReader = new BufferedReader(new FileReader(file));
			//init the tokenizer
			st = new PitTokenizer(bufReader);
			st.normalState();
		}
		catch (Exception e) {
			//cant open file
			throw new ParseErrorException(
					"CockpitObject.java: Cannot open input file " + e);
		}      

		try {
			//first: set UsPr to default dat format
			UsPr.datFormat = UsPr.DAT_NONE;

			int id = 0, indTralha;

			// MANAGER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			PitComment managerComments = new PitComment();
			managerComments.readComments(st);

			if ((st.ttype != st.TT_WORD) || ((indTralha = st.sval.indexOf('#')) == -1)) {
				throw new ParseErrorException("expecting #id manager");
			}

			//takes the # out
			id = Integer.parseInt(st.sval.substring(indTralha + 1));
			//reads the manager
			st.nextToken();
			if ( (st.ttype != st.TT_WORD) || (st.sval.indexOf("manager") == -1)) {
				throw new ParseErrorException("Expecting #id manager");
			}

			//lets read the manager!
			pm = new PitManager(id);
			pm.parseData(st);
			pm.comments = managerComments;
			//set static properties on PitClasses

			// ENDMANAGER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

			//Now we read objects, buttons and buttonviews
			PitComment comments;
			do {
				comments = new PitComment();
				comments.readComments(st);

				//gets the #<number>
				if ( 
				  (st.ttype != st.TT_WORD) ||
				  ( (indTralha = st.sval.indexOf('#')) == -1)) 
				{
					throw new ParseErrorException("expecting #id");
				}
				//and takes the # out
				id = Integer.parseInt(st.sval.substring(indTralha + 1));

				//reads the component type
				st.nextToken();
				if (st.ttype != st.TT_WORD) {
					throw new ParseErrorException("Expecting component type");
				}
				else if (st.sval.equals("panel")) {
					break;
				}

				//now we parse what we got
				if (st.sval.equals("adi")) {
					PitADI a = new PitADI(id);
					a.comments = comments;
					a.parseData(st);
					objs.add(a, id);
				}
				else if (st.sval.equals("button")) {
					PitButton bt = new PitButton(id);
					bt.comments = comments;
					bt.parseData(st);
					buttons.add(bt, id);
				}
				else if (st.sval.equals("buttonview")) {
					PitButtonView bv = new PitButtonView(id);
					bv.comments = comments;
					bv.parseData(st);
					bviews.add(bv, id);
				}
				else if (st.sval.equals("chevron")) {
					PitChevLift ch = new PitChevLift(false, id);
					ch.comments = comments;
					ch.parseData(st);
					objs.add(ch, id);
				}
				else if (st.sval.equals("ded")) {
					PitDED d = new PitDED(id);
					d.comments = comments;
					d.parseData(st);
					objs.add(d, id);
				}
				else if (st.sval.equals("dial")) {
					PitDial d = new PitDial(id);
					d.comments = comments;
					d.parseData(st);
					objs.add(d, id);
				}
				else if (st.sval.equals("liftline")) {
					PitChevLift ll = new PitChevLift(true, id);
					ll.comments = comments;
					ll.parseData(st);
					objs.add(ll, id);
				}
				else if (st.sval.equals("digits")) {
					PitDigits d = new PitDigits(id);
					d.comments = comments;
					d.parseData(st);
					objs.add(d, id);
				}
				else if (st.sval.equals("hsi")) {
					PitHSI h = new PitHSI(id);
					h.comments = comments;
					h.parseData(st);
					objs.add(h, id);
				}
				else if (st.sval.equals("indicator")) {
					PitIndicator in = new PitIndicator(id);
					in.comments = comments;
					in.parseData(st);
					objs.add(in, id);
				}
				else if (st.sval.equals("kneeview")) {
					PitKneeboard kv = new PitKneeboard(id);
					kv.comments = comments;
					kv.parseData(st);
					objs.add(kv, id);
				}
				else if (st.sval.equals("light")) {
					PitLight l = new PitLight(id);
					l.comments = comments;
					l.parseData(st);
					objs.add(l, id);
				}
				else if (st.sval.equals("machasi")) {
					PitMachASI m = new PitMachASI(id);
					m.comments = comments;
					m.parseData(st);
					objs.add(m, id);
				}
				else if (st.sval.equals("mirror")){
					PitMirror m = new PitMirror(id);
					m.comments = comments;
					m.parseData(st);
					objs.add(m, id);
				}
				else if (st.sval.equals("sound")) {
					PitSound s = new PitSound(id);
					s.comments = comments;
					s.parseData(st);
					sounds.add(s, id);
				}
				else if (st.sval.equals("text")) {
					PitText t = new PitText(id);
					t.comments = comments;
					t.parseData(st);
					objs.add(t, id);
				}
			}
			while (true);

			//now we read Panels
			do {
				if ((st.sval.equals("buffer"))){
					break;
				}
				else if (st.ttype != st.TT_WORD) {
					throw new ParseErrorException("Expecting panel or buffer");
				}
				else if (st.sval.equals("panel")) {
				    PitPanel pp = new PitPanel(id);
				    pp.parseData(st);
				    pp.comments = comments;
				    panels.add(pp, id);
				}

				//lets read the next panel
				comments = new PitComment();
				comments.readComments(st);

				//gets the #<number>
				if (st.ttype == st.TT_EOF){
					break;
				}
				else if ( (st.ttype != st.TT_WORD) ||
						( (indTralha = st.sval.indexOf('#')) == -1)) {
					throw new ParseErrorException("Error reading panels "+ 
							" expecting #id");
						}

				//and takes the # out
				id = Integer.parseInt(st.sval.substring(indTralha + 1));
				//the element type
				st.nextToken();
			}
			while (true);

			//lets read all buffer and surfaces
			PitBuffer currentBuffer = null;
			do {
				//read a buffer
				if (st.ttype == st.TT_EOF){
					break;
				}
				else if (st.sval.equals("buffer")) {
					PitBuffer pb = new PitBuffer(id);
					pb.parseData(st);
					pb.comments = comments;
					currentBuffer = pb;
					//PitBuffer.skipData(st); //no reading of buffers
				} 
				else {
					//parse error
					throw new ParseErrorException("Expecting buffer");                        
				}

				//then read all the surfaces this buffer has
				do{
					//lets read the surfaces!
					comments = new PitComment();
					comments.readComments(st);

					//gets the #<number>
					if (st.ttype == st.TT_EOF){
						break; //end of file!
					}
					else if ( (st.ttype != st.TT_WORD) ||
							( (indTralha = st.sval.indexOf('#')) == -1)) {
						throw new ParseErrorException("Expecting #id");
							}

					//and takes the # out
					id = Integer.parseInt(st.sval.substring(indTralha + 1));
					//the element type
					st.nextToken();

					if (st.sval.equals("surface")) {
						//System.out.println(id);
						PitSurface ps = new PitSurface(id);
						ps.parseData(st);
						ps.comments = comments;
						surfs.add(ps, id);
						ps.buffer = currentBuffer;
					}
					else if (st.sval.equals("buffer")) {
						break;
					}
					else {
						throw new 
							ParseErrorException("Expecting surface or buffer");
					}
				}while (true); //surfaces

				if (st.ttype == st.TT_EOF){
					break; //EOF
				}
			} while (true); //buffers and surfaces
			// everything has been read, now we link all information
			linkEverything();

			Cockpit.isNew = isNew;
			bufReader.close();
			Cockpit.loaded = true;                    
			System.out.println("Parser ended successfully");
		}
		catch (Exception e) {
			st.pushBack();
			String eToken;
			switch (st.ttype) {
				case (StreamTokenizer.TT_EOF):
					eToken = "<End of file>";
					break;
				case (StreamTokenizer.TT_NUMBER):
					eToken = String.valueOf(st.nval);
					break;
				case (StreamTokenizer.TT_WORD):
					eToken = st.sval;
					break;
				default:
					eToken = String.valueOf( (char) st.ttype);
			}           

			throw new ParseErrorException("\nWrong dat format(parse error)\n" +
					"line: " + (st.lineno()+ PitComment.numCommentsRead) +
					"\nat word(token): " + eToken +
					"\nnumber of Objects(read):" + objs.getNumComponents() +
					"\nnumber of Surfaces(read):" + surfs.getNumComponents()
					);
		}
		// notify loader listeners of cockpit loading
		for (CockpitLoaderListener cll : loaderListeners){
		    cll.cockpitLoaded();
		}		
	}

	/* PitPanel functions ********************************/
	/** changes the current panel being edited. If new panel is invalid, does not
	 * change panel at all and return false.
	 *
	 * @param   id  the panel id
	 * @return false if id is invalid(or -1), true otherwise
	 */
	public static boolean setCurrentPanel(int id) {
	    PitPanel p = null;
	    p = (PitPanel)panels.get(id);

	    if (p != null){
		currentPanel = p;
		// clear selected objects
		selectedObjects.clear();
		
		// notify listeners
		for (CockpitListener cl : pitListeners){
		    cl.panelChanged(currentPanel.id);
		    cl.selectedObjectsChanged();
		}
		return true;
	    }
	    else {
		return false;
	    }
	}

	/** returns the current panel */
	public static PitPanel getCurrentPanel(){
		return currentPanel;
	}

	/** deletes the panel. Cannot delete 1100 */
	public static void deletePanel(int id){
	    //gets the panel
	    PitPanel pp = (PitPanel)panels.get(id);
	    if ((pp == null) || (id == 1100)){
		    return;
	    }

	    // remove pit form listeners
	    UsPr.removeUsPrListener(Cockpit.currentPanel);

	    //deletes everything this panel has
	    pp.deleteAll();
	    panels.delete(id);
	    
	    // go to panel 1100
	    Cockpit.setCurrentPanel(1100);    	    
	}

	/** deletes current panel */
	public static void deleteCurrentPanel(){
	    deletePanel(Cockpit.currentPanel.id);
	}

	
	/** adds a new panel to cockpit and set is as the current one 
	 * @param newPanel panel being added
	 */
	public static void addPanel(PitPanel newPanel){
	    Cockpit.panels.add(newPanel, newPanel.id);
	    Cockpit.setCurrentPanel(newPanel.id);
	    // pit panel constructor adds it to uspr listener
	    //UsPr.addUsPrListener(newPanel);
	}
	
	
	/* ****************************************************/
	
	/* selected objects functions **************************/
	/** move all selected objects by a given offset  
	 * @param offset to move
	 * @param moveInPanel if true move destlocs, otherwise move srclocs
	 * @param moveAll move all srclocs / destlocs?
	 */
	public static void moveSelectedObjects(Point offset, boolean moveInPanel, boolean moveAll){
	    for (PitObject po : selectedObjects){
		if (moveInPanel){
		    if (moveAll){
			po.moveAll(offset.x, offset.y);
		    }
		    else {
			po.move(offset.x, offset.y);
		    }
		    currentPanel.repaint();			    
		}
		else {
		    if (moveAll){
			po.moveAllInTemplate(offset.x, offset.y);
		    }
		    else {
			po.moveInTemplate(offset.x, offset.y);
		    }
		    template.repaint();
		}	    
	    }
	    // notify listeners
	    for (CockpitListener pl : pitListeners){
		pl.selectedObjectsChanged();
	    }	    
	}
	
	/** resize selected object. Only works if a single object is selected
	 *  Only works if theres a single object selected
	 * @param offset to move
	 * @param target corner upon which resize is based
	 * @param resizeInPanel if true, resize in panel, otherwise, template
	 */
	public static void resizeSelectedObject(Point offset, int target, boolean resizeInPanel){
	    if (selectedObjects.size() != 1){
		return;
	    }
	    
	    PitObject po = selectedObjects.get(0);
	    if (resizeInPanel){
		po.resize(offset.x, offset.y, target);
		currentPanel.repaint();
	    }
	    else {
		po.resizeInTemplate(offset.x, offset.y, target);
		template.repaint();
	    }
	    // notify listeners
	    for (CockpitListener pl : pitListeners){
		pl.selectedObjectsChanged();
	    }	  	    
	}
	
	/** returns the first selected object at a given point
	 * @param p the point we are looking for
	 * @param inTemplate look in template instead of panel
	 * @return the object, or null
	 */
	public static PitObject getSelectedObjectsXY(Point p, boolean inTemplate){
	    for (PitObject po : selectedObjects){
		if (inTemplate){
		    if (po.pointIsInside_template(p)){
			return po;
		    }
		}
		else {
		    if (po.pointIsInside(p)){
			return po;
		    }		    
		}
	    }
	    return null;
	}
	
	/** @return number of objects selected */
	public static int getSelectedObjectsSize(){
	    return selectedObjects.size();
	}
	
	/** gets a selected object by index
	 * @return the object at position, or null if out of bounds
	 */
	public static PitObject getSelectedObjectAt(int i){
	    try{
		return selectedObjects.get(i);
	    }
	    catch (Exception e){
		return null;
	    }
	}
	
	/** gets all selected objects as a vector */
	public static Vector<PitObject> getSelectedObjectsAsVector(){
	    Vector<PitObject> ret = new Vector<PitObject>(selectedObjects.size());
	    ret.addAll(selectedObjects);
	    return ret;
	}
	
	/** sends selected objects to cockpit background */
	public static void sendSelectedToBg(){
	    for (PitObject po : selectedObjects){
		currentPanel.sendToBg(po);
	    }
	}

	/** sends selected objects to cockpit foreground */
	public static void sendSelectedToFg(){
	    for (PitObject po : selectedObjects){
		currentPanel.sendToFg(po);
	    }
	}

	/** Deletes selected object from cockpit and panel. */
	public static void deleteSelectedObjects(){
	    int size = selectedObjects.size();
	    if ((size == 0) || (currentPanel == null)){
		return;
	    }

	    for (PitObject po : selectedObjects){		    
		currentPanel.deleteObject(po);
	    }
	    selectedObjects.clear();
	    currentPanel.repaint();
	    Cockpit.template.repaint();
	    // notify listeners
	    for (CockpitListener pl : pitListeners){
		pl.selectedObjectsChanged();
	    }
	}	
	
	/** Sets object as selected object
	 * @param p the object being added
	 * @param clear selectedObjects before adding
	 */
	public static void setSelectedObject(PitObject p){
	    selectedObjects.clear();
	    addSelectedObject(p);
	}

	/** Adds the object being passed to the selectedObjects vector. Similar to the
	 * setCurrentObject, but does not clear the vector.
	 * @param p the object being added
	 */
	public static void addSelectedObject(PitObject p) {
	    if (p != null){
		selectedObjects.add(p);
	    }
	    currentPanel.repaint();
	    template.repaint();
	    // notify listeners
	    for (CockpitListener pl : pitListeners){
		pl.selectedObjectsChanged();
	    }	    
	}
	
	/** selects all objects from current panel */
	public static void selectAllObjectsFromCurrentPanel(){
	    selectedObjects.clear();
	    selectedObjects.addAll(currentPanel.buttonviewsVector);
	    selectedObjects.addAll(currentPanel.objectsVector);
	    if (currentPanel.hud != null){
		selectedObjects.add(currentPanel.hud);		
	    }
	    if (currentPanel.rwr != null){
		selectedObjects.add(currentPanel.rwr);		
	    }
	    for (int i=0;i<currentPanel.mfds.length;++i){
		if (currentPanel.mfds[i] != null){
		    selectedObjects.add(currentPanel.mfds[i]);
		}
	    }
	    
	    // notify listeners
	    for (CockpitListener pl : pitListeners){
		pl.selectedObjectsChanged();
	    }	    	    
	}

	/** remove the given object from selected objects list */
	public static void removeFromSelectedObjects(PitObject p){
	    selectedObjects.remove(p);
	    Cockpit.currentPanel.repaint();
	    Cockpit.template.repaint();
	    // notify listeners
	    for (CockpitListener pl : pitListeners){
		pl.selectedObjectsChanged();
	    }	    		
	}

	/** clears selected objects */
	public static void removeAllSelectedObjects(){
	    selectedObjects.clear();
	    Cockpit.currentPanel.repaint();
	    Cockpit.template.repaint();
	    // notify listeners
	    for (CockpitListener pl : pitListeners){
		pl.selectedObjectsChanged();
	    }	    			    
	}

	/** centers panel and template on first selected object 
	 * @param centerPanel indicates panel should center
	 * @param indicates template should be centered
	 */
	public static void centerOnSelectedObject(boolean centerPanel, boolean centerTemplate){
	    PitObject po;
	    try {
		po = selectedObjects.get(0);
	    }
	    catch (Exception e){
		// no objects selected
		return;
	    }
	    if (centerPanel && (currentPanel != null)){
		currentPanel.centerOn(po);
	    }
	    if (centerTemplate && (template != null)){
		template.centerOn(po);
	    }
	}
	
	/** finds if a given object is selected 
	 */
	public static boolean isObjectSelected(PitObject po){
	    return selectedObjects.contains(po);
	}
	
	/** makes all selected objects destlocs equal to srclocs */
	public static void adjustSizeFromDestToAllSelectedObjects(){
	    for (PitObject po : selectedObjects){
		po.equalSrclocsToDest(0);
	    }
	    Cockpit.currentPanel.repaint();
	    Cockpit.template.repaint();	    
	}
	/** makes all selected objects destlocs equal to srclocs */
	public static void adjustSizeFromSrcToAllSelectedObjects(){
	    for (PitObject po : selectedObjects){
		po.equalDestlocToSrc(0);
	    }
	    Cockpit.currentPanel.repaint();
	    Cockpit.template.repaint();	    
	}
	
	public static void equalSrcsToAllSelectedObjects(){
	    for (PitObject po : selectedObjects){
		po.equalSrclocs(0);
	    }
	    Cockpit.currentPanel.repaint();
	    Cockpit.template.repaint();	    	    
	}
	public static void equalDstsToAllSelectedObjects(){
	    for (PitObject po : selectedObjects){
		po.equalDestlocs(0);
	    }
	    Cockpit.currentPanel.repaint();
	    Cockpit.template.repaint();	    	    
	}
	
	/*****************************************************/

	/******************************************************/
	//action related

	/** adds the action to the action list */
	public static void addAction(ActionInterface action){
		actionList.addLast(action);
		actionToUndo = action;
		actionToRedo = null;
		//list limit
		while (actionList.size() > UsPr.maxActions){
			actionList.removeFirst();                
		}        
	}

	/** executes a cockpit action */
	public static void executeAction(ActionInterface action){
		try {
			action.execute();
			addAction(action);
		}
		catch (Exception e){
			System.out.println("Cant execute action: " + e.getMessage());
		}
	}

	/** undo called */
	public static void undoAction() throws CantUndoException{
		//any action?
		if (actionToUndo == null){
			throw new CantUndoException("No more actions to undo");
		}

		//undo action
		actionToUndo.undo();
		actionToRedo = actionToUndo;

		//move to next action
		int ind = actionList.indexOf(actionToUndo);
		int ls = actionList.size();
		actionToUndo = (ind == 0) ?
			null:
			(ActionInterface)actionList.get(ind-1);
	}

	/** redo called */
	public static void redoAction() throws CantRedoException {
		//any action?
		if (actionToRedo == null){
			throw new CantRedoException("No action to redo");
		}

		//redo action
		actionToRedo.redo();
		actionToUndo = actionToRedo;

		//move to next action
		//move to next action
		int ind = actionList.indexOf(actionToRedo);
		int ls = actionList.size();
		actionToRedo = (ind >= (ls-1)) ?
			null:
			(ActionInterface)actionList.get(ind+1);

	}

	/******************************************************/

	/**
	 * This function is called after the cockpit is read, linking all pointers.
	 * It also sets shared variables among all objects, like the cob object. 
	 * In fact, cob should be static.
	 */
	private static void linkEverything() {
		//panels must load the buffers
		for (int i=0;i<panels.getNumComponents();i++){
			PitPanel pp = (PitPanel)panels.elementAt(i);
			pp.linkBuffer();
		}
		Cockpit.template.load();

	}

	/** returns the datfile name */
	public static String getDatName(){
		return file.getName();
	}


	/** toggles flood light state. Possible states are off, low and hight
	 */
	public static void toggleFloodMode(){
		floodState = (floodState+1) % 3;
	}

	/** Sets flood mode to the given state. Must be valid, or no effect */
	public static void setFloodMode(int st){
		if ((st > 3) || (st < 0)){
			return;
		}
		floodState = st;
	}

	/** toggles instrument light state. Possible states are off, low and hight
	 */
	public static void toggleInstMode(){
		instState = (instState+1) % 3;
	}

	/** Sets instrument mode to the given state. Must be valid, or no effect */
	public static void setInstMode(int st){
		if ((st > 3) || (st < 0)){
			return;
		}
		instState = st;
	}

	/** returns flood light in float format, affected by its state. 
		* Light intensity depends on switch state
		*/
	public static float[] getFloodF(){
		float fc[] = new float[3];
		if (floodState == FLOOD_OFF){
			fc[0] = fc[1] = fc[2] = 0.0f;
		}
		else if (floodState == FLOOD_LOW) {
			Color c = pm.getFloodlightColor();
			fc[0] = c.getRed() / 510.0f;
			fc[1] = c.getGreen() / 510.0f;
			fc[2] = c.getBlue() / 510.0f;
		}
		else {
			Color c = pm.getFloodlightColor();
			fc[0] = c.getRed() / 255.0f;
			fc[1] = c.getGreen() / 255.0f;
			fc[2] = c.getBlue() / 255.0f;
		}

		return fc;
	}

	/** returns instrument light in float format, affected by its state. 
		* Light intensity depends on inst switch
	 */
	public static float[] getInstF(){
		float ic[] = new float[3];

		if (instState == INST_OFF){
			ic[0] = ic[1] = ic[2] = 0.0f;
		}
		else if (instState == INST_LOW){
			Color c = pm.getInstlightColor();
			ic[0] = c.getRed() / 510.0f;
			ic[1] = c.getGreen() / 510.0f;
			ic[2] = c.getBlue() / 510.0f;

		}
		else {
			Color c = pm.getInstlightColor();
			ic[0] = c.getRed() / 255.0f;
			ic[1] = c.getGreen() / 255.0f;
			ic[2] = c.getBlue() / 255.0f;
		}

		return ic;
	
	}

	/** reloads all images, from buffers and template.
		* Called on light change
		*/
	public static void reloadImages(){
		// for panels, we unload all, as they need to be reloaded
		for (int i=0;i<panels.getNumComponents();i++){
			PitPanel p = (PitPanel)panels.elementAt(i);
			p.unload();
		}
		// only current panel is loaded

		
		//reload template
		template.load();

		// repaints everything
		template.repaint();
		currentPanel.repaint();

	}

	
	////////////////////////////
	// COCKPIT LISTENER STUFF //
	////////////////////////////
	public static void addCockpitListener(CockpitListener cl){
	    pitListeners.add(cl);
	}
	
	public static void removeCockpitListener(CockpitListener cl){
	    pitListeners.remove(cl);
	}
	
	public static void removeAllCockpitListeners(){
	    pitListeners.clear();
	}
	/////////////////////////////
	// COCKPIT LOADER LISTENER //
	/////////////////////////////
	public static void addCockpitLoaderListener(CockpitLoaderListener cll){
	    loaderListeners.add(cll);
	}
	
	public static void removeCockpitLoaderListener(CockpitLoaderListener cll){
	    loaderListeners.remove(cll);
	}
	
	public static void removeAllCockpitLoaderListeners(){
	    loaderListeners.clear();
	}
	
}

