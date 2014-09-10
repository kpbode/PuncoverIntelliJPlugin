package de.kpbo.puncover.gutter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.project.Project;
import de.kpbo.puncover.model.CodeAnnotation;
import de.kpbo.puncover.model.CodeFile;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

/**
 * Created by karl on 07/09/14.
 */
public class CodeTextAnnotationGutterProvider implements TextAnnotationGutterProvider {

    private final Project project;
    private final CodeFile file;

    public CodeTextAnnotationGutterProvider(Project project, CodeFile file) {
        this.project = project;
        this.file = file;
    }

    @Nullable
    @Override
    public String getLineText(int i, Editor editor) {

        CodeAnnotation annotation = file.getAnnotationAtLineNumber(i);
        if (annotation == null) {
            return null;
        }

        return annotation.getShortText();
    }

    @Nullable
    @Override
    public String getToolTip(int i, Editor editor) {

        CodeAnnotation annotation = file.getAnnotationAtLineNumber(i);
        if (annotation == null) {
            return null;
        }

        return annotation.getLongText();
    }

    @Override
    public EditorFontType getStyle(int i, Editor editor) {

        return EditorFontType.PLAIN;
    }

    @Nullable
    @Override
    public ColorKey getColor(int i, Editor editor) {
        return null;
    }

    @Nullable
    @Override
    public Color getBgColor(int i, Editor editor) {
        return null;
    }

    @Override
    public List<AnAction> getPopupActions(int i, Editor editor) {
        return null;
    }

    @Override
    public void gutterClosed() {

    }

}

