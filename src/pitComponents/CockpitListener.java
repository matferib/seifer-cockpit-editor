/*
 * CockpitListener.java
 *
 * Created on 12 de Outubro de 2006, 21:41
 *
 * Implementation of CockpitListener
 */

package pitComponents;

/**
 * Any class which wants to be notified of Cockpit events should implement this
 * interface.
 *
 * @author matheus
 */
public interface CockpitListener {
    /** panel has changed */
    void panelChanged(int panelId);
    /** called when selected objects change or are changed (moved/resized) */
    public void selectedObjectsChanged();    
}
