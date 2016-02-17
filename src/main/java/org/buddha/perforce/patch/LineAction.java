/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buddha.perforce.patch;

/**
 *
 * @author buddha
 */
public enum LineAction {

	ADD("+"),REMOVE("-"),DEFAULT(" ");
	
	private String a;
	
	private LineAction(String a) {
		this.a = a;
	}

	@Override
	public String toString() {
		return a; 
	}
	
	
}
