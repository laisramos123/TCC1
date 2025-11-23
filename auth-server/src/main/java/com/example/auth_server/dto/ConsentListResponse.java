package com.example.auth_server.dto;

import java.util.List;

public class ConsentListResponse {
    private List<Object> data;
    private Object links;
    private Meta meta;
    
    public static class Meta {
        private int totalRecords;
        private int totalPages;
        
        public static Meta builder() { return new Meta(); }
        public Meta totalRecords(int val) { this.totalRecords = val; return this; }
        public Meta totalPages(int val) { this.totalPages = val; return this; }
        public Meta build() { return this; }
    }
    
    public static ConsentListResponse builder() { return new ConsentListResponse(); }
    public ConsentListResponse data(List<Object> val) { this.data = val; return this; }
    public ConsentListResponse links(Object val) { this.links = val; return this; }
    public ConsentListResponse meta(Meta val) { this.meta = val; return this; }
    public ConsentListResponse build() { return this; }
}
