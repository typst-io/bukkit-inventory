# bukkit-inventory

A lightweight library for doing give, take, has operations on any inventory datatype.

## Motivation

To add an item into an inventory:

```java
inventory.addItem(itemStack);
```

But when if we add it to a specific slot 3? then:

```java
inventory.setItem(3, itemStack);
```

What if there's already an item on the slot, and it can be stacked?

```java
// ItemStack item;
ItemStack slotItem = inventory.getItem(3);
if (slotItem == null || slotItem.getType() == AIR) {
    inventory.setItem(slot, item);
} else if (slotItem.isSimilar(item)) {
    slotItem.setAmount(min(slotItem.getAmount() + item.getAmount(), slotItem.maxStackSize));
}
```

We need above code, and moreover what if we want to add an item on 2 or more slots? and what if the target inventory has expressed as another datatype like `Map<Int, ItemStack>`, `List<ItemStack>`?

`bukkit-inventory` normalized such requirements:

```text
val iterator = new SubInventoryIterator(inventory, Arrays.asList(3, 4));
Inventories.giveItem(iterator, item);
```

If you use kotlin:

```kotlin
inventory.sub(3, 4).giveItem(item)
```

## Usage

```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly 'org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT'
    implementation 'io.typst:bukkit-inventory:1.0.0'
    // Use below instead for kotlin:
    // implementation 'io.typecraft:bukkit-inventory-kotlin:1.0.0'
}

assemble.dependsOn(shadowJar)
```
