package nl.knmi.adaguc.security;

import java.security.Security;

import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import tools.Debug;

public class InitProvider {

  private static InitProvider instance;
  private static JcaPEMKeyConverter converter;
  
  private InitProvider(){}
  
  public static synchronized InitProvider getInstance(){
      if(instance == null){
          instance = new InitProvider();
          Debug.println("Creating BC provider");
          converter = (new JcaPEMKeyConverter().setProvider("BC"));
          Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
      }
      return instance;
  }

  public static JcaPEMKeyConverter getConverter() {
    getInstance();
    return InitProvider.converter;
  }
}