package org.cyducks.satark.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class FirestoreService {

    private final Firestore firestore;

    public String getModeratorToken(String moderatorId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("fcm_tokens").document(moderatorId);
        DocumentSnapshot document = docRef.get().get();

        if(document.exists()) {
            return document.get("token", String.class);
        } else {
            return null;
        }
    }


}
