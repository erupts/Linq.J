package xyz.erupt.linq.data.student;

public class StudentVo {

    private String name;

    private String subjectName;

    private Integer score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "StudentVo{" +
                "name='" + name + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", score=" + score +
                '}';
    }
}
