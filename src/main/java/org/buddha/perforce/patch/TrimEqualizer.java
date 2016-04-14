/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buddha.perforce.patch;

import difflib.myers.Equalizer;

/**
 *
 * @author jbuddha
 */
public class TrimEqualizer<T> implements Equalizer<T>{

    @Override
    public boolean equals(T original, T revised) {
        return original.toString().trim().equals(revised.toString().trim());
    }
    
}
