/*
 * MainMenuPanelListener.java
 *
 * Created on 14 de Outubro de 2006, 09:24
 */

package menus;

/**
 * All classes interested in MainMenu Panel entry must implement this
 * @author matheus
 */
public interface MainMenuPanelListener {
    public void createNewPanel();
    public void deleteCurrentPanel();
    public void showPanelProperties();
    public void reloadPanelImage();
    public void autoTilt();
}
