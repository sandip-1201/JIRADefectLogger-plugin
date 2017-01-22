package org.jenkinsci.plugins.JIRADefectLogger;

import javax.naming.AuthenticationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

import hudson.model.TaskListener;

public class DefectLogger {
	
	private static String BASE_URL;
	private static String userName;
	private static String passWord;
	private static String projectKey;
	private static String issueType;
	private static int counter=0;
	
	public static void initializeDefectLogger(String url, String username, String password, String projectkey, String issuetype){
		BASE_URL=url;
		userName=username;
		passWord=password;
		projectKey=projectkey;
		issueType=issuetype;
	}
	
	public static void Demo(){
	String auth = new String(Base64.encode(userName+":"+passWord));
		System.out.println(auth);
		try {
			//Get Projects
			String projects = invokeGetMethod(auth, BASE_URL+"/rest/api/2/issuetype");
			System.out.println(projects);
			JSONArray projectArray = new JSONArray(projects);
			for (int i = 0; i < projectArray.length(); i++) {
				JSONObject proj = projectArray.getJSONObject(i);
				System.out.println("Key:"+proj.getString("name"));
			}
		} catch (AuthenticationException e) {
			System.out.println("Username or Password wrong!");
			e.printStackTrace();
		} catch (ClientHandlerException e) {
			System.out.println("Error invoking REST method");
			e.printStackTrace();
		} catch (JSONException e) {
			System.out.println("Invalid JSON output");
			e.printStackTrace();
		}
	}
	
	public static void finishLogging(){
		counter=0;
	}
	
	public static int getNumberOfDefectsLogged(){
		return counter;
	}
	
	public static void logDefect(String summary, String desc, TaskListener listener) {
		
		String auth = new String(Base64.encode(userName+":"+passWord));
		
		try {
			System.out.println("Logging New Defect...........................");
			
			String createIssueData = "{\"fields\":{\"project\":{\"key\":\""+projectKey+"\"},\"summary\":\""+summary+"\",\"description\":\""+desc+"\",\"issuetype\":{\"name\":\""+issueType+"\"}}}";
			String issue = invokePostMethod(auth, BASE_URL+"/rest/api/2/issue", createIssueData);
			System.out.println(issue);
			JSONObject issueObj = new JSONObject(issue);
			String newKey = issueObj.getString("key");
			System.out.println("Key:"+newKey);
			
			System.out.println("Defect Logged with Key "+newKey);
			listener.getLogger().println("Defect Logged with Key "+newKey);
			counter++;
			
	/*		//Update Issue
			String editIssueData = "{\"fields\":{\"assignee\":{\"name\":\"test\"}}}";
			invokePutMethod(auth, BASE_URL+"/rest/api/2/issue/"+newKey, editIssueData);
			
			invokeDeleteMethod(auth, BASE_URL+"/rest/api/2/issue/DEMO-13");
			*/
		} catch (AuthenticationException e) {
			System.out.println("Username or Password wrong!");
			listener.getLogger().println(e.getMessage());
		} catch (ClientHandlerException e) {
			System.out.println("Error invoking REST method");
			listener.getLogger().println(e.getMessage());
		} catch (JSONException e) {
			System.out.println("Invalid JSON output");
			listener.getLogger().println(e.getMessage());
		}

	}

	private static String invokeGetMethod(String auth, String url) throws AuthenticationException, ClientHandlerException {
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
				.accept("application/json").get(ClientResponse.class);
		int statusCode = response.getStatus();
		if (statusCode == 401) {
			throw new AuthenticationException("Invalid Username or Password");
		}
		return response.getEntity(String.class);
	}
	
	private static String invokePostMethod(String auth, String url, String data) throws AuthenticationException, ClientHandlerException {
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
				.accept("application/json").post(ClientResponse.class, data);
		int statusCode = response.getStatus();
		if (statusCode == 401) {
			throw new AuthenticationException("Invalid Username or Password");
		}
		return response.getEntity(String.class);
	}
	
	private static void invokePutMethod(String auth, String url, String data) throws AuthenticationException, ClientHandlerException {
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
				.accept("application/json").put(ClientResponse.class, data);
		int statusCode = response.getStatus();
		if (statusCode == 401) {
			throw new AuthenticationException("Invalid Username or Password");
		}
	}
	
	private static void invokeDeleteMethod(String auth, String url) throws AuthenticationException, ClientHandlerException {
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
				.accept("application/json").delete(ClientResponse.class);
		int statusCode = response.getStatus();
		if (statusCode == 401) {
			throw new AuthenticationException("Invalid Username or Password");
		}
	}

}
