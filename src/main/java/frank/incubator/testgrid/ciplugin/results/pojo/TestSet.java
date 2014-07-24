package frank.incubator.testgrid.ciplugin.results.pojo;

import java.util.ArrayList;
import java.util.List;


public class TestSet {
	private List<String> testcaseIdentifies = new ArrayList<String>();
	private List<TestCase> missRunTestcases = new ArrayList<TestCase>();
	private List<TestCase> testcases = new ArrayList<TestCase>();


	public List<TestCase> getTestcases() {
		return testcases;
	}

	public void setTestcases(List<TestCase> testcases) {
		this.testcases = testcases;
	}

	public List<String> getTestcaseIdentifies() {
		return testcaseIdentifies;
	}

	public List<TestCase> getMissRunTestcases() {
		return missRunTestcases;
	}

	public void setMissRunTestcases(List<TestCase> missRunTestcases) {
		this.missRunTestcases = missRunTestcases;
	}

	public void setTestcaseIdentifies(List<String> testcaseIdentifies) {
		this.testcaseIdentifies = testcaseIdentifies;
	}

	private String timeCost;
	
	public String getTimeCost() {
		return timeCost;
	}

	public void setTimeCost(String timeCost) {
		this.timeCost = timeCost;
	}

}
