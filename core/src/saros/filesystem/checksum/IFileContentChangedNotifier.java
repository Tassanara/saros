package saros.filesystem.checksum;

public interface IFileContentChangedNotifier<T> {

  public void addFileContentChangedListener(IFileContentChangedListener<T> listener);

  public void removeFileContentChangedListener(IFileContentChangedListener<T> listener);
}
