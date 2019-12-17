package com.client.traveller.data.repository.user

import android.net.Uri
import android.util.Log
import com.client.traveller.data.db.TripDao
import com.client.traveller.data.db.UserDao
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.auth.*
import com.client.traveller.data.network.firebase.firestore.Users
import com.client.traveller.data.network.firebase.storage.Avatars
import com.client.traveller.ui.util.Coroutines.io
import com.client.traveller.ui.util.toLocalUser
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val tripDao: TripDao,
    private val authNormal: AuthNormal,
    private val authGoogle: AuthGoogle,
    private val authFacebook: AuthFacebook,
    private val authUtils: AuthUtils,
    private val authProvider: AuthProvider
) : UserRepository {

    /**
     * W tle
     * Metoda loguje do aplikacji użytkownika który się zarejstrował - w tym momencie zostaje przekirowany do [HomeFragment]
     * Nestęnie zostaje dodany do firestore z domyśllnym avatarem
     *
     *
     * @param user użytkownik do zalogowania
     * @param function callback, w razie niepowodzenia zostaje wysłany exception
     */
    override fun registerUser(user: User, function: (Boolean, Exception?) -> Unit) {

        this.updateLocalUserDataAsync(user)

        io {

            //funkcje firebase gwarantują przynajmniej jedno prawidłowe wykonanie
            ////wykonuje się to gdy użytkownik jest już alogowany
            Avatars.getDefaultAvatarImageReference().downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    if (user.image == null) user.image = it.result.toString()
                    Users.createUser(user).addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            // aktualizacja avatara
                            updateLocalUserDataAsync(user)
                            function.invoke(it.isSuccessful, it.exception)
                        } else {
                            function.invoke(it.isSuccessful, it.exception)
                        }
                    }
                } else {
                    function.invoke(it.isSuccessful, it.exception)
                }
            }
        }
    }

    override fun loginUser(
        username: String,
        password: String,
        function: (Boolean, Exception?) -> Unit
    ) {

        authNormal.login(username, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    this.updateLocalUserDataAsync(it.result?.user?.toLocalUser()!!)
                } else {
                    function.invoke(it.isSuccessful, it.exception)
                }
            }
    }


    override fun loginGoogleUser(
        task: Task<GoogleSignInAccount>?,
        function: (Boolean, Exception?) -> Unit
    ) {
        if (task != null)
            authGoogle.login(task)?.addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result?.user
                    user?.let {
                        Users.getUserByUid(user.uid).addOnCompleteListener {
                            if (!it.result?.exists()!!) {
                                // createUser
                                this.registerUser(user.toLocalUser(), function)
                            } else {
                                // login
                                this.updateLocalUserDataAsync(user.toLocalUser())
                            }
                        }
                    }
                } else {
                    function.invoke(it.isSuccessful, it.exception)
                }
            }
    }

    override fun loginFacebookUser(
        accessToken: AccessToken,
        function: (Boolean, Exception?) -> Unit
    ) {
        authFacebook.login(accessToken).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = it.result?.user
                user?.let {
                    Users.getUserByUid(user.uid).addOnCompleteListener {
                        if (!it.result?.exists()!!) {
                            // createUser
                            this.registerUser(user.toLocalUser(), function)
                        } else {
                            // login
                            this.updateLocalUserDataAsync(user.toLocalUser())
                        }
                    }
                }
            } else {
                function.invoke(it.isSuccessful, it.exception)
            }
        }
    }

    /**
     * Pobranie danych o użytkowniku z firestore
     */
    // TODO mogło popsuć avatary
    override fun updateLocalUserDataAsync(user: User) {
        Users.getUserByUid(user.idUserFirebase!!).addOnSuccessListener { snapshot ->
            val userFirestore = snapshot.toObject(User::class.java)
            userFirestore?.let { io { userDao.upsert(it) } }
        }
    }

    override fun createUserEmailPassword(
        email: String,
        password: String,
        displayName: String,
        function: (Boolean, Exception?, FirebaseUser?) -> Unit
    ) {
        authNormal.createUser(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = it.result?.user
                user?.let { userNotNull ->
                    this.registerUser(userNotNull.toLocalUser(displayName)) { isSuccessful, exception ->
                        if (isSuccessful) {
                            function.invoke(isSuccessful, exception, userNotNull)
                        } else {
                            function.invoke(isSuccessful, exception, null)
                        }
                    }
                }
            } else {
                function.invoke(it.isSuccessful, it.exception, null)
            }
        }
    }

    override fun resetPasswordSendEmail(
        email: String,
        function: (Boolean, Exception?) -> Unit
    ) {
        authNormal.resetEmail(email).addOnCompleteListener {
            function.invoke(it.isSuccessful, it.exception)
        }
    }

    override fun updateProfile(
        user: FirebaseUser,
        profileChangeRequest: UserProfileChangeRequest,
        function: (Boolean, Exception?) -> Unit
    ) {
        authUtils.updateFirebaseUserProfile(user, profileChangeRequest).addOnCompleteListener {
            if (!it.isSuccessful) {
                function.invoke(it.isSuccessful, it.exception)
            }
        }
    }

    override fun sendEmailVerification(
        user: FirebaseUser,
        function: (Boolean, Exception?) -> Unit
    ) {
        authUtils.sendEmailVerification(user).addOnCompleteListener {
            function.invoke(it.isSuccessful, it.exception)
        }
    }

    override fun updateProfile(user: User) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(user.displayName)
            .setPhotoUri(Uri.parse(user.image))
            .build()

        FirebaseAuth.getInstance().currentUser?.updateEmail(user.email!!)
        FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdates)
        Users.updateEmail(user.idUserFirebase!!, user.email!!)
        Users.updateImage(user.idUserFirebase!!, user.image!!)
        Users.updateUsername(user.idUserFirebase!!, user.displayName!!)
        this.updateLocalUserDataAsync(user)
    }

    override fun updateAvatar(user: User, imageUri: String) {
        Users.updateImage(user.idUserFirebase!!, user.image!!)
            .addOnFailureListener {
                Log.e(javaClass.simpleName, it.localizedMessage)
            }
    }

    override fun getCurrentUser() = this.userDao.getUser()

    override fun logoutUser(googleSignInClient: GoogleSignInClient) {
        io {
            userDao.deleteUser()
            tripDao.deleteCurrentTrip()
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            when (true) {
                authProvider.isUserFacebookAuth(user) -> authFacebook.logout()
                authProvider.isUserGoogleAuth(user) -> authGoogle.logout(googleSignInClient)
                authProvider.isUserNormalAuth(user) -> authNormal.logout()
            }
        }
    }

    override fun setEmailVerifiedAsync() {
        io {
            userDao.setEmailVerified()
            Users.changeVerifiedStatus(state = true)
        }
    }

    /**
     * Metoda zwraca wszystkich userow których emaile pasują do podanych jako parametr
     * w ostatniej iteracji pętli jeśli zakończy się prawdłowo wartość jest zwracana
     *
     * @param emails lista email szukanych userów
     */
    override suspend fun getUsersByEmails(emails: ArrayList<String>) = withContext(Dispatchers.IO) {
        suspendCoroutine<List<User>> { continution ->
            val users = mutableListOf<User>()
            for ((index, value) in emails.withIndex()) {
                if (index == emails.size - 1) {
                    Users.getUserByEmail(value).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var user: User? = null
                            try {
                                user = task.result?.first()?.toObject(User::class.java)
                            } catch (ex: Exception) {
                                if (ex is NoSuchElementException) Log.e(
                                    javaClass.simpleName,
                                    "no such element"
                                )
                            }
                            user?.let { users.add(it) }
                            continution.resumeWith(Result.success(users))
                        }
                    }
                } else {
                    Users.getUserByEmail(value).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var user: User? = null
                            try {
                                user = task.result?.first()?.toObject(User::class.java)
                            } catch (ex: Exception) {
                                if (ex is NoSuchElementException) Log.e(
                                    javaClass.simpleName,
                                    "no such element"
                                )
                            }
                            user?.let { users.add(it) }
                        }
                    }
                }
            }
        }
    }

    override suspend fun getUserByFirestoreId(id: String) = withContext(Dispatchers.IO) {
        suspendCoroutine<User> { continuation ->
            Users.getUserByUid(id).addOnSuccessListener {
                val user = it.toObject(User::class.java)
                user?.let { userNotNull -> continuation.resumeWith(Result.success(userNotNull)) }
            }
        }
    }

    override suspend fun getUsersByIds(ids: ArrayList<String>) = withContext(Dispatchers.IO) {
        suspendCoroutine<List<User>> { continution ->
            val users = mutableListOf<User>()
            for ((index, value) in ids.withIndex()) {
                if (index == ids.size - 1) {
                    Users.getUserByUid(value).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var user: User? = null
                            try {
                                user = task.result?.toObject(User::class.java)
                            } catch (ex: Exception) {
                                if (ex is NoSuchElementException) Log.e(
                                    javaClass.simpleName,
                                    "no such element"
                                )
                            }
                            user?.let { users.add(it) }
                            continution.resumeWith(Result.success(users))
                        }
                    }
                } else {
                    Users.getUserByUid(value).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var user: User? = null
                            try {
                                user = task.result?.toObject(User::class.java)
                            } catch (ex: Exception) {
                                if (ex is NoSuchElementException) Log.e(
                                    javaClass.simpleName,
                                    "no such element"
                                )
                            }
                            user?.let { users.add(it) }
                        }
                    }
                }
            }
        }
    }

    override suspend fun getUserName(uid: String) = withContext(Dispatchers.IO) {
        suspendCoroutine<String> { continuation ->
            Users.getUserByUid(uid).addOnSuccessListener {
                val name = it.getString("displayName")
                name?.let { s -> continuation.resumeWith(Result.success(s)) }
            }
        }
    }

    override suspend fun getCurrentUserNonLive(): User? = this.userDao.getUserNonLiveData()
}