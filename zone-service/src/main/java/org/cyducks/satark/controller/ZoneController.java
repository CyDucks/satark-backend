package org.cyducks.satark.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cyducks.satark.data.ConflictZone;
import org.cyducks.satark.service.ZoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/zones")
@Slf4j
@RequiredArgsConstructor
public class ZoneController {
    private final ZoneService zoneService;

    @PostMapping("/conflict")
    public ResponseEntity<ConflictZone> createZone(@RequestBody ConflictZone zone) {
        log.info("Creating new conflict zone...");
        ConflictZone created = zoneService.createZone(zone);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{zoneId}")
    public ResponseEntity<ConflictZone> getZone(@PathVariable String zoneId) {
        log.info("Fetching zone with id: {}", zoneId);
        return ResponseEntity.ok(zoneService.getZone(zoneId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ConflictZone>> getActiveZones() {
        log.info("Fetching all active zones");
        return ResponseEntity.ok(zoneService.getActiveZones());
    }

    @DeleteMapping("/{zoneId}")
    public ResponseEntity<Void> deactivateZone(@PathVariable String zoneId) {
        log.info("Deactivating zone: {}", zoneId);
        zoneService.deactivateZone(zoneId);
        return ResponseEntity.ok().build();
    }
}
