package pitComponents.pitHelperComponents;

import pitComponents.*;
import java.io.*;
import javax.swing.JTextArea;

/** Represents a file reference. Just like a file, but with the aditional operations
 * for falcon files
 */
public class PitFile {
    private String name;
    
    public PitFile() {
    }
    
    public PitFile(String filename){
        name = new String(filename);
    }
    
    /** clones the given PitFile, returning a new one with the same values.
     *
     * @param file the PitFile to be cloned
     * @returns the clone PitFile
     */
    public static PitFile clone(PitFile file){
        if (file == null)
            return null;
        
        PitFile f = new PitFile();
        f.name = new String(file.name);
        return f;
    }
    
    //we receive everything after the =
    public void parseData(StreamTokenizer st) throws ParseErrorException {
        name = new String();
        try {
            //well loop till we get a ;
            // and concatenate everything
            
            do {
                if (st.ttype == ';') {
                    break;
                }
                
                switch (st.ttype) {
                    case (StreamTokenizer.TT_NUMBER):
                        name = name.concat(Integer.toString( (int) st.nval));
                        break;
                    case (StreamTokenizer.TT_WORD):
                        name = name.concat(st.sval);
                        break;
                    default:
                        name = name.concat(Character.toString( (char) st.ttype));
                }
                
                st.nextToken();
            }
            while (true);
            st.pushBack();
        } catch (Exception e) {
            throw new ParseErrorException("pitFile.java: " + e.toString());
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String newName){
        name = new String(newName);
    }
}