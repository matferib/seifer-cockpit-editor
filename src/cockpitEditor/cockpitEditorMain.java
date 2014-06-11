package cockpitEditor;

//import programWindows.MainWindow;
import windows.NewMainWindow;
import java.io.*;

public class cockpitEditorMain {

    public static void main(String[] args) {

        //we try to get the directory from a file, if not we use root
        String initCWD;
        try {
            RandomAccessFile conf = new RandomAccessFile("conf.txt", "r");
            initCWD = conf.readLine().trim();
            conf.close();
	    UsPr.cwd = new File(initCWD);
        }
        catch (Exception e){
            initCWD = new String("/");
        }
        
        //MainWindow mw = new MainWindow(initCWD);
	NewMainWindow mw = new NewMainWindow();        
        mw.setVisible(true);
    }

}

