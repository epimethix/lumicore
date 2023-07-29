package com.github.epimethix.lumicore.common.ui;

import java.util.Locale;

import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.properties.Theme;

public abstract class AbstractApplication implements Application {

	@Override
	public Locale getDefaultLocale() {
		return getApplicationProperties().getDefaultLocale();
	}

	@Override
	public void setDefaultLocale(Locale locale) {
		getApplicationProperties().setDefaultLocale(locale);
	}

	@Override
	public Theme getTheme() {
		return getApplicationProperties().getTheme();
	}

	@Override
	public void setTheme(Theme t) {
		getApplicationProperties().setTheme(t);
	}
}
