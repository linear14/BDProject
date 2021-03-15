package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.PasswordType
import com.bd.bdproject.util.BitDamApplication

class SetPasswordViewModel: ViewModel() {

    var type: PasswordType = PasswordType.TYPE_NEW

    val isActivate: MutableLiveData<Boolean> = MutableLiveData(BitDamApplication.pref.bitdamPassword != null)
    val tempPassword: MutableLiveData<String> = MutableLiveData()
    val confirmPassword: MutableLiveData<String> = MutableLiveData()
    var passwordHint: String = ""

    fun clickButton(cmd: String) {
        var temp = if(type == PasswordType.TYPE_NEW) {
            tempPassword.value?:""
        } else {
            confirmPassword.value?:""
        }

        val length = temp.length

        if(cmd == "BACK") {
            temp = if(length == 0) { "" }
            else { temp.substring(0, length - 1) }
        } else {
            temp = if(length == 4) { temp }
            else { temp + cmd }
        }

        if(type == PasswordType.TYPE_NEW) {
            tempPassword.value = temp
        } else {
            confirmPassword.value = temp
        }
    }

    fun confirmPassword(): ConfirmPasswordEntry {
        val isSame = tempPassword.value == confirmPassword.value
        return ConfirmPasswordEntry(isSame, confirmPassword.value)
    }
}

data class ConfirmPasswordEntry(
    val isSame: Boolean,
    val password: String? = null
)