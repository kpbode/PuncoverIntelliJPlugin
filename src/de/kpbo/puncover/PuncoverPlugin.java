package de.kpbo.puncover;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorGutterAction;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import de.kpbo.puncover.gutter.CodeTextAnnotationGutterProvider;
import de.kpbo.puncover.gutter.ShowAnnotationDetailsGutterAction;
import de.kpbo.puncover.model.CodeFile;
import de.kpbo.puncover.model.CodeStatistics;
import de.kpbo.puncover.ui.ReportPane;
import de.kpbo.puncover.utils.VirtualFileUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

//import java.nio.file.*;

/**
 * Created by karl on 07/09/14.
 */
public class PuncoverPlugin implements ProjectComponent, EditorFactoryListener {

    public static final String DETAILS_TOOL_WINDOW_ID = "Details";

    private final Project project;
    private final HashMap<String, Editor> editors;
    private CodeStatistics statistics;
    private VirtualFileAdapter virtualFileAdapter;

    public PuncoverPlugin(Project project) {
        this.project = project;
        this.editors = new HashMap<String, Editor>();
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "PuncoverPlugin";
    }

    public void projectOpened() {

        registerToolWindow();

        readCodeStatisticsFromProjectFile();


        EditorFactory.getInstance().addEditorFactoryListener(this, new Disposable() {
            @Override
            public void dispose() {
            }
        });

        createFileWatcher();

    }

    private void readCodeStatisticsFromProjectFile() {
        CodeStatistics statistics = CodeStatistics.read(project.getBaseDir());
        this.statistics = statistics;
    }

    private void rereadCodeStatistics() {

        for (Editor editor : editors.values()) {
            editor.getGutter().closeAllAnnotations();
        }

        readCodeStatisticsFromProjectFile();

        for (Editor editor : editors.values()) {

            setupGutterAnnotationsForEditor(editor);

        }

    }

    private void createFileWatcher() {

        // TODO: currently it seems as if the FileAdapter only calls back when the IDE comes into foreground again. 

        this.virtualFileAdapter = new VirtualFileAdapter() {

            private void handleFileEvent(VirtualFileEvent event) {

                if (CodeStatistics.FILE_NAME.equals(event.getFileName())) {

                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            rereadCodeStatistics();
                        }
                    });

                }

            }

            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                super.fileCreated(event);

                handleFileEvent(event);
            }

            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                super.contentsChanged(event);

                handleFileEvent(event);
            }
        };

        VirtualFileManager.getInstance().addVirtualFileListener(virtualFileAdapter);
    }

    public void projectClosed() {

        unregisterToolWindow();

        removeFileWatcher();
    }

    private void removeFileWatcher() {

        VirtualFileManager.getInstance().removeVirtualFileListener(virtualFileAdapter);

    }

    private void registerToolWindow() {

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(DETAILS_TOOL_WINDOW_ID, false, ToolWindowAnchor.RIGHT);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        ReportPane reportPane = new ReportPane();

        Content content = contentFactory.createContent(reportPane, "", true);

        toolWindow.getContentManager().addContent(content);
        //toolWindow.setTitle("Puncover");

    }

    private void unregisterToolWindow() {

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(DETAILS_TOOL_WINDOW_ID);

    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent editorFactoryEvent) {

        Editor editor = editorFactoryEvent.getEditor();
        VirtualFile file = setupGutterAnnotationsForEditor(editor);
        if (file == null) {
            return;
        }

        editors.put(file.getName(), editor);
    }

    private VirtualFile setupGutterAnnotationsForEditor(Editor editor) {

        if (statistics == null) {
            return null;
        }

        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());

        if (file == null) {
            return null;
        }

        String relativeFilePath = VirtualFileUtils.determineRelativePathFromFileToDirectory(file, project.getBaseDir());
        CodeFile codeFile = statistics.getCodeFileForPath(relativeFilePath);

        TextAnnotationGutterProvider annotationGutterProvider = new CodeTextAnnotationGutterProvider(project, codeFile);
        EditorGutterAction gutterAction = new ShowAnnotationDetailsGutterAction(project, codeFile);

        editor.getGutter().registerTextAnnotation(annotationGutterProvider, gutterAction);
        return file;
    }

    @Override
    public void editorReleased(@NotNull EditorFactoryEvent editorFactoryEvent) {

        Editor editor = editorFactoryEvent.getEditor();
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file == null) {
            return;
        }

        editors.remove(file.getName());

    }

}
