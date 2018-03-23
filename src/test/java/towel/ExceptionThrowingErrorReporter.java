package towel;

/**
 * This just means the tests will fail immediately if the reporter receives something it shouldn't.
 *
 * Not the best approach because because when the test does fail, it's difficult to figure out why sometimes.
 *
 * @todo do something better for unit tests, so the error is easier to locate
 */
public class ExceptionThrowingErrorReporter implements ErrorReporter {

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public boolean hasNotices() {
        return false;
    }

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
