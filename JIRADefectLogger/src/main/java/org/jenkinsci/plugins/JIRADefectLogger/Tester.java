package org.jenkinsci.plugins.JIRADefectLogger;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import hudson.console.ConsoleNote;
import hudson.model.TaskListener;

public class Tester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//	DefectLogger.initializeDefectLogger("https://jiradevops.atlassian.net","sandipbhattacharjee1201@gmail.com","intel12345","DEV","Bug");
	//	DefectLogger.Demo();
		System.out.println(FormValidator.validateDefectXml("D:\\DefectTrace.xml"));
	}

}
