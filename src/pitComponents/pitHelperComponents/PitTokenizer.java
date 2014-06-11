package pitComponents.pitHelperComponents;

import java.io.*;
import pitComponents.*;
import pitComponents.pitObjects.*;
import pitComponents.pitHelperComponents.*;

public class PitTokenizer extends StreamTokenizer {
	/** creates a tokenizer which is capable of reading falcon 2d cockpit files. */
    public PitTokenizer(Reader r) {
        super(r);
    }

	/* sets all properties used by cockpit editor for this tokenizer. */
    public void normalState(){
        lowerCaseMode(true);
        slashSlashComments(false);
        slashStarComments(false);
        eolIsSignificant(false);
        parseNumbers();
        ordinaryChar(';');
        ordinaryChar('=');
        ordinaryChar('/');
        wordChars('#', '#');
        wordChars('_', '_');
        wordChars('a', 'z');
        wordChars('A', 'Z');
        wordChars('0', '9');
        wordChars('\u00A0','\u00FF');
        whitespaceChars('\u0000','\u0020');
    }

	/** places the tokenizer in comment state (where it reads comments). */
    public void commentState(){
        this.resetSyntax();
        this.eolIsSignificant(true);
        slashSlashComments(false);
    }

    /** Reads everything until after the comment. All objects call this prior to 
     * reading an entry, and ignoring the comments.
     */
    public void skipComment() throws Exception{
        //reads until we find something useful
//        char tst;
        nextToken();
//        tst = (char)ttype;

        while (ttype == '/') {
            nextToken(); // gets second slash
            if (ttype == '/') {
                commentState();
                //we are reading a comment, lets get eol
                nextToken();
//                tst = (char)ttype;
                //we read till eol
                while (! ( (ttype == StreamTokenizer.TT_EOL) ||
                           (ttype == StreamTokenizer.TT_EOF))) {
                    nextToken();
//                    tst = (char)ttype;
                }
                PitComment.numCommentsRead++;
            }
            else {
                throw new ParseErrorException(
                  "PitComment.java: missing / on comment");
            }
            normalState();
            nextToken();
//            tst = (char)ttype;
        }
        //when we get here, we got something which is not a comment =)

    }
    
    
    /** Reads until it finds a ;. When it returns, st_val is pointing to the ;.
     */
	public void skipLine() throws Exception{
		//reads until we find a ;
		char a = (char)ttype;        
		nextToken();

		while (ttype != ';') {
			a=(char)ttype;
			nextToken(); // gets second slash
		}

		//when we get here, we reached the ;
	}
	
    /* Parsing functions **************************************************/
    /** parses an hex number. */
	public long parseHex() throws ParseErrorException {
        long res;
        try {
            if (ttype != TT_NUMBER) {
                throw new ParseErrorException("expecting number");
            }
            res = (int) nval;
            nextToken();
            if (ttype == ';') {
                pushBack();
            }
            else {
                if (ttype != TT_WORD) {
                        throw new ParseErrorException("expecting xNumber");
                }
                String saux = sval;
                res = Integer.parseInt(saux.substring(1), 16);
            }
            return res;
        }
        catch (Exception e) {
            throw new ParseErrorException("pitComponent, parseHex: " + e);
        }
    }

	/** tokenize falcon color, returning its PitColor. */
    public PitColor parseColor() throws ParseErrorException {
        PitColor color = new PitColor();
        try {
            //first we take the 0 out, then we get xNumber as a word
            if (ttype != TT_NUMBER) {
                throw new ParseErrorException("expecting 0x number");
            }
            nextToken();

            if (ttype != TT_WORD) {
                throw new ParseErrorException("expecting xNumber");
            }
            String saux = sval;
            color.setAll("0" + saux);

            return color;
        }
        catch (Exception e) {
            throw new ParseErrorException("pitComponent, parseColor: " + e);
        }
    }
    /* end parsing **********************************************************/
	
    

}