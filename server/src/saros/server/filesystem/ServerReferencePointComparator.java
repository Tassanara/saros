package saros.server.filesystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IReferencePointComparator;

/**
 * Server implementation of the reference point comparator. It compares the absolute paths of the
 * resources represented by the reference point.
 *
 * @see ServerProjectImpl#getLocation()
 */
public class ServerReferencePointComparator implements IReferencePointComparator {

  @Override
  public boolean areNested(IReferencePoint r1, IReferencePoint r2) {
    ServerProjectImpl s1 = (ServerProjectImpl) r1;
    ServerProjectImpl s2 = (ServerProjectImpl) r2;

    Path p1 = Paths.get(s1.getLocation().toString());
    Path p2 = Paths.get(s2.getLocation().toString());

    return p1.equals(p2) || p1.startsWith(p2) || p2.startsWith(p1);
  }
}
