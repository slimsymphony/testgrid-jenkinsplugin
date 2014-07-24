package frank.incubator.testgrid.ciplugin;

import frank.incubator.testgrid.ciplugin.builder.AbstractBuilder;
import frank.incubator.testgrid.ciplugin.builder.BuilderFactory;
import frank.incubator.testgrid.ciplugin.logging.Logger;
import frank.incubator.testgrid.ciplugin.results.PublishAction;
import frank.incubator.testgrid.ciplugin.util.BuilderEnum;
import frank.incubator.testgrid.ciplugin.util.CommonConstants;
import frank.incubator.testgrid.ciplugin.util.Utils;
import frank.incubator.testgrid.common.CommonUtils;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * This is the entry class of this project. It inherits from Builder class and
 * implements data bound constructor, which takes arguments from UI
 * configuration as parameters. This class also has getters for each argument
 * used in the UI.
 * 
 * One of the most important overridden methods is 'perform'. Once this build
 * step starts, that method is accessed by the framework. In perform-method,
 * user selected tool specific builders are accessed and run. If perform-method
 * returns false, then next build step is not run.
 * 
 * This class sets build result and action in this method to indicate results
 * and pass data to the metrics tool.
 */
public class TestGridBuilder extends Builder {

	// Constructor parameters
	private final String testGridAddress;
	private final String testStrategy;
	private final String testTool;
	private final String flashArguments;
	private final String taskTimeout;
	private final String taskPublishTimeout;
	private final String taskReserveTimeout;
	private final String testTimeout;
	private final String testAuxiliaries;
	private final String releasePackageUrl;
	private final String testPackageUrls;
	private final String originalTestsetUrl;
	private final String deviceRequirements;

	/**
	 * Constructor that takes all configurable field values as parameters.
	 * 
	 * @param test
	 */
	@DataBoundConstructor
	public TestGridBuilder(String testGridAddress, String testFarmEntry, String testStrategy, String testTool,
			String flashArguments, String testToolRepo, String testToolRepoBranch, String testToolRepoTag, String testCaseRepo,
			String testCaseRepoBranch, String buildTarget, String taskTimeout, String taskPublishTimeout,
			String taskReserveTimeout, String testTimeout, String testAuxiliaries, String releasePackageUrl,
			String testPackageUrls, String originalTestsetUrl, String deviceRequirements) {
		this.testGridAddress = testGridAddress;
		this.testStrategy = testStrategy;
		this.testTool = testTool;
		this.flashArguments = flashArguments;
		this.taskTimeout = taskTimeout;
		this.taskPublishTimeout = taskPublishTimeout;
		this.taskReserveTimeout = taskReserveTimeout;
		this.testTimeout = testTimeout;
		this.testAuxiliaries = testAuxiliaries;
		this.releasePackageUrl = releasePackageUrl;
		this.testPackageUrls = testPackageUrls;
		this.originalTestsetUrl = originalTestsetUrl;
		this.deviceRequirements = deviceRequirements;
	}

	/**
	 * Entry point when this plug-in is executed.
	 * 
	 * Returns true if this run was successful and next build step should be
	 * run.
	 * 
	 * @param build
	 * @param launcher
	 * @param listener
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		Context context = new Context();
		PublishAction publishAction = new PublishAction();
		try {
			context.setPublishAction(publishAction);
			context.setTestGridAddress(testGridAddress);
			context.setTestTool(testTool);
			context.setTestStrategy(testStrategy);
			context.setFlashArguments(flashArguments);
			context.setBuild(build);
			context.setLauncher(launcher);
			context.setListener(listener);
			context.setWorkspace(build.getWorkspace());
			Logger logger = new Logger(listener.getLogger());
			context.setLogger(logger);
			context.setOwner(Utils.getTaskOwner(context));
			context.setTaskTimeout(taskTimeout);
			context.setTaskPublishTimeout(taskPublishTimeout);
			context.setTaskReserveTimeout(taskReserveTimeout);
			context.setTestTimeout(testTimeout);
			context.setTestAuxiliaries(testAuxiliaries);
			context.setReleasePackageUrl(releasePackageUrl);
			context.setTestPackageUrls(testPackageUrls);
			context.setOriginalTestsetUrl(originalTestsetUrl);
			context.setDeviceRequirements(deviceRequirements);
			// Expand parameters from UI
			expandParameters(build, listener, context);
			// Print system information
			printSystemInformation(context);

			// Resolve tool
			String tool = StringUtils.upperCase(context.getTestTool());
			BuilderEnum builderEnum = BuilderEnum.valueOf(tool);
			AbstractBuilder builder = BuilderFactory.createBuilder(builderEnum);
			logFormat(context.getLogger(), "builder perform start...");
			builder.perform(context);
			logFormat(context.getLogger(), "builder perform end...");

			// Print how long it took to run this build step
			logFormat(context.getLogger(), "Total time: " + build.getDurationString());

		} catch (IOException e) {
			logFormat(context.getLogger(), "IOException occurred: " + e.getMessage());
			logFormat(context.getLogger(), "Stack trace:" + CommonUtils.getErrorStack(e));
			build.setResult(Result.FAILURE);
		} catch (InterruptedException e) {
			logFormat(context.getLogger(), "InterruptedException occurred: " + e.getMessage());
			logFormat(context.getLogger(), "Stack trace:" + CommonUtils.getErrorStack(e));
			build.setResult(Result.ABORTED);
		} catch (Exception e) {
			logFormat(context.getLogger(), "Exception occurred: " + e.getMessage());
			logFormat(context.getLogger(), "Stack trace:" + CommonUtils.getErrorStack(e));
			build.setResult(Result.FAILURE);
		} finally {
			build.addAction(publishAction);
			logFormat(context.getLogger(), "Result is " + build.getResult());
			logFormat(context.getLogger(), "Set Result start...");
			setResult(context);
			logFormat(context.getLogger(), "Set Result end...");

		}
		return true;
	}

	private void expandParameters(AbstractBuild<?, ?> build, BuildListener listener, Context context) throws Exception {
		String testGridAddress = Utils.expandParameters(build, listener, context.getTestGridAddress());
		String testTool = Utils.expandParameters(build, listener, context.getTestTool());
		String flashArguments = Utils.expandParameters(build, listener, context.getFlashArguments());
		String testStrategy = Utils.expandParameters(build, listener, context.getTestStrategy());

		String taskTimeout = Utils.expandParameters(build, listener, context.getTaskTimeout());
		String taskPublishTimeout = Utils.expandParameters(build, listener, context.getTaskPublishTimeout());
		String taskReserveTimeout = Utils.expandParameters(build, listener, context.getTaskReserveTimeout());
		String testTimeout = Utils.expandParameters(build, listener, context.getTestTimeout());
		String releasePackageUrl = Utils.expandParameters(build, listener, context.getReleasePackageUrl());
		String testPackageUrls = Utils.expandParameters(build, listener, context.getTestPackageUrls());
		String testAuxiliaries = Utils.expandParameters(build, listener, context.getTestAuxiliaries());
		String deviceRequirements = Utils.expandParameters(build, listener, context.getDeviceRequirements());
		String originalTestsetUrl = Utils.expandParameters(build, listener, context.getOriginalTestsetUrl());
		Map<String, Object> deviceRequirementsMap = new HashMap<String, Object>();
		String releasePackageName = "";
		ArrayList<String> testPackageNames = new ArrayList<String>();
		String originalTestsetName = "";
		// Handle case that release package && test package are specified

		// Copy release package to workspace if necessary
		FilePath releasePackageSource = new FilePath(build.getWorkspace().getChannel(), releasePackageUrl);
		releasePackageName = releasePackageSource.getName();
		FilePath releasePackageTarget = context.getWorkspace().child(releasePackageName);
		if (!releasePackageSource.exists()) {
			throw new Exception("Error: specified release package does not exist!");
		} else if (releasePackageSource.equals(releasePackageTarget)) {
			logFormat(context.getLogger(), "Info: specified release package is in workspace, no need to copy.");
		} else {
			releasePackageSource.copyTo(releasePackageTarget);
			logFormat(context.getLogger(), "Info: successfully copied release package from: " + releasePackageUrl);
		}
		// Copy test packages to workspace, extract and read needed information
		for (String testPackageUrl : testPackageUrls.split("\n")) {
			// Copy test packages to workspace
			testPackageUrl = testPackageUrl.trim(); // Trim '\r' if it exists
			FilePath testPackageSource = new FilePath(build.getWorkspace().getChannel(), testPackageUrl);
			String testPackageName = testPackageSource.getName();
			FilePath testPackageTarget = context.getWorkspace().child(testPackageName);
			if (!testPackageSource.exists()) {
				throw new Exception("Error: specified test package does not exist!");
			} else if (testPackageSource.equals(testPackageTarget)) {
				logFormat(context.getLogger(), "Info: specified test package is in workspace, no need to copy.");
			} else {
				testPackageSource.copyTo(testPackageTarget);
				logFormat(context.getLogger(), "Info: successfully copied release package from: " + testPackageUrl);
			}
			// Add to test package name list
			testPackageNames.add(testPackageName);
		}
		// Copy original testset if it's specified
		if (!StringUtils.isBlank(originalTestsetUrl)) {
			FilePath originalTestsetSource = new FilePath(build.getWorkspace().getChannel(), originalTestsetUrl);
			originalTestsetName = originalTestsetSource.getName();
			FilePath originalTestsetTarget = context.getWorkspace().child(originalTestsetName);
			if (!originalTestsetSource.exists()) {
				throw new Exception("Error: specified original testset file does not exist!");
			} else if (originalTestsetSource.equals(originalTestsetTarget)) {
				logFormat(context.getLogger(), "Info: specified original testset file is in workspace, no need to copy.");
			} else {
				originalTestsetSource.copyTo(originalTestsetTarget);
				logFormat(context.getLogger(), "Info: successfully copied original testset from: " + originalTestsetUrl);
			}
		} else {
			logFormat(context.getLogger(),
					"Warning: original testset is blank. It's OK for UT. But there will be problem if this is a Marble test.");
		}
		// Parse device requirements
		Properties props = new Properties();
		props.load(new StringReader(deviceRequirements));
		Iterator<Entry<Object, Object>> iter = props.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) iter.next();
			String requirementKey = entry.getKey().toString();
			if (!entry.getValue().toString().equals(StringUtils.EMPTY)) {
				deviceRequirementsMap.put(requirementKey, entry.getValue().toString());
			}
		}

		context.setTestGridAddress(testGridAddress);
		context.setTestTool(testTool);
		context.setTestStrategy(testStrategy);
		context.setFlashArguments(flashArguments);
		context.setReleasePackageUrl(releasePackageUrl);
		context.setTestPackageUrls(testPackageUrls);
		context.setOriginalTestsetUrl(originalTestsetUrl);
		context.setReleasePackageName(releasePackageName);
		context.setTestPackageNames(testPackageNames);
		context.setOriginalTestsetName(originalTestsetName);
		context.setDeviceRequirements(deviceRequirements);
		context.setDeviceRequirementsMap(deviceRequirementsMap);
		context.setTaskTimeout(taskTimeout);
		context.setTaskPublishTimeout(taskPublishTimeout);
		context.setTaskReserveTimeout(taskReserveTimeout);
		context.setTestTimeout(testTimeout);
		context.setTestAuxiliaries(testAuxiliaries);
	}

	private void printSystemInformation(Context context) {
		AbstractBuild<?, ?> build = context.getBuild();
		Logger logger = context.getLogger();
		// Print info about plug-in and the system configuration
		logger.log("\n-----------------------------------------------------");
		logger.log("TestGrid Plugin");
		logger.log(String.format("    Plug-in: testgrid-ciplugin.hpi [%1$s]",
				Hudson.getInstance().getPluginManager().getPlugin("testgrid-ciplugin").getVersion()));
		logger.log(String.format("    Bugs/Feature Request: %1$s\n", "N/A"));
		logger.log(String.format("    Machine: %1$s", build.getBuiltOnStr()));
		logger.log(String.format("    Jenkins version: %1$s", build.getHudsonVersion()));
		logger.log(String.format("    Java version: %1$s", System.getProperty("java.version")));
		logger.log(String.format("    OS version: %1$s [%2$s] %3$s", System.getProperty("os.name"),
				System.getProperty("os.version"), System.getProperty("os.arch")));
		logger.log(String.format("    User/Country: %1$s [%2$s - %3$s]", System.getProperty("user.name"),
				System.getProperty("user.timezone"), System.getProperty("user.country")));
		logger.log(String.format("    Build Link: %1$s%2$s", Hudson.getInstance().getRootUrl(), build.getUrl()));
		logger.log(String.format("    Build Workspace: %1$s", build.getWorkspace().getRemote()));
		logger.log("-------------------------------------------------------");
		logger.log("---- Tool Setting--------------------");
		logger.log("---- Test Tool:               " + context.getTestTool());
		logger.log("---- Flash Arguments:         " + context.getFlashArguments());
		logger.log("-------------------------------------");
		logger.log("---- Tool And Case Location----------");
		logger.log("---- Releage Package Url:     " + context.getReleasePackageUrl());
		logger.log("---- Test Package Urls:       " + context.getTestPackageUrls());
		logger.log("---- Original Testset Url:    " + context.getOriginalTestsetUrl());
		if (StringUtils.isNotBlank(context.getDeviceRequirements())) {
			logger.log("---- Device requirements(from job configuration):" + context.getDeviceRequirements());
		} else {
			logger.log("---- Device requirements(from file job.info):" + context.getDeviceRequirementsMap().toString());
		}
		logger.log("-------------------------------------");
		logger.log("---- Allocation options--------------");
		logger.log("---- TestGrid Address:       " + context.getTestGridAddress());
		logger.log("-------------------------------------");
		logger.log("---- Advanced Options----------------");
		logger.log("---- Task Timeout:            " + context.getTaskTimeout() + " minutes");
		logger.log("---- Task Publish Timeout:    " + context.getTaskPublishTimeout() + " minutes");
		logger.log("---- Task Reserve Timeout:    " + context.getTaskReserveTimeout() + " minutes");
		logger.log("---- Test Timeout:            " + context.getTestTimeout() + " minutes");
		logger.log("---- Test Strategy:           " + context.getTestStrategy());
		logger.log("---- Test Auxiliaries:        " + context.getTestAuxiliaries());
		logger.log("-------------------------------------");
		logger.log("-------------------------------------------------------\n");
	}

	private void logFormat(Logger logger, String message) {
		logger.log(this.getClass().getSimpleName(), message);
	}

	private void setResult(Context context) {
		if (context.getBuild().getResult() == null) {
			if (context.getPublishAction().getTestcaseCnt() == 0) {
				logFormat(context.getLogger(), "No test cases run-> FAILURE");
				context.getBuild().setResult(Result.FAILURE);
			} else if (context.getSplitCnt() > context.getResultZipCnt()) {
				logFormat(context.getLogger(), "Some result zip files are missing -> FAILURE");
				context.getBuild().setResult(Result.FAILURE);
			} else if (context.getPublishAction().getFailureCnt() > 0) {
				logFormat(context.getLogger(), "Failures exists -> UNSTABLE");
				context.getBuild().setResult(Result.UNSTABLE);
			} else {
				logFormat(context.getLogger(), "SUCCESS");
				context.getBuild().setResult(Result.SUCCESS);
			}
		}
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Implementation for configuration page extension. Configuration in
	 * TestGridBuilder - config.jelly. Form validation and default values must
	 * be implemented to this class.
	 */
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		/**
		 * Constructor for this class
		 */
		public DescriptorImpl() {
			super(TestGridBuilder.class);
			load();
		}

		// Default values
		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultTestGridAddress() {
			return CommonConstants.DEFAULT_TESTGRID_ADDRESS;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultTestTool() {
			return CommonConstants.DEFAULT_TEST_TOOL;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultFlashArguments() {
			return CommonConstants.DEFAULT_FLASH_ARGUMENTS;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultTestStrategy() {
			return CommonConstants.DEFAULT_TESTSTRATEGY;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultTaskTimeout() {
			return CommonConstants.DEFAULT_TASK_TIMEOUT;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultTaskPublishTimeout() {
			return CommonConstants.DEFAULT_TASK_PUBLISH_TIMEOUT;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultTaskReserveTimeout() {
			return CommonConstants.DEFAULT_TASK_RESERVE_TIMEOUT;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultTestTimeout() {
			return CommonConstants.DEFAULT_TEST_TIMEOUT;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultTestAuxiliaries() {
			return CommonConstants.DEFAULT_TEST_AUXILIARIES;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultReleasePackageUrl() {
			return CommonConstants.DEFAULT_RELEASE_PACKAGE_URI;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDefaultTestPackageUrls() {
			return CommonConstants.DEFAULT_TEST_PACKAGE_URIS;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getDeviceRequirements() {
			return CommonConstants.DEFAULT_DEVICE_REQUIREMENTS;
		}

		/**
		 * Field's default value
		 * 
		 * @return
		 */
		public String getOriginalTestsetUrl() {
			return CommonConstants.DEFAULT_ORIGINAL_TESTSET_URL;
		}

		// Validation checks
		/**
		 * Validation of the form field. Checks if field is empty.
		 * 
		 * @param value
		 * @return
		 * @throws IOException
		 * @throws ServletException
		 */
		public FormValidation doChecktestGridAddress(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error("A value is required for the TestGrid Adrress. (default: "
						+ CommonConstants.DEFAULT_TESTGRID_ADDRESS + ")");
			}
			return FormValidation.ok();
		}

		/**
		 * Validation of the form field. Checks if field is empty.
		 * 
		 * @param value
		 * @return
		 * @throws IOException
		 * @throws ServletException
		 */
		public FormValidation doCheckTestTool(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error("A value is required for the Test Tool. (default: "
						+ CommonConstants.DEFAULT_TEST_TOOL + ")");
			}
			return FormValidation.ok();
		}

		/**
		 * Returns true if this task is applicable to the given project.
		 * 
		 * NOTE: Warnings suppressed due to data is called by Jenkins base and
		 * this is type safe.
		 * 
		 * @param aClass
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return FreeStyleProject.class.isAssignableFrom(aClass);
		}

		/**
		 * Returns the name used in the configuration screen.
		 * 
		 * @return
		 */
		@Override
		public String getDisplayName() {
			return CommonConstants.PLUGIN_NAME;
		}
	}
}
