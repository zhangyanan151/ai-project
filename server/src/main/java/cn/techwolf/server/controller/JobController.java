package cn.techwolf.server.controller;

import cn.techwolf.server.common.ApiResponse;
import cn.techwolf.server.model.Job;
import cn.techwolf.server.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/job")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/list")
    public ApiResponse<List<Job>> getJobList() {
        try {
            List<Job> jobs = jobService.getJobList();
            return ApiResponse.success(jobs);
        } catch (Exception e) {
            log.error("获取零工列表失败", e);
            return ApiResponse.success(List.of());
        }
    }

    @PostMapping("/create")
    public ApiResponse<Void> createJob(@RequestBody Job job) {
        try {
            jobService.createJob(job);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("创建零工信息失败", e);
            return ApiResponse.error("创建失败");
        }
    }
}