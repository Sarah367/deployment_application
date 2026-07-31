package org.example.deployment_app.services;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ShareSessionService {
    private final List<String> sharedPaths = new CopyOnWriteArrayList<>();

    public void startSharing(List<String> paths) {
        sharedPaths.clear(); // wipes out paths from prev session
        sharedPaths.addAll(paths); // add new paths
    }

    public void stopSharing() {
        sharedPaths.clear();
    }

    public List<String> getSharedPaths() {
        return sharedPaths;
    }

    public boolean isSharing() {
        return !sharedPaths.isEmpty();
    }
}
