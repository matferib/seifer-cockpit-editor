/*
 * CockpitLoaderListener.java
 *
 * Created on 19 de Outubro de 2006, 09:31
 *
 * Cockpit loader interface
 */

package pitComponents;

/**
 * Classes interested in cockpit loading event should implement this interface
 * @author matheus
 */
public interface CockpitLoaderListener {
    /** called after cockpit is successfully loaded */
    public void cockpitLoaded();
    /** called before unloading a cockpit */
    public void cockpitUnloaded();
}
