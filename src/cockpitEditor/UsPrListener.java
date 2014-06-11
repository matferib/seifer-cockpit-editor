/*
 * UsPrListener.java
 *
 * Created on 12 de Outubro de 2006, 22:05
 *
 * Implementation of UsPr listener
 */

package cockpitEditor;

/**
 * Everyone who wants to be notified of changes in user preferences must
 * implement this
 *
 * @author matheus
 */
public interface UsPrListener {
    /** notifies zoom has been changed */
    public void zoomChanged(double zoom);
}
