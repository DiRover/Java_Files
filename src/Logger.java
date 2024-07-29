public class Logger {

    StringBuilder records = new StringBuilder();

    public void recordAction(String record) {
        this.records.append(record);
        this.records.append("\n");
    }

    public String getRecords() {
        return records.toString();
    }
}
