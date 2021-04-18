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
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.databinding.ActivitySettingBinding
import com.bd.bdproject.util.DriveServiceHelper
import com.bd.bdproject.util.AlarmUtil
import com.bd.bdproject.util.AlarmUtil.NOT_USE_ALARM
import com.bd.bdproject.util.BitDamApplication
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
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
            settingLock.setOnClickListener {
                startActivity(Intent(it.context, SetPasswordActivity::class.java))
            }

            settingSendDataToDrive.setOnClickListener {
                requestSignIn(SEND_DATA)
            }

            settingRetrieveDataFromDrive.setOnClickListener {
                requestSignIn(RETRIEVE_DATA)
            }

            settingHelp.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO/*, Uri.fromParts("mailto", "", null)*/).apply {
                    type = "message/rfc822"
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("bitdam@gmail.com"))
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

        setSwitchPush()
        setSwitchAnimation()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            switchPush.isChecked = BitDamApplication.pref.usePush
            switchAnimation.isChecked = BitDamApplication.pref.isAnimationActivate
        }
    }

    private fun setSwitchPush() {
        binding.switchPush.setOnCheckedChangeListener { view, newCheckedState ->
            val oldCheckedState = BitDamApplication.pref.usePush
            BitDamApplication.pref.usePush = newCheckedState

            if(newCheckedState) {
                if(oldCheckedState != newCheckedState) {
                    AlarmUtil.setDairyAlarm(view.context, binding.root, 22)
                    AlarmUtil.setThreeDayAlarm(view.context)
                }
            } else {
                if(oldCheckedState != newCheckedState) {
                    AlarmUtil.setDairyAlarm(view.context, binding.root, NOT_USE_ALARM)
                    AlarmUtil.setThreeDayAlarm(view.context)
                }
            }
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

        val client: GoogleSignInClient = GoogleSignIn.getClient(this, signInOptions)
        val signInIntent = client.signInIntent

        when(type) {
            SEND_DATA -> { startSendDataForResult.launch(signInIntent) }
            RETRIEVE_DATA -> { startRetrieveDataForResult.launch(signInIntent) }
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
                Toast.makeText(this, "Uploaded successfully", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "저장 오류", Toast.LENGTH_SHORT).show()
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
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Found Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "파일 탐색 오류", Toast.LENGTH_SHORT).show()
                BitdamLog.contentLogger(it.message.toString())
            }
    }

    private fun downloadFile(fileId: String = "1I41aKcJYlI6Amip8Jyz6xndvHWxQ7zBP") {
        val progressDialog = ProgressDialog(this).apply {
            title = "Uploading to Google Drive"
            setMessage("Please wait...")
        }
        progressDialog.show()

        driveServiceHelper?.retrieveFile(fileId)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Downloaded Succesfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "다운로드 오류", Toast.LENGTH_SHORT).show()
                BitdamLog.contentLogger(it.message.toString())
            }

    }

/*    private fun setFirstAlarmTime(isNewAlarm: Boolean) {
        // val time = BitDamApplication.pref.dairyAlarmTime
        val calendar = Calendar.getInstance()
        // TEST
        calendar.timeInMillis = System.currentTimeMillis() + 1000 * 10
        *//*calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 31)
        calendar.set(Calendar.SECOND, 0)*//*

        if(calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1)
        }

        BitDamApplication.pref.dairyAlarmTime = calendar.timeInMillis

        dairyNotification(calendar, isNewAlarm)
    }

    private fun dairyNotification(calendar: Calendar, isNewAlarm: Boolean) {
        val pm = this.packageManager
        val receiver = ComponentName(this, DeviceBootReceiver::class.java)
        val alarmIntent = Intent(this, DairyAlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, 0)
        }
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(isNewAlarm) {
            val nextAlarmTime = calendar.time
            val toastMessage = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분").format(nextAlarmTime)
            Toast.makeText(this, "다음 알람은 $toastMessage 으로 설정되었습니다.", Toast.LENGTH_SHORT).show()

            // alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, 15 * 60000, alarmIntent)
            // alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)

            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        } else {
            alarmManager.cancel(alarmIntent)
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            Toast.makeText(this, "알람이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }

    }*/
}