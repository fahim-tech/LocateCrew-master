package com.example.locatecrew.data.firebase

import android.location.Location
import com.example.locatecrew.data.model.Announcement
import com.example.locatecrew.data.model.Comment
import com.example.locatecrew.data.model.Group
import com.example.locatecrew.data.model.LocationData
import com.example.locatecrew.data.model.User
import com.example.locatecrew.data.store.LoggedInUserManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FirebaseRepository @Inject constructor() {
    private val firestore = Firebase.firestore

    suspend fun getGroups(): QuerySnapshot = firestore.collection("groups").get().await()

    suspend fun createGroup(group: Group): String {
        val groupCode = generateRandomGroupCode()
        val newGroup = group.copy(groupid = groupCode)
        firestore.collection("groups").add(newGroup).await()
        return groupCode
    }

    suspend fun getUsers(): QuerySnapshot = firestore.collection("users").get().await()

    suspend fun createUser(user: User) {
        val userData = hashMapOf(
            "username" to user.username,
            "password" to user.password,
            "phone" to user.phone,
            "photoPath" to user.photoPath,
            "email" to user.email,
            "latitude" to user.location?.latitude,
            "longitude" to user.location?.longitude,
            "lastUpdatedTimestamp" to user.lastUpdatedTimestamp
        )
        firestore.collection("users").add(userData).await()
    }

    suspend fun getUserByUsername(username: String): User? {
        val querySnapshot = firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()

        println("I WAS HERE")
        val userData = querySnapshot.documents.firstOrNull()?.data ?: return null

        println("User: $userData")
        val user = User(
            username = userData["username"] as String,
            password = userData["password"] as String,
            phone = userData["phone"] as? String,
            photoPath = userData["photoPath"] as? String,
            email = userData["email"] as? String,
            location = LocationData(
                latitude = userData["latitude"] as Double?,
                longitude = userData["longitude"] as Double?
            )
        )
        println(user)
        return user
    }

    suspend fun getUsersByUsernames(usernames: List<String>): List<User> {
        if (usernames.isEmpty()) {
            return emptyList()
        }
        val querySnapshot = firestore.collection("users")
            .whereIn("username", usernames)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { doc ->
            val userData = doc.data
            User(
                username = userData?.get("username") as String,
                password = userData["password"] as String,
                phone = userData["phone"] as? String,
                photoPath = userData["photoPath"] as? String,
                email = userData["email"] as? String,
                location = LocationData(
                    latitude = userData["latitude"] as Double,
                    longitude = userData["longitude"] as Double
                )
            )
        }
    }

    private fun generateRandomGroupCode(): String {
        val random = Random
        return (100000..999999).random(random).toString()
    }

    suspend fun getGroupById(groupId: String): Group? {
        val documentSnapshot = try {
            firestore.collection("groups").whereEqualTo("groupid", groupId).get()
                .await().documents.firstOrNull()
        } catch (e: Exception) {
            println("Error getting group: $groupId, $e")
            return null
        }
        return documentSnapshot?.toObject(Group::class.java)
    }

    fun loadGroupById(groupId: String) : Flow<Group?> {
        return flow {
            val group = getGroupById(groupId)
            emit(group)
        }
    }

    fun getGroupByIdFlow(groupId: String): Flow<Group?> = flow {
        val group = getGroupById(groupId)
        emit(group)
    }

    suspend fun joinGroup(groupCode: String, username: String) {
        val groupData = firestore.collection("groups").whereEqualTo("groupid", groupCode).get().await().documents.firstOrNull()
        val groupDocId = groupData?.id ?: return
        val group = groupData.toObject(Group::class.java) ?: return
        println("HEREEEE     $group")
        if (group != null) {
            val newMemberUsernames = group.memberUsernames.toMutableList()
            newMemberUsernames.add(username)
            firestore.collection("groups").document(groupDocId).set(
                mapOf(
                    "memberUsernames" to newMemberUsernames
                ),
                SetOptions.merge()
            ).await()
        }
    }






    private fun generateRandomAnnouncementId(): String {
        val random = Random
        return (100000000..999999999).random(random).toString()
    }

    suspend fun getAnnouncementsForGroup(groupId: String): List<Announcement> {
        val querySnapshot = firestore.collection("announcements").whereEqualTo("groupId", groupId).get().await()
        return querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(Announcement::class.java)
        }
    }

    suspend fun getCommentsForAnnouncement(announcementId: String): List<Comment> {
        val querySnapshot = firestore.collection("comments").whereEqualTo("announcementId", announcementId).get().await()
        return querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(Comment::class.java)
        }
    }

    suspend fun getAnnouncementById(announcementId: String): Announcement? {
        val documentSnapshot = firestore.collection("announcements").whereEqualTo("id", announcementId).get().await().documents.firstOrNull()
        return documentSnapshot?.toObject(Announcement::class.java)
    }

    suspend fun addComment(comment: Comment) {
        val commentsCollection = firestore.collection("comments")
        commentsCollection.add(comment)
    }

    fun addAnnouncement(announcement: Announcement) {
        announcement.id = generateRandomAnnouncementId()
        val announcementsCollection = firestore.collection("announcements")
        announcementsCollection.add(announcement)
    }

    fun getAnnouncements(callback: (List<Announcement>?, Exception?) -> Unit) {
//        sorted by time created in descending
        val announcementsCollection = firestore.collection("announcements").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
        val announcements = mutableListOf<Announcement>()

        announcementsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val announcement = document.toObject(Announcement::class.java)
                    if (announcement != null) {
                        announcements.add(announcement)
                    }
                }
                callback(announcements, null)
            }
            .addOnFailureListener { e ->
                callback(null, e)
            }
    }

    fun getComments(callback: (List<Comment>?, Exception?) -> Unit) {
//        sorted by time created in descending
        val commentsCollection = firestore.collection("comments").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
        val comments = mutableListOf<Comment>()

        commentsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val comment = document.toObject(Comment::class.java)
                    if (comment != null) {
                        comments.add(comment)
                    }
                }
                callback(comments, null)
            }
            .addOnFailureListener { e ->
                callback(null, e)
            }
    }



//    LOCATION

    // Update the user's location in Firebase
    suspend fun updateUserLocation(username: String, latitude: Double, longitude: Double) {
        val timestamp = System.currentTimeMillis()
        val userRef = firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()
            .documents
            .firstOrNull()

        val userDocId = userRef?.id ?: return

        firestore.collection("users").document(userDocId).set(
            mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "lastUpdatedTimestamp" to timestamp
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun getUserLocation(username: String): LocationData? {
        val userRef = firestore.collection("users").whereEqualTo("username", username).get().await().documents.firstOrNull()
        val userData = userRef?.data ?: return null

        return userData["location"] as? LocationData
    }

    fun getGroupMembersFlow(groupId: String): Flow<List<User>> {
        return flow {
            val group = getGroupById(groupId)
            val usernames = group?.memberUsernames ?: emptyList()
            val users = getUsersByUsernames(usernames)
            emit(users)
        }
    }
}