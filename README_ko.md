# inventory

![Maven Central Version](https://img.shields.io/maven-central/v/io.typst/inventory-core)

모든 게임의 인벤토리를 표현할 수 있는 데이터와 그에 대한 연산을 수행할 수 있게 일반화한 경량 라이브러리입니다.

인벤토리 데이터를 다음과 같은 타입으로 표현하고: 
- Map<Int, MyGameItem>
- List<MyGameItem>
- MyGameInventory

해당 데이터 다음 연산을 수행할 수 있습니다: give, take, has, ...

## Import

### Minecraft Bukkit API 모듈

Gradle:

```groovy
dependencies {
    // Java
    implementation("io.typst:inventory-bukkit:$THE_LATEST")
    // or Kotlin
    implementation("io.typst:inventory-bukkit-kotlin:$THE_LATEST")
}
```

Maven:

```xml
<dependencies>
    <dependency>
        <groupId>io.typst</groupId>
        <artifactId>inventory-bukkit</artifactId>
        <version>$THE_LATEST</version>
    </dependency>
</dependencies>
```

### 커스텀 구현자를 위한 Core 모듈
```groovy
dependencies {
    implementation("io.typst:inventory:inventory-core")
}
```

## Motivation

### 마인크래프트 Bukkit API 바깥의 유즈 케이스

전제조건: `io.typst:inventory-bukkit` 종속성 적용

예시 1: 특정 슬롯(3)에만 아이템 넣고 남는 건 드롭

```java
// 버킷 API: 자바 예시
InventoryMutator<ItemStack, Player> mutator = BukkitInventories.from(inventory)
      .withSubInventory(3) // 범위 선택 가능
      .giveItemOrDrop(player, item);
```
```kotlin
// 버킷 API: 코틀린 예시
inventory.toMutator().withSubInventory(3) // 범위 선택 가능
    .giveItemOrDrop(player, item)
```

예시 2: 인벤토리에 공간 충분할 때만 넣기

```java
if (BukkitInventories.from(inventory).giveItem(item)) {
    // 성공, 인벤토리 데이터 업데이트 됨
} else {
    // 실패: 공간 없음, 인벤토리 데이터 변동 X
}
```

예시 3: 인벤토리를 List 나 Map 으로 표현했을 경우

```java
// map: Map<Int, ItemStack>
BukkitInventories.from(map).giveItemOrDrop(player, item);
if (BukkitInventories.from(map).takeItems(items)) {
  // success
}
```

## Bukkit API implementation

### Java

- BukkitInventories
- BukkitItemStacks

### Kotlin

- InventoryExtensions
- ItemStackExtensions

## 개념

`inventory` 는 크게 세 가지 역할로 나뉩니다:

1. 아이템 추상화(ItemStackOps<A>): 아이템 타입 `A` 를 어떻게 비교/복사/생성/비움으로 표현할지 정의
2. 인벤토리 뷰(InventoryAdapter<A>): 어떤 저장 구조든 `슬롯 -> 아이템` 형태로 다루게 해주는 어댑터
3. 순수 연산(InventorySnapshotView<A>): 실제 인벤토리를 바로 건드리지 않고, 연산 결과(변경될 슬롯, 남는 수량)를 계산해서 돌려주는 계층

이 세 가지를 조합해서 마인크래프트 Inventory 는 물론 어떤 게임에서든 인벤토리 연산을 할 수 있습니다.

## 흐름

- 인벤토리 표현 데이터를 `InventoryAdapter<A>` 로 감싸기
- 사용하는 아이템 타입에 따른 `ItemStackOps<A>` 구현
- `InventorySnapshotView<A>` 스냅샷 생성
- give/take/has 등의 연산 호출
- 결과에 포함된 modifiedItems 를 직접 게임 인벤토리 상태에 적용


## 구성 요소

### InventoryMutator<A>

순수 연산 결과를 실제 게임 인벤토리에 적용하는 역할:
- `InventorySnapshotView` 의 결과를 읽고:
  - 슬롯에 반영하거나
  - 남는 아이템을 엔티티 주변에 드랍 등의 부수효과 처리
- 자주 쓰는 give, take 패턴의 IO 작업

이로 인해 계산과 이펙트가 깔끔하게 분리됩니다:
- `giveItemOrDrop(Entity, Item)`
- `giveItem(Item): Boolean`
- `takeItems(Item...): Boolean`

### InventorySnapshotView<A>

실제 인벤토리를 건드리지 않는 순수 연산 게층:
- `giveItem(A): InventoryPatch<A>`
- `takeItems(A...): InventoryPatch<A>`
- `hasItems(A): Boolean`
- `countItems(ItemKey)`
- `findSpaces(A): Map<Int, Int>`
- `findSlots(A): Map<Int, Int>`
- ...

### InventoryPatch<A>

연산 결과를 명시적으로 표현:
  - modifiedItems: 변경될 슬롯(게임 인벤토리에 덮어쓰기 가능한 형태)
  - failure: 실패 정보
    - giveLeftoverItems: give 시 공간이 없어 다 넣지 못 한 아이템
    - takeRemainingItems: take 시 부족한 아이템

### InventoryAdapter<A>

슬롯 기반 접근을 일반화하는 인터페이스, 구현:

- `ListInventoryAdapter<A>`: List 형태의 인벤토리
- `MapInventoryAdapter<A>`: Map 형태의 인벤토리
- `BukkitInventoryAdapter<A>`: 버킷 API 의 인벤토리
- `SubInventoryAdapter<A>`: InventoryAdapter<A> 를 특정 범위 슬롯으로 나누기 

### ItemStackOps<A>

아이템 타입에 따른 공통 연산 정의:

- `empty(): A`: 빈 아이템 표현 
- `isEmpty(A): Boolean`: 빈 아이템인지 판단
- `isSimilar(A, A): Boolean`: 비슷한 아이템인지 판단
- `getAmount(A): Int` / `setAmount(A, Int)`: 아이템 개수 get/set
- `getMaxStackSize(A): Int`: 아이템의 쌓일 수 있는 최대 개수 get
- `copy(A): A`: 아이템 copy

