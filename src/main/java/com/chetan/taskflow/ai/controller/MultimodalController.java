package com.chetan.taskflow.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/multimodal")
public class MultimodalController {

    private final ChatClient chatClient;

    public MultimodalController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping(
            value = "/analyze-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String analyzeImage(
            @RequestParam("prompt") String prompt,
            @RequestParam("image") MultipartFile image)
            throws IOException {

        MimeType mimeType =
                image.getContentType() != null
                        ? MimeTypeUtils.parseMimeType(image.getContentType())
                        : MimeTypeUtils.IMAGE_PNG;

        Media media = Media.builder()
                .mimeType(mimeType)
                .data(image.getBytes())
                .build();

        return chatClient
                .prompt()
                .user(user -> user
                        .text(prompt)
                        .media(media))
                .call()
                .content();
    }
}