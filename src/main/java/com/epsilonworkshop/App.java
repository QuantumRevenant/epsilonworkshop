/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.epsilonworkshop;

import com.epsilonworkshop.view.LoginFrame;
import com.epsilonworkshop.view.MainFrame;

import javax.swing.*;

/**
 *
 * @author sebap
 */
public class App {

    public static void main(String[] args) {
        applyLookAndFeel();
        // SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    private static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            // se queda con el aspecto por defecto
        }
    }
}
