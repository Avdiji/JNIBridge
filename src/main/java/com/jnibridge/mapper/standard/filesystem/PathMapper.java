package com.jnibridge.mapper.standard.filesystem;

import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for <code>std::filesystem::path</code>
 */
@Mapping(
        cType = "std::filesystem::path",
        jniType = "jobject",
        templates = @Mapping.MappingTemplate(
                inPath = "com/jnibridge/mappings/standard/filesystem/Path.in.mapping",
                outPath = "com/jnibridge/mappings/standard/filesystem/Path.out.mapping",
                cleanupPath = "com/jnibridge/mappings/standard/filesystem/Path.cleanup.mapping"
        )
)
public class PathMapper implements TypeMapper {
}
