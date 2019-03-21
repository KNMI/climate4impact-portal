package nl.knmi.adaguc.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.ietf.jgss.GSSException;

import lombok.Getter;
import nl.knmi.adaguc.tools.Debug;
import nl.knmi.adaguc.tools.Tools;


/**
 * 
 * @author maartenplieger
 *
 */


public class PemX509Tools {

	/**
	 * 
	 * @author maartenplieger
	 *
	 */
	@Getter
	public class X509Info{
		public X509Info(String certOpenIdIdentifier, String uniqueId2) {
			this.uniqueId = uniqueId2;
			this.CN = certOpenIdIdentifier;
		}
		String uniqueId;
		String CN;
	}

	/**
	 * 
	 * @author maartenplieger
	 *
	 */
	@Getter
	public class X509UserCertAndKey{
		public X509UserCertAndKey(X509Certificate userSlCertificate, PrivateKey privateKey) {
			this.userSlCertificate = userSlCertificate;
			this.privateKey = privateKey;
		}
		X509Certificate userSlCertificate;
		PrivateKey privateKey;
	}

	/**
	 * Reads private key from PEM file
	 * @param fileName Location to the PEM file containing the private key
	 * @return KeyPair, containing the private key
	 * @throws IOException
	 */
	public static PrivateKey readPrivateKeyFromPEM (String fileName) throws IOException{
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		String pemData = Tools.readFile(fileName);
		Debug.println("Reading " + fileName);
		String privateKeyPem = null;
		try{              
			privateKeyPem = "-----BEGIN RSA PRIVATE KEY-----\n"+pemData.split("-----BEGIN RSA PRIVATE KEY-----")[1];
		}catch(Exception e){			
		  privateKeyPem = "-----BEGIN PRIVATE KEY-----\n"+pemData.split("-----BEGIN PRIVATE KEY-----")[1];
		}
		PEMParser pemParser = new PEMParser(new StringReader(privateKeyPem));
		Object object = pemParser.readObject();
		pemParser.close();
		PEMKeyPair ukp = (PEMKeyPair) object;
		return converter.getKeyPair(ukp).getPrivate();
	}

	/**
	 * Read certificate from PEM file
	 * @param fileName Location to PEM file containing the public key
	 * @return Returns the X509 certificate
	 * @throws IOException
	 * @throws CertificateException
	 */
	public static X509Certificate readCertificateFromPEM (String fileName) throws IOException, CertificateException{
		PemReader pemReader = new PemReader(new FileReader(fileName));
		PemObject obj = pemReader.readPemObject();
		pemReader.close();
		X509CertificateHolder vla = new X509CertificateHolder((obj).getContent());
		JcaX509CertificateConverter certconv = new JcaX509CertificateConverter().setProvider("BC");
		X509Certificate b = certconv.getCertificate(vla);	
		return b;
	}


	/**
	 * Returns x509Info object with properties on success, otherwise null.
	 * @param request
	 * @return
	 */
	public X509Info getUserIdFromCertificate(HttpServletRequest request){
		// org.apache.catalina.authenticator.SSLAuthenticator
		X509Certificate[] certs = (X509Certificate[]) request
				.getAttribute("javax.servlet.request.X509Certificate");
		if (null != certs && certs.length > 0) {
			return getUserIdFromCertificate(certs[0]);
		}
		return null;
	}

	public String getUserIdFromSubjectDN (String subjectDN) {
		String[] dnItems = subjectDN.split(", ");
		for (int j = 0; j < dnItems.length; j++) {
			int CNIndex = dnItems[j].indexOf("CN");
			if (CNIndex != -1) {
				return dnItems[j].substring("CN=".length()
						+ CNIndex);
			}
		}
		return null;
	}
	/**
	 * Returns information about the given certificate, like CN and serial number. This method does not verify
	 * the certificate against trustroots. 
	 * @param cert The X509Certificate to get the information from
	 * @return
	 */
	public X509Info getUserIdFromCertificate(X509Certificate cert){
		/*Trying to get user info from X509 cert*/
		String CertOpenIdIdentifier = null;
		String uniqueId = null;
		uniqueId = "x509_"+cert.getSerialNumber();
		String subjectDN = cert.getSubjectDN().toString();
		CertOpenIdIdentifier = getUserIdFromSubjectDN(subjectDN);
		if(CertOpenIdIdentifier == null || uniqueId == null){
			return null;
		}
		return (new PemX509Tools()).new X509Info(CertOpenIdIdentifier, uniqueId);
	}


	/**
	 * Signs the CSR using CA certificate and private key
	 * @param caCert
	 * @param caPrivateKey
	 * @param csr
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws CertificateException
	 * @throws CertIOException
	 * @throws OperatorCreationException
	 */
	public static X509Certificate signCSR(PKCS10CertificationRequest csr, X509Certificate caCert, PrivateKey caPrivateKey)
			throws NoSuchAlgorithmException, InvalidKeyException, CertificateException, CertIOException, OperatorCreationException {
		Calendar notAfter = Calendar.getInstance();
		notAfter.add(Calendar.YEAR, 1);

		Calendar notBefore = Calendar.getInstance();
		//notBefore.add(Calendar., -1);


		JcaPKCS10CertificationRequest jcaRequest = new JcaPKCS10CertificationRequest(csr);
		X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(caCert,
				BigInteger.valueOf(System.currentTimeMillis()), notBefore.getTime(), notAfter.getTime(), jcaRequest.getSubject(), jcaRequest.getPublicKey());

		//		JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
		//		certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(caCert))
		//		.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(jcaRequest.getPublicKey()))
		//		.addExtension(Extension.basicConstraints, true, new BasicConstraints(0))
		//		.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment))
		//		.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));

		//		// add pkcs extensions
		//		Attribute[] attributes = csr.getAttributes();
		//		for (Attribute attr : attributes) {
		//			// process extension request
		//			if (attr.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
		//				Extensions extensions = Extensions.getInstance(attr.getAttrValues().getObjectAt(0));
		//				@SuppressWarnings("unchecked")
		//				Enumeration<ASN1ObjectIdentifier> e = (Enumeration<ASN1ObjectIdentifier>  )extensions.oids();
		//				while (e.hasMoreElements()) {
		//					ASN1ObjectIdentifier oid =  e.nextElement();
		//					Extension ext = extensions.getExtension(oid);
		//					certificateBuilder.addExtension(oid, ext.isCritical(), ext.getParsedValue());
		//				}
		//			}
		//		}

		ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(caPrivateKey);
		return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateBuilder.build(signer));
	}

	/**
	 * Converts a certificate to a PEM string
	 * @param certHolder
	 * @return
	 * @throws IOException
	 */
	public static String certificateToPemString(Object certHolder) throws IOException {
		StringWriter str = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(str);
		pemWriter.writeObject(certHolder);
		pemWriter.close();
		str.close();
		return str.toString();
	}

	/**
	 * Writes a certificate to a file in PEM format
	 * @param certHolder
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeCertificateToPemFile(Object certHolder, String fileName) throws IOException {
		Tools.writeFile(fileName, certificateToPemString(certHolder));		
	}

	public static void writePrivateKeyToPemFile(PrivateKey certHolder, String fileName) throws IOException {
		Tools.writeFile(fileName, privateKeyToPemString(certHolder));		
	}


	public static String privateKeyToPemString(PrivateKey certHolder) throws IOException {
		StringWriter str = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(str);
		pemWriter.writeObject(certHolder);
		pemWriter.close();
		str.close();
		return str.toString();
	}
	/**
	 * Creates a selfsigned certificate authority 
	 * @param CN The common name of the CA
	 * @param keyPairCA
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws OperatorCreationException
	 * @throws CertificateException
	 */
	static X509Certificate createSelfSignedCA(String CN, KeyPair keyPairCA) throws IOException, NoSuchAlgorithmException, OperatorCreationException, CertificateException{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		byte[] pk = keyPairCA.getPublic().getEncoded();
		SubjectPublicKeyInfo bcPk = SubjectPublicKeyInfo.getInstance(pk);
		X509v1CertificateBuilder certGen = new X509v1CertificateBuilder(
				new X500Name(CN),
				BigInteger.ONE,
				new Date(),
				cal.getTime(),
				new X500Name(CN),
				bcPk
				);
		X509CertificateHolder certHolder = certGen
				.build(new JcaContentSignerBuilder("SHA1withRSA").build(keyPairCA.getPrivate()));
		X509Certificate caCert = new JcaX509CertificateConverter().setProvider( "BC" )
				.getCertificate( certHolder );
		return caCert;
	}

	/**
	 * Verify a PEM based certificate against a java truststore
	 * @param clientCertLocation
	 * @param trustRootsLocation
	 * @throws CertificateException
	 * @throws IOException
	 * @throws CertificateVerificationException
	 */
	public static void verifyCertificate(String clientCertLocation, String trustRootsLocation) throws CertificateException, IOException, CertificateVerificationException {
		String[] fileList = Tools.ls(trustRootsLocation);
		if(fileList == null){
			throw new IOException("No certificates found in specified directory");
		}
		Set<X509Certificate> trust = new HashSet<X509Certificate>();
		for(String file : fileList){
			if(file.endsWith(".0") || file.endsWith(".pem") ){
				if ( file.equals("09184877.0") == true ||    // Intermediate CA: DC=uk, DC=ac, DC=ceda, O=STFC RAL, CN=Centre for Environmental Data Analysis
						file.equals("439ce3f7.0") == true || // RootCA         : C=UK, O=eScienceSLCSHierarchy, OU=Authority, CN=SLCS Top Level CA
						true)
				{
					X509Certificate tr = PemX509Tools.readCertificateFromPEM(trustRootsLocation + "/" + file);
					trust.add(tr);
				}
			}
		}
		X509Certificate clientCertificate = PemX509Tools.readCertificateFromPEM(clientCertLocation);
		trust.add(clientCertificate);                        // Client CA: DC=uk, DC=ac, DC=ceda, O=STFC RAL, CN=https://ceda.ac.uk/openid/C3Smagic.C3Smagic
		CertificateVerifier.verifyCertificate(clientCertificate,trust);
	}

	public static PKCS10CertificationRequest createCSR(String clientCSRCN, KeyPair keypair) throws OperatorCreationException {
		PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
				new X500Principal(clientCSRCN), 
				keypair.getPublic()
				);
		JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
		ContentSigner signer = csBuilder.build(keypair.getPrivate());
		PKCS10CertificationRequest csr = p10Builder.build(signer);	
		return csr;
	}

	/**
	 * Complete workflow of setting up a PEM based self signed CA and creates a client certificate
	 * This is useful for programmatically testing two way SSL (See unit test)
	 * @param testBaseDir
	 * @param clientCertDir
	 * @param clientCertLocation
	 * @param clientCN
	 * @param trustRootsDir
	 * @throws NoSuchAlgorithmException
	 * @throws OperatorCreationException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	public static void setupCAandClientCert(String testBaseDir, String clientCertDir, String clientCertLocation, String clientCN, String trustRootsDir) throws NoSuchAlgorithmException, OperatorCreationException, CertificateException, IOException, InvalidKeyException{
		/* Step 1 - Initialize security provider and key generator*/
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		int keySize = 2048;
		KeyPairGenerator keyGenkeyGeneratorRSA = KeyPairGenerator.getInstance("RSA");
		keyGenkeyGeneratorRSA.initialize(keySize, new SecureRandom());

		/* Step 2 - Generate KeyPair for CA */
		KeyPair keyPairCA = keyGenkeyGeneratorRSA.generateKeyPair();

		/* Step 3 - Generate CA */
		X509Certificate caCertificate = PemX509Tools.createSelfSignedCA("CN=CA TESTROOTCA", keyPairCA);

		/* Step 4 - Generate KeyPair for CSR */
		KeyPair keyPairCSR = keyGenkeyGeneratorRSA.generateKeyPair();

		/* Step 5 - Generate CSR */
		PKCS10CertificationRequest csr = PemX509Tools.createCSR("CN="+clientCN, keyPairCSR);

		/* Step 6 - Sign CSR with CA */		
		X509Certificate signedCrt = PemX509Tools.signCSR(csr, caCertificate, keyPairCA.getPrivate());

		/* Step 7 - Write certificates in PEM format to fs */
		Tools.rmdir(testBaseDir);
		Tools.mksubdirs(trustRootsDir);
		Tools.mksubdirs(clientCertDir);
		PemX509Tools.writeCertificateToPemFile(caCertificate,trustRootsDir +"/ca.pem");
		PemX509Tools.writeCertificateToPemFile(signedCrt, clientCertLocation);
	}
	/**
	 * Setup the certificate for specific user.
	 * @param clientId
	 * @param caCertificate
	 * @param privateKey
	 * @throws NoSuchAlgorithmException
	 * @throws OperatorCreationException
	 * @throws InvalidKeyException
	 * @throws CertificateException
	 * @throws CertIOException
	 */
	public X509UserCertAndKey setupSLCertificateUser(String clientId, X509Certificate caCertificate, PrivateKey privateKey)
			throws NoSuchAlgorithmException, OperatorCreationException, InvalidKeyException, CertificateException, CertIOException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		KeyPairGenerator keyGenkeyGeneratorRSA = KeyPairGenerator.getInstance("RSA");
		int keySize = 2048;
		keyGenkeyGeneratorRSA.initialize(keySize, new SecureRandom());
		/* Step 4 - Generate KeyPair for CSR */
		KeyPair keyPairCSR = keyGenkeyGeneratorRSA.generateKeyPair();

		/* Step 5 - Generate CSR */
		PKCS10CertificationRequest csr = PemX509Tools.createCSR("CN="+clientId, keyPairCSR);

		//		 try {
		//			PemX509Tools.writeCertificateToPemFile(csr, "/tmp/_usercsr.csr");
		//			PemX509Tools.writeCertificateToPemFile(caCertificate, "/tmp/_ca.pem");
		//			PemX509Tools.writePrivateKeyToPemFile(privateKey, "/tmp/_ca.key");
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		/* Step 6 - Sign CSR with CA */		
		X509Certificate signedCrt = PemX509Tools.signCSR(csr, caCertificate, privateKey);

		//		try {
		//			PemX509Tools.writeCertificateToPemFile(signedCrt, "/tmp/_user.crt");
		//			PemX509Tools.writePrivateKeyToPemFile(keyPairCSR.getPrivate(), "/tmp/_user.key");
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}


		return new X509UserCertAndKey(signedCrt, keyPairCSR.getPrivate());
	}

	/**
	 * Sets up a closable http client for two way SSL or client authentication
	 * @param trustStoreLocation Location of the truststore file (.ts file)
	 * @param trustStorePassword Password of the truststore
	 * @param clientCertificate The client certificate in PEM format, can be null
	 * @return Closable httpclient to be used in get or post requests
	 * @throws KeyManagementException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException
	 * @throws SignatureException
	 * @throws GSSException
	 */
	public CloseableHttpClient getHTTPClientForPEMBasedClientAuthPEM(
			String trustStoreLocation,
			char [] trustStorePassword, 
			String clientCertificate
			) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException, GSSException{


		X509UserCertAndKey certAndKey = null;
		if(clientCertificate!=null){
			/* Read the client auth certificates */
			PrivateKey clientPrivateCred = readPrivateKeyFromPEM(clientCertificate);
			X509Certificate clientCert = readCertificateFromPEM(clientCertificate);
			certAndKey = new X509UserCertAndKey(clientCert,clientPrivateCred);

		}
		return getHTTPClientForPEMBasedClientAuth(trustStoreLocation,trustStorePassword,certAndKey);
	}

	/**
	 * 
	 * @param trustStoreLocation
	 * @param trustStorePassword
	 * @param certAndKey
	 * @return
	 * @throws KeyManagementException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException
	 * @throws SignatureException
	 * @throws GSSException
	 */
	public CloseableHttpClient getHTTPClientForPEMBasedClientAuth(
			String trustStoreLocation,
			char [] trustStorePassword, 
			X509UserCertAndKey certAndKey
			) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException, GSSException{
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

		/* Load the server JKS truststore */
		FileInputStream trustStoreStream = new FileInputStream(new File(trustStoreLocation));
		try {
			trustStore.load(trustStoreStream, trustStorePassword);
		} finally {
			try {
				trustStoreStream.close();
			} catch (Exception ignore) {
			}
		}
		trustStoreStream.close();
		if(certAndKey!=null){
			trustStore.setKeyEntry("privateKeyAlias", certAndKey.getPrivateKey(),
					trustStorePassword, new Certificate[] { certAndKey.getUserSlCertificate()});
		}

		SSLContext sslContext =
				new SSLContextBuilder()
				.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
				.loadKeyMaterial(trustStore, trustStorePassword)
				.build();

		return HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(new HostnameVerifier() {
      public boolean verify(String arg0, SSLSession arg1) {
          return true;
      }
  }).build();
	}


	public static void main(String [ ] args){
		Debug.println("main");
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		try {
			char [] CLIENT_TRUSTSTORE_PASSWORD = "changeit".toCharArray();
			String CLIENT_TRUSTSTORE = "/home/c3smagic/config/esg-truststore.ts";
			String certLoc = "/home/c3smagic/impactspace/esg-dn1.nsc.liu.se.esgf-idp.openid.maartenplieger/certs/creds.pem";
			String url = "https://compute-test.c3s-magic.eu:9000/user/getuserinfofromcert";
			CloseableHttpClient httpClient = (new PemX509Tools()).getHTTPClientForPEMBasedClientAuthPEM(CLIENT_TRUSTSTORE, CLIENT_TRUSTSTORE_PASSWORD, certLoc);
			CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet(url));
			String result = EntityUtils.toString(httpResponse.getEntity());
			Debug.println(result);

		}  catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GSSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Debug.println("/main");
	}

  public static void setup() {
    InitProvider.getInstance();
  }
}
