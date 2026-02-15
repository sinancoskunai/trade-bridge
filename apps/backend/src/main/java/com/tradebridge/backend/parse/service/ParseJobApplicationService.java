package com.tradebridge.backend.parse;

import java.util.List;

public interface ParseJobApplicationService {

    String createJob(String draftId);

    void enqueue(String parseJobId);

    List<ParseJobResponse> list(String status);

    ParseJobResponse requeue(String parseJobId);
}
