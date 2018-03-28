package towel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represent a source code file
 */
class SourceFile {

    private final Path filePath;
    private final File file;

    SourceFile(Path filePath) throws IOException {
        this.filePath = Objects.requireNonNull(filePath).toAbsolutePath();
        if (!filePath.getFileName().toString().endsWith(".twl")) {
            throw new IllegalArgumentException("Source files must end with the '.twl' extension.");
        }

        file = new File(this.filePath.toString());
        assertFile();
    }

    Path getParentDirectory() {
        return filePath.getParent();
    }

    public String getName() {
        return filePath.getFileName().toString();
    }

    public String getNamespace() {
        return getName().replace(".twl", "");
    }

    String readAllContents() throws IOException {
        assertFile();
        byte[] encoded = Files.readAllBytes(filePath);
        return new String(encoded, Charset.forName("utf-8"));
    }

    private String getPathString() {
        return filePath.toString();
    }

    private void assertFile() throws IOException {
        if (!file.isFile()) {
            throw new IOException(String.format("File %s is not a file.", getPathString()));
        }
        if (!file.exists()) {
            throw new IOException(String.format("File %s does not exist.", getPathString()));
        }
        if (!file.canRead()) {
            throw new IOException(String.format("File %s is not readable.", getPathString()));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SourceFile) {
            return ((SourceFile) obj).getPathString().equals(getPathString());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getPathString().hashCode();
    }
}
