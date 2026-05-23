package com.tripmate.controller;

import com.tripmate.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/api/trips/{tripId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID tripId,
                                            @AuthenticationPrincipal UserDetails ud) {
        byte[] pdf = exportService.generateTripPdf(tripId, ud.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"trip-summary.pdf\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .body(pdf);
    }
}
