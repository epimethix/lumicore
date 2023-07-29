package com.github.epimethix.lumicore.swing.remoteai;

import java.io.File;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.github.epimethix.lumicore.remoteai.Generator;
import com.github.epimethix.lumicore.swing.control.LTextArea;

@SuppressWarnings("serial")
public class AIChatPanel extends JPanel {
	private final LTextArea taPrompt;
	
	private final JSplitPane spLayout;
	
	public AIChatPanel(JFrame frame, Generator generator, Supplier<File> outputDir) {
		taPrompt = new LTextArea(5, 500);
		spLayout = new JSplitPane();
	}
}
