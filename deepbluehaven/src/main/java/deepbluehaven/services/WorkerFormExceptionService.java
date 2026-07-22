package deepbluehaven.services;

public class WorkerFormExceptionService extends RuntimeException {

    private final String field;

    public WorkerFormExceptionService(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}