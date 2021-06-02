package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.PasswordType
import com.bd.bdproject.common.BitDamApplication

class PasswordViewModel: ViewModel() {

    var type: PasswordType = PasswordType.TYPE_NEW

    val isActivate: MutableLiveData<Boolean> = MutableLiveData(BitDamApplication.pref.bitdamPassword != null)
    val tempPassword: MutableLiveData<String> = MutableLiveData()
    val confirmPassword: MutableLiveData<String> = MutableLiveData()
    val verifyPassword: MutableLiveData<String> = MutableLiveData()
    var passwordHint: String = ""

    fun clickButton(cmd: String) {
        var temp = when(type) {
            PasswordType.TYPE_NEW -> tempPassword.value?:""
            PasswordType.TYPE_CONFIRM -> confirmPassword.value?:""
            PasswordType.TYPE_VERIFY -> verifyPassword.value?:""
        }

        val length = temp.length

        if(cmd == "BACK") {
            temp = if(length == 0) { "" }
            else { temp.substring(0, length - 1) }
        } else {
            temp = if(length == 4) { temp }
            else { temp + cmd }
        }

        when(type) {
            PasswordType.TYPE_NEW -> tempPassword.value = temp
            PasswordType.TYPE_CONFIRM -> confirmPassword.value = temp
            PasswordType.TYPE_VERIFY -> verifyPassword.value = temp
        }
    }

    fun confirmPassword(): ConfirmPasswordEntry {
        val isSame = tempPassword.value == confirmPassword.value
        return ConfirmPasswordEntry(isSame, confirmPassword.value)
    }

    fun verifyPassword(): Boolean {
        return verifyPassword.value == BitDamApplication.pref.bitdamPassword
    }
}

data class ConfirmPasswordEntry(
    val isSame: Boolean,
    val password: String? = null
)