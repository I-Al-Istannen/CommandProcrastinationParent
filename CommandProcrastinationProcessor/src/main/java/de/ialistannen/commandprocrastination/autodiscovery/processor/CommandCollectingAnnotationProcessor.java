package de.ialistannen.commandprocrastination.autodiscovery.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.kohsuke.MetaInfServices;

@SupportedAnnotationTypes("de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@MetaInfServices(Processor.class)
public class CommandCollectingAnnotationProcessor extends AbstractProcessor {

  private List<String> foundElements;
  private String packageName;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    foundElements = new ArrayList<>();

    packageName = processingEnv.getOptions().get("targetPackage");
    if (packageName == null || packageName.isBlank()) {
      processingEnv.getMessager().printMessage(
          Kind.ERROR, "No 'targetPackage' option given!"
      );
    }
    packageName = packageName.trim();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.errorRaised() || roundEnv.processingOver()) {
      return false;
    }

    for (TypeElement annotation : annotations) {
      Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
      System.out.println(elements);

      for (Element element : elements) {
        processElement(element);
      }

      String classNames = foundElements.stream()
          .map(it -> "      " + it + ".class,")
          .collect(Collectors.joining("\n"));

      String collectedClass = "package " + packageName + ";\n"
          + "import de.ialistannen.commandprocrastination.autodiscovery.processor.CollectedCommands;\n"
          + "public class CommandClasses implements CollectedCommands {\n"
          + "  public static final Class[] COMMAND_CLASSES = new Class[]{\n"
          + classNames
          + "  };\n"
          + "public Class<?>[] getCollectedCommands() {\n"
          + "    return COMMAND_CLASSES;\n"
          + "  }"
          + "}";

      saveClass(collectedClass);

      foundElements.clear();
    }
    return true;
  }

  private void saveClass(String collectedClass) {
    try {
      JavaFileObject commandClasses = processingEnv.getFiler().createSourceFile(
          packageName + ".CommandClasses"
      );

      try (Writer writer = commandClasses.openWriter()) {
        writer.write(collectedClass);
      }
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
    }
  }

  private void processElement(Element element) {
    TypeElement type = (TypeElement) element;

    if (type.getNestingKind() != NestingKind.TOP_LEVEL) {
      if (!type.getModifiers().contains(Modifier.STATIC)) {
        processingEnv.getMessager().printMessage(
            Kind.ERROR, "Commands need to be static inner classes", type
        );
      }
    }

    type.getEnclosedElements().stream()
        .filter(it -> it.getKind() == ElementKind.CONSTRUCTOR)
        .map(it -> (ExecutableElement) it)
        .filter(it -> it.getParameters().size() != 0)
        .findFirst()
        .ifPresent(constructor -> processingEnv.getMessager().printMessage(
            Kind.ERROR,
            "You need a no-args constructor",
            type
        ));
    foundElements.add(type.getQualifiedName().toString());
  }
}
