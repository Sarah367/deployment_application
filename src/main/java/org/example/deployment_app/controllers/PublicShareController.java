package org.example.deployment_app.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.example.deployment_app.FileItem;
import org.example.deployment_app.services.FileBrowserService;
import org.example.deployment_app.services.ShareSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public-share")
public class PublicShareController {
    private final ShareSessionService shareSessionService;
    private final FileBrowserService fileBrowserService;
    private static final long MAX_CHUNK_SIZE = 1024 * 1024;
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

    @GetMapping("/download")
    public void downloadFile(@RequestParam String file, @RequestHeader HttpHeaders header, HttpServletResponse response) throws IOException {
        List<String> sharedPaths = shareSessionService.getSharedPaths();
        if (!fileBrowserService.isPathWithinSharedPaths(file, sharedPaths)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File target = new File(file);


        if (!target.exists() || target.isDirectory()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //UrlResource resource = new UrlResource(target.toURI());

        long contentLength = target.length();
        String mimeType = Files.probeContentType(target.toPath());
        response.setContentType(mimeType != null ? mimeType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

        List<HttpRange> ranges = header.getRange();

        if (ranges.isEmpty()) {
            // process it as a regular file
            response.setStatus(HttpStatus.OK.value());
            response.setContentLengthLong(contentLength);
            Files.copy(target.toPath(), response.getOutputStream());
            return;
        }
        // specific byte range requested otherwise.
        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(contentLength);
        long end = range.getRangeEnd(contentLength);

        if (start > end) {
            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength);
            response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
            return;
        }
        long rangeLength = Math.min(MAX_CHUNK_SIZE, end - start+1);
        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
        response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + (start +rangeLength-1) + "/" + contentLength);
        response.setContentLengthLong(rangeLength);

        try (RandomAccessFile raf = new RandomAccessFile(target, "r")) {
            OutputStream out = response.getOutputStream();
            raf.seek(start);
            byte[] buffer = new byte[8192];
            long remaining = rangeLength; int read;
            while (remaining > 0 && (read = raf.read(buffer,0, (int) Math.min(buffer.length, remaining))) != -1) {
                out.write(buffer, 0, read);
                remaining -= read;
            }
        }

    }
}
