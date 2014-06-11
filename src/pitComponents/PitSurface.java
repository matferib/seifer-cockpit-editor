package pitComponents;


import pitComponents.pitHelperComponents.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import windows.PropertiesDialog;
import pitComponents.pitDialogComponents.*;


/** represents the surface entries, at the end of the file together with buffers.
 * Each surface can be thought of as a srcloc for the background gifs. Each panel
 * can use one or more surfaces. They do that declaring the surface id, and an area
 * which would represent the destloc.
 */
public class PitSurface {
    
    /** surface id */
    public int id;  
    
    /** the filename we get data from */
    public PitFile filename;
    
    /** The surface srclocs come from the surface entry itself. */
    public PitArea srcloc;
    
    /** surfaces are grouped by buffers, for performance reasons, it seems.
     * this points to the buffer the surface is in
     */
    public PitBuffer buffer;
    
    /** indicates if the gif file is already loaded */
    public boolean loaded = false;
    
    /** all objects can have comments, including surfaces */
    public PitComment comments;
    
    /** used to create new surface, actually this is not used anymore, as 
     * we cant create surfaces due to BMS new implementation
     */
    public boolean canBeCreated = false;
    
    /** Creates a new surface, with all values nulled, and id given by parameter
     *
     * @param id the surface id
     */
    public PitSurface(int id) {
        this.id = id;
    }
    
    /** returns a new surface, with all properties equal to this one, except 
     * for the Id. Useful for removing duplicated surfaces.
     *
     * @param newId the id of the clone surface
     * @return the new clone surface, with id newId
     */
    public PitSurface clone(int newId){
        //create surface
        PitSurface ps = new PitSurface(newId);

        ps.filename = PitFile.clone(filename);
        ps.srcloc = PitArea.clone(srcloc);
        ps.buffer = null;
        ps.loaded = false;
        ps.comments = PitComment.clone(comments);
        ps.canBeCreated = false;
        
        return ps;
    }
    
    /**
     * Changes this surface area. Useful for panels(buffers) with only one surface
     * covering the entire screen. The new area is transparent by default
     *
     * @param the surface area
     */
    public void setArea(Rectangle r){
        srcloc = new PitArea(r);
        //srcloc.transparent = true;
        srcloc.opaque = false;
    }
    
  
    /** Checks if image exists.
     *
     * @return the filename if its not found
     */
    public String check(){
        File f = new File(Cockpit.cwd, filename.getName());
        
        if (!f.exists()){
            return ("File " + filename.getName() + " does not exist\n");
        }
        
        return "";
    }
    
    public void parseData(PitTokenizer st) throws ParseErrorException {
        String token = null;
        
        try {
            do {
                st.skipComment();
                if (st.ttype != st.TT_WORD) {
                    throw new ParseErrorException("expecting word token");
                }
                
                token = st.sval;
                if (token.equals("#end")) {
                    break;
                }
                
                st.nextToken();
                if (st.ttype == '=') {
                    //skip =
                    st.nextToken();
                }
                
                if (token.equals("filename")) {
                    filename = new PitFile();
                    filename.parseData(st);
                }
                else if (token.equals("srcloc")) {
                    srcloc = new PitArea();
                    srcloc.parseData(st);
                }
                
                //get the ;
                st.nextToken();
                if (st.ttype != ';') {
                    throw new Exception("expecting ;");
                }
            }
            while (true);
        }
        
        catch (Exception e) {
            throw new ParseErrorException("pitSurface.java: " + e);
        }
    }

    /**
     * Sets the surface filename. Used by the buffer, when its image is changed.
     *
     * @param filename the image filename, no directories
     */
    public void setFileName(String filename){
        this.filename = new PitFile(filename);
    }
    
    /**
     * Writes the surface to the given file, using the given filename. This 
     * happens because surfaces get their names from the buffer they are inside.
     * So the caller, in this case, the panel which is writing, must use the
     * buffer filename
     *
     * @param arq the destination file
     * @param filename the filename this surface gets data from, taken from a 
     * buffer
     */
    public void writeComponent(misc.RAFile arq, String filename) {
        try {
            //if (comments != null){
                //comments.write(arq);
            //}
            arq.writeln("#" + id + " SURFACE");
            arq.writeln("\tfilename = " + filename + ";");
            arq.writeln("\tsrcloc = " + srcloc + ";");
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitSurface.java-> Erro de IO: " + io);
        }
    }
}

class SurfDialog extends PropertiesDialog {
	JTabbedPane tp = new JTabbedPane();
	PitSurface surf;
    
	//main
	JLabel filename = new JLabel();
	AreasFieldPanel surfArea = new AreasFieldPanel(this, "area");
    
	//comments
	CommentsPane comments = new CommentsPane(this);

    public SurfDialog(JFrame parent, PitSurface surf){
        super(parent, null, "Surface Properties", false);
        setProperties(surf);
    }
    
    public SurfDialog(JDialog parent, PitSurface surf){
        super(parent, null, "Surface Properties", true);
        setProperties(surf);
    }
    
    private void setProperties(PitSurface surf){
        this.surf = surf;
        this.setSize(400,400);
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("Surface #" + surf.id, JLabel.CENTER));

            mainP.add(filename);
            mainP.add(surfArea);

            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");
        }
        //comments
        {
            tp.add("comms", comments);
            tp.setToolTipTextAt(1, "comments");
        }

        c.add(tp, BorderLayout.CENTER);

        if (!isNew){
            loadValues();
		}
    }

    public void loadValues(){
        //main
        filename.setText(surf.filename.getName()); //read only, buffer controls
        surfArea.setFields(surf.srcloc);

        //comments
        comments.setComments(surf.comments);
    }

	protected void setValues() throws Exception {
		//main
		surf.srcloc = surfArea.getArea();

		//update comments
		surf.comments = comments.getComments();
    }
}



