package com.example.mini_project_ss14.llmops.service;

public class InfiniteLoopGuardException extends RuntimeException {

    public InfiniteLoopGuardException(String message) {
        super(message);
    }
}
