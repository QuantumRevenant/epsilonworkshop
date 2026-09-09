/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.epsilonworkshop;

import com.epsilonworkshop.view.MainFrame;

import javax.swing.*;

/**
 *
 * @author sebap
 */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
