package com.jnibridge;

import com.jnibridge.generator.compose.jni.ClassInfoJNIComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.extractor.ClassInfoExtractor;
import com.jnibridge.generator.scanner.ClassScanner;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JNIBridge {

    public static void generateJNIInterface(@NotNull final Path outPath, @NotNull final String... classes) {

        List<Class<?>> classesToMap = ClassScanner.getClassesToMap(classes);

        Map<Class<?>, String> classMappings = classesToMap.stream()
                .collect(Collectors.toMap(
                        clazz -> clazz,
                        clazz -> {
                            ClassInfo extractedClassInfo = ClassInfoExtractor.extract(clazz);
                            return new ClassInfoJNIComposer(extractedClassInfo).compose();
                        }
                ));


        //noinspection ResultOfMethodCallIgnored
        outPath.toFile().mkdir();

        for(Map.Entry<Class<?>, String> classMapping : classMappings.entrySet()) {
            final String filename = String.format("%s/%s.jni.h",outPath, classMapping.getKey().getSimpleName());

            try( FileWriter writer = new FileWriter(filename)) {


                writer.write(classMapping.getValue());

            } catch (IOException e) {
                throw new RuntimeException(String.format("Unable to create file: %s", filename), e);
            }
        }

    }
}
