/*
 * FalconImage.java
 *
 * Created on 11 de Setembro de 2005, 14:19
 */

package pitComponents.pitHelperComponents;
import java.awt.image.*;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author  matheus
 * Used for drawing cockpit with different levels of illumination, following the
 * new convention used by the cobra exe. There are 4 possible states for the cockpit
 * - lights off: only enviroment light
 * - flood on: env + cockpit flood
 * - inst on: env + cockpit instrument light
 * - ints + flood on: env + inst + flood
 *
 * Environment light and flood light affects all colors of the palette, regardless
 * of position. Instrument light affects only the last 16 colors of palette, which
 * are drawn in full brightness in original falcon.
 *
 * The difference between original method and new one is given by the inst and flood
 * light switches position. In original version, last 48 colors were always drawn 
 * in full brightness, while the rest was affected by day light illumination.
 *
 * Now, for lights, the last 48 are always drawn in full, while the other objects
 * depend on the inst color. For the other colors, we depend now on daylight and
 * flood light.
 *
 * Colors are divided into 3 components: RGB. 1.0 is maximum intensity and 0 is
 * darkness. Illumination can be vewied as a multiplication factor. So, if we have 
 * a mid red flood light(0.5, 0.0, 0.0), used on a white surface(1.0 1.0 1.0) with
 * no environment light, the surface will be midred too (1.0*0.5 = 0.5, 1.0*0.0 = 0.0,
 * 1.0*0.0=0.0).
 *
 * FalconImage is based on buffered images, which are used by the editor to draw
 * objects in different states.
 */
public class FalconImage extends BufferedImage {
    
    /** these constants are used to manipulate the different types of light */
    public final int FI_ENV = 0, FI_FLOOD=1, FI_INST=2;
    
    /** environment, flood and instrument lights */
    private Color colors[] = {
        new Color(0.0f, 0.0f, 0.0f),
        new Color(0.6f, 0.0f, 0.0f),
        new Color(0.0f, 0.0f, 0.0f)
    };
    
    /** used for color model manipulation. Original is the original color model,
     * ALL is used for all objects except lights, and light for lights 
    */
    public final int FI_CM_ORIGINAL=0, FI_CM_ALL = 1, FI_CM_LIGHTS = 2;
    
    /** the color models of the image, for objects and for lights. Lights require
     * a different color model, because inst light doesnt affect them.
     */
    private IndexColorModel cms[] = new IndexColorModel[3];

    
    /** Creates a new instance of FalconImage */
    public FalconImage(int width, int height, IndexColorModel c) {
        super(width, height, TYPE_BYTE_INDEXED, c);
        computeColorModels();
    }

    
    /** sets the light given by parameter which.
     *
     * @param which: FI_ENV, environment; FI_FLOOD, flood; FI_INST, instrument.
     * @param r red factor
     * @param g green factor
     * @param b blue factor
     */
    public void setLight(int which, float r, float g, float b){
        if ((which < 0) || (which >= 3)){
            System.out.println("FalconImage.java: Invalid which, " + which + "!");
            return;
        }
    
        //sets the new color
        Color c = new Color(r, g, b);
        colors[which] = c;
        
        //now compute new color models
        computeColorModels();
    }
    
    
    /** sets the environment light */
    public void setEnv(float r, float g, float b){
        setLight(FI_ENV, r, g, b);
    }
    
    /** sets the flood light */
    public void setFlood(float r, float g, float b){
        setLight(FI_FLOOD, r, g, b);        
    }
    
    /** sets the instrument lights */
    public void setInst(float r, float g, float b){
        setLight(FI_INST, r, g, b);        
    }

    
    /** overrides getColorModel to reflect our change in palettes */
    public ColorModel getColorModel(){
        return (cms[FI_CM_ALL] == null)? super.getColorModel() : cms[FI_CM_ALL];
    }
    
    /** computes the color models, based on the current lights */
    public void computeColorModels(){
        //original color model        
        IndexColorModel ocm = cms[FI_CM_ORIGINAL];
        if (ocm == null){
            ocm = (IndexColorModel)super.getColorModel();
        }
        
        //palette
        byte plt[][] = new byte[3][256];
        ocm.getReds(plt[0]);
        ocm.getGreens(plt[1]);        
        ocm.getBlues(plt[2]);

        //light factors for all 3 RGB components
        float lfs[] = new float[3];                
        lfs[0] = (colors[FI_ENV].getRed() + colors[FI_FLOOD].getRed()) / 256.0f;
        lfs[1] = (colors[FI_ENV].getGreen() + colors[FI_FLOOD].getGreen()) / 256.0f;
        lfs[2] = (colors[FI_ENV].getBlue() + colors[FI_FLOOD].getBlue()) / 256.0f;
        for (int i=0;i<3;i++){
            if (lfs[i] > 1.0f){
                lfs[i] = 1.0f;
            }                
        }
        
        //ALL and LIGHT palette
        byte aplt[][] = new byte[3][256];
        byte lplt[][] = new byte[3][256];
        
        //first colors are the same for ALL and LIGHT models
        for (int i=0;i<256-48;i++){
            for (int c=0;c<3;c++){
                aplt[c][i] = (byte)(plt[c][i] * lfs[c]);
                lplt[c][i] = aplt[c][i];
            }
        }
        
        //compute light factors with inst included
        lfs[0] = (colors[FI_ENV].getRed() + colors[FI_FLOOD].getRed() + colors[FI_INST].getRed()) / 256.0f;
        lfs[1] = (colors[FI_ENV].getGreen() + colors[FI_FLOOD].getGreen() + colors[FI_INST].getGreen()) / 256.0f;
        lfs[2] = (colors[FI_ENV].getBlue() + colors[FI_FLOOD].getBlue() + colors[FI_INST].getBlue()) / 256.0f;
        for (int i=0;i<3;i++){
            if (lfs[i] > 1.0f){
                lfs[i] = 1.0f;
            }                
        }
        //now, ALL is affected by instLight, while LIGHT is draw in full
        for (int i=256-48;i<256;i++){
            for (int c=0;c<3;c++){
                lplt[c][i] = plt[c][i];
                aplt[c][i] = (byte)(plt[c][i] * lfs[c]);
            }
        }

        //rewrite color models :)
        cms[FI_CM_ALL] = new IndexColorModel(8, 256, aplt[0], aplt[1], aplt[2], 0);
        cms[FI_CM_LIGHTS] = new IndexColorModel(8, 256, lplt[0], lplt[1], lplt[2], 0);    
    }
    
}





















