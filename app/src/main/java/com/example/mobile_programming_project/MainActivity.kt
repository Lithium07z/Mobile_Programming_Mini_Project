package com.example.mobile_programming_project

import android.R.attr.value
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_programming_project.databinding.ActivityMainBinding
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.math.round


class MainActivity : AppCompatActivity() {

    private var arithmeticResult: TextView? = null  // 연산 결과 텍스트 뷰
    private var arithmeticInput: TextView? = null   // 연산 입력 텍스트 뷰

    private var arithmeticResultFlag = false        // 연산 결과 텍스트 뷰 상태 플래그
    private var arithmeticInputFlag = false         // 연산 입력 텍스트 뷰 상태 플래그
    private var increaseFlag: Boolean = false       // 넘버 패드 숫자 증가 플래그
    private var DecreaseFlag: Boolean = false       // 넘버 패드 숫자 감소 플래그

    private var firstNum: Double = 0.0              // 첫번째 피연산자
    private var secondNum: Double = 0.0             // 두번째 피연산자
    private var resultNum: Double = 0.0             // 연산 결과
    private var arithOper: String? = null           // 연산자

    private lateinit var gestureDetector: GestureDetector   // GestureDetector 클래스를 사용해 안드로이드에서 제공하는 제스처 이벤트를 쉽게 처리
    private val buttonList = mutableListOf<Button>()        // 버튼 프리셋 저장, 불러오기, 리셋을 위해 버튼을 담아둘 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)   // 뷰 바인딩
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.navigationBars())
            controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Android 10 이하에서는 deprecated된 방법을 사용합니다.
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

        buttonList.addAll(
            listOf(
                binding.btn0,
                binding.btn1,
                binding.btn2,
                binding.btn3,
                binding.btn4,
                binding.btn5,
                binding.btn6,
                binding.btn7,
                binding.btn8,
                binding.btn9
            )
        )   // 숫저 패드를 버튼 리스트에 추가

        gestureDetector = GestureDetector(this, GestureListener())  // GestureDetector 객체를 생성하여 제스처 이벤트를 처리하기 위한 gestureDetector를 초기화

        binding.btnSaveLoadReset?.setOnTouchListener { _, event ->   // btnResetSaveAndLoad 버튼이 눌리면 호출
            increaseFlag = false    // 넘버 패드 숫자 증가 플래그 비활성화
            increaseFlag = false    // 넘버 패드 숫자 감소 플래그 비활성화
            gestureDetector.onTouchEvent(event) // gestureDetector.onTouchEvent(event)를 호출하여 GestureDetector를 통해 감지된 제스처 이벤트를 처리
            true
        }

        // XML 레이아웃에서 정의한 뷰들을 찾아와서 초기화
        arithmeticResult = binding.arithmeticResult
        arithmeticInput = binding.arithmeticIntput
    }

    fun btnNum(view: View) {                    // 숫자 버튼이 눌렸을 때
        if (!increaseFlag && !DecreaseFlag) {   // 넘버 패드 숫자 증가, 감소 버튼이 모두 눌리지 않은 경우
            if (!arithmeticResultFlag) {        // 연산 결과 텍스트 뷰의 상태가 초기화가 필요한 상태라면 (= 버튼을 눌러서 결과를 얻은 뒤 새로 숫자를 입력하려는 경우 등)
                arithmeticResult?.text = ""     // 빈 문자열로 초기화 해줌
            }

            if ((view as Button).text.toString().toDouble() < 0.0) {
                arithmeticResult?.text =
                    view.text.toString() // 넘버 패드 숫자 감소로 음수가 입력되는 경우 음수로 텍스트를 교체함 (Ex. 10-99는 Double형 변환이 불가능하므로 -99만 출력)
            } else {                                        // 정상적인 양수가 입력된 경우
                if (arithmeticResult?.text!!.length < 15) { // 15자리까지만 입력 받음
                    arithmeticResult?.append(view.text)     // 누른 버튼을 현재 연산 결과 텍스트 뷰의 숫자에 덧붙임
                }
            }

            arithmeticResultFlag = true // 연산 결과 텍스트 뷰의 상태를 초기화가 필요 없는 상태로 변경
            secondNum = arithmeticResult?.text.toString()
                .toDouble() // 연산 결과 텍스트를 문자열로 바꾸고 해당 문자열을 toDouble을 사용해 Double형으로 형변환
        } else if (increaseFlag) {      // 넘버 패드 숫자 증가 플래그가 True라면 (넘버 패드 증가 숫자버튼이 눌렸다면)
            var increaseValue: Double   // 넘버 패드의 값을 얼마나 증가 시킬지 저장하는 변수

            if (arithmeticResult?.text.isNullOrEmpty()) { // 아무 값도 없다면
                increaseValue = 1.0             // 기본적으로 1이 증가함
            } else {                            // 값이 있다면
                increaseValue =
                    arithmeticResult?.text.toString().toDouble()   // 해당 값을 increaseValue에 넣음
            }

            (view as Button).text =
                if ((view.text.toString().toDouble() + increaseValue) % 1 == 0.0) { // 계산 결과가 정수라면
                    (view.text.toString().toDouble() + increaseValue).toLong()
                        .toString()    // 실수에서 정수형으로 변경하여 출력
                } else {                                                            // 계산 결과가 실수라면
                    val decimalFormat = DecimalFormat("#.##")                // 소수점 이하 2자리까지만 표시
                    decimalFormat.format((view.text.toString().toDouble() + increaseValue)).toString()
                    // 실수 형으로 그대로 두되, 소수점 세번째 자리에서 반올림하여 소수점 두번째 자리까지만 증가시킴
                }
        } else if (DecreaseFlag) {      // 넘버 패드 숫자 감소 플래그가 True라면 (넘버 패드 감소 숫자버튼이 눌렸다면)
            var decreaseValue: Double   // 넘버 패드의 값을 얼마나 감소 시킬지 저장하는 변수
            if (arithmeticResult?.text.isNullOrEmpty()) { // 아무 값도 없다면
                decreaseValue = 1.0             // 기본적으로 1이 감소함
            } else {                            // 값이 있다면
                decreaseValue = arithmeticResult?.text.toString().toDouble()    // 해당 값을 decreaseValue에 넣음
            }

            (view as Button).text =
                if ((view.text.toString().toDouble() - decreaseValue) % 1 == 0.0) { // 계산 결과가 정수라면
                    (view.text.toString().toDouble() - decreaseValue).toLong()
                        .toString()    // 실수에서 정수형으로 변경하여 출력
                } else {                                                            // 계산 결과가 실수라면
                    val decimalFormat = DecimalFormat("#.##")                // 소수점 이하 2자리까지만 표시
                    decimalFormat.format((view.text.toString().toDouble() - decreaseValue)).toString()
                    // 실수 형으로 그대로 두되, 소수점 세번째 자리에서 반올림하여 소수점 두번째 자리까지만 감소시킴
                }
        }
    }

    fun btnBack(view: View) { // "⬅" 버튼이 눌렸을 때
        var arithmeticContent = arithmeticResult?.text.toString() // 연산 결과 텍스트의 내용을 받아옴

        if (arithmeticContent.isNotEmpty()) {   // 연산 결과 텍스트가 비어있지 않다면
            arithmeticResult?.text = arithmeticContent.substring(
                0,
                arithmeticContent.length - 1
            )   // 전체 숫자에서 마지막 숫자를 제외한 다음 다시 넣어 줌
        }
    }

    fun btnClear(view: View) {      // "C"버튼이 눌렸을 때 (연산 결과 텍스트 초기화)
        arithmeticResult?.text = "" // 연산 결과 텍스트 초기화
        arithmeticResultFlag = false// 연산 결과 텍스트 뷰의 상태를 초기화가 필요한 상태로 변경
        secondNum = 0.0             // 입력한 두번째 피연산자 초기화
    }

    fun btnAC(view: View) {         // "AC" 버튼이 눌렸을 때 (전체 텍스트 븊 초기화)
        arithmeticResult?.text = "" // 연산 결과 텍스트 초기화
        arithmeticInput?.text = ""  // 연산 입력 텍스트 초기화
        arithmeticResultFlag = false// 연산 결과 텍스트 뷰의 상태를 초기화가 필요한 상태로 변경
        arithmeticInputFlag = false // 연산 입력 텍스트 뷰의 상태를 초기화가 필요한 상태로 변경
        firstNum = 0.0              // 첫번째 피연산자 초기화
        secondNum = 0.0             // 두번째 피연산자 초기화
        arithOper = ""              // 연산자 초기화
    }

    fun btnDot(view: View) {    // "." 버튼이 눌렸을 때
        var dotFlag = false     // 연산식에 "."이 있는지 확인하기 위한 플래그

        arithmeticResult?.text?.let {
            if (it.contains(".")) { // 연산식에 "."이 있다면
                dotFlag = true            // dotFlag를 true로 바꿔줌
            }
        }

        if (!arithmeticResultFlag) {        // 연산 결과 텍스트 뷰의 상태가 초기화가 필요한 상태라면 (Clear 후 연산 결과 텍스트에 아무것도 없는 경우 등)
            arithmeticResult?.text = "0."   // "0."을 기본적으로 제공하고 이후 숫자를 추가하도록 함
            arithmeticResultFlag = true     // 연산 결과 텍스트 뷰의 상태를 초기화가 필요 없는 상태로 변경
        } else if (!dotFlag) {              // dotFlag가 false라면 (연산 결과 텍스트에 이미 값이 입력되어 있다면)
            arithmeticResult?.append(".")   // 연산 결과 텍스트에 "."을 추가
        }
    }

    fun btnOper(view: View) {   // 연산자 버튼이 눌렸을 때
        val btn =
            (view as Button).text // view에는 text속성이 없기때문에 Button형으로 바꿔서 text를 가져옴, btnOper에 연결된 연산자 버튼의 연산자를 가져옴

        if (!arithmeticInputFlag) { // 연산 입력 텍스트 뷰의 상태가 초기화가 필요한 상태라면
            arithmeticInput?.text = // 연산 입력 텍스트를 변경함
                if (secondNum % 1.0 == 0.0) {       // 두번째 피연산자가 정수라면
                    "${secondNum.toLong()} $btn"    // long으로 형변환하여 연산자를 추가 (Ex. 1.0 x > 1 x 로 화면에 출력)
                } else {                            // 두번째 피연산자가 실수라면
                    "$secondNum $btn"               // 그대로 연산자를 추가  (Ex. 1.5 x > 1.5 로 화면에 출력)
                }

            firstNum =
                secondNum            // 연산자가 입력되었으므로 연산 결과 텍스트의 두번째 피연산자를 첫번째 피연산자로 옮김 (연산자가 입력된 다음 값이 들어올테니 첫번째 피연산자로 옮기고 연산 입력 텍스트로 이동해야 함)
            arithmeticInputFlag = true      // 연산 입력 텍스트 뷰의 상태를 초기화가 필요 없는 상태로 변경
            arithmeticResultFlag = false    // 연산 결과 텍스트 뷰의 상태를 초기화가 필요한 상태로 변경
        } else {
            if (!arithmeticResultFlag) {    // 첫번째 피연산자가 이미 있는 상태에서 연산자를 바꾸는 경우 (Ex. 1.5 x > 1.5 +)
                arithmeticInput?.text =     // 연산 입력 텍스트를 변경함
                    if (firstNum % 1.0 == 0.0) {    // 첫번째 피연산자가 정수라면
                        "${firstNum.toLong()} $btn" // long으로 형변환하여 연산자를 추가
                    } else {                        // 첫번째 피연산자가 실수라면
                        "$firstNum $btn"            // 그대로 연산자를 추가
                    }
            } else {    // 첫번째 피연산자 이후 연산자가 입력된 다음 두번째 피연산자까지 모두 입력된 상태에서 연산자를 클릭한 경우 (Ex. 1.5 + 9, 까지 입력 후 연산자(+, -, x, ÷)를 누름)
                resultNum = performOperation(
                    arithOper,
                    firstNum,
                    secondNum
                )   // 연산자에 맞는 연산 후 결과 값을 반환해 resultNum에 넣음

                resultNum = round(resultNum * 1000000000) / 1000000000  // 부동 소수점 오차 보정

                arithmeticInput?.text =             // 연산 입력 텍스트를 변경함
                    if (resultNum % 1.0 == 0.0) {   // 연산 결과가 정수라면
                        "${resultNum.toLong()} $btn"// long으로 형변환하여 연산자를 추가
                    } else {                        // 연산 결과가 실수라면
                        "$resultNum $btn"           // 그대로 연산자를 추가
                    }

                arithmeticResultFlag = false        // 연산 결과 텍스트 뷰의 상태를 초기화가 필요한 상태로 변경
                firstNum = resultNum                // 연산 결과를 첫번째 피연산자에 넣어 연산 입력 텍스트 뷰에 보이도록 함 (Ex. 1.5 + 9, 이후 +를 눌렀다면 연산 입력 텍스트 뷰에 10.5 + 가 보임)
            }
        }
        arithOper = btn.toString()  // 현재 눌린 버튼의 연산자를 ArithOper에 저장
    }

    fun btnEqual(view: View) {      // "=" 버튼이 눌렸을 때 호출되는 함수
        arithmeticInput?.text = ""  // 연산 입력 텍스트를 초기화

        if (!arithOper.isNullOrEmpty()) {               // 아무 값도 없이 "="를 누르는 경우를 방지
            if (arithOper == "-" && secondNum < 0.0) {  // 연산자가 -고 입력된 두번째 피연산자도 음수라면 (EX. 100 - -100 >> 100 + 100)
                arithOper = "+"                         // 연산자를 덧셈 연산자로 교체하고
                secondNum = -secondNum                  // 두번째 피연산자를 양수로 변경
            }
            arithmeticInput?.append(                    // 연산 입력 텍스트를 변경함
                if (firstNum % 1.0 == 0.0) {            // 첫번째 피연산자가 정수라면
                    "${firstNum.toLong()} $arithOper"   // long으로 형변환하여 연산자를 추가
                } else {                                // 첫번째 피연산자가 실수라면
                    "${firstNum} $arithOper"            // 그대로 연산자를 추가
                }                                       // Ex. 1 +
            )

            arithmeticInput?.append(            // 연산 입력 텍스트에 추가로 변경
                if (secondNum % 1.0 == 0.0) {   // 두번째 피연산자가 정수라면
                    " ${secondNum.toLong()} ="  // long으로 형변환하여 "="을 추가
                } else {                        // 두번째 피연산자가 실수라면
                    " ${secondNum} ="           // 그대로 "="을 추가
                }                               // Ex. 1 + 2 =
            )

            resultNum = performOperation(
                arithOper,
                firstNum,
                secondNum
            )    // 연산자에 맞는 연산 후 결과 값을 반환해 resultNum에 넣음

            resultNum = round(resultNum * 1000000000) / 1000000000  // 부동 소수점 오차 보정

            arithmeticResult?.text =            // 연산 결과 텍스트를 변경함
                if (resultNum % 1.0 == 0.0) {   // 연산 결과가 정수라면
                    "${resultNum.toLong()}"     // long으로 형변환함
                } else {                        // 연산 결과가 실수라면
                    "${resultNum}"              // 그대로 둠
                }

            firstNum = resultNum            // 첫번째 피연산자를 연산 결과 값으로 대체하여 사용자에게 보여줌
            arithmeticResultFlag = false    // 연산 결과 출력 후 초기화가 필요한 상태로 변경
        }
    }

    fun btnIncrease(view: View) {       // "⬆" 버튼이 눌렸을 때 호출되는 함수
        increaseFlag = !increaseFlag    // 넘버 패드 숫자 증가 플래그를 반전
        DecreaseFlag = false            // 증가, 감소 버튼이 동시에 활성화 되는 것을 방지하기 위해 감소 버튼을 비활성화로 변경
    }

    fun btnDecrease(view: View) {       // "⬇" 버튼이 눌렸을 때 호출되는 함수
        DecreaseFlag = !DecreaseFlag    // 넘버 패드 숫자 감소 플래그를 반전
        increaseFlag = false            // 증가, 감소 버튼이 동시에 활성화 되는 것을 방지하기 위해 증가 버튼을 비활성화로 변경
    }

    fun btnCall(view: View) {           // "☎" 버튼이 눌렸을 때 호출되는 함수
        // 명시적 인텐트를 만든 뒤 Intent.ACTION_DIAL을 사용해 Intent가 전화 다이얼을 열게 함 이후 Uri.parse로 전화번호를 URI로 변환함
        val callIntent =
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${arithmeticResult?.text.toString()}"))
        startActivity(callIntent)   // 전화 앱 열기
    }

    fun btnCalendar(view: View) {   // "📅" 버튼이 눌렸을 때 호출되는 함수
        val year =
            arithmeticResult!!.text.substring(0, 4).toInt()      // 연산 결과 텍스트 뷰애서 앞의 4자리를 연도로 가져옴
        val month = arithmeticResult!!.text.substring(4, 6)
            .toInt() - 1 // 연산 결과 텍스트 뷰애서 5 ~ 6자리를 월로 가져옴, 캘린더의 달은 0부터 시작함
        val day =
            arithmeticResult!!.text.substring(6, 8).toInt()       // 연산 결과 텍스트 뷰에서 마지막 2자리를 일로 가져옴

        val calendar = Calendar.getInstance()   // Calendar 인스턴스를 생성
        calendar.set(year, month, day)          // 연산 결과 텍스트 뷰에 작성된 숫자를 기반으로 캘린더의 연도, 월, 일을 설정

        val intent = Intent(Intent.ACTION_EDIT) // 캘린더 앱을 열기 위한 Intent 생성
            // 캘린더 이벤트를 처리하기 위한 데이터 URI과 MIME 타입을 지정
            .setDataAndType(
                android.provider.CalendarContract.Events.CONTENT_URI,
                "vnd.android.cursor.item/event"
            )
            .putExtra("beginTime", calendar.timeInMillis)   // 기본적으로 현재 시간부터
            .putExtra("endTime", calendar.timeInMillis + (60 * 60 * 1000)) // 1시간 뒤 까지의 일정으로 세팅
            // (Ex. 캘린더를 실행한 시점이 16:55분이라면, 캘린더가 켜지고 일정의 시간 부분이 16:55 ~ 17:55로 세팅 됨)
            // 밀리세컨이므로 1000을 곱해서 1초, 60을 곱해서 1분, 다시 60을 곱해서 1시간이 됨

        startActivity(intent)   // 캘린더 앱 열기
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {   // GestureDetector.SimpleOnGestureListener을 상속받은 GestureListener inner 클래스
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {    // 싱글 탭(일반 클릭)이 감지되면 호출 (Save)
            val sharedPreference = getSharedPreferences(
                "NumberPadSave",
                MODE_PRIVATE
            )   // SharedPreferences를 사용하여 "NumberPadSave"에 데이터를 저장
            val editor: SharedPreferences.Editor =
                sharedPreference.edit() // SharedPreferences를 수정하기 위해 sharedPreference.edit()를 설정

            for (i in 0..9) {   // 버튼 리스트의 각 버튼 텍스트를 SharedPreferences에 저장
                editor.putString(i.toString(), buttonList.get(i).text.toString())
            }

            editor.apply() // apply를 사용하여 비동기적으로 변경사항을 즉시 반영

            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean { // 더블 탭(더블 클릭)이 감지되면 호출 (Load)
            val sharedPreference = getSharedPreferences(
                "NumberPadSave",
                MODE_PRIVATE
            )   // SharedPreferences에서 "NumberPadSave" 이름으로 저장된 데이터를 가져옴

            for (i in 0..9) {   // 저장된 버튼 프리셋으로 현재 버튼 리스트의 텍스트를 업데이트
                buttonList.get(i).text = sharedPreference.getString(i.toString(), "저장된 버튼 프리셋 없음")
            }
            
            return true
        }

        override fun onLongPress(e: MotionEvent) {  // 롱 프레스(길게 클릭)가 감지되면 호출 (Reset)
            for (i in 0..9) {   // 버튼 리스트의 각 버튼 텍스트를 해당 버튼의 인덱스로 초기화
                buttonList.get(i).text = i.toString()
            }
        }
    }

    private fun performOperation(
        operator: String?,
        firstNumber: Double,
        secondNumber: Double
    ): Double {                     // 연산을 수행하는 함수
        return when (operator) {    // 연산자에 따라 그에 맞는 연산 후 결과 값을 반환함
            "+" -> firstNumber + secondNumber   // 연산자가 +인 경우 덧셈을
            "-" -> firstNumber - secondNumber   // 연산자가 -인 경우 뺄셈을
            "x" -> firstNumber * secondNumber   // 연산자가 x인 경우 곱셈을
            else -> firstNumber / secondNumber  // 연산자가 /인 경우 나눗셈의 몫을
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {    // 화면 전환 시 현재 상태 저장
        super.onSaveInstanceState(outState)
        for (i in 0..9) {   // 넘버패드 저장
            outState.putString(i.toString(), buttonList.get(i).text.toString())
        }

        outState.putString("arithmeticInput", arithmeticInput!!.text.toString())    // 연산 입력 텍스트 뷰 저장
        outState.putString("arithmeticResult", arithmeticResult!!.text.toString())  // 연산 결과 텍스트 뷰 저장
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {   // 화면 전환 시 저장된 상태 불러오기
        super.onRestoreInstanceState(savedInstanceState)
        for (i in 0..9) {   // 넘버패드 불러오기
            buttonList.get(i).text = savedInstanceState.getString(i.toString())
        }

        arithmeticInput!!.text = savedInstanceState.getString("arithmeticInput")    // 연산 입력 텍스트 뷰 불러오기
        arithmeticResult!!.text = savedInstanceState.getString("arithmeticResult")  // 연산 결과 텍스트 뷰 불러오기
    }
}