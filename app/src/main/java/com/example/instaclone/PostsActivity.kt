package com.example.instaclone

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instaclone.databinding.ActivityPostsBinding
import com.example.instaclone.models.Post
import com.example.instaclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


const val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostsActivity : AppCompatActivity() {
    private var signedInUser: User? = null
    private lateinit var firestoredb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter
    private lateinit var binding: ActivityPostsBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)


//imp steps to make an rv
        //create layout file which represents one post-done
        //create data source-done
        posts = mutableListOf()
        //create the adapter
        adapter = PostsAdapter(this, posts)
        //bind the adapter and layout manager to the rv
        binding.rvPosts.adapter = adapter
        binding.rvPosts.layoutManager = LinearLayoutManager(this)

        firestoredb = FirebaseFirestore.getInstance()
        firestoredb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid.toString()).get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i("sucess bisi", "signed in user:${signedInUser}")


            }
            .addOnFailureListener { exception ->
                Log.i("failure hogya guru", "failure ", exception)

            }


        var postsReference = firestoredb.collection("posts").limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)
        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username != null) {
            supportActionBar?.title = username
            postsReference = postsReference.whereEqualTo("user.username", username)
        }
        postsReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for (post in postList) {
                Log.i("why fail", "Post $post")
            }
        }
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_profile) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}