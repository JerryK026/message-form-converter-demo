# message-form-converter-demo

이 프로젝트는 예전에 수행했었던 인턴 과제를 다시 해보는 프로젝트입니다. 가상의 환경을 구성하여 대규모 트래픽 환경에 대해 실습해 보는 것이 목표입니다.

### 학습 목표
과제 목표인 분당 100만 트래픽을 허용하기 위한 어떤 시도를 해볼 수 있나 여러 시도
1. 처리량을 늘리기 위해 해볼 수 있는 것들
   - CPU / IO bound 중 어떤 것이고 해결하기 위해 맞는 처리 방법 (scale out, ...)
   - 메모리 누수가 발생하진 않는지
   - 패킷 수가 너무 많아지면 어떤 처리를 할 수 있는지
2. 메시지 큐 종류별로 이 앱에 어떤 도움이 될 수 있는지(redis, RabbitMq, Kafka) 실습
   - MQ별 처리량 비교
   - MQ별 특성 비교
     - HA 방법
     - 토픽 처리 방법
     - 죽었을 때 MSG 처리 방법
3. Kotlin 숙달

### 조건
1. 트래픽은 분당 100만을 목표로 한다
2. Inner Server는 0 ~ 0.5초간 random sleep 후 response한다
2. Inner Server는 요청 받으면 요청 정보를 db에 저장한다. 나중에 count해서 msg 유실률 / 처리율을 확인하기 위한 용도
3. TTL은 각각 5초로 지정한다. Inner Server 측에서 5초 안에 처리하지 못한 건 처리하지 못한 걸로 간주한다

- TTL, random sleep은 무작위로 정한 것이기 때문에 수정될 수 있다
- 트래픽은 당시 지시받았던 것이나, 현실적인 조건에 따라 조정되어야 할 필요 있음

### 시나리오
1. Client는 다음과 같은 꼴의 JSON 요청을 보낸다
2. Server는 받은 요청을 전문 형태로 전환해 Inner Server로 전송한다
3. Inner Server는 잠시 Sleep한 후 처리한 내용을 저장한다

### 요청

**Client -> Server**
```json (utf-8)
{
  "id": "a1b2c3d4e5f6",
  "clientCode": "SOKO",
  "requestTypeId": "AB001",
  "memberId": 1234567890,
  "requestedAt": "2023-08-17 17:56:35"
}
```

- id : 메시지의 unique한 값 (12자리 문자열)
- clientCode : client의 정보 (4자리 문자열)
- requestTypeId : 요청의 비즈니스 정보 (5자리 문자열)
- memberId : 요청을 보낸 client의 id (10자리 숫자)
- requestedAt : client에서 요청을 보낸 시간 (19자리 문자열)

**Server -> InnerServer**
```fulltext (euc-kr)
a1b2c3d4e5f6SOKOAB001DATA1DATA2DATA3DATA4DATA512345678902023-08-17 17:56:35
```

Server에서 requestTypeId를 활용해 비즈니스 정책에 대해 조회하고 그 양식으로 전문을 만들어 InnerServer에 전송한다
(requestTypeId와 memberId사이에 추가되었다)

### 환경 구성
**DB 정보**
request_type 테이블 스키마 => 약 1천개 data

| 필드명   |  타입  |  비고 |
|------|-------|------|
|  id  |  INT  |  PK  |

data 테이블 스키마 => 약 5천개 data

| 필드명   |  타입  |  비고 |
|------|-------|------|
|  id  |  INT  |  PK  |
|  request_type_id  |  INT  |  FK  |
|  content  |  VARCHAR(100)  |  NOT NULL  |

log 테이블 스키마

| 필드명         | 타입         | 비고 |
|-------------|------------|----|
| id          | INT        | PK |
| request_id  | INT        |    |
| client_code | VARCHAR(4) |    |
| member_id   | INT        |    |
| request_type_id | INT | FK |
| created_at  | DATETIME   |    |


### 해야 할 일들
- [ ] 애플리케이션 환경 구성
- [ ] 애플리케이션 구현
- [ ] 애플리케이션 테스트 데이터 생성
- [ ] 종합 환경 구성
- [ ] 부하 테스트 & 피드백