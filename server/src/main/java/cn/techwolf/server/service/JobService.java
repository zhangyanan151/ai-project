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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Slf4j
@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GptService gptService;

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
            // 需要内存分页
            if (matchedJobs.size() < page * size) {
                page = 1; // 重置页码为第一页，避免越界
            }
            int start = Math.max(((page - 1) * size), 0);
            int end = Math.min(start + size, matchedJobs.size());
            matchedJobs = matchedJobs.subList(start, end);
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

    public void processJobFile(String email, MultipartFile file) throws Exception {
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
        } else if ("png".equals(fileExtension)) {
            BufferedImage image = ImageIO.read(file.getInputStream());
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("/usr/local/share/tessdata"); // 设置Tesseract数据文件路径
            tesseract.setLanguage("chi_sim"); // 设置中文识别
            try {
                text = tesseract.doOCR(image);
            } catch (TesseractException e) {
                log.error("OCR识别失败: {}", e.getMessage());
                throw new RuntimeException("图片识别失败");
            }
        } else {
            throw new RuntimeException("不支持的文件类型");
        }
        log.info("开始处理文件: {}", text);
        List<GptService.GptMessage> messages = new ArrayList<>();
        messages.add(new GptService.GptMessage("system", "帮我把这段文字按照职位名，工作内容，工作时间，工作地点，联系方式这几个字段提取出来。并以JSON格式返回给我，json中职位名为title，工作内容为description，工作时间为workingTime，工作地点为location，联系方式为contactPhone。如果找不到某个字段，则该字段可以为空字符串。返回的json串务必是标准的格式"));
        messages.add(new GptService.GptMessage("user", text));
        List<Job> jobs = gptService.requestGPT(messages, 500);
        log.info("GPT返回结果: {}", jobs);
        if(null == jobs || jobs.isEmpty()) {
            jobs = extractJobInfoList(text);
        }
        for (Job job : jobs) {
            log.info("解析出的职位: {}", job);
            createJob(email, job);
        }
        log.info("文件处理完成，共导入{}条职位信息: {}", jobs.size(), originalFilename);
    }

    public void processText(String email, String text) throws Exception {
        log.info("开始处理文本: {}", text);
        List<GptService.GptMessage> messages = new ArrayList<>();
        messages.add(new GptService.GptMessage("system", "帮我把这段文字按照职位名，工作内容，工作时间，工作地点，联系方式这几个字段提取出来。并以JSON格式返回给我，json中职位名为title，工作内容为description，工作时间为workingTime，工作地点为location，联系方式为contactPhone。如果找不到某个字段，则该字段可以为空字符串。返回的json串务必是标准的格式"));
        messages.add(new GptService.GptMessage("user", text));
        List<Job> jobs = gptService.requestGPT(messages, 500);
        log.info("GPT返回结果: {}", jobs);
        for (Job job : jobs) {
            createJob(email, job);
        }
        log.info("文本处理完成，共导入{}条职位信息", jobs.size());
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