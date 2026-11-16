/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.tree;

/**
 * Nodo de un árbol para inspección y demostración académica: su valor, su
 * padre, sus hijos (con "—" cuando no existen), su nivel y su tipo (RAÍZ,
 * INTERNO, HOJA o RAÍZ/HOJA). Equivalente de NodeInfo y SlotInfo.
 *
 * @author sebap
 */
public record TreeNodeInfo(String value, String parent, String left, String right,
        int level, String kind) {

}
