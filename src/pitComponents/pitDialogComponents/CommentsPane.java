package pitComponents.pitDialogComponents;

import javax.swing.*;
import java.util.*;
import pitComponents.pitHelperComponents.*;
import java.awt.*;

public class CommentsPane extends JScrollPane {
    JTextArea comments = new JTextArea("//name: ");
    Window parent;

    public CommentsPane(Window parent) {
        this.parent = parent;
        comments.setColumns(180);
        comments.setRows(20);
        this.setViewportView(comments);
    }

    public void setComments(PitComment comment){
        if (comment != null){
            comments.setText(comment.toString());
        }
    }

    /** Gets the comments from the comments pane. If the comments do not start
     * with the double slash, we add it on the fly. By doing it, we avoid the 
     * problem with new objects created with comments, and copied to clipboard.
     *
     * They wouldnt have the // leading comment lines, resulting in failed paste
     *
     * @throws if some error occurs
     */
    public PitComment getComments() throws Exception {
        int lines = comments.getLineCount();
        PitComment pc = new PitComment();

        try {
            for (int i = 0; i < lines; i++) {
                int s = 0, l = 0;

                s = comments.getLineStartOffset(i);
                l = comments.getLineEndOffset(i) - s;
                if (l > 0){
                    String comLine = comments.getText(s, l).trim();
                    if (comLine.startsWith("//")){
                        pc.add(comLine);
                    }
                    else {
                        pc.add("//" + comLine);
                    }
                }

            }
            return pc;
        }
        catch (Exception e){
			throw new Exception("Error getting comments");
        }
    }
}