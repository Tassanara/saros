package saros.filesystem.checksum;

import org.eclipse.core.resources.IFile;
import saros.filesystem.ResourceConverter;

public class EclipseFileSystemChecksumCache extends FileSystemChecksumCache<IFile> {

  public EclipseFileSystemChecksumCache(
      IFileContentChangedNotifier<IFile> fileContentChangedNotifier,
      IAbsolutePathResolver<IFile> absolutePathResolver) {

    super(fileContentChangedNotifier, absolutePathResolver);
  }

  @Override
  protected IFile convert(saros.filesystem.IFile file) {
    return ResourceConverter.getDelegate(file);
  }
}
