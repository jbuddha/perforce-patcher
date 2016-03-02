/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.buddha.perforce.patch;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientViewMapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jbuddha
 */
public final class Mapping {
    private final Map<String,String> map;
    private final String localRoot;
    private final IClient client;
    
    public Mapping(IClient client) {
        this.client = client;
        this.localRoot = client.getRoot();
        List<IClientViewMapping> mappings = client.getClientView().getEntryList();
        map = new HashMap<>();
        for(IClientViewMapping mapping: mappings)
        {
            addMapping(mapping.getLeft(), mapping.getRight());
        }
    }
    
    public void addMapping(String left, String right){
        map.put(left.replace("...",""), 
                right.replace("//"+client.getName(), localRoot)
                     .replace("...",""));
    }

    public String findLocalPath(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String findLeft(String remotePath) {
        int len = 0;
        String l = null;
        for(String left: map.keySet()) {
            if(left.length() > len && remotePath.contains(left)) {
                len = left.length();
                l = left;
            }
        }
        return l;
    }

    public String findRight(String remotePath) {
        String left = findLeft(remotePath);
        String residualLeft = remotePath.replace(left, "");
        return map.get(left) + residualLeft;
    }
}
