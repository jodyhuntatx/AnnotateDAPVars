/*
 * This app takes the name of a CyberArk EPV safe as input and
 * updates corresponding passwords in DAP with annotations composed
 * of designated account properties. The enables searching DAP for
 * passwords using account property values, similar to AIM CP/CCP queries.
 */

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

import com.google.gson.Gson;

public class AnnotateDAPVars {

    public static Boolean DEBUG;

    public static void main(String[] args) {

	// set to true to enable debug output
	AnnotateDAPVars.DEBUG=false;
	PASJava.DEBUG=false;
	DAPJava.DEBUG=false;
	JavaREST.DEBUG=true;

	// turn off all cert validation - FOR DEMO ONLY
	disableSSL(); 

	// create 2D array that associates strings of account properties with DAP variable names 
	createAnnotationMap();

	// iterate over annotationMap updating DAP variables w/ annotations
	annotateVariables();

    } // main()

    // ==========================================
    // createAnnotationMap() 
    //
    // Creates a 2D array that associates strings of account properties with DAP variable names 
    //
    public static void createAnnotationMap() {

	PASJava.initConnection( System.getenv("PAS_IIS_SERVER_IP") );
	PASJava.logon(	System.getenv("PAS_ADMIN_NAME"), System.getenv("PAS_ADMIN_PASSWORD") );

	String pasSafeName = System.getenv("PAS_SAFE_NAME");

	// get array of all accounts in safe
        PASAccount[] pasAccounts = PASJava.getAccounts(pasSafeName);

	// for each account, get account details which includes optional & custom properties
	for(Integer i=0; i < pasAccounts.length; i++) {

	    String keywords = pasAccounts[i].safeName + ","
				+ pasAccounts[i].platformId + ","
				+ pasAccounts[i].userName;

	    PASAccountDetail[] pasAccountDetails = PASJava.getAccountDetails(keywords, pasSafeName);

	    if(pasAccountDetails.length > 1) {
		System.out.println("More than one detail record returned for keywords: " + keywords);
		System.out.println("Using these details from first record:");
		pasAccountDetails[0].print();
	    }
	    pasAccounts[i].details=pasAccountDetails[0];	// set account details to first details record
	}

	if(AnnotateDAPVars.DEBUG) {
	    System.out.println("Accounts + Details ===========");
	    for(Integer i=0; i < pasAccounts.length; i++) {
		pasAccounts[i].print();
		System.out.println("");
	    }
	    System.out.println("==============================");
	    System.out.println("");
	}

	// create annotation map array for account list 
	annotationMap = new String[pasAccounts.length][2];
   	for(Integer i=0; i < pasAccounts.length; i++) {

	    String pasAccountName;
	    // check for if account is member of an account group
	    String groupName = pasAccounts[i].getValue("GroupName");
	    if(groupName != null) { 
		pasAccountName = pasAccounts[i].getValue("VirtualUsername");
	    }
	    else {
		pasAccountName = pasAccounts[i].name;
	    }

	    //	column 0 = DAP variable name search string of form SAFE_NAME/ACCOUNT_NAME/password
	    				// (replace spaces in account name with %20)
	    annotationMap[i][0] = pasSafeName + "/"
				+ pasAccountName.replace(" ","%20") + "/"
				+ "password";

	    //	column 1 = concatentated strings of property values for annotation
	    annotationMap[i][1] = pasAccounts[i].safeName
				+ pasAccounts[i].platformId
				+ pasAccountName;
	} 

	if(AnnotateDAPVars.DEBUG) {
	    System.out.println("Annotation Map =======");
   	    for(Integer i=0; i<pasAccounts.length; i++) {
		System.out.println(i +") annotation: " + annotationMap[i][0]);
		System.out.println("    query: " + annotationMap[i][1]);
	    }
	    System.out.println("======================");
	    System.out.println("");
	}

    } // createAnnotationMap()

    // ==========================================
    // annotateVariables() - iterate over annotationMap, updating DAP variables w/ annotations
    //
    public static void annotateVariables() {

	DAPJava.initConnection(
				System.getenv("CONJUR_APPLIANCE_URL"),
				System.getenv("CONJUR_ACCOUNT")
				);
	String userApiKey = DAPJava.authnLogin(
				System.getenv("CONJUR_USER"),
				System.getenv("CONJUR_PASSWORD")
				);
	DAPJava.authenticate(
				System.getenv("CONJUR_USER"),
				userApiKey
				);

   	for(Integer i=0; i < annotationMap.length; i++) {
	    String searchVarName = annotationMap[i][0];

	    // parse search output json from DAP into Variable array
            Gson gson = new Gson();
	    Variable[] var = (Variable[]) gson.fromJson( DAPJava.search(searchVarName), Variable[].class );
	    switch(var.length) {
		case 0 :
			System.out.println("No variable found using: " + searchVarName);
			System.out.println("");
			break;
		case 1 :
			// variable id is in format "account:variable:name" - just want the name
	  		String varId = var[0].id.split(":")[2]; 
			annotateVariable(varId, annotationMap[i][1]);
			break;
		default :
			System.out.println("More than one variable found using: " + searchVarName);
			for (Integer j = 0; j < var.length; j++) {
		            System.out.println(var[j].id.split(":")[2]);
       			}
			System.out.println("");
	    } // switch
	} // for

    } // annotateVariables()

    // ==========================================
    // annotateVariable(varId,annotationString)
    //
    public static void annotateVariable(String _varId, String _annotationString) {

	System.out.println("Annotating variable:" + _varId + "\n\twith: " + _annotationString);
	
	// generate policy - REST method accepts text - no need to create a file
	String policyText = "---\n"
			    + "- !variable\n"
			    + "  id: " + _varId + "\n"
			    + "  annotations:\n"
			    + "    attribs: " + _annotationString;

	// load policy using "delete" method equating to PATCH REST call
	DAPJava.loadPolicy("delete", "root", policyText);

	// test lookup with annotation
	if(DAPJava.DEBUG) {
	    System.out.println("Looking for: " + _annotationString + "\n"
				+ "Found: " + DAPJava.search(_annotationString));
	}

    } // annotateVariable()

   /*********************************************************
    *                    PRIVATE MEMBERS
    *********************************************************/

    // the annotation map is the primary datastructure for this app
    private static String[][] annotationMap;

    // ==========================================
    // void disableSSL()
    //   from: https://nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
    //
    private static void disableSSL() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
 
        // Install the all-trusting trust manager
	try {
	        SSLContext sc = SSLContext.getInstance("SSL");
        	sc.init(null, trustAllCerts, new java.security.SecureRandom());
        	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	} catch(NoSuchAlgorithmException e) {
		e.printStackTrace();
	} catch(KeyManagementException e) {
		e.printStackTrace();
	}

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
 
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    } // disableSSL
 
} // AnnotateDAPVars
