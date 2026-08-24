package hr.algebra.brassbirmingham.engine;

public record ApplyResult(boolean accepted, String message) {

    public static ApplyResult ok(String message) {
        return new ApplyResult(true, message);
    }

    public static ApplyResult rejected(String message) {
        return new ApplyResult(false, message);
    }
}
