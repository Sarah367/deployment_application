package org.example.deployment_app.controllers;

import org.example.deployment_app.FileItem;
import org.example.deployment_app.services.FileBrowserService;
import org.example.deployment_app.services.ShareSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/public-share")
public class PublicShareController {
    private final ShareSessionService shareSessionService;
    private final FileBrowserService fileBrowserService;

    @Autowired
    public PublicShareController(ShareSessionService shareSessionService, FileBrowserService fileBrowserService) {
        this.shareSessionService = shareSessionService;
        this.fileBrowserService = fileBrowserService;
    }

    @GetMapping("/items")
    public ResponseEntity<List<FileItem>> getSharedItems() {
        List<String> sharedPaths = shareSessionService.getSharedPaths();
        List<FileItem> items = new ArrayList<>();

        for (String path : sharedPaths) {
            try {
                items.add(fileBrowserService.describePath(path));
            } catch (IllegalArgumentException e) {
                // skip file if moved/deleted suddenly.
            }
        }
        return ResponseEntity.ok(items);
    }

    @GetMapping
    public ResponseEntity<?> browseSharedPath(@RequestParam String path) {
        List<String> sharedPaths = shareSessionService.getSharedPaths();
        if (fileBrowserService.isPathWithinSharedPaths(path, sharedPaths)) {
            return ResponseEntity.ok(fileBrowserService.listDirectory(path));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied!");
    }
}
