package org.jenkinsci.plugins.JIRADefectLogger;

import java.io.File;
import java.io.IOException;

import javax.naming.AuthenticationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

public class FormValidator {
	public static String validateUrl(String url,String username,String password){
		String auth = new String(Base64.encode(username+":"+password));
		System.out.println(auth);
		try {
			String response = invokeGetMethod(auth, url+"/rest/api");
			if(response.contains("Page Unavailable"))
				return "Invalid URL";
			return "Connection Established";
		} catch (AuthenticationException e) {
			System.out.println("Username or Password wrong!");
			e.printStackTrace();
			return "Invalid Username or Password";

		} catch (ClientHandlerException e) {
			System.out.println("Error invoking REST method");
			e.printStackTrace();
			return "Connection Error";
		}
	}

	public static String validateProject(String url,String username,String password,String projectKey){
		String auth = new String(Base64.encode(username+":"+password));
		System.out.println(auth);
		try {
			String response = invokeGetMethod(auth, url+"/rest/api");
			if(response.contains("Page Unavailable"))
				return "Connection Error";
			String projects = invokeGetMethod(auth, url+"/rest/api/2/project");
			System.out.println(projects);
			JSONArray projectArray = new JSONArray(projects);
			for (int i = 0; i < projectArray.length(); i++) {
				JSONObject proj = projectArray.getJSONObject(i);
				if(proj.getString("key").equals(projectKey)){
					return "Vaild Project Key";
				}
			}
			return "Invalid Project Key";
		} catch (AuthenticationException e) {
			System.out.println("Username or Password wrong!");
			e.printStackTrace();
			return "Connection Error";

		} catch (ClientHandlerException e) {
			System.out.println("Error invoking REST method");
			e.printStackTrace();
			return "Connection Error";
		}catch (JSONException e) {
			System.out.println("Invalid JSON output");
			e.printStackTrace();
			return "Connection Error";
		}
	}

	public static String validateIssueType(String url,String username,String password,String issueType){
		String auth = new String(Base64.encode(username+":"+password));
		System.out.println(auth);
		try {
			String response = invokeGetMethod(auth, url+"/rest/api");
			if(response.contains("Page Unavailable"))
				return "Connection Error";
			String issuetypes = invokeGetMethod(auth, url+"/rest/api/2/issuetype");
			System.out.println(issuetypes);
			JSONArray projectArray = new JSONArray(issuetypes);
			for (int i = 0; i < projectArray.length(); i++) {
				JSONObject proj = projectArray.getJSONObject(i);
				if(proj.getString("name").equals(issueType)){
					return "Vaild Issue Type";
				}
			}
			return "Invalid Issue Type";
		} catch (AuthenticationException e) {
			System.out.println("Username or Password wrong!");
			e.printStackTrace();
			return "Connection Error";

		} catch (ClientHandlerException e) {
			System.out.println("Error invoking REST method");
			e.printStackTrace();
			return "Connection Error";
		}catch (JSONException e) {
			System.out.println("Invalid JSON output");
			e.printStackTrace();
			return "Connection Error";
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

	public static boolean validateDefectXml(String file){
		try{
			File inputFile = new File(file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			NodeList defectList = doc.getElementsByTagName("defect");
			for(int i=0;i<defectList.getLength();i++){
				Node defect=defectList.item(i);
				NodeList attributes=defect.getChildNodes();
				if(attributes.getLength()!=2)
					return false;
				if(!(attributes.item(0).getNodeName().equals("summary")&&attributes.item(1).getNodeName().equals("description")))
					return false;
			}
			return true;
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

}
