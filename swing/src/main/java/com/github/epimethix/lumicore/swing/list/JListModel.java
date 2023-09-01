package com.github.epimethix.lumicore.swing.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class JListModel<E> extends AbstractListModel<E> {

	private Comparator<E> comparator;

	private Function<E, Boolean> filter;

	private List<E> data = new ArrayList<>();
	private List<E> dataFiltered = new ArrayList<>();

	public void setComparator(Comparator<E> comparator) {
		this.comparator = comparator;
	}

	private void sort() {
		if (Objects.nonNull(comparator)) {
			Collections.sort(data, comparator);
		}
	}

	private void filter() {
		dataFiltered.clear();
		if (Objects.nonNull(filter)) {
			dataFiltered.addAll(data.stream().filter(e -> filter.apply(e)).collect(Collectors.toList()));
		} else {
			dataFiltered.addAll(data);
		}
	}

	public void filter(Function<E, Boolean> filter) {
		this.filter = filter;
		filter();
		fireContentsChanged(this, 0, getSize());
	}

	@Override
	public int getSize() {
		return dataFiltered.size();
	}

	@Override
	public E getElementAt(int index) {
		return dataFiltered.get(index);
	}

	public void setElementAt(int selectedIndex, E e2) {
		data.set(selectedIndex, e2);
		filter();
		sort();
		fireContentsChanged(this, selectedIndex, selectedIndex + 1);
	}

	public List<E> getData() {
		return new ArrayList<>(data);
	}

	public List<E> getData(int[] indices) {
		ArrayList<E> result = new ArrayList<>();
		for (int i = 0; i < indices.length; i++) {
			result.add(data.get(indices[i]));
		}
		return data;
	}

	public void setData(List<E> data) {
		if (Objects.isNull(data)) {
			this.data = new ArrayList<>();
		} else {
			this.data = new ArrayList<>(data);
		}
		filter();
		sort();
		fireContentsChanged(this, 0, getSize());
	}

	public void addDatum(E datum) {
		data.add(datum);
		filter();
		sort();
		fireContentsChanged(this, getSize() - 1, getSize());
	}

	public void addDatum(int index, E datum) {
		data.add(index, datum);
		filter();
		sort();
		fireContentsChanged(this, index, getSize());
	}

	public void addData(List<E> data) {
		this.data.addAll(data);
		filter();
		sort();
		fireContentsChanged(this, 0, getSize());
	}

	public void removeDatum(E datum) {
		removeDatum(data.indexOf(datum));
	}

	public void removeDatum(int index) {
		if (index > -1 && index < data.size()) {
			data.remove(index);
			filter();
			fireContentsChanged(this, index, getSize());
		}
	}

	public void removeData(Collection<E> data) {
		this.data.removeAll(data);
		filter();
		fireContentsChanged(this, 0, getSize());
	}

	public void clear() {
		data.clear();
		filter();
		fireContentsChanged(this, 0, getSize());
	}

	public void refresh() {
		sort();
		filter();
		fireContentsChanged(this, 0, getSize());
	}
}
