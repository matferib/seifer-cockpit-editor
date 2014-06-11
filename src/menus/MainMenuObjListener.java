/*
 * MainMenuObjListener.java
 *
 * Created on 14 de Outubro de 2006, 10:29
 *
 */

package menus;

/**
 * Any class interested in main menu object events should implement this
 * interface
 * @author matheus
 */
public interface MainMenuObjListener {
	/** object -> new selected */
	public void createNewObject();
	
	/** object -> delete selected */
	public void deleteSelectedObjects();
	
	/** object -> properties selected */
	public void showObjectProperties();
	
	/** object -> button list selected */
	public void showButtonList();
	
	/** obj    -> size dst->src */
	public void objDstSrc();
	
	/** obj    -> size src->dst */
	public void objSrcDst();
	
	/** obj    -> equal dests */
	public void objEqualDsts();
	
	/** obj    -> equal srcs */
	public void objEqualSrcs();
}
