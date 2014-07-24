package frank.incubator.testgrid.ciplugin.util;

public enum BuilderEnum {
	CUSTOMIZATION("frank.incubator.testgrid.ciplugin.builder.CustomizationBuilder"),
	MONKEY("frank.incubator.testgrid.ciplugin.builder.impl.MonkeyBuilder"), CTS("frank.incubator.testgrid.ciplugin.builder.impl.CTSBuilder"), JUNIT(
			"frank.incubator.testgrid.ciplugin.builder.impl.JunitBuilder"), ROBOTIUM(
			"frank.incubator.testgrid.ciplugin.builder.impl.RobotiumBuilder"), NST("frank.incubator.testgrid.ciplugin.builder.impl.NSTBuilder"), MARBLE(
			"frank.incubator.testgrid.ciplugin.builder.impl.MarbleBuilder"), BDD("frank.incubator.testgrid.ciplugin.builder.impl.BDDBuilder"), UIAUTOMATOR(
			"frank.incubator.testgrid.ciplugin.builder.impl.UIAutomatorBuilder");

	private String value = "";

	private BuilderEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
