package pitComponents.pitHelperComponents;

import pitComponents.*;
import java.util.*;
import java.io.*;
import pitComponents.pitObjects.PitObject;


/**
 * A comment is just that, a comment. Falcon does not use it for nothing, so all
 * comment lines are ignored. This program uses the comment as a label to objects.
 * We cant ignore them because users use them for manually changing things.
 *
 * <p>All comments begin with //. Comments in the form name: are used to label
 * objects. So if an object has a comment like //name: "object bla", it will be
 * draw with a label "object bla" in labeled mode(L key).
 */
public class PitComment {
    ///each line of comment is an entry in this vector
    Vector comments;
    ///we need this for line count in the file, its the total number of comment lines
    public static int numCommentsRead = 0; 

    /**
     * Creates a new comment object, empty
     */
    public PitComment() {
        comments = new Vector(3);
    }

    /**
     * Clones a comment
     *
     * @return the new comment, a clone of the older one.
     */
    static public PitComment clone(PitComment pc){
        if (pc==null)
            return null;
        PitComment npc = new PitComment();
        for (int i=0;i<pc.comments.size();i++){
            npc.comments.add(pc.comments.get(i));
        }
        return npc;
    }

    /**
     * Adds a line of comment to this PitComment object.
     *
     * @param s the line string
     */
    public void add(String s){
        comments.add(s);
    }

    /**
     * Writes the comment to the file. All
     *
     * @param arq the destination file, must be opened and in the right position
     */
    public void write(misc.RAFile arq) throws IOException{
        //write comments
        arq.writeln("");
        for (int i = 0; i < comments.size(); i++) {
            String s = (String)comments.get(i);
            s = s.trim();            
            if (s.startsWith("//")){
                s = s.substring(2);
            }
            s = s.trim();
            arq.writeln("//" + s);            
        }
    }

    /**
     * Reads the comment from a tokenizer.
     *
     * @param static the tokenizer
     * @throws if a parse error occurs, i wonder how.
     */
    public void readComments(PitTokenizer st) throws Exception{
        char tst;
        st.nextToken();
        tst = (char)st.ttype;
      
        while (st.ttype == '/') {
            st.nextToken(); // gets second slash
            if (st.ttype == '/') {
                st.commentState();
                //we are reading a comment
                String comment = new String("//");
                st.nextToken();
                tst = (char)st.ttype;
                //we read till eol
                while (! ( (st.ttype == StreamTokenizer.TT_EOL) ||
                           (st.ttype == StreamTokenizer.TT_EOF)
                       )) {
                    comment += ((char)st.ttype);
                    st.nextToken();
                    tst = (char)st.ttype;
                }
                numCommentsRead++;
                comments.add(comment);
            }
            else {
                throw new ParseErrorException(
                  "PitComment.java: missing / on comment");
            }
            st.normalState();
            st.nextToken();
            tst = (char)st.ttype;
        }

    }

    /**
     * a label is everything after the "name:" in the comment. Its used to draw
     * the object in labeled mode(L key).
     *
     * @return the label string
     */
    public String getLabel(){
        String s = null;
        for (int i=0;i<comments.size();i++){
            String saux = ((String)comments.get(i)).toString();
            int j = saux.toLowerCase().indexOf("name:");
            if (j != -1){
                s = saux.substring(j+5).trim();

                break;
            }
        }
        return s;
    }

    /**
     * Converts the comment to the human readble format. Does not check for //
     * in the beginning, but trim it. This is used to show the comments in the
     * property dialogs.
     *
     * @return the string representation of the comment
     */
    public String toString(){
        String s = new String();
        for (int i=0;i<comments.size();i++){
            Object o =  comments.get(i);
            String aux = (String)o;
            aux.trim();
            s = s + aux + PitObject.ls;
        }
        return s;
    }
    
    /** this is the same as toString, just to make function calls uniform
     */
    public String toTextFormat(){
       return toString();  
    }


}