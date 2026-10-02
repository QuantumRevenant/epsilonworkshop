/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.view.avances.Avance1Panel;
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

        // Contenido inicial: bienvenida simple.
        // Cuando existan ClientPanel, VehiclePanel, etc., se reemplaza
        // por un JTabbedPane o CardLayout que los contenga a todos.
        JLabel placeholder = new JLabel(
                "Bienvenido a Épsilon Workshop", SwingConstants.CENTER);
        placeholder.setFont(placeholder.getFont().deriveFont(20f));
        add(placeholder, BorderLayout.CENTER);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu toolsMenu = new JMenu("Herramientas");
        JMenuItem algorithmsItem = new JMenuItem("Algoritmos (sort/arreglos)");
        algorithmsItem.addActionListener(e -> openAlgorithmsPanel());
        toolsMenu.add(algorithmsItem);

        menuBar.add(toolsMenu);
        return menuBar;
    }

    private void openAlgorithmsPanel() {
        JDialog dialog = new JDialog(this, "Algoritmos", false);
        dialog.setContentPane(new Avance1Panel());
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
