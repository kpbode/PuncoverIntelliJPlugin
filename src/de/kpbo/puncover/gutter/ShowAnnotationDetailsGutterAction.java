package de.kpbo.puncover.gutter;

import com.intellij.openapi.editor.EditorGutterAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import de.kpbo.puncover.PuncoverPlugin;
import de.kpbo.puncover.model.CodeAnnotation;
import de.kpbo.puncover.model.CodeFile;
import de.kpbo.puncover.ui.ReportPane;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by karl on 07/09/14.
 */
public class ShowAnnotationDetailsGutterAction implements EditorGutterAction {

    private final Project project;
    private final CodeFile file;


    public ShowAnnotationDetailsGutterAction(Project project, CodeFile file) {
        this.project = project;
        this.file = file;
    }

    @Override
    public void doAction(int i) {

        CodeAnnotation annotation = file.getAnnotationAtLineNumber(i);
        if (annotation == null) {
            return;
        }

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow(PuncoverPlugin.DETAILS_TOOL_WINDOW_ID);

        Content content = toolWindow.getContentManager().getContent(0);
        if (content != null) {

            JComponent component = content.getComponent();
            if (component instanceof ReportPane) {

                ReportPane reportPane = (ReportPane) component;

                String url = annotation.getHTMLPath(project);
                try {
                    reportPane.setUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }

        if (!toolWindow.isVisible()) {
            toolWindow.show(null);
        }

    }

    @Override
    public Cursor getCursor(int i) {
        return null;
    }

}
