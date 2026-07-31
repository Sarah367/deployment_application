package org.example.deployment_app;

public record FileItem(
    String name, // display name only
    String path, // the full path on the disk.
    boolean directory, // true if its a folder, false if its a simple file
    long sizeBytes // file size

) {}
