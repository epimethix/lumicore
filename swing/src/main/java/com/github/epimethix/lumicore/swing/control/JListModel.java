package com.github.epimethix.lumicore.swing.control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class JListModel<E> extends AbstractListModel<E> {

	private List<E> data = new ArrayList<>();

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public E getElementAt(int index) {
		return data.get(index);
	}

	public List<E> getData() {
		return data;
	}

	public void setData(List<E> data) {
		this.data = data;
		fireContentsChanged(this, 0, getSize());
	}

	public void addDatum(int index, E datum) {
		data.add(index, datum);
		fireContentsChanged(this, index, getSize());
	}

	public void removeDatum(int index) {
		data.remove(index);
		fireContentsChanged(this, index, getSize());
	}
	
	public void addDatum(E datum) {
		data.add(datum);
		fireContentsChanged(this, getSize()-1, getSize());
	}

	public void clear() {
		data.clear();
		fireContentsChanged(this, 0, getSize());
	}
}
