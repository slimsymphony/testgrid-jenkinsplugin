package frank.incubator.testgrid.ciplugin.results.pojo;

import frank.incubator.testgrid.ciplugin.util.CommonConstants.TESTCASE_RESULT;


public class TestCase {
	private String name;
	private String classname;
	private String failInfo;
	private String detail;
	private String time;
	private String resultUrl;
	private String testcaseIdentify;
	private TESTCASE_RESULT testcase_Result;
	private String relativepath;

	public TESTCASE_RESULT getTestcase_Result() {
		return testcase_Result;
	}

	public void setTestcase_Result(TESTCASE_RESULT testcase_Result) {
		this.testcase_Result = testcase_Result;
	}

	public String getTestcaseIdentify() {
		return testcaseIdentify;
	}

	public void setTestcaseIdentify(String testcaseIdentify) {
		this.testcaseIdentify = testcaseIdentify;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname( String classname ) {
		this.classname = classname;
	}

	public String getFailInfo() {
		return failInfo;
	}

	public void setFailInfo( String failInfo ) {
		this.failInfo = failInfo;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail( String detail ) {
		this.detail = detail;
	}

	public String getTime() {
		return time;
	}

	public void setTime( String time ) {
		this.time = time;
	}

	public String getResultUrl() {
		return resultUrl;
	}

	public void setResultUrl( String resultUrl ) {
		this.resultUrl = resultUrl;
	}

	public String getRelativepath() {
		return relativepath;
	}

	public void setRelativepath( String relativepath ) {
		this.relativepath = relativepath;
	}

}
