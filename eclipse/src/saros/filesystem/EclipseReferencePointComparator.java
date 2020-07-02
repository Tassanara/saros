package saros.filesystem;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * Eclipse implementation of the reference point comparator. It compares the full paths of the
 * Eclipse delegates represented by the reference point.
 *
 * @see IResource#getFullPath()
 */
public class EclipseReferencePointComparator implements IReferencePointComparator {

  @Override
  public boolean areNested(IReferencePoint r1, IReferencePoint r2) {
    IContainer d1 = ResourceConverter.getDelegate(r1);
    IContainer d2 = ResourceConverter.getDelegate(r2);

    IPath p1 = d1.getFullPath();
    IPath p2 = d2.getFullPath();

    return p1.equals(p2) || p1.isPrefixOf(p2) || p2.isPrefixOf(p1);
  }
}
