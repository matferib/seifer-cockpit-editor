package pitComponents.pitHelperComponents;

import pitComponents.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;


//DONE!!!!!!

/** Represents an area in the pit. An area has basically 2 points. The upleft
 * and the downright. The area includes these points, so an area from (0,0) to
 * (1,1) is 2 pixel x 2pixel. There are 2 representations of the area, the 2 point
 * just mentioned and the rectangle one(point, width, height). Both are useful
 * for different purposes, and must be updated in all write operations.
 * Ideally the area properties should be read only. As it is not possible,
 * there are methods for handling area writes.
 */
public class PitArea  {
    
    //both are necessary, as some use opaque, some use trans, and others use none
    public boolean transparent, opaque;
    public Point upl, dor; //upleft and downright points, NEVER WRITE into THIS!!!!
    public Rectangle rectangle; //another way to represent
    private boolean canHaveModifier = true; ///< indicates this area can have a transparent/opaque modifier
    private boolean resizable = true;    ///< indicates this area can be resized
    ///these represents the type of resize we can do
    public static final int UP=1, RIGHT=2, DOWN=4, LEFT=8;
    
    static private int tolerance = 4; //the tolerance to the border, to resize or move
    
    public PitArea(boolean canHaveModifier, boolean resizable) {
        this.canHaveModifier = canHaveModifier;
	this.resizable = resizable;
        transparent = false;
        opaque = false;
        upl = new Point();
        dor = new Point();
        rectangle = new Rectangle();
    }
    
    public PitArea() {
        this(true, true);
    }
    
    
    public PitArea(Rectangle r){
        this();
        setRectangle(r);
    }
    
    public void setResizable(boolean r){
        resizable = r;
    }
    
    public void setCanHaveModifier(boolean canHave){
        this.canHaveModifier = canHave;
    }
    
    static public PitArea clone(PitArea a){
        if (a == null){
            return null;
	}
        Rectangle r = (Rectangle)a.rectangle.clone();
        PitArea narea = new PitArea(r);
        narea.transparent = a.transparent;
        narea.opaque = a.opaque;
        return narea;
    }
    
    /**
     * Makes this area size equal to another one (same width and height)
     * Only works if area is resizable
     */
    public void sameAreaAs(PitArea base){
	if (!resizable){
	    return;
	}
        rectangle.width = base.rectangle.width;
        rectangle.height = base.rectangle.height;
        update2Points();
    }
    
    /**
     * Makes this object width same as argument
     */
    public void sameWidthAs(PitArea base){
	if (!resizable){
	    return;
	}
        rectangle.width = base.rectangle.width;
        update2Points();
    }

    /**
     * Makes this object height same as argument
     */
    public void sameHeightAs(PitArea base){
	if (!resizable){
	    return;
	}
        rectangle.height = base.rectangle.height;
        update2Points();
    }    
    
    
    /* Checks if a point is inside the area. Usually called by objects to see if
     * they have been clicked.
     *
     * @param p the point we are checking
     * @return true if the given point is inside the area, false otherwise
     */
    public boolean pointIsInside(Point p) {
        if ( (p.x >= upl.x) && (p.x <= dor.x) &&
                (p.y >= upl.y) && (p.y <= dor.y)){
            return true;
        }
        return false;
    }
    
    /** updates this area rectangle based on 2point coordinates. */
    private void updateRectangle(){
        Rectangle r = rectangle;
        r.x = upl.x; r.y = upl.y;
        r.width = dor.x - upl.x + 1;
        r.height = dor.y - upl.y + 1;
    }
    
    /** updates this area 2points coords based on the rectangle. */
    private void update2Points(){
        upl.x = rectangle.x;
        upl.y = rectangle.y;
        dor.x = rectangle.x + rectangle.width -1;
        dor.y = rectangle.y + rectangle.height - 1;
    }
    
    /* Given a rectangle, update this area */
    public void setRectangle(Rectangle r){
	if (!resizable){
	    return;
	}
        rectangle = r;
        upl.x = r.x;
        upl.y = r.y;
        dor.x = upl.x + r.width -1;
        dor.y = upl.y + r.height -1;
    }
    
    public void parseData(StreamTokenizer st) throws ParseErrorException {
        int tempvals[] = new int[4], i = 0;
        
        //we shoud read 4 numbers, and maybe the transparent and opaque keywords
        while (st.ttype != ';') {
            
            switch (st.ttype) {
                case (StreamTokenizer.TT_NUMBER):
                    tempvals[i++] = (int) st.nval;
                    break;
                case (StreamTokenizer.TT_WORD):
                    if (st.sval.equals("transparent")) {
                        transparent = true;
                    } else if (st.sval.equals("opaque")) {
                        opaque = true;
                    } else {
                        throw new ParseErrorException(
                                "PitArea.java: expecting only transparent or opaque keywords");
                    }
                    break;
                default:
                    throw new ParseErrorException(
                            "PitArea.java: expecting [transparent|opaque] uply uplx dry drx;");
            }
            try {
                st.nextToken();
            } catch (Exception e) {
                throw new ParseErrorException("PitArea.java: next token error" +
                        e);
            }
            
        }
        //falcon uses inverted y,x
        upl.y = tempvals[0];
        upl.x = tempvals[1];
        dor.y = tempvals[2];
        dor.x = tempvals[3];
        updateRectangle();
        
        //puts the ; back
        st.pushBack();
        
    }
    
    /** Returns the string representation of this area. */
    public String toString() {
        //returns the string format of the data, like: "transparent 0 0 600 800"
        String numbers = upl.y + " " + upl.x + " " + dor.y + " " + dor.x;
        if (canHaveModifier){
            if (transparent) {
                return ("transparent " + numbers);
            }
            if (opaque) {
                return ("opaque " + numbers);
            }
        }
        return numbers;
    }
    
   
    /* Positional functions ***************************************/
    public boolean move(int xoff, int yoff, Dimension limit) {
        upl.x += xoff;
        upl.y += yoff;
        dor.x += xoff;
        dor.y += yoff;
        //checklimits call upate rectangle
        return checkLimits(limit);
    }
    
    /** Resizes the area, based on a corner/side. Called by objects to
     * resize their areas.
     *
     * @param xinc the axis x increment
     * @param yinc the axis y increment
     * @param target the corner this resize is based
     * @param limit the resolution, it wont cross this limit.
     * @return true if operation is successful
     */
    public boolean resize(int xinc, int yinc, int target, Dimension limit){
        if (!resizable){
	    return false;
	}
        if ((target & LEFT) != 0){
            upl.x += xinc;
            //check if upl is still upl
            if (upl.x >= dor.x) {
                upl.x = dor.x - 1;
            }
        } else if ((target & RIGHT) != 0){
            dor.x += xinc;
            //check if upl is still upl
            if (dor.x <= upl.x){
                dor.x = upl.x + 1;
            }
        }
        
        if ((target & UP) != 0){
            upl.y += yinc;
            if (upl.y >= dor.y){
                upl.y = dor.y-1;
            }
        } else if ((target & DOWN) != 0){
            dor.y += yinc;
            if (dor.y <= upl.y){
                dor.y = upl.y+1;
            }
        }
        //checklimits call upate rectangle
        return checkLimits(limit);
    }
    
    /** Resizes the area equally on both axis. Used by objects which must be a
     * perfect square, like hsi.
     *
     * @param inc how much to resize
     * @param target which corner is the resize based
     * @param limit the resolution, we cant cross this limit
     */
    public void resizeBothAxis(int inc, int target, Dimension limit){
        resize(inc, inc, target, limit);
    }
    
    /** checks if the operation can be done in limits.
     * If so, object is updated. Else, restored
     * @return true if it can be done, false otherwise
     */
    private boolean checkLimits(Dimension limit){
        //out of bounds
        /**if (upl.x < 0) {
            upl.x = 0;
        }
        if (upl.y < 0) {
            upl.y = 0;
        }
        if (dor.x >= limit.width) {
            dor.x = limit.width-1;
        }
        if (dor.y >= limit.height) {
            dor.y = limit.height-1;
        }*/
	// out of bounds, restore
	if ((upl.x < 0) || (upl.y < 0) || (dor.x >= limit.width) || (dor.y >= limit.height)){
	    this.update2Points();
	    return false;
	}
	// update rect if its ok
	else {
	    updateRectangle();
	    return true;
	}
    }
    /**************************************************************/
    
    /* Detection functions ************************/
    /** Gets the point at the center of the given area
     */
    public Point getCenter(){
        Point p = new Point();
        p.x = this.rectangle.x + (this.rectangle.width/2);
        p.y = this.rectangle.y + (this.rectangle.height/2);
        return p;
    }
    /** gets the point at relative coordinate <x,y> 
    * 0,0 is the center, 1,1 is up right, -1,-1 is down left
     */
    public Point getCoordinate(float x, float y){
        Point p = new Point();
        Point c = this.getCenter();
        int xoff =  ((int)((this.rectangle.width/2.0)*x));
	// y is inverted, since + is up
        int yoff = ((int)((this.rectangle.height/2.0)*-y));
        p.x = c.x + xoff;
        p.y = c.y + yoff;
        return p;
    }
    
    /** use to know the operation we do in the object. If the point is near its
     * border, we resize. Else, we move.
     *
     *@param point the querying point
     *@return 0 if not near a border(ie, move), or the way to resize
     */
    public int getOperation(Point point){
	if (!resizable){
	    return 0;
	}
        int res = 0; //means move only
        if (Math.abs(point.x - upl.x) <= tolerance)
            res |= LEFT;
        if (Math.abs(point.x - dor.x) <= tolerance)
            res |= RIGHT;
        if (Math.abs(point.y - upl.y) <= tolerance)
            res |= UP;
        if (Math.abs(point.y - dor.y) <= tolerance)
            res |= DOWN;
        
        if ((res & (UP | DOWN)) == (UP | DOWN))
            return 0; //cant choose up and down simultaneosuly
        else if ((res & (LEFT | RIGHT )) == (LEFT | RIGHT))
            return 0; //similar to above
        else
            return res;
        
    }
    //similar to above, but we ARE resizing(ctrl is held)
    public int getResizeCorner(Point point){
	if (!resizable){
	    return 0;
	}
        int res=0;
        //are we closer to left or right?
        if (Math.abs(point.x - upl.x) <= Math.abs(point.x - dor.x))
            res |= LEFT;
        else
            res |= RIGHT;
        //are we closer to up or down?
        if (Math.abs(point.y - upl.y) <= Math.abs(point.y - dor.y))
            res |= UP;
        else
            res |= DOWN;
        
        return res;
        
    }
    
    public Cursor getCursor(Point p){
        if (pointIsInside(p)){
            int border = getOperation(p);
            switch (border){
                case (LEFT | UP):
                    return(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                case (LEFT | DOWN):
                    return(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                case (RIGHT | UP):
                    return(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                case (RIGHT | DOWN):
                    return(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                case (LEFT):
                    return(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                case (RIGHT):
                    return(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                case (UP):
                    return(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                case (DOWN):
                    return(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                default:
                    return(Cursor.getDefaultCursor());
            }
        }
        else {
            return(Cursor.getDefaultCursor());
        }
    }
    /* End detection ******************************/
    
    /* Drawing functions ****************************************/
    //this function draws a rectangle to the graphics
    public void draw(Graphics2D g) {
        g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        //g.drawRect(rectangle.x-1, rectangle.y-1, rectangle.width, rectangle.height);
    }
    
    //this draws the text inside our area
    public void drawText(String text, Graphics2D g) {
        Rectangle2D r = g.getFont().getStringBounds(text, g.getFontRenderContext());
        float xbias = (float)(r.getWidth()) / 2.0f;
	float ybias = (float)(r.getHeight()) / 2.0f;
        Point center = getCenter();
        g.drawString(text, center.x - xbias, center.y + ybias );
    }
    
    //this draws text under the area
    public void drawTextUnder(String text, Graphics2D g) {
        //we need to get strlen to center the text inside the rectangle
        Rectangle2D r = g.getFont().getStringBounds(text, g.getFontRenderContext());
        float xbias = (float)(r.getWidth()) / 2.0f;
        Point center = getCenter();
        g.drawString(text, center.x - xbias, dor.y + 9);
    }
    
    //this draws text under the area
    public void drawTextOver(String text, Graphics2D g) {
        //we need to get strlen to center the text inside the rectangle
        Rectangle2D r = g.getFont().getStringBounds(text, g.getFontRenderContext());
        float xbias = (float)(r.getWidth()) / 2.0f;
        Point center = getCenter();
        g.drawString(text, center.x - xbias,  upl.y - 9);
    }
    /* End draw ***********************************************************/
    
}
