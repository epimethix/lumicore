package com.github.epimethix.lumicore.swing.list;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class JListController<T> implements MouseListener, KeyListener, ListDataListener {

	@FunctionalInterface
	public interface ListContextMenu<T> {
		void showContextMenu(List<T> selection, Component source, int x, int y);
	}

	public static <T> JListController<T> initializeListController() {
		JListModel<T> model = new JListModel<>();
		JList<T> list = new JList<>(model);
		return initializeListController(list, model);
	}

	public static <T> JListController<T> initializeListController(JList<T> list, JListModel<T> model) {
		return new JListController<>(list, model);
	}

	private final JList<T> list;
	private final JListModel<T> model;

	private Consumer<List<T>> onActivate;
	private Consumer<List<T>> onDelete;
	private Consumer<List<T>> onUpdate;
	private ListContextMenu<T> contextMenu;

	private JListController(JList<T> list, JListModel<T> model) {
		this.list = list;
		this.model = model;
		this.list.addMouseListener(this);
		this.list.addKeyListener(this);
		this.model.addListDataListener(this);
	}

	public List<T> getSelection() {
		List<T> result = new ArrayList<>();
		for(int i = 0; i < model.getSize(); i++) {
			if(list.isSelectedIndex(i)) {
				result.add(model.getElementAt(i));
			}
		}
		return result;
	}

	public void setOnActivate(Consumer<List<T>> onActivate) {
		this.onActivate = onActivate;
	}

	public void setRemoveOnDeletePressed() {
		setOnDelete(lst -> model.removeData(lst));
	}

	public void setOnDelete(Consumer<List<T>> onDelete) {
		this.onDelete = onDelete;
	}
	
	public void setOnUpdate(Consumer<List<T>> onUpdate) {
		this.onUpdate = onUpdate;
	}

	public void setContextMenu(ListContextMenu<T> contextMenu) {
		this.contextMenu = contextMenu;
	}

	public void addData(List<T> data) {
		this.model.addData(data);
	}

	public void removeData(List<T> data) {
		model.removeData(data);
	}

	public void setData(List<T> data) {
		model.setData(data);
	}

	public List<T> getData() {
		return model.getData();
	}

	public void setCellRenderer(ListCellRenderer<T> cellRenderer) {
		list.setCellRenderer(cellRenderer);
	}

	public JList<T> getList() {
		return list;
	}

	public void filter(Function<T, Boolean> filter) {
		model.filter(filter);
	}

	public void clearSelection() {
		list.setSelectedIndices(new int[] {});
	}

	public List<T> removeSelection() {
//		List<T> rm = model.getData(list.getSelectedIndices());
		List<T> rm = new ArrayList<>();
		for(int i =0 ; i < model.getSize();i++) {
			if(list.isSelectedIndex(i)) {
				rm.add(model.getElementAt(i));
			}
		}
		model.removeData(rm);
		list.setSelectedIndices(new int[0]);
		return rm;
	}

	public void clear() {
		model.clear();
		list.setSelectedIndices(new int[0]);
	}

	public void setSelectionModeSingle() {
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void setSelectionModeMulti() {
		list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public void setSelectionMode(int mode) {
		list.getSelectionModel().setSelectionMode(mode);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (Objects.nonNull(onActivate) && e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
			onActivate.accept(getSelection());
		} else if (Objects.nonNull(contextMenu) && e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
//			contextMenu.accept(getSelection(), list);
			contextMenu.showContextMenu(getSelection(), list, e.getX(), e.getY());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if (Objects.nonNull(onActivate) && e.getKeyCode() == KeyEvent.VK_ENTER) {
			onActivate.accept(getSelection());
		} else if (Objects.nonNull(onDelete) && e.getKeyCode() == KeyEvent.VK_DELETE) {
			onDelete.accept(getSelection());
		} else {
//			System.err.println(e.getKeyCode() + "?" + KeyEvent.VK_CONTEXT_MENU);
//			if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_SPACE) {
//				System.err.println("ALT+SPACE");
//			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void intervalAdded(ListDataEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		if(Objects.nonNull(onUpdate)) {
			onUpdate.accept(getData());
		}
	}

	public void refresh() {
		model.refresh();
	}
}
