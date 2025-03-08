package cn.techwolf.server.service;

import cn.techwolf.server.model.Job;
import cn.techwolf.server.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
}