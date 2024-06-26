package edu.utap.firenote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// Firebase insists we have a no argument constructor
data class Chat(
    var peopleLeft: Int = 0,
    // Written on the server
    @ServerTimestamp val timeStamp: Timestamp? = null,
    // FirestoreID is generated by firestore, used as primary key
    @DocumentId var firestoreID: String = ""
)
