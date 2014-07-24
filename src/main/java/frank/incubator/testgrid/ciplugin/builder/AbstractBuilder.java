package frank.incubator.testgrid.ciplugin.builder;

import hudson.FilePath;
import hudson.model.Hudson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;

import com.google.gson.reflect.TypeToken;
import frank.incubator.testgrid.ciplugin.Context;
import frank.incubator.testgrid.ciplugin.logging.Logger;
import frank.incubator.testgrid.ciplugin.util.CommonConstants;
import frank.incubator.testgrid.ciplugin.util.Utils;
import frank.incubator.testgrid.client.TaskClient;
import frank.incubator.testgrid.client.TestStrategy;
import frank.incubator.testgrid.common.CommonUtils;
import frank.incubator.testgrid.common.file.FileTransferDescriptor;
import frank.incubator.testgrid.common.message.BrokerDescriptor;
import frank.incubator.testgrid.common.model.Device;
import frank.incubator.testgrid.common.model.DeviceRequirement;
import frank.incubator.testgrid.common.model.Task;
import frank.incubator.testgrid.common.model.Test;
import frank.incubator.testgrid.common.model.TestSuite;

public abstract class AbstractBuilder {
	protected Logger logger;
	/**
	* Each Builder will take this as entry to perform
	* @param context Every param from UI can get from it
	* @return testResult return it to TestGridBuilder
	 * @throws Exception 
	*/
	public void perform( Context context ) throws Exception {
		logger = context.getLogger();
		logFormat("create TaskClient start...");
		TaskClient taskClient = createTaskClient(context);
		logFormat("create TaskClient end...");
		triggerTest(context, taskClient);
		logFormat("handle test result start...");
		handleResult(context);
		logFormat("handle test result end...");
	}

	/**
	* Create TestSuite Contains all kinds of test,it's used to case spliting
	* @param context Every param from UI can get from it
	* @param releaseFilesAndArtifactsZipMap prepared by dataFilesMap
	* @return ts as result
	*/
	protected TestSuite createTestSuite(Context context, Map<String, ArrayList<FilePath>> releaseFilesAndArtifactsZipMap) throws IOException, InterruptedException {
		TestSuite ts=new TestSuite();
		Iterator<Entry<String, ArrayList<FilePath>>> iter = releaseFilesAndArtifactsZipMap.entrySet().iterator();
		int index=1;
		while (iter.hasNext()) {
			Map.Entry<String, ArrayList<FilePath>> entry = (Map.Entry<String, ArrayList<FilePath>>) iter.next();
			ArrayList<FilePath> dataFiles = (ArrayList<FilePath>) entry.getValue();
			ts.addTest(createTest(context, dataFiles, index, entry.getKey()));
			index++;
		}
		return ts;
	}
	
	/**
	* Create Test which will be Contained in TestSuite
	* @param dataFiles prepared by prepareFiles
	* @return test as result
	*/
	protected Test createTest(Context context, List<FilePath> dataFiles, int index, String testPackageName) throws IOException, InterruptedException{
		Test test = new Test();
		String testId = Utils.makeTestId(context, testPackageName);
		logFormat("test id is: " + testId);
		test.setId(testId);
		String resultsFileName = CommonConstants.RESULT_PRIX + index + CommonConstants.ZIP_EXTEND;
		test.setTimeout(Long.parseLong(context.getTestTimeout())*CommonConstants.ONE_MINUTE);
		test.setExecutorApplication(CommonConstants.PYTHON_COMMAND);
		test.setExecutorScript(CommonConstants.STARTUP_SCRIPT);
		test.setExecutorEnvparams(null);
		String executorParameters = " " + context.getFlashArguments() + " " + CommonConstants.EXECUTOR_RESULT_COMMAND + " " + resultsFileName;
		test.setExecutorParameters(executorParameters);
		test.setResultsFilename(resultsFileName);
		test.setArtifacts(createArtifacts(dataFiles));
		test.setUrl(Hudson.getInstance().getRootUrl() + context.getBuild().getUrl());
		return test;
	}
	
	/**
	* Create Artifacts which will be transfered to TestNode
	* @param dataFiles prepared by prepareFiles
	* @return ats as result
	*/
	protected Map<String, Long> createArtifacts(List<FilePath> dataFiles) throws IOException,InterruptedException {
		final Map<String, Long> ats = new HashMap<String, Long>();
		for (FilePath filePath : dataFiles) {
			ats.put(filePath.getName(), filePath.length());
		}
		return ats;
	}
	
	/**
	* Trigger the test to invoke taskClient
	* @param context Every param from UI can get from it
	* @param taskClient client to TestGrid
	 * @throws Exception 
	*/
	public void triggerTest( Context context, TaskClient taskClient ) throws InterruptedException{
		logFormat("trigger Test start...");
		Date taskClientStartTime = new Date();
		try {
			taskClient.begin();
			taskClient.join();
		} catch (InterruptedException e) {
			logFormat("User Manually Cancel the jenkins job");
			taskClient.cancelTask("User Manually Cancelled job");
			throw e;
		}
		Date taskClientEndTime = new Date();
		logFormat("trigger Test end...");
		logFormat("TestGrid time: " + Utils.getDurationAsString(taskClientStartTime, taskClientEndTime));
	}
	
	/**
	 * locate testCasePackage that is saved in the file-system.
	 * 
	 * @param context
	 * @throws Exception
	 *             when cannot create script
	 */
	protected Map<String, ArrayList<FilePath>> locateTestCasePackage(Context context) throws IOException, InterruptedException {
		Map<String, ArrayList<FilePath>> releasePackageAndTestPackageMap = new TreeMap<String, ArrayList<FilePath>>();
		FilePath workspace = context.getWorkspace();
		ArrayList<FilePath> testPackagesList = new ArrayList<FilePath>();
		for(String testPackageName : context.getTestPackageNames()){
			testPackagesList.add(workspace.child(testPackageName));
		}
		FilePath releasePackageZip = workspace.child(context.getReleasePackageName());
		for (FilePath ArtifactsZip : testPackagesList) {
			ArrayList<FilePath> flashFilesAndArtifactsZipPair = new ArrayList<FilePath>();
			flashFilesAndArtifactsZipPair.add(ArtifactsZip);
			flashFilesAndArtifactsZipPair.add(releasePackageZip);
			logFormat("put the " + ArtifactsZip.getName() + " and " + releasePackageZip.getName() + " into releasePackageAndTestPackageMap");
			releasePackageAndTestPackageMap.put(ArtifactsZip.getName(), flashFilesAndArtifactsZipPair);
		}
		context.setSplitCnt(testPackagesList.size());
		if (testPackagesList.size() == 0 || releasePackageAndTestPackageMap.size()==0) {
			logFormat("No TestCase Package found...");
		}
		return releasePackageAndTestPackageMap;
	}

	/**
	* Create different task by different builder
	* @param context Every param from UI can get from it
	* @return task as result
	*/
	protected abstract Task createTask( Context context ) throws IOException, InterruptedException;
	
	/**
	* Prepare all files need to transfer to testnode,eg. execute.py and other test case files
	* @param context Every param from UI can get from it
	* @return dataFiles as result
	*/
	protected abstract Map<String, ArrayList<FilePath>> prepareFiles(Context context) throws IOException, InterruptedException;
	
	/**
	 * handle result.zip after test finished
	 * @param context Every param from UI can get from it
	 * @return result as result
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	protected void handleResult(Context context) throws IOException, InterruptedException, DocumentException {
		logFormat("process Njunit start...");
		processNjunit(context);
		logFormat("process Njunit end...");
		logFormat("Archive Results start...");
		archiveResults(context);
		logFormat("Archive Results end...");
		logFormat("Publish Report start...");
		publishReport(context);
		logFormat("Publish Results end...");
	}
	
	protected abstract void processNjunit(Context context)throws IOException, InterruptedException;
	protected abstract void archiveResults(Context context)throws IOException, InterruptedException;
	protected abstract void publishReport(Context context) throws IOException, DocumentException, InterruptedException;
	
	/**
	* Create TaskClient to invoke testgrid
	* @param context Every param from UI can get from it
	* @return client as result
	 * @throws Exception 
	*/
	protected TaskClient createTaskClient( Context context ) throws Exception {
		// Test Auxiliaries
		FileTransferDescriptor ftDescriptor = null;
		try {
			ftDescriptor = CommonUtils.fromJson(context.getTestAuxiliaries(), FileTransferDescriptor.class );
		}catch(Exception ex){
			throw new Exception("Error: wrong test auxiliaries has been configured. Please check if it's in correct format: \n" + context.getTestAuxiliaries());
		}
		if(ftDescriptor == null){
			throw new Exception("Error: wrong test auxiliaries has been configured and we got null ftDescriptor. Please check if it's in correct format: \n" + context.getTestAuxiliaries());
		}
		// Test cloud address could be a json format object or a simple URI
		String msgUri = context.getTestGridAddress();
		BrokerDescriptor[] bds = null;
		try{
			bds = CommonUtils.fromJson( context.getTestGridAddress(), new TypeToken<BrokerDescriptor[]>(){}.getType() );
		}catch (Exception ex){
			logFormat("Warning:  configured test cloud address is not in correct json format: " + context.getTestGridAddress() + "\nNow Try to use it as a simple URI ...");
			if(!msgUri.contains("://") && !msgUri.contains(":\\\\")){
				throw new Exception("Invalid URI string. Please check if it's in correct format: \n" + context.getTestGridAddress());
			}
		}
		// Work space, test strategy, task id
		FilePath workspaceFilePath = context.getWorkspace();
		String workspaceStr = workspaceFilePath.getRemote();
		String testStrategyStr = context.getTestStrategy();
		TestStrategy testStrategyEnum = TestStrategy.valueOf( StringUtils.upperCase( testStrategyStr ) );
		Task task = createTask( context );
		String taskId = Utils.makeTaskId(context);
		logFormat("task id is: " + taskId);
		task.setId(taskId);
		task.setTaskOwner( context.getOwner() );
		// Device requirement
		DeviceRequirement dr = new DeviceRequirement();
		Map<String, Object> requirement = context.getDeviceRequirementsMap();
		dr.setMain( Device.createRequirement( requirement ) );
		task.setRequirements( dr );
		// Create task client
		TaskClient client = new TaskClient(msgUri, task, testStrategyEnum, new File(workspaceStr), logger.getLogger(), ftDescriptor, false, bds);
		client.setTaskTimeout(Long.parseLong(context.getTaskTimeout())*CommonConstants.ONE_MINUTE);
		client.setPublishTimeout(Long.parseLong(context.getTaskPublishTimeout())*CommonConstants.ONE_MINUTE);
		client.setReserveTimeout(Long.parseLong(context.getTaskReserveTimeout())*CommonConstants.ONE_MINUTE);
		return client;
	}
	
	protected void logFormat( String message) {
		logger.log(this.getClass().getSimpleName(), message);
	}
}
