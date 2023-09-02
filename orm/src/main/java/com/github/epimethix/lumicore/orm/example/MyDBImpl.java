package com.github.epimethix.lumicore.orm.example;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.DatabaseApplication;
import com.github.epimethix.lumicore.orm.SQLDatabase;

public class MyDBImpl extends SQLDatabase implements MyDB {
	
	private final static String SEQ_EXAMPLE_ID = "SEQ_EXAMPLE_ID";

	public MyDBImpl(DatabaseApplication databaseApplication) throws ConfigurationException {
		super(databaseApplication);
	}

	@Override
	public long nextExampleID() {
		return next(SEQ_EXAMPLE_ID);
	}

}
