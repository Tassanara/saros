package saros.misc.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.log4j.Logger;
import saros.activities.ResourceTransportWrapper;
import saros.annotations.Component;
import saros.communication.extensions.ActivitiesExtension;
import saros.filesystem.IFile;
import saros.filesystem.IPath;
import saros.filesystem.IPathFactory;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.filesystem.IResource.Type;
import saros.repackaged.picocontainer.Startable;
import saros.session.ISarosSession;

/**
 * Converts session- and IDE-dependent IResource objects to a session- and IDE-independent XML
 * representation and vice versa.
 *
 * <p><b>Example:</b> The XML representation of an {@link IFile} belonging to a {@linkplain
 * IReferencePoint reference point} with the id <code>"ABC"</code> and having the {@linkplain IPath
 * reference-point-relative path} <code>"src/Main.java"</code> is:
 *
 * <pre>
 * &lt;saros.activities.ResourceTransportWrapper i="ABC" p="%2Fsrc%2FMain.java" t="FILE"/&gt;
 * </pre>
 */
@Component
public class ResourceTransportWrapperConverter implements Converter, Startable {

  private static final Logger log = Logger.getLogger(ResourceTransportWrapperConverter.class);

  private static final String PATH = "p";
  private static final String REFERENCE_POINT_ID = "i";
  private static final String TYPE = "t";

  private final ISarosSession session;
  private final IPathFactory pathFactory;

  public ResourceTransportWrapperConverter(ISarosSession session, IPathFactory pathFactory) {
    this.session = session;
    this.pathFactory = pathFactory;
  }

  @Override
  public void start() {
    ActivitiesExtension.PROVIDER.registerConverter(this);
  }

  @Override
  public void stop() {
    ActivitiesExtension.PROVIDER.unregisterConverter(this);
  }

  @Override
  public boolean canConvert(Class clazz) {
    return clazz.equals(ResourceTransportWrapper.class);
  }

  @Override
  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    ResourceTransportWrapper<?> wrapper = (ResourceTransportWrapper<?>) value;
    IResource resource = wrapper.getResource();

    String i = session.getReferencePointId(resource.getReferencePoint());
    if (i == null) {
      log.error(
          "Could not retrieve reference point id for reference point '"
              + resource.getReferencePoint().getName()
              + "' of resource "
              + resource
              + ". Make sure you don't create activities for non-shared resources");
      return;
    }

    // TODO use IPath.toPortableString() instead?
    String p = URLCodec.encode(pathFactory.fromPath(resource.getReferencePointRelativePath()));

    Type type = resource.getType();

    if (type != Type.FILE && type != Type.FOLDER) {
      throw new IllegalStateException(
          "Illegal resource type " + type + " for resource " + resource);
    }

    String t = type.name();

    writer.addAttribute(REFERENCE_POINT_ID, i);
    writer.addAttribute(PATH, p);
    writer.addAttribute(TYPE, t);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

    String i = reader.getAttribute(REFERENCE_POINT_ID);
    String p = URLCodec.decode(reader.getAttribute(PATH));
    String t = reader.getAttribute(TYPE);

    IReferencePoint referencePoint = session.getReferencePoint(i);
    if (referencePoint == null) {
      log.error(
          "Could not create resource because there is no shared reference point for id '"
              + i
              + "'");
      return null;
    }

    IPath path = pathFactory.fromString(p);

    Type type = Type.valueOf(t);

    if (type == Type.FILE) {
      return new ResourceTransportWrapper<>(referencePoint.getFile(path));
    } else if (type == Type.FOLDER) {
      return new ResourceTransportWrapper<>(referencePoint.getFolder(path));
    } else {
      throw new IllegalStateException(
          "Illegal resource type "
              + type
              + ". This should not be possible and might hint at a version mismatch.");
    }
  }
}
