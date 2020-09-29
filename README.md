# 키친포스

## 요구 사항

## 용어 사전

| 한글명 | 영문명 | 설명 |
| --- | --- | --- |
| 상품 | product | 메뉴를 관리하는 기준이 되는 데이터 |
| 메뉴 그룹 | menu group | 메뉴 묶음, 분류 |
| 메뉴 | menu | 메뉴 그룹에 속하는 실제 주문 가능 단위 |
| 메뉴 상품 | menu product | 메뉴에 속하는 수량이 있는 상품 |
| 금액 | amount | 가격 * 수량 |
| 주문 테이블 | order table | 매장에서 주문이 발생하는 영역 |
| 빈 테이블 | empty table | 주문을 등록할 수 없는 주문 테이블 |
| 주문 | order | 매장에서 발생하는 주문 |
| 주문 상태 | order status | 주문은 조리 ➜ 식사 ➜ 계산 완료 순서로 진행된다. |
| 방문한 손님 수 | number of guests | 필수 사항은 아니며 주문은 0명으로 등록할 수 있다. |
| 단체 지정 | table group | 통합 계산을 위해 개별 주문 테이블을 그룹화하는 기능 |
| 주문 항목 | order line item | 주문에 속하는 수량이 있는 메뉴 |
| 매장 식사 | eat in | 포장하지 않고 매장에서 식사하는 것 |

## 1단계 요구사항

#### 메뉴 그룹 관리

1. 메뉴 그룹 생성
    
    - 메뉴 그룹명(name)으로 생성
    
2. 메뉴 그룹들 조회

    - 생성된 메뉴 그룹들 조회

#### 메뉴 관리

1. 메뉴 생성

    - 메뉴명(name), 가격(price), 메뉴 그룹 ID(menuGroupId), 메뉴상품들(List<menuProducts>)로 생성
    
        - 가격은 0보다 큰 수, 메뉴 상품들을 다 더한 값보다 클 수 없음
        
        - 메뉴 그룹 ID는 이미 등록된 메뉴 그룹 ID
        
        - 메뉴 상품들 - 상품 ID(productId), 수량(quantity)
        
            - 상품 ID는 이미 등록된 상품 ID
    
2. 메뉴들 조회

    - 생성된 메뉴들 조회

#### 주문 관리

1. 주문 등록

    - 테이블 ID(orderTableId), 주문 항목(orderLineItems)으로 생성
    
        - 테이블 ID는 필수 값, 이미 등록된 테이블 ID
    
        - 주문 항목 - 메뉴 ID(menuID), 수량(quantity)
        
            - 메뉴 ID는 이미 등록된 메뉴 ID

2. 주문들 조회

    - 등록된 주문들 조회

3. 주문 상태 변경

    - 주문 ID(orderId)와 변경할 주문 상태(orderStatus)를 받음
    
    - 기존 주문(order) 상태가 계산 완료인 경우 변경 불가

#### 상품 관리

1. 상품 생성

    - 상품 이름(name), 상품 가격(price)으로 생성
    
    - 상품 가격은 0보다 커야 함

2. 상품들 조회

    - 등록된 상품들 조회

#### 테이블 그룹 관리

1. 테이블 그룹 지정

    - 주문 테이블들(orderTables)으로 생성
    
        - 주문 테이블 - 주문 테이블 ID(id)
    
        - 둘 이상이어야 함
        
        - 이미 등록된 주문 테이블이어야 함
        
        - 테이블 그룹 설정이 되어있지 않아야 함

2. 테이블 그룹 해제

    - 테이블 그룹 ID(tableGroupId)로 해제
    
        - 테이블 그룹에 속한 테이블 모두 계산 완료 상태인 경우에만 해제 가능 

#### 테이블관리

1. 테이블 생성

    - 방문한 손님 수(numberOfGuests)와 테이블 비어있는지 여부(empty)를 받아서 생성

2. 테이블들 조회

    - 등록된 테이블들 조회
    
3. 테이블 비어있는지 여부 변경

    - 테이블 ID(orderTableId)와 테이블 비어있는지 여부(empty)를 받아서 변경
    
    - 계산 완료된 테이블만 변경 가능
    
4. 테이블 방문한 손님 수 변경

    - 테이블 ID(orderTableId)와 방문한 손님 수(numberOfGuests)를 받아서 변경
    
    - 방문한 손님 수는 0 이상이어야 함
