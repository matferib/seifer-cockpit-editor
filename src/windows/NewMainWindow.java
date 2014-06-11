/*
 * NewMainWindow.java
 *
 * Created on 9 de Outubro de 2006, 20:46
 *
 * Main window for cockpit editor. Will have menu, template and panel at the
 * same frame
 */

package windows;
import actions.ActionTargets;
import cockpitEditor.UsPr;
import cockpitEditor.UsPrListener;
import menus.*;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import pitComponents.Cockpit;
import pitComponents.CockpitListener;
import pitComponents.PitButton;
import pitComponents.PitPanel;
import pitComponents.PitTemplate;
import pitComponents.pitHelperComponents.PitArea;
import pitComponents.pitHelperComponents.PitClipboardVector;
import pitComponents.pitObjects.PitObject;

/**
 * Main window for cockpit editor: responsible for answering menu events
 * and keyboard inputs
 *
 * @author matheus
 */
public class NewMainWindow extends JFrame implements WindowListener, ActionListener, 
			 MainMenuCockpitListener, MainMenuObjListener, MainMenuAboutListener, MainMenuPanelListener, MainMenuTemplateListener,
			 CockpitListener,
			 KeyListener,
			 ClipboardOwner     
{
	/** scroll panes for the panels */
	private JScrollPane pitScPane = new JScrollPane(), templateScPane = new JScrollPane();
	/** constant used to tell which panel is selected */
	static public final int ACTIVE_NONE = 0, ACTIVE_PANEL = 1, ACTIVE_TEMPLATE = 2;
	/** which scroll pane is the active (for receiving keyboard events) */
	static private int activeSide = ACTIVE_NONE;
	/** contains pit panel and template */
	private JSplitPane spPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, pitScPane, templateScPane);

	/** bottom bar, displaying infomation */
	BottomBar bb = new BottomBar();    
	/** main division: main and bottom bar */
	private JSplitPane mainSp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spPane, bb);

	/** main menu */
	private MainMenu mainMenu = new MainMenu();

	/** Creates a new instance of NewMainWindow */
	public NewMainWindow() {
		// window layout
		Container c = this.getContentPane();
		// main division
		mainSp.setDividerSize(3);
		mainSp.setResizeWeight(1.0);
		c.add(mainSp);

		// panel template split
		spPane.setOneTouchExpandable(true);
		spPane.setResizeWeight(0.5);
		spPane.setDividerLocation(0.5);	

		// listen for window events
		addWindowListener(this);
		// listen for cockpit events
		Cockpit.addCockpitListener(bb);
		Cockpit.addCockpitListener(this);

		// menu stuff
		setJMenuBar(mainMenu);
		mainMenu.addMainMenuListener(MainMenu.ABOUT_M, this);
		mainMenu.addMainMenuListener(MainMenu.COCKPIT_M, this);
		mainMenu.addMainMenuListener(MainMenu.PANEL_M, this);		
		mainMenu.addMainMenuListener(MainMenu.OBJECTS_M, this);
		mainMenu.addMainMenuListener(MainMenu.TEMPLATE_M, this);

		// window attributes
		setTitle("Seifers Cockpit Editor for Falcon4");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(800, 600);
		setResizable(true);
		setLocationRelativeTo(null);	

		//process keyboard and mouse events
		addKeyListener(this);
		setFocusable(true);
		requestFocusInWindow();
		requestFocus();
	}

	/** sets the active side. It will receive keyboard inputs 
	 * @param which ACTIVE_NONE, ACTIVE_PANEL, ACTIVE_TEMPLATE
	 */
	static public void setActive(int which){
		switch (which){
			case ACTIVE_PANEL:
				activeSide = ACTIVE_PANEL;
				break;
			case ACTIVE_TEMPLATE:
				activeSide = ACTIVE_TEMPLATE;
				break;
			default:
				activeSide = ACTIVE_NONE;
		}
	}

	///////////////////////////////
	// WINDOW LISTENER INTERFACE //
	///////////////////////////////
	public void windowActivated(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	/** asks before closing */
	public void windowClosing(WindowEvent e){
		closeAndQuit();
	}

	///////////////////////////////
	// ACTION LISTENER INTERFACE //
	///////////////////////////////
	public void actionPerformed(ActionEvent e){
		JButton b = (JButton)e.getSource();
		System.out.println(b.getText());

	}

	/////////////////////////
	// MAIN MENU LISTENERS //
	/////////////////////////
	public void newCockpit(){
		//first: cockpit resolution
		ResolutionDialog rd = new ResolutionDialog(this);
		Dimension res = rd.getResolution();
		if (res == null){
			// no res
			return;
		}
		//second: directory where cockpit stuff will be saved
		JFileChooser c = new JFileChooser(UsPr.cwd);
		c.setMultiSelectionEnabled(false);
		//c.setFileFilter(new NewFileFilter());
		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);           
		c.setDialogTitle("Select the directory for the cockpit");
		c.showOpenDialog(this);
		File dir = c.getSelectedFile();
		if (dir == null){
			// no file
			return;
		}

		// close current pit
		closeCurrentPit();
		//open new cockpit
		try {
			Cockpit.createNew(res, dir, new File("templates/cockpit.dat"));
			openCurrentPit();
		}
		catch (Exception e){
			System.out.println("Could not create new cockpit: " + e);
		}
	}

	public void openCockpit(boolean autoRes){
		File arq;
		JFileChooser chooser = new JFileChooser(Cockpit.cwd == null ? UsPr.cwd : Cockpit.cwd);
		int indSuf;
		String prefix = null;
		String entrStr;
		//get the file name
		chooser.setFileFilter(new CockpitFilter());
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION){
			return;
		}
		
		if ((arq = chooser.getSelectedFile()) != null) {
			//now we read the file
			try {
				closeCurrentPit();
				Dimension res = null;
				if (!autoRes){
					ResolutionDialog rd = new ResolutionDialog(this);
					res = rd.getResolution();
					if (res == null){
						return;
					}
				}
				Cockpit.readFile(arq, false, res);
				openCurrentPit();
			}
			catch (Exception ex) {
				System.out.println("CockpitEditor: " + ex);
			}
		}   	
	}

	public void showUserPreferences(){
		UsPr.showUserPreferences(this);
	}

	public void showVersion(){
		VersionDialog.showVersionDialog(this);
	}

	public void closeAndQuit(){
		int a = JOptionPane.showConfirmDialog(
				this, 
				"Are you sure you want to exit?", "Exit confirmation", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE
				);
		if (a == JOptionPane.YES_OPTION){
			System.exit(0);
		}
	}

	public void saveCockpit(boolean saveAs, boolean cleanSave){
		// TODO opens a dialog asking for filename
		if (saveAs){
			;
		}
		// TODO make cockpit save
		if (cleanSave){
			Cockpit.clearCockpit();
		}
		//regular save
		try{
			Cockpit.save();
			JOptionPane.showMessageDialog(this, "Cockpit saved successfully", "Save successful", JOptionPane.INFORMATION_MESSAGE);
		} 
		catch (Exception e){
			JOptionPane.showMessageDialog(this, "Error saving the pit: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void showManagerProperties(){
		Cockpit.pm.showProperties(this);
	}

	////////////////////////////////
	// MAIN MENU OBJECTS LISTENER //
	////////////////////////////////
	public void createNewObject(){
		NewObjectDialog nod = new NewObjectDialog(this);
		nod.setVisible(true);	
	}

	public void deleteSelectedObjects(){
		//delete current object
		if (JOptionPane.showConfirmDialog(this, "Confirm delete?", "Delete", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
			Cockpit.deleteSelectedObjects();
		}
	}

	public void objDstSrc(){
		Cockpit.adjustSizeFromDestToAllSelectedObjects();
	}

	public void objSrcDst(){
		Cockpit.adjustSizeFromSrcToAllSelectedObjects();
	}

	public void objEqualDsts(){
		Cockpit.equalDstsToAllSelectedObjects();
	}

	public void objEqualSrcs(){
		Cockpit.equalSrcsToAllSelectedObjects();
	}


	public void showButtonList(){
		PitButton.showButtonList(this);
	}

	public void showObjectProperties(){
		// we only show if a single object is selected
		PitPanel p = Cockpit.getCurrentPanel();
		PitObject o = Cockpit.getSelectedObjectAt(0);
		if (o != null){
			o.showProperties(this);
		}
	}

	////////////////////////////////////////
	// MAIN MENU PANEL LISTENER FUNCTIONS //
	////////////////////////////////////////
	public void createNewPanel(){
		int ans = JOptionPane.showConfirmDialog(this, "Create new panel?", "Confirm", JOptionPane.YES_NO_OPTION);
		if (ans == JOptionPane.YES_OPTION){
			//create new panel
			PitPanel newPanel = new PitPanel();
			//properties
			boolean ret = newPanel.showNew(this);
			if (ret){
				Cockpit.addPanel(newPanel);
			}
		}	
	}

	public void deleteCurrentPanel(){
		//delete panel
		if (JOptionPane.showConfirmDialog(this,
					"Youre about to delete an entire panel. Are you sure?",
					"Delete Panel", JOptionPane.OK_CANCEL_OPTION)
				== JOptionPane.OK_OPTION){
			int pid = Cockpit.currentPanel.id;
			if (pid == 1100){
				JOptionPane.showMessageDialog(this, 
						"Cannot delete panel 1100", "Error", 
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Cockpit.deleteCurrentPanel();
				}
	}

	public void autoTilt(){
		if (Cockpit.currentPanel != null){
			Cockpit.currentPanel.autoTilt();
			Cockpit.currentPanel.repaint();
		}
	}

	public void showPanelProperties(){
		if (Cockpit.currentPanel != null){
			Cockpit.currentPanel.showProperties(this);
		}
	}

	public void reloadPanelImage(){
		if (Cockpit.currentPanel != null){
			Cockpit.currentPanel.load();
		}
	}

	/////////////////////////////////
	// MAIN MENU TEMPLATE LISTENER //
	/////////////////////////////////
	public void reloadTemplateImage(){
		if (Cockpit.template != null){
			Cockpit.template.load();
		}
	}

	public void showTemplateProperties(){
		if (Cockpit.template != null){
			Cockpit.template.showProperties(this);
		}	
	}

	//////////////////////////////////
	// COCKPIT OPEN CLOSE FUNCTIONS //
	//////////////////////////////////
	/** closes cockpit panels */
	private void closeCurrentPit(){
		//spPane.setLeftComponent(null);
		//spPane.setRightComponent(null);
		pitScPane.setViewportView(null);
		templateScPane.setViewportView(null);
	}

	/** open cockpit panels */
	private void openCurrentPit(){
		Cockpit.setCurrentPanel(1100);
		//shows the template window
		templateScPane.setViewportView(Cockpit.template);
		//shows the panel window
		pitScPane.setViewportView(Cockpit.currentPanel);
		//spPane.setLeftComponent(Cockpit.currentPanel);
		//spPane.setRightComponent(Cockpit.template);

	}

	/** Sets the current panel on this window.
	 *
	 * @param id the panel we are changing to. If -1 or invalid, does nothing
	 */
	private void setCurrentPanel(int id){
		pitScPane.setViewportView(Cockpit.getCurrentPanel());
	}

	//////////////////////
	// COCKPIT LISTENER //
	//////////////////////
	public void panelChanged(int panelId){
		setCurrentPanel(panelId);
	}
	public void selectedObjectsChanged(){
		// do nothing
	}

	//////////////////
	// KEYLISTERNER //
	//////////////////
	public void keyTyped(KeyEvent k){
	}
	public void keyPressed(KeyEvent k){                
		PitPanel p = Cockpit.currentPanel;
		if (p==null){
			return;
		}
		PitTemplate temp = Cockpit.template;
		int size = Cockpit.getSelectedObjectsSize();

		//auxiliary booleans to help ctrl, shift and alt detection
		boolean ctrlDown = false, shiftDown = false, altDown = false;
		int cmask = KeyEvent.CTRL_DOWN_MASK, 
				smask = KeyEvent.SHIFT_DOWN_MASK, 
				amask = KeyEvent.ALT_DOWN_MASK;

		ctrlDown = (k.getModifiersEx() & cmask) != 0;
		shiftDown = (k.getModifiersEx() & smask) != 0;
		altDown = (k.getModifiersEx() & amask) != 0;

		//auxiliary variable for targets
		ActionTargets targets = new ActionTargets();
		for (int i=0;i<size;i++){
			//the object
			PitObject o = Cockpit.getSelectedObjectAt(i);
			//the areas
			PitArea areas[] = new PitArea[1];
			areas[0] = o.destloc[o.currentSelectedDest];

			//create target
			targets.addObject(o);
		}


		//numpad keys
		if (k.getKeyLocation() == k.KEY_LOCATION_NUMPAD){

			switch (k.getKeyCode()) {
				/* panel walk *************************************/
				case KeyEvent.VK_KP_UP:
				case KeyEvent.VK_NUMPAD8:
					Cockpit.setCurrentPanel(p.adjpanels[PitPanel.UP]);
					break;
				case KeyEvent.VK_KP_LEFT:
				case KeyEvent.VK_NUMPAD4:
					Cockpit.setCurrentPanel(p.adjpanels[PitPanel.LEFT]);
					break;
				case KeyEvent.VK_KP_DOWN:
				case KeyEvent.VK_NUMPAD2:
					Cockpit.setCurrentPanel(p.adjpanels[PitPanel.DOWN]);
					break;
				case KeyEvent.VK_KP_RIGHT:
				case KeyEvent.VK_NUMPAD6:
					Cockpit.setCurrentPanel(p.adjpanels[PitPanel.RIGHT]);
					break;
					//diagonais
				case KeyEvent.VK_HOME:
				case KeyEvent.VK_NUMPAD7:
					Cockpit.setCurrentPanel(p.adjpanels[PitPanel.UPL]);
					break;
					//diagonais
				case KeyEvent.VK_PAGE_UP:
				case KeyEvent.VK_NUMPAD9:
					Cockpit.setCurrentPanel(p.adjpanels[PitPanel.UPR]);
					break;
					//diagonais
				case KeyEvent.VK_PAGE_DOWN:
				case KeyEvent.VK_NUMPAD3:
					Cockpit.setCurrentPanel(p.adjpanels[PitPanel.UPL]);
					break;
					//diagonais
				case KeyEvent.VK_END:
				case KeyEvent.VK_NUMPAD1:
					Cockpit.setCurrentPanel(p.adjpanels[PitPanel.UPL]);
					break;
					// end panel walk

				default: //hack for the numpad + and -
					char keychar = k.getKeyChar();
					switch (keychar){
						case '+':{
											 UsPr.zoomIn();
						}
						break;
						case '-':{
											 UsPr.zoomOut();
						}
						break;
					}
			}
		} //keyboard keys
		else {
			switch (k.getKeyCode()) {

				//zoom stuff
				case KeyEvent.VK_EQUALS: {
																	 UsPr.zoomIn();                   
																	 break;
				}
				case KeyEvent.VK_MINUS: {
																	UsPr.zoomOut();                   
																	break;
				}

				//copy and paste options****************************/
				//NOTE: we only copy objects and buttonviews
				//[shift] ctrl C
				case KeyEvent.VK_C: {
															//get the clipboard
															Toolkit t = Toolkit.getDefaultToolkit();
															Clipboard clipBoard = t.getSystemClipboard();

															//are we copying or checking
															boolean supercopy = false, normalcopy = false;
															if (ctrlDown && shiftDown){
																supercopy = true;
																normalcopy = false;
															}
															else if (ctrlDown){
																supercopy = false;
																normalcopy = true;
															}
															else {
																supercopy = false;
																normalcopy = false;
															}

															if (supercopy) {
																//copy all objects from the window
																PitClipboardVector allPanelElements = 
																	new PitClipboardVector(Cockpit.currentPanel);

																clipBoard.setContents(allPanelElements, this);
																JOptionPane.showMessageDialog(this, "Objects copied successfully");                      
															} else if (normalcopy) {
																if (Cockpit.getSelectedObjectsSize() == 0){
																	JOptionPane.showMessageDialog(this, "No selected object");
																	break;
																}

																//copy selected objjects
																PitClipboardVector allPanelElements = 
																	new PitClipboardVector(Cockpit.getSelectedObjectsAsVector());

																clipBoard.setContents(allPanelElements, this);

																//copiedOb = Cockpit.currentPanel.currentObject;
																JOptionPane.showMessageDialog(this, "Current objects copied successfully");
															} else {
																//check
																JList list = new JList(Cockpit.checkPit());
																if (list.getModel().getSize() == 0){
																	//no error
																	JOptionPane.showMessageDialog(this, "No errors found",
																			"Ok", JOptionPane.OK_OPTION);
																} 
																else {
																	JDialog ed = new JDialog(this, true);
																	ed.setSize(400, 400);
																	ed.setTitle("Errors and Warnings");
																	ed.setLocationRelativeTo(this.getComponent(0));
																	JScrollPane sp = new JScrollPane(list);
																	ed.getContentPane().add(sp);
																	ed.setVisible(true);
																}
															}
															break;
				}
				//ctrl V
				case KeyEvent.VK_V:{
														 //get the clipboard
														 Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();

														 //check ctrl key
														 if (!ctrlDown)
															 return;

														 Transferable buffer = clipBoard.getContents(this);
														 int numObjsAdded = 0;
														 try {
															 Object o = buffer.getTransferData(DataFlavor.stringFlavor);
															 numObjsAdded = Cockpit.readString((String)o);                            
														 }
														 catch (Exception e){
															 numObjsAdded = 0;
														 }
														 JOptionPane.showMessageDialog(this, numObjsAdded + 
																 " object(s) pasted successfully");

														 this.repaint();
														 break;
				}
				/*************************************************/

				case KeyEvent.VK_A:{
														 //shift: select all objects for panel	    
														 if (shiftDown){
															 //select all objects
															 Cockpit.removeAllSelectedObjects();
															 //select all objects and buttonviews
															 Cockpit.selectAllObjectsFromCurrentPanel();
															 p.repaint();
															 Cockpit.template.repaint();      		    
														 } 
														 //toggle alpha SLOW!!!!!!!!!		    
														 else {
															 //alpha
															 UsPr.drawAlpha = !UsPr.drawAlpha;
															 p.repaint();
															 Cockpit.template.repaint();                        
														 }
				}                    
				break;
				//show current obj properties or panel properties
				case KeyEvent.VK_P:{
														 if (shiftDown){
															 //panel properties
															 p.showProperties(this);
														 } else {
															 //we only show properties if theres one, 
															 //and only one object selected
															 if (size == 1){
																 //p.currentObject.showProperties(this);
																 PitObject po = Cockpit.getSelectedObjectAt(0);
																 if (po != null){
																	 po.showProperties(this);
																 }
																 p.repaint();
															 }
														 }
														 break;
				}
				//unselect current object
				case KeyEvent.VK_U:
													 Cockpit.removeAllSelectedObjects();
													 p.repaint();                    
													 break;

													 /* toggle options ***********************************/
													 /* toggle night mode */
				case KeyEvent.VK_N:{
														 UsPr.toggleNightMode();
				}
				break;
				//toggle label
				case KeyEvent.VK_L:
				UsPr.drawLabel = !UsPr.drawLabel;
				p.repaint();
				Cockpit.template.repaint();
				break;
				/* end toggle ************************************/

				/* fine tunning move ****************************/
				case KeyEvent.VK_UP:
				Cockpit.moveSelectedObjects(new Point(0, -1), (activeSide == ACTIVE_PANEL), false);
				break;
				case KeyEvent.VK_RIGHT:
				Cockpit.moveSelectedObjects(new Point(1, 0), (activeSide == ACTIVE_PANEL), false);
				break;
				case KeyEvent.VK_DOWN:
				Cockpit.moveSelectedObjects(new Point(0, 1), (activeSide == ACTIVE_PANEL), false);
				break;
				case KeyEvent.VK_LEFT:
				Cockpit.moveSelectedObjects(new Point(-1, 0), (activeSide == ACTIVE_PANEL), false);		    
				break;
				/***********************************************/


				//add remove stuff
				case KeyEvent.VK_INSERT:{
																	//if shift is pressed, we create a new panel, else we
																	//create a new object
																	if (shiftDown){
																		createNewPanel();
																	} 
																	else {
																		createNewObject();
																	}
				}
				break;
				case KeyEvent.VK_DELETE:{
																	if (shiftDown){
																		deleteCurrentPanel();
																	} 
																	else {
																		deleteSelectedObjects();
																	}
				}
				break;
				/* RWR ***************************************************/
				case KeyEvent.VK_R:{
														 //reload panel
														 if (shiftDown){
															 p.load();
														 }                    
														 else{
															 //rwr
															 if (p.rwr == null) {
																 p.addRWR(new Rectangle(100, 100, 100, 100));
															 }
															 Cockpit.setSelectedObject(p.rwr);
															 Cockpit.centerOnSelectedObject(true, false);
														 }
				}
				break;

				/* HUD ***************************************************/
				case KeyEvent.VK_H:

				//hud
				if (p.hud == null) {
					p.addHud(new Rectangle(100, 100, 100, 100));
				}
				Cockpit.setSelectedObject(p.hud);
				Cockpit.centerOnSelectedObject(true, false);

				break;                   
				/* MFDs **************************************************/
				case KeyEvent.VK_1:
				//left MFD
				if (p.mfds[0] == null) {
					p.addMFD(new Rectangle(100, 100, 100, 100), 0, true);
				}
				Cockpit.setSelectedObject(p.mfds[0]);
				Cockpit.centerOnSelectedObject(true, false);		    
				break;
				case KeyEvent.VK_2:
				//right MFD
				if (p.mfds[1] == null) {
					p.addMFD(new Rectangle(100, 100, 100, 100), 1, true);
				}
				Cockpit.setSelectedObject(p.mfds[1]);
				Cockpit.centerOnSelectedObject(true, false);		    

				break;
				case KeyEvent.VK_3:
				//third MFD
				if (p.mfds[2] == null) {
					p.addMFD(new Rectangle(100, 100, 100, 100), 2, true);
				}
				Cockpit.setSelectedObject(p.mfds[2]);
				Cockpit.centerOnSelectedObject(true, false);		    

				break;
				case KeyEvent.VK_4:
				//fourth MFD
				if (p.mfds[3] == null) {
					p.addMFD(new Rectangle(100, 100, 100, 100), 3, true);
				}
				Cockpit.setSelectedObject(p.mfds[3]);
				Cockpit.centerOnSelectedObject(true, false);		    

				break;
				/* Save ************************/
				/* there are two types, the clean and the regular. Clean cleans
				 * the file, and is accessed by shift S
				 */
				case KeyEvent.VK_S:
				{
					if (shiftDown){
						//clean
						if (JOptionPane.showConfirmDialog(this,
									"Clear file before saving?",
									"Clear", JOptionPane.OK_CANCEL_OPTION)
								== JOptionPane.OK_OPTION){
							Cockpit.clearCockpit();
								}                        
					}
					//regular save
					try{
						if (JOptionPane.showConfirmDialog(this,
									"Confirm save?",
									"Save", JOptionPane.OK_CANCEL_OPTION)
								== JOptionPane.OK_OPTION){

							Cockpit.save();
							JOptionPane.showMessageDialog(this, "Cockpit saved successfully" ,
									"Save successful", JOptionPane.INFORMATION_MESSAGE);
								}
					} catch (Exception e){
						JOptionPane.showMessageDialog(this, "Error saving the pit: " + e.getMessage() ,
								"Error", JOptionPane.ERROR_MESSAGE);
					}

					break;
				}
				/***************************************************/

				/* Show panel object list */
				case KeyEvent.VK_O:
				{
					p.showObjectsList(this);

				}
				break;
				/* show button list(B) or send to bg (shift B) */
				case KeyEvent.VK_B:
				{
					if (shiftDown){
						Cockpit.sendSelectedToBg();
						JOptionPane.showMessageDialog(this, "Object(s) moved to background");                        
					}
					else {
						PitButton.showButtonList(this);
					}
				}
				break;
				case KeyEvent.VK_F:
				{
					if (shiftDown){
						Cockpit.sendSelectedToFg();
						JOptionPane.showMessageDialog(this, "Object(s) moved to foreground");
					}                    
				}                
				break;
				//make srclocs = destlocs
				case KeyEvent.VK_E:
				{
					Cockpit.adjustSizeFromDestToAllSelectedObjects();
				}                
				break;
				//change current object sTate
				case KeyEvent.VK_T:
				{
					//returns all panel objects to original state                    
					if (ctrlDown && shiftDown){
						PitObject po;
						int vSize;
						//first all objects
						vSize = Cockpit.currentPanel.objectsVector.size();
						for (int i=0;i<vSize;i++){
							po = (PitObject)Cockpit.currentPanel.objectsVector.get(i);
							po.resetState();
						}

						//now all buttonviews
						vSize = Cockpit.currentPanel.buttonviewsVector.size();
						for (int i=0;i<vSize;i++){
							po = (PitObject)Cockpit.currentPanel.buttonviewsVector.get(i);                            
							po.resetState();
						}

					}
					//else we only apply on selected objects
					else {                        
						for (int i=0;i<size;i++){
							PitObject po = Cockpit.getSelectedObjectAt(i);
							if (ctrlDown){
								po.resetState();
							}
							else if (shiftDown){
								po.previousState();
							}
							else {
								po.nextState();
							}
						}
					}

					//now we refresh
					Cockpit.currentPanel.repaint();
					Cockpit.template.repaint();                    
				}
				break;
				//auto tilt
				case KeyEvent.VK_I:
				{
					if (shiftDown){
						Cockpit.currentPanel.autoTilt();
						Cockpit.currentPanel.repaint();
					}
				}
				break;
				/*case KeyEvent.VK_8:
					{
					UsPr.vFov -= 0.2;
					System.out.println("fov: " + UsPr.vFov);
					Cockpit.currentPanel.computeTilt();
					Cockpit.currentPanel.repaint();
					}                
					break;
					case KeyEvent.VK_9:
					{
					UsPr.vFov += 0.2;
					System.out.println("fov: " + UsPr.vFov);
					Cockpit.currentPanel.computeTilt();
					Cockpit.currentPanel.repaint();                    
					}                
					break; */

				/** undo redo actions */
				case KeyEvent.VK_Z:
				{
					//undo
					if (ctrlDown){
						try{
							Cockpit.undoAction();
						}
						catch (Exception e){
							System.out.println(e.getMessage());
						}
					}
					Cockpit.currentPanel.repaint();
					Cockpit.template.repaint();                    
				}
				break;
				case KeyEvent.VK_Y:
				{
					//undo
					if (ctrlDown){
						try{
							Cockpit.redoAction();
						}
						catch (Exception e){
							System.out.println(e.getMessage());
						}
					}
					Cockpit.currentPanel.repaint();
					Cockpit.template.repaint();                    
				}
				break;

			}
		}
	}
	public void keyReleased(KeyEvent k){
	}

	/////////////////////
	// CLIPBOARD OWNER //
	/////////////////////
	public void lostOwnership(Clipboard clipboard, Transferable contents){
		//do nothing
	}         

	public void autoPanTilt() {
	}
}

/** the bottom bar of the screen */
class BottomBar extends JPanel implements CockpitListener, UsPrListener {
	// graphic components
	/** text at bottom bar */    
	private JLabel bbText = new JLabel("", JLabel.CENTER);
	/** mouse position in the coodinates */
	private JLabel bbMousePosition = new JLabel("", JLabel.CENTER);
	/** coordinates of selected objects */
	private JLabel bbSelectedInfo = new JLabel("", JLabel.CENTER);
	/** zoom */
	private JLabel bbZoom = new JLabel("zoom: 1.0", JLabel.CENTER);

	// internal state variables, used to build labels
	/** zoom value */
	private double zoom = 1.0;
	/** mouse position */
	private Point mousePosition = new Point();
	/** panel id, -1 if none */
	private int panelId = -1;
	/** selected object coordinate (if single) */
	private Rectangle selectedRect = null;
	/** selected object type */
	private String selectedShortDesc = "";


	/** constructor: builds a new bottom bar with no information */
	public BottomBar() {
		this.setMinimumSize(new Dimension(1, 35));
		// first row: panel
		// second row: coordinates, zoom and mouse
		setLayout(new GridLayout(2,1)); 

		JPanel pn = new JPanel(new GridLayout(1, 3));
		pn.add(bbSelectedInfo);
		pn.add(bbMousePosition);
		pn.add(bbZoom);

		this.add(bbText);
		this.add(pn);
		this.setOpaque(false);
	}

	/** sets the mouse position label in the bottom bar. */
	public void setMousePosition(Point p){
		mousePosition.setLocation(p);
		bbMousePosition.setText("coordinates: <" + p.x + "," + p.y + ">");
		repaint();
	}

	/** sets zoom */
	private void setZoom(double z){
		zoom = z;
		bbZoom.setText("zoom: " + z);
	}

	/** Sets panel id in bottom bar */
	private void setPanelId(int id){
		panelId = id;
		if (panelId != -1){
			bbText.setText("Panel: " + panelId);
		}
		else {
			bbText.setText("No panel");
		}
	}

	/** sets selected object information if obj type is not null */
	public void setSelectedInfo(String shortDesc, Rectangle objCoords){
		if (shortDesc != null){
			selectedShortDesc = shortDesc;
			// we set if type is not null
			if (objCoords != null){
				if (selectedRect == null){
					selectedRect = new Rectangle(objCoords);
				}
				else {
					selectedRect.setBounds(objCoords.x, objCoords.y, objCoords.width, objCoords.height);		
				}
				bbSelectedInfo.setText(
					selectedShortDesc + " @ " + 
					"<" + selectedRect.x + " " + selectedRect.y + ", " + 
					(selectedRect.x + selectedRect.width - 1) + " " + (selectedRect.y + selectedRect.height - 1) +  ">"
				);
			}
			else {
				bbSelectedInfo.setText(selectedShortDesc);
			}
		}
		else {
			// erases all info from selected object
			selectedRect = null;
			selectedShortDesc = null;
			bbSelectedInfo.setText("");	    
		}
		repaint();
	}

	////////////////////////////////
	// COCKPIT LISTENER INTERFACE //
	////////////////////////////////
	public void selectedObjectsChanged(){
		if (Cockpit.getSelectedObjectsSize() != 1){
			setSelectedInfo(null, null);
		}
		else {
			String objType = null;
			PitObject o = Cockpit.getSelectedObjectAt(0);	
			setSelectedInfo(o.getShortDescription(), o.destloc == null ? null : o.destloc[o.currentSelectedDest].rectangle);
		}	
	}

	public void panelChanged(int id){
		setPanelId(id);
		repaint();
	}

	public void flushingCockpit(){
		setPanelId(-1);
		setSelectedInfo(null, null);
		repaint();
	}

	/////////////////////////////
	// USPR LISTENER INTERFACE //
	/////////////////////////////
	public void zoomChanged(double zoom){
		setZoom(zoom);
		repaint();
	}  
}

class CockpitFilter extends javax.swing.filechooser.FileFilter {

	public CockpitFilter() {
	}

	public boolean accept(File f) {
		return (f.getName().toLowerCase().endsWith(".dat") || f.isDirectory() );
	}
	public String getDescription(){
		return("Cockpit Data File");
	}

}

