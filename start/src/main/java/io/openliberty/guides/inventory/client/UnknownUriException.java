package io.openliberty.guides.inventory.client;

@SuppressWarnings("serial")
public class UnknownUriException extends Exception {

	public UnknownUriException() {
		super();
	}

	public UnknownUriException( String message ) {
		super( message );
	}
	
}
