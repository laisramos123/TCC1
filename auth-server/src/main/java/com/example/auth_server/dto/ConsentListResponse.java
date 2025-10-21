package com.example.auth_server.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentListResponse {
    @JsonProperty("data")
    private List<ConsentResponse.Data> data;

    @JsonProperty("links")
    private ConsentResponse.Links links;

    @JsonProperty("meta")
    private ConsentResponse.Meta meta;
}
