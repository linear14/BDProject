package com.bd.bdproject.test

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivityTestBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*

class TestActivity : AppCompatActivity() {

    lateinit var driveServiceHelper: DriveServiceHelper

    lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestSignIn()

        binding.btnTest.setOnClickListener { uploadFile() }

    }

    private fun requestSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        val client: GoogleSignInClient = GoogleSignIn.getClient(this@TestActivity, signInOptions)
        val signInIntent = client.signInIntent
        this.startActivityForResult(signInIntent, 400)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            400 -> {
                if(resultCode == RESULT_OK) {
                    handleSignInIntent(data)
                }
            }
        }
    }

    private fun handleSignInIntent(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { signInAccount ->
                val credential = GoogleAccountCredential
                    .usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))

                credential.selectedAccount = signInAccount.account

                val googleDriveService = Drive.Builder (
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
                )
                    .setApplicationName("My Drive Tutorial")
                    .build()

                driveServiceHelper = DriveServiceHelper(googleDriveService)

            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

    }

    fun uploadFile() {
        val progressDialog = ProgressDialog(this).apply {
            title = "Uploading to Google Drive"
            setMessage("Please wait...")
        }
        progressDialog.show()

        val filePath = "/data/data/com.bd.bdproject/databases/BITDAM_DB"
        driveServiceHelper?.createFile(filePath)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded successfully", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Check your google drive api key", Toast.LENGTH_SHORT).show()
            }

    }
}