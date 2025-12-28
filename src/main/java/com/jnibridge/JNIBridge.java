package com.jnibridge;

import com.jnibridge.exception.JniBridgeException;
import com.jnibridge.generator.compose.jni.ClassInfoJNIComposer;
import com.jnibridge.generator.compose.jni.helper.JniBridgeExceptionComposer;
import com.jnibridge.generator.compose.jni.helper.JniBridgeHandleComposer;
import com.jnibridge.generator.compose.jni.helper.polymorphism.PolymorphicHelperComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.generator.scanner.ClassScanner;
import com.jnibridge.nativeaccess.IPointer;
import com.jnibridge.utils.ResourceUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
@SuppressWarnings("ResultOfMethodCallIgnored")
public class JNIBridge {

    /**
     * Generates JNI interface header files (.jni.h) for the specified Java classes.
     *
     * @param outPath        the output directory where the generated JNI header files will be stored.
     *                       If the directory does not exist, it will be created.
     * @param classes        fully qualified names of the classes/packages to generate JNI headers for.
     * @param nativeIncludes All C++ includes needed for the mapping.
     * @param customJNIFiles Resource-Paths, to include centralized, custom JNI-code.
     * @throws RuntimeException if a header file cannot be created or written.
     */
    public static void generateJNIInterface(@NotNull final Path outPath, @NotNull final String[] classes, @NotNull final String[] nativeIncludes, @NotNull final Map<Path, String> customJNIFiles) {

        // extract all classes to map
        List<Class<?>> classesToMap = ClassScanner.getClassesToMap(classes);

        // @formatter:off
        // map classes to map/extracted class-infos
        Map<Class<?>, ClassInfo> classMappings = classesToMap.stream()
                .collect(Collectors.toMap(
                        clazz -> clazz,
                        clazz -> ClassInfoExtractor.extract(clazz, classesToMap)
                ));

        // generate the JniBridgeHandle - helper file.
        generateJniBridgeHandle(outPath, Arrays.stream(nativeIncludes).collect(Collectors.toList()));

        // generate the JniBridge Exception-handler file.
        generateJniBridgeExceptionHandler(outPath);

        // generate the polymorphic helper files.
        generatePolymorphicHelpers(
                outPath,
                classMappings.values().stream()
                        .filter(classInfo -> IPointer.class.isAssignableFrom(classInfo.getClazz()))
                        .collect(Collectors.toList()));

        // generate any user-defined custom files.
        generateCustomJNIFiles(customJNIFiles);
        // @formatter:on

        // generate the 'actual' JNI files...
        createJNIFiles(outPath, classMappings);
    }

    /**
     * Generates JNI interface header files (.jni.h) for the specified Java classes.
     *
     * @param outPath the output directory where the generated JNI header files will be stored.
     *                If the directory does not exist, it will be created.
     * @param classes fully qualified names of the classes/packages to generate JNI headers for.
     * @throws RuntimeException if a header file cannot be created or written.
     */
    @SuppressWarnings("unused")
    public static void generateJNIInterface(@NotNull final Path outPath, @NotNull final String[] classes, @NotNull final String[] nativeIncludes) {
        generateJNIInterface(outPath, classes, nativeIncludes, new HashMap<>());
    }

    /**
     * Method creates actual .jni.cpp files for the corresponding java classes.
     *
     * @param outPath       The output path of the generated JNI-File.
     * @param classMappings The generated JNI-Content.
     */
    private static void createJNIFiles(@NotNull final Path outPath, @NotNull final Map<Class<?>, ClassInfo> classMappings) {
        for (Map.Entry<Class<?>, ClassInfo> classMapping : classMappings.entrySet()) {

            // compute the output path of the generated jni-file (reflects the package path)
            Class<?> clazz = classMapping.getKey();
            Path classPackageAsPath = Paths.get(clazz.getPackage().getName().replace(".", "/"));
            Path actualPath = outPath.resolve(classPackageAsPath);
            actualPath.toFile().mkdirs();

            // compose and write the jni-file...
            final String fullFilename = String.format("%s/%s", actualPath, ResourceUtils.getFilename(clazz, "jni", "cpp"));
            try (FileWriter writer = new FileWriter(fullFilename)) {
                writer.write(new ClassInfoJNIComposer(classMapping.getValue()).compose());
            } catch (IOException e) {
                throw new JniBridgeException(String.format("Unable to create file: %s", fullFilename), e);
            }
        }
    }

    /**
     * Generate the file, which the JNIBridge uses internally, to handle mapping logic.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void generateJniBridgeHandle(@NotNull final Path outPath, Collection<String> allNativeIncludes) {
        final Path internalPath = Paths.get(outPath.toString(), "internal");
        internalPath.toFile().mkdirs();

        // generate filenames of the internals
        final String ptrWrapperFilename = String.format("%s/%s", internalPath, JniBridgeHandleComposer.INTERNAL_FILENAME);

        // create the corresponding internal files...
        try (FileWriter jniHandleWriter = new FileWriter(ptrWrapperFilename)
        ) {
            jniHandleWriter.write(new JniBridgeHandleComposer(allNativeIncludes).compose());
        } catch (IOException e) {
            throw new JniBridgeException(String.format("Unable to create file: %s", ptrWrapperFilename), e);
        }
    }

    /**
     * @param outPath The output path of the generated JNI-File.
     */
    private static void generateJniBridgeExceptionHandler(@NotNull final Path outPath) {
        final Path internalPath = Paths.get(outPath.toString(), "internal");
        internalPath.toFile().mkdirs();

        try (FileWriter jniExceptionWriter = new FileWriter(String.format("%s/%s", internalPath, JniBridgeExceptionComposer.FILENAME))) {
            jniExceptionWriter.write(new JniBridgeExceptionComposer().compose());
        } catch (IOException e) {
            throw new JniBridgeException("Unable to create JNIBridge exception-handler", e);
        }
    }

    /**
     * Generate the polymorphic helper, header files.
     *
     * @param outPath         Out-path of the Polymorphic helper files.
     * @param iPointerClasses Classes, that implement the {@link IPointer} interface.
     */
    private static void generatePolymorphicHelpers(@NotNull final Path outPath, Collection<ClassInfo> iPointerClasses) {
        final Path internalPath = Paths.get(outPath.toString(), "internal/polymorphism");
        internalPath.toFile().mkdirs();

        List<String> convenienceHeaderIncludes = new ArrayList<>();
        for (ClassInfo classInfo : iPointerClasses) {

            final String filename = ResourceUtils.getFilename(classInfo.getClazz(), "helper", "jni", "cpp");
            final String fullFilename = String.format("%s/%s", internalPath, filename);

            try (FileWriter rawPolymorphicHelperWriter = new FileWriter(fullFilename)) {
                rawPolymorphicHelperWriter.write(new PolymorphicHelperComposer(classInfo).compose());

            } catch (IOException e) {
                throw new JniBridgeException("Unable to create polymorphic helper", e);
            } finally {
                convenienceHeaderIncludes.add(String.format("#include \"polymorphism/%s\"", filename));
            }
        }
        generatePolymorphicHelperConvenienceHeader(outPath, convenienceHeaderIncludes);
    }

    /**
     * Generate a convenience header for the polymorphic headers.
     *
     * @param outPath  The out-path of the convenience file.
     * @param includes All polymorphic helper file-includes.
     */
    private static void generatePolymorphicHelperConvenienceHeader(@NotNull final Path outPath, @NotNull final Collection<String> includes) {
        final Path internalPath = Paths.get(outPath.toString(), "internal");
        internalPath.toFile().mkdirs();

        final String filename = String.format("%s/%s", internalPath, PolymorphicHelperComposer.FILENAME);
        try (FileWriter headerWriter = new FileWriter(filename)) {

            final StringBuilder result = new StringBuilder();
            result.append("#pragma once\n");
            includes.forEach(include -> result.append("\n").append(include));

            headerWriter.write(result.toString());

        } catch (IOException e) {
            throw new JniBridgeException("Unable to create convenience header for polymorphic helpers", e);
        }
    }

    /**
     * Generate Custom-JNI files if any have been generated.
     *
     * @param customFiles A Map of file paths and the desired content.
     */
    private static void generateCustomJNIFiles(@NotNull final Map<Path, String> customFiles) {
        for (final Map.Entry<Path, String> entry : customFiles.entrySet()) {
            final Path filePath = entry.getKey();
            final File outFile = filePath.toFile();

            // 1. Create parent directories
            final File parentDir = outFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs() && !parentDir.exists()) {
                    throw new JniBridgeException(
                            "Unable to create directories: " + parentDir.getAbsolutePath()
                    );
                }
            }

            // 2. Write file at the correct path
            try (FileWriter writer = new FileWriter(outFile)) {
                final String content = ResourceUtils.load(entry.getValue());
                writer.write(content);
            } catch (IOException e) {
                throw new JniBridgeException(
                        "Unable to create file: " + outFile.getAbsolutePath(), e
                );
            }
        }
    }
}
