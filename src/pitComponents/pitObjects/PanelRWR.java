package pitComponents.pitObjects;

import java.io.*;
import pitComponents.pitObjects.*;
import pitComponents.pitHelperComponents.*;
import java.io.RandomAccessFile;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import windows.PropertiesDialog;

public class PanelRWR extends PitObject {
    public PanelRWR() {
        super(0, 0, 1); //one destination
        destloc[0] = new PitArea();
    }
    public PanelRWR(Rectangle r) {
        super(0, 0, 1); //one destination
        destloc[0] = new PitArea(r);
    }
    public Object clone(){
        PanelRWR rwr = new PanelRWR();
        this.cloneMeTo(rwr);
        return rwr;
    }
    public void writeComponent(misc.RAFile arq) throws java.lang.Exception {
        arq.writeln("\trwr = " + destloc[0] + ";");
    }

    public String toTextFormat() {
        return "\trwr = " + destloc[0] + ";" + ls;        
    }
    
    public void parseData(PitTokenizer st) throws pitComponents.ParseErrorException {
        destloc[0].parseData(st);
    }
    public void draw(Graphics2D g){
        destloc[0].draw(g);
        destloc[0].drawText("RWR", g);
    }
	
	
	protected PropertiesDialog getPropertiesDialog(JFrame parent){
		return new RwrPropDialog(parent, this);
	}

	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		return new RwrPropDialog(parent, this);
	}

}
class RwrPropDialog extends PropertiesDialog {
    private PanelRWR rwr; //the object we create

    // area fields text tips
    private static String fieldTips[] = {
      "Up Left X", "Up Left Y", "Down Right X", "Down Right Y"};

    //the textfields
    private JTextField
      //the dest text fields
      destFields[] = new JTextField[4];

    
    public RwrPropDialog(JFrame parent, PanelRWR rwr){
        super(parent, rwr, "RWR properties", false);
        setProperties(rwr);        
    }

    public RwrPropDialog(JDialog parent, PanelRWR rwr){
        super(parent, rwr, "RWR properties", true);
        setProperties(rwr);
    }
    
    
    private void setProperties(PanelRWR rwr){

        this.rwr = rwr;
        Container c = this.getContentPane();
        BorderLayout bl = new BorderLayout();
        c.setLayout(bl);
        {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
            p.add(new JLabel("RWR destination area"));
            c.add(p, BorderLayout.NORTH);
        }

        {
            //destP: the panel
            JPanel destP = new JPanel();

            destP.add(new JLabel("format: uplX  uplY  drX  drY"));

            //fields
            JPanel p2 = new JPanel();
            p2.setLayout(new FlowLayout(FlowLayout.LEFT));
            for (int j = 0; j < 4; j++) {
                destFields[j] = new JTextField();
                destFields[j].setColumns(4);
                destFields[j].setToolTipText(fieldTips[j]);
                p2.add(destFields[j]);
            }
            destP.add(p2);

            //sets the fields text
            destFields[0].setText(Integer.toString(rwr.destloc[0].upl.x));
            destFields[1].setText(Integer.toString(rwr.destloc[0].upl.y));
            destFields[2].setText(Integer.toString(rwr.destloc[0].dor.x));
            destFields[3].setText(Integer.toString(rwr.destloc[0].dor.y));

            //add the tabbed pane
            c.add(destP, BorderLayout.CENTER);

        }
        {
            //create/cancel buttons
            JPanel p = new JPanel();

            updateBt.addActionListener(this);
            p.add(updateBt);
            updateBt.setForeground(Color.green);

            closeBt.addActionListener(this);
            closeBt.setForeground(Color.red);
            p.add(closeBt);
            c.add(p, BorderLayout.SOUTH);
        }

    }
	
	protected void loadValues(){
		;
	}

	
	protected void setValues() throws Exception {
		Rectangle r = new Rectangle();
		r.x = Integer.parseInt(destFields[0].getText());
		r.y = Integer.parseInt(destFields[1].getText());
		r.width =
		  Integer.parseInt(destFields[2].getText()) - r.x + 1;
		r.height =
		  Integer.parseInt(destFields[3].getText()) - r.y + 1;
		if (r.width <= 0){
			throw new Exception("Negative width");
		}
		else if (r.height <= 0){
			throw new Exception("Negative height");
		}
		rwr.destloc[0] = new PitArea(r);
		parent.repaint();
	}	
}
