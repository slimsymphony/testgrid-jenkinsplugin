package frank.incubator.testgrid.ciplugin.results.pojo;

import java.util.ArrayList;
import java.util.List;


public class TestSuite {
	private List<TestCase> testcases = new ArrayList<TestCase>();
	private String name;
	public List<TestCase> getTestcases() {
		return testcases;
	}

	public void setTestcases( List<TestCase> testcases ) {
		this.testcases = testcases;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

}
