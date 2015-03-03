package impactservice;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.GSSException;

import tools.Debug;

/**
 * Servlet implementation class GetDownloadScriptHandler
 */
public class GetDownloadScriptHandler extends HttpServlet {
  public class GetCredentialsException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String message=null;
    
    public GetCredentialsException(String m) {
      message=m;
    }
    
    public String getMessage() {
      return message;
    }

  }

  private static final long serialVersionUID = 1L;

  ImpactUser user=null;
  /**
   * @see HttpServlet#HttpServlet()
   */
  public GetDownloadScriptHandler() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try{
      user=LoginManager.getUser(request,response);
      if(user == null)return;
    }catch(Exception e){
      user = null;
    }
    System.err.println("user:"+user.id);
    String urls=request.getParameter("urls");
    String openid=request.getParameter("openid");
    String password=request.getParameter("password");
    System.err.println(urls+";"+openid+";"+"*********");
    if ((urls!=null)&&(password!=null)&&(openid!=null)) {
      //Use openid and password to get credentials for download
      String credentials;
      try {
        credentials = getCredentials(user, openid, password);
      } catch (GetCredentialsException e) {
        response.setContentType("text/html");
        ServletOutputStream os=response.getOutputStream();
        os.print("Error retrieving download credentials for user "+user.id+" because:"+e.getMessage());
        os.close();
        return;
      }
      Date notAfter=getCertEndDate(user.getWorkspace()+"certs/"+"downloadcreds.pem");
      Format fmt=new SimpleDateFormat("yyyyMMddHHmm");

      String today=fmt.format(new Date());

      urls=urls.replaceAll("\r", "");

      checkCredentials(user.getWorkspace()+"certs/"+"downloadcreds.pem");

      //Generate the wget script, by reading template file and including the credentials, credential expiry and the filelist
      Map<String,String> fillins=new HashMap<String,String>();
      fillins.put("URLS", urls);
      fillins.put("OPENID", openid);
      fillins.put("CREDENTIALS", credentials);
      fillins.put("VALID_UNTIL", fmt.format(notAfter));
      String wgetScript=generateWgetScript(Configuration.DownloadScriptConfig.getDownloadScriptTemplate(), fillins);
      ServletOutputStream os=response.getOutputStream();
      response.setContentType("application/octet-stream");
      response.setHeader("Content-Disposition", "attachment; filename=\"IMPACTDL_"+today+".sh\"");
      os.print(wgetScript);
    }
  }

  private static X509Certificate loadPublicX509(String filename) 
      throws GeneralSecurityException {
    InputStream is = null;  
    X509Certificate crt = null;

    try {
      is = new FileInputStream(filename);

      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      crt = (X509Certificate)cf.generateCertificate(is);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      closeSilent(is);
    }
    return crt;
  }

  private static PrivateKey loadPrivateKey(String fileName) 
      throws IOException, GeneralSecurityException {
    PrivateKey key = null;
    BufferedReader br = null;
    try {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
      StringBuilder builder = new StringBuilder();
      boolean inKey = false;
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (!inKey) {
          if (line.startsWith("-----BEGIN ") && 
              line.endsWith(" PRIVATE KEY-----")) {
            inKey = true;
          }
          continue;
        }
        else {
          if (line.startsWith("-----END ") && 
              line.endsWith(" PRIVATE KEY-----")) {
            inKey = false;
            break;
          }
          builder.append(line);
        }
      }
      //
      byte[] encoded = DatatypeConverter.parseBase64Binary(builder.toString());
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      key = kf.generatePrivate(keySpec);
    } finally {
      //      System.err.println("Error with cert "+fileName);
      closeSilent(br);
    }
    return key;
  }

  private static void closeSilent(final BufferedReader br) {
    if (br == null) return;
    try { br.close(); } catch (Exception ign) {}
  }

  private static void closeSilent(final InputStream is) {
    if (is == null) return;
    try { is.close(); } catch (Exception ign) {}
  }

  private Date getCertEndDate(String filename) {
    X509Certificate pubkey=null;
    try {
      pubkey = loadPublicX509(filename);
    } catch (GeneralSecurityException e) {
    }
    if (pubkey==null) {
      return null;
    }
    return pubkey.getNotAfter();
  }

  private String getCredentialsFromSession(ImpactUser user, String openid, String password) throws IOException {
    byte[] credentials_encoded=Files.readAllBytes(Paths.get(user.getWorkspace()+"certs/"+"creds.pem"));
    return new String(credentials_encoded);
  }

  private String getCredentials(ImpactUser user, String openid, String password) throws GetCredentialsException  {
    URI myproxyURL;
    ExtendedGSSCredential cred=null;
    String msg=null;
    try {
      myproxyURL = new URI(user.userMyProxyService);
//      MyProxy myProxy = new MyProxy(myproxyURL.getHost(), myproxyURL.getPort());
      MyProxy myProxy = new MyProxy("bvlpenes.knmi.nl", myproxyURL.getPort());
      cred = (ExtendedGSSCredential) myProxy.get(openid, password, 60*60*24*7);
    } catch (MyProxyException e) {
      msg="MyProxy get failed with host "+user.userMyProxyService;
      Debug.println(msg);
      throw new GetCredentialsException(msg);
    } catch (URISyntaxException e) {
      msg="URI parse error: "+user.userMyProxyService;
      Debug.println(msg);
      throw new GetCredentialsException(msg);
    } catch (IllegalArgumentException e) {
      msg="Error found: "+e.getMessage();
      Debug.println(msg);
      throw new GetCredentialsException(msg);
    }
    
    byte[] data=null;
    try {
      data = cred.export(ExtendedGSSCredential.IMPEXP_OPAQUE);
      cred.dispose();
    } catch (GSSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
    // release system resources held by the credential

    try {
      FileOutputStream out=null;
      try {
        out = new FileOutputStream(user.getWorkspace()+"certs/"+"downloadcreds.pem");
        out.write(data);
        out.close();
        out=null;
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        if (out!=null) {
          out.close();
        }
//      } catch (IOException e) {
      }
    } catch (IOException e) {}
    return data==null?null:new String(data);
  }

  private void checkCredentials(String filename) {
    try {
      X509Certificate pubkey=loadPublicX509(filename);
      System.err.println("end of validity: "+pubkey.getNotAfter());
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
  }

  private String generateWgetScript(String downloadScriptTemplate,
      Map<String, String> fillins) throws IOException {
    byte[] wgetScript_encoded=Files.readAllBytes(Paths.get(downloadScriptTemplate));
    String wgetScript=new String(wgetScript_encoded, StandardCharsets.ISO_8859_1);
    for (Map.Entry<String, String> entry: fillins.entrySet()) {
      String search="\\|"+entry.getKey()+"\\|";
      String replaceWith=entry.getValue();
      wgetScript=wgetScript.replaceAll(search, replaceWith);
    }
    return wgetScript;
  }


}
