package com.example.borntobe

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern

/** Utils.class :
 * 개발 편의를 위해 프로젝트 전체에서 자주 사용되는 특정 로직이나 독립적인 기능을 구현해둔 클래스 입니다.
 * val utils = Utils(this) 와 같이 생성자를 통해 현재 활성화된 Context를 매개변수로 전달합니다. */
class Utils(private val context: Context) {
    private var toast: Toast? = null

    /** showToast() 메소드
     * 토스트 메세지를 생성해 현재 활성화된 Context에 띄워주는 메소드 */
    fun showToast(message: String) {
        toast?.cancel()
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }

    /** hideKeyboard() 메소드
     * 현재 화면에서 Focus된 View(예 : EditText)가 아닌 화면의 다른 부분을 클릭하면,
     * 나타났던 키보드가 숨겨지는 메소드 */
    fun hideKeyboard() {
        val activity = context as AppCompatActivity
        val currentFocusView = activity.currentFocus
        if (currentFocusView != null) {
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                currentFocusView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    /** isRegularID() 메소드
     * 사용자가 입력한 ID와 PW가 조건에 맞는지 유효성 검사 후,
     * 유효한 ID와 PW면 true 리턴  */
    fun isRegularID(id: String, editText: EditText): Boolean {
        // ID 유효성 조건 : 6자 이상의 영문 or 영문과 숫자 조합
        val idPattern1 = "^[a-zA-Z]{6,}$"
        val idPattern2 = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]{2,}$"
        val isMatch1 = Pattern.matches(idPattern1, id)
        val isMatch2 = Pattern.matches(idPattern2, id)
        Log.i("isMatch", "isMatch1 = $isMatch1")
        Log.i("isMatch", "isMatch2 = $isMatch2")
        return if (isMatch1 || isMatch2) {
            editText.setBackgroundResource(R.drawable.bg_round_square_stroke_gray)
            true
        } else {
            editText.setBackgroundResource(R.drawable.bg_round_square_stroke_red)
            false
        }
    }
    fun isRegularPW(pw:String, editText: EditText): Boolean {
        // PW 유효성 조건 : 영어 + 숫자 + 특수 문자 + 글자수 8~16자
        val pwPattern = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#$%?_])[A-Za-z0-9!@#$%?_]{8,16}$"
        val isMatch = Pattern.matches(pwPattern, pw)
        return if (isMatch){
            editText.setBackgroundResource(R.drawable.bg_round_square_stroke_gray)
            true
        } else {
            editText.setBackgroundResource(R.drawable.bg_round_square_stroke_red)
            false
        }
    }
}