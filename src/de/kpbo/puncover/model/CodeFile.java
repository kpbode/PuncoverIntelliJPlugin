package de.kpbo.puncover.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by karl on 08/09/14.
 */
public class CodeFile {

    private Map<Integer, CodeAnnotation> annotations = new HashMap<Integer, CodeAnnotation>();
    private String name;
    private String path;

    public CodeFile(String filePath) {
        this.path = filePath;
        this.name = extractFileNameFromPath(filePath);
    }

    private String extractFileNameFromPath(String filePath) {

        if (filePath == null) {
            return null;
        }

        int lastSlashIndex = filePath.lastIndexOf("/");
        if (lastSlashIndex > -1) {
            return filePath.substring(lastSlashIndex + 1);
        }

        return filePath;
    }

    public void addAnnotation(CodeAnnotation annotation) {
        if (annotation == null) {
            return;
        }

        annotation.setFile(this);

        annotations.put(annotation.getLineNumber(), annotation);
    }

    public CodeAnnotation getAnnotationAtLineNumber(int lineNumber) {

        if (annotations == null) {
            return null;
        }

        CodeAnnotation annotation = annotations.get(lineNumber + 1);
        if (annotation == null) {
            return null;
        }

        return annotation;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
