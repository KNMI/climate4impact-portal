package nl.knmi.adaguc.security;

import javax.servlet.http.HttpServletRequest;

public class AuthenticatorFactory{
	public static AuthenticatorImpl getAuthenticator(HttpServletRequest request){
		 return new AuthenticatorImpl(request);
	}
}
