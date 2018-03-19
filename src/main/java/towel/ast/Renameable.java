package towel.ast;

public interface Renameable {
    String getOriginalName();
    String getName();
    void setName(String lookupName);
}
