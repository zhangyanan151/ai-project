package cn.techwolf.server.service;

import cn.techwolf.server.model.Job;
import cn.techwolf.server.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public List<Job> getJobList() {
        try {
            return jobRepository.findAll();
        } catch (Exception e) {
            log.error("获取零工列表失败", e);
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
}