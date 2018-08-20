package com.example.shawn.anjeonmingug

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {
    fun getKeyHash(context: Context): String? {
        val manager = context.getPackageManager()
        val info = manager?.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES)
        /*val manager = context.packageManager
        val packageInfo = manager?.getPackageInfo(context, PackageManager.GET_SIGNATURES) ?: return null*/

        for (signature in info!!.signatures) {
            try {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
            } catch (e: NoSuchAlgorithmException) {
                Log.w(TAG, "Unable to get MessageDigest. signature=$signature", e)
            }

        }
        return null
    }
    var authStateListener: FirebaseAuth.AuthStateListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseAuth.getInstance().signOut() // test case
        var service = Intent(this, MyService:: class.java)
        startService(service);

        // push notification setup
        /*OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.idsAvailable { userId, registrationId ->
            Log.d("debug", "User:$userId")
            if (registrationId != null)
                Log.d("debug", "registrationId:$registrationId")
            }*/
        //println("///////     " + getKeyHash(this))



        //구� 로그�션
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        //구� 로그�래�� 만듬
        var googleSignInClient = GoogleSignIn.getClient(this,gso)

        //비�번호 찾기
        button_forgetpassword.setOnClickListener {
            var FindPasswordActivity = Intent(this, FindPasswordActivity::class.java)
            startActivity(FindPasswordActivity)
        }

        //�원가�창
        button_getstarted.setOnClickListener {
            var SignupActivity = Intent(this, SignupActivity::class.java)
            startActivity(SignupActivity)
        }
        //로그
        button_login.setOnClickListener {
            loginId()
        }

        button_google.setOnClickListener {
            var signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1)
        }
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            //�션
            var user = firebaseAuth.currentUser
            if (user != null) {
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }
    }
    fun loginId(){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(editText_email.text.toString(),editText_password.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "로그�에 �공��니", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                    }else{
                        Toast.makeText(this,task.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            //구� 로그�에 �공�을�어�는 �큰 값을 가지곈는 Task
            var  task = GoogleSignIn.getSignedInAccountFromIntent(data)
            //ApiException 캐스
            var account = task.getResult(ApiException::class.java)
            //Credential 구� 로그�에 �공�다�증
            var credential = GoogleAuthProvider.getCredential(account.idToken,null)
            //�이�베�스구� �용�록
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener {
                        task ->
                        if(task.isSuccessful){
                            Toast.makeText(this, "구� �이�동�공��니",Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this,task.exception.toString(),Toast.LENGTH_LONG).show()
                        }
                    }
        }
    }

}
