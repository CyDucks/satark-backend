package org.cyducks.satark.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class FCMService {
    private static final String MASS_REPORT_EVENT = "org.cyducks.satark.MASS_REPORT_EVENT";

    @Autowired
    private FirestoreService firestoreService;

    @Autowired
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
}
