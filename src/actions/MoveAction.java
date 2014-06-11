/*
 * MoveAction.java
 *
 * Created on 21 de Setembro de 2005, 11:18
 */

package actions;

import java.awt.Point;

import pitComponents.Cockpit;
import pitComponents.pitHelperComponents.PitArea;
import pitComponents.pitObjects.PitObject;

/**
 * Represents a move action. Any object can be moved around, triggering a move
 * action. The move can be of a single area, or multiple areas, or even to multiple
 * objects!
 *
 * Move is defined as a vector, the move offset.
 *
 * @author  matheus
 */
public class MoveAction implements ActionInterface{

    /** move types */
    public static final int AllPanel=0, SingPanel=1, AllTemplate=2, SingTemplate=3; 
    /** type of movement made */
    private static int moveType;
    
    /** move offset. Beware: not always the move moves this ammount */
    int xoff, yoff;
    
    /** the objects moved */
    private ActionTargets targets;
    /** targets areas before move */
    private ObjAreas bAreas[];
    /** targets areas after move */
    private ObjAreas aAreas[];
    
    /** Creates a new instance of MoveAction */
    public MoveAction(ActionTargets targets, int moveType){
        this.targets = targets;
	xoff = yoff = 0;
	this.moveType = moveType;
    }
    
    /** Creates a move action with a given offset */
    public MoveAction(ActionTargets targets, int moveType, int xoff, int yoff){
	this.targets = targets;
	this.xoff = xoff;
	this.yoff = yoff;
	this.moveType = moveType;
    }
    
    /** beginning of a move action. Save src for all */
    public void startMove(){
	bAreas = new ObjAreas[targets.objects.size()];
	for (int i=0;i<targets.objects.size();++i){
	    PitObject o = (PitObject)targets.objects.get(i);
	    bAreas[i] = new ObjAreas(o);
	}
    }
    
    /** ends a move action */
    public void endMove(){
	aAreas = new ObjAreas[targets.objects.size()];
	for (int i=0;i<targets.objects.size();++i){
	    PitObject o = (PitObject)targets.objects.get(i);
	    aAreas[i] = new ObjAreas(o);
	}	
    }
    
    /** execute action */
    public void execute() throws CantExecuteException {
	for (int i=0;i<targets.objects.size();++i){
	    PitObject o = (PitObject)targets.objects.get(i);
	    switch (moveType){
		case AllPanel:
		    o.moveAll(xoff, yoff);
		break;
		case SingPanel:
		    o.move(xoff, yoff);
		break;
		case AllTemplate:
		    o.moveInTemplate(xoff, yoff);
		break;
		case SingTemplate:
		    o.moveAllInTemplate(xoff, yoff);
		break;
	    }
	}
    }
    
    /** undo move */
    public void undo() throws CantUndoException {
	for (int i=0;i<targets.objects.size();++i){
	    PitObject o = (PitObject)targets.objects.get(i);
	    // restore srclocs
	    for (int j=0;j<bAreas[i].srcs.length;++j){
		o.srcloc[j] = PitArea.clone(bAreas[i].srcs[j]);
	    }
	    // restore destlocs
	    for (int j=0;j<bAreas[i].dests.length;++j){
		o.destloc[j] = PitArea.clone(bAreas[i].dests[j]);
	    }	    	    
	}		
    }
    
    /** redo move */
    public void redo() throws CantRedoException {
	for (int i=0;i<targets.objects.size();++i){
	    PitObject o = (PitObject)targets.objects.get(i);
	    // redo srclocs
	    for (int j=0;j<aAreas[i].srcs.length;++j){
		o.srcloc[j] = PitArea.clone(aAreas[i].srcs[j]);
	    }
	    // redo destlocs
	    for (int j=0;j<aAreas[i].dests.length;++j){
		o.destloc[j] = PitArea.clone(aAreas[i].dests[j]);
	    }	    	    
	}			
    }
    
}

/** used for saving object areas */
class ObjAreas{
    PitObject o;
    PitArea srcs[];
    PitArea dests[];
    
    ObjAreas(PitObject o){
	this.o = o;
	srcs = new PitArea[o.srcloc.length];
	dests = new PitArea[o.destloc.length];
	for (int i=0;i<o.srcloc.length;++i){
	    srcs[i] = PitArea.clone(o.srcloc[i]);
	}
	for (int i=0;i<o.destloc.length;++i){
	    dests[i] = PitArea.clone(o.destloc[i]);
	}
    }
}












