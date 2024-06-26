package edu.utap.firenote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date as javaTimestamp

// Firebase insists we have a no argument constructor
data class Profile(
    //Queue Status: 0 - not queued, 1 - waiting, 2 - matched
    var queueStatus: Int = 0,
    var chatRoomID: String = "",
    var chatPartnerID1: String = "",
    var chatPartnerName1: String = "",
    var chatPartnerID2: String = "",
    var chatPartnerName2: String = "",
    var name: String = "",
    var rating: Int = 0,
    var maxRating: Int = 0,
    var sent: Int = 0,
    var received: Int = 0,
    var chatEndDate: javaTimestamp? = null,

    // Written on the server
    @ServerTimestamp val timeStamp: Timestamp? = null,
    // FirestoreID is generated by firestore, used as primary key
    @DocumentId var firestoreID: String = ""
)
