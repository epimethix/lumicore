package com.github.epimethix.lumicore.swing.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class JListModel<E> extends AbstractListModel<E> {

	private Comparator<E> comparator;

	private List<E> data = new ArrayList<>();

	public void setComparator(Comparator<E> comparator) {
		this.comparator = comparator;
	}

	private void sort() {
		if (Objects.nonNull(comparator)) {
			Collections.sort(data, comparator);
		}
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public E getElementAt(int index) {
		return data.get(index);
	}

	public void setElementAt(int selectedIndex, E e2) {
		data.set(selectedIndex, e2);
		sort();
		fireContentsChanged(this, selectedIndex, selectedIndex + 1);
	}

	public List<E> getData() {
		return new ArrayList<>(data);
	}

	public void setData(List<E> data) {
		this.data = new ArrayList<>(data);
		sort();
		fireContentsChanged(this, 0, getSize());
	}

	public void addDatum(int index, E datum) {
		data.add(index, datum);
		sort();
		fireContentsChanged(this, index, getSize());
	}

	public void removeDatum(E datum) {
		removeDatum(data.indexOf(datum));
	}

	public void removeDatum(int index) {
		if (index > -1 && index < data.size()) {
			data.remove(index);
			fireContentsChanged(this, index, getSize());
		}
	}

	public void addDatum(E datum) {
		data.add(datum);
		sort();
		fireContentsChanged(this, getSize() - 1, getSize());
	}

	public void clear() {
		data.clear();
		fireContentsChanged(this, 0, getSize());
	}

	public void refresh() {
		fireContentsChanged(this, 0, getSize());
	}
}
