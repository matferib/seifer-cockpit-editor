/*
 * MainMenuListener.java
 *
 * Created on 12 de Outubro de 2006, 16:04
 *
 * Interface for Main Menu
 */

package menus;

/**
 * Any class using which wants to be notified of main menu cockpit events
 * should implement this
 * @author matheus
 */
public interface MainMenuCockpitListener {
	
	/** new cockpit was called */
	public void newCockpit();
	
	/** open cockpit was called
	* @param autoRes use autoresolution 
	*/
	public void openCockpit(boolean autoRes);

	/** saves a cockpit was called
	* @param saveAs indicates save as was chosen instead of regular save
	* @param cleanSave clear cockpit before saving
	*/
	public void saveCockpit(boolean saveAs, boolean cleanSave);
	
	/** shows cockpit manager properties. */
	public void showManagerProperties();

	/** close and quit was called */
	public void closeAndQuit();
}
