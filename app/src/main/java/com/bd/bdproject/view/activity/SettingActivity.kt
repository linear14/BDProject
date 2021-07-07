package com.bd.bdproject.view.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.data.model.DBInfo
import com.bd.bdproject.databinding.ActivitySettingBinding
import com.bd.bdproject.dialog.DBSelector
import com.bd.bdproject.common.BitDamApplication
import com.bd.bdproject.common.Constant.INFO_DB
import com.bd.bdproject.common.DriveServiceHelper
import com.bd.bdproject.view.activity.SetPasswordActivity.Companion.SET_PASSWORD_FAILED
import com.bd.bdproject.view.activity.SetPasswordActivity.Companion.SET_PASSWORD_SUCCESS
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*

class SettingActivity : AppCompatActivity() {

    companion object {
        const val SEND_DATA = 1
        const val RETRIEVE_DATA = 2
    }

    private lateinit var driveServiceHelper: DriveServiceHelper
    lateinit var binding: ActivitySettingBinding

    private val showSnackBarForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getIntExtra("TYPE", -1)
            data?.let {
                when(data) {
                    SET_PASSWORD_SUCCESS -> {
                        Snackbar.make(binding.root, "암호 설정이 완료되었습니다.", Snackbar.LENGTH_SHORT).show()
                    }
                    SET_PASSWORD_FAILED -> {
                        Snackbar.make(binding.root, "설정 오류가 발생했습니다. 다시 시도해주세요.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val startSendDataForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleSignInIntent(result.data, SEND_DATA)
        }
    }

    private val startRetrieveDataForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleSignInIntent(result.data, RETRIEVE_DATA)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            settingPush.setOnClickListener {
                startActivity(Intent(it.context, PushActivity::class.java))
            }

            settingLock.setOnClickListener {
                val intent = Intent(it.context, SetPasswordActivity::class.java)
                showSnackBarForResult.launch(intent)
            }

            settingSendDataToDrive.setOnClickListener {
                requestSignIn(SEND_DATA)
            }

            settingRetrieveDataFromDrive.setOnClickListener {
                requestSignIn(RETRIEVE_DATA)
            }

            settingNotice.setOnClickListener {
                val category = "1.%20%EB%B9%9B%EB%8B%B4/%EA%B3%B5%EC%A7%80%EC%82%AC%ED%95%AD"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bdprojects.tistory.com/category/$category"))
                startActivity(intent)
            }

            settingReview.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("market://details?id=${packageName}")
                startActivity(intent)
            }

            settingQuestion.setOnClickListener {
                val category = "1.%20%EB%B9%9B%EB%8B%B4/%EC%9E%90%EC%A3%BC%20%EB%AC%BB%EB%8A%94%20%EC%A7%88%EB%AC%B8"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bdprojects.tistory.com/category/$category"))
                startActivity(intent)
            }

            settingHelp.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO/*, Uri.fromParts("mailto", "", null)*/).apply {
                    type = "message/rfc822"
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("project.bdapps@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "문의 제목")
                    putExtra(Intent.EXTRA_TEXT, "문의내용을 남겨주세요.\n(비밀번호 찾기 및 데이터 복원은 불가능합니다)")
                }
                startActivity(Intent.createChooser(emailIntent,""))
            }

            settingLicense.setOnClickListener {
                startActivity(Intent(it.context, LicenseActivity::class.java))
            }

            try {
                val pInfo: PackageInfo = applicationContext.packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName
                tvVersion.text = "현재 버전 $version"
            } catch (e: Exception) {
                tvVersion.text = "현재 버전을 알 수 없습니다."
            }

            btnBack.setOnClickListener { onBackPressed() }
        }

        setSwitchAnimation()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            switchAnimation.isChecked = BitDamApplication.pref.isAnimationActivate
        }
    }

    private fun setSwitchAnimation() {
        binding.switchAnimation.setOnCheckedChangeListener { _, newCheckedState ->
            val oldCheckedState = BitDamApplication.pref.isAnimationActivate
            BitDamApplication.pref.isAnimationActivate = newCheckedState

            if(newCheckedState) {
                if(oldCheckedState != newCheckedState) {
                    Snackbar.make(binding.root, "애니메이션이 활성화됩니다.", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                if(oldCheckedState != newCheckedState) {
                    Snackbar.make(binding.root, "애니메이션이 비활성화됩니다.", Snackbar.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun requestSignIn(type: Int) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        GoogleSignIn.getClient(this, signInOptions).also { client ->
            client.signOut()
                .addOnSuccessListener {
                    val signInIntent = client.signInIntent

                    when(type) {
                        SEND_DATA -> { startSendDataForResult.launch(signInIntent) }
                        RETRIEVE_DATA -> { startRetrieveDataForResult.launch(signInIntent) }
                    }
                }
                .addOnFailureListener {
                    Snackbar.make(binding.root, "구글 계정 연동에 실패했습니다.", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun handleSignInIntent(data: Intent?, type: Int) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { signInAccount ->
                val credential = GoogleAccountCredential
                    .usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))

                credential.selectedAccount = signInAccount.account

                val googleDriveService = Drive.Builder (
                    NetHttpTransport() /*AndroidHttp.newCompatibleTransport()*/,
                    GsonFactory(),
                    credential
                )
                    .setApplicationName("BITDAM_DRIVE")
                    .build()

                driveServiceHelper = DriveServiceHelper(googleDriveService)

                when(type) {
                    SEND_DATA -> { uploadFile() }
                    RETRIEVE_DATA -> { findFiles() }
                }

            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

    }

    private fun uploadFile() {
        val progressDialog = ProgressDialog(this).apply {
            title = "Uploading to Google Drive"
            setMessage("Please wait...")
        }
        progressDialog.show()

        val filePath = "/data/data/com.bd.bdproject/databases/BITDAM_DB"
        driveServiceHelper?.createFile(filePath)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Snackbar.make(binding.root, "성공적으로 저장되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Snackbar.make(binding.root, "저장 오류가 발생했습니다.", Snackbar.LENGTH_SHORT).show()
                BitdamLog.contentLogger(it.message.toString())
            }

    }

    private fun findFiles() {
        val progressDialog = ProgressDialog(this).apply {
            title = "Uploading to Google Drive"
            setMessage("Please wait...")
        }
        progressDialog.show()

        driveServiceHelper?.findFiles()
            .addOnSuccessListener { files ->
                progressDialog.dismiss()

                val dbNameBundle = Bundle().apply {
                    val bundleArrayList = arrayListOf<DBInfo>().apply {
                        files.forEach {
                            add(DBInfo(it.first, it.second))
                        }
                    }
                    putParcelableArrayList(INFO_DB, bundleArrayList)
                }

                val selector = DBSelector { id, dialog ->
                    downloadFile(id, dialog)
                }
                selector.arguments = dbNameBundle
                selector.show(supportFragmentManager, DBSelector.DB_SELECTOR)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Snackbar.make(binding.root, "파일 탐색에 실패했습니다.", Snackbar.LENGTH_SHORT).show()
                BitdamLog.contentLogger(it.message.toString())
            }
    }

    private fun downloadFile(fileId: String, dialog: DialogFragment) {
        val progressDialog = ProgressDialog(this).apply {
            title = "Uploading to Google Drive"
            setMessage("Please wait...")
        }
        progressDialog.show()

        BitdamLog.contentLogger("다운로드 파일 : $fileId")
        driveServiceHelper?.retrieveFile(fileId)
            .addOnSuccessListener {
                progressDialog.dismiss()
                dialog.dismiss()
                Snackbar.make(binding.root, "데이터를 성공적으로 불러왔습니다.", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                dialog.dismiss()
                Snackbar.make(binding.root, "다운로드 오류가 발생했습니다.", Snackbar.LENGTH_SHORT).show()
                BitdamLog.contentLogger(it.message.toString())
            }

    }
}