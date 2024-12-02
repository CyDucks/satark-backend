package org.cyducks.satark.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cyducks.satark.data.ConflictZone;
import org.cyducks.satark.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final FCMService fcmService;

    public ConflictZone createZone(ConflictZone zone) {
        ConflictZone savedZone = zoneRepository.save(zone);
        log.info(savedZone.toString());
        fcmService.broadcastNewZone(zone.getId());

        return savedZone;
    }

    public ConflictZone getZone(String zoneId) {
        return zoneRepository.findById(zoneId).orElseThrow(() -> new NoSuchElementException("Zone not found: " + zoneId));
    }


    public List<ConflictZone> getActiveZones() {
        return zoneRepository.findByActiveTrue();
    }

    public void deactivateZone(String zoneId) {
        ConflictZone zone = getZone(zoneId);
        zone.setActive(false);
        zoneRepository.save(zone);
        try {
            fcmService.sendPushNotification("fs", "fs", "fs");
        } catch (ExecutionException | FirebaseMessagingException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
