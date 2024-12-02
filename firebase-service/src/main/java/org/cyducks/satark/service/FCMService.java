package org.cyducks.satark.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Service
@AllArgsConstructor
@Slf4j
public class FCMService {
    private static final String MASS_REPORT_EVENT = "org.cyducks.satark.MASS_REPORT_EVENT";

    private FirestoreService firestoreService;
    private FirebaseMessaging firebaseMessaging;

    public void sendPushNotification(String moderatorId, String title, String body) throws ExecutionException, InterruptedException, FirebaseMessagingException {
        String token = firestoreService.getModeratorToken(moderatorId);

        if(token != null) {
            Message message = Message.builder()
                    .setToken(token)
                    .putData("event_type", MASS_REPORT_EVENT)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            firebaseMessaging.send(message);
        } else {
            throw new IllegalArgumentException("FCM token not found");
        }
    }

    public void broadcastNewZone(String zoneId) {
        Message message = Message.builder()
                .setTopic("zone_updates")
                .putData("type", "NEW_ZONE")
                .putData("zone_id", zoneId)
                .putData("timestamp", Instant.now().toString())
                .build();

        sendMessage(message);
    }

    public void broadcastZoneDeactivation(String zoneId) {
        Message message = Message.builder()
                .setTopic("zone_updates")
                .putData("type", "ZONE_DEACTIVATED")
                .putData("zoneId", zoneId)
                .build();

        sendMessage(message);
    }

    public void sendMessage(Message message) {
        firebaseMessaging.sendAsync(message)
                .addListener(() -> {
                    log.info("Message sendAsync");
                }, Executors.newSingleThreadExecutor());
    }
}
