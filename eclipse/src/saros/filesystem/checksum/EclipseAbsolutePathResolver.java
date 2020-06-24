package saros.filesystem.checksum;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * Helper class returning the location of the given <code>IFile</code>.
 *
 * @see org.eclipse.core.resources.IFile#getLocation()
 */
public class EclipseAbsolutePathResolver implements IAbsolutePathResolver<IFile> {

  @Override
  public String getAbsolutePath(IFile file) {
    IPath eclipsePath = file.getLocation();

    if (eclipsePath == null) {
      return null;
    }

    return eclipsePath.toOSString();
  }
}
