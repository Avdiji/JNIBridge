package com.jnibridge;

import com.jnibridge.generator.compose.ClassInfoComposer;
import com.jnibridge.generator.compose.jni.ClassInfoJNIComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.generator.scanner.ClassScanner;
import com.jnibridge.utils.ResourceUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The {@code JNIBridge} class provides functionality to generate JNI (Java Native Interface)
 * header files for given Java classes.
 * <p>
 * These headers can be used for creating native
 * implementations in C/C++ that interact with Java classes.
 * <p>
 *
 * @author Fitor Avdiji
 * @version 1.0.0
 */
public class JNIBridge {

    /**
     * Generates JNI interface header files (.jni.h) for the specified Java classes.
     *
     * @param outPath the output directory where the generated JNI header files will be stored.
     *                If the directory does not exist, it will be created.
     * @param classes fully qualified names of the classes/packages to generate JNI headers for.
     * @throws RuntimeException if a header file cannot be created or written.
     */
    public static void generateJNIInterface(@NotNull final Path outPath, @NotNull final String... classes) {

        // extract all classes to map
        List<Class<?>> classesToMap = ClassScanner.getClassesToMap(classes);

        // map classes to map/extracted class-infos
        Map<Class<?>, String> classMappings = classesToMap.stream()
                .collect(Collectors.toMap(
                        clazz -> clazz,
                        clazz -> {
                            ClassInfo extractedClassInfo = ClassInfoExtractor.extract(clazz);
                            return new ClassInfoJNIComposer(extractedClassInfo).compose();
                        }
                ));

        createInternalFiles(outPath);
        createJNIFiles(outPath, classMappings);
    }

    /**
     * Method creates actual .jni.cpp files for the corresponding java classes.
     *
     * @param outPath       The output path of the generated JNI-Files.
     * @param classMappings The generated JNI-Content.
     */
    private static void createJNIFiles(@NotNull final Path outPath, @NotNull final Map<Class<?>, String> classMappings) {
        for (Map.Entry<Class<?>, String> classMapping : classMappings.entrySet()) {

            Class<?> clazz = classMapping.getKey();
            Path classPackageAsPath = Paths.get(clazz.getPackage().getName().replace(".", "/"));
            Path actualPath = outPath.resolve(classPackageAsPath);

            //noinspection ResultOfMethodCallIgnored
            actualPath.toFile().mkdirs();

            final String filename = String.format("%s/%s.jni.cpp", actualPath, clazz.getSimpleName());

            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(classMapping.getValue());
            } catch (IOException e) {
                throw new RuntimeException(String.format("Unable to create file: %s", filename), e);
            }
        }
    }

    /**
     * Create the files, which the JNIBridge uses internally, to handle mapping logic.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void createInternalFiles(@NotNull final Path outPath) {
        outPath.toFile().mkdirs();

        final String ptrWrapperFilename = String.format("%s/%s", outPath, ClassInfoComposer.WRAPPER_NAME);
        String ptrWrapperContent = ResourceUtils.load("com/jnibridge/internals/JniBridgePtrWrapper.jni.cpp");

        try(FileWriter ptrWrapperWriter = new FileWriter(ptrWrapperFilename)) {
            ptrWrapperWriter.write(ptrWrapperContent);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to create file: %s", ptrWrapperFilename), e);
        }
    }
}
