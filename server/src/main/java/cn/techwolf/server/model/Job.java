package cn.techwolf.server.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "7_job")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;  // 职位名称

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;  // 工作内容

    @Column(nullable = false)
    private String workingTime;  // 工作时间

    @Column(nullable = false)
    private String location;  // 工作地点

    @Column(nullable = false)
    private Double salary;

    @Column(nullable = false)
    private String contactPhone;  // 联系人手机号

    private Long creatorId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}