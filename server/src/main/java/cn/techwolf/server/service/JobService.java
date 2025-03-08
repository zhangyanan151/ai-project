package cn.techwolf.server.service;

import cn.techwolf.server.model.Job;
import cn.techwolf.server.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    private final Random random = new Random();

    public Page<Job> getJobList(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<Job> allJobs = jobRepository.findAll();
            
            // 模拟随机返回测试数据
            List<Job> randomJobs = getRandomJobs(allJobs, size);
            return new org.springframework.data.domain.PageImpl<>(randomJobs, pageable, allJobs.size());
        } catch (Exception e) {
            log.error("获取零工列表失败", e);
            throw e;
        }
    }

    public Job getJobDetail(Long jobId) {
        try {
            return jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("职位不存在"));
        } catch (Exception e) {
            log.error("获取职位详情失败: jobId={}", jobId, e);
            throw e;
        }
    }

    public Page<Job> searchJobs(String keyword, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<Job> allJobs = jobRepository.findAll();
            
            // 模拟基于关键词的智能搜索
            List<Job> searchResults = searchJobsByNLP(allJobs, keyword);
            return new org.springframework.data.domain.PageImpl<>(searchResults, pageable, searchResults.size());
        } catch (Exception e) {
            log.error("搜索职位失败: keyword={}", keyword, e);
            throw e;
        }
    }

    public void createJob(Job job) {
        try {
            jobRepository.save(job);
            log.info("创建零工信息成功: title={}", job.getTitle());
        } catch (Exception e) {
            log.error("创建零工信息失败: title={}", job.getTitle(), e);
            throw e;
        }
    }

    private List<Job> getRandomJobs(List<Job> allJobs, int size) {
        List<Job> randomJobs = new ArrayList<>();
        int totalSize = allJobs.size();
        if (totalSize == 0) return randomJobs;

        // 随机选择指定数量的职位
        for (int i = 0; i < Math.min(size, totalSize); i++) {
            int randomIndex = random.nextInt(allJobs.size());
            randomJobs.add(allJobs.get(randomIndex));
            allJobs.remove(randomIndex);
        }
        return randomJobs;
    }

    private List<Job> searchJobsByNLP(List<Job> allJobs, String keyword) {
        // 模拟自然语言搜索，这里简单实现为关键词匹配
        // TODO: 集成真实的NLP服务
        return allJobs.stream()
                .filter(job -> matchesKeyword(job, keyword))
                .toList();
    }

    private boolean matchesKeyword(Job job, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        keyword = keyword.toLowerCase();
        return job.getTitle().toLowerCase().contains(keyword) ||
                job.getDescription().toLowerCase().contains(keyword) ||
                job.getLocation().toLowerCase().contains(keyword);
    }
    
    public void processJobFile(MultipartFile file) throws IOException {
        try {
            // TODO: 实现文件内容解析和数据存储
            String originalFilename = file.getOriginalFilename();
            log.info("开始处理文件: {}", originalFilename);
            
            // 根据文件类型选择不同的解析策略
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";
            
            switch (fileExtension) {
                case "pdf":
                    processPdfFile(file);
                    break;
                case "doc":
                case "docx":
                    processWordFile(file);
                    break;
                case "png":
                    processImageFile(file);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的文件类型");
            }
            
            log.info("文件处理完成: {}", originalFilename);
        } catch (Exception e) {
            log.error("文件处理失败", e);
            throw e;
        }
    }
    
    private void processPdfFile(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        
        Job job = extractJobInfo(text);
        if (job != null) {
            createJob(job);
            log.info("PDF文件解析完成并保存职位信息: {}", job.getTitle());
        }
    }
    
    private void processWordFile(MultipartFile file) throws IOException {
        XWPFDocument document = new XWPFDocument(file.getInputStream());
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);
        String text = extractor.getText();
        extractor.close();
        document.close();
        
        Job job = extractJobInfo(text);
        if (job != null) {
            createJob(job);
            log.info("Word文件解析完成并保存职位信息: {}", job.getTitle());
        }
    }
    
    private void processImageFile(MultipartFile file) throws IOException {
        // 使用临时文件存储上传的图片
        File tempFile = File.createTempFile("upload_", "_temp");
        file.transferTo(tempFile);
        

    }
    
    private Job extractJobInfo(String text) {
        Job job = new Job();
        
        // 使用正则表达式提取关键信息
        extractJobTitle(text, job);
        extractWorkContent(text, job);
        extractWorkTime(text, job);
        extractLocation(text, job);
        extractContact(text, job);
        
        return isValidJob(job) ? job : null;
    }
    
    private void extractJobTitle(String text, Job job) {
        // 匹配职位名称的常见模式
        Pattern pattern = Pattern.compile("(?:招聘|职位|岗位)[：:\\s]*([^，。\\n]{2,20})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setTitle(matcher.group(1).trim());
        }
    }
    
    private void extractWorkContent(String text, Job job) {
        // 匹配工作内容的常见模式
        Pattern pattern = Pattern.compile("(?:工作内容|岗位职责|工作职责|主要工作)[：:\\s]*([^。]*?。)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setDescription(matcher.group(1).trim());
        }
    }
    
    private void extractWorkTime(String text, Job job) {
        // 匹配工作时间的常见模式
        Pattern pattern = Pattern.compile("(?:工作时间|上班时间)[：:\\s]*([^。\\n]{2,50})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setWorkingTime(matcher.group(1).trim());
        }
    }
    
    private void extractLocation(String text, Job job) {
        // 匹配工作地点的常见模式
        Pattern pattern = Pattern.compile("(?:工作地点|工作地址|地址|地点)[：:\\s]*([^，。\\n]{2,50})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            job.setLocation(matcher.group(1).trim());
        }
    }
    
    private void extractContact(String text, Job job) {
        // 匹配联系人手机号的常见模式
        Pattern pattern = Pattern.compile("(?:联系电话|手机号|电话|联系方式)[：:\\s]*(\\d{11})");
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