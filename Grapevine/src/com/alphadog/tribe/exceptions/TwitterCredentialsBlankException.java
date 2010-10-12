package com.alphadog.tribe.exceptions;

@SuppressWarnings("serial")
public class TwitterCredentialsBlankException extends Exception {

	public TwitterCredentialsBlankException() {
		super();
	}
	
	public TwitterCredentialsBlankException(String errorMessage) {
		super(errorMessage);
	}

}
