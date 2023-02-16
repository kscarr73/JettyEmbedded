package com.progbits.jetty.embedded;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the Filter Information
 * 
 * 
 * @author scarr
 */
public class FilterSet {
    private final String path;
    private final Filter filter;
    private final EnumSet<DispatcherType> types;
    
    private Map<String, String> initParams = null;
    
    public FilterSet(String path, Filter filter) {
        this.path = path;
        this.filter = filter;
        types = EnumSet.of(DispatcherType.REQUEST);
    }
    
    public FilterSet(String path, Filter filter, EnumSet<DispatcherType> types) {
        this.path = path;
        this.filter = filter;
        this.types = types;
    }
    
    public FilterSet(String path, Filter filter, EnumSet<DispatcherType> types, Map<String, String> initParams) {
        this.path = path;
        this.filter = filter;
        this.types = types;
        this.initParams = initParams;
    }

    public String getPath() {
        return path;
    }

    public Filter getFilter() {
        return filter;
    }
    
    public Map<String, String> getInitParams() {
        return initParams;
    }
    
    public EnumSet<DispatcherType> getTypes() {
        return types;
    }
    
    public FilterSet addInitParam(String key, String value) {
        if (initParams == null) {
            initParams = new HashMap<>();
        }
        
        initParams.put(key, value);
        
        return this;
    }
}
