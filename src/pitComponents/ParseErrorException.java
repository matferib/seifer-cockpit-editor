package pitComponents;

public class ParseErrorException extends Exception {
	public ParseErrorException(String s) {
		super("ParseErrorException: " + s);
	}
}