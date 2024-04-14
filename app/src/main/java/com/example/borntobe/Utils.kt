package com.example.borntobe

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Utils(private val context: Context) {
    private var toast: Toast? = null

    // Toast message 생성 함수
    fun showToast(message: String) {
        toast?.cancel()
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }

    // 화면 터치 시 키보드 내리기
    fun hideKeyboard() {
        val activity = context as AppCompatActivity
        val currentFocusView = activity.currentFocus
        if (currentFocusView != null) {
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                currentFocusView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}