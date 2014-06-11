/*
 * CantRedoException.java
 *
 * Created on June 3, 2005, 1:02 PM
 *
 * When a redo operation fails, it should throw this
 */

package actions;

/**
 *
 * @author matheus
 */
public class CantRedoException extends Exception {
    
    /** Creates a new instance of CantRedoException */
    public CantRedoException() {
        super("Cant Redo Action");
    }

    public CantRedoException(String message) {
        super(message);
    }
    
    
}
