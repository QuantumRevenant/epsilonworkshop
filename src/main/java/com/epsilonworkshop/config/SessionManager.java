/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.config;

import com.epsilonworkshop.dao.UserDAO;
import com.epsilonworkshop.exception.AuthenticationException;
import com.epsilonworkshop.model.User;

/**
 *
 * @author sebap
 */
public class SessionManager {

     private static User currentUser;
    private static Long currentMechanicId;

    private SessionManager() {
    }

    public static void login(User user, Long mechanicId) {
        currentUser = user;
        currentMechanicId = mechanicId;
    }

    public static void logout() {
        currentUser = null;
        currentMechanicId = null;
    }

    public static User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException(
                "No hay ninguna sesión activa. Verifica isLoggedIn() antes de llamar a getCurrentUser()."
            );
        }
        return currentUser;
    }

    public static Long getCurrentMechanicId() {
        return currentMechanicId;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
