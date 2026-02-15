package com.tradebridge.backend.parse;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ParseJobService {

    private final ParseJobStateService parseJobStateService;
    private final ParseJobRunner parseJobRunner;

    public ParseJobService(ParseJobStateService parseJobStateService, ParseJobRunner parseJobRunner) {
        this.parseJobStateService = parseJobStateService;
        this.parseJobRunner = parseJobRunner;
    }

    public String createJob(String draftId) {
        return parseJobStateService.createJob(draftId);
    }

    public void enqueue(String parseJobId) {
        parseJobRunner.runAsync(parseJobId);
    }

    public List<ParseJobResponse> list(String status) {
        return parseJobStateService.list(status);
    }

    public ParseJobResponse requeue(String parseJobId) {
        ParseJobResponse response = parseJobStateService.requeueState(parseJobId);
        enqueue(response.parseJobId());
        return response;
    }
}
