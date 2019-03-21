package nl.knmi.adaguc.security;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationExceptionImpl extends AuthenticationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthenticationExceptionImpl(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

}
