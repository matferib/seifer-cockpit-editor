/*
 * GifFilter.java
 *
 * Created on 19 de Setembro de 2005, 17:24
 */

package pitComponents.pitHelperComponents;

import java.io.File;

/**
 * A filter for selecting gif files. Used by 
 * @author  matheus
 */
public class GifFilter extends javax.swing.filechooser.FileFilter {

  public GifFilter() {
  }
  
  public boolean accept(File f) {
         return (f.getName().toLowerCase().endsWith(".gif") || f.isDirectory() );
  }
  
  public String getDescription(){
         return("GIF files");
  }
}
