/*
 * CantExecuteException.java
 *
 * Created on 21 de Setembro de 2005, 11:38
 */

package actions;

/**
 *
 * @author  matheus
 */
public class CantExecuteException extends Exception {
    
    /** Creates a new instance of CantExecuteException */
    public CantExecuteException() {
        super("Cant execute action");
    }

    public CantExecuteException(String message) {
        super(message);
    }
    
}
