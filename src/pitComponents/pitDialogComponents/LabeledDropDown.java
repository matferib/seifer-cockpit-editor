package pitComponents.pitDialogComponents;

import javax.swing.*;

public class LabeledDropDown extends JPanel {
    JComboBox combo;
    JLabel label;

    public LabeledDropDown(String label, String items[]){
        this.label = new JLabel(label);
        combo = new JComboBox(items);
        add(this.label);
        add(combo);
    }

    public LabeledDropDown(String label) {
        this.label = new JLabel(label);
        combo = new JComboBox();
    }

    public void setValues(String values[]){
        for (int i=0;i<values.length;i++){
            combo.addItem(values[i]);
        }
    }

    public void setSelectedIndex(int index){
        combo.setSelectedIndex(index);
    }

    public int getSelectedIndex(){
        return combo.getSelectedIndex();
    }
    
    public String getSelectedItem(){
        return (String)combo.getSelectedItem();
    }

}