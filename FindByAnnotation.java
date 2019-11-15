/*
 * Retrieves DAP Password based on PAS properties.
 */

import com.google.gson.Gson;

public class FindByAnnotation {
    public static void main(String[] args) {

	if(args.length != 1) {
	    System.out.println("Requires search string as argument.");
	    System.exit(-1);
	}

	DAPJava.initJavaKeyStore(
				System.getenv("JAVA_KEY_STORE_FILE"),
				System.getenv("JAVA_KEY_STORE_PASSWORD")
				);
	DAPJava.initConnection(
				System.getenv("CONJUR_APPLIANCE_URL"),
				System.getenv("CONJUR_ACCOUNT")
				);
	DAPJava.authenticate(
				System.getenv("CONJUR_AUTHN_LOGIN"),
				System.getenv("CONJUR_AUTHN_API_KEY")
				);

	String searchAttributes = args[0];
	System.out.println("Looking for variable with attribute: " + searchAttributes);

	// use Google gson class to parse DAPJava json search output into Variable class array
	Gson gson = new Gson();
	Variable[] var = (Variable[]) gson.fromJson( DAPJava.search(searchAttributes), Variable[].class );
	switch(var.length) {
		case 0 :
			System.out.println("No variable found with attributes: " + searchAttributes);
			System.exit(-1);
			break;
		case 1 :	
	  		String varId = var[0].id.split(":")[2]; // variable id is in format "account:variable:id"
			System.out.println("Found " + varId);
			System.out.println(" with value: " + DAPJava.variableValue(varId));
			break;
		default :
			System.out.println("More than one variable found with attributes: " + searchAttributes);
			for (Integer i = 0; i < var.length; i++) {
		            System.out.println(var[i].id.split(":")[2]);
       			}
			System.exit(-1);
	} // switch

    } // main()

} // FindByAnnotation
