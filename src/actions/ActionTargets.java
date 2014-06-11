/*
 * ActionTarget.java
 *
 * Created on 21 de Setembro de 2005, 11:25
 */

package actions;

import pitComponents.pitHelperComponents.PitArea;
import pitComponents.pitObjects.PitObject;
import java.util.Vector;

/**
 * Every action has targets. Targets are the objects which were affected by the action.
 * For example, if you move an ADI and a buttonview, theyre both targets.
 *
 * This class encompasses the objects, the panel id and the areas for each object
 *
 * @author  matheus
 */
public class ActionTargets {
    
    /** objects upon which action was taken */
    public Vector objects;
    
    /** Creates a new instance of ActionTarget */
    public ActionTargets() {
	objects = new Vector();
	/*
	srcAreas = new PitArea[o.srcloc.length];
	destAreas = new PitArea[o.destloc.length];
	//copy areas
	for (int i=0;i<o.srcloc.length;++i){
	    srcAreas[i] = PitArea.clone(o.srcloc[i]);
	}
	for (int i=0;i<o.destloc.length;++i){
	    destAreas[i] = PitArea.clone(o.destloc[i]);
	}
	*/	
    }
    
    /** adds an object to this action target */
    public void addObject(PitObject o){
	objects.add((Object)o);
    }
    
    /** removes an object from targets */
    public void removeObject(PitObject o){
	objects.remove((Object)o);
    }
    
    
    
    
    
}

/** each object moved has areas target. For example, you can move only srcloc[0]
 * or destloc[2].
 **/
/*class AreasTarget {
    public PitArea areas[];
    
    public AreasTarget(PitArea areas[]){
        this.areas = areas;
    }
}*/