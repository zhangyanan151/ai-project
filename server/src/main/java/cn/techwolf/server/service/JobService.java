package cn.techwolf.server.service;

import cn.techwolf.server.model.Job;
import cn.techwolf.server.model.User;
import cn.techwolf.server.repository.JobRepository;
import cn.techwolf.server.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    public Page<Job> getJobList(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobRepository.findAll(pageable);
    }

    public Job getJobDetail(String email, Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("职位不存在"));
    }

    public Page<Job> searchJobs(String email, String keyword, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<Job> allJobs = jobRepository.findAll();
            List<Job> matchedJobs = allJobs.stream()
                    .filter(job -> matchesKeyword(job, keyword))
                    .toList();
            return new PageImpl<>(matchedJobs, pageable, matchedJobs.size());
        } catch (Exception e) {
            log.error("搜索职位失败: keyword={}", keyword, e);
            return Page.empty();
        }
    }

    private boolean matchesKeyword(Job job, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }

        String[] keywords = keyword.toLowerCase().split("\\s+");
        String jobText = String.join(" ",
                job.getTitle() != null ? job.getTitle().toLowerCase() : "",
                job.getDescription() != null ? job.getDescription().toLowerCase() : "",
                job.getLocation() != null ? job.getLocation().toLowerCase() : "",
                job.getWorkingTime() != null ? job.getWorkingTime().toLowerCase() : ""
        );

        return Arrays.stream(keywords)
                .allMatch(kw -> jobText.contains(kw));
    }

    public void createJob(String email, Job job) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        job.setCreatorId(user.getId());
        jobRepository.save(job);
    }

    public void processJobFile(String email, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件名不能为空");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        String text;

        if ("pdf".equals(fileExtension)) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(document);
            }
        } else if ("doc".equals(fileExtension) || "docx".equals(fileExtension)) {
            try (XWPFDocument document = new XWPFDocument(file.getInputStream());
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                text = extractor.getText();
            }
        } else {
            throw new RuntimeException("不支持的文件类型");
        }
        log.info("开始处理文件: {}", text);
        List<Job> jobs = extractJobInfoList(text);
        for (Job job : jobs) {
            createJob(email, job);
        }
        log.info("文件处理完成，共导入{}条职位信息: {}", jobs.size(), originalFilename);
    }

    private List<Job> extractJobInfoList(String text) {
        List<Job> jobs = new ArrayList<>();
        Pattern jobPattern = Pattern.compile("(?:职位名|岗位名)[：:\\s]*([^。]*?。|[^\n]*\n)[\\s\\S]*?(?:(?:联系电话|手机号|电话|联系方式|联系人)[：:\\s]*([^。]*?。|[^\n]*\n))");
        Matcher jobMatcher = jobPattern.matcher(text);

        while (jobMatcher.find()) {
            String jobBlock = jobMatcher.group(0);
            Job job = new Job();
            extractJobTitle(jobBlock, job);
            extractWorkContent(jobBlock, job);
            extractWorkTime(jobBlock, job);
            extractLocation(jobBlock, job);
            extractContact(jobBlock, job);

            if (isValidJob(job)) {
                jobs.add(job);
            }
        }

        if (jobs.isEmpty()) {
            throw new RuntimeException("未能从文件中提取到有效的职位信息");
        }

        return jobs;
    }

    private void extractJobTitle(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:职位名|岗位名)[：:\\s]*([^。]*?。|[^\n]*\n)");
        //过滤掉一些特殊字符，例如\n
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setTitle(matcher.group(1).replaceAll("\n", ""));
        }
    }

    private void extractWorkContent(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:工作内容|岗位职责|主要工作)[：:\\s]*([^。]*?。|[^\n]*\n)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setDescription(matcher.group(1).trim());
        }
    }

    private void extractWorkTime(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:工作时间|上班时间|工作班次)[：:\\s]*([^。]*?。|[^\n]*\n)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setWorkingTime(matcher.group(1).trim());
        }
    }

    private void extractLocation(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:工作地点|工作地址|地址|地点|工作区域)[：:\\s]*([^。]*?。|[^\n]*\n)");

        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setLocation(matcher.group(1).trim());
        }
    }

    private void extractContact(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:联系电话|手机号|电话|联系方式|联系人)[：:\\s]*([^。]*?。|[^\n]*\n)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setContactPhone(matcher.group(1).trim());
        }
    }

    private boolean isValidJob(Job job) {
        return job.getTitle() != null &&
                job.getDescription() != null &&
                job.getWorkingTime() != null &&
                job.getLocation() != null &&
                job.getContactPhone() != null;
    }
}