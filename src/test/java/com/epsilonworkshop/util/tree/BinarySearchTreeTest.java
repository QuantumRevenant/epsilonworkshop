/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Árbol de referencia de la mayoría de los tests:
 *
 * 50 / \ 30 70 / \ / \ 20 40 60 80
 *
 * @author sebap
 */
public class BinarySearchTreeTest {

    private BinarySearchTree<Integer> tree;

    @BeforeEach
    void setUp() {
        tree = BinarySearchTree.naturalOrder();
    }

    private void buildFullTree() {
        for (int value : new int[]{50, 30, 70, 20, 40, 60, 80}) {
            tree.insert(value);
        }
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(0, tree.height());
        assertTrue(tree.inOrder().isEmpty());
    }

    @Test
    void shouldReturnInOrderSortedRegardlessOfInsertionOrder() {
        buildFullTree();

        assertEquals(List.of(20, 30, 40, 50, 60, 70, 80), tree.inOrder());
    }

    @Test
    void shouldRejectDuplicateKeys() {
        assertTrue(tree.insert(5));
        assertFalse(tree.insert(5));
        assertEquals(1, tree.size());
    }

    @Test
    void shouldRejectNullValues() {
        assertThrows(IllegalArgumentException.class, () -> tree.insert(null));
        assertThrows(IllegalArgumentException.class, () -> tree.find(null));
        assertThrows(IllegalArgumentException.class, () -> tree.delete(null));
    }

    @Test
    void shouldFindExistingAndMissingValues() {
        buildFullTree();

        assertEquals(Optional.of(60), tree.find(60));
        assertTrue(tree.contains(20));
        assertFalse(tree.contains(99));
        assertEquals(Optional.empty(), tree.find(99));
    }

    @Test
    void shouldReturnAllTraversalsInExpectedOrder() {
        buildFullTree();

        assertEquals(List.of(50, 30, 20, 40, 70, 60, 80), tree.preOrder());
        assertEquals(List.of(20, 30, 40, 50, 60, 70, 80), tree.inOrder());
        assertEquals(List.of(20, 40, 30, 60, 80, 70, 50), tree.postOrder());
        assertEquals(List.of(50, 30, 70, 20, 40, 60, 80), tree.levelOrder());
    }

    @Test
    void shouldReturnMinAndMax() {
        buildFullTree();

        assertEquals(20, tree.min());
        assertEquals(80, tree.max());
    }

    @Test
    void shouldThrowMinAndMaxWhenEmpty() {
        assertThrows(NoSuchElementException.class, tree::min);
        assertThrows(NoSuchElementException.class, tree::max);
    }

    @Test
    void shouldComputeHeight() {
        buildFullTree();
        assertEquals(3, tree.height());

        tree.clear();
        tree.insert(1);
        assertEquals(1, tree.height());
    }

    @Test
    void shouldDegenerateIntoALineWhenInsertingSortedData() {
        for (int i = 1; i <= 5; i++) {
            tree.insert(i);
        }

        assertEquals(5, tree.height()); // sin balanceo, la altura es n
    }

    @Test
    void shouldTraverseDegenerateTreeWithoutRecursion() {
        int n = 10_000;
        for (int i = 1; i <= n; i++) {
            tree.insert(i);
        }

        assertEquals(n, tree.height());
        assertEquals(n, tree.inOrder().size());
        assertEquals(n, tree.postOrder().size());
    }

    @Test
    void shouldDeleteLeaf() {
        buildFullTree();

        assertTrue(tree.delete(20));

        assertEquals(List.of(30, 40, 50, 60, 70, 80), tree.inOrder());
        assertEquals(6, tree.size());
    }

    @Test
    void shouldDeleteNodeWithOneChild() {
        tree.insert(50);
        tree.insert(30);
        tree.insert(20); // 30 tiene un solo hijo (20)

        assertTrue(tree.delete(30));

        assertEquals(List.of(50, 20), tree.levelOrder());
        assertEquals(List.of(20, 50), tree.inOrder());
    }

    @Test
    void shouldDeleteNodeWithTwoChildrenUsingSuccessor() {
        buildFullTree();

        assertTrue(tree.delete(30)); // lo reemplaza su sucesor, el 40

        assertEquals(List.of(20, 40, 50, 60, 70, 80), tree.inOrder());
        assertEquals(List.of(50, 40, 70, 20, 60, 80), tree.levelOrder());
    }

    @Test
    void shouldDeleteRootWithTwoChildren() {
        buildFullTree();

        assertTrue(tree.delete(50)); // lo reemplaza su sucesor, el 60

        assertEquals(List.of(20, 30, 40, 60, 70, 80), tree.inOrder());
        assertEquals(List.of(60, 30, 70, 20, 40, 80), tree.levelOrder());
    }

    @Test
    void shouldDeleteOnlyNodeAndAllowReinsertion() {
        tree.insert(10);

        assertTrue(tree.delete(10));
        assertTrue(tree.isEmpty());

        tree.insert(5);
        assertEquals(List.of(5), tree.inOrder());
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingValue() {
        buildFullTree();

        assertFalse(tree.delete(99));
        assertEquals(7, tree.size());
    }

    @Test
    void shouldClearTree() {
        buildFullTree();

        tree.clear();

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.height());
    }

    @Test
    void shouldSearchByKeyUsingCustomComparator() {
        BinarySearchTree<String> byLength
                = new BinarySearchTree<String>(Comparator.comparingInt(String::length));
        byLength.insert("bb");
        byLength.insert("a");
        byLength.insert("ccc");

        // Misma longitud = misma clave: "zz" sirve de objeto de prueba para hallar "bb".
        assertEquals(Optional.of("bb"), byLength.find("zz"));
        assertFalse(byLength.insert("xx"));
        assertEquals(3, byLength.size());
    }

    @Test
    void shouldDescribeNodesWithParentsChildrenAndKinds() {
        buildFullTree();

        List<TreeNodeInfo> nodes = tree.describeNodes(v -> String.valueOf(v));

        assertEquals(7, nodes.size());

        TreeNodeInfo root = nodes.get(0);
        assertEquals("50", root.value());
        assertEquals("—", root.parent());
        assertEquals("30", root.left());
        assertEquals("70", root.right());
        assertEquals("RAÍZ", root.kind());
        assertEquals(0, root.level());

        TreeNodeInfo internal = nodes.get(1);
        assertEquals("30", internal.value());
        assertEquals("50", internal.parent());
        assertEquals("INTERNO", internal.kind());

        TreeNodeInfo leaf = nodes.get(3);
        assertEquals("20", leaf.value());
        assertEquals("30", leaf.parent());
        assertEquals("—", leaf.left());
        assertEquals("—", leaf.right());
        assertEquals("HOJA", leaf.kind());
        assertEquals(2, leaf.level());
    }

    @Test
    void shouldDescribeSingleNodeAsRootAndLeaf() {
        tree.insert(7);

        assertEquals("RAÍZ/HOJA", tree.describeNodes(v -> String.valueOf(v)).get(0).kind());
    }
}
