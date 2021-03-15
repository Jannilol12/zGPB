package discord;

public record DictionaryEntry(String definition, String example, String author, String url, int thumbsUp, int thumbsDown, String date) {

    @Override
    public String toString() {
        return "DictionaryEntry{" +
               "definition='" + definition + '\'' +
               ", example='" + example + '\'' +
               ", author='" + author + '\'' +
               ", url='" + url + '\'' +
               ", thumbsUp=" + thumbsUp +
               ", thumbsDown=" + thumbsDown +
               ", date='" + date + '\'' +
               '}';
    }
}
