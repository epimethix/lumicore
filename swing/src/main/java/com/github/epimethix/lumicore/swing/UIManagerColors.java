package com.github.epimethix.lumicore.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.github.epimethix.lumicore.swing.util.DialogUtils;
import com.github.epimethix.lumicore.swing.util.GridBagUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class UIManagerColors {
	public static final void showColorDialog(Component comp) {
		JPanel pnScroll = new JPanel(new GridBagLayout());
		{
			final GridBagConstraints c = GridBagUtils.initGridBagConstraints();
			UIManager.getDefaults().keySet().stream().sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
					.forEachOrdered(o -> {
						Color color = UIManager.getDefaults().getColor(o);
						if (Objects.nonNull(color)) {
							JPanel pnColor = new JPanel();
							pnColor.setBackground(color);
							GridBagUtils.addGridBagLine(pnScroll, c, new JLabel(o.toString()), pnColor);
						}
					});
		}
		JPanel view = new JPanel(new BorderLayout());
		view.add(LayoutUtils.initScrollPane(pnScroll), BorderLayout.CENTER);
		JButton btClose = new JButton("Close");
		JPanel pnCloseFlow = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		pnCloseFlow.add(btClose);
		view.add(pnCloseFlow, BorderLayout.SOUTH);
		JDialog dialog = DialogUtils.initializeJDialog(comp, "UIManager Colors", view, true);
		btClose.addActionListener(e -> dialog.setVisible(false));
		dialog.setVisible(true);
	}
}
