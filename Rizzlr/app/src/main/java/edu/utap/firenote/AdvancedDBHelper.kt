package edu.utap.firenote

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import edu.utap.firenote.model.Chat
import edu.utap.firenote.model.Matchmaker
import edu.utap.firenote.model.Message
import edu.utap.firenote.model.Profile
import java.util.*

class AdvancedDBHelper() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val chatRoot = "allChats"
    private val profileRoot = "allProfiles"
    private val matchmakerRoot = "matchmaker"
    private val matchmakerDoc = "matchmakerDoc"
    private val messageRoot = "allMessages"

    private var messageListener : ListenerRegistration? = null
    private var profileListener : ListenerRegistration? = null

    // If we want to listen for real time updates use this
    // .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
    // But be careful about how listener updates live data
    // and noteListener?.remove() in onCleared
    fun adjustRating(uuid : String, amount : Int) {
        db.collection(profileRoot)
            .document(uuid)
            .get()
            .addOnSuccessListener {
                val myProf = it?.toObject(Profile::class.java)
                val myRating = myProf?.rating
                val maxRating = myProf?.maxRating
                myProf?.rating = myRating!! + amount

                db.collection(profileRoot)
                    .document(uuid)
                    .update("rating", myRating + amount)
                if(myRating + amount > maxRating!!) {
                    db.collection(profileRoot)
                        .document(uuid)
                        .update("maxRating", myRating + amount)
                }

            }
    }

    fun adjustMessagesSentAndReceived(uuid : String, sent : Int, received : Int) {
        db.collection(profileRoot)
            .document(uuid)
            .get()
            .addOnSuccessListener {
                val myProf = it?.toObject(Profile::class.java)
                val newSent = myProf?.sent?.plus(sent)
                val newReceived = myProf?.received?.plus(received)

                db.collection(profileRoot)
                    .document(uuid)
                    .update("sent", newSent)
                db.collection(profileRoot)
                    .document(uuid)
                    .update("received", newReceived)
            }
    }

    fun instantiateMessageListener(prof : Profile, result : MutableLiveData<List<Message>>) {
        messageListener = db.collection(chatRoot)
            .document(prof.chatRoomID)
            .collection(messageRoot)
            .orderBy("timeStamp")
            .addSnapshotListener () { value, e ->
                if (e != null) {
                    Log.w("MY NOTE", "Listen failed.", e)
                    return@addSnapshotListener
                }

                result.postValue(value?.documents?.mapNotNull {
                    it.toObject(Message::class.java)
                })

            }
    }
    fun instantiateProfileListener(userID : String, result : MutableLiveData<Profile>) {
        profileListener = db.collection(profileRoot)
            .document(userID)
            .addSnapshotListener() { value, e ->
                if (e != null) {
                    Log.w("MY NOTE", "Listen failed.", e)
                    return@addSnapshotListener
                }
                result.postValue(value?.toObject(Profile::class.java))
            }
    }
    fun killMessageListener() {
        messageListener?.remove()
    }
    fun killProfileListener() {
        profileListener?.remove()
    }
    fun createMessage(
        msg: Message,
        chatRoomID: String
    ) {
        // We can get ID locally
        // note.firestoreID = db.collection("allNotes").document().id

        db.collection(chatRoot)
            .document(chatRoomID)
            .collection(messageRoot)
            .add(msg)
    }
    fun getProfile(userUID: String, name: String, result: MutableLiveData<Profile>) {
        db.collection(profileRoot)
            .document(userUID)
            .get()
            .addOnSuccessListener {
                if(it.exists()) {
                    val postResult = it.toObject(Profile::class.java)
                    if(postResult?.name != name) {
                        postResult?.name = name
                        db.collection(profileRoot)
                            .document(userUID)
                            .update("name", name)
                    }
                    result.postValue(postResult)
                } else {
                    val prof = Profile(timeStamp = Timestamp.now(), chatRoomID = "none")
                    prof.firestoreID = userUID
                    prof.name = name
                    updateProfile(userUID, prof)
                    result.postValue(prof)
                }
            }
    }

    fun updateProfile(userUID: String, newProfile: Profile) {
        //SSS
        db.collection(profileRoot)
            .document(userUID)
            .set(newProfile)
            .addOnSuccessListener {
            }
    }
    fun getMatchmaker(result: MutableLiveData<Matchmaker>) {
        db.collection(matchmakerRoot)
            .document(matchmakerDoc)
            .get()
            .addOnSuccessListener {
                if(it.exists()) {
                    result.postValue(it.toObject(Matchmaker::class.java))
                } else{
                    val newMatchmaker = Matchmaker(firestoreID = matchmakerDoc)
                    result.postValue(newMatchmaker)
                    db.collection(matchmakerRoot)
                        .document(matchmakerDoc)
                        .set(newMatchmaker)
                }
            }
    }
    public fun updateMatchmaker(matchmaker: Matchmaker) {
        db.collection(matchmakerRoot)
            .document(matchmakerDoc)
            .set(matchmaker)
            .addOnSuccessListener {
            }
    }

    public fun createNewChat() : String {
        val chatID = UUID.randomUUID().toString()
        val myChat = Chat()
        myChat.peopleLeft = 3
        db.collection(chatRoot)
            .document(chatID)
            .set(myChat)
            .addOnSuccessListener {
            }
        return chatID
    }

    fun deleteChat(chatID : String) {
        db.collection(chatRoot)
            .document(chatID)
            .get()
            .addOnSuccessListener {
                if(it.exists()) {
                    val myChat = it.toObject(Chat::class.java)

                    if(myChat?.peopleLeft == 1) {
                        db.collection(chatRoot)
                            .document(chatID)
                            .delete()
                    } else {
                        myChat?.peopleLeft = myChat?.peopleLeft?.minus(1)!!
                        db.collection(chatRoot)
                            .document(chatID)
                            .set(myChat)
                    }
                }

            }

    }
    public fun updateProfileFields(userID : String, status : Int, chatID : String,
                                    partner1ID : String, partnerName1 : String,
                                    partner2ID : String, partnerName2 : String,
                                    endDate : java.util.Date) {
        val updateMap = mapOf<String, Any>(
            "queueStatus" to status,
            "chatRoomID" to chatID,
            "chatPartnerID1" to partner1ID,
            "chatPartnerID2" to partner2ID,
            "chatPartnerName1" to partnerName1,
            "chatPartnerName2" to partnerName2,
            "chatEndDate" to endDate
        )

        db.collection(profileRoot)
            .document(userID)
            .update(updateMap)
    }
}