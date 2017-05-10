package it.sorintlab.lambda;

@SuppressWarnings("serial")
public class UnrecognizedOptionException extends Exception {

	public UnrecognizedOptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnrecognizedOptionException(String message) {
		super(message);
	}

}