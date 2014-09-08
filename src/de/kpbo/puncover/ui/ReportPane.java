package de.kpbo.puncover.ui;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by karl on 07/09/14.
 */
public class ReportPane extends JBScrollPane {

    private final JEditorPane editorPane;

    public ReportPane() {

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);

        setViewportView(editorPane);
        this.editorPane = editorPane;

    }

    public void setUrl(String url) throws IOException {
        editorPane.setPage(url);
    }

}
