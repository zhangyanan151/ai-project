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
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            page = Math.max(page - 1, 0);
            Page<Job> jobs = jobService.getJobList(email, page, size);
            return ApiResponse.success(jobs);
        } catch (Exception e) {
            log.error("获取零工列表失败: email={}", email, e);
            return ApiResponse.error("获取列表失败");
        }
    }

    @GetMapping("/{jobId}")
    public ApiResponse<Job> getJobDetail(
            @RequestParam String email,
            @PathVariable Long jobId) {
        try {
            Job job = jobService.getJobDetail(email, jobId);
            return ApiResponse.success(job);
        } catch (Exception e) {
            log.error("获取职位详情失败: jobId={}, email={}", jobId, email, e);
            return ApiResponse.error("获取详情失败");
        }
    }

    @GetMapping("/search")
    public ApiResponse<Page<Job>> searchJobs(
            @RequestParam String email,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Page<Job> jobs = jobService.searchJobs(email, keyword, page, size);
            return ApiResponse.success(jobs);
        } catch (Exception e) {
            log.error("搜索职位失败: keyword={}, email={}", keyword, email, e);
            return ApiResponse.error("搜索失败");
        }
    }

    @PostMapping("/create")
    public ApiResponse<Void> createJob(
            @RequestParam String email,
            @RequestBody Job job) {
        try {
            jobService.createJob(email, job);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("创建零工信息失败: email={}", email, e);
            return ApiResponse.error("创建失败");
        }
    }

    @PostMapping("/upload")
    public ApiResponse<Boolean> uploadJobFile(
            @RequestParam String email,
            @RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";
            boolean check = Arrays.asList("pdf", "doc", "docx", "png").contains(fileExtension);
            log.info("上传文件: filename={}, email={}， check={}", file.getOriginalFilename(), email, check);
            // 验证文件类型
            if (!check) {
                log.error("不支持的文件类型: {}", fileExtension);
                return ApiResponse.error("不支持的文件类型");
            }
            Job job = new Job();
            job.setTitle("灵活用工" + System.currentTimeMillis());
            job.setWorkingTime("早上11点 到我晚上10点");
            job.setLocation("上海");
            job.setSalary(1000.0);
            job.setDescription("我是一个灵活用工，我可以在早上11点 到我晚上10点");
            job.setContactPhone("1234567890");
            jobService.createJob(email, job);
//            jobService.processJobFile(email, file);
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("文件上传失败: filename={}, email={}", file.getOriginalFilename(), email, e);
            return ApiResponse.error("文件上传失败");
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.asList("pdf", "doc", "docx", "png").contains("png"));
    }
}