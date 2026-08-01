package org.example.deployment_app.services;

import org.example.deployment_app.FileItem;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.tags.Param;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FileBrowserService {
    // listing the top level drives/roots
    public List<FileItem> listRoots() {
        List<FileItem> roots = new ArrayList<>();

        for (File root : File.listRoots()) {
            roots.add(new FileItem(
               root.getPath(),
               root.getAbsolutePath(),
               true,
               0
            ));
        }
        return roots;
    }
    /**
    @param path absolute path of folder to look inside (which we get from the listRoots() function!)
    @throws IllegalArgumentException if path isnt actually real, throw exception.
    */
    public List<FileItem> listDirectory(String path) {
        File dir = new File(path);
        // make sure its actually a folder and that path is real.
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Not a valid directory: " + path);
        }

        File[] contents = dir.listFiles();
        if (contents == null) {
            throw new IllegalArgumentException("Cannot read directory (permission denied or driver is unplugged.)");

        }

        List<FileItem> items = new ArrayList<>();

        for (File file : contents) {
            items.add(new FileItem(
                file.getName(),
                file.getAbsolutePath(),
                file.isDirectory(),
                file.isDirectory() ? 0 : file.length()
            ));
        }

        items.sort(
                Comparator.comparing(FileItem::directory, Comparator.reverseOrder())
                        .thenComparing(FileItem::name, String.CASE_INSENSITIVE_ORDER)
        );

        return items;
    }

    public FileItem describePath(String path) {
        File file = new File(path);

        if (!file.exists()) {
            throw new IllegalArgumentException("Path no longer exists: " + path);
        }

        return new FileItem(
                file.getName(),
                file.getAbsolutePath(),
                file.isDirectory(),
                file.isDirectory() ? 0 : file.length()
        );
    }
}
