package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;


import java.io.*;
import javax.swing.*;

//DONE!!!!!!!

public class PitSound {

    int entry, id;
    public PitComment comments;
    
    public PitSound(int id) {
        this.id = id;
    }
    public Object clone(){
        return null;
    }
    
    
    public void parseData(PitTokenizer st) throws ParseErrorException {
        try {
            
            st.skipComment();
            if (!st.sval.equals("entry")) {
                throw new Exception("expecting entry");
            }
            
            st.nextToken();
            if (st.ttype == '=') { //skip equal
                st.nextToken();
            }
            if (st.ttype != st.TT_NUMBER) {
                throw new Exception("Expecting number for entry");
            }
            
            entry = (int) st.nval;
            st.nextToken();
            if (st.ttype != ';') {
                throw new Exception("missing ;");
            }
            
            //gets the #end
            st.skipComment();
            if ( (st.ttype != st.TT_WORD) || (!st.sval.equals("#end"))) {
                throw new Exception("expecting #end");
            }
            
        }
        catch (Exception e) {
            throw new ParseErrorException("pitSound.java: " + e);
        }
    }
    
    public void writeComponent(misc.RAFile arq) {
        try {
            if (comments != null){
                comments.write(arq);
            }
            arq.writeln("#" + id + " SOUND");
            arq.writeln("\tentry = " + entry + ";");
            arq.writeln("#END");
        }
        catch (IOException io) {
            System.out.println("pitSound.java-> erro de IO: " + io);
        }
    }

    public String toTextFormat() {
        String ls = PitObject.ls;
        String s = new String();
        if (comments != null){
            s += comments;
        }
        s += "#" + id + " SOUND" + ls;
        s += "\tentry = " + entry + ";" + ls;
        s += "#END" + ls;
        return s;
    }

    
    public void showProperties(JFrame parent){
        
    }
    
}

