/*
 * CantUndoException.java
 *
 * Created on June 3, 2005, 1:01 PM
 *
 * When an undo operation fails, it should throw this exception
 */

package actions;

/**
 *
 * @author matheus
 */
public class CantUndoException extends Exception{
    
    public CantUndoException() {
        super("Cant Undo Action");
    }

    public CantUndoException(String message) {
        super(message);
    }
    
}
