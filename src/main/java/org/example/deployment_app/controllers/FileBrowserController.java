package org.example.deployment_app.controllers;

import org.example.deployment_app.FileItem;
import org.example.deployment_app.services.FileBrowserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/browse")
public class FileBrowserController {
    private final FileBrowserService fileBrowserService;

    public FileBrowserController(FileBrowserService fileBrowserService) {
        this.fileBrowserService = fileBrowserService;
    }

    // we do GET method here: /api/browse/roots
    @GetMapping("/roots")
    public ResponseEntity<List<FileItem>> getRoots() {
        return ResponseEntity.ok(fileBrowserService.listRoots()); // returns list of directories

    }

    // /api/browse?path=C:\
    @GetMapping
    public ResponseEntity<?> browse(@RequestParam String path) {
        try {
            return ResponseEntity.ok(fileBrowserService.listDirectory(path));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
