/*
 * RAFile.java
 *
 * Created on March 1, 2005, 5:07 PM
 */

package misc;
import java.io.*;

/**
 *
 * @author mribeiro
 * Just like the RandomAccessFile class, but with a writeln function that
 * write bytes and add the right eol char
 */
public class RAFile extends RandomAccessFile {
    
    /// the line separator, system dependant
    private String eol = System.getProperty("line.separator");
    
    /** Creates a new instance of RAFile */
    public RAFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }
    
    /** Writes a line to the file, using the system eol character. Should help
     * with the eol headache windows/linux. Calls writebytes with the eol char
     * appended.
     *
     * @param line the line to be written, no eol char
     * @throws IOException if an error occurs
     */
    public void writeln(String line) throws IOException {
        super.writeBytes(line + eol);
    }
    
    
    
}
