package frank.incubator.testgrid.ciplugin.builder;

import hudson.FilePath;
import hudson.model.Hudson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.dom4j.DocumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import frank.incubator.testgrid.ciplugin.Context;
import frank.incubator.testgrid.ciplugin.results.PublishAction;
import frank.incubator.testgrid.ciplugin.results.pojo.TestCase;
import frank.incubator.testgrid.ciplugin.results.pojo.TestResult;
import frank.incubator.testgrid.ciplugin.results.pojo.TestSet;
import frank.incubator.testgrid.ciplugin.util.CommonConstants;
import frank.incubator.testgrid.ciplugin.util.Utils;
import frank.incubator.testgrid.common.model.Task;
import frank.incubator.testgrid.common.model.TestSuite;

public class CustomizationBuilder extends AbstractBuilder {

	@Override
	protected Task createTask( Context context ) throws IOException, InterruptedException {
		logFormat("create Task start...");
		Task task = new Task();
		logFormat("prepare Files start...");
		Map<String, ArrayList<FilePath>> releaseFilesAndArtifactsZipMap=prepareFiles(context);
		logFormat("prepare Files end...");
		logFormat("create TestSuite start...");
		TestSuite testsuite = createTestSuite(context,releaseFilesAndArtifactsZipMap);
		logFormat("create TestSuite end...");
		task.setTestsuite(testsuite);
		logFormat("create Task end...");
		return task;
	}	
	

	protected Map<String, ArrayList<FilePath>> prepareFiles(Context context) throws IOException, InterruptedException{
		FilePath startupScript = Utils.createStartupStriptInWorkspace(context);
		Map<String, ArrayList<FilePath>> releaseFilesAndArtifactsZipMap = locateTestCasePackage(context);
		Iterator<Entry<String, ArrayList<FilePath>>> iter = releaseFilesAndArtifactsZipMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, ArrayList<FilePath>> entry = (Map.Entry<String, ArrayList<FilePath>>) iter.next();
			ArrayList<FilePath> dataFiles = (ArrayList<FilePath>)entry.getValue();
			dataFiles.add(startupScript);
			}
		return releaseFilesAndArtifactsZipMap;
	}
	
	/**
	 * handle result.zip after test finished
	 * @param context Every param from UI can get from it
	 * @return result as result
	 */
	protected void processNjunit(Context context) throws IOException, InterruptedException {
		FilePath workspace = context.getWorkspace();
		FilePath resultsFolder = workspace.child(CommonConstants.TEST_RESULTS_DIR);
		if (resultsFolder.exists()) {
			List<FilePath> results = Utils
					.findAllFilesFromPathRecursively(resultsFolder, CommonConstants.RESULT_ARCHIVE_PATTERN, context.getLogger());
			context.setResultZipCnt(results.size());
			List<FilePath> splitedNjunitXMLs = new ArrayList<FilePath>();
			if (results.size() == 0) {
				logFormat("No result.zip found...");
			} else {
				logFormat("Result.zip found then copying to results Folder and extracting...");
				for (FilePath result : results) {
					logFormat("The " + result.getName() + " path is " + result.getRemote());
					try {
						// Extract result.zip
						result.unzip(result.getParent());
						// add to splitedNjunitXMLs
						FilePath resultsFolderUnderTestInstanceFolder = result.getParent().child(CommonConstants.TEST_RESULTS_DIR);
						FilePath splitedNjunitXml = resultsFolderUnderTestInstanceFolder.child(CommonConstants.NJUNIT_XML);
						if (splitedNjunitXml.exists()) {
							splitedNjunitXMLs.add(splitedNjunitXml);
						} else {
							logFormat("No njunit.xml found under " + resultsFolderUnderTestInstanceFolder.getRemote()
									+ " and indicates that at least one of sub tests is failure");
						}
					} catch (Exception e) {
						logFormat("unzip result.zip and find testset, njunit Exception: " + e.getMessage());
					}

				}
			}
			if (splitedNjunitXMLs.size() > 0) {
				mergeNjunit(resultsFolder, splitedNjunitXMLs, CommonConstants.NJUNIT_XML);
			}
			// To find njunit.xml in resultsFolder
			FilePath njunitXml = resultsFolder.child(CommonConstants.NJUNIT_XML);
			if (!njunitXml.exists()) {
				logFormat("No njunit.xml found...");
			}

		} else {
			logFormat("No results folder found under workspace...");
		}
	}
	

	protected void mergeNjunit(FilePath resultsFolder, List<FilePath> splitedNjunitXMLs,String mergedFileName) throws IOException, InterruptedException{
		FilePath mergedNjunitXml = resultsFolder.child(mergedFileName);
		Document doc_main = null;
		try {
			DocumentBuilder builder=Utils.getDocumentBuilder();
			for (int i = 0; i < splitedNjunitXMLs.size(); i++) {
				if (i == 0) {
					doc_main = builder.parse(splitedNjunitXMLs.get(i).read());
					addAttributeToTestCase(doc_main,splitedNjunitXMLs.get(i));
					continue;
				}
				Document doc_vice = builder.parse(splitedNjunitXMLs.get(i).read());
				addAttributeToTestCase(doc_vice,splitedNjunitXMLs.get(i));
				NodeList nodes_vice = doc_vice.getElementsByTagName(CommonConstants.TESTSUITE_NODE);
				NodeList nodes_main = doc_main.getElementsByTagName(CommonConstants.TESTSUITE_NODE);
				//merge cases under same testsuite
				for (int j = 0; j < nodes_vice.getLength(); j++) {
					Element ele_vice = (Element) nodes_vice.item(j);
					String testsuite_name_vice = ele_vice.getAttribute(CommonConstants.TESTSUITE_ATTR_NAME);
					boolean isImportTestSuiteNodeNeeded = true;
					for (int k = 0; k < nodes_main.getLength(); k++) {
						Element ele_main = (Element) nodes_main.item(k);
						String testsuite_name_main = ele_main.getAttribute(CommonConstants.TESTSUITE_ATTR_NAME);
						if (testsuite_name_main.equals(testsuite_name_vice)) {
							isImportTestSuiteNodeNeeded = false;
							logFormat("Same testsuite node is " + testsuite_name_main);
							for (int m = 0; m < ele_vice.getChildNodes().getLength(); m++) {
								if (!ele_vice.getChildNodes().item(m).getNodeName().equals(CommonConstants.PROPERTIES_NODE)) {
									Node importNode = doc_main.importNode(ele_vice.getChildNodes().item(m), true);
									ele_main.appendChild(importNode);
								}
							}
						}
					}
					if (isImportTestSuiteNodeNeeded) {
						Node node = (Node) doc_main.importNode(nodes_vice.item(j), true);
						nodes_main.item(0).getParentNode().appendChild(node);
					}
				}
			}
			// Write the content into xml file
			Utils.transform(doc_main, mergedNjunitXml);
		} catch (TransformerException e) {
			logFormat("TransformerException " + e.getMessage());
		} catch (ParserConfigurationException e) {
			logFormat("ParserConfigurationException " + e.getMessage());
		} catch (SAXException e) {
			logFormat("SAXException " + e.getMessage());
		}
	}

	protected void addAttributeToTestCase(Document doc,FilePath rawNjunit) {
		NodeList testcaseList = doc.getElementsByTagName(CommonConstants.TESTCASE_NODE);
		for (int j = 0; j < testcaseList.getLength(); j++) {
			Element ele = (Element)testcaseList.item(j);
			ele.setAttribute(CommonConstants.TESTCASE_NODE_ATTR_RELATIVEPATH, rawNjunit.getParent().getParent().getName() + "/"+CommonConstants.TEST_RESULTS_DIR);
		}
	}

	protected void archiveResults(Context context) throws IOException, InterruptedException{
		FilePath workspace = context.getWorkspace();
		FilePath resultsFolder = workspace.child(CommonConstants.TEST_RESULTS_DIR);
		logFormat("Copy results folder to artifacts start...");
		File artifactsDir = context.getBuild().getArtifactsDir();
		logFormat("artifactsDir URL is "+artifactsDir.getAbsolutePath());
		if (resultsFolder.exists()) {
			int fileCopyCnt = resultsFolder.copyRecursiveTo( new FilePath( context.getBuild().getWorkspace().getChannel() ,artifactsDir.getAbsolutePath() ) );
			logFormat("The number of files copied is " + fileCopyCnt);
		} else {
			logFormat("No results folder found for artifacts copying ");
			return;
		}
		logFormat("Copy results folder to artifacts end...");
	}

	protected void publishReport(Context context) throws IOException, DocumentException, InterruptedException{
		PublishAction action = context.getPublishAction();
		FilePath workspace = context.getWorkspace();
		FilePath resultsFolder = workspace.child(CommonConstants.TEST_RESULTS_DIR);
		FilePath njunitXml = resultsFolder.child(CommonConstants.NJUNIT_XML);
		String content = njunitXml.readToString();
		TestResult tr = Utils.parseNjuint(content);
		
		FilePath originalTestsetFilePath = workspace.child(context.getOriginalTestsetName());
		if (originalTestsetFilePath != null && originalTestsetFilePath.exists()) {
			List<TestCase> missRunTestcases = new ArrayList<TestCase>();
			String testSet_content = originalTestsetFilePath.readToString();
			byte[] b = testSet_content.getBytes("UTF-8"); 
			testSet_content = new String(b,3,b.length-3,"UTF-8");
			TestSet testSet = Utils.parseTestSet(testSet_content);
			List<TestCase> totalTestcases = testSet.getTestcases();
			for (TestCase testcase : totalTestcases) {
				String testcaseIdentify = testcase.getTestcaseIdentify();
				if (!tr.getTestcaseIdentifies().contains(testcaseIdentify)) {
					missRunTestcases.add(testcase);
					testSet.setMissRunTestcases(missRunTestcases);
				}
			}
			for (TestCase missRunTestcase : missRunTestcases) {
				String t = "<strong>Case Name: </strong>" + missRunTestcase.getName();
				action.getMissedCases().add(t);
			}
			action.setMissedCnt(missRunTestcases.size());
			action.setTestcaseCnt(totalTestcases.size());
		} else {
			action.setTestcaseCnt(tr.getRunTestcases().size());
		}

		for (frank.incubator.testgrid.ciplugin.results.pojo.TestSuite testsuite : tr.getTestsuites()) {
			for (TestCase ca : testsuite.getTestcases()) {
				String htmlReportUrl = makeHtmlReportUrlOfTestCase(context, resultsFolder, ca);
				StringBuilder sb = new StringBuilder();
				sb.append("<strong>Case Name: </strong>");
				// Show link to HTML report if it exists, otherwise show only test case name
				if(htmlReportUrl == null || htmlReportUrl.isEmpty()){
					sb.append(ca.getName());
				}else{
					sb.append("<a href=\"");
					sb.append(htmlReportUrl);
					sb.append("\">");
					sb.append(ca.getName());
					sb.append("</a>");
				}
				// Add more details for failed cases
				if (!ca.getTestcase_Result().equals(CommonConstants.TESTCASE_RESULT.SUCCESS)) {
					sb.append(" <strong>Fail Info: </strong>");
					sb.append(ca.getFailInfo());
					sb.append("<strong> Detail: </strong>");
					sb.append(ca.getDetail());
				}
				sb.append("<strong> Time Cost: </strong>");
				sb.append(ca.getTime());
				String t = sb.toString();
				logFormat("Test case result string: " + t);
				// Add to result list
				if (ca.getTestcase_Result().equals(CommonConstants.TESTCASE_RESULT.SUCCESS))
					action.getSuccessCases().add(t);
				if (ca.getTestcase_Result().equals(CommonConstants.TESTCASE_RESULT.FAILURE))
					action.getFailureCases().add(t);
				if (ca.getTestcase_Result().equals(CommonConstants.TESTCASE_RESULT.NORESULT))
					action.getNaCases().add(t);
			}
		}
		action.setSuccessCnt(action.getSuccessCases().size());
		action.setFailureCnt(action.getFailureCases().size());
		action.setNaCnt(action.getNaCases().size());
		action.setTotalTimeCost(tr.getTimeCost());
		action.setTestsetName(tr.getTestsetName());
	}

	protected String makeHtmlReportUrlOfTestCase(Context context, FilePath resultsFolder, TestCase ca) throws IOException, InterruptedException{
		String relativePath = ca.getRelativepath() + "/html/" + ca.getName() + ".html";
		FilePath htmlReportFile = resultsFolder.child(relativePath);
		if(htmlReportFile.exists()){
			logFormat("Info: HTML report file " + htmlReportFile.getRemote() + " exists, link to that html report file will be created.");
			StringBuilder sb = new StringBuilder();
			sb.append(Hudson.getInstance().getRootUrl());
			sb.append(context.getBuild().getUrl());
			sb.append("artifact/");
			sb.append(relativePath);
			return sb.toString();
		}else{
			logFormat("Warning: HTML report file " + htmlReportFile.getRemote() + " does not exist!");
			return null;
		}
	}
}
	
