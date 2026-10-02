package com.chetan.taskflow.ai.dto;

public record SimilarityResponse(
        double similarityAB,
        double similarityAC
) {
}