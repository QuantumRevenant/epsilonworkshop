/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author sebap
 */
public class PasswordHasher {

    private PasswordHasher() {
    }
    
    public static String hash(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    public static boolean verify(String password, String hash){
        return BCrypt.checkpw(password, hash);
    }
}
