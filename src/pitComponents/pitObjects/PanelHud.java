package pitComponents.pitObjects;

import pitComponents.pitHelperComponents.*;
import pitComponents.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import windows.PropertiesDialog;
import java.awt.geom.*;

//like thge the PanelMFD, this is not a real pitobject, but share many of its characteristics
public class PanelHud extends PitObject {


    public PanelHud() {
        super(0, 0, 1); //no id, no src, one destloc
    }

    public PanelHud(Rectangle r){
        this();
        destloc[0] = new PitArea(r);
    }
    public Object clone(){
        PanelHud hud = new PanelHud();
        super.cloneMeTo(hud);
        return hud;
    }

    public void parseData(PitTokenizer st) throws ParseErrorException{
        destloc[0] = new PitArea();
        destloc[0].parseData(st);
    }

    public void writeComponent(misc.RAFile arq) throws IOException{
        arq.writeln("\thud = " + destloc[0] + ";");
    }
    
    public String toTextFormat(){
        return "\thud = " + destloc[0] + ";" + ls;
    }    
    
    /** gets the guncross position */
    public Point getGC(){
        Rectangle hr = this.destloc[0].rectangle;
        
        //guncross
        int gcx = hr.x + (hr.width / 2);
        int gcy = hr.y + (int)(hr.height / 5.3571428);
        
        return new Point(gcx, gcy);
    }
    
    public void draw(Graphics2D g){
        destloc[0].draw(g);
        destloc[0].drawText("HUD", g);
        //guncross
        Point gc = getGC();
        int gcx = gc.x;
        int gcy = gc.y;
/*        Rectangle hr = this.destloc[0].rectangle;
        
        int gcx = hr.x + (hr.width / 2);
        //Gc=((Yb-Yt)/5.3571428)+Yt
        int gcy = hr.y + (int)(hr.height / 5.3571428);*/
        
        Shape hl = new Line2D.Double(gcx-10, gcy, gcx + 10, gcy);
        Shape vl = new Line2D.Double(gcx, gcy-10, gcx, gcy + 10);
        g.draw(hl);
        g.draw(vl);
    }

	protected PropertiesDialog getPropertiesDialog(JFrame parent){
		HudPropDialog hpd = new HudPropDialog(parent, this);
		return hpd;
	}

	protected PropertiesDialog getNewPropertiesDialog(JDialog parent){
		HudPropDialog hpd = new HudPropDialog(parent, this);
		return hpd;
	}
}

class HudPropDialog extends PropertiesDialog {
	
    private PanelHud hud; //the object we create

    // area fields text tips
    private static String fieldTips[] = {
      "Up Left X", "Up Left Y", "Down Right X", "Down Right Y"};

    //the textfields
    private JTextField
      //the dest text fields
      destFields[] = new JTextField[4];
   
    public HudPropDialog(JFrame parent, PanelHud hud){
        super(parent, hud, "HUD properties", false);
        setProperties(hud);
    }
   
    public HudPropDialog(JDialog parent, PanelHud hud){
        super(parent, hud, "HUD properties", true);
        setProperties(hud);
    }
    
    public void setProperties(PanelHud hud){

        //window stuff
        this.hud = hud;
        Container c = this.getContentPane();
        BorderLayout bl = new BorderLayout();
        c.setLayout(bl);
        {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
            p.add(new JLabel("HUD destination area"));
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
		
		loadValues();

    }

	protected void loadValues(){
		//sets the fields text
		destFields[0].setText(Integer.toString(hud.destloc[0].upl.x));
		destFields[1].setText(Integer.toString(hud.destloc[0].upl.y));
		destFields[2].setText(Integer.toString(hud.destloc[0].dor.x));
		destFields[3].setText(Integer.toString(hud.destloc[0].dor.y));
	}
	
	protected void setValues() throws Exception {
		Rectangle r = new Rectangle();
		r.x = Integer.parseInt(destFields[0].getText());
		r.y = Integer.parseInt(destFields[1].getText());
		r.width = Integer.parseInt(destFields[2].getText()) - r.x + 1;
		r.height = Integer.parseInt(destFields[3].getText()) - r.y + 1;
		if (r.width <= 0){
			throw new Exception("Negative width");
		}
		else if (r.height <= 0){
			throw new Exception("Negative height");
		}
		hud.destloc[0] = new PitArea(r);
	}
}