package org.example.deployment_app.controllers;

import org.example.deployment_app.ShareRequest;
import org.example.deployment_app.services.NetworkService;
import org.example.deployment_app.services.ShareSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.Inet4Address;

@RestController
@RequestMapping("/api/share")
public class ShareController {
    private final ShareSessionService shareSessionService;
    private final NetworkService networkService;

    public ShareController(ShareSessionService shareSessionService, NetworkService networkService) {
        this.shareSessionService = shareSessionService;
        this.networkService = networkService;
    }

    @PostMapping
    public ResponseEntity<String> startSharing(@RequestBody ShareRequest request) {
        shareSessionService.startSharing(request.paths());
        //System.out.println(networkService.getLocalNetworkIp()//);
        return ResponseEntity.ok("Sharing started with " + request.paths().size() + " item(s).");

    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopSharing() {
        shareSessionService.stopSharing();
        return ResponseEntity.ok("Sharing stopped.");
    }
}
