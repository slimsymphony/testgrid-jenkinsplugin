package frank.incubator.testgrid.ciplugin.builder;

import frank.incubator.testgrid.ciplugin.util.BuilderEnum;

public class BuilderFactory {
	public static AbstractBuilder createBuilder( BuilderEnum builderEnum ) {
		AbstractBuilder builder = null;
		if ( !builderEnum.equals( "" ) ) {
			try {
				builder = ( AbstractBuilder ) Class.forName( builderEnum.getValue() ).newInstance();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		return builder;
	}
}
