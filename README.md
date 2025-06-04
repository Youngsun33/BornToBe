# BornToBe
## 💻프로젝트 소개
![졸업전시회  발표 영상_썸네일_코더_크기 변경 전](https://github.com/user-attachments/assets/e0afb279-59ce-475a-8d51-70da73b2d97a)
BornToBe는 사용자가 제공한 사진을 통해 얼굴형 및 체형 분석을 수행하고, 맞춤 스타일링을 제공하는 안드로이드 어플리케이션입니다.

## 📆개발 기간
#### 2024.03.06(수) ~ 2024.12.20(금)

## 👩‍💻팀원 소개
### Team '코더'
+ <a href="https://github.com/seeize">박시윤</a> : 결과 화면 담당 (분석 결과 제공)
+ <a href="https://github.com/eunjinpark385">박은진</a> : 로그인/회원가입 화면, VGG-16 기반 전이학습 얼굴형 분류 모델 개발, 손 분석 화면, 얼굴형 분석 화면 중 이미지 전처리 기능 담당
+ <a href="https://github.com/heoyoungsun33">허영선</a> : UI 디자인, 얼굴형 분석 화면 (카메라 권한 및 사용자로부터 전달 받은 이미지 저장)

## ⚙️ 개발 환경
+ **JDK** : `JDK 1.8.0`
+ **IDE** : `Android Studio Iguana (2023.2.1)`
+ **DB** : `Firestore`
+ **OS** : `Windows 11`

## 📌 주요 기능
#### 로그인
- 사용자 실시간 입력값 검사
- 자동 로그인
- DB 검증

#### 회원가입
- 사용자 실시간 입력값 검사
- 사용자 입력값 유효성 검사
- 아이디 중복 검사
- DB에 신규 사용자 생성

#### 얼굴형 분석
- VGG-16 기반 전이학습으로 얼굴형 분류 모델 개발
- 개발한 모델은 tflite로 변환하여 어플리케이션에 탑재
- 사용자 제공 사진으로 얼굴형 판단
- 사용자 제공 사진 전처리
- 4개의 얼굴형 (타원형, 계란형, 둥근형, 사각형) 중 하나로 분류
- 모델 정확도는 모든 클래스에서 약 90%를 보임
- 모델은 https://github.com/eunjinpark385/face_analysis_model 참조

#### 체형 분석
- '손'을 통한 채형 분석 수행
- MediaPipe Library의 hand_landmark_detection API 활용

## 🎬어플리케이션 시연 영상
https://github.com/user-attachments/assets/98a05c7f-fa95-46f9-988c-61ea47cbb9c2



## 📚 참고 문헌
- <a href="https://www.dbpia.co.kr/journal/articleDetail?nodeId=NODE11788126">체형 분석 참고 논문 : 박선미, 정은영. (2023). 골격진단을 위한 진단요소의 수치화 연구 (손을 중심으로). 국제보건미용학회지, 17(3), 166-176.</a>
- <a href = "https://ai.google.dev/edge/mediapipe/solutions/vision/hand_landmarker/android?hl=ko&_gl=1*1ub3r6f*_up*MQ..*_ga*MjA0MjIzMDQ2Ny4xNzE3OTI3NTg1*_ga_P1DBVKWT6V*MTcxNzkyNzU4NS4xLjAuMTcxNzkyNzU4NS4wLjAuMTM3MTM1MjMwNw.."> MediaPipe Hand Landmarker 공식 가이드 </a>
