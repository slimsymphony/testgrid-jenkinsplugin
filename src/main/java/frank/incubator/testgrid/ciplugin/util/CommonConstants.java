package frank.incubator.testgrid.ciplugin.util;


public class CommonConstants {
	// DEBUG
	public static final boolean DEBUG_ENABLED = true;
	public static final boolean TIMESTAMP_ENABLED = true;

	// PLUG-IN NAME
	public static final String PLUGIN_NAME = "TestGrid Plugin";

	// DEFAULT VALUES FOR TESTGRID CONFIGURAITON
	public static final String DEFAULT_TESTGRID_ADDRESS = "${TESTGRID_ADDRESS}";
	public static final String DEFAULT_TEST_TOOL = "${TESTGRID_TEST_TOOL}";
	public static final String DEFAULT_FLASH_ARGUMENTS = "${TESTGRID_FLASH_ARGUMENTS}";
	public static final String DEFAULT_LOAD_BALANCER_ARGUMENTS = "${TESTGRID_LOAD_BALANCER_ARGUMENTS}";
	public static final String DEFAULT_TESTFARM_ENTRY = "${TESTGRID_TESTFARM_ENTRY}";
	public static final String DEFAULT_TESTSTRATEGY = "${TESTGRID_TESTSTRATEGY}";
	public static final String DEFAULT_TASK_TIMEOUT = "${TESTGRID_TASK_TIMEOUT}";
	public static final String DEFAULT_TASK_PUBLISH_TIMEOUT = "${TESTGRID_TASK_PUBLISH_TIMEOUT}";
	public static final String DEFAULT_TASK_RESERVE_TIMEOUT = "${TESTGRID_TASK_RESERVE_TIMEOUT}";
	public static final String DEFAULT_TEST_TIMEOUT = "${TESTGRID_TEST_TIMEOUT}";
	public static final String DEFAULT_TEST_AUXILIARIES = "${TESTGRID_TEST_AUXILIARIES}";
	public static final String DEFAULT_RELEASE_PACKAGE_URI = "${RELEASE_PACKAGE_URL}";
	public static final String DEFAULT_TEST_PACKAGE_URIS = "${TEST_PACKAGE_URLS}";
	public static final String DEFAULT_DEVICE_REQUIREMENTS = "${DEVICE_REQUIREMENTS}";
	public static final String DEFAULT_ORIGINAL_TESTSET_URL = "${ORIGINAL_TESTSET_URL}";

	// BUILD PARAMETERS
	public static final String BUILD_URL = "${BUILD_URL}";
	public static final String BUILD_TARGET = "${BUILD_TARGET}";
	public static final String BACKEND_URL = "${BACKEND_URL}";
	public static final String BUILDGROUP_ID = "${BUILD_GROUP_ID}";
	public static final String BUILD_ID = "${BUILD_ID}";
	public static final String TEST_FILES = "${TEST_FILES}";
	public static final String FLASH_FILES = "${FLASH_FILES}";
	public static final String CALLBACK_URL = "${CALLBACK_URL}";

	// TEST DATA
	public static final String STARTUP_SCRIPT = "startup.py";
	public static final String INDEX_FILE = "index";
	public static final String ORIGINAL_TESTSET = "testset.orig";
	public static final String JUNIT_PARSER_SCRIPT = "junit_parser.py";
	public static final String RESULT_PRIX = "result_";
	public static final String RESULT_ARCHIVE_PATTERN = "*result*.zip";
	public static final String ZIP_PATTERN = "*.zip";
	public static final String TESTSET_PATTERN = "*.testset";
	public static final String ZIP_EXTEND = ".zip";
	public static final String NJUNIT_XML = "njunit.xml";
	public static final String TEST_PRIX = "Test_";
	public static final String NA = "NA";
	public static final String TARGET_PRODUCT_KEY = "TARGET_PRODUCT";
	public static final String TARGET_BUILD_VARIANT_KEY = "TARGET_BUILD_VARIANT";
	public static final String TEST_RESULT_URL_KEY = "TEST_RESULT_URL";
	
	public static final long ONE_SECOND = 1000L;
	public static final long ONE_MINUTE = 60000L;
	public static final long ONE_HOUR = 3600000L;

	// TEST RESULTS
	public static final String TEST_RESULTS_DIR = "results";

	// TESTGRID PARAM
	public static final String PYTHON_COMMAND = "python";
	public static final String EXECUTOR_RESULT_COMMAND = " --result ";
	public static final String EXECUTOR_FLASH_COMMAND = " --flash fastboot ";
	public static final String TESTSUITE_NODE = "testsuite";
	public static final String TESTCASE_NODE = "testcase";
	public static final String PROPERTIES_NODE = "properties";
	public static final String TESTSUITE_ATTR_NAME = "name";
	public static final String ARTIFACT_PATH="artifact/";
	public static final String TESTCASE_NODE_ATTR_RELATIVEPATH = "relativepath";
	public static final String XML_FORMAT_INDENT_AMOUNT = "{http://xml.apache.org/xslt}indent-amount";
	
	public static enum RESULT {
		SUCCESS, UNSTABLE,FAILURE,ABORTED
	}
	public static enum TESTCASE_RESULT {
		SUCCESS, FAILURE, NORESULT
	}

}
