package xyz.erupt.linq.data.student;

import java.time.LocalDateTime;

public class Student {

    private Long id;

    private String name;

    private Integer age;

    private LocalDateTime enrollmentTime;

    public Student(Long id, String name, Integer age, LocalDateTime enrollmentTime) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.enrollmentTime = enrollmentTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDateTime getEnrollmentTime() {
        return enrollmentTime;
    }

    public void setEnrollmentTime(LocalDateTime enrollmentTime) {
        this.enrollmentTime = enrollmentTime;
    }
}
