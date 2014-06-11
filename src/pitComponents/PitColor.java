package pitComponents;

import java.awt.*;

/** Represents a color, in falcon format. Falcon uses 0xFFbbggrr format, which 
	* is opposite of what we know. So I wrapped this weirdness inside this class
	* avoiding confusion
	*/
public class PitColor {
    private int red, green, blue;//, alpha;

    //constructors ------------------
    public PitColor() {
        red = green = blue = 0; //pitch black
    }
    public PitColor(Color c) {
        red = c.getRed();
        green = c.getGreen();
        blue = c.getBlue();
    }
    public PitColor(String st) {
        setAll(st);
    }
		public PitColor(int color){
			red = color & 0xFF;
			green = (color & 0xFF00) >> 8;
			blue = (color & 0xFF0000) >> 16;
		}
    //--------------------------------

    static public PitColor clone(PitColor pc){
        if (pc == null)
            return null;
        PitColor npc = new PitColor();
        npc.red = pc.red;
        npc.green = pc.green;
        npc.blue = pc.blue;
        return npc;
    }

    //setting colors-----------------
    public void setRed(int r) {
        red = r;
    }

    public void setGreen(int g) {
        green = g;
    }

    public void setBlue(int b) {
        blue = b;
    }

    //set altogether---------------------
    public void setColor(Color c){
        red = c.getRed();
        green = c.getGreen();
        blue = c.getBlue();
    }

    public void setAll(String st) {
        String s = st.toLowerCase().trim();
        int size = s.length();
        int indx = s.indexOf("0x");
        if ((indx != 0) || (size != 10 )){
            //error
            red = green = blue = 0;
        }
        else {
            String saux = s.substring(4);
            int l = saux.length();
            red = (int)Integer.parseInt(saux.substring(4, 6), 16);
            green = (int)Integer.parseInt(saux.substring(2, 4), 16);
            blue = (int)Integer.parseInt(saux.substring(0, 2), 16);
        }
    }


    //get methods
	/** returns the pit color as a falcon string representation. */
    public String toString() {
        String bstr = Integer.toHexString(blue);
        if (bstr.length() > 2)
            bstr = bstr.substring(bstr.length()-2, bstr.length());
        else if (bstr.length() == 1)
            bstr = "0" + bstr;

        String gstr = Integer.toHexString(green);
        if (gstr.length() > 2)
            gstr = gstr.substring(gstr.length()-2, gstr.length());
        else if (gstr.length() == 1)
            gstr = "0" + gstr;


        String rstr = Integer.toHexString(red);
        if (rstr.length() > 2)
            rstr = rstr.substring(rstr.length()-2, rstr.length());
        else if (rstr.length() == 1)
            rstr = "0" + rstr;

        return "0xff" + bstr + gstr + rstr;
    }

    public Color toColor(){
        return new Color(red, green, blue);
    }

}
