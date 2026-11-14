/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.array;

/**
 * Casilla de un arreglo para inspección y demostración académica: qué hay en
 * cada posición y qué marca la señala (cima, frente, final). Es el equivalente
 * de NodeInfo (Avance 2) para las estructuras basadas en arreglo.
 *
 * @author sebap
 */
public record SlotInfo(int index, String value, String marker) {
}