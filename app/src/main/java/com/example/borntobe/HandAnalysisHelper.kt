package com.example.borntobe

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.sqrt

class HandAnalysisHelper(
    handLandmarkerResult: HandLandmarkerResult,
    handStandardResult: HandLandmarkerResult
) {
    // ***** 규격 길이 정의 : 웨이브 *****
    // 1. 손 직선 길이 : 적당한 편
    private val standardHandStraight = calDistance(handStandardResult, 0, 12)
    // 2. 손 너비 : 적당한 편
    private val standardHandWidth = calDistance(handStandardResult, 5, 17)
    // 3. 가운데 손가락 길이 : 적당한 편
    private val standardMiddleFingerLength = calDistance(handStandardResult, 9, 12)

    // ***** 사용자 손 분류 변수 *****
    // 1. 손 직선 길이
    private val handStraightLine = calDistance(handLandmarkerResult, 0, 12)
    // 2. 손 너비
    private val handWidth = calDistance(handLandmarkerResult, 5, 17)
    // 3. 가운데 손가락 길이
    private val middleFingerLength = calDistance(handLandmarkerResult, 9, 12)

    // ***** 유형 분류 변수 *****
    private lateinit var handSize: String
    private lateinit var handSizeWidth: String
    private lateinit var middleFingerSize: String
    private lateinit var bodyShape: String

    // calDistance() : 두 랜드마크 사이의 거리 계산 메소드
    private fun calDistance(
        result: HandLandmarkerResult,
        firstLandmarkIndex: Int,
        secondLandmarkIndex: Int
    ): Float {
        val landmarks = result.landmarks()
        // 1. 첫 번째 Landmark
        val firstLandmark = landmarks[0][firstLandmarkIndex]
        val firstLandmarkX = firstLandmark.x()
        val firstLandmarkY = firstLandmark.y()
        // 2. 두 번째 Landmark
        val secondLandmark = landmarks[0][secondLandmarkIndex]
        val secondLandmarkX = secondLandmark.x()
        val secondLandmarkY = secondLandmark.y()
        // 3. 두 좌표 간 거리 계산
        val xDistance = (firstLandmarkX - secondLandmarkX) * (firstLandmarkX - secondLandmarkX)
        val yDistance = (firstLandmarkY - secondLandmarkY) * (firstLandmarkY - secondLandmarkY)

        return sqrt(xDistance + yDistance)
    }

    fun classifier() {
        // 손 직선 길이 분류
        handSize = if (standardHandStraight - 5 > handStraightLine)
            "small"
        else if (standardHandStraight + 5 < handStraightLine)
            "big"
        else
            "middle"
        // 손 너비 분류
        handSizeWidth = if (standardHandWidth - 5 > handWidth)
            "small"
        else if (standardHandWidth + 5 < handWidth)
            "big"
        else
            "middle"
        // 가운데 손가락 길이 분류
        middleFingerSize = if (standardMiddleFingerLength - 5 > middleFingerLength)
            "short"
        else if (standardMiddleFingerLength + 5 < middleFingerLength)
            "long"
        else
            "middle"
    }

    fun analysisBodyShape(): String {
        bodyShape = if (handSize == "small" && handSizeWidth == "small" && middleFingerSize == "short")
            "Straight"
        else if (handSize == "big" && handSizeWidth == "big" && middleFingerSize == "long")
            "Natural"
        else
            "Wave"

        return bodyShape
    }
}