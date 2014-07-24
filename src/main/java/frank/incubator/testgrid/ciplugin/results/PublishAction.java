package frank.incubator.testgrid.ciplugin.results;

import java.util.ArrayList;
import java.util.List;

import hudson.model.InvisibleAction;

public class PublishAction extends InvisibleAction {

	private List<String> failureCases = new ArrayList<String>();
	private List<String> naCases = new ArrayList<String>();
	private List<String> successCases = new ArrayList<String>();
	private List<String> missedCases = new ArrayList<String>();
	private int testcaseCnt;
	private int successCnt;
	private int failureCnt;
	private int naCnt;
	private int missedCnt;
	private String totalTimeCost;
	private String testsetName;
	
	public String getTestsetName() {
		return testsetName;
	}
	public void setTestsetName(String testsetName) {
		this.testsetName = testsetName;
	}
	public int getMissedCnt() {
		return missedCnt;
	}
	public void setMissedCnt(int missedCnt) {
		this.missedCnt = missedCnt;
	}
	
	public List<String> getFailureCases() {
		return failureCases;
	}
	public void setFailureCases(List<String> failureCases) {
		this.failureCases = failureCases;
	}
	public List<String> getSuccessCases() {
		return successCases;
	}
	public void setSuccessCases(List<String> successCases) {
		this.successCases = successCases;
	}
	public List<String> getMissedCases() {
		return missedCases;
	}
	public void setMissedCases(List<String> missedCases) {
		this.missedCases = missedCases;
	}
	
	public String getTotalTimeCost() {
		return totalTimeCost;
	}
	public void setTotalTimeCost(String totalTimeCost) {
		this.totalTimeCost = totalTimeCost;
	}
	public int getTestcaseCnt() {
		return testcaseCnt;
	}
	public void setTestcaseCnt(int testcaseCnt) {
		this.testcaseCnt = testcaseCnt;
	}
	public int getSuccessCnt() {
		return successCnt;
	}
	public void setSuccessCnt(int successCnt) {
		this.successCnt = successCnt;
	}
	public int getFailureCnt() {
		return failureCnt;
	}
	public void setFailureCnt(int failureCnt) {
		this.failureCnt = failureCnt;
	}
	public List<String> getNaCases() {
		return naCases;
	}
	public void setNaCases( List<String> naCases ) {
		this.naCases = naCases;
	}
	public int getNaCnt() {
		return naCnt;
	}
	public void setNaCnt( int naCnt ) {
		this.naCnt = naCnt;
	}
	
}
