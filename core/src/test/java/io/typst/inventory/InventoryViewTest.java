package io.typst.inventory;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryViewTest {

    private final MockItemOps ops = new MockItemOps();
    private final ItemKey emptyKey = MockItem.defaultKey;

    private MockItem mi(String id, int amount, int max) {
        return new MockItem(id, amount, max);
    }

    private ItemKey key(String id) {
        return new ItemKey(id, id);
    }

    private InventorySnapshotView<MockItem> viewOf(LinkedHashMap<Integer, MockItem> map) {
        return new InventorySnapshotView<>(
                new MapInventoryAdapter<>(map, ops.empty()),
                ops,
                emptyKey
        );
    }

    @Test
    void emptyFactoryAndIteratorAndLowLevelGuards() {
        InventorySnapshotView<MockItem> emptyView =
                InventorySnapshotView.empty(ops, emptyKey);

        assertFalse(emptyView.iterator().hasNext());

        assertTrue(emptyView.findSpaces(ops.empty()).isEmpty());
        assertTrue(emptyView.findSpaces(0, 64, x -> true).isEmpty());
        assertTrue(emptyView.findSpaces(1, 0, x -> true).isEmpty());
        assertTrue(emptyView.findSlots(0, x -> true).isEmpty());
    }

    @Test
    void toImmutableCopiesAndNormalizesEmptySlots() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("air", 1, 1)); // empty-like
        inv.put(1, mi("apple", 2, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);
        InventorySnapshotView<MockItem> imm = view.toImmutable();

        assertTrue(ops.isEmpty(imm.getInventory().get(0)));
        assertFalse(ops.isEmpty(imm.getInventory().get(1)));
        assertEquals("apple", imm.getInventory().get(1).getId());
    }

    @Test
    void updatedOverwritesAndAddsSlots() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 1, 64));
        inv.put(1, mi("dirt", 1, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);

        InventorySnapshotView<MockItem> upd = view.updated(
                Map.of(1, mi("apple", 2, 64), 2, mi("dirt", 3, 64))
        );

        assertEquals("apple", upd.getInventory().get(0).getId());
        assertEquals(1, upd.getInventory().get(0).getAmount());

        assertEquals("apple", upd.getInventory().get(1).getId());
        assertEquals(2, upd.getInventory().get(1).getAmount());

        assertEquals("dirt", upd.getInventory().get(2).getId());
        assertEquals(3, upd.getInventory().get(2).getAmount());
    }

    @Test
    void subInventoryIterableAndVarargs() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 1, 64));
        inv.put(1, mi("dirt", 1, 64));
        inv.put(2, mi("apple", 1, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);

        InventorySnapshotView<MockItem> sub1 = view.subInventory(List.of(0, 2));
        InventorySnapshotView<MockItem> sub2 = view.subInventory(0, 2);

        Set<Integer> k1 = new LinkedHashSet<>();
        sub1.forEach(e -> k1.add(e.getKey()));
        Set<Integer> k2 = new LinkedHashSet<>();
        sub2.forEach(e -> k2.add(e.getKey()));

        assertEquals(Set.of(0, 2), k1);
        assertEquals(Set.of(0, 2), k2);
    }

    @Test
    void findSpaces_highLevelAndStackingBranches() {
        // stacking + skip when space=0
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 64, 64)); // full => space 0 (skip branch)
        inv.put(1, mi("apple", 60, 64)); // space 4
        inv.put(2, mi("dirt", 10, 64));
        inv.put(3, mi("apple", 63, 64)); // space 1

        InventorySnapshotView<MockItem> view = viewOf(inv);

        Map<Integer, Integer> spaces = view.findSpaces(mi("apple", 8, 64));
        assertEquals(Map.of(1, 4, 3, 1), spaces);

        // empty-slot path + early break
        LinkedHashMap<Integer, MockItem> inv2 = new LinkedHashMap<>();
        inv2.put(0, ops.empty());
        inv2.put(1, mi("apple", 50, 64));
        inv2.put(2, mi("dirt", 64, 64));
        inv2.put(3, ops.empty());

        InventorySnapshotView<MockItem> view2 = viewOf(inv2);

        Map<Integer, Integer> spaces2 = view2.findSpaces(mi("apple", 20, 64));
        assertEquals(Map.of(0, 20), spaces2); // breaks right away after slot0
    }

    @Test
    void findSpacesByKey_handlesNullCreate() {
        // if itemOps.create(key) returns null => maxStack=0 => empty result
        // choose a key MockItemOps doesn't know, if that's how it's implemented
        InventorySnapshotView<MockItem> view = viewOf(new LinkedHashMap<>());
        Map<Integer, Integer> spaces = view.findSpaces(new ItemKey("", ""), 10);
        assertTrue(spaces.isEmpty());
    }

    @Test
    void findSpacesByKey() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 64, 64)); // full => space 0 (skip branch)
        inv.put(1, mi("apple", 60, 64)); // space 4
        inv.put(2, mi("dirt", 10, 64));
        inv.put(3, mi("apple", 63, 64)); // space 1
        InventorySnapshotView<MockItem> view = viewOf(inv);

        assertEquals(Map.of(1, 1), view.findSpaces(key("apple"), 1));
    }

    @Test
    void findSlotsNoMatching() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 0, 64)); // predicate true but amount=0 => skip
        inv.put(1, mi("apple", 2, 64));
        inv.put(2, mi("dirt", 7, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);
        Map<Integer, Integer> result = view.findSlots(new MockItem("abc", 1, 64));
        assertEquals(Map.of(), result);
    }

    @Test
    void findSlots_branchesIncludingZeroAmountSlot() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 0, 64)); // predicate true but amount=0 => skip
        inv.put(1, mi("apple", 2, 64));
        inv.put(2, mi("dirt", 7, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);

        Map<Integer, Integer> slots = view.findSlots(1, x -> ops.isSimilar(x, mi("apple", 1, 64)));
        assertEquals(Map.of(1, 1), slots);

        // normal stop-early path
        LinkedHashMap<Integer, MockItem> inv2 = new LinkedHashMap<>();
        inv2.put(0, mi("apple", 5, 64));
        inv2.put(1, mi("apple", 2, 64));
        inv2.put(2, mi("dirt", 7, 64));

        InventorySnapshotView<MockItem> view2 = viewOf(inv2);
        Map<Integer, Integer> slots2 = view2.findSlots(mi("apple", 6, 64));

        assertEquals(Map.of(0, 5, 1, 1), slots2);
    }

    @Test
    void takeItem_slotsEmptyBranchCreatesRemaining() {
        InventorySnapshotView<MockItem> emptyView =
                InventorySnapshotView.empty(ops, emptyKey);

        MockItem base = mi("apple", 1, 64);

        InventoryPatch<MockItem> patch =
                emptyView.takeItem(3, base, x -> ops.isSimilar(x, base));

        assertTrue(patch.getModifiedItems().isEmpty());
        assertTrue(patch.getDiff().isEmpty());
        assertEquals(1, patch.getFailure().getTakeRemainingItems().size());
        assertEquals(3, patch.getFailure().getTakeRemainingItems().get(0).getAmount());
    }

    @Test
    void takeItem_normalBranch_coversNewAmountPathsAndNoRemaining() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 2, 64)); // will become empty (newAmount<=0)
        inv.put(1, mi("apple", 5, 64)); // will become 3 (newAmount>0)
        inv.put(2, mi("apple", 1, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);

        MockItem base = mi("apple", 4, 64);
        InventoryPatch<MockItem> patch =
                view.takeItem(4, base, x -> ops.isSimilar(x, base));

        assertTrue(patch.isSuccess());

        assertTrue(ops.isEmpty(patch.getModifiedItems().get(0)));
        assertEquals(3, patch.getModifiedItems().get(1).getAmount());

        assertEquals(2, patch.getDiff().size());
        assertEquals(2, patch.getDiff().get(0).getValue().getAmount());
        assertEquals(2, patch.getDiff().get(1).getValue().getAmount());

        assertTrue(patch.getFailure().isEmpty());
    }

    @Test
    void takeItem_normalBranch_withRemainingAfterTakingAll() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 2, 64));
        inv.put(1, mi("apple", 5, 64));
        inv.put(2, mi("apple", 1, 64)); // total 8

        InventorySnapshotView<MockItem> view = viewOf(inv);

        MockItem base = mi("apple", 10, 64);
        InventoryPatch<MockItem> patch =
                view.takeItem(10, base, x -> ops.isSimilar(x, base));

        assertFalse(patch.isSuccess());
        assertEquals(3, patch.getModifiedItems().size());
        assertEquals(1, patch.getFailure().getTakeRemainingItems().size());
        assertEquals(2, patch.getFailure().getTakeRemainingItems().get(0).getAmount());
    }

    @Test
    void takeItems_skipsEmptyItemsAndVarargsDelegates() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 3, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);

        InventoryPatch<MockItem> patch =
                view.takeItems(ops.empty(), mi("apple", 2, 64));

        assertEquals(1, patch.getModifiedItems().size());
        assertEquals(1, patch.getModifiedItems().get(0).getAmount());
        assertEquals(1, patch.getDiff().size());

        InventoryPatch<MockItem> patch2 =
                view.takeItems(mi("apple", 1, 64));
        assertEquals(1, patch2.getDiff().size());
    }

    @Test
    void takeItemByKey_delegate() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 3, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);

        InventoryPatch<MockItem> patch = view.takeItem(1, key("apple"));
        assertEquals(1, patch.getDiff().size());
        assertTrue(patch.getFailure().isEmpty());
    }

    @Test
    void hasItemsAndCountItems() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 2, 64));
        inv.put(1, mi("apple", 0, 64));
        inv.put(2, mi("apple", 1, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);

        assertTrue(view.hasItems(mi("apple", 3, 64)));
        assertFalse(view.hasItems(mi("apple", 4, 64)));

        assertEquals(3, view.countItems(key("apple")));
    }

    @Test
    void giveItems_spacesEmptyBranch_returnsEmptyPatch() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 64, 64));
        inv.put(1, mi("dirt", 64, 64));

        InventorySnapshotView<MockItem> view = viewOf(inv);

        InventoryPatch<MockItem> patch = view.giveItems(mi("apple", 1, 64));
        assertTrue(patch.getModifiedItems().isEmpty());
        assertTrue(patch.getDiff().isEmpty());
        assertTrue(patch.getFailure().isEmpty());
        assertFalse(patch.isSuccess());
    }

    @Test
    void giveItems_normalBranch_noLeftover() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 60, 64)); // space 4
        inv.put(1, mi("dirt", 64, 64));
        inv.put(2, ops.empty());         // space for 6
        inv.put(3, mi("apple", 63, 64)); // would be next if needed

        InventorySnapshotView<MockItem> view = viewOf(inv);

        InventoryPatch<MockItem> patch = view.giveItems(mi("apple", 10, 64));

        assertTrue(patch.getFailure().isEmpty());

        assertEquals(2, patch.getModifiedItems().size());
        assertEquals(64, patch.getModifiedItems().get(0).getAmount()); // stacked
        assertEquals(6, patch.getDiff().get(1).getValue().getAmount()); // from empty slot

        assertEquals(2, patch.getDiff().size());
    }

    @Test
    void giveItems_normalBranch_withLeftover() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, mi("apple", 60, 64)); // space 4
        inv.put(1, mi("apple", 64, 64)); // full
        inv.put(2, mi("dirt", 64, 64));
        inv.put(3, ops.empty()); // space 64 => total space 68

        InventorySnapshotView<MockItem> view = viewOf(inv);

        InventoryPatch<MockItem> patch = view.giveItems(mi("apple", 70, 64));

        assertFalse(patch.getFailure().isEmpty());
        assertEquals(1, patch.getFailure().getGiveLeftoverItems().size());
        assertEquals(2, patch.getFailure().getGiveLeftoverItems().get(0).getAmount());

        assertEquals(Set.of(0, 3),
                patch.getModifiedItems().keySet());
    }

    @Test
    void giveItems_varargsDelegates() {
        LinkedHashMap<Integer, MockItem> inv = new LinkedHashMap<>();
        inv.put(0, ops.empty());

        InventorySnapshotView<MockItem> view = viewOf(inv);

        InventoryPatch<MockItem> patch = view.giveItems(
                mi("apple", 1, 64),
                mi("dirt", 1, 64)
        );

        // at least executed without throwing and used varargs overload
        assertNotNull(patch);
    }
}
