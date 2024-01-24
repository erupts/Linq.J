package xyz.erupt.eql;

import org.junit.Before;
import org.junit.Test;
import xyz.erupt.eql.data.*;
import xyz.erupt.eql.util.Columns;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentDatasetTest {

    private final List<Student> students = new ArrayList<>();

    private final List<StudentScore> studentScores = new ArrayList<>();

    private final List<StudentSubject> studentSubjects = new ArrayList<>();


    @Before
    public void before() throws IOException {
        students.add(new Student(1L, "Genevieve", 18, LocalDateTime.now().minusYears(3)));
        students.add(new Student(2L, "Liz", 18, LocalDateTime.now().minusYears(5)));
        students.add(new Student(3L, "Berg", 33, LocalDateTime.now().minusYears(9)));
        students.add(new Student(4L, "Milo", 21, LocalDateTime.now().minusYears(2)));
        students.add(new Student(5L, "Thanos", 19, LocalDateTime.now().minusYears(7)));

        studentSubjects.add(new StudentSubject(1L, "math"));
        studentSubjects.add(new StudentSubject(2L, "physics"));
        studentSubjects.add(new StudentSubject(3L, "computer"));

        studentScores.add(new StudentScore(1L, 1L, 50, LocalDateTime.now().minusDays(90)));
        studentScores.add(new StudentScore(1L, 2L, 70, LocalDateTime.now().minusDays(232)));
        studentScores.add(new StudentScore(1L, 3L, 76, LocalDateTime.now().minusDays(21)));
        studentScores.add(new StudentScore(2L, 1L, 11, LocalDateTime.now().minusDays(90)));
        studentScores.add(new StudentScore(2L, 2L, 54, LocalDateTime.now().minusDays(33)));
        studentScores.add(new StudentScore(2L, 3L, 76, LocalDateTime.now().minusDays(123)));
        studentScores.add(new StudentScore(3L, 1L, 43, LocalDateTime.now().minusDays(4)));
        studentScores.add(new StudentScore(3L, 2L, 90, LocalDateTime.now().minusDays(34)));
        studentScores.add(new StudentScore(3L, 3L, 54, LocalDateTime.now().minusDays(55)));
        studentScores.add(new StudentScore(4L, 1L, 50, LocalDateTime.now().minusDays(655)));
        studentScores.add(new StudentScore(4L, 2L, 102, LocalDateTime.now().minusDays(12)));
        studentScores.add(new StudentScore(4L, 3L, 121, LocalDateTime.now().minusDays(22)));

        studentScores.add(new StudentScore(5L, 3L, 8, LocalDateTime.now().minusDays(22)));
    }

    @Test
    public void joinTest() {
        List<StudentVo> studentVos = Linq.from(students)
                .innerJoin(studentScores, StudentScore::getStudentId, Student::getId)
                .innerJoin(studentSubjects, StudentSubject::getId, StudentScore::getSubjectId)
                .select(
                        Columns.of(Student::getName, StudentVo::getName),
                        Columns.of(StudentSubject::getName, StudentVo::getSubjectName),
                        Columns.of(StudentScore::getScore, StudentVo::getScore)
                )
                .write(StudentVo.class);
        assert studentScores.size() == studentVos.size();
    }

    @Test
    public void groupTest() {
        List<StudentScoreAnalysis> studentVos = Linq.from(students)
                .innerJoin(studentScores, StudentScore::getStudentId, Student::getId)
                .innerJoin(studentSubjects, StudentSubject::getId, StudentScore::getSubjectId)
                .groupBy(Columns.of(Student::getName))
                .select(
                        Columns.of(Student::getName, StudentScoreAnalysis::getName),
                        Columns.avg(StudentScore::getScore, StudentScoreAnalysis::getAvgScore),
                        Columns.max(StudentScore::getScore, StudentScoreAnalysis::getMaxScore),
                        Columns.min(StudentScore::getScore, StudentScoreAnalysis::getMinScore),
                        Columns.sum(StudentScore::getScore, StudentScoreAnalysis::getTotalScore),
                        Columns.count(StudentScoreAnalysis::getSubjectCount)
                )
                .write(StudentScoreAnalysis.class);
        System.out.println(studentVos);
    }

}
