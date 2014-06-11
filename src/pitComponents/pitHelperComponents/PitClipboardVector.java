/*
 * PitClipboardVector.java
 *
 * Created on May 4, 2005, 8:39 AM
 */

package pitComponents.pitHelperComponents;

import java.util.*;
import java.awt.datatransfer.*; 
import pitComponents.PitPanel;
import pitComponents.pitObjects.PanelHud;
import pitComponents.pitObjects.PanelMFD;
import pitComponents.pitObjects.PanelRWR;
import pitComponents.pitObjects.PitObject;

/**
 * ideally, this should be sent to clipboard... but it doesnt work
 * @author mfr
 */
public class PitClipboardVector implements Transferable{
    /** the data flavor we accept, string flavor */
    private DataFlavor flavors[] = { DataFlavor.stringFlavor };
    /** the contents of the clipboard. When we copy, we immediately convert
     * all objects to string format 
     */
    private String contents;
    
    /** Creates a new instance of PitClipboardVector */
    public PitClipboardVector(PitPanel panel) {
        //for each buttonview of the other panel
        Vector v = new Vector(panel.buttonviewsVector);
        v.addAll(panel.objectsVector);
        
        //we convert all to text now, to avoid getting newer references
        convertToText(v);
    }

    /** creates an instance of the clipboard vector from the vector. BUT only
     * falcon objects, buttonviews, buttons and sounds are copied
     */
    public PitClipboardVector(Vector vector) {
        int size = vector.size();
        Vector v = new Vector(size);
        Object po;
        for (int i=0;i<size;i++){
            po = vector.get(i);
            if (!(po instanceof PitObject)){
                continue;
            }
            else if ((po instanceof PanelHud) || 
                    (po instanceof PanelMFD) ||
                    (po instanceof PanelRWR)){
                continue;
            }
            else {
                v.add(po);
            }
        }
        
        //we convert all to text now, to avoid getting newer references
        convertToText(v);
    }
    
    /** converts all objects contained in the vector to text format */
    private void convertToText(Vector v){
        contents = new String();
        for (int i=0;i<v.size();i++){
             contents += ((PitObject)v.get(i)).toTextFormat() + PitObject.ls;
        }
    }
    
    public Object getTransferData(DataFlavor flavor){
        if (isDataFlavorSupported(flavor)) {
            //data favor supported
            return contents;
        }
        return null;
    }
    
    public DataFlavor[] getTransferDataFlavors(){
        return flavors;
    }
    
    public boolean isDataFlavorSupported(DataFlavor flavor){
        return (true);
    }
}

class PitObjectFlavor extends DataFlavor {
    
    /** Creates a new instance of DFlavor */
    public PitObjectFlavor() {
        this.setHumanPresentableName("Cockpit Editor Object");
    }
    
}








