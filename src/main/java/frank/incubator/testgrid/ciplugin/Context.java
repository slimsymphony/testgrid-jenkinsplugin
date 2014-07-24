package frank.incubator.testgrid.ciplugin;

import frank.incubator.testgrid.ciplugin.logging.Logger;
import frank.incubator.testgrid.ciplugin.results.PublishAction;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.util.List;
import java.util.Map;

public class Context {

	private AbstractBuild<?, ?> build;
	private BuildListener listener;
	private Launcher launcher;
	private String testGridAddress;
	private String testStrategy;
	private String testTool;
	private String flashArguments;
	private Logger logger;
	private FilePath workspace;
	private String owner;
	private String releasePackageUrl;				// URL of release package.
	private String testPackageUrls;					// URL of test package. Multiple lines. Each line represents one test package URL
	private String originalTestsetUrl;				// URL of original testset.
	private String deviceRequirements;				// Device requirements
	private String releasePackageName;				// Release package name, e.g. "athena-eng.zip"
	private List<String> testPackageNames;			// Test package names, e.g. ["test-package-xxx-1.zip", "test-package-xxx-2.zip", "test-package-xxx-3.zip"]
	private String originalTestsetName;				// Original testset name. e.g. marble_rfa_all.testset
	private String taskTimeout;
	private String taskPublishTimeout;
	private String taskReserveTimeout;
	private String testTimeout;
	private String testAuxiliaries;
	private PublishAction publishAction;
	private int splitCnt;
	private int resultZipCnt;
	Map<String, Object> deviceRequirementsMap;
	
	public Map<String, Object> getDeviceRequirementsMap() {
		return deviceRequirementsMap;
	}

	public void setDeviceRequirementsMap(Map<String, Object> deviceRequirementsMap) {
		this.deviceRequirementsMap = deviceRequirementsMap;
	}

	public int getResultZipCnt() {
		return resultZipCnt;
	}

	public void setResultZipCnt(int resultZipCnt) {
		this.resultZipCnt = resultZipCnt;
	}

	public int getSplitCnt() {
		return splitCnt;
	}

	public void setSplitCnt(int splitCnt) {
		this.splitCnt = splitCnt;
	}

	public PublishAction getPublishAction() {
		return publishAction;
	}

	public void setPublishAction(PublishAction publishAction) {
		this.publishAction = publishAction;
	}
	
	public String getTaskTimeout() {
		return taskTimeout;
	}

	public void setTaskTimeout(String taskTimeout) {
		this.taskTimeout = taskTimeout;
	}

	public String getTaskPublishTimeout() {
		return taskPublishTimeout;
	}

	public void setTaskPublishTimeout(String taskPublishTimeout) {
		this.taskPublishTimeout = taskPublishTimeout;
	}

	public String getTaskReserveTimeout() {
		return taskReserveTimeout;
	}

	public void setTaskReserveTimeout(String taskReserveTimeout) {
		this.taskReserveTimeout = taskReserveTimeout;
	}

	public String getTestTimeout() {
		return testTimeout;
	}

	public void setTestTimeout(String testTimeout) {
		this.testTimeout = testTimeout;
	}

	public FilePath getWorkspace() {
		return workspace;
	}

	public void setWorkspace( FilePath workspace ) {
		this.workspace = workspace;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger( Logger logger ) {
		this.logger = logger;
	}

	public String getTestGridAddress() {
		return testGridAddress;
	}

	public void setTestGridAddress(String testGridAddress) {
		this.testGridAddress = testGridAddress;
	}

	public String getTestStrategy() {
		return testStrategy;
	}

	public void setTestStrategy( String testStrategy ) {
		this.testStrategy = testStrategy;
	}

	public String getTestTool() {
		return testTool;
	}

	public void setTestTool( String testTool ) {
		this.testTool = testTool;
	}

	public String getFlashArguments() {
		return flashArguments;
	}

	public void setFlashArguments( String flashArguments ) {
		this.flashArguments = flashArguments;
	}

	public String getTestAuxiliaries() {
		return testAuxiliaries;
	}

	public void setTestAuxiliaries( String testAuxiliaries ) {
		this.testAuxiliaries = testAuxiliaries;
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public Launcher getLauncher() {
		return launcher;
	}

	public BuildListener getListener() {
		return listener;
	}

	public void setBuild( AbstractBuild<?, ?> build ) {
		this.build = build;
	}

	public void setLauncher( Launcher launcher ) {
		this.launcher = launcher;
	}

	public void setListener( BuildListener listener ) {
		this.listener = listener;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner( String owner ) {
		this.owner = owner;
	}
	public String getReleasePackageUrl() {
		return releasePackageUrl;
	}

	public void setReleasePackageUrl(String releasePackageUrl) {
		this.releasePackageUrl = releasePackageUrl;
	}

	public String getTestPackageUrls() {
		return testPackageUrls;
	}

	public void setTestPackageUrls(String testPackageUrls) {
		this.testPackageUrls = testPackageUrls;
	}
	
	public String getOriginalTestsetUrl() {
		return originalTestsetUrl;
	}

	public void setOriginalTestsetUrl(String originalTestsetUrl) {
		this.originalTestsetUrl = originalTestsetUrl;
	}
	
	public String getDeviceRequirements() {
		return deviceRequirements;
	}

	public void setDeviceRequirements(String deviceRequirements) {
		this.deviceRequirements = deviceRequirements;
	}
	
	public String getReleasePackageName() {
		return releasePackageName;
	}

	public void setReleasePackageName(String releasePackageName) {
		this.releasePackageName = releasePackageName;
	}

	public List<String> getTestPackageNames() {
		return testPackageNames;
	}

	public void setTestPackageNames(List<String> testPackageNames) {
		this.testPackageNames = testPackageNames;
	}

	public String getOriginalTestsetName() {
		return originalTestsetName;
	}

	public void setOriginalTestsetName(String originalTestsetName) {
		this.originalTestsetName = originalTestsetName;
	}

	public Context() {
	}
}
