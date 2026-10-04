package com.chetan.taskflow.ai.memory;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class SemanticMemoryService {

    private final VectorStore vectorStore;

    public SemanticMemoryService(
            @Qualifier("semanticMemoryVectorStore")
            VectorStore vectorStore) {

        this.vectorStore = vectorStore;
    }

    public void remember(
            Long userId,
            String memory) {

        Document document =
                new Document(
                        memory,
                        Map.of(
                                "userId", userId,
                                "type", "user_memory",
                                "createdAt",
                                Instant.now().toString()
                        )
                );

        vectorStore.add(
                List.of(document)
        );
    }

    public List<Document> recall(
            Long userId,
            String query) {

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .topK(5)

                        /*
                         * CRITICAL:
                         *
                         * Never search memories belonging
                         * to every user.
                         */
                        .filterExpression(
                                "userId == " + userId
                        )
                        .build();

        return vectorStore.similaritySearch(
                searchRequest
        );
    }
}