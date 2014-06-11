package pitComponents.pitObjects;

import pitComponents.*;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitDialogComponents.*;

import java.awt.event.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import windows.PropertiesDialog;

/** the board at the pilots leg. */
public class PitKneeboard extends PitObject {

      public PitKneeboard(int id) {
          super(id, 0, 1);
      }
      public Object clone(){
          PitKneeboard k = new PitKneeboard(0);
          this.cloneMeTo(k);
          return k;
      }
      
      
      public void parseData(PitTokenizer st) throws ParseErrorException {
          String token = null;
          try {
              do {
                  //				st.nextToken();
                  st.skipComment();
                  if (st.ttype != st.TT_WORD) {
                      throw new ParseErrorException("expecting word token");
                  }
                  
                  token = st.sval;
                  if (token.equals("#end")) {
                      break;
                  }
                  
                  st.nextToken();
                  if (st.ttype == '=') {
                      //skip =
                      st.nextToken();
                  }
                  if ( (!token.endsWith("loc")) && (st.ttype != st.TT_NUMBER)) {
                      throw new ParseErrorException("expecting number token");
                  }
                  
                  if (token.equals("destloc")) {
                      destloc[0] = new PitArea();
                      destloc[0].parseData(st);
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
              //try to get the label
              if (comments != null){
                  label = comments.getLabel();
              }
              
          }
          catch (Exception e) {
              throw new ParseErrorException("pitKneeView.java: " + e);
          }
          
      }
      
      public void writeComponent(misc.RAFile arq) {
          try {
              if (comments != null){
                  comments.write(arq);
              }
              arq.writeln("#" + id + " KNEEVIEW");
              if (cyclebits == -1){
                  arq.writeln("\tcyclebits = -1;");
              }
              else{
                  arq.writeln("\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                  ";");
              }
              arq.writeln("\tdestloc = " + destloc[0] + ";");
              arq.writeln("#END");
          }
          catch (IOException io) {
              System.out.println("pitSound.java-> erro de IO: " + io);
          }
      }

      public String toTextFormat() {
          String s = new String();
          if (comments != null){
              s += comments;
          }
          s += "#" + id + " KNEEVIEW"+ls;
          if (cyclebits == -1){
              s += "\tcyclebits = -1;"+ls;
          } else{
              s += "\tcyclebits = 0x" + Long.toHexString(cyclebits) +
                      ";"+ls;
          }
          s += "\tdestloc = " + destloc[0] + ";"+ls;
          s += "#END"+ls;
          return s;
      }
      
      public void draw(Graphics2D g) {
          destloc[0].draw(g);
          destloc[0].drawText("Knee Board", g);
      }
      
      public void drawLabeled(Graphics2D g) {
          destloc[0].draw(g);
          if (label != null){
              destloc[0].drawText("Knee Board" + label, g);
          }
          else {
              destloc[0].drawText("Knee Board", g);
          }
      }
      
      public void drawTemplate(Graphics2D g) {
          
      }

	protected PropertiesDialog getPropertiesDialog(JFrame parent){
		KvPropDialog kpd = new KvPropDialog(parent, this);
		return kpd;
	}
	
	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		KvPropDialog kpd = new KvPropDialog(parent, this);
		return kpd;
	}
}

class KvPropDialog extends PropertiesDialog {
    PitKneeboard kb;
    JTabbedPane tp = new JTabbedPane();
    
    //main
    LabeledHexField cyclebits = new LabeledHexField(this, "cyclebits", 8);
    
    //areas
    AreasFieldPanel dest = new AreasFieldPanel(this, "destloc");

    //comments
    CommentsPane comments = new CommentsPane(this);

    public KvPropDialog(JFrame parent, PitKneeboard kb){
        super(parent, kb, "Kneeview Properties", false);
        setProperties(kb);
    }
    
    public KvPropDialog(JDialog parent, PitKneeboard kb){
        super(parent, kb, "Kneeview Properties", true);
        setProperties(kb);
    }
    
    private void setProperties(PitKneeboard kb){
        this.kb = kb;
        this.setSize(400,400);
        Container c = this.getContentPane();
        tp.setTabPlacement(tp.LEFT);

        //main tab
        {
            JPanel mainP = new JPanel(new GridLayout(8,1));
            mainP.add(new JLabel("KNEEVIEW #" + kb.id, JLabel.CENTER));

            mainP.add(cyclebits);

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

        if (!isNew)
            loadValues();

    }

    public void loadValues(){
        //main
        cyclebits.setValue(kb.cyclebits);

        //areas
        dest.setEnabled(true);
        dest.setFields(kb.destloc[0]);

        //comments
        comments.setComments(kb.comments);
    }

    protected void setValues() throws Exception {
		//update main
		kb.cyclebits = cyclebits.getValue();

		//areas
		kb.destloc[0] = dest.getArea();
		if (kb.destloc[0] == null){
			JOptionPane.showMessageDialog(this, "invalid destloc");
			throw new Exception("invalid destloc");
		}

		//update comments
		kb.comments = comments.getComments();
		if (kb.comments != null){
			kb.label = kb.comments.getLabel();
		}
    }
}


