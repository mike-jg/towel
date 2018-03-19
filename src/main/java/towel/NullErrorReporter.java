package towel;

public class NullErrorReporter implements ErrorReporter {

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
