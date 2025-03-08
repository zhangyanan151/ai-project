package cn.techwolf.server.repository;

import cn.techwolf.server.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    // 根据职位标题模糊查询
    List<Job> findByTitleContaining(String title);
    
    // 根据工作地点查询
    List<Job> findByLocation(String location);
    
    // 根据薪资范围查询
    List<Job> findBySalaryBetween(Integer minSalary, Integer maxSalary);
}