package com.smartsub.controller;

import com.smartsub.dto.CsvImportItemDto;
import com.smartsub.service.CsvImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private com.smartsub.service.ReceiptScanningService receiptScanningService;

    @PostMapping("/csv")
    public ResponseEntity<?> uploadTransactionCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please select a valid CSV file to upload"));
        }

        try {
            List<CsvImportItemDto> detected = csvImportService.parseAndDetectSubscriptions(file);
            return ResponseEntity.ok(detected);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error parsing CSV: " + e.getMessage()));
        }
    }

    @PostMapping("/receipt")
    public ResponseEntity<?> uploadReceipt(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please select an invoice or receipt file to upload"));
        }

        try {
            com.smartsub.dto.ReceiptScanResultDto result = receiptScanningService.scanReceipt(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error scanning receipt: " + e.getMessage()));
        }
    }
}
