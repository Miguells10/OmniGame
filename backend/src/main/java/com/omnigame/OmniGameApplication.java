package com.omnigame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * OmniGame AI — Entry Point
 *
 * <p>AI-First SaaS platform for game modding and technical support.
 * Orchestrates three AI agents: The Harvester (content ingestion),
 * The Collector (RAG chatbot), and The Security Auditor (file scanning).</p>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@SpringBootApplication
@EnableAsync
public class OmniGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmniGameApplication.class, args);
    }
}
