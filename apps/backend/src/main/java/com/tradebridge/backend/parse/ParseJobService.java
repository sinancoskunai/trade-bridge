package com.tradebridge.backend.parse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.common.ApiException;
import com.tradebridge.backend.product.persistence.ParseJobEntity;
import com.tradebridge.backend.product.persistence.ParseJobRepository;

@Service
public class ParseJobService {

    private final ParseJobRepository parseJobRepository;
    private final ParseJobRunner parseJobRunner;

    public ParseJobService(ParseJobRepository parseJobRepository, ParseJobRunner parseJobRunner) {
        this.parseJobRepository = parseJobRepository;
        this.parseJobRunner = parseJobRunner;
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

    public void enqueue(String parseJobId) {
        parseJobRunner.runAsync(parseJobId);
    }

    public List<ParseJobResponse> list(String status) {
        List<ParseJobEntity> jobs = (status == null || status.isBlank())
                ? parseJobRepository.findAllByOrderByCreatedAtDesc()
                : parseJobRepository.findByStatusOrderByCreatedAtDesc(status);

        return jobs.stream().map(this::toResponse).toList();
    }

    public ParseJobResponse requeue(String parseJobId) {
        ParseJobEntity job = parseJobRepository.findById(parseJobId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Parse job not found"));

        job.setStatus(ParseStatuses.PENDING);
        job.setLastError(null);
        job.setStartedAt(null);
        job.setFinishedAt(null);
        job.setUpdatedAt(Instant.now());
        parseJobRepository.save(job);

        enqueue(job.getId());
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
