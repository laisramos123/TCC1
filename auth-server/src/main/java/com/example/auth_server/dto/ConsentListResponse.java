package com.example.auth_server.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentListResponse {

    @JsonProperty("data")
    private List<ConsentResponse.Data> data;

    @JsonProperty("links")
    private Links links;

    @JsonProperty("meta")
    private Meta meta;

    public ConsentListResponse() {
    }

    public List<ConsentResponse.Data> getData() {
        return data;
    }

    public void setData(List<ConsentResponse.Data> data) {
        this.data = data;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Links {

        @JsonProperty("self")
        private String self;

        @JsonProperty("first")
        private String first;

        @JsonProperty("last")
        private String last;

        @JsonProperty("next")
        private String next;

        @JsonProperty("prev")
        private String prev;

        public Links() {
        }

        public String getSelf() {
            return self;
        }

        public void setSelf(String self) {
            this.self = self;
        }

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public String getPrev() {
            return prev;
        }

        public void setPrev(String prev) {
            this.prev = prev;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {

        @JsonProperty("totalRecords")
        private Integer totalRecords;

        @JsonProperty("totalPages")
        private Integer totalPages;

        @JsonProperty("requestDateTime")
        private LocalDateTime requestDateTime;

        public Meta() {
        }

        public Integer getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(Integer totalRecords) {
            this.totalRecords = totalRecords;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public LocalDateTime getRequestDateTime() {
            return requestDateTime;
        }

        public void setRequestDateTime(LocalDateTime requestDateTime) {
            this.requestDateTime = requestDateTime;
        }
    }
}