package pitComponents.pitHelperComponents;

import java.util.Vector;

/** 
 * Optimizes storage and performance. Falcon can use a wide range of ids, so this
 * saves some space allocating dinamically and also is fast.
 */
public class PitComponentArray {
    private Object m[][] = new Object[1000][]; //1,000,000 objects
    private Vector pcVector = new Vector(100); //in case we need to run all elements
    
    /**
     * Builds an empty array
     */
    public PitComponentArray() {
    }
    
    /**
     * Adds an object to the array, in the given position. If position
     * is not allocated, allocates dynamically.
     *
     * @param o the object being added
     * @param id    the position in the array, usually the object id
     * @return 0 on error, 1 if all goes ok
     */
    public int add(Object o, int id) {
        if (id >= 1000000)
            return 0;
        int r1 = (id / 1000);
        if (m[r1] == null){
            m[r1] = new Object[1000];
        }
        int r2 = (id % 1000);
        if (m[r1][r2] == null){
            m[r1][r2] = o;
            pcVector.add(o);
            return 1;
        } else return 0;
    }
    
    /**
     * Gets the object in position id, returning it.
     *
     * @param id the object id
     * @return the object at that position, null if it does not exist.
     */
    public Object get(int id) {
        if ((id >= 1000000) || (id < 0)){
            return null;
        }
        int r1 = (id / 1000);
        int r2 = (id % 1000);
        if ((m[r1] == null)||(m[r1][r2] == null)){
            return null;
        } else return m[r1][r2];
    }
    
    /**
     * Gets a free id in the array. Given a base, tries to get the id as close
     * to it as possible. The base is usually a panel number, so panel objects
     * will be numbered close it.
     *
     * @param base  the base number, usually a panel id
     * @return  the id, or -1 if array is full
     */
    public int getFreeId(int base){
        for (int i=0;i<1000000;i++){
            base = (base + 1) % 1000000;
            if (get(base) == null){
                return base;
            }
        }
        //array full!! LOL!!
        return -1;
    }
    
    /**
     * Just like the get freeid, but instead, try to find one that is multiple of
     * 100, which seems to be the default for cockpits. Sorry cockpit makers, but
     * I cant allow you to choose the the id, else my program will be a whole mess
     *
     * @return the id of the panel, or -1 if there are no free ids for panel
     */
    public int getFreeIdBase100(){
        int base = 0;
        for (int i=0; i<10000;i+=100){
            if (get(i) == null){
                return base;
            }
        }
        //no free id for panel!
        return -1;
        
    }
    
    public void delete(int id){
        if (id >= 1000000)
            return;
        int r1 = (id / 1000);
        int r2 = (id % 1000);
        if ((m[r1] == null)||(m[r1][r2] == null)){
            return;
        }
        pcVector.remove(m[r1][r2]);
        m[r1][r2] = null;
    }
    
    /** returns the objects as a Vector */
    public Vector getObjects() {
        return pcVector;
    }
    
    /** returns the number of elements in this array. */
    public int getNumComponents() {
        return pcVector.size();
    }
    
    /** Returns the element at position index.
     * @param index the desired object position
     * @return the desired object, null if it position is invalid
     */
    public Object elementAt(int index) {
        try {
            return pcVector.get(index);
        } catch (Exception e){
            return null;
        }
    }
    
    
}
