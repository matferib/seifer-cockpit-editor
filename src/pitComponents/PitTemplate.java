package pitComponents;

import cockpitEditor.UsPrListener;
import pitComponents.pitHelperComponents.*;
import pitComponents.pitObjects.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import cockpitEditor.UsPr;
import javax.imageio.ImageIO;
import pitComponents.pitDialogComponents.LabeledField;
import pitComponents.pitDialogComponents.LabeledFloatFields;
import windows.NewMainWindow;
import windows.PropertiesDialog;

public class PitTemplate extends JPanel implements MouseListener, MouseMotionListener, UsPrListener {
	// mouse stuff
	/** last position */
	private Point lastp;
	/** operation mouse is doing */
	private int operation;

	/** the size of this template. The resolution is taken from the file itself if
	 * it exists, else its parsed from the dat file
	 */
	public Dimension resolution = new Dimension();

	/** the template background file */
	protected PitFile file;

	/** If false, will be loaded before next use */
	protected boolean loaded = false;

	/** there are 5 types of image, the original template, 
	 *	inst and flood applied (for all objects but lights)
	 *  flood and max brightness on last (for light objects)
	 *  and transparent version of the last 2.
	 */
	private BufferedImage images[] = new BufferedImage[5];

	/** constants indicating the type of images. IM_ORIGINAL should
	 * only used internally.
	 */
	public static final int IM_ORIGINAL=0, IM_FLOOD_INST=1, 
				 IM_FLOOD=2, IM_FLOOD_INST_T=3, IM_FLOOD_T=4;

	/* constructs a template, with empty file and no images at all. */
	public PitTemplate() {
		file = new PitFile();
		setOpaque(false);        
		addMouseListener(this);
		addMouseMotionListener(this);
		UsPr.addUsPrListener(this);
	}

	// parsing: when we read the manager
	public void parseData(StreamTokenizer st) throws ParseErrorException {
		try {
			resolution.width = (int) st.nval;
			st.nextToken();
			resolution.height = (int) st.nval;
			st.nextToken();
			file.parseData(st);
			//load();
		}
		catch (Exception e) {
			throw new ParseErrorException("PitTemplate.java: " + e);
		}
	}

	//save stuff
	public void writeComponents(misc.RAFile f) {
		try {
			f.writeln("\ttemplatefile = " +
					resolution.width + " " + resolution.height +
					" " + file.getName() + ";");
		}
		catch (Exception e) {
			System.out.println("PitTemplate.java: erro de IO " + e);
		}
	}

	/** Checks the template. Sees if the file size is right to the declared size,
	 * and if template image exists.
	 *
	 * @return the string describing the error, empty otherwise.
	 */
	public String check(){
		String s = new String();
		File f = new File(Cockpit.cwd, file.getName());
		if (!f.exists()){
			s = s.concat("Template image " + file.getName() + " not found\n!");
		}
		return s;
	}

	/** Gets the file associated with this template.
	 *
	 * @return the file associated with this template
	 */
	public PitFile getPitFile() {
		return file;
	}

	/** flushes all images from template
	 */
	public void flush(){        
		for (int i=0; i < images.length; i++){
			if (images[i] != null){
				images[i].flush();
				images[i] = null;
			}
		}
	}

	/** 
	 * Loads both template images(transparent and normal). 
	 * If they cant be loaded, return immediatly
	 * setting both to null. We cant load templates like the panels, because we
	 * need to grab pixels from them.
	 * This call releases previous allocated memory
	 */
	public void load() {
		loaded = true;
		flush();
		try {
			images[IM_ORIGINAL] = javax.imageio.ImageIO.read(
				new File(Cockpit.cwd + "/" + getPitFile().getName())
			);
		}
		catch (Exception e){
			for (int i=0;i<images.length;i++){
				images[i] = null;
			}
			changeZoom(UsPr.getZoom(), null);
			return;
		}
		int w = images[IM_ORIGINAL].getWidth(),
				h = images[IM_ORIGINAL].getHeight();

		resolution = new Dimension(w, h);
		//get the color model of the image, so we can create the transparent.
		PixelGrabber pg =
			new PixelGrabber(images[IM_ORIGINAL], 0, 0, w, h, false);
		try {
			pg.grabPixels();
		} catch (Exception e){
			System.out.println("PitTemplate.java: error grabbing pixel");
		}

		// get palette from original image
		int oPal[] = new int[256];

		// original and derivated color models 
		IndexColorModel cm[] = new IndexColorModel[5];
		cm[IM_ORIGINAL] = (IndexColorModel)pg.getColorModel();
		cm[IM_ORIGINAL].getRGBs(oPal);

		// derivated palette
		int dPal[] = new int[256];

		// get lights
		float flood[] = Cockpit.getFloodF();
		float inst[] = Cockpit.getInstF();
		float amb[] = UsPr.getAmbientLightF();

		// lower colors, ambient + flood
		// l will be applied to them
		float l[] = new float[3];
		for (int i=0;i<3;i++){
			l[i] = flood[i] + amb[i];
			// never highter than 1.0f
			if (l[i] > 1.0f){
				l[i] = 1.0f;
			}
		}
		for (int i=0;i<(256-48);i++){
			// remember: colors are stored as BGR
			int r = (int)(((oPal[i] & 0xFF0000) >> 16) * l[0]);
			int g = (int)(((oPal[i] & 0x00FF00) >> 8) * l[1]);
			int b = (int)(((oPal[i] & 0x0000FF)) * l[2]);

			// store on derivated palette
			dPal[i] = (r << 16) | (g << 8) | (b);
		}
		// higher colors, ambient + flood + inst
		for (int i=0;i<3;i++){
			l[i] += inst[i];
			// never highter than 1.0f
			if (l[i] > 1.0f){
				l[i] = 1.0f;
			}
		}
		for (int i=(256-48);i<256;i++){
			// remember: colors are stored as BGR
			int r = (int)(((oPal[i] & 0xFF0000) >> 16) * l[0]);
			int g = (int)(((oPal[i] & 0x00FF00) >> 8) * l[1]);
			int b = (int)(((oPal[i] & 0x0000FF)) * l[2]);

			// store on derivated palette
			dPal[i] = (r << 16) | (g << 8) | (b);
		}

		// create color models for IM_FLOOD_INST and IM_FLOOD_INST_T
		//transparent color model
		cm[IM_FLOOD_INST] = new IndexColorModel(
				8, //bits
				256,// colors
				dPal, //palette
				0, // start offset
				false, //no alpha
				-1, // no transparent index
				DataBuffer.TYPE_BYTE //should we use short instead?  maybe faster
				);

		cm[IM_FLOOD_INST_T] = new IndexColorModel(
				8, //bits
				256,// colors
				dPal, //palette
				0, // start offset
				false, //no alpha
				0, //transparent index
				DataBuffer.TYPE_BYTE //should we use short instead?  maybe faster
				);

		// create the derivated palette for light objects
		// lower colors are the same as the derivated we created above
		// higher colors are used in full original brightness
		int dlPal[] = (int[])dPal.clone();
		for (int i=(256-48);i<256;i++){
			dlPal[i] = oPal[i];
		}	

		// create color models for IM_FLOOD and IM_FLOOD_T
		cm[IM_FLOOD] = new IndexColorModel(
				8, //bits
				256,// colors
				dlPal, //palette
				0, // start offset
				false, //no alpha
				-1, // no transparent index
				DataBuffer.TYPE_BYTE //should we use short instead?  maybe faster
				);

		cm[IM_FLOOD_T] = new IndexColorModel(
				8, //bits
				256,// colors
				dlPal, //palette
				0, // start offset
				false, //no alpha
				0, //transparent index
				DataBuffer.TYPE_BYTE //should we use short instead?  maybe faster
				);


		//flood + inst images
		images[IM_FLOOD_INST] = new BufferedImage(
				w, h,
				BufferedImage.TYPE_BYTE_INDEXED, cm[IM_FLOOD_INST]
				);
		images[IM_FLOOD_INST_T] = new BufferedImage(
				w, h,
				BufferedImage.TYPE_BYTE_INDEXED, cm[IM_FLOOD_INST_T]
				);

		//flood + full brightness
		images[IM_FLOOD] = new BufferedImage(
				w, h,
				BufferedImage.TYPE_BYTE_INDEXED, cm[IM_FLOOD]
				);
		images[IM_FLOOD_T] = new BufferedImage(
				w, h,
				BufferedImage.TYPE_BYTE_INDEXED, cm[IM_FLOOD_T]
				);

		//fill the images with the original content
		DataBuffer buf[] = new DataBuffer[5];
		for (int i=0;i<5;i++){
			buf[i] = images[i].getRaster().getDataBuffer();
		}
		int oSize = buf[IM_ORIGINAL].getSize(); // original size
		// copy from original to other ones
		for (int i=0;i<oSize;i++){
			for (int j=1;j<images.length;j++){
				buf[j].setElem(i, buf[IM_ORIGINAL].getElem(i));
			}
		}
		changeZoom(UsPr.getZoom(), null);
	}

	/** Marks template as unload, so it will be loaded on next draw. */
	public void unload(){
		loaded = false;
	}

	/** returns the given image type. 
	 * Checks for valid index, 0 is not valid (internally only) */
	public Image getImage(int type){
		if ((type<=0) || (type>5)){
			return null;
		}

		return images[type];
	}


	/* returns the object at XY location in the template */
	public PitObject getXY(Point p) {
		PitPanel cp = Cockpit.currentPanel;
		if (cp == null)
			return null;

		PitObject po = null, pres = null, dial=null;
		//try to get selected object first
		if ((po = Cockpit.getSelectedObjectsXY(p, true)) != null){
			return po;
		}

		// highest priority: buttonviews
		for (int i = 0; i < Cockpit.currentPanel.buttonviewsVector.size(); i++) {
			po = (PitObject) Cockpit.currentPanel.buttonviewsVector.get(i);
			if (po.pointIsInside_template(p)) {
				return po;
			}
		}

		//second: objects
		for (int i = 0; i < Cockpit.currentPanel.objectsVector.size(); i++) {
			po = (PitObject) Cockpit.currentPanel.objectsVector.get(i);
			if (po.pointIsInside_template(p)) {
				if (po instanceof PitLight) {
					pres = po; //lights are low priority
				}
				else if (po instanceof PitDial) {
					dial = po; //lights are the last
				}
				else {
					return po;
				}
			}
		}

		//fourth: lights, because they can occupy the whole screen
		if (pres != null){
			return pres;
		}
		else {
			//last dials cause they dont have a src, although it present in the .dat
			return dial;
		}
	} 


	/** centers the viewport on the current selected area
	 * @param po object to center template on
	 */
	public void centerOn(PitObject po){
		if ((po == null)||(po.srcloc==null)) return;

		int src = (po.currentSelectedSrc == -1)?0:po.currentSelectedSrc;
		Point point = (Point) po.srcloc[po.currentSelectedSrc].getCenter();
		point.x *= UsPr.getZoom();
		point.y *= UsPr.getZoom();

		JViewport v = (JViewport)this.getParent();
		point.x -= (v.getWidth() / 2);
		point.y -= (v.getHeight() / 2);

		if (point.x < 0)
			point.x = 0;
		if (point.y < 0)
			point.y = 0;

		v.setViewPosition(point);
	}
	/*******************************************************/

	/** Shows template properties. Called by the template window when shift P 
	 * is pressed. Template properties are bgfile and template size
	 *
	 * @param parent the frame calling
	 */
	public void showProperties(JFrame parent){
		TemplatePropDialog tpd = new TemplatePropDialog(parent, this, false);
		tpd.setVisible(true);
	}

	/** uspr listener for zoom */
	public void zoomChanged(double zoom){
		changeZoom(zoom, null);
	}    

	/** Called when the zoom factor is changed.
	 * @param po object to center on after zoom
	 */
	public void changeZoom(double zoom, PitObject po){
		//when we change zoom, we must resize our size
		Dimension d = new Dimension(
				(int)(zoom*resolution.width), 
				(int)(zoom*resolution.height)
				);

		this.setPreferredSize(d);
		this.setSize(d);

		// if theres a selected object, center on it
		if (po != null){
			centerOn(po);
		}
		repaint();
	}

	/* mouse stuff ******************************************/
	public void mouseClicked(MouseEvent e){
		NewMainWindow.setActive(NewMainWindow.ACTIVE_TEMPLATE);
		double zoom = UsPr.getZoom();
		Point p = e.getPoint();
		p.x /= zoom;
		p.y /= zoom;

		//gets the object at this position in template
		PitObject po = getXY(p);
		if (po == null) {
			Cockpit.removeAllSelectedObjects();
		} 
		else {
			Cockpit.setSelectedObject(po);
			Cockpit.centerOnSelectedObject(true, false);
		}
	}

	public void mouseEntered(MouseEvent e){
	}
	public void mouseExited(MouseEvent e){
	}
	public void mousePressed(MouseEvent e){
		double zoom = UsPr.getZoom();
		Point p = e.getPoint();
		p.x /= zoom;
		p.y /= zoom;

		lastp = p;
		PitObject po = getXY(lastp); //gets the object
		PitPanel cp = Cockpit.currentPanel;
		if (cp ==null){
			return;
		}
		int size = Cockpit.getSelectedObjectsSize();
		if (size == 0){
			return;
		}
		//is the point inside a selected object template?
		boolean isInside  = false;
		for (int i=0;i<size;i++){
			if (po == Cockpit.getSelectedObjectAt(i)){
				isInside = true;
				break;
			}
		}       

		if ((isInside)&&(po != null)){
			if ((e.getModifiersEx() & e.BUTTON3_DOWN_MASK) != 0){
				//right button, move all src
				operation = -1;
			}
			//if shift is held, we always move, also if more than one object selected
			else if (((e.getModifiersEx() & e.SHIFT_DOWN_MASK) != 0) || (size > 1)){
				operation = 0;
			}
			//else if ctrl is held, we always resize
			else if ((e.getModifiersEx() & e.CTRL_DOWN_MASK) != 0){
				operation = po.getResizeCornerInTemplate(lastp);
			}
			//else, it depends on where we click
			else{
				operation = po.getOperationInTemplate(lastp);
			}
		} 
		else {
			Cockpit.removeAllSelectedObjects();
			cp.repaint();
			repaint();
		}
	}
	public void mouseReleased(MouseEvent e){
	}

	public void mouseDragged(MouseEvent e){
		double zoom = UsPr.getZoom();
		PitPanel cp = Cockpit.currentPanel;
		if (cp ==null)
			return;
		int size = Cockpit.getSelectedObjectsSize();
		if (size == 0)
			return;

		Point p = e.getPoint();
		p.x /= zoom;
		p.y /= zoom;

		//we only drag selected objects
		Point newp = p;
		if (operation == -1){
			for (int i=0;i<size;i++){
				PitObject po = Cockpit.getSelectedObjectAt(i);
				po.moveAllInTemplate(newp.x - lastp.x, newp.y-lastp.y);
			}            
		}
		else if (operation == 0){
			for (int i=0;i<size;i++){
				PitObject po = Cockpit.getSelectedObjectAt(i);
				po.moveInTemplate(newp.x - lastp.x, newp.y-lastp.y);
			}
		}
		else{
			PitObject po = Cockpit.getSelectedObjectAt(0);
			po.resizeInTemplate(newp.x - lastp.x, newp.y-lastp.y, operation);
		}
		lastp = newp;
		repaint();
		Cockpit.currentPanel.repaint();
	}

	public void mouseMoved(MouseEvent e){
		double zoom = UsPr.getZoom();
		PitObject po;
		//adjust point
		Point  p = e.getPoint();
		p.x /= zoom;
		p.y /= zoom;

		//show mouse coords
		//bb.setMouseCoordinates(p);

		//if we are near a border, change cursor
		//we do this only for 1 selected object
		PitPanel panel = Cockpit.currentPanel;
		if (panel == null){
			setCursor(Cursor.getDefaultCursor());
			return;
		}
		int size = Cockpit.getSelectedObjectsSize();
		if (size == 1){
			po = Cockpit.getSelectedObjectAt(0);
			setCursor(po.getCursorInTemplate(p));
		}
		else {
			setCursor(Cursor.getDefaultCursor());
		}
	}
	/* end mouse ****************************************/

	/**
	 * Draws the template in the given graphics context. Drawing will take
	 * zoom into account. Also, all selected objects are drawn, and labeled.
	 */
	public void paintComponent(Graphics g) {
		// loads if necessary
		if (!loaded){
			load();
		}

		double zoom = UsPr.getZoom();
		Graphics2D g2 = (Graphics2D)g;
		g2.scale(zoom, zoom);
		PitPanel cp = Cockpit.currentPanel;

		Image image = getImage(PitTemplate.IM_FLOOD_INST);
		if (image != null){
			g.drawImage(image, 0, 0, this);
		}

		//we draw each selected object here
		if (cp != null){
			int size = Cockpit.getSelectedObjectsSize();
			for (int i=0;i<size;i++){
				PitObject o = Cockpit.getSelectedObjectAt(i);
				Color otherAreasColor, selectedAreaColor;

				g2.setStroke(UsPr.regularStroke);
				if (UsPr.drawAlpha){
					g2.setColor(UsPr.templateColorAlpha);                
				}
				else {
					g2.setColor(UsPr.templateColor);
				}

				if (UsPr.drawLabel) {
					o.drawTemplateLabeled((Graphics2D)g);
				}
				else {
					o.drawTemplate((Graphics2D)g);
				}

				g2.setStroke(UsPr.selectedStroke);

				//now we draw the selected area
				if (UsPr.drawLabel) {
					o.drawSelectedSrcTemplateLabeled((Graphics2D)g);
				} else {
					o.drawSelectedSrcTemplate((Graphics2D)g);
				}
			}
		}
	}	
}


/** the template bottom bar, where we draw mouse coordinates */
class TemplateBB extends JPanel {
	private PitTemplate template;
	private JLabel bbMouseCoordinates = new JLabel("", JLabel.CENTER);
	private JLabel bbSelectedAreaCoordinates = new JLabel("", JLabel.CENTER);
	private JLabel bbDescription = new JLabel("", JLabel.CENTER);

	public TemplateBB(PitTemplate template){
		this.template = template;
		setMinimumSize(new Dimension(1, 30));
		//mouse and object coordinates
		setLayout(new GridLayout(2, 1));
		this.add(bbDescription);
		JPanel aux = new JPanel(new GridLayout(1, 2));
		aux.add(bbSelectedAreaCoordinates);
		aux.add(bbMouseCoordinates);
		this.add(aux);
		this.setOpaque(false);
	}

	public void setMouseCoordinates(Point p){
		bbMouseCoordinates.setText("< x: " + p.x + " y: " + p.y + " >");
		this.repaint();
	}

	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D)g;

		//no current panel, we dont write description nor selected area
		if (Cockpit.currentPanel == null){
			bbDescription.setText("");
			bbSelectedAreaCoordinates.setText("");
		}

		int size = Cockpit.getSelectedObjectsSize();
		//we show cooridnates only for 1 selected object
		if (size != 1){
			bbSelectedAreaCoordinates.setText("");
			bbDescription.setText("Panel: " + Cockpit.currentPanel.id);
			return;
		}

		//if we get here, we got a single selected object
		PitObject po = Cockpit.getSelectedObjectAt(0);
		//the object coordinates
		if ((po.srcloc == null) || (po.srcloc[po.currentSelectedSrc] == null)){
			bbSelectedAreaCoordinates.setText("");
		} 
		else {
			Rectangle r = po.srcloc[po.currentSelectedSrc].rectangle;
			bbSelectedAreaCoordinates.setText("coordinates: < " +
					r.x + " " +
					r.y + ", " +
					(r.x + r.width - 1) + " " +
					(r.y + r.height - 1) + " >");
		}

		//now we get general finromation
		String obType = null;
		//panel objects
		if (po instanceof PanelHud){
			obType = "HUD ";
		}
		else if (po instanceof PanelMFD){
			obType = ((PanelMFD)po).mySuf +  " MFD ";
		}
		else if (po instanceof PanelRWR){
			obType = "RWR ";
		}
		else {
			//the objects
			if (po instanceof PitButtonView){
				obType = "buttonview ";
			} else if (po instanceof PitADI){
				obType = "ADI(obj) ";
			} else if (po instanceof PitChevLift){
				obType = "Chevron/Liftline(obj) ";
			} else if (po instanceof PitDED){
				obType = "DED(obj) ";
			} else if (po instanceof PitDial){
				obType = "dial(obj) ";
			} else if (po instanceof PitDigits){
				obType = "digits(obj) ";
			} else if (po instanceof PitHSI){
				obType = "hsi(obj) ";
			} else if (po instanceof PitIndicator){
				obType = "indicator(obj) ";
			} else if (po instanceof PitKneeboard){
				obType = "kneeview(obj) ";
			} else if (po instanceof PitLight){
				obType = "light(obj) ";
			} else if (po instanceof PitMachASI){
				obType = "machASI(obj) ";
			} else if (po instanceof PitText){
				obType = "text(obj) ";
			}
			//the id of object
			obType = obType.concat(Integer.toString(po.id));
		}

		bbDescription.setText("Panel: " + Cockpit.currentPanel.id + ", " + obType);


	}
}

/** represents the template properties. A template has dimension and a gif file. */
class TemplatePropDialog extends PropertiesDialog {

	/** buttons: auto for auto template size and
	 */
	private JButton autoBt = new JButton("Auto");


	/** a reference to the cockpit template */
	private PitTemplate template;

	/** the gif file */
	private LabeledField bgField = new LabeledField(this, "Background", 
			"The gif file to be loaded as template background, usually its is named" +
			" as XX_tmplt.gif, where XX are the 2 initials of cockpit resolution", 
			10, "Browse");

	/** template dimensions */
	private LabeledFloatFields dimFields = new LabeledFloatFields(this, "Template size", 2, 6);

	public TemplatePropDialog(JFrame parent, PitTemplate t, boolean isNew){
		super(parent, null, "Template Properties", isNew);
		setProperties(t);
	}

	public TemplatePropDialog(JDialog parent, PitTemplate t, boolean isNew){
		super(parent, null, "Template Properties", isNew);
		setProperties(t);
	}

	private void setProperties(PitTemplate t){
		this.template = t;
		Container c = this.getContentPane();

		//listeners
		bgField.addActionListener(this);
		updateBt.addActionListener(this);
		autoBt.addActionListener(this);

		//main tab
		{
			JPanel mainP = new JPanel(new GridLayout(8,1));
			mainP.add(new JLabel("Template", JLabel.CENTER));
			mainP.add(bgField);
			mainP.add(dimFields);
			c.add(mainP);
		}

		if (!isNew)
			loadValues();
	}

	protected void loadValues(){
		//main
		{
			//bg image
			bgField.setText(template.file.getName());
			//dimensions
			int dim[] = new int[2];
			dim[0] = template.resolution.width;
			dim[1] = template.resolution.height;
			dimFields.setFields(dim);
		}
	}

	protected void setValues() throws Exception {
		//update main
		{
			//get new image
			String s = bgField.getText();
			template.file = new PitFile(s);

			//get new dimension from image if it exists
			BufferedImage image = null;
			try {
				image = ImageIO.read(new File(s));
				template.resolution = new Dimension(image.getWidth(), image.getHeight());
			}
			catch (IOException io){
			//else, we get user dimensions
				int dim[] = dimFields.getFieldsAsInteger();
				template.resolution = new Dimension(dim[0], dim[1]);
			}

			//free image
			if (image != null){
				image.flush();
			}

			//load template
			template.load();
			template.repaint();
		}
	}
	

	protected boolean processActionEvent(ActionEvent e){
		Object o = e.getSource();
		if (o == bgField){
			//browse window for gif selection

			//open the dialog so one can choose the file
			JFileChooser chooser = new JFileChooser(Cockpit.cwd);

			GifFilter gf = new GifFilter();
			chooser.setFileFilter(gf);
			chooser.showOpenDialog(this);
			File f = chooser.getSelectedFile();
			if (f == null){
				return true;
			}

			//now we set the field to the image name and the size according
			String name = f.getName();
			bgField.setText(name.toLowerCase());

			//read the image
			BufferedImage image = null;
			try{
				image = ImageIO.read(f);
				int dim[] = new int[2];
				dim[0] = image.getWidth();
				dim[1] = image.getHeight();
				dimFields.setFields(dim);
			}
			catch (Exception ex){
				int dim[] = new int[2];
				dim[0] = -1;
				dim[1] = -1;
				dimFields.setFields(dim);
			}
			if (image != null){
				image.flush();
			}   
			return true;
		}
		return false;
	}    
}









