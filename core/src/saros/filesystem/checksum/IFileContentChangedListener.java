package saros.filesystem.checksum;

public interface IFileContentChangedListener<T> {
  public void fileContentChanged(T file);
}
