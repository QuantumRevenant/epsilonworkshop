/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sebap
 */
public class PasswordHasherTest {
    
    @Test
    void shouldGenerateHash(){
        String password = "testPassword123";
        
        String hash=PasswordHasher.hash(password);
        
        assertNotNull(hash);
        assertFalse(hash.isBlank());
        assertNotEquals(password,hash);
    }
    
    @Test
    void shouldVerifyCorrectPassword(){
        String password = "testPassword123";
        
        String hash=PasswordHasher.hash(password);
        
        assertTrue(PasswordHasher.verify(password, hash));
    }
    
    @Test
    void shouldRejectIncorrectPassword(){
        String password = "testPassword123";
        String wrongPassword="wrongPassword456";
        
        String hash=PasswordHasher.hash(password);
        
        assertFalse(PasswordHasher.verify(wrongPassword, hash));
    }
    
    @Test
    void shouldGenerateDifferenteRejectIncorrectPassword(){
        String password = "testPassword123";
        String wrongPassword="wrongPassword456";
        
        String hash=PasswordHasher.hash(password);
        
        assertFalse(PasswordHasher.verify(wrongPassword, hash));
    }
    
    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String password = "testPassword123";

        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void shouldVerifyBothHashesForSamePassword() {
        String password = "testPassword123";

        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);

        assertTrue(PasswordHasher.verify(password, hash1));
        assertTrue(PasswordHasher.verify(password, hash2));
    }
}
