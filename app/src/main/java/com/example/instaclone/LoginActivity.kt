package com.example.instaclone

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.instaclone.databinding.ActivityLoginBinding

import com.google.firebase.auth.FirebaseAuth


@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityLoginBinding
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth=FirebaseAuth.getInstance()
        if (auth.currentUser!=null){
            goPostsActivity()
        }
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnlogin.setOnClickListener{
            binding.btnlogin.isEnabled=false
           val email= binding.name.text.toString()
           val password= binding.password.text.toString()
            if(email.isBlank()||password.isBlank()){
                Toast.makeText(this,"Email/password cannot be empty",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Firebase authentication

            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task->
                binding.btnlogin.isEnabled=true
                if (task.isSuccessful){
                    Toast.makeText(this,"Success!",Toast.LENGTH_LONG).show()
                    goPostsActivity()
                }else{
                    Log.e(TAG,"signIn with Email Failed",task.exception)
                    Toast.makeText(this,"Authentication failed!",Toast.LENGTH_SHORT).show()
                }
            }
        }




    }

    private fun goPostsActivity() {
        Log.i(TAG,"goPostsActivity")
        val intent=Intent(this,PostsActivity::class.java)
        startActivity(intent)
        finish()
    }
}