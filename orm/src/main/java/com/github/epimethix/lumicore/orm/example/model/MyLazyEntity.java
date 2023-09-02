package com.github.epimethix.lumicore.orm.example.model;

import java.time.LocalDate;

import com.github.epimethix.lumicore.common.orm.model.LazyEntity;
import com.github.epimethix.lumicore.orm.annotation.entity.ImplementationClass;

@ImplementationClass(MyLazyEntityImpl.class)
public interface MyLazyEntity extends LazyEntity<Long> {

	Long getCount();

	LocalDate getDate();

	String getComment();
}
