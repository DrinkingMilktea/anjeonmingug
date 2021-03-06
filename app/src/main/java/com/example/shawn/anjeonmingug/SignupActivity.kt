package com.example.shawn.anjeonmingug

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.*


@Suppress("DEPRECATION")
class SignupActivity : AppCompatActivity() {
    var authStateListener: FirebaseAuth.AuthStateListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate---------------------------------------")
        setContentView(R.layout.activity_signup)

        //로그인 세션을 체크하는 부분
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            //세션
            var user = firebaseAuth.currentUser
            if (user != null) {
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }
        button_signup.setOnClickListener {
            println("onCreate")
            createEmailId()
        }
    }

    var userId : String? = null;

    var database = FirebaseDatabase.getInstance()

    // not return

    private fun writeNewUser(name: String, email: String, pass: String, playerId : String) {
        class User {
            var key : String
            var name: String
            var email: String
            var pass : String
            var playerId : String
            var createdTime : Any

            constructor(key:String, name: String, email: String, pass: String, playerId : String) {
                this.key = key
                this.name = name
                this.email = email
                this.pass = pass
                this.playerId = playerId
                val now = System.currentTimeMillis()
                val date = Date(now)
                val sdf = SimpleDateFormat("yyyy-MM-dd : hh-mm-ss")
                val getTime = sdf.format(date)
                this.createdTime = getTime
            }
        }

        var myRef = database.getReference()

        val key = FirebaseAuth.getInstance().currentUser!!.uid
        val post =User(key!!, name, email, pass, playerId)
        myRef.child("Users").child(key!!).setValue(post)
    }

    fun createEmailId() {
        val userEmail = editText_email.text.toString()
        val userPass = editText_password.text.toString()
        val userName : String = editText_name.text.toString()

        // 푸쉬 알림에 필요한 토큰을 생성한다.
        val refreshedToken = FirebaseInstanceId.getInstance().token
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, userPass)
        .addOnCompleteListener { task ->
            if (task.isSuccessful){
                // 새로운 사용자를 생성하고 데이터베이스에 정보를 저장한다.
                this.writeNewUser(userName, userEmail, userPass, refreshedToken!!)
                Toast.makeText(this, "회원가입에 성공하였습니다.", Toast.LENGTH_LONG).show()
            }else {
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener!!)

    }

    override fun onPause() {
        super.onPause()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener!!)
    }
}