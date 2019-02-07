package nl.knmi.adaguc.security;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticatorInterface {

	void init(HttpServletRequest request);
	public String getClientId();
}
