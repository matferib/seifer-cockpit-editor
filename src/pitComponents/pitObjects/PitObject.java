package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import java.awt.*;
import javax.swing.*;
import windows.PropertiesDialog;

/**
 * Object for the cockpit. Encompasses every pit objects
 * which includes: mashASI, ADI, HSI, chevron, liftline, dial
 * digits, indicator, light, text, pitButtonView
 */
abstract public class PitObject implements Cloneable {
	/** this is the line separator, used by objects */
	public static final String ls = System.getProperty("line.separator");
	public static final int MAX = 20; //max number of srcs and dests an obj can have
	public String label;
	public PitComment comments;
	public int id;
	public int callbackslot;
	public long cyclebits;
	public PitColor color[]; 
	/** Objects can have srclocs and destlocs, or both(even none!). The srcloc
	 * and destloc array hold those values. But even though an object can have, say
	 * 5 srclocs, it doesnt mean it all are valid. So usually, you check srcloc==null
	 * and srcloc[desired] == null.
	 */
	public PitArea srcloc[], destloc[];
	/** If the object doees not have state, this is set to -1, otherwise,
	 * indicates the number of states
	 */
	public int states = -1;
	public int currentState = -1; ///-1 indicates no states, used by bv and lights
	public int currentSelectedDest=0; ///which destloc are we operating on?
	public int currentSelectedSrc=0; ///which src?
	public int persistant = 0; ///are we?
	
	//should we draw the rectangle around the object?
	static public boolean drawRectangle = true;

	/* Constructors ******************************/
	protected PitObject(int id) {
		this.id = id;
	}

	protected PitObject(int id, int numSrc, int numDst) {
		this.id = id;
		setNumSrcs(numSrc);
		setNumDsts(numDst);
	}
	/* end constructors *******************************/

	/* number of srcs and dests ************************/
	/** Changes the number of sources an object has. After this call, all 
	 * current sources are not valid anymore, as the srcloc array is reallocated.
	 * If numSrc == 0, srcloc is null.
	 *
	 * @param   numSrc  the new number of sources.
	 */
	public void setNumSrcs(int numSrc) {
	    if (numSrc > 0){
		srcloc = new PitArea[numSrc];
	    }
	    else {
		srcloc = null;
	    }
	}

	/** Changes the number of destinations an object has. After this call, all 
	 * current destinations are not valid anymore, as the destloc array is 
	 * reallocated. If numDst == 0, destloc is null.
	 *
	 * @param   numDst  the new number of destinations.
	 */
	public void setNumDsts(int numDst) {
		if (numDst > 0){
			destloc = new PitArea[numDst];
		}
		else {
			destloc = null;
		}
	}
	/* end src and dests ********************************/

	/* State functions ******************************************************/
	/** Tells if the object CAN have states. An object has states if the states
	 * variable is not equal to -1.
	 *
	 * @return true if object has states
	 */
	public boolean hasStates(){
		return (states != -1);        
	}

	/** Tells how many states an object has, -1 if it doesnt have any.
	 * state variable is responsible for keeping track of this.
	 *
	 * @return the number of states the objects has, -1 if N/A
	 */
	public int getStates(){
		return (states);        
	}

	/** Sets the number of states an object has. An object can have 0 or more
	 * states. 0 means it doesnt have states, but could have.
	 */
	public void setStates(int states){
		this.states = states;
	}

	/** change the state of current object(next state).
	* If it doesnt have any, does nothing. Attention, repaint must be called!
	*/
	public void nextState(){
		//we only change states if an object can have state and uses its states
		if ((!hasStates()) || (getStates()==0)){
			return;
		}        
		this.currentState = (this.currentState + 1) % this.states;
	}

	/** change the state of current object(previous state).
	 * If it doesnt have any, do nothing. Attention repaint must be called!
	 */    
	public void previousState(){
		if ((!hasStates()) || (getStates()==0)){
			return;
		}
		//we sum states to avoid being under 0
		this.currentState = (this.currentState + this.states - 1) % this.states;
	}

	/** resets the object state: dials go to point 0, buttonviews and lights
	 * go to initial state(no template overdraw).
	 */
	public void resetState(){
		if (!hasStates()){
			return;
		}
		//we sum states to avoid being under 0
		this.currentState = 0;
	}

	/* End state functions **************************************************/


	/* Move and resize functions ********************************************/
	/** Moves the object in the panel. The parameters tell how much it must be
	 * moved. Called by mouse drag and drops. Max x and y are given by the
	 * resolution
	 *
	 * @param xoff how much to move in x axis
	 * @param yoff how much to move in y axis
	 * @param resolution the limits of the move
	 * @return true of operation was successful
	 */
	public boolean move(int xoff, int yoff) {
	    //we move the current selected destloc
	    return destloc[currentSelectedDest].move(xoff, yoff, Cockpit.resolution);
	}

	/** Moves all dests of the object in the panel. The parameters tell how much it must be
	 * moved. Called by mouse drag and drops. Max x and y are given by the
	 * resolution
	 *
	 * @param xoff how much to move in x axis
	 * @param yoff how much to move in y axis
	 * @return true if all areas were moved
	 */
	public boolean moveAll(int xoff, int yoff) {
	    if ((destloc == null)){
		return false;
	    }
	    boolean ret = true;	    
	    for (int i=0;i<destloc.length;i++){
		if (destloc[i] != null){
		    //we move the current selected destloc
		    ret &= destloc[i].move(xoff, yoff, Cockpit.resolution);
		}
	    }
	    return ret;
	}

	/** Moves all srclocs of the object in the template. The parameters tell how much it must be
	 * moved. Called by mouse drag and drops. Max x and y are given by the
	 * resolution
	 *
	 * @param xoff how much to move in x axis
	 * @param yoff how much to move in y axis
	 */
	public void moveAllInTemplate(int xoff, int yoff) {        
		if ((srcloc != null)){
			for (int i=0;i<srcloc.length;i++){
				if (srcloc[i] != null){
					//we move the current selected destloc
					srcloc[i].move(xoff, yoff, Cockpit.template.resolution);
				}
			}
		}
	}

	//move the object srcloc on the template
	public void moveInTemplate(int xoff, int yoff) {
		//we move the current src
		srcloc[currentSelectedSrc].move(xoff, yoff, Cockpit.template.resolution);
	}

	/**
	 * Makes all srclocs the same size, based on the base srcloc.
	 */
	public void equalSrclocs(int base){
		if ((srcloc == null) || (srcloc[base] == null))
			return;

		PitArea baseSrc = srcloc[base];
		for (int i=0;i<srcloc.length;i++){
			srcloc[i].sameAreaAs(baseSrc);
		}
	}

	/**
	 * Makes all destlocs the same size, based on the base destloc.
	 */
	public void equalDestlocs(int base){
		if ((destloc == null) || (destloc[base] == null))
			return;

		PitArea baseDest = destloc[base];
		for (int i=0;i<destloc.length;i++){
			destloc[i].sameAreaAs(baseDest);
		}
	}

	/**
	 * Make destlocs the same size of a base srcloc
	 */
	public void equalDestlocToSrc(int baseSrc){
		if ((srcloc == null) || (srcloc[baseSrc] == null))
			return;

		for (int i=0;i<destloc.length;i++){
			destloc[i].sameAreaAs(srcloc[baseSrc]);
		}
	}

	/**
	 * make srclocs the same size as a base destloc
	 */
	public void equalSrclocsToDest(int baseDest){
		if ((destloc == null) || (destloc[baseDest] == null))
			return;

		for (int i=0;((i<srcloc.length)&&(srcloc[i]!=null));i++){
			srcloc[i].sameAreaAs(destloc[baseDest]);
		}
	}

	/** makes all srclocs equal to destlocs. If only one destloc, make all srclocs
	 * equal. If more than one destloc, makes each src equal to the corresponding 
	 * dest. Im lazy, I prefer doing here for all objects instead of overriding
	 * for each type of them
	 */
	public void equalSrcsToDests(){
		if ((srcloc == null) || (destloc == null)){
			return;
		} 

		//for indicators, we only use one direction
		if (this instanceof PitIndicator){
			PitIndicator ind = (PitIndicator)this;
			for (int i=0;i<ind.numtapes;i++){                
				if ((destloc[i] != null) && (srcloc[i] != null)){
					if (ind.orientation.equals("vertical")){
						srcloc[i].sameWidthAs(destloc[i]);
					}
					else {
						srcloc[i].sameHeightAs(destloc[i]);
					}
				}
			}
			return;
		}

		//other objects
		if (destloc.length == 1){
			equalSrclocsToDest(0);
		}
		else if (destloc.length > 1){
			for (int i=0;(i<destloc.length) && (i<srcloc.length);i++){
				if ((destloc[i] != null) && (srcloc[i] != null)){
					srcloc[i].sameAreaAs(destloc[i]);
				}
			}            
		}
	}


	/** Resize the object, based on a corner. The corner is the closest to the
	 * click. Objects that have special resize, like HSI, must override this
	 * and implement its own
	 *
	 * @param xinc  x axis increment
	 * @param yinc  y axis increment
	 * @param target the corner the resize will be based.
	 */
	public boolean resize(int xinc, int yinc, int target){
		//we cant resize the current selected destloc
		return destloc[currentSelectedDest].resize(xinc, yinc, target, Cockpit.resolution);
	}

	/** Resize the area in template, based on a corner. The corner is the closest 
	 * to the click. Objects that have special resize, like HSI, must override this
	 * and implement its own
	 *
	 * @param xinc  x axis increment
	 * @param yinc  y axis increment
	 * @param target the corner the resize will be based.
	 */
	public void resizeInTemplate(int xinc, int yinc, int target){
		//we cant resize the current selected destloc
		srcloc[currentSelectedSrc].resize(xinc, yinc, target, Cockpit.template.resolution);
	}
	/* End move*************************************************************/

	/* Detection functions *************************************************/
	public boolean pointIsInside(Point p) {
		//we detect for all destlocs
		if (destloc == null){
			return false;
		}
		//we get higher dests first...
		for (int i=destloc.length-1; i>=0;i--){
			if ((destloc[i]!=null) && (destloc[i].pointIsInside(p))){
				currentSelectedDest = i;
				return true;
			}
		}
		return false;
	}

	public boolean pointIsInside_template(Point p) {
		//we detect for all srclocs
		if (srcloc == null)
			return false;

		for (int i=0; i<srcloc.length;i++){
			if (srcloc[i] != null &&
					p.x >= srcloc[i].upl.x &&
					p.x <= srcloc[i].dor.x &&
					p.y >= srcloc[i].upl.y &&
					p.y <= srcloc[i].dor.y
			){
				currentSelectedSrc = i;
				return true;
			}
		}
		return false;
	}

	//when you drag, you can move or resize the component
	//this function represents what type of op you do
	//we have 4 pix tolerance(2 for each side)
	public int getOperation(Point point){
		return destloc[currentSelectedDest].getOperation(point);
	}
	public int getResizeCorner(Point point){
		return destloc[currentSelectedDest].getResizeCorner(point);
	}
	public int getOperationInTemplate(Point point){
		return srcloc[currentSelectedSrc].getOperation(point);
	}
	public int getResizeCornerInTemplate(Point point){
		return srcloc[currentSelectedSrc].getResizeCorner(point);
	}

	/** gets the cursor for the mouse. This is used to see if we are near a 
	 * border, so we can change the cursor to arrows
	 */
	public Cursor getCursor(Point point){
		if ((destloc != null) && (destloc[currentSelectedDest] != null)){
			return destloc[currentSelectedDest].getCursor(point);
		}
		else {
			return Cursor.getDefaultCursor();
		}
	}

	/** Gets the cursor for the object, in template. Cursor changes to arrow if
	 * its near the border of the selected srcloc
	 */
	public Cursor getCursorInTemplate(Point point){
		if ((srcloc != null) && (srcloc[currentSelectedSrc] != null)){
			return srcloc[currentSelectedSrc].getCursor(point);
		}
		else {
			return Cursor.getDefaultCursor();
		}
	}


	/* end detection functions ********************************************/

	/* Draw Functions ******************************************************/
	
	/** Draws a src area into the dst area, scaling as needed. Usually called by
	 * the draw functions.
	 *
	 * @param g the  graphisc context we are drawing to
	 * @param src the source area identifier
	 * @param dest the destination area identifier
	 */
	protected void drawSrcDst(Graphics2D g, int src, int dst) {
	    if (
			(destloc == null) || (srcloc == null) || 
			(destloc[dst] == null)|| (srcloc[src] == null)
	    ){
			return;
	    }
	    Image image;
	    Rectangle r = destloc[dst].rectangle;
	    Rectangle r2 = srcloc[src].rectangle;

	    //now this is most curious! The destloc defines if the area will be
	    //transparent or not... kinda weird.
	    // lights use different template image, full brightness on last colors
	    if (this instanceof PitLight){
		    if (destloc[dst].transparent){
			    image = Cockpit.template.getImage(PitTemplate.IM_FLOOD_T);
		    }
		    else {			
			    image = Cockpit.template.getImage(PitTemplate.IM_FLOOD);
		    }
	    }
	    else {
		    if (destloc[dst].transparent){
			    image = Cockpit.template.getImage(PitTemplate.IM_FLOOD_INST_T);
		    }
		    else {			
			    image = Cockpit.template.getImage(PitTemplate.IM_FLOOD_INST);
		    }
	    }

	    if (image != null) {
		    g.drawImage(image, 
			r.x, r.y, r.x + r.width, r.y + r.height,
			r2.x, r2.y, r2.x + r2.width, r2.y + r2.height, null
		    );
	    }
	    if (drawRectangle){
		    destloc[dst].draw(g);
	    }
	}

	//same as above, and draws the label too
	protected void drawSrcDstLabeled(Graphics2D g, int src, int dst) {
		drawSrcDst(g, src, dst);
		if ((label != null)){
			destloc[dst].drawText(label.toString(), g);
		}
	}

	/**
	 * Draws the object. The object is drawn as follows: the srcloc[0] is drawn 
	 * in the desloc[0] area. Then destloc[0] rectangle is drawn around it. Not
	 * all components are drawn like this, so they must override this method and
	 * implement its own way.
	 *
	 * @param   g   the graphics context of the panel window.
	 */    
	public void draw(Graphics2D g) {
		drawSrcDst(g, 0, 0);
	}

	/**
	 * Draws the object selected destination outline.
	 *
	 * @param   g   the graphics context of the panel window.
	 */    
	public void drawCurrentDestOutline(Graphics2D g) {
		if ((destloc != null) && (destloc.length > currentSelectedDest) && (destloc[currentSelectedDest] != null)){
			destloc[currentSelectedDest].draw(g);
		}
		else {
			currentSelectedDest = 0;
		}
	}


	/**
	 * Draws the object, with a label. The label is a comment of the type name:.
	 * Everything after the name: is the label.
	 *
	 * @param   g   the graphics context of the panel window.
	 * @param   im  the template image, where we get data from
	 * @see draw(Graphics2d, Image)
	 */
	public void drawLabeled(Graphics2D g) {
		draw(g);
		if ((label != null) && (destloc != null) & (destloc[0] != null)){
			destloc[0].drawText(label.toString(), g);
		}        
	}

	/** Draws the object in the template window. Basically, all of the objects
	 * srclocs are drawn, including the selected src.
	 *
	 * @param   g   the graphics context of the template window, cast to 2d
	 */
	public void drawTemplate(Graphics2D g) {
		if (srcloc == null)
			return;

		for (int i = 0; i < srcloc.length; i++) {
			if (srcloc[i] != null){
				srcloc[i].draw(g);
			}
		}
	}

	/** Draws the object in the template, using a label. The label in the 
	 * template is a string of the form src + i, where i is a number identifying
	 * which srcloc it is.
	 *
	 * @param   g   the graphics context of the template window.
	 * @see drawTemplate(Graphics2d)
	 */
	public void drawTemplateLabeled(Graphics2D g) {
		drawTemplate(g);
		if (srcloc == null)
			return;
		for (int i = 0; i < srcloc.length; i++) {
			if (srcloc[i] == null)
				break;

			String s = "src" + i;
			if (Math.abs(srcloc[i].dor.y - Cockpit.resolution.height) < 10){
				//we are almost on the lower edge
				srcloc[i].drawTextOver(s, g);
			}
			else {
				srcloc[i].drawTextUnder(s, g);
			}
		}
	}

	/** Draws the selected src only.
	 *
	 * @param g the graphics context of the template window, cast to Graphics2D
	 */
	public void drawSelectedSrcTemplate(Graphics2D g) {
		if ((srcloc != null) && (srcloc[currentSelectedSrc] != null)){
			srcloc[currentSelectedSrc].draw(g);
		}
	}

	/** Draws the selected src only, but with label.
	 *
	 * @param g the graphics context of the template window, cast to Graphics2D
	 */
	public void drawSelectedSrcTemplateLabeled(Graphics2D g) {
		drawSelectedSrcTemplate(g);
		if (srcloc == null)
			return;

		int i = currentSelectedSrc;
		String s = "src" + i;
		if (Math.abs(srcloc[i].dor.y - Cockpit.resolution.height) < 10){
			//we are almost on the lower edge
			srcloc[i].drawTextOver(s, g);
		} else {
			srcloc[i].drawTextUnder(s, g);
		}
	}    
	/* end draw functions ************************************************/


	// used by the clone method
	protected void cloneMeTo(PitObject po){
		//shallow copy
		po.id = id;
		po.callbackslot = callbackslot;
		po.cyclebits = cyclebits;
		po.states = states;
		po.currentSelectedDest = currentSelectedDest;
		po.currentSelectedSrc = currentSelectedSrc;
		po.currentState = currentState;
		po.persistant = persistant;

		//deep copy
		po.comments = PitComment.clone(comments);
		if (comments != null){
			po.label = po.comments.getLabel();
		}
		//srcloc
		if (srcloc != null){
			po.srcloc = new PitArea[srcloc.length];
			for (int i = 0; i < srcloc.length; i++) {
				po.srcloc[i] = PitArea.clone(srcloc[i]);
			}
		}
		else {
			po.srcloc = null;
		}
		if (destloc != null){
			po.destloc = new PitArea[destloc.length];
			for (int i=0;i<destloc.length;i++){
				po.destloc[i] = PitArea.clone(destloc[i]);
			}
		}
		else {
			po.destloc = null;
		}
		if (color != null){
			po.color = new PitColor[color.length];
			for (int i = 0; i < color.length; i++) {
				po.color[i] = PitColor.clone(color[i]);
			}
		}
		else {
			po.color = null;
		}

		//we dont need to copy static fields

	}

	//call this to check the object
	//TODO: this will be abstract
	/** Checks the object. Each type of object has its own features which should
	 * be checked. For example, HSI must be perfectly square. Called when checking
	 * a cockpit.
	 *
	 * @return a string describing the error found.
	 */
	public String check(){
		String s;
		return s = new String();
	}


	/** returns object short description, including type and ID */
	public String getShortDescription(){
	    String s = null;
	    if (this instanceof PanelHud){
			s = new String("HUD");
			return s;
	    }
	    else if (this instanceof PanelRWR){
			s = new String("RWR");
			return s;
	    }
	    else if (this instanceof PanelMFD){
			s = new String("MFD ");
			s = s + ((PanelMFD)this).mySuf;
			return s;
	    }

	    if (this instanceof PitADI){
			s = new String("ADI ");
	    }
	    else if (this instanceof PitButtonView){
			s = new String("Buttonview ");            
	    }
	    else if (this instanceof PitChevLift){
		if (((PitChevLift)this).ehLift)
		    s = new String("LiftLine ");
		else
		    s = new String("Chevron ");
	    }
	    else if (this instanceof PitDED){
			s = new String("DED ");
	    }
	    else if (this instanceof PitDial){
			s = new String("Dial ");
	    }
	    else if (this instanceof PitDigits){
			s = new String("Digits ");
	    }
	    else if (this instanceof PitHSI){
			s = new String("HSI ");
	    }
	    else if (this instanceof PitIndicator){
			s = new String("Indicator ");
	    }
	    else if (this instanceof PitKneeboard){
			s = new String("Kneeboard ");
	    }     
	    else if (this instanceof PitLight){
			s = new String("Light ");
	    }
	    else if (this instanceof PitMachASI){
			s = new String("MachASI ");
	    }
	    else if (this instanceof PitText){
			s = new String("Text ");
	    }
	    else {
			s = new String();
	    }
	    s = s + this.id;
	    if (this.label != null){
			s = s + " " + label;
	    }
	    return s;	    
	}
	
	/** this gets a sort description of the object. Attention, deprecated, will
	 * be substituted by toShortDescription
	 */
	public String toString(){
	    return getShortDescription();
	}
	
	/**
	* Shows the properties of a newly created object. 
	* @return if the object can be added to pit (all values correct), returns true.
	* Otherwise, return false.
	*/
	public boolean showNew(JDialog parent){
		PropertiesDialog pd = getNewPropertiesDialog(parent);
		pd.setVisible(true);
		boolean ret = pd.getFieldsOk();
		pd.dispose();
		return ret;
	}

	/**
	* Shows this object properties. Every type of object has many properties
	* associated with it. ID, callbackslot, cyclebits, srcloc are examples of
	* objects properties.
	*
	* @param   parent  the parent window, for modal
	*/
	public void showProperties(JFrame parent){
		PropertiesDialog pd = getPropertiesDialog(parent);
		pd.setVisible(true);		
	}

	/* abstract functions *******************************/

	/** parses the object from a tokenizer. The tokenizer must be in the exact
	* position for the object to be parsed. That means the tokenizer has just read
	* the #id type entry
	*
	* @param st the tokenizer
	* @throws if a parse error occurs
	*/
	abstract public void parseData(PitTokenizer st) throws ParseErrorException;
	
	/** Saves the component to a file. The component is saved in the current
	* file position, starting with the #ID <type> entry end ending with the 
	* #end. When saving a cockpit, we call this for all components, in the 
	* right order: manager, sounds, buttons, buttonviews, objects, panels,
	* surfaces and buffers.
	*
	* @param   arq the file we are saving to
	* @throws  if theres anything wrong while saving.
	*/    
	abstract public void writeComponent(misc.RAFile arq) throws Exception;

	/** returns the new property dialog for this object. */
	abstract protected PropertiesDialog getNewPropertiesDialog(JDialog parent);
	
	/** returns the properties dialog for this object. */
	abstract protected PropertiesDialog getPropertiesDialog(JFrame parent);
	
	/* Makes a deep copy of the component. This means all references are copied,
	 * so the clone is completely independent of the source component. Useful
	 * for coping/pasting objects on another panel. The cloned id can be different
	 * from the src object
	 *
	 * @return  the cloned object
	 */
	abstract public Object clone();

	/** convert the object to text format, to be transfered to clipboard.
	 * Text format is the same as you see in the cockpit dat file
	 */
	abstract public String toTextFormat();
	/* end abstract *************************************/
}

