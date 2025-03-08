package cn.techwolf.server.controller;

import cn.techwolf.server.common.ApiResponse;
import cn.techwolf.server.model.Job;
import cn.techwolf.server.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/job")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/list")
    public ApiResponse<Page<Job>> getJobList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Page<Job> jobs = jobService.getJobList(page, size);
            return ApiResponse.success(jobs);
        } catch (Exception e) {
            log.error("获取零工列表失败", e);
            return ApiResponse.error("获取列表失败");
        }
    }

    @GetMapping("/{jobId}")
    public ApiResponse<Job> getJobDetail(@PathVariable Long jobId) {
        try {
            Job job = jobService.getJobDetail(jobId);
            return ApiResponse.success(job);
        } catch (Exception e) {
            log.error("获取职位详情失败: jobId={}", jobId, e);
            return ApiResponse.error("获取详情失败");
        }
    }

    @GetMapping("/search")
    public ApiResponse<Page<Job>> searchJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Page<Job> jobs = jobService.searchJobs(keyword, page, size);
            return ApiResponse.success(jobs);
        } catch (Exception e) {
            log.error("搜索职位失败: keyword={}", keyword, e);
            return ApiResponse.error("搜索失败");
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

    @PostMapping("/upload")
    public ApiResponse<Void> uploadJobFile(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";

            // 验证文件类型
            if (!Arrays.asList("pdf", "doc", "docx", "png").contains(fileExtension)) {
                log.error("不支持的文件类型: {}", fileExtension);
                return ApiResponse.error("不支持的文件类型");
            }

            jobService.processJobFile(file);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("文件上传失败: filename={}", file.getOriginalFilename(), e);
            return ApiResponse.error("文件上传失败");
        }
    }
}