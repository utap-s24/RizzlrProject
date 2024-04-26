package edu.utap.firenote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.utap.firenote.model.Matchmaker
import edu.utap.firenote.model.Message
import edu.utap.firenote.model.Profile

class ModernViewModel() : ViewModel() {

    private val dbHelp = AdvancedDBHelper()
    private val prof = MutableLiveData<Profile>()

    //Length of a chat in millisenconds
    private val chatLength = 300000

    private val matchmaker = MutableLiveData<Matchmaker>()

    private val allMessages = MutableLiveData<List<Message>>()

    private val chatWithPerson1 = MediatorLiveData<List<Message>>().apply {
        addSource(allMessages) {
            val filteredList : MutableList<Message> = mutableListOf()
            val myProfile = getProfile()
            for(m in it) {
                if(myProfile.firestoreID == m.receiverUID && myProfile.chatPartnerID1 == m.senderUID
                    || myProfile.firestoreID == m.senderUID && myProfile.chatPartnerID1 == m.receiverUID) {
                    filteredList.add(m)
                }
            }

            postValue(filteredList)
        }
    }
    private val chatWithPerson2 = MediatorLiveData<List<Message>>().apply {
        addSource(allMessages) {
            val filteredList : MutableList<Message> = mutableListOf()
            val myProfile = getProfile()
            for(m in it) {
                if(myProfile.firestoreID == m.receiverUID && myProfile.chatPartnerID2 == m.senderUID
                    || myProfile.firestoreID == m.senderUID && myProfile.chatPartnerID2 == m.receiverUID) {
                    filteredList.add(m)
                }
            }
            postValue(filteredList)
        }
    }
    private val chatAllReceived = MediatorLiveData<List<Message>>().apply {
        addSource(allMessages) {
            val filteredList : MutableList<Message> = mutableListOf()
            val myProfile = getProfile()

            for(m in it) {
                if(m.receiverUID == myProfile.firestoreID) {
                    filteredList.add(m)
                }
            }
            postValue(filteredList.reversed())
        }
    }

    fun login(uid : String, name : String) {
        dbHelp.getProfile(uid, name, prof)
    }
    fun observeProfile() : LiveData<Profile> {
        return prof
    }
    fun getProfile() : Profile {
        return prof.value!!
    }

    fun setQueueStatus(state : Int) {
        val temp = prof.value
        temp?.queueStatus = state
        prof.postValue(temp!!)
        updateCurrentProfile()
    }

    private fun updateCurrentProfile() {
        dbHelp.updateProfile(prof.value?.firestoreID!!, prof.value!!)
    }
    private fun updateProfile(userID : String, status : Int, chatID : String,
                              partnerID1 : String, partnerName1 : String,
                              partnerID2 : String, partnerName2 : String, end : java.util.Date) {
        dbHelp.updateProfileFields(userID, status, chatID, partnerID1, partnerName1, partnerID2, partnerName2, end)
    }
    private fun makeMatch(m : Matchmaker) {
        val chatID = dbHelp.createNewChat()
        val endTime = java.util.Date(System.currentTimeMillis() + chatLength)

        updateProfile(m.person1UUID, 2, chatID, m.person2UUID, m.person2Name,
            prof.value?.firestoreID!!, prof.value?.name!!, endTime)
        updateProfile(m.person2UUID, 2, chatID, prof.value?.firestoreID!!, prof.value?.name!!,
            m.person1UUID, m.person1Name, endTime)

        prof.value?.chatRoomID = chatID
        prof.value?.chatPartnerID1 = m.person1UUID
        prof.value?.chatPartnerName1 = m.person1Name
        prof.value?.chatPartnerID2 = m.person2UUID
        prof.value?.chatPartnerName2 = m.person2Name
        prof.value?.chatEndDate = endTime
    }
    private fun resetMatchmaker(m : Matchmaker) {
        m.count  = 0
        m.person2UUID = ""
        m.person2Name = ""
        m.person1UUID = ""
        m.person1Name = ""
    }
    fun queryMatchmaker() {
        dbHelp.getMatchmaker(matchmaker)
    }
    fun matchmakerUpdate(m : Matchmaker) {
        if(m.count == 0) {
            m.count++
            m.person1UUID = prof.value?.firestoreID!!
            m.person1Name = prof.value?.name!!
            prof.value?.queueStatus = 1
        } else if(m.count == 1) {
            m.count++
            m.person2UUID = prof.value?.firestoreID!!
            m.person2Name = prof.value?.name!!
            prof.value?.queueStatus = 1
        } else if(m.count == 2){
            makeMatch(m)
            resetMatchmaker(m)

            prof.value?.queueStatus = 2
        }
        val temp = prof.value
        prof.postValue(temp!!)

        updateCurrentProfile()

        dbHelp.updateMatchmaker(m)

    }

    fun createMessage(text : String, recipient : String) {
        val m = Message()
        val profile = prof.value!!
        m.text = text
        m.receiverUID = recipient
        m.senderUID = profile.firestoreID

        dbHelp.createMessage(m, profile.chatRoomID)
    }
    fun updateListeners(queueStatus : Int) {
        dbHelp.getProfile(AuthWrap.getCurrentUser().uid, AuthWrap.getCurrentUser().name, prof)

        if(queueStatus == 1) {
            dbHelp.killMessageListener()
            dbHelp.instantiateProfileListener(getProfile().firestoreID, prof)
        } else if(queueStatus == 2) {
            dbHelp.killProfileListener()
            dbHelp.instantiateMessageListener(getProfile(), allMessages)
        } else if(queueStatus == 3) {
            dbHelp.killMessageListener()
            dbHelp.instantiateProfileListener(getProfile().firestoreID, prof)
        } else if(queueStatus == 0) {
            dbHelp.killProfileListener()
        }
    }
    fun deleteChat(chatId : String) {
        val currentProf = getProfile()
        var sentCount = 0
        var receivedCount = 0
        if(allMessages.value != null) {
            for(m in allMessages.value!!) {
                if(m.receiverUID == currentProf.firestoreID) {
                    receivedCount++
                }
                if(m.senderUID == currentProf.firestoreID) {
                    sentCount++
                }
            }
        }

        dbHelp.adjustMessagesSentAndReceived(currentProf.firestoreID, sentCount, receivedCount)
        dbHelp.deleteChat(chatId)
    }
    fun adjustRatings(personID : String, amount : Int) {
        dbHelp.adjustRating(personID, amount)
    }
    fun observeMatchmaker() : LiveData<Matchmaker> {
        return matchmaker
    }

    fun observeChatWithPerson1() : LiveData<List<Message>> {
        return chatWithPerson1
    }
    fun observeChatWithPerson2() : LiveData<List<Message>> {
        return chatWithPerson2
    }
    fun observeChatAllReceived() : LiveData<List<Message>> {
        return chatAllReceived
    }
}

