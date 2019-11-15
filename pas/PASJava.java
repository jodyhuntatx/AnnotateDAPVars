import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.io.UnsupportedEncodingException;
import com.google.gson.Gson;

public class PASJava {

     public static Boolean DEBUG=false; // set to true for debug output

    /******************************************************************
     * 				PUBLIC MEMBERS
     *
     * void initConnection(serverHost)
     * void logon(username,password)
     * PASAccount[] getAccounts(safeName)
     * PASAccountDetail[] getAccountDetails(keyWords,safeName)
     * PASAccountGroup[] getAccountGroups(safeName)
     * PASAccountGroupMember[] getAccountGroupMembers(safeName)
     *
     ******************************************************************/

    // ===============================================================
    // void initConnection() - initializes base server URL
    //
    public static void initConnection(String _pasServerHost) {
	pasServerUrl = "https://" + _pasServerHost + "/PasswordVault/api";

	// cuz sometimes the old way is the only way to get what you want
	pasServerUrlclassic = "https://" + _pasServerHost + "/PasswordVault/WebServices/PIMServices.svc";

    } // initConnection


    // ===============================================================
    // logon(username,password) - logs user in and sets session token
    //
    public static void logon(String _user, String _password) {

	String requestUrl = pasServerUrl + "/auth/Cyberark/Logon";
	String bodyContent = "{"
				+ "\"username\":\"" + _user + "\","
				+ "\"password\":\"" + _password + "\""
			   + "}";

	// get session token and save w/o double quotes
	pasSessionToken = JavaREST.httpPost(requestUrl, bodyContent, "").replace("\"","");

	if(PASJava.DEBUG) {
	    System.out.println("");
	    System.out.println("====== PASJava.login() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("bodyContent: " + bodyContent);
	    System.out.println("sessionToken: " + pasSessionToken);
	    System.out.println("=============================");
	    System.out.println("");
	}

    } //logon

    // ===============================================================
    // PASAccount[] getAccounts(safeName) - returns array of all accounts in a safe
    //
    public static PASAccount[] getAccounts(String _safeName) {

	String requestUrl = pasServerUrl + "/accounts?filter=safeName%20eq%20" + _safeName;
	String authHeader = pasSessionToken;

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.getAccounts() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("===================================");
	    System.out.println("");
	}

	String accountOutput = JavaREST.httpGet(requestUrl, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw account listing:");
	    System.out.println(accountOutput);
	    System.out.println("");
	}

        // parse account json output into PASAccountList
        Gson gson = new Gson();
        PASAccountList pasAccountList = (PASAccountList) gson.fromJson( accountOutput, PASAccountList.class );

	if(PASJava.DEBUG) {
	    System.out.println("PAS Account List =====");
	    pasAccountList.print();
	    System.out.println("======================");
	    System.out.println("");
	}

	return pasAccountList.value;

    } // getAccounts

    // ===============================================================
    // PASAccountDetail[] getAccountDetails(keywordString,safeName) - returns details of account w/ keyString
    //
    public static PASAccountDetail[] getAccountDetails(String _keywordString, String _safeName) {

	String requestUrl = pasServerUrlclassic + "/Accounts?Keywords=" 
				+ _keywordString.replace(" ","%20") + "&Safe=" + _safeName;
	String authHeader = pasSessionToken;

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.getAccountDetails() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("=========================================");
	    System.out.println("");
	}

	String detailOutput = JavaREST.httpGet(requestUrl, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw detail listing:");
	    System.out.println(detailOutput);
	    System.out.println("");
	}

        // parse account json output into PASAccountDetail
        Gson gson = new Gson();
        PASAccountDetailList pasAccountDetails = (PASAccountDetailList) gson.fromJson( detailOutput, PASAccountDetailList.class );

	if(PASJava.DEBUG) {
	    System.out.println("PAS Account Details =====");
	    pasAccountDetails.print();
	    System.out.println("=========================");
	    System.out.println("");
	}

	return pasAccountDetails.accounts;

    } // getAccounts

    // ===============================================================
    // getAccountGroups(safeName) - returns array of all account groups in safe
    //
    public static PASAccountGroup[] getAccountGroups(String _safeName) {

	String requestUrl = pasServerUrl + "/AccountGroups?Safe=" + _safeName;
	String authHeader = pasSessionToken;

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.getAccountGroups() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("========================================");
	    System.out.println("");
	}

	String groupOutput = JavaREST.httpGet(requestUrl, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw json account group output:");
	    System.out.println(groupOutput);
	    System.out.println("");
	}

        // parse account group json output into PASAccountGroup array
        Gson gson = new Gson();
        PASAccountGroup[] pasAccountGroups = (PASAccountGroup[]) gson.fromJson( groupOutput, PASAccountGroup[].class );

	if(PASJava.DEBUG) {
	    System.out.println("PAS Account Groups =====");
	    for(Integer i=0; i < pasAccountGroups.length; i++) {
	        pasAccountGroups[i].print();
	    }
	    System.out.println("========================");
	    System.out.println("");
	}

	return pasAccountGroups;

    } // getAccountGroups

    // ===============================================================
    // getAccountGroupMembers(GroupID) - returns array of all members in account group
    //
    public static PASAccountGroupMember[] getAccountGroupMembers(String _groupId) {

	String requestUrl = pasServerUrl + "/AccountGroups/" + _groupId + "/Members";
	String authHeader = pasSessionToken;

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.getAccountGroupMembers() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("==============================================");
	    System.out.println("");
	}

	String memberOutput = JavaREST.httpGet(requestUrl, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw account group member output:");
	    System.out.println(memberOutput);
	    System.out.println("");
	}

        // parse account group member json output into PASAccountGroupMember array
        Gson gson = new Gson();
        PASAccountGroupMember[] pasAccountGroupMembers = (PASAccountGroupMember[]) gson.fromJson( memberOutput, PASAccountGroupMember[].class );

	if(PASJava.DEBUG) {
	    System.out.println("PAS Account Group Members =====");
	    for(Integer i=0; i < pasAccountGroupMembers.length; i++) {
	        pasAccountGroupMembers[i].print();
	        System.out.println("");
	    }
	    System.out.println("==============================");
	    System.out.println("");
	}

	return pasAccountGroupMembers;

    } // getAccountGroupMembers


    /******************************************************************
     * 				PRIVATE MEMBERS
     ******************************************************************/

    static private String pasServerUrl;
    static private String pasServerUrlclassic;
    static private String pasSessionToken;

    // ===============================================================
    // String base64Encode() - base64 encodes argument and returns encoded string
    //
    private static String base64Encode(String input) {
	String encodedString = "";
	try {
	    encodedString = Base64.getEncoder().encodeToString(input.getBytes("utf-8"));
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
	  return encodedString;
    } // base64Encode

    // ===============================================================
    // String base64Decode() - base64 decodes argument and returns decoded string
    //
    private static String base64Decode(String input) {
	byte[] decodedBytes = Base64.getDecoder().decode(input);
	return new String(decodedBytes);
    } // base64Decode

} // PASJava
