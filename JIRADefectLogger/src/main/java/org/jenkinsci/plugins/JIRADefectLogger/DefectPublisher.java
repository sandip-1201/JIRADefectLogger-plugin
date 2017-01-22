package org.jenkinsci.plugins.JIRADefectLogger;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link DefectPublisher} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class DefectPublisher extends Recorder implements SimpleBuildStep {

	private final String url;
	private final String username;
	private final String password;
	private final String projectkey;
	private final String issuetype;
	private final String defectxml;

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public DefectPublisher(String url,String username,String password,String projectkey,String issuetype,String defectxml) {
		this.url = url;
		this.username=username;
		this.password=password;
		this.projectkey=projectkey;
		this.issuetype=issuetype;
		this.defectxml=defectxml;
	}

	/**
	 * We'll use this from the {@code config.jelly}.
	 */
	/*public String getName() {
		return name;
	}*/

	@Override
	public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
		// This is where you 'build' the project.
		// Since this is a dummy, we just say 'hello world' and call that a build.

		// This also shows how you can consult the global configuration of the builder
		listener.getLogger().println("Preparing to Log defects");
		DefectController.logDefects(url, username, password, projectkey, issuetype, defectxml,listener);
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}

	/**
	 * Descriptor for {@link DefectPublisher}. Used as a singleton.
	 * The class is marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See {@code src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly}
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		/**
		 * To persist global configuration information,
		 * simply store it in a field and call save().
		 *
		 * <p>
		 * If you don't want fields to be persisted, use {@code transient}.
		 */
		private boolean useFrench;

		/**
		 * In order to load the persisted global configuration, you have to 
		 * call load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}

		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 *
		 * @param value
		 *      This parameter receives the value that the user has typed.
		 * @return
		 *      Indicates the outcome of the validation. This is sent to the browser.
		 *      <p>
		 *      Note that returning {@link FormValidation#error(String)} does not
		 *      prevent the form from being saved. It just means that a message
		 *      will be displayed to the user. 
		 */
		public FormValidation doCheckUrl(@QueryParameter String url, @QueryParameter String username, @QueryParameter String password)
				throws IOException, ServletException {
			String message=FormValidator.validateUrl(url, username, password);
			if (message.equals("Invalid URL"))
				return FormValidation.error("Invalid JIRA URL");
			else if (message.equals("Invalid Username or Password"))
				return FormValidation.error("Invalid Username or Password");
			else if(message.equals("Connection Error"))
				return FormValidation.error("Connection Error");
			else if(message.equals("Connection Established"))
				return FormValidation.ok();
			return FormValidation.ok();
		}

		public FormValidation doCheckProjectkey(@QueryParameter String url, @QueryParameter String username, @QueryParameter String password, @QueryParameter String projectkey)
				throws IOException, ServletException {
			String message=FormValidator.validateProject(url, username, password, projectkey);
			if(message.equals("Connection Error"))
				return FormValidation.error("Please enter valid JIRA url and creadentials first");
			else if(message.equals("Invalid Project Key"))
				return FormValidation.error("Invalid Project Key");
			else if(message.equals("Valid Project Key"))
				return FormValidation.ok();
			return FormValidation.ok();
		}

		public FormValidation doCheckIssuetype(@QueryParameter String url, @QueryParameter String username, @QueryParameter String password, @QueryParameter String issuetype)
				throws IOException, ServletException {
			String message=FormValidator.validateIssueType(url, username, password, issuetype);
			if(message.equals("Connection Error"))
				return FormValidation.error("Please enter valid JIRA url and creadentials first");
			else if(message.equals("Invalid Issue Type"))
				return FormValidation.error("Invalid Issue Type");
			else if(message.equals("Valid Issue Type"))
				return FormValidation.ok();
			return FormValidation.ok();
		}
		public FormValidation doCheckDefectxml(@QueryParameter String defectxml)
				throws IOException, ServletException {
			if(!FormValidator.validateDefectXml(defectxml))
				return FormValidation.error("Invalid Defect XML");
			else
				return FormValidation.ok();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project types 
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Log Defects to JIRA";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			useFrench = formData.getBoolean("useFrench");
			// ^Can also use req.bindJSON(this, formData);
			//  (easier when there are many fields; need set* methods for this, like setUseFrench)
			save();
			return super.configure(req,formData);
		}

		/**
		 * This method returns true if the global configuration says we should speak French.
		 *
		 * The method name is bit awkward because global.jelly calls this method to determine
		 * the initial state of the checkbox by the naming convention.
		 */
		public boolean getUseFrench() {
			return useFrench;
		}
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		// TODO Auto-generated method stub
		return BuildStepMonitor.NONE;
	}
}

