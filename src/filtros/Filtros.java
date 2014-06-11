package filtros;

import javax.swing.filechooser.*;
import java.io.File;

public class Filtros extends FileFilter {

  public Filtros() {
  }
  public boolean accept(File f) {
         return (f.getName().toLowerCase().endsWith(".dat") || f.isDirectory() );
  }
  public String getDescription(){
         return("Cockpit Data File");
  }
}