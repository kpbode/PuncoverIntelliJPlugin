package de.kpbo.puncover.utils;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.net.URI;

/**
 * Created by karl on 08/09/14.
 */
public class VirtualFileUtils {

    public static String determineRelativePathFromFileToDirectory(VirtualFile file, VirtualFile directory) {

        URI directoryURI = new File(directory.getPath()).toURI();
        URI fileURI = new File(file.getPath()).toURI();
        URI relativizedURI = directoryURI.relativize(fileURI);

        return relativizedURI.getPath();
    }

}
