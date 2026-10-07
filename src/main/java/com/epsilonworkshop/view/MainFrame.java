/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.view.avances.Avance1Panel;
import com.epsilonworkshop.view.avances.Avance2Panel;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author sebap
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Épsilon Workshop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        JLabel placeholder = new JLabel(
                "Bienvenido a Épsilon Workshop", SwingConstants.CENTER);
        placeholder.setFont(placeholder.getFont().deriveFont(20f));
        add(placeholder, BorderLayout.CENTER);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu toolsMenu = new JMenu("Herramientas");

        JMenuItem avance1Item = new JMenuItem("Avance 1: Algoritmos (sort/arreglos)");
        avance1Item.addActionListener(e -> openAvance1Panel());
        toolsMenu.add(avance1Item);

        JMenuItem avance2Item = new JMenuItem("Avance 2: TADs (listas enlazadas)");
        avance2Item.addActionListener(e -> openAvance2Panel());
        toolsMenu.add(avance2Item);

        menuBar.add(toolsMenu);
        return menuBar;
    }

    private void openAvance1Panel() {
        JDialog dialog = new JDialog(this, "Avance 1: Algoritmos", false);
        dialog.setContentPane(new Avance1Panel());
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openAvance2Panel() {
        JDialog dialog = new JDialog(this, "Avance 2: TADs", false);
        dialog.setContentPane(new Avance2Panel());
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}