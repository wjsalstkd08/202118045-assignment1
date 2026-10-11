202118045 전민상
웹서비스설계 실습1
==============================

아이템(이름, 가격)을 등록, 조회, 수정, 삭제하는 REST API 서버입니다.
과제용 API는 v3(/api/v3/items)입니다.


[api] 요청을 받아 처리하는 코드
------------------------------

v3/ItemController3.java
  - 과제 요구사항을 적용한 최종 API.
  - 모든 응답을 같은 형식으로 보내고, 잘못된 입력은 400으로 거절한다.
  - POST 2개, GET 3개, PUT 2개, DELETE 2개.
      POST   /api/v3/items                아이템 등록
      POST   /api/v3/items/with-header    아이템 등록 + 받은 헤더 값 반환
      GET    /api/v3/items                전체 조회
      GET    /api/v3/items/{id}           단건 조회
      GET    /api/v3/items/search         이름 검색
      PUT    /api/v3/items/{id}           보낸 값만 수정
      PUT    /api/v3/items/{id}/price     가격만 수정
      DELETE /api/v3/items/{id}           단건 삭제
      DELETE /api/v3/items?confirm=true   전체 삭제 (실수 방지를 위해 confirm 필요)

dto/ItemDto.java
  - 응답으로 돌려주는 아이템 정보(id, name, price).

request/ItemCreateRequest.java, ItemUpdateRequest.java, ItemPriceUpdateRequest.java
  - 요청으로 받는 값의 형식과 규칙(이름 필수, 가격 0 이상 등)을 정한다.
  - 규칙을 어기면 400 응답이 나간다.


[common] 응답 형식과 오류 처리
------------------------------
ApiResponse.java
  - 모든 응답을 같은 모양으로 만들기 위한 클래스.
      성공: { "status": "success", "data": ... }
      실패: { "status": "error", "code": ..., "message": ... }

ErrorCode.java
  - 오류 종류마다 상태 코드(400, 404, 500 등)와 메시지를 한곳에 모아 둔 목록.

ApiException.java
  - 요청을 처리하다 실패했을 때 던지는 예외. (예: 없는 아이템 → 404)

GlobalExceptionHandler.java
  - 서버에서 발생한 모든 예외를 받아 위의 실패 형식으로 바꿔 응답한다.
  - 예상하지 못한 오류는 내부 내용을 숨기고 500으로 응답한다.


[config] 미들웨어(인터셉터)
------------------------------
LoggingInterceptor.java
  - 요청이 들어오고 끝날 때마다 콘솔에 로그를 남긴다.
  - 요청 ID, 상태 코드, 처리 시간(ms)을 출력한다.

MaintenanceInterceptor.java
  - 점검 모드가 켜져 있으면 요청을 컨트롤러로 보내지 않고 503을 응답한다.

WebConfig.java
  - 위 두 인터셉터를 등록한다. (로그 → 점검 모드 검사 순서로 실행)


[admin] 5xx 응답 확인용 코드
------------------------------
이 서버는 데이터를 메모리에만 저장해서 평소에는 서버 오류(5xx)가 나지 않는다.
과제의 "5xx 응답 코드 2개 이상" 조건을 확인하기 위해 일부러 오류를 만드는 API를 따로 두었다.

AdminController.java
  - GET /admin/error-test : 일부러 예외를 발생시켜 500 응답을 확인한다.
  - PUT /admin/maintenance : {"enabled": true}로 점검 모드를 켜면 /api 요청이 503이 된다.
                             {"enabled": false}로 다시 끈다.

MaintenanceState.java
  - 점검 모드가 켜졌는지 꺼졌는지 기억한다.

MaintenanceRequest.java
  - 점검 모드 켜기/끄기 요청의 형식.