package org.jenkinsci.plugins.JIRADefectLogger;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import hudson.model.TaskListener;

public class DefectController {
	public static void logDefects(String url, String username, String password, String projectKey, String issueType, String defectXml,TaskListener listener){
		try{
			listener.getLogger().println("Analyzing Defects........");
			DefectLogger.initializeDefectLogger(url, username, password, projectKey, issueType);	
			File inputFile = new File(defectXml);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			NodeList defectList = doc.getElementsByTagName("defect");
			if(defectList.getLength()==0)
				listener.getLogger().println("No Defects Found....Finishing Build");
			else{
				listener.getLogger().println(defectList.getLength()+" defects found...Initializing Logger...");
				for(int i=0;i<defectList.getLength();i++){
					Node defect=defectList.item(i);
					NodeList attributes=defect.getChildNodes();
					String summary=attributes.item(0).getTextContent();
					String desc=attributes.item(1).getTextContent();
					System.out.println(summary);
					System.out.println(desc);
					DefectLogger.logDefect(summary, desc,listener);
				}
				System.out.println(DefectLogger.getNumberOfDefectsLogged()+" Defects Logged......");
				listener.getLogger().println(DefectLogger.getNumberOfDefectsLogged()+" Defects Logged......");
				DefectLogger.finishLogging();
			}
		}catch(Exception e){
			listener.getLogger().println(e.getMessage());
		}

	}
}
