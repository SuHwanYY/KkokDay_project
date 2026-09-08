const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const logger = require("firebase-functions/logger");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore } = require("firebase-admin/firestore");
const { getStorage } = require("firebase-admin/storage");

initializeApp();
const auth = getAuth();
const db = getFirestore();
const storage = getStorage();

const MAX_RECENT_PLACES = 30;

// 서울 리전(asia-northeast3)으로 통일 — Firestore/Hosting과 같은 리전.
setGlobalOptions({ region: "asia-northeast3" });

/**
 * 카카오 로그인 → Firebase 로그인 연결 다리.
 *
 * 앱은 카카오 SDK로 로그인해서 얻은 "카카오 액세스 토큰"을 그대로 신뢰하면 안 된다
 * (클라이언트가 보낸 값은 위조될 수 있음). 그래서 이 함수가 그 토큰을 카카오 서버에
 * 직접 물어봐서("이 토큰 진짜 맞아?") 검증하고, 검증된 카카오 사용자 ID를 바탕으로
 * Firebase 커스텀 토큰을 발급해준다. 앱은 이 커스텀 토큰으로 signInWithCustomToken을
 * 호출해 비로소 Firebase Auth 세션(request.auth)을 갖게 된다.
 *
 * uid 규칙: "kakao:{카카오 사용자 ID}" — 이메일 가입 사용자의 Firebase 기본 uid와
 * 겹칠 일이 없도록 접두사를 붙인다. (계획서 4-1절: 이메일 계정과 카카오 계정은
 * 이번 버전에서 자동 연동하지 않고 별개로 취급 — 그 결정과 일치하는 형태)
 */
exports.verifyKakaoAndMintFirebaseToken = onCall(async (request) => {
  const kakaoAccessToken = request.data?.kakaoAccessToken;

  if (typeof kakaoAccessToken !== "string" || kakaoAccessToken.trim().length === 0) {
    throw new HttpsError("invalid-argument", "kakaoAccessToken이 필요합니다.");
  }

  let kakaoUserId;
  try {
    const response = await fetch("https://kapi.kakao.com/v2/user/me", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${kakaoAccessToken}`,
        "Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
      },
    });

    if (!response.ok) {
      logger.warn("카카오 토큰 검증 실패", { status: response.status });
      throw new HttpsError(
        "unauthenticated",
        "카카오 액세스 토큰이 유효하지 않습니다.",
      );
    }

    const body = await response.json();
    kakaoUserId = body?.id;

    if (kakaoUserId === undefined || kakaoUserId === null) {
      throw new HttpsError("internal", "카카오 응답에서 사용자 ID를 찾을 수 없습니다.");
    }
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    logger.error("카카오 사용자 조회 중 오류", error);
    throw new HttpsError("internal", "카카오 사용자 정보를 확인하는 중 오류가 발생했습니다.");
  }

  const uid = `kakao:${kakaoUserId}`;

  try {
    const customToken = await auth.createCustomToken(uid, {
      provider: "kakao",
    });
    return { customToken };
  } catch (error) {
    logger.error("Firebase 커스텀 토큰 발급 실패", error);
    throw new HttpsError("internal", "로그인 토큰 발급 중 오류가 발생했습니다.");
  }
});

/**
 * users/{uid}/recentPlaces에 문서가 생성/갱신될 때마다 개수를 세서 30개를 넘으면
 * selectedAt이 가장 오래된 것부터 초과분을 지운다.
 *
 * 삭제 이벤트에서도 이 함수가 다시 트리거되지만, 그땐 이미 30개 이하라 아래에서
 * 바로 return돼 끝난다 — 무한 루프가 아니다.
 */
exports.cleanupRecentPlaces = onDocumentWritten(
  "users/{uid}/recentPlaces/{placeId}",
  async (event) => {
    if (!event.data?.after?.exists) return;

    const { uid } = event.params;
    const collectionRef = db.collection("users").doc(uid).collection("recentPlaces");

    const snapshot = await collectionRef.orderBy("selectedAt", "asc").get();
    const excess = snapshot.size - MAX_RECENT_PLACES;
    if (excess <= 0) return;

    const batch = db.batch();
    snapshot.docs.slice(0, excess).forEach((doc) => batch.delete(doc.ref));
    await batch.commit();

    logger.info(`recentPlaces 정리: uid=${uid}, ${excess}개 삭제`);
  },
);

/**
 * places/{placeDocId}/reviews/{uid}에 생성/수정/삭제가 있을 때마다 그 장소의 평균 평점과
 * 리뷰 개수를 다시 계산해 places/{placeDocId} 집계 문서에 반영한다. 클라이언트는 이 필드를
 * 절대 직접 쓰지 않는다(firestore.rules에서 places 문서 write를 전부 막음) — 카테고리 목록을
 * 로드할 때 리뷰 서브컬렉션 전체를 읽지 않고 이 집계 문서 하나만 봐도 avgRating/reviewCount를
 * 얻을 수 있게 하기 위해서다.
 *
 * 리뷰가 하나도 안 남으면 집계 문서 자체를 지운다 — 리뷰 없는 장소는 places 문서가 아예
 * 없던 상태로 되돌아간다(목록의 평점 배지도 자연히 사라진다).
 */
exports.recalculatePlaceRating = onDocumentWritten(
  "places/{placeDocId}/reviews/{uid}",
  async (event) => {
    const { placeDocId } = event.params;
    const placeRef = db.collection("places").doc(placeDocId);
    const snapshot = await placeRef.collection("reviews").get();

    if (snapshot.empty) {
      await placeRef.delete().catch(() => {});
      logger.info(`평점 집계 삭제: place=${placeDocId} (리뷰 0개)`);
      return;
    }

    let sum = 0;
    snapshot.forEach((doc) => {
      sum += doc.data().rating ?? 0;
    });
    const reviewCount = snapshot.size;
    const avgRating = Math.round((sum / reviewCount) * 10) / 10;

    await placeRef.set({ avgRating, reviewCount }, { merge: true });
    logger.info(`평점 재계산: place=${placeDocId} avg=${avgRating} count=${reviewCount}`);
  },
);

/**
 * 회원탈퇴. request.auth 기준 본인 계정만 지울 수 있다(uid를 파라미터로 받지 않는다).
 * Firestore/Storage 데이터를 최대한 정리한 뒤 마지막에 Firebase Auth 계정 자체를
 * 지운다 — 신원(Auth) 삭제는 되돌릴 수 없으니 항상 맨 마지막 단계로 둔다.
 *
 * 앞 단계(리뷰/코스/서브컬렉션/닉네임/프로필 사진)는 하나가 실패해도 나머지 정리를
 * 계속 시도한다(회원탈퇴 자체가 막히는 것보다 일부 잔여 데이터가 남는 게 낫다) —
 * 다만 마지막 Auth 삭제가 실패하면 클라이언트에 명확히 실패로 알려 재시도를 유도한다.
 */
exports.deleteAccount = onCall(async (request) => {
  const uid = request.auth?.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "로그인이 필요합니다.");
  }

  // 1~2. 본인이 작성한 리뷰 전체 삭제 + 그 리뷰들이 쓰던 Storage 사진 삭제.
  // recalculatePlaceRating 트리거가 각 리뷰 삭제마다 자동으로 평점을 재계산하므로
  // 집계(places/{placeDocId}) 쪽은 별도로 손댈 필요가 없다.
  try {
    const reviewsSnapshot = await db.collectionGroup("reviews").where("authorUid", "==", uid).get();
    const placeDocIds = new Set(reviewsSnapshot.docs.map((doc) => doc.ref.parent.parent.id));

    await Promise.all(reviewsSnapshot.docs.map((doc) => doc.ref.delete()));
    await Promise.all(
      Array.from(placeDocIds).map((placeDocId) =>
        storage
          .bucket()
          .deleteFiles({ prefix: `reviews/${placeDocId}/${uid}/` })
          .catch((error) => {
            logger.warn(`리뷰 사진 삭제 실패(무시): placeDocId=${placeDocId} uid=${uid}`, error);
          }),
      ),
    );
    logger.info(`계정 삭제: 리뷰 ${reviewsSnapshot.size}개 삭제 uid=${uid}`);
  } catch (error) {
    logger.warn(`리뷰/리뷰 사진 삭제 중 오류(계속 진행): uid=${uid}`, error);
  }

  // 3. 내가 만든 코스 전체 삭제 — 코스마다 딸린 votes 서브컬렉션도 같이 지운다.
  try {
    const coursesSnapshot = await db.collection("courses").where("ownerId", "==", uid).get();
    await Promise.all(
      coursesSnapshot.docs.map(async (courseDoc) => {
        const votesSnapshot = await courseDoc.ref.collection("votes").get();
        await Promise.all(votesSnapshot.docs.map((voteDoc) => voteDoc.ref.delete()));
        await courseDoc.ref.delete();
      }),
    );
    logger.info(`계정 삭제: 코스 ${coursesSnapshot.size}개 삭제 uid=${uid}`);
  } catch (error) {
    logger.warn(`코스 삭제 중 오류(계속 진행): uid=${uid}`, error);
  }

  // 4. users/{uid}의 recentPlaces/favorites 서브컬렉션 전체 삭제.
  try {
    for (const subcollection of ["recentPlaces", "favorites"]) {
      const snapshot = await db.collection("users").doc(uid).collection(subcollection).get();
      await Promise.all(snapshot.docs.map((doc) => doc.ref.delete()));
    }
  } catch (error) {
    logger.warn(`recentPlaces/favorites 삭제 중 오류(계속 진행): uid=${uid}`, error);
  }

  // 5. 이 uid가 예약해둔 닉네임 반납 — 다른 사용자가 다시 쓸 수 있게 한다.
  try {
    const nicknamesSnapshot = await db.collection("nicknames").where("uid", "==", uid).get();
    await Promise.all(nicknamesSnapshot.docs.map((doc) => doc.ref.delete()));
  } catch (error) {
    logger.warn(`닉네임 반납 중 오류(계속 진행): uid=${uid}`, error);
  }

  // 6. 프로필 사진 삭제.
  try {
    await storage.bucket().deleteFiles({ prefix: `profileImages/${uid}/` });
  } catch (error) {
    logger.warn(`프로필 사진 삭제 실패(무시): uid=${uid}`, error);
  }

  // 7. users/{uid} 문서 자체 삭제.
  try {
    await db.collection("users").doc(uid).delete();
  } catch (error) {
    logger.warn(`users 문서 삭제 중 오류(계속 진행): uid=${uid}`, error);
  }

  // 8. Firebase Auth 계정 삭제 — 여기서부터는 실패하면 그대로 클라이언트에 알린다.
  try {
    await auth.deleteUser(uid);
  } catch (error) {
    logger.error(`Firebase Auth 계정 삭제 실패: uid=${uid}`, error);
    throw new HttpsError("internal", "계정 삭제 중 오류가 발생했습니다. 다시 시도해주세요.");
  }

  logger.info(`계정 삭제 완료: uid=${uid}`);
  return { success: true };
});
