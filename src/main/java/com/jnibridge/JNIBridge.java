package com.jnibridge;

import com.jnibridge.exception.JniBridgeException;
import com.jnibridge.generator.compose.Composer;
import com.jnibridge.generator.compose.jni.helper.JniBridgeExceptionComposer;
import com.jnibridge.generator.compose.jni.ClassInfoJNIComposer;
import com.jnibridge.generator.compose.jni.helper.JniBridgeHandleComposer;
import com.jnibridge.generator.compose.jni.helper.polymorphism.PolymorphicHelperComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.generator.scanner.ClassScanner;
import com.jnibridge.nativeaccess.IPointer;
import org.jetbrains.annotations.NotNull;

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
     * @param outPath            the output directory where the generated JNI header files will be stored.
     *                           If the directory does not exist, it will be created.
     * @param classes            fully qualified names of the classes/packages to generate JNI headers for.
     * @param nativeIncludes     All C++ includes needed for the mapping.
     * @param customJNICodePaths Resource-Paths, to include centralized, custom JNI-code.
     * @throws RuntimeException if a header file cannot be created or written.
     */
    public static void generateJNIInterface(@NotNull final Path outPath, @NotNull final String[] classes, @NotNull final String[] nativeIncludes, @NotNull final String[] customJNICodePaths) {

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
        generateJniBridgeHandle(
                outPath,
                Arrays.stream(nativeIncludes).collect(Collectors.toList()),
                Arrays.stream(customJNICodePaths).collect(Collectors.toList())
        );

        // generate the JniBridge Exception-handler file.
        generateJniBridgeExceptionHandler(outPath);

        // generate the polymorphic helper files.
        generatePolymorphicHelpers(
                outPath,
                classMappings.values().stream()
                        .filter(classInfo -> IPointer.class.isAssignableFrom(classInfo.getClazz()))
                        .collect(Collectors.toList()));
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
        generateJNIInterface(outPath, classes, nativeIncludes, new String[]{});
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
            final String filename = String.format("%s/%s.jni.cpp", actualPath, clazz.getSimpleName());
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(new ClassInfoJNIComposer(classMapping.getValue()).compose());
            } catch (IOException e) {
                throw new JniBridgeException(String.format("Unable to create file: %s", filename), e);
            }
        }
    }

    /**
     * Generate the file, which the JNIBridge uses internally, to handle mapping logic.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void generateJniBridgeHandle(@NotNull final Path outPath, Collection<String> allNativeIncludes, @NotNull final Collection<String> customJNICodePaths) {
        final Path internalPath = Paths.get(outPath.toString(), "internal");
        internalPath.toFile().mkdirs();

        // generate filenames of the internals
        final String ptrWrapperFilename = String.format("%s/%s", internalPath, JniBridgeHandleComposer.INTERNAL_FILENAME);

        // create the corresponding internal files...
        try (FileWriter jniHandleWriter = new FileWriter(ptrWrapperFilename)
        ) {
            jniHandleWriter.write(new JniBridgeHandleComposer(allNativeIncludes, customJNICodePaths).compose());
        } catch (IOException e) {
            throw new JniBridgeException(String.format("Unable to create file: %s", ptrWrapperFilename), e);
        }
    }

    /**
     *
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
            final String filename = String.format("%s/%s", internalPath, Composer.getPolyHelperFilename(classInfo));

            try (FileWriter rawPolymorphicHelperWriter = new FileWriter(filename)) {
                rawPolymorphicHelperWriter.write(new PolymorphicHelperComposer(classInfo).compose());

            } catch (IOException e) {
                throw new JniBridgeException("Unable to create polymorphic helper", e);
            } finally {
                convenienceHeaderIncludes.add(String.format("#include \"polymorphism/%s\"", Composer.getPolyHelperFilename(classInfo)));
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
}
