package com.tradebridge.backend.parse.service;

import java.util.List;

import com.tradebridge.backend.parse.model.ParseJobResponse;

public interface ParseJobApplicationService {

    String createJob(String draftId);

    void enqueue(String parseJobId);

    List<ParseJobResponse> list(String status);

    ParseJobResponse requeue(String parseJobId);
}
