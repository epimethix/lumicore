package com.github.epimethix.lumicore.devtools;

import com.github.epimethix.lumicore.properties.ApplicationProperties;
import com.github.epimethix.lumicore.properties.Theme;

public class AppProperties extends ApplicationProperties {

	public AppProperties(String propertiesName) {
		super(propertiesName);
		setTheme(Theme.DARK);
	}
}
