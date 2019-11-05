//package com.client.traveller.ui.chat.messeage
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import androidx.recyclerview.widget.RecyclerView
//import com.client.traveller.R
//import com.client.traveller.data.db.entities.User
//
//class MesseageAdapter(
//    private val users: List<User>,
//    private val context: Context
//): RecyclerView.Adapter<> {
//
//    companion object {
//        private const val MSG_TYPE_LEFT = 0
//        private const val MSG_TYPE_RIGHT = 1
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) {
//        val view = LayoutInflater.from(this.context).inflate(R.layout.item_users_list_chat, parent, false)
//        return MesseageAdapter.
//    }
//
//    override fun getItemCount(): Int {
//        return users.size
//    }
//
//    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//        private lateinit var userName: String
//        private lateinit var profile_image: ImageView
//
//        init {
//            userName = itemView.findViewById(R.id.user_name)
//        }
//    }
//}