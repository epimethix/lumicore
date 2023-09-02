package com.github.epimethix.lumicore.orm.example;

import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.orm.annotation.database.Repositories;

@Repositories("com.github.epimethix.lumicore.orm.example.repository")
public interface MyDB extends Database {
	long nextExampleID();
}
