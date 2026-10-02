package com.chetan.taskflow.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai/knowledge")
public class KnowledgeController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public KnowledgeController(
            VectorStore vectorStore,
            ChatClient.Builder chatClientBuilder) {

        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(2)
                        .build()
        );

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        return chatClient
                .prompt()
                .system("""
                    You are the TaskFlow AI assistant.

                    Answer the user's question using only the
                    information provided in the context below.

                    If the answer cannot be determined from the
                    context, say that you do not have enough
                    information.

                    CONTEXT:

                    %s
                    """.formatted(context))
                .user(question)
                .call()
                .content();
    }

    @PostMapping("/load")
    public String loadKnowledge() {

        List<Document> documents = List.of(

                new Document("""
                        TaskFlow uses JWT-based authentication.
                        Authentication tokens expire after 60 minutes.
                        """),

                new Document("""
                        TaskFlow uses PostgreSQL as its relational database.
                        """),

                new Document("""
                        TaskFlow consists of an Auth Service,
                        Task Service and AI Service.
                        """)
        );

        vectorStore.add(documents);

        return "TaskFlow knowledge loaded successfully";
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query) {

        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(2)
                        .build()
        );
    }
}