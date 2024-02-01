package xyz.erupt.linq.data;

import java.time.LocalDateTime;

public class StudentScore {

    private Long studentId;

    private Long subjectId;

    private Integer score;

    private LocalDateTime createdAt;

    public StudentScore(Long studentId, Long subjectId, Integer score, LocalDateTime createdAt) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.score = score;
        this.createdAt = createdAt;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
