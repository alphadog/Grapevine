package com.alphadog.grapevine.exceptions;

@SuppressWarnings("serial")
public class TwitterCredentialsBlankException extends Exception {

	public TwitterCredentialsBlankException() {
		super();
	}
	
	public TwitterCredentialsBlankException(String errorMessage) {
		super(errorMessage);
	}

}
