package org.example.deployment_app.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.example.deployment_app.services.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/qr")
public class QrController {
    private final NetworkService networkService;

    @Autowired
    public QrController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping("/share-url")
    public ResponseEntity<String> getShareUrl() {
        String url = buildShareUrl();
        return ResponseEntity.ok(url); // returns plain url in case the qr code doesnt work.
    }

    @GetMapping(value="/image", produces= MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrImage() {
        String url = buildShareUrl();

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE,300,300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix,"PNG", outputStream);

            return ResponseEntity.ok(outputStream.toByteArray());

        } catch (WriterException | java.io.IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String buildShareUrl() {
        String ip = networkService.getLocalNetworkIp();
        return "http://" + ip + ":8080/share.html";
    }
}
