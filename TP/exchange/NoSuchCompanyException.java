package exchange;

public class NoSuchCompanyException extends Exception {
	public NoSuchCompanyException() { super(); }
	public NoSuchCompanyException(String message) { super(message); }
}
