package com.github.epimethix.lumicore.orm.example.repository;

import java.sql.SQLException;
import java.util.Optional;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.orm.example.model.MyMutableEntity;

public interface MyMutableEntityRepository extends Repository<MyMutableEntity, Long> {
	Optional<MyMutableEntity> selectLastEntry() throws SQLException;
}
