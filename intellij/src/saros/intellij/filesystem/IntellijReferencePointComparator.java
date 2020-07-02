package saros.intellij.filesystem;

import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IReferencePointComparator;

/**
 * Intellij implementation of the reference point comparator. It compares the absolute paths of the
 * virtual files represented by the reference point.
 *
 * @see VirtualFile#getPath()
 */
public class IntellijReferencePointComparator implements IReferencePointComparator {

  @Override
  public boolean areNested(IReferencePoint r1, IReferencePoint r2) {
    IntellijReferencePoint i1 = (IntellijReferencePoint) r1;
    IntellijReferencePoint i2 = (IntellijReferencePoint) r2;

    Path p1 = Paths.get(i1.getVirtualFile().getPath());
    Path p2 = Paths.get(i2.getVirtualFile().getPath());

    return p1.equals(p2) || p1.startsWith(p2) || p2.startsWith(p1);
  }
}
