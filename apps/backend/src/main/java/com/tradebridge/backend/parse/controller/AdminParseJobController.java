package com.tradebridge.backend.parse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradebridge.backend.parse.model.ParseJobResponse;
import com.tradebridge.backend.parse.service.ParseJobApplicationService;

@RestController
@RequestMapping("/admin/parse-jobs")
public class AdminParseJobController {

    private final ParseJobApplicationService parseJobService;

    public AdminParseJobController(ParseJobApplicationService parseJobService) {
        this.parseJobService = parseJobService;
    }

    @GetMapping
    public List<ParseJobResponse> list(@RequestParam(required = false) String status) {
        return parseJobService.list(status);
    }

    @PostMapping("/{parseJobId}/requeue")
    public ParseJobResponse requeue(@PathVariable String parseJobId) {
        return parseJobService.requeue(parseJobId);
    }
}
