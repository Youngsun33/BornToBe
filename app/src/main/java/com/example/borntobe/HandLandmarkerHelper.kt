package com.example.borntobe

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

// HandLandmarkerHelper.kt : Initializes the hand landmark detector and handles the model and delegate selection.
// HandLandmarkerHelper.kt : hand landmark 감지기를 초기화 & 모델과 선택자 처리
class HandLandmarkerHelper(
    // minHandDetectionConfidence : 손 탐지가 성공적으로 수행되기 위해 필요한 최소 신뢰 점수
    // minHandTrackingConfidence : 손 추적이 성공적으로 수행되기 위해 필요한 최소 신뢰 점수
    // maxNumHands : Hand landmark 감지기가 감지할 수 있는 최대 손의 개수
    // runningMode : 단일 이미지용으로 설정
    private var minHandDetectionConfidence: Float = DEFAULT_HAND_DETECTION_CONFIDENCE,
    private var minHandTrackingConfidence: Float = DEFAULT_HAND_TRACKING_CONFIDENCE,
    private var minHandPresenceConfidence: Float = DEFAULT_HAND_PRESENCE_CONFIDENCE,
    private var maxNumHands: Int = DEFAULT_NUM_HANDS,
    private var currentDelegate: Int = DELEGATE_CPU,
    private var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context
) {
    companion object {
        private const val MP_HAND_LANDMARKER_TASK = "hand_landmarker.task"
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_HANDS = 1
    }

    private var handLandmarker: HandLandmarker? = null
    init {
        setupHandLandmarker()
    }

    fun clearHandLandmarker() {
        handLandmarker?.close()
    }

    /** Initialize the Hand landmarker using current settings on the thread that is using it.
     *  CPU can be used with Landmarker that are created on the main thread and used on a background thread,
     *  but the GPU delegate needs to be used on the thread that initialized the Landmarker **/
    private fun setupHandLandmarker() {
        // Set general hand landmarker options
        val baseOptionBuilder = BaseOptions.builder()

        // Use the specified hardware for running the model. Default to CPU
        when (currentDelegate) {
            DELEGATE_CPU -> {
                baseOptionBuilder.setDelegate(Delegate.CPU)
            }

            DELEGATE_GPU -> {
                baseOptionBuilder.setDelegate(Delegate.GPU)
            }
        }
        // 1. Create the task
        baseOptionBuilder.setModelAssetPath(MP_HAND_LANDMARKER_TASK)
        val baseOptions = baseOptionBuilder.build()
        // 2. HandLandMarker 사용 위한 Options 설정
        val optionsBuilder =
            HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(minHandDetectionConfidence)
                .setMinTrackingConfidence(minHandTrackingConfidence)
                .setMinHandPresenceConfidence(minHandPresenceConfidence)
                .setNumHands(maxNumHands)
                .setRunningMode(runningMode)
        val options = optionsBuilder.build()
        // 3. createFromOptions() : 주어진 옵션들로 구성 옵션 적용
        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    // Accepted a Bitmap and runs hand landmarker inference on it to return
    // results back to the caller
    fun detectImage(image: Bitmap): ResultBundle? {
        if (runningMode != RunningMode.IMAGE) {
            throw IllegalArgumentException(
                "Attempting to call detectImage" + " while not using RunningMode.IMAGE"
            )
        }

        // Inference time is the difference between the system time at the
        // start and finish of the process
        val startTime = SystemClock.uptimeMillis()

        // Convert the input Bitmap object to an MPImage object to run inference
        // 4. 입력 값으로 받은 Bitmap 을 MPImage 로 변환
        val mpImage = BitmapImageBuilder(image).build()

        // Run hand landmarker using MediaPipe Hand Landmarker API
        handLandmarker?.detect(mpImage)?.also { landmarkResult ->
            val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
            return ResultBundle(
                listOf(landmarkResult),
                inferenceTimeMs,
                image.height,
                image.width
            )
        }

        return null
    }

    data class ResultBundle(
        val results: List<HandLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )
}
