package saros.filesystem.checksum;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Bridge class that maps Eclipse Resource change events to unique identifiers by retrieving the
 * absolute path relative to the workspace and converting the path to a unique string.
 *
 * @author Stefan Rossbach
 */
public class FileContentNotifierBridge
    implements IFileContentChangedNotifier<IFile>, IResourceChangeListener {

  private CopyOnWriteArrayList<IFileContentChangedListener<IFile>> fileContentChangedListeners =
      new CopyOnWriteArrayList<>();

  public FileContentNotifierBridge() {
    ResourcesPlugin.getWorkspace()
        .addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
  }

  @Override
  public void resourceChanged(IResourceChangeEvent event) {
    if (event.getDelta() == null) return;

    Deque<IResourceDelta> stack = new LinkedList<IResourceDelta>();

    stack.addAll(Arrays.asList(event.getDelta().getAffectedChildren()));

    while (!stack.isEmpty()) {
      IResourceDelta delta = stack.pop();
      stack.addAll(Arrays.asList(delta.getAffectedChildren()));

      if (delta.getResource().getType() == IResource.FILE) {

        // TODO check the Eclipse API to ignore more events
        if (delta.getKind() == IResourceDelta.NO_CHANGE) continue;

        if ((delta.getKind() == IResourceDelta.CHANGED)
            && (delta.getFlags() == IResourceDelta.MARKERS)) continue;

        final IFile file = delta.getResource().getAdapter(IFile.class);

        for (IFileContentChangedListener<IFile> listener : fileContentChangedListeners) {
          listener.fileContentChanged(file);
        }
      }
    }
  }

  @Override
  public void addFileContentChangedListener(IFileContentChangedListener<IFile> listener) {
    fileContentChangedListeners.add(listener);
  }

  @Override
  public void removeFileContentChangedListener(IFileContentChangedListener<IFile> listener) {
    fileContentChangedListeners.remove(listener);
  }
}
