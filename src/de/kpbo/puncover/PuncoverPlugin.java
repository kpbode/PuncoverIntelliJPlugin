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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import de.kpbo.puncover.gutter.CodeTextAnnotationGutterProvider;
import de.kpbo.puncover.gutter.ShowAnnotationDetailsGutterAction;
import de.kpbo.puncover.model.CodeAnnotation;
import de.kpbo.puncover.model.CodeFile;
import de.kpbo.puncover.model.CodeStatistics;
import de.kpbo.puncover.ui.ReportPane;
import de.kpbo.puncover.utils.VirtualFileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by karl on 07/09/14.
 */
public class PuncoverPlugin implements ProjectComponent, EditorFactoryListener {

    public static final String DETAILS_TOOL_WINDOW_ID = "Details";

    private final Project project;
    private final HashMap<String, Editor> editors;
    private CodeStatistics statistics;

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

    private void createFileWatcher() {

        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {

                final VirtualFile jsonFile = project.getBaseDir().findChild(CodeStatistics.FILE_NAME);
                if (jsonFile == null) {
                    return;
                }

                try {

                    final Path path = FileSystems.getDefault().getPath(project.getBasePath());
                    final WatchService watchService = FileSystems.getDefault().newWatchService();
                    final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                    while (true) {

                        final WatchKey key;
                        try {
                            key = watchService.take();

                            for (WatchEvent<?> event : watchKey.pollEvents()) {

                                final Path p = (Path) event.context();
                                if (p.endsWith(CodeStatistics.FILE_NAME)) {

                                    ApplicationManager.getApplication().invokeLater(new Runnable() {

                                        @Override
                                        public void run() {

                                            for (Editor editor : editors.values()) {
                                                editor.getGutter().closeAllAnnotations();
                                            }

                                            readCodeStatisticsFromProjectFile();

                                            for (Editor editor : editors.values()) {

                                                setupGutterAnnotationsForEditor(editor);

                                            }


                                        }
                                    });


                                }
                            }

                            key.reset();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void projectClosed() {

        unregisterToolWindow();
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
