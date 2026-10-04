package com.chetan.taskflow.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SemanticMemoryConfig {

    @Bean("semanticMemoryVectorStore")
    public VectorStore semanticMemoryVectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel) {

        return PgVectorStore.builder(
                        jdbcTemplate,
                        embeddingModel
                )
                .vectorTableName("semantic_memory")
                .initializeSchema(true)
                .build();
    }
}