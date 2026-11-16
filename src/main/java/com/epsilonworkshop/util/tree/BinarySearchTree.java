/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.tree;

import com.epsilonworkshop.util.queue.ArrayQueue;
import com.epsilonworkshop.util.queue.QueueADT;
import com.epsilonworkshop.util.stack.ArrayStack;
import com.epsilonworkshop.util.stack.StackADT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Árbol binario de búsqueda sin balanceo.
 *
 * El orden lo define un Comparator, así se puede indexar una entidad por un
 * campo (por ejemplo, un cliente por su documento) sin modificar su clase.
 *
 * Todo es iterativo, sin recursión: en un árbol degenerado (datos insertados ya
 * ordenados) la altura llega a n y la recursión desbordaría la pila de la JVM.
 * Los recorridos usan la pila y la cola estáticas de este mismo avance; su
 * capacidad es el tamaño del árbol, que nunca se supera porque cada nodo entra
 * una sola vez.
 *
 * @author sebap
 */
public final class BinarySearchTree<T> implements BinaryTreeADT<T> {

    private static final class Node<T> {

        T value;
        Node<T> left;
        Node<T> right;

        Node(T value) {
            this.value = value;
        }
    }

    // Nodo en espera durante la descripción por niveles.
    private record Pending<T>(Node<T> node, String parent, int level) {

    }

    private final Comparator<? super T> comparator;
    private Node<T> root;
    private int size;

    public BinarySearchTree(Comparator<? super T> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "El comparador es obligatorio");
    }

    /**
     * Árbol ordenado por el orden natural del tipo (Integer, String,
     * WorkOrder...).
     */
    public static <T extends Comparable<? super T>> BinarySearchTree<T> naturalOrder() {
        return new BinarySearchTree<>(Comparator.naturalOrder());
    }

    // ================= Operaciones =================
    @Override
    public boolean insert(T value) {
        requireValue(value);

        if (root == null) {
            root = new Node<>(value);
            size++;
            return true;
        }

        Node<T> current = root;
        while (true) {
            int cmp = comparator.compare(value, current.value);
            if (cmp == 0) {
                return false;
            }
            if (cmp < 0) {
                if (current.left == null) {
                    current.left = new Node<>(value);
                    size++;
                    return true;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = new Node<>(value);
                    size++;
                    return true;
                }
                current = current.right;
            }
        }
    }

    @Override
    public Optional<T> find(T probe) {
        requireValue(probe);

        Node<T> current = root;
        while (current != null) {
            int cmp = comparator.compare(probe, current.value);
            if (cmp == 0) {
                return Optional.of(current.value);
            }
            current = (cmp < 0) ? current.left : current.right;
        }
        return Optional.empty();
    }

    @Override
    public boolean contains(T probe) {
        return find(probe).isPresent();
    }

    @Override
    public boolean delete(T probe) {
        requireValue(probe);

        Node<T> parent = null;
        Node<T> current = root;

        while (current != null) {
            int cmp = comparator.compare(probe, current.value);
            if (cmp == 0) {
                break;
            }
            parent = current;
            current = (cmp < 0) ? current.left : current.right;
        }

        if (current == null) {
            return false;
        }

        // Dos hijos: el nodo toma el valor de su sucesor (el menor del subárbol
        // derecho) y se elimina el nodo del sucesor, que tiene a lo sumo un hijo.
        if (current.left != null && current.right != null) {
            Node<T> successorParent = current;
            Node<T> successor = current.right;
            while (successor.left != null) {
                successorParent = successor;
                successor = successor.left;
            }
            current.value = successor.value;
            parent = successorParent;
            current = successor;
        }

        // Cero o un hijo: el hijo (si hay) ocupa el lugar del nodo eliminado.
        Node<T> child = (current.left != null) ? current.left : current.right;
        replaceChild(parent, current, child);
        size--;
        return true;
    }

    private void replaceChild(Node<T> parent, Node<T> oldChild, Node<T> newChild) {
        if (parent == null) {
            root = newChild;
        } else if (parent.left == oldChild) {
            parent.left = newChild;
        } else {
            parent.right = newChild;
        }
    }

    @Override
    public T min() {
        if (root == null) {
            throw new NoSuchElementException("El árbol está vacío");
        }
        Node<T> current = root;
        while (current.left != null) {
            current = current.left;
        }
        return current.value;
    }

    @Override
    public T max() {
        if (root == null) {
            throw new NoSuchElementException("El árbol está vacío");
        }
        Node<T> current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.value;
    }

    @Override
    public int height() {
        if (root == null) {
            return 0;
        }

        QueueADT<Node<T>> queue = new ArrayQueue<>(traversalCapacity());
        queue.enqueue(root);
        int levels = 0;

        while (!queue.isEmpty()) {
            int nodesInLevel = queue.size();
            for (int i = 0; i < nodesInLevel; i++) {
                Node<T> node = queue.dequeue();
                if (node.left != null) {
                    queue.enqueue(node.left);
                }
                if (node.right != null) {
                    queue.enqueue(node.right);
                }
            }
            levels++;
        }
        return levels;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    // ================= Recorridos =================
    @Override
    public List<T> preOrder() {
        List<T> result = new ArrayList<>(size);
        if (root == null) {
            return result;
        }

        StackADT<Node<T>> stack = new ArrayStack<>(traversalCapacity());
        stack.push(root);

        while (!stack.isEmpty()) {
            Node<T> node = stack.pop();
            result.add(node.value);
            // El derecho entra primero para que el izquierdo salga antes.
            if (node.right != null) {
                stack.push(node.right);
            }
            if (node.left != null) {
                stack.push(node.left);
            }
        }
        return result;
    }

    @Override
    public List<T> inOrder() {
        List<T> result = new ArrayList<>(size);
        StackADT<Node<T>> stack = new ArrayStack<>(traversalCapacity());
        Node<T> current = root;

        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            current = stack.pop();
            result.add(current.value);
            current = current.right;
        }
        return result;
    }

    @Override
    public List<T> postOrder() {
        List<T> result = new ArrayList<>(size);
        StackADT<Node<T>> stack = new ArrayStack<>(traversalCapacity());
        Node<T> current = root;
        Node<T> lastVisited = null;

        while (current != null || !stack.isEmpty()) {
            if (current != null) {
                stack.push(current);
                current = current.left;
            } else {
                Node<T> top = stack.peek();
                if (top.right != null && lastVisited != top.right) {
                    current = top.right;
                } else {
                    result.add(top.value);
                    lastVisited = stack.pop();
                }
            }
        }
        return result;
    }

    @Override
    public List<T> levelOrder() {
        List<T> result = new ArrayList<>(size);
        if (root == null) {
            return result;
        }

        QueueADT<Node<T>> queue = new ArrayQueue<>(traversalCapacity());
        queue.enqueue(root);

        while (!queue.isEmpty()) {
            Node<T> node = queue.dequeue();
            result.add(node.value);
            if (node.left != null) {
                queue.enqueue(node.left);
            }
            if (node.right != null) {
                queue.enqueue(node.right);
            }
        }
        return result;
    }

    // ================= Inspección =================
    @Override
    public List<TreeNodeInfo> describeNodes(Function<? super T, String> display) {
        List<TreeNodeInfo> result = new ArrayList<>(size);
        if (root == null) {
            return result;
        }

        QueueADT<Pending<T>> queue = new ArrayQueue<>(traversalCapacity());
        queue.enqueue(new Pending<>(root, "—", 0));

        while (!queue.isEmpty()) {
            Pending<T> pending = queue.dequeue();
            Node<T> node = pending.node();
            String label = display.apply(node.value);
            boolean leaf = node.left == null && node.right == null;

            String kind;
            if (pending.level() == 0) {
                kind = leaf ? "RAÍZ/HOJA" : "RAÍZ";
            } else {
                kind = leaf ? "HOJA" : "INTERNO";
            }

            result.add(new TreeNodeInfo(
                    label,
                    pending.parent(),
                    node.left == null ? "—" : display.apply(node.left.value),
                    node.right == null ? "—" : display.apply(node.right.value),
                    pending.level(),
                    kind));

            if (node.left != null) {
                queue.enqueue(new Pending<>(node.left, label, pending.level() + 1));
            }
            if (node.right != null) {
                queue.enqueue(new Pending<>(node.right, label, pending.level() + 1));
            }
        }
        return result;
    }

    // ================= Privados =================
    // Pila y cola son de capacidad fija y no admiten capacidad 0.
    private int traversalCapacity() {
        return Math.max(1, size);
    }

    private void requireValue(T value) {
        if (value == null) {
            throw new IllegalArgumentException("No se admiten valores null");
        }
    }
}
