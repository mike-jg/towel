package towel;

public class ExceptionThrowingErrorReporter implements ErrorReporter {

    @Override
    public void notice(String message) {
        throw new RuntimeException(message);
    }

    @Override
    public void notice(String message, int line, int character) {
        throw new RuntimeException(message);
    }

    @Override
    public void error(String message) {
        throw new RuntimeException(message);
    }

    @Override
    public void error(String message, int line, int character) {
        throw new RuntimeException(message);
    }

    public static class IgnoreNoticesErrorReporter extends ExceptionThrowingErrorReporter {
        @Override
        public void notice(String message) {

        }

        @Override
        public void notice(String message, int line, int character) {

        }
    }
}
