package saros.filesystem;

/** Interface providing methods to compare two reference points. */
public interface IReferencePointComparator {

  /**
   * Returns whether the two given reference point are nested. Reference points are nested if
   *
   * <ul>
   *   <li>they point to the same resource or
   *   <li>one reference point points to child resource of the other reference point.
   * </ul>
   *
   * @param r1 the first reference point to compare
   * @param r2 the second reference point to compare
   * @return whether the two given reference point are nested
   */
  boolean areNested(IReferencePoint r1, IReferencePoint r2);
}
