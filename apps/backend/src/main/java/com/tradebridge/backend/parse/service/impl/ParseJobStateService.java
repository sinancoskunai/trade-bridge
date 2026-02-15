package com.tradebridge.backend.parse.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.common.ApiException;
import com.tradebridge.backend.parse.model.ParseJobData;
import com.tradebridge.backend.parse.model.ParseJobResponse;
import com.tradebridge.backend.parse.model.ParseStatuses;
import com.tradebridge.backend.product.persistence.entity.ParseJobEntity;
import com.tradebridge.backend.product.persistence.repository.ParseJobRepository;

@Service
public class ParseJobStateService {

    private final ParseJobRepository parseJobRepository;

    public ParseJobStateService(ParseJobRepository parseJobRepository) {
        this.parseJobRepository = parseJobRepository;
    }

    public String createJob(String draftId) {
        ParseJobEntity job = new ParseJobEntity();
        job.setId(UUID.randomUUID().toString());
        job.setDraftId(draftId);
        job.setStatus(ParseStatuses.PENDING);
        job.setAttempts(0);
        job.setLastError(null);
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        parseJobRepository.save(job);
        return job.getId();
    }

    public ParseJobData markParsing(String parseJobId) {
        ParseJobEntity job = parseJobRepository.findById(parseJobId).orElse(null);
        if (job == null) {
            return null;
        }

        job.setStatus(ParseStatuses.PARSING);
        job.setAttempts(job.getAttempts() + 1);
        job.setStartedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        job.setLastError(null);
        parseJobRepository.save(job);

        return new ParseJobData(job.getId(), job.getDraftId(), job.getAttempts());
    }

    public void markFinished(String parseJobId, boolean reviewRequired) {
        ParseJobEntity job = parseJobRepository.findById(parseJobId).orElse(null);
        if (job == null) {
            return;
        }
        job.setStatus(reviewRequired ? ParseStatuses.REVIEW_REQUIRED : ParseStatuses.COMPLETED);
        job.setUpdatedAt(Instant.now());
        job.setFinishedAt(Instant.now());
        job.setLastError(null);
        parseJobRepository.save(job);
    }

    public void markFailed(String parseJobId, String error) {
        ParseJobEntity job = parseJobRepository.findById(parseJobId).orElse(null);
        if (job == null) {
            return;
        }
        job.setStatus(ParseStatuses.FAILED);
        job.setUpdatedAt(Instant.now());
        job.setFinishedAt(Instant.now());
        job.setLastError(error);
        parseJobRepository.save(job);
    }

    public List<ParseJobResponse> list(String status) {
        List<ParseJobEntity> jobs = (status == null || status.isBlank())
                ? parseJobRepository.findAllByOrderByCreatedAtDesc()
                : parseJobRepository.findByStatusOrderByCreatedAtDesc(status);
        return jobs.stream().map(this::toResponse).toList();
    }

    public ParseJobResponse requeueState(String parseJobId) {
        ParseJobEntity job = parseJobRepository.findById(parseJobId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Parse job not found"));

        job.setStatus(ParseStatuses.PENDING);
        job.setLastError(null);
        job.setStartedAt(null);
        job.setFinishedAt(null);
        job.setUpdatedAt(Instant.now());
        parseJobRepository.save(job);

        return toResponse(job);
    }

    private ParseJobResponse toResponse(ParseJobEntity entity) {
        return new ParseJobResponse(
                entity.getId(),
                entity.getDraftId(),
                entity.getStatus(),
                entity.getAttempts(),
                entity.getLastError(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString(),
                entity.getStartedAt() == null ? null : entity.getStartedAt().toString(),
                entity.getFinishedAt() == null ? null : entity.getFinishedAt().toString());
    }
}
