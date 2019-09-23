package com.client.traveller.data.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.client.traveller.data.db.UserDao
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.Users
import com.client.traveller.data.network.firebase.storage.Avatars
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RepositoryImpl(
    private val userDao: UserDao,
    private val usersFirestore: Users,
    private val avatars: Avatars
) : Repository {

    /**
     * Zapisuje dane użytkownika w lokalnej bazie danych co jest równoznaczne z zalogowaniem użytkownika w aplikacji
     * Oraz zapisuje dane użytkownika dp firestore
     *
     * Jest to operacja na którą musimy czekać
     *
     * @param user użytkownik do zalogowania
     */
    override fun saveUser(user: User) {
        GlobalScope.launch(Dispatchers.IO) {
            userDao.upsert(user)
        }

        //funkcje firebase gwarantują przynajmniej jedno prawidłowe wykonanie
        ////wykonuje się to gdy użytkownik jest już alogowany
        avatars.getDefaultAvatarImageReference().downloadUrl
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.e(javaClass.simpleName, it.result.toString())
                    user.image = it.result.toString()
                    usersFirestore.createUser(user)
                        .addOnFailureListener { exception ->
                            Log.e(javaClass.simpleName, exception.localizedMessage)
                            throw exception
                        }
                    //aktualizuje image url, tutaj ponieważ zajmuje dużo czasu to
                    GlobalScope.launch(Dispatchers.IO) {
                        userDao.upsert(user)
                    }
                } else {
                    it.exception?.let { exception ->
                        Log.e(javaClass.simpleName, exception.localizedMessage)
                        throw exception
                    }
                }
            }
    }



    override fun updateProfile(user: User) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(user.displayName)
            .setPhotoUri(Uri.parse(user.image))
            .build()

//        val tasks = mutableListOf<Task<Void>?>()
//
//        tasks.add(FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdates))
//        tasks.add(usersFirestore.updateEmail(user.idUserFirebase!!, user.email!!))
//

        FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdates)
            ?.addOnFailureListener {
                Log.e(javaClass.simpleName, it.localizedMessage)
            }
        usersFirestore.updateEmail(user.idUserFirebase!!, user.email!!)
            .addOnFailureListener {
                Log.e(javaClass.simpleName, it.localizedMessage)
            }
        usersFirestore.updateImage(user.idUserFirebase!!, user.image!!)
            .addOnFailureListener {
                Log.e(javaClass.simpleName, it.localizedMessage)
            }
        usersFirestore.updateUsername(user.idUserFirebase!!, user.displayName!!)
            .addOnFailureListener {
                Log.e(javaClass.simpleName, it.localizedMessage)
            }

        GlobalScope.launch(Dispatchers.IO){
            userDao.upsert(user)
        }
    }

    override fun getUser(): LiveData<User> {
        return userDao.getUser()
    }

    override fun deleteUserLocal() {
        GlobalScope.launch(Dispatchers.IO) {
            userDao.deleteUser()
        }
    }

    override suspend fun setEmailVerified() {
        GlobalScope.launch(Dispatchers.IO) {
            userDao.setEmailVerified()
        }
    }
}