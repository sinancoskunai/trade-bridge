package com.tradebridge.backend.parse;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ParseJobService implements ParseJobApplicationService {

    private final ParseJobStateService parseJobStateService;
    private final ParseJobRunner parseJobRunner;

    public ParseJobService(ParseJobStateService parseJobStateService, ParseJobRunner parseJobRunner) {
        this.parseJobStateService = parseJobStateService;
        this.parseJobRunner = parseJobRunner;
    }

    @Override
    public String createJob(String draftId) {
        return parseJobStateService.createJob(draftId);
    }

    @Override
    public void enqueue(String parseJobId) {
        parseJobRunner.runAsync(parseJobId);
    }

    @Override
    public List<ParseJobResponse> list(String status) {
        return parseJobStateService.list(status);
    }

    @Override
    public ParseJobResponse requeue(String parseJobId) {
        ParseJobResponse response = parseJobStateService.requeueState(parseJobId);
        enqueue(response.parseJobId());
        return response;
    }
}
