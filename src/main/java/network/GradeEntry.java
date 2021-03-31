package network;

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
}
