/*
 * ActionInterface.java
 *
 * Created on June 3, 2005, 12:59 PM
 *
 * ActionsInterface describes an action in the program, such as moving,
 * deleting, creating, editing an object. There are two reasons for this:
 * we can undo/redo stuff and encapsulate the actions
 */

package actions;

/**
 *
 * @author matheus 
 * An interface for action objects. Actions must implement both of the functions
 * below
 */
public interface ActionInterface {
    public void execute() throws CantExecuteException;
    public void undo() throws CantUndoException;
    public void redo() throws CantRedoException;
}
