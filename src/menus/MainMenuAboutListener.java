/*
 * MainMenuAboutListener.java
 *
 * Created on 14 de Outubro de 2006, 10:17
 *
 */

package menus;

/**
 * Any class which wants to be notified of main menu about events should
 * implement this
 * @author matheus
 */
public interface MainMenuAboutListener {
    public void showVersion();
    public void showUserPreferences();
}
