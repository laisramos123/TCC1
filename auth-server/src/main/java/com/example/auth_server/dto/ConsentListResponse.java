package com.example.auth_server.dto;

import java.util.List;

public class ConsentListResponse {
    private List<ConsentResponse.Data> data;
    private Object links;
    private Meta meta;
    
    public static class Meta {
        private int totalRecords;
        private int totalPages;
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
    
    public List<ConsentResponse.Data> getData() { return data; }
    public void setData(List<ConsentResponse.Data> data) { this.data = data; }
    public Object getLinks() { return links; }
    public void setLinks(Object links) { this.links = links; }
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
}
