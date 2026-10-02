package com.chetan.taskflow.ai.controller;

import com.chetan.taskflow.ai.dto.SimilarityResponse;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;

    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/api/ai/embed")
    public float[] embed(@RequestParam String text) {

        return embeddingModel.embed(text);
    }

    @GetMapping("/api/ai/similarity")
    public SimilarityResponse similarity() {

        float[] vectorA = embeddingModel.embed(
                "Spring Boot is used to build Java applications."
        );

        float[] vectorB = embeddingModel.embed(
                "Spring is a framework for developing Java services."
        );

        float[] vectorC = embeddingModel.embed(
                "Elephants are large mammals."
        );

        double similarityAB = cosineSimilarity(vectorA, vectorB);
        double similarityAC = cosineSimilarity(vectorA, vectorC);

        return new SimilarityResponse(
                similarityAB,
                similarityAC
        );
    }

    private double cosineSimilarity(float[] a, float[] b) {

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < a.length; i++) {

            dotProduct += a[i] * b[i];

            magnitudeA += a[i] * a[i];
            magnitudeB += b[i] * b[i];
        }

        return dotProduct /
                (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }
}