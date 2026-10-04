# 여기어때 잘난체 (Yeogi Eottae Jalnan)

- 배포처: 여기어때(GoodChoice) — https://www.goodchoice.kr/font/mobile
- 소개 페이지(눈누): https://noonnu.cc/en/font_page/115
- 실제 받은 파일: 눈누 공식 웹폰트 CDN(jsDelivr, GitHub `projectnoonnu/noonfonts_four` 저장소)에 올라간
  WOFF(`JalnanOTF00.woff`)를 받아, Android가 지원하지 않는 WOFF 포맷이라 `fonttools`로
  `flavor` 래퍼만 벗겨 원본 OTF(SFNT/CFF)로 되돌렸다(글자 외곽선 데이터 자체는 그대로,
  가로/세로 폭·좌표 등 폰트 내용 변경 없음). 결과물: `app/src/main/res/font/yeogi_jalnan.otf`.
- 라이선스 요지(눈누 폰트 페이지 기준): 개인·기업 모두 상업적 이용 무료. 폰트 파일 자체를
  재배포·판매하는 것은 금지 — 이 폰트를 별도로 재배포하지 말고, 콕데이 앱 안에서 텍스트를
  그리는 용도로만 사용한다.
