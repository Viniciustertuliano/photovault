package com.PhotoVault.dto.response;

public class PhotographerSummaryDTO {

    private Long id;
    private String name;

    public PhotographerSummaryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
