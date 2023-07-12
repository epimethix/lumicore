package com.github.epimethix.lumicoreexample.devtools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.epimethix.accounting.gui.L;

public class ExampleDevToolsStarter {

	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		List<String> argsList = new ArrayList<>(Arrays.asList(args));
		argsList.add("--labels-controller=".concat(L.class.getName()));
//		DevTools.launchDevTools(argsList.toArray(new String[] {}));
	}
}
