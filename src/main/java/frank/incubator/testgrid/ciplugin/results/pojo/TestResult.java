package frank.incubator.testgrid.ciplugin.results.pojo;

import java.util.ArrayList;
import java.util.List;


public class TestResult {
	private List<TestSuite> testsuites = new ArrayList<TestSuite>();
	private List<String> testcaseIdentifies = new ArrayList<String>();
	private List<TestCase> RunTestcases = new ArrayList<TestCase>();
	private String timeCost;
	private String testsetName;
	
	public List<TestCase> getRunTestcases() {
		return RunTestcases;
	}

	public void setRunTestcases(List<TestCase> runTestcases) {
		RunTestcases = runTestcases;
	}

	public List<String> getTestcaseIdentifies() {
		return testcaseIdentifies;
	}

	public void setTestcaseIdentifies(List<String> testcaseIdentifies) {
		this.testcaseIdentifies = testcaseIdentifies;
	}
	
	public String getTestsetName() {
		return testsetName;
	}

	public void setTestsetName(String testsetName) {
		this.testsetName = testsetName;
	}

	public String getTimeCost() {
		return timeCost;
	}

	public void setTimeCost(String timeCost) {
		this.timeCost = timeCost;
	}

	public List<TestSuite> getTestsuites() {
		return testsuites;
	}

	public void setTestsuites( List<TestSuite> testsuites ) {
		this.testsuites = testsuites;
	}
}
