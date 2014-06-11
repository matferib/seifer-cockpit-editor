package pitComponents;
import cockpitEditor.UsPr;
import pitComponents.pitObjects.*;
import pitComponents.pitHelperComponents.*;
import java.io.*;
import javax.swing.JTextArea;
import java.awt.*;
import java.awt.image.*;

/**
 * Each panel may or may not have a buffer associated with it. A buffer is an
 * optimization, probably used by falcon to render surfaces faster.
 */
public class PitBuffer {
    
    /* usually, id is not used, equals to 1
     */
    public int id; 
    
    /** the name of the image this buffer represents */
    private PitFile filename;
    
    /** the size of the buffer, usually the resolution of the cockpit, but not
     * sure of it
     */
    private Dimension buffersize;
    
		/** 3 buffer images. 
			* Index 0 is the original image. 
			* Index 1 is the same with lighting effect applied.
			* Index 2 is same as 1, but is transparent.
			* 0 is never drawn, is just a reference for creating 1 and 2
			* Buffer dont need instrument light (it only applies to template).
			*/
		public BufferedImage images[]= new BufferedImage[3];

		/** image types panel can require. Either transparent or solid. Original is used only internally */
		public static final int IM_ORIGINAL=0, IM_OPAQUE = 1, IM_TRANSPARENT = 2;

    
    /** indicates the buffer is loaded, for drawing purposes */
    public boolean loaded = false;
    
    /** every component can have comments */
    public PitComment comments;
    
    /** creates an empty buffer, with the given id
     *
     * @param id the buffer id, usually 1
     */
    public PitBuffer(int id) {
        this.id = id;
        //by default, we create the buffers with the cockpit resolution size
        buffersize = new Dimension(Cockpit.resolution);
    }
    
    /**
     * Buffer dont need to be read. They are part of the panel, and are saved
     * when saving the pit. This works just like parseData, except it gets no
     * data and is static.
     *
     * @param st the stream tokenizer
     */
    public static void skipData(PitTokenizer st) throws ParseErrorException{
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
                if (st.ttype == '=') { //skip =
                    st.nextToken();
                }
                
                if ((token.equals("filename")) || (token.equals("buffersize"))) {
                    st.skipLine();
                } 
                else {
                    throw new Exception("Invalid token ;");
                }
            }
            while (true);
        }
        
        catch (Exception e) {
            throw new ParseErrorException("pitBuffer.java: " + e);
        }
        
    }
    
    /** parses the data, reading buffer information */
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
                if (st.ttype == '=') { //skip =
                    st.nextToken();
                }
                if ( (!token.equals("filename")) && (st.ttype != st.TT_NUMBER)) {
                    throw new ParseErrorException("expecting number token");
                }
                
                if (token.equals("filename")) {
                    filename = new PitFile();
                    filename.parseData(st);
                } 
                else if (token.equals("buffersize")) {
                    int n0, n1;
                    
                    n0 = (int) st.nval;
                    st.nextToken();
                    if ( (st.ttype != st.TT_NUMBER)) {
                        throw new ParseErrorException(
                                "buffersize expecting second number token");
                    }
                    n1 = (int) st.nval;
                    buffersize = new Dimension(n0, n1);
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
            throw new ParseErrorException("pitBuffer.java: " + e);
        }
    }
    
    /** writes the buffer to the file, at the specified position
     *
     * @param arq the file, opened and in position
     */
    public void writeComponent(misc.RAFile arq) {
        try {
            //if (comments != null){
                //comments.write(arq);
            //}
            arq.writeln("#" + id + " BUFFER");
            arq.writeln("\tfilename = " + filename.getName() + ";");
            arq.writeln("\tbuffersize = " + buffersize.width + " " +
                    buffersize.height + ";");
            arq.writeln("#END");

        } catch (IOException io) {
            System.out.println("pitBuffer.java-> IO Error: " + io);
        }
    }
    
    /** returns the buffer filename */
    public String getFilename() {
        return this.filename.getName();
    }
    
    /** sets the buffer filename */
    public void setFilename(String newName) {
        filename = new PitFile(newName);
    }
    
    /** returns the buffer size */
    public Dimension getSize() {
        return this.buffersize;
    }
    
    /** flushes the buffer, freeing image memory */
    private void flush(){
        for (int i=0;i<images.length;i++){
            if (images[i] != null){
                images[i].flush();
				images[i] = null;
            }
        }
    }
    
    /**
     * Loads buffer images. Then the images can be used for
     * reading by other components. Returns the image size, or
		 * cockpit resolution in case of error (when we cant get image size, we
		 * use cockpit res)
     */
    public Dimension load() {
			// set as loaded
			loaded = true;

			flush();
			String fn = Cockpit.cwd + "/" + filename.getName();
			try{
				images[IM_ORIGINAL] = javax.imageio.ImageIO.read(new File(fn));
			}
			catch (Exception e){
				flush();
				return Cockpit.resolution;
			}

			// image dimensions
			int w = images[IM_ORIGINAL].getWidth(), 
					h = images[IM_ORIGINAL].getHeight();

			//get the color model of the image, so we can generate its variants
			PixelGrabber pg = new PixelGrabber(
					images[IM_ORIGINAL], 0, 0, w, h, false);
			try {
				pg.grabPixels();
			} catch (Exception e){
				System.out.println("PitTemplate.java: error grabbing pixel");
				flush();
				return Cockpit.resolution;
			}

			// original colors palette
			int originalPalette[] = new int[256];
			
			//create the color models, based on the normal one
			//IndexColorModel cm[] = new IndexColorModel[4];
			IndexColorModel cm[] = new IndexColorModel[3]; // original and the 2 modified

			cm[IM_ORIGINAL] = (IndexColorModel)pg.getColorModel();
			cm[IM_ORIGINAL].getRGBs(originalPalette);
	
			// derivated palette
			int derivatedPalette[] = new int[256];
		
			// ambient and flood for first palette colors
			float lightFactor[] = new float[3];
			float amb[] = UsPr.getAmbientLightF();
			float flood[] = Cockpit.getFloodF();
			for (int i=0;i<3;i++){
				lightFactor[i] = amb[i] + flood[i];
				if (lightFactor[i] > 1.0f){
					lightFactor[i] = 1.0f; // never higher than 1
				}
			}
			for (int i=0;i<(256-48);i++){
				//internally, its AABBGGRR
				int r = (int)(lightFactor[0] * ((originalPalette[i] & 0xFF0000) >> 16));
				int g = (int)(lightFactor[1] * ((originalPalette[i] & 0x00FF00) >> 8));
				int b = (int)(lightFactor[2] * ((originalPalette[i] & 0x0000FF)));
				derivatedPalette[i] = (r << 16) | (g << 8) | (b);
			}

			// now we generate for the last colors
			float inst[] = Cockpit.getInstF();
			for (int i=0;i<3;i++){
				lightFactor[i] += inst[i];
				if (lightFactor[i] > 1.0f){
					lightFactor[i] = 1.0f; // never higher than 1
				}
			}
			for (int i=(256-48);i<256;i++){
				int r = (int)(lightFactor[0] * (originalPalette[i] & 0xFF0000 >> 16));
				int g = (int)(lightFactor[1] * ((originalPalette[i] & 0x00FF00) >> 8));
				int b = (int)(lightFactor[2] * ((originalPalette[i] & 0x0000FF)));
				derivatedPalette[i] = (r << 16) | (g << 8) | (b);
			}

			//create the derivated color model, no transparency
			cm[IM_OPAQUE] = new IndexColorModel(
					8,/*bits*/ 
					256,/*colors*/
					derivatedPalette, 
					0,/*start offset*/ 
					false,/* has alpha */
					258,
					DataBuffer.TYPE_BYTE
			);
			//and the transparent one
			cm[IM_TRANSPARENT] = new IndexColorModel(
					8,/*bits*/ 
					256,/*colors*/
					derivatedPalette, 
					0,/*start offset*/ 
					false,/* has alpha */
					0,
					DataBuffer.TYPE_BYTE				
			);

			// empty derivated image
			images[IM_OPAQUE] = new BufferedImage(w, h,	BufferedImage.TYPE_BYTE_INDEXED, cm[IM_OPAQUE]); 
			// empty derivated with transparency
			images[IM_TRANSPARENT] = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, cm[IM_TRANSPARENT]);

			// fill the images with content
			// first we get the data buffers of all images
			DataBuffer buf[] = new DataBuffer[images.length];
			for (int i=0;i<images.length;i++){
				buf[i] = images[i].getRaster().getDataBuffer();
			}
			// now copy from original image to derivated ones, applying color model
			for (int i=0;i<buf[IM_ORIGINAL].getSize();i++){
				// apply on other images, 0 is original
				int elem = buf[IM_ORIGINAL].getElem(i);
				buf[IM_OPAQUE].setElem(i, elem);
				buf[IM_TRANSPARENT].setElem(i, elem);
			}

			// is this second check necessary?
			Dimension buffersize = 
				(images[IM_ORIGINAL] == null) ?
				Cockpit.resolution:
				new Dimension(w, h);

			return buffersize;
		}
	
		/** unloads the buffer. Means it will be loaded before next use. */
		public void unload(){
			loaded = false;
		}
		
		/** returns the required image to caller. Image can be solid or transparent version */
		public Image getImage(int imType){
			return ((imType==IM_OPAQUE) || (imType==IM_TRANSPARENT)) ? 
				images[imType] : 
				null
			;
		}
		
}
