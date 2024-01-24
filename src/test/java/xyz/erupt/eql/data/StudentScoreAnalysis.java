package xyz.erupt.eql.data;

public class StudentScoreAnalysis {

    private String name;

    private Integer totalScore;

    private Float avgScore;

    private Float maxScore;

    private Integer minScore;

    private Integer subjectCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Float getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(Float avgScore) {
        this.avgScore = avgScore;
    }

    public Float getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Float maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getMinScore() {
        return minScore;
    }

    public void setMinScore(Integer minScore) {
        this.minScore = minScore;
    }

    public Integer getSubjectCount() {
        return subjectCount;
    }

    public void setSubjectCount(Integer subjectCount) {
        this.subjectCount = subjectCount;
    }

}
