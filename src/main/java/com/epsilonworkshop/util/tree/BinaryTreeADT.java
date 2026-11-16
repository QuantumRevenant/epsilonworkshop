/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.tree;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Contrato de un árbol binario de búsqueda.
 *
 * @author sebap
 */
public interface BinaryTreeADT<T> {

    /**
     * Inserta el valor. Devuelve false, sin insertar, si ya hay uno con la
     * misma clave.
     */
    boolean insert(T value);

    /**
     * Busca el elemento cuya clave equivale a la del objeto de prueba.
     */
    Optional<T> find(T probe);

    boolean contains(T probe);

    /**
     * Elimina el elemento cuya clave equivale a la del objeto de prueba.
     * Devuelve false si no existe.
     */
    boolean delete(T probe);

    /**
     * Menor elemento. Lanza NoSuchElementException si el árbol está vacío.
     */
    T min();

    /**
     * Mayor elemento. Lanza NoSuchElementException si el árbol está vacío.
     */
    T max();

    /**
     * Número de niveles: 0 si está vacío, 1 si solo hay raíz.
     */
    int height();

    int size();

    boolean isEmpty();

    void clear();

    List<T> preOrder();

    List<T> inOrder();

    List<T> postOrder();

    List<T> levelOrder();

    /**
     * Los nodos por niveles, con padre, hijos y tipo. `display` convierte un
     * valor en texto.
     */
    List<TreeNodeInfo> describeNodes(Function<? super T, String> display);
}
