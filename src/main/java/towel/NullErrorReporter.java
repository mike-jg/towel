package towel;

public class NullErrorReporter implements ErrorReporter {

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

    }

    @Override
    public void notice(String message, int line, int character) {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void error(String message, int line, int character) {

    }
}
