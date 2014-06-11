/*
 * MainMenu.java
 *
 * Created on 12 de Outubro de 2006, 10:12
 *
 * Main menu for cockpit editor
 * Has functions to disable and enable stuff
 */

package menus;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import pitComponents.Cockpit;
import pitComponents.CockpitListener;
import pitComponents.CockpitLoaderListener;

/**
 * MainMenu class
 * @author matheus
 */
public class MainMenu extends JMenuBar implements ActionListener, CockpitLoaderListener, CockpitListener {
    
	// listeners for this menu
	/** listeners for cockpit menu */
	private Vector<MainMenuCockpitListener> cockpitListeners = new Vector<MainMenuCockpitListener>();
	
	/** listeners for panel menu */
	private Vector<MainMenuPanelListener> panelListeners = new Vector<MainMenuPanelListener>();
	
	/** listeners for object menu */
	private Vector<MainMenuObjListener> objListeners = new Vector<MainMenuObjListener>();
	
	/** listeners for template menu */
	private Vector<MainMenuTemplateListener> templateListeners = new Vector<MainMenuTemplateListener>();
	
	/** listeners for about menu */
	private Vector<MainMenuAboutListener> aboutListeners = new Vector<MainMenuAboutListener>();
    
	/** constants to identify menus event changes */
	private static final int 
		COCKPIT_NOT_LOADED = 0,         ///< cockpit has not been loaded
		COCKPIT_LOADED = 1,             ///< cockpit has been loaded
		PANEL_UNLOADED = 2,             ///< panel is not loaded
		PANEL_LOADED = 3,               ///< panel is loaded
		SELECTED_OBJECTS_EMPTY = 4,     ///< selected objects is empty
		SELECTED_OBJECTS_EXIST =5;      ///< there are selected objects
	 
    
	/** handlers to menus WARNING: finals must always match string positions */
	public static final int COCKPIT_M=0, PANEL_M=1, OBJECTS_M=2, TEMPLATE_M=3, ABOUT_M=4;
	
	/** strings for each menu */
	private String menuStrs[] = {
		"Cockpit", "Panel", "Object", "Template", "About"
	};    
	
	/** handlers to menuitems WARNING: must always match string positions */
	public static final int[] 
		NEW_MI ={0,0}, OPENAR_MI={0,1}, OPEN_MI={0,2}, SAVE_MI={0,3}, SAVECL_MI={0,4}, SAVEAS_MI={0,5}, /*null,*/ MAN_MI={0,7}, /*null,*/ EXIT_MI={0,9},
		PANNEW_MI={1,0}, PANDEL_MI={1,1}, /*null,*/PANAUTOTILT_MI={1,3}, PANRELOAD_MI={1,4}, /* null,*/ PANLIST_MI={1,6}, /*null,*/PANPROP_MI={1,8},
		OBJNEW_MI={2,0}, OBJDEL_MI={2,1}, /* null,*/OBJED_MI={2,3}, OBJES_MI={2,4}, OBJDS_MI={2,5}, OBJSD_MI={2,6}, /*null,*/OBJBLIST_MI={2,8}, /*null,*/OBJPROP_MI={2,10},
		RELOADT_MI={3,0}, /*null,*/TEMPPROP_MI={3,2},  
		USPR_MI={4,0}, CED_MI={4,1}
	;
	
	/** strings for menu items, null represents a dash */
	private String menuItemStrs[][] = {
		// cockpit menu
		{"New", "Open (auto res)", "Open", "Save", "Save (clean)", "Save As", null, "Manager", null, "Exit"},
		// panel menu
		{"New", "Delete Current", null, "Auto Tilt", "Reload Image", null, "List", null, "Properties"},
		// object menu
		{"New", "Delete", null, "Size: equal dests", "Size: equal srcs","Size: dst->src", "Size: src->dst", null, "Button List", null, "Properties"},
		// template menu
		{"Reload Image", null, "Properties"},
		// about menu
		{"User Preferences", "Cockpit Editor"}
	};
	
	/** menus in menu bar */
	private JMenu menus[] = new JMenu[menuStrs.length];
	
	/** menu items in menus */
	private JMenuItem menuItems[][] = new JMenuItem[menuStrs.length][];
        
	/** Creates a new instance of MainMenu */
	public MainMenu() {
		// creates menu and menuitems
		for (int i=0;i<menuStrs.length;++i){
			// builds menus
			menus[i] = new JMenu(menuStrs[i]);
			this.add(menus[i]);
			// builds items
			menuItems[i] = new JMenuItem[menuItemStrs[i].length];
			for (int j=0;j<menuItemStrs[i].length;++j){
				// if menuitem is null, use dash
				if (menuItemStrs[i][j] == null){
					menuItems[i][j] = null;
					menus[i].addSeparator();
				}
				else {
					menuItems[i][j] = new JMenuItem(menuItemStrs[i][j]);
					menuItems[i][j].addActionListener(this);
					menus[i].add(menuItems[i][j]);		    
				}
			}
			menus[i].setEnabled(false);
		}
		// we want to be notifed of loading events
		Cockpit.addCockpitLoaderListener(this);
		Cockpit.addCockpitListener(this);

		setupMenus(COCKPIT_NOT_LOADED);
    }
    
	/** sets up all menu enabled / disabled states */
	private void setupMenus(int cpState){
		switch (cpState){
			case COCKPIT_NOT_LOADED:
				// disable all
				for (int i=0;i<menus.length;++i){
					menus[i].setEnabled(false);		    
				}

				menus[COCKPIT_M].setEnabled(true);
				setEnabledMenuItemsOfMenu(COCKPIT_M, false);
				// enable open, exit, new
				findMenuItem(OPENAR_MI).setEnabled(true);
				findMenuItem(OPEN_MI).setEnabled(true);
				findMenuItem(NEW_MI).setEnabled(true);
				findMenuItem(EXIT_MI).setEnabled(true);		
			break;
			case COCKPIT_LOADED:
				// enable all
				for (int i=0;i<menus.length;++i){
					menus[i].setEnabled(true);		    
				}		
				setEnabledMenuItemsOfMenu(COCKPIT_M, true);
				findMenuItem(OBJBLIST_MI).setEnabled(true);
			break;
			case PANEL_UNLOADED:
				// disable panel
				setEnabledMenuItemsOfMenu(PANEL_M, false);
			break;    
			case PANEL_LOADED:
				// load panel		
				setEnabledMenuItemsOfMenu(PANEL_M, true);
			break;
			case SELECTED_OBJECTS_EMPTY:
				// disable all menuitems from Object menu but new
				setEnabledMenuItemsOfMenu(OBJECTS_M, false);
				findMenuItem(OBJNEW_MI).setEnabled(true);
				findMenuItem(OBJBLIST_MI).setEnabled(true);
				menus[OBJECTS_M].setEnabled(true);
			break;
			case SELECTED_OBJECTS_EXIST:
				setEnabledMenuItemsOfMenu(OBJECTS_M, true);
				findMenu(OBJECTS_M).setEnabled(true);
			break;
		}
		findMenu(ABOUT_M).setEnabled(true);
		// saveas always disabled
		findMenuItem(SAVEAS_MI).setEnabled(false);
		findMenuItem(PANLIST_MI).setEnabled(false);
    }

    /** finds a menu item */
    private JMenuItem findMenuItem(int idx[]){
		if (
			(idx.length != 2) || 
			(idx[0] < 0) || (idx[0] >= menuItems.length) || 
			(idx[1] < 0) || (idx[1] >= menuItems[idx[0]].length)
		){
			return null;
		}
		return menuItems[idx[0]][idx[1]];	
    }

	/** finds a menu */
	private JMenu findMenu(int idx){
		if ((idx < 0) || (idx >= menus.length)){
			return null;
		}
		return menus[idx];	
	}
    
	/** enable/disable all menuitems of a menu */
	public void setEnabledMenuItemsOfMenu(int idx, boolean enabled){
		for (int i=0;i<menuItems[idx].length;++i){
			if (menuItems[idx][i] != null){
				menuItems[idx][i].setEnabled(enabled);
			}
		}	    
	}

    
	/////////////////////////
	// MAIN MENU LISTENERS //
	/////////////////////////
	/** adds a listener to this menu 
	* @param type one of COCKPIT_M, PANEL_M, OBJECTS_M, TEMPLATE_M, ABOUT_M
	* @param l listener object
	*/
	public void addMainMenuListener(int type, Object l){
		switch (type){
			case COCKPIT_M:
				cockpitListeners.add((MainMenuCockpitListener)l);
			break;
			case PANEL_M:
				panelListeners.add((MainMenuPanelListener)l);
			break;
			case OBJECTS_M:
				objListeners.add((MainMenuObjListener)l);
			break;
			case TEMPLATE_M:
				templateListeners.add((MainMenuTemplateListener)l);
			break;
			case ABOUT_M:
				aboutListeners.add((MainMenuAboutListener)l);
			break;
		}
	}
	
	/** removes a listener from this menu 
	* @param type one of COCKPIT_M, PANEL_M, OBJECTS_M, TEMPLATE_M, ABOUT_M
	* @param l listener object
	*/
	public void removeMainMenuListener(int type, Object l){
		switch (type){
			case COCKPIT_M:
				cockpitListeners.remove((MainMenuCockpitListener)l);
			break;
			case PANEL_M:
				panelListeners.remove((MainMenuPanelListener)l);
			break;
			case OBJECTS_M:
				objListeners.remove((MainMenuObjListener)l);
			break;
			case TEMPLATE_M:
				templateListeners.remove((MainMenuTemplateListener)l);
			break;
			case ABOUT_M:
				aboutListeners.remove((MainMenuAboutListener)l);
			break;
		}
	}
    
	/** remove all listeners */
	public void removeAllListeners(){
		cockpitListeners.clear();
		panelListeners.clear();
		objListeners.clear();
		templateListeners.clear();
		aboutListeners.clear();	
	}
    
    
	///////////////////////
	// ACTION LISTENENER //
	///////////////////////
	public void actionPerformed(ActionEvent e){
		Object o = e.getSource();
		if (o instanceof JMenuItem){	    
			JMenuItem mi = (JMenuItem)o;
			// cockpit menu //
			// ------------ //
			// new
			if (mi == findMenuItem(NEW_MI)){
				// new
				for (MainMenuCockpitListener l : cockpitListeners){
					l.newCockpit();
				}
			}
			// open and open auto res
			else if ((mi == findMenuItem(OPEN_MI)) || (mi == findMenuItem(OPENAR_MI))){
				// open
				for (MainMenuCockpitListener l : cockpitListeners){
					l.openCockpit(mi == findMenuItem(OPENAR_MI));
				}		
			}
			// save, save clean and save as
			else if ((mi == findMenuItem(SAVE_MI)) || (mi == findMenuItem(SAVEAS_MI)) || (mi == findMenuItem(SAVECL_MI))){
				for (MainMenuCockpitListener l : cockpitListeners){
					l.saveCockpit(mi == findMenuItem(SAVEAS_MI), mi == findMenuItem(SAVECL_MI));
				}				
			}
			else if (mi == findMenuItem(MAN_MI)){
				for (MainMenuCockpitListener l : cockpitListeners){
					l.showManagerProperties();
				}								
			}
			// exit
			else if (mi == findMenuItem(EXIT_MI)){
				for (MainMenuCockpitListener l : cockpitListeners){
					l.closeAndQuit();
				}				
			}	    
			// panel menu //
			// ---------- //
			//{"new", "delete", "auto tilt", "reload image", "Properties"},
			else if (mi == findMenuItem(PANNEW_MI)){
				for (MainMenuPanelListener l : panelListeners){
					l.createNewPanel();
				}						
			}
			else if (mi == findMenuItem(PANDEL_MI)){
				for (MainMenuPanelListener l : panelListeners){
					l.deleteCurrentPanel();
				}						
			}
			else if (mi == findMenuItem(PANAUTOTILT_MI)){
				for (MainMenuPanelListener l : panelListeners){
					l.autoTilt();
				}						
			}	    
			else if (mi == findMenuItem(PANRELOAD_MI)){
				for (MainMenuPanelListener l : panelListeners){
					l.reloadPanelImage();
				}						
			}
			else if (mi == findMenuItem(PANPROP_MI)){
				for (MainMenuPanelListener l : panelListeners){
					l.showPanelProperties();
				}								
			}
			// object menu //
			// ----------- //
			else if (mi == findMenuItem(OBJNEW_MI)){
				for (MainMenuObjListener l : objListeners){
					l.createNewObject();
				}								
			}
			else if (mi == findMenuItem(OBJDEL_MI)){
				for (MainMenuObjListener l : objListeners){
					l.deleteSelectedObjects();
				}								
			}
			else if (mi == findMenuItem(OBJBLIST_MI)){
				for (MainMenuObjListener l : objListeners){
					l.showButtonList();
				}										
			}
			else if (mi == findMenuItem(OBJDS_MI)){
				for (MainMenuObjListener l : objListeners){
					l.objDstSrc();
				}
			}
			else if (mi == findMenuItem(OBJSD_MI)){
				for (MainMenuObjListener l : objListeners){
					l.objSrcDst();
				}
			}	    
			else if (mi == findMenuItem(OBJED_MI)){
				for (MainMenuObjListener l : objListeners){
					l.objEqualDsts();
				}
			}	    
			else if (mi == findMenuItem(OBJES_MI)){
				for (MainMenuObjListener l : objListeners){
					l.objEqualSrcs();
				}
			}	    	    
			else if (mi == findMenuItem(OBJPROP_MI)){
				for (MainMenuObjListener l : objListeners){
					l.showObjectProperties();
				}								
			}	    	    
			// template menu //
			// ------------- //
			//{"Reload Image", "Properties"},
			else if (mi == findMenuItem(RELOADT_MI)){
				for (MainMenuTemplateListener l : templateListeners){
					l.reloadTemplateImage();
				}								
			}	    
			else if (mi == findMenuItem(TEMPPROP_MI)){
				for (MainMenuTemplateListener l : templateListeners){
					l.showTemplateProperties();
				}								
			}	    
			// about menu //
			// ---------- //	    
			// version
			else if (mi == findMenuItem(CED_MI)){
				for (MainMenuAboutListener l : aboutListeners){
					l.showVersion();
				}		
			}
			// user preferences
			else if (mi == findMenuItem(USPR_MI)){
				for (MainMenuAboutListener l : aboutListeners){
					l.showUserPreferences();
				}		
			}	    
			// other (for test) //
			// ---------------- //
			else {
				System.out.println(mi.getText());
			}
		}
	}
    
	//////////////////////////////
	// COCKPIT LOADER FUNCTIONS //
	//////////////////////////////
	public void cockpitLoaded(){
		setupMenus(COCKPIT_LOADED);
	}
	
	public void cockpitUnloaded(){
		setupMenus(COCKPIT_NOT_LOADED);
	}
    
	/////////////////////////////
	// COCKPIT LISTERNER STUFF //
	/////////////////////////////
	public void panelChanged(int panelId){
		setupMenus((panelId == -1) ? PANEL_UNLOADED : PANEL_LOADED);
	}

	public void selectedObjectsChanged(){
		setupMenus((Cockpit.getSelectedObjectsSize() == 0) ? SELECTED_OBJECTS_EMPTY : SELECTED_OBJECTS_EXIST);
	}
}
