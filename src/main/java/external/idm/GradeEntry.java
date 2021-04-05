package external.idm;

import java.util.Objects;

public record GradeEntry(String id, String name, String semester, String date,
                         String grade, String points, String status, String ects,
                         String mark, String attempt) {

    @Override
    public String toString() {
        return "GradeEntry{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", semester='" + semester + '\'' +
               ", date='" + date + '\'' +
               ", grade='" + grade + '\'' +
               ", points='" + points + '\'' +
               ", status='" + status + '\'' +
               ", ects='" + ects + '\'' +
               ", mark='" + mark + '\'' +
               ", attempt='" + attempt + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradeEntry that = (GradeEntry) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(semester, that.semester) && Objects.equals(date, that.date) && Objects.equals(grade, that.grade) && Objects.equals(points, that.points) && Objects.equals(status, that.status) && Objects.equals(ects, that.ects) && Objects.equals(mark, that.mark) && Objects.equals(attempt, that.attempt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, semester, date, grade, points, status, ects, mark, attempt);
    }
}
