package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import windows.PropertiesDialog;


public class PitText
  extends PitObject {
      
      public int numstrings;
      
      public PitText(int id) {
          //pit text do not have src...
          super(id, 0, 1);
      }
      public Object clone(){
          PitText t = new PitText(0);
          super.cloneMeTo(t);
          t.numstrings = numstrings;
          return t;
      }
      
      
      public void parseData(PitTokenizer st) throws ParseErrorException {
          String token = null;
          try {
              do {
                  st.skipComment();
                  if (st.ttype != st.TT_WORD) {
                      throw new ParseErrorException("expecting word token");
                  }
                  
                  token = st.sval;
                  if (token.equals("#end")) {
                      break;
                  }
                  
                  st.nextToken();
                  if (st.ttype == '=') { //skip =
                      st.nextToken();
                  }
                  if ( (!token.endsWith("loc")) && (st.ttype != st.TT_NUMBER)) {
                      throw new ParseErrorException("expecting number token");
                  }
                  
                  if (token.equals("destloc")) {
                      destloc[0] = new PitArea();
                      destloc[0].parseData(st);
                  }
                  else if (token.equals("callbackslot")) {
                      callbackslot = (int) st.nval;
                  }
                  else if (token.equals("numstrings")) {
                      numstrings = (int) st.nval;
                  }
                  else if (token.equals("cyclebits")) {
                      cyclebits = st.parseHex();
                  }
                  
                  //get the ;
                  st.nextToken();
                  if (st.ttype != ';') {
                      throw new Exception("expecting ;");
                  }
              }
              while (true);
          }
          catch (Exception e) {
              throw new ParseErrorException("pitText.java: " + e);
          }
          
      }
      
      public String toTextFormat(){
          String text = new String();

          if (comments != null){
              text += comments;
          }
          text.concat("#" + id + " TEXT" + ls);
          if (cyclebits == -1){
              text += "\tcyclebits = -1;" + ls;
          }
          else{
              text += "\tcyclebits = 0x" + Long.toHexString(cyclebits) + ";" + ls;
          }
          text += "\tcallbackslot = " + callbackslot + ";" + ls;
          text += "\tnumstrings = " + numstrings + ";" + ls;
          text += "\tdestloc = " + destloc[0] + ";" + ls;
          text += "#END" + ls;
          return text;          
      }
      
      public void writeComponent(misc.RAFile arq) {
          try {
              if (comments != null){
                  comments.write(arq);
              }
              arq.writeln("#" + id + " TEXT");
              if (cyclebits == -1){
                  arq.writeln("\tcyclebits = -1;");
              }
              else{
                  arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                  ";");
              }
              arq.writeln("\tcallbackslot = " + callbackslot + ";");
              arq.writeln("\tnumstrings = " + numstrings + ";");
              arq.writeln("\tdestloc = " + destloc[0] + ";");
              arq.writeln("#END");
          }
          catch (IOException io) {
              System.out.println("pitText.java-> Erro de IO: " + io);
          }
      }
      
      public void draw(Graphics2D g) {
          destloc[0].draw(g);
          destloc[0].drawText("text", g);
      }
      
      public void drawLabeled(Graphics2D g) {
          destloc[0].draw(g);
          if (comments != null){
              destloc[0].drawText("text" + label, g);
          }
          else {
              destloc[0].drawText("text", g);
          }
      }
      
	  protected PropertiesDialog getPropertiesDialog(JFrame parent){
		  TextPropDialog tpd = new TextPropDialog(parent, this);
		  return tpd;
	  }

	  protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		  TextPropDialog tpd = new TextPropDialog(parent, this);
		  return tpd;
	  }
}

class TextPropDialog extends PropertiesDialog {
    PitText text;
    JTabbedPane tp = new JTabbedPane();

    //main
    LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);
    LabeledNumField callbackslot = new LabeledNumField(this, "callbackslot", 5);
    LabeledNumField numstrings =
      new LabeledNumField(this, "numstrings", "number of strings", 5);

    //areas: destloc
    AreasFieldPanel dest = new AreasFieldPanel(this, "destloc");

    //comments
    CommentsPane comments = new CommentsPane(this);

    
    public TextPropDialog(JFrame parent, PitText text){
        super(parent, text, "Text properties", false);
        setProperties(text);
    }

    
    public TextPropDialog(JDialog parent, PitText text){
        super(parent, text, "Text properties", true);
        setProperties(text);
    }
    
    private void setProperties(PitText text){
        this.text = text;
        this.setSize(400,400);
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("TEXT #" + text.id, JLabel.CENTER));

            mainP.add(cyclebits);
            mainP.add(callbackslot);
            mainP.add(numstrings);

            tp.add("main", mainP);
            tp.setToolTipTextAt(0, "main properties");
        }
        //areas
        {
            JPanel areasP = new JPanel(new GridLayout(8,1));

            areasP.add(dest);

            tp.add("areas", areasP);
            tp.setToolTipTextAt(1, "destination area");
        }
        //comments
        {
            tp.add("comms", comments);
            tp.setToolTipTextAt(2, "comments");
        }

        c.add(tp, BorderLayout.CENTER);

        if (!isNew){
            loadValues();
		}

    }

    protected void loadValues(){
        //main
        cyclebits.setValue(text.cyclebits);
        numstrings.setValue(text.numstrings);
        callbackslot.setValue(text.callbackslot);

        //areas
        dest.setEnabled(true);
        dest.setFields(text.destloc[0]);

        //comments
        comments.setComments(text.comments);
    }

    protected void setValues() throws Exception {
		//update main
		text.cyclebits = cyclebits.getValue();
		text.callbackslot = callbackslot.getValue();
		text.numstrings = numstrings.getValue();

		//areas
		text.destloc[0] = dest.getArea();

		//update comments
		text.comments = comments.getComments();
		if (text.comments != null){
			text.label = text.comments.getLabel();
		}
    }
}




