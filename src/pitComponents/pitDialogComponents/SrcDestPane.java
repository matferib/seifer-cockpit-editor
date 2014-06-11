package pitComponents.pitDialogComponents;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import pitComponents.pitHelperComponents.*;
import javax.swing.JScrollPane;

/* this class is used to show many src/dest areas in the dialogs */
public class SrcDestPane extends JScrollPane {

    private int num = 0;
    private static int MAX = 20;
    private AreasFieldPanel[] fields;
    private Window parent;

    //we receive a label to make tips
    public SrcDestPane(Window parent, String title, String label) {
        this(parent, title, label, MAX);
    }

    public SrcDestPane(Window parent, String title, String label, int num) {
        this.setHorizontalScrollBarPolicy(
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setVerticalScrollBarPolicy(
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        this.parent = parent;

        //create the fields
        if ((num <= 0)||(num >= MAX))
            this.num = MAX;
        else
            this.num = num;
        fields = new AreasFieldPanel[num];

        //this is the viewport panel
        JPanel mainP = new JPanel();
        mainP.setLayout(new BorderLayout());
        this.setViewportView(mainP);

        {
            //title
            mainP.add(new JLabel(title, JLabel.CENTER), BorderLayout.NORTH);
        }
        {
            //fields
            JPanel fp = new JPanel(new GridLayout(this.num+1, 1));
            //we show MAX labels
            for (int i = 0; i < this.num; i++) {
                String s;
                if (i<10) s = label + "0"+i;
                else s = label + i;

                fields[i] = new AreasFieldPanel(parent, s);
                fp.add(fields[i]);

            }
            mainP.add(fp, BorderLayout.CENTER);
        }
    }

    public void enableFields(int num){
        for (int i=0;i<this.num;i++){
            if (i<num){
                fields[i].setEnabled(true);
            }
            else{
                fields[i].setEnabled(false);
            }
        }
    }

    public PitArea getFields(int i){
        try {
            if (i >= num)
                return null;
            return fields[i].getArea();
        }
        catch (Exception e2){
            return null;
        }
    }

    public void setFields(int i, PitArea a){
        if (i >= num)
            return ;
        fields[i].setFields(a);
    }
}