package com.github.epimethix.lumicore.orm.example.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.model.MutableEntity;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToOne;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;

public class MyMutableEntity implements MutableEntity<Long> {
	@PrimaryKey
	private Long id;
	private Integer count;
	@ManyToOne
	private MyImmutableEntity myImmutableEntity;
	@ManyToOne(lazy = true)
	private MyLazyEntity myLazyEntity;
	private LocalDate date;
	private LocalDateTime time;

	public MyMutableEntity() {}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public MyImmutableEntity getMyImmutableEntity() {
		return myImmutableEntity;
	}

	public void setMyImmutableEntity(MyImmutableEntity myImmutableEntity) {
		this.myImmutableEntity = myImmutableEntity;
	}

	public MyLazyEntity getMyLazyEntity() {
		return myLazyEntity;
	}

	public void setMyLazyEntity(MyLazyEntity myLazyEntity) {
		this.myLazyEntity = myLazyEntity;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	@Override
	public int hashCode() {
		return Objects.hash(count, date, id, myImmutableEntity, time);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyMutableEntity other = (MyMutableEntity) obj;
		return Objects.equals(count, other.count) && Objects.equals(date, other.date) && Objects.equals(id, other.id)
				&& Objects.equals(myImmutableEntity, other.myImmutableEntity) && Objects.equals(time, other.time);
	}
}
