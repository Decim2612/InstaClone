package com.example.instaclone

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.instaclone.databinding.ActivityCreateBinding
import com.example.instaclone.models.Post
import com.example.instaclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference



@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityCreateBinding
private const val PICK_PHOTO_CODE = 1234

@Suppress("DEPRECATION")
class CreateActivity : AppCompatActivity() {
    private var signedInUser: User? = null
    private var photoUri: Uri? = null
    private lateinit var firestoredb: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storageReference = FirebaseStorage.getInstance().reference
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
        binding.btnChooseImage.setOnClickListener {
            Log.i("bhsi image choose krle", "open up image picker on device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }
        binding.btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }

    }


    private fun handleSubmitButtonClick() {
        if (photoUri == null) {
            Toast.makeText(this, "no photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.AcDescription.text.isBlank()) {
            Toast.makeText(this, "description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (signedInUser == null) {
            Toast.makeText(this, "no signed in user", Toast.LENGTH_SHORT).show()
            return
        }
        binding.btnSubmit.isEnabled=false
        val photoUploadUri=photoUri as Uri
        Log.i("lalllllllllu","yha thak shi")
        //upload image to firebase storage
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
                Log.i("upload task", "uploaded bytes${photoUploadTask.result?.bytesTransferred}")
                //retrieve image from the fore base storage and fetch the url of that image
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                val post = Post(
                    binding.AcDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)
                firestoredb.collection("posts").add(post)
            }.addOnCompleteListener {postCreationTask->
                binding.btnSubmit.isEnabled=true
                if(!postCreationTask.isSuccessful){
                    Log.e("error0000","exception during firebase operations",postCreationTask.exception)
                    Toast.makeText(this,"failed to save post",Toast.LENGTH_SHORT).show()
                }
                binding.AcDescription.text.clear()
                binding.ivImage.setImageResource(0)
                Toast.makeText(this,"Sucess!",Toast.LENGTH_SHORT).show()
                val profileIntent=Intent(this,ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)
                finish()

            }

        //update the post collection with iage url and description

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                Log.i("photo", "photouri $photoUri")
                binding.ivImage.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "Image picker action cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }


}