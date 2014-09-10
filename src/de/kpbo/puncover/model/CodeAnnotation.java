package de.kpbo.puncover.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by karl on 07/09/14.
 */
public class CodeAnnotation {

    private CodeFile file;
    private int lineNumber;
    private String shortText;
    private String longText;
    private String name;

    public CodeFile getFile() {
        return file;
    }

    public void setFile(CodeFile file) {
        this.file = file;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public String getLongText() {
        return longText;
    }

    public void setLongText(String longText) {
        this.longText = longText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHTMLPath(Project project) {

        VirtualFile buildDirectory = project.getBaseDir().findChild("build");
        if (buildDirectory == null || !buildDirectory.isDirectory()) {
            return null;
        }

        VirtualFile htmlBaseDirectory = buildDirectory.findChild("puncover");
        if (htmlBaseDirectory == null || !htmlBaseDirectory.isDirectory()) {
            System.err.println("not a directory");
            return null;
        }

        String fileName = file.getName();

        VirtualFile fileDirectory = htmlBaseDirectory.findChild(fileName);
        if (fileDirectory == null || !fileDirectory.isDirectory()) {
            System.err.println("not a directory");
            return null;
        }

        VirtualFile htmlFile = fileDirectory.findChild(getSymbolName() + ".html");
        if (htmlFile == null) {
            System.err.println("cannot find html fileName");
            return null;
        }

        String url = htmlFile.toString();

        return url;
    }

    private String getSymbolName() {
        return "symbol_" + name;
    }
}
