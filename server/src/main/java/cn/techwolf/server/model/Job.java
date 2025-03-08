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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}