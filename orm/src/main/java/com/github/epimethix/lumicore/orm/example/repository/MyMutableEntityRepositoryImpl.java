package com.github.epimethix.lumicore.orm.example.repository;

import java.sql.SQLException;
import java.util.Optional;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.orm.SQLRepository;
import com.github.epimethix.lumicore.orm.example.model.MyMutableEntity;

public class MyMutableEntityRepositoryImpl extends SQLRepository<MyMutableEntity, Long>
		implements MyMutableEntityRepository {
	public MyMutableEntityRepositoryImpl(Database db) throws ConfigurationException {
		super(db, MyMutableEntity.class, Long.class);
	}

	@Override
	public Optional<MyMutableEntity> selectLastEntry() throws SQLException {
		return selectFirst(b->b.orderByDesc(getSchemaName(), getEntityClass(), "id"));
	}
}
