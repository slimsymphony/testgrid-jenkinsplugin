package frank.incubator.testgrid.ciplugin.util;

import frank.incubator.testgrid.ciplugin.Context;
import frank.incubator.testgrid.ciplugin.logging.Logger;
import frank.incubator.testgrid.ciplugin.results.pojo.TestCase;
import frank.incubator.testgrid.ciplugin.results.pojo.TestResult;
import frank.incubator.testgrid.ciplugin.results.pojo.TestSet;
import frank.incubator.testgrid.ciplugin.results.pojo.TestSuite;
import frank.incubator.testgrid.common.CommonUtils;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Hudson;
import hudson.triggers.TimerTrigger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.w3c.dom.Document;

public class Utils {

	/**
	 * Private constructor to offer only static methods
	 */
	private Utils() {
		// No implementation required
	}

	/**
	 * Converts date object to duration string
	 * 
	 * @param dateBefore
	 *            before the operation started
	 * @param dateAfter
	 *            after the operation ended
	 * @return
	 */
	public static String getDurationAsString(Date dateBefore, Date dateAfter) {

		String duration = "UNKNOWN";
		boolean enableHours = false;
		boolean enableMillis = false;
		boolean enableMinutes = false;

		// Convert time to units
		long milliseconds = dateAfter.getTime() - dateBefore.getTime();

		// Hours
		long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
		if (0 < hours) {
			enableHours = true;
		}

		long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));

		if (!enableHours && 0 < minutes) {

			// Enable minutes
			enableMinutes = true;
		} else {

			// Enable milliseconds
			enableMillis = true;
		}

		long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
		milliseconds -= TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds);

		// Create duration string
		if (enableHours) {

			// Create time with seconds as the most accurate unit
			duration = String.format("%d h %d m %d s", hours, minutes, seconds);
		} else if (enableMinutes) {

			// Create time without hours or minutes
			duration = String.format("%d m %d s", minutes, seconds);
		} else if (enableMillis) {

			// Create time without hours or minutes
			duration = String.format("%d.%03d s", seconds, milliseconds);
		} else {

			// No implementation required
		}

		return duration;
	}

	/**
	 * Finds all files from given FilePath recursively with a pattern. Returns
	 * all files that are found with the search pattern from every sub-directory
	 * found. If no match is found, an empty array is returned.
	 * 
	 * NOTE: This is a recursive function! NOTE: This function ignores .git
	 * folders!
	 * 
	 * @param filepath
	 *            Target path
	 * @param fileSearchPattern
	 *            Pattern that should be found (e.g. *.zip)
	 * @param tracer
	 *            Used for tracing
	 * @return array of matching files
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static List<FilePath> findAllFilesFromPathRecursively(FilePath filepath, String fileSearchPattern, Logger logger) throws IOException,
			InterruptedException {
		List<FilePath> files = new ArrayList<FilePath>();

		// Get files with given pattern
		FilePath[] tempFiles = filepath.list(fileSearchPattern);

		// Save all found files from directory root to the list
		for (FilePath file : tempFiles) {
			files.add(file);
		}

		// Search through directories
		List<FilePath> directories = filepath.listDirectories();
		if (!directories.isEmpty()) {

			// Go through the list directories
			for (FilePath directory : directories) {

				// Continue search only if directory does not contain .git
				if (!directory.getRemote().contains(".git")) {
					files.addAll(findAllFilesFromPathRecursively(directory, fileSearchPattern, logger));
				}
			}
		}

		return files;
	}

	// To wipe and trim the parameterized string on build step
	private static String wipeAndTrimParameterizedStringRef(String origin, String patternToWipe) {
		origin = StringUtils.trimToEmpty(origin);
		if (StringUtils.equalsIgnoreCase(origin, patternToWipe)) {
			return StringUtils.EMPTY;
		}
		return origin;
	}

	/**
	 * Expands and verifies parameters
	 * 
	 * @param build
	 * @param listener
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws Exception
	 */
	public static String expandParameters(AbstractBuild<?, ?> build, BuildListener listener, String param) throws IOException, InterruptedException {
		EnvVars env = build.getEnvironment(listener);
		String retParam = wipeAndTrimParameterizedStringRef(env.expand(param), param);
		return retParam;
	}

	private static void logFormat(Logger logger,String message) {
		logger.log(Utils.class.getSimpleName(), message);
	}
	
	public static FilePath createStartupStriptInWorkspace(Context context) throws IOException, InterruptedException{
		String fileName = CommonConstants.STARTUP_SCRIPT;
		FilePath filePath = context.getWorkspace().child(fileName);
		// Make content string
		StringBuilder sb = new StringBuilder();
		sb.append("import os,sys\n");
		sb.append("import glob ,importlib\n");
		sb.append("if __name__=='__main__':\n");
		sb.append("    #unzip theSCVPackage.7z\n");
		sb.append("    zips = glob.glob(\"*.zip\")\n");
		sb.append("    for zip in zips:\n");
		sb.append("        print '7z x %s -y'%zip\n");
		sb.append("        os.system('7z x %s -y'%zip)\n");
		sb.append("    if os.path.exists('execute.py'):\n");
		sb.append("        sys.stdout = sys.__stdout__\n");
		sb.append("        print 'load execute module'\n");
		sb.append("        execute = importlib.import_module('execute')\n");
		sb.append("        execute.main()\n");
		sb.append("        print 'execute module finised!'\n");
		sb.append("    else:\n");
		sb.append("        print 'execute.py not found'");
		// Write to target file in work space
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(sb.toString().getBytes());
			filePath.copyFrom(is);
		} finally {
			if(is != null){
				is.close();
			}
		}
		return filePath;
	}

	public static String getTaskOwner(Context context) {
		Logger logger = context.getLogger();
		List<Cause> causes = context.getBuild().getCauses();
		logFormat(logger,"Build causes count: " + String.valueOf(causes.size()));
		String taskOwner="";
		
		if (causes.size()==0) {
			logFormat(logger,"No build cause found...");
			return CommonConstants.NA;
		}
		
		// If triggered by more than one upCause then use the latest one.
		if (causes.size() > 1) {
			logFormat(logger,"More than one cause, using latest...");
		}
		Cause cause = causes.get(causes.size() - 1);
		
		if (cause instanceof Cause.UserIdCause) {
			Cause.UserIdCause userCause = (Cause.UserIdCause) cause;
			logFormat(logger,"Build caused by user: " + userCause.getUserName());
			taskOwner=userCause.getUserName();
		} 
		else if (cause instanceof TimerTrigger.TimerTriggerCause) {
			TimerTrigger.TimerTriggerCause timerCause=(TimerTrigger.TimerTriggerCause)cause;
			logFormat(logger,"Build caused by timer trigger: "+timerCause.getShortDescription());
			taskOwner=timerCause.getShortDescription();
		}
		else if (cause instanceof Cause.UpstreamCause) {
			Cause.UpstreamCause upCause = (Cause.UpstreamCause) cause;
			logFormat(logger,"Build caused by upstream project: " + upCause.getUpstreamProject());
			taskOwner=upCause.getUpstreamProject();
		}
		else if (cause instanceof Cause.RemoteCause) {
			Cause.RemoteCause remoteCause=(Cause.RemoteCause)cause;
			logFormat(logger,"Build caused by RemoteCause: "+remoteCause.getShortDescription());
			taskOwner=remoteCause.getShortDescription();
		}
		else {
			logFormat(logger,"Build caused by other cause: "+cause.getShortDescription());
			taskOwner=cause.getShortDescription();
		}
		return taskOwner;

	}
	
	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setIgnoringComments(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		return builder;
	}
	
	public static void transform(Document doc_main,FilePath mergedFile) throws TransformerException, IOException, InterruptedException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(CommonConstants.XML_FORMAT_INDENT_AMOUNT, "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc_main);
		StreamResult result = new StreamResult(mergedFile.write());
		transformer.transform(source, result);
	}
	
	
	@SuppressWarnings( "unchecked" )
	public static TestResult parseNjuint( String xmlContent ) throws DocumentException {
		TestResult result = new TestResult();
		org.dom4j.Document doc = DocumentHelper.parseText(xmlContent);
		List<Element> list = doc.getRootElement().selectNodes( CommonConstants.TESTSUITE_NODE );
		TestSuite ts = null;
		TestCase tc = null;
		long totaltimeCost=0;
		String testsetName = doc.getRootElement().attributeValue("name");
		if (testsetName!=null) {
			result.setTestsetName(testsetName);
		}
		String robtiumRunTime = doc.getRootElement().attributeValue("time");
		if (robtiumRunTime!=null) {
			float second = Float.parseFloat(robtiumRunTime)*1000;
			long secondToMillis=Math.round(second);
			totaltimeCost=secondToMillis;
		}
		String name = null;
		for ( Element el : list ) {
			ts = new TestSuite();
			result.getTestsuites().add( ts );
			name = el.attributeValue( "name" );
			ts.setName( name );
			List<Element> cases = el.selectNodes( CommonConstants.TESTCASE_NODE );
			for ( Element cas : cases ) {
				tc = new TestCase();
				ts.getTestcases().add( tc );
				tc.setName( cas.attributeValue( "name" ) );
				tc.setClassname( cas.attributeValue( "classname" ) );
				tc.setRelativepath( cas.attributeValue( "relativepath" ) );
				String time = cas.attributeValue( "time" );
				tc.setTime(time);
				if (StringUtils.contains(time, "s")) {
					long timeCost = getTimeCost( time);
					totaltimeCost+=timeCost;
				}
				Element fn = ( Element ) cas.selectSingleNode( "failure" );
				Element na = ( Element ) cas.selectSingleNode( "na" );
				if ( fn != null ) {
					tc.setTestcase_Result(CommonConstants.TESTCASE_RESULT.FAILURE);
					tc.setFailInfo( fn.attributeValue( "message" ) );
					tc.setDetail( fn.attributeValue( "detail" ) );
				} else if( na != null ) {
					tc.setTestcase_Result( CommonConstants.TESTCASE_RESULT.NORESULT );
					tc.setFailInfo( na.attributeValue( "message" ) );
					tc.setDetail( na.attributeValue( "detail" ) );
				} else {
					tc.setTestcase_Result( CommonConstants.TESTCASE_RESULT.SUCCESS );
				}
				String script = cas.attributeValue("script");
				if (script!=null) {
					String[] splitScriptStr = StringUtils.split(script, "\\");
					script=splitScriptStr[splitScriptStr.length-1];
					tc.setTestcaseIdentify(script);
					result.getTestcaseIdentifies().add(script);
				}
				result.getRunTestcases().add(tc);
			}
			
		}
		String duration = getDurationAsString(totaltimeCost);
		result.setTimeCost(duration);
		return result;
	}
	
	public static long getTimeCost(String time){
		long timeCost=0;
		if (StringUtils.contains(time, "h")) {
			String hourStr= StringUtils.split(time, "h")[0].trim();
			long hourToMillis= TimeUnit.HOURS.toMillis(Long.parseLong(hourStr));
			long minuteToMillis=0;
			long secondToMillis=0;
			if (StringUtils.contains(time, "m")) {
				String minuteStr= StringUtils.substringBetween(time, "h", "m").trim();
				minuteToMillis = TimeUnit.MINUTES.toMillis(Long.parseLong(minuteStr));
			}
			if (StringUtils.contains(time, "s")) {
			String secondStr= StringUtils.substringBetween(time, "m", "s").trim();
			float second = Float.parseFloat(secondStr)*1000;
			secondToMillis=Math.round(second);
			}
			timeCost=hourToMillis+minuteToMillis+secondToMillis;
		} else if (StringUtils.contains(time, "m")) {
			String minuteStr= StringUtils.split(time, "m")[0].trim();
			long minuteToMillis = TimeUnit.MINUTES.toMillis(Long.parseLong(minuteStr));
			long secondToMillis=0;
			if (StringUtils.contains(time, "s")) {
				String secondStr= StringUtils.substringBetween(time, "m", "s").trim();
				float second = Float.parseFloat(secondStr)*1000;
				secondToMillis=Math.round(second);
			}
			timeCost=minuteToMillis+secondToMillis;
		}
		else if (StringUtils.contains(time, "s")) {
			String secondStr= StringUtils.split(time, "s")[0].trim();
			float second = Float.parseFloat(secondStr)*1000;
			long secondToMillis=Math.round(second);
			timeCost=secondToMillis;
		}
		return timeCost;
	}
	
	@SuppressWarnings( "unchecked" )
	public static TestSet parseTestSet( String xmlContent ) throws DocumentException{
		TestSet testset = new TestSet();
		 org.dom4j.Document doc = DocumentHelper.parseText( xmlContent );
		List<Element> cases = doc.getRootElement().selectNodes(CommonConstants.TESTCASE_NODE );
		TestCase tc = null;
			for ( Element cas : cases ) {
				tc = new TestCase();
				Element testscript = cas.element("testscript");
				String directory = testscript.attributeValue("directory");
				String[] directory_array = StringUtils.split(directory, "\\");
				directory=directory_array[directory_array.length-1];
				String file = testscript.attributeValue("file");
				String clazz = testscript.attributeValue("class");
				String method = testscript.attributeValue("method");
				String testcaseIdentify = directory+"."+file+"."+clazz+"."+method;
				tc.setTestcaseIdentify(testcaseIdentify);
				tc.setName( cas.attributeValue( "name" ) );
				testset.getTestcases().add(tc);
			}
			return testset;
		}
		
	
	public static String getDurationAsString(long milliseconds) {

		String duration = "UNKNOWN";
		boolean enableHours = false;
		boolean enableMillis = false;
		boolean enableMinutes = false;


		// Hours
		long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
		if (0 < hours) {
			enableHours = true;
		}

		long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));

		if (!enableHours && 0 < minutes) {

			// Enable minutes
			enableMinutes = true;
		} else {

			// Enable milliseconds
			enableMillis = true;
		}

		long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
		milliseconds -= TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds);

		// Create duration string
		if (enableHours) {

			// Create time with seconds as the most accurate unit
			duration = String.format("%d h %d m %d s", hours, minutes, seconds);
		} else if (enableMinutes) {

			// Create time without hours or minutes
			duration = String.format("%d m %d s", minutes, seconds);
		} else if (enableMillis) {

			// Create time without hours or minuteso
			duration = String.format("%d.%03d s", seconds, milliseconds);
		} else {

			// No implementation required
		}

		return duration;
	}
	
	public static String makeTaskId(Context context){
		String taskId = Hudson.getInstance().getRootUrl() + context.getBuild().getUrl();
		taskId = taskId.replace("http://", "");
		taskId = taskId.replace("http:\\\\", "");
		taskId = taskId.replace(":", "_");
		taskId = taskId.replace("/", "_");
		taskId = taskId.replace("\\", "_");
		return taskId;
	}
	
	public static String makeTestId(Context context, String testPackageName){
		String testPackageBaseName = testPackageName;
		int index = testPackageName.lastIndexOf('.');
		if(index >= 1){
			testPackageBaseName = testPackageBaseName.substring(0, index);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(testPackageBaseName);
		sb.append("_");
		sb.append(CommonUtils.generateToken(5));
		return sb.toString();
	}
}
