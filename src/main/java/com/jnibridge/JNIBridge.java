package com.jnibridge;

import com.jnibridge.generator.compose.jni.ClassInfoJNIComposer;
import com.jnibridge.generator.compose.jni.PtrWrapperJNIComposer;
import com.jnibridge.generator.compose.polymorphism.PolymorphicHelperComposer;
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
public class JNIBridge {

    /**
     * Generates JNI interface header files (.jni.h) for the specified Java classes.
     *
     * @param outPath the output directory where the generated JNI header files will be stored.
     *                If the directory does not exist, it will be created.
     * @param classes fully qualified names of the classes/packages to generate JNI headers for.
     * @throws RuntimeException if a header file cannot be created or written.
     */
    public static void generateJNIInterface(@NotNull final Path outPath, @NotNull final String[] classes, @NotNull final String[] nativeIncludes) {

        // extract all classes to map
        List<Class<?>> classesToMap = ClassScanner.getClassesToMap(classes);

        // map classes to map/extracted class-infos
        Map<Class<?>, ClassInfo> classMappings = classesToMap.stream()
                .collect(Collectors.toMap(
                        clazz -> clazz,
                        clazz -> ClassInfoExtractor.extract(clazz, classesToMap)
                ));

        createInternalFiles(outPath, classMappings.values(), Arrays.stream(nativeIncludes).collect(Collectors.toList()));

        createPolymorphicHelpers(outPath, classMappings.values().stream()
                .filter(classInfo -> IPointer.class.isAssignableFrom(classInfo.getClazz()))
                .collect(Collectors.toList()));

        createJNIFiles(outPath, classMappings);
    }

    /**
     * Method creates actual .jni.cpp files for the corresponding java classes.
     *
     * @param outPath       The output path of the generated JNI-Files.
     * @param classMappings The generated JNI-Content.
     */
    private static void createJNIFiles(@NotNull final Path outPath, @NotNull final Map<Class<?>, ClassInfo> classMappings) {
        for (Map.Entry<Class<?>, ClassInfo> classMapping : classMappings.entrySet()) {

            Class<?> clazz = classMapping.getKey();
            Path classPackageAsPath = Paths.get(clazz.getPackage().getName().replace(".", "/"));
            Path actualPath = outPath.resolve(classPackageAsPath);

            //noinspection ResultOfMethodCallIgnored
            actualPath.toFile().mkdirs();

            final String filename = String.format("%s/%s.jni.cpp", actualPath, clazz.getSimpleName());

            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(new ClassInfoJNIComposer(classMapping.getValue()).compose());
            } catch (IOException e) {
                throw new RuntimeException(String.format("Unable to create file: %s", filename), e);
            }
        }
    }

    /**
     * Create the file, which the JNIBridge uses internally, to handle mapping logic.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void createInternalFiles(@NotNull final Path outPath, Collection<ClassInfo> classesToMap, Collection<String> allNativeIncludes) {
        final Path internalPath = Paths.get(outPath.toString(), "internal");
        internalPath.toFile().mkdirs();

        // generate filenames of the internals
        final String ptrWrapperFilename = String.format("%s/%s", internalPath, PtrWrapperJNIComposer.INTERNAL_FILENAME);

        // create the corresponding internal files...
        try (FileWriter ptrWrapperWriter = new FileWriter(ptrWrapperFilename);
        ) {
            ptrWrapperWriter.write(new PtrWrapperJNIComposer(allNativeIncludes).compose());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to create file: %s", ptrWrapperFilename), e);
        }
    }

    private static void createPolymorphicHelpers(@NotNull final Path outPath, Collection<ClassInfo> instanceClasses) {
        final Path internalPath = Paths.get(outPath.toString(), "internal/polymorphism");
        internalPath.toFile().mkdirs();

        List<String> convenienceHeaderIncludes = new ArrayList<>();
        for (ClassInfo classInfo : instanceClasses) {
            final String filename = String.format("%s/%s", internalPath, PolymorphicHelperComposer.getHelperFilename(classInfo));

            try (FileWriter rawPolymorphicHelperWriter = new FileWriter(filename)) {
                rawPolymorphicHelperWriter.write(new PolymorphicHelperComposer(classInfo).compose());

            } catch (IOException e) {
                throw new RuntimeException("Unable to create polymorphic helper", e);
            } finally {
                convenienceHeaderIncludes.add(String.format("#include \"polymorphism/%s\"", PolymorphicHelperComposer.getHelperFilename(classInfo)));
            }
        }

        createPolymorphicHelperConvenienceHeader(outPath, convenienceHeaderIncludes);
    }

    private static void createPolymorphicHelperConvenienceHeader(@NotNull final Path outPath, @NotNull final Collection<String> includes) {
        final Path internalPath = Paths.get(outPath.toString(), "internal");
        internalPath.toFile().mkdirs();

        final String filename = String.format("%s/%s", internalPath, PolymorphicHelperComposer.POLYMORPHIC_CONVENIENCE_HEADER_FILENAME);
        try(FileWriter headerWriter = new FileWriter(filename)) {

            final StringBuilder result = new StringBuilder();
            result.append("#pragma once\n");
            includes.forEach(include -> result.append("\n").append(include));

            headerWriter.write(result.toString());

        } catch (IOException e) {
            throw new RuntimeException("Unable to create convenience header for polymorphic helpers", e);
        }

    }
}
