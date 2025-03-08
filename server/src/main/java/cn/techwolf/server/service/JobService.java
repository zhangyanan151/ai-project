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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

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

        Job job = extractJobInfo(text);
        createJob(email, job);
        log.info("文件处理完成: {}", originalFilename);
    }

    private Job extractJobInfo(String text) {
        Job job = new Job();
        extractJobTitle(text, job);
        extractWorkContent(text, job);
        extractWorkTime(text, job);
        extractLocation(text, job);
        extractContact(text, job);

        if (!isValidJob(job)) {
            throw new RuntimeException("职位信息不完整");
        }

        return job;
    }

    private void extractJobTitle(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:招聘|职位|岗位)[：:\\s]*([^，。\n]{2,20})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setTitle(matcher.group(1));
        }
    }

    private void extractWorkContent(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:工作内容|岗位职责|工作职责|主要工作)[：:\\s]*([^。]*?。)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setDescription(matcher.group(1));
        }
    }

    private void extractWorkTime(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:工作时间|上班时间)[：:\\s]*([^。\n]{2,50})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setWorkingTime(matcher.group(1));
        }
    }

    private void extractLocation(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:工作地点|工作地址|地址|地点)[：:\\s]*([^，。\n]{2,50})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setLocation(matcher.group(1));
        }
    }

    private void extractContact(String text, Job job) {
        Pattern pattern = Pattern.compile("(?:联系电话|手机号|电话|联系方式)[：:\\s]*(\\d{11})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setContactPhone(matcher.group(1));
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