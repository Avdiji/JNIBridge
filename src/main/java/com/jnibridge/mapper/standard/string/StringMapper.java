package com.jnibridge.mapper.standard.string;

import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for  <code>std::string</code>.
 */
@Mapping(
        cType = "std::string",
        jniType = "jstring",
        inPath = "com/jnibridge/mappings/standard/string/String.in.mapping",
        outPath = "com/jnibridge/mappings/standard/string/String.out.mapping",
        cleanupPath = "com/jnibridge/mappings/standard/string/String.cleanup.mapping"
)
public class StringMapper implements TypeMapper {
}
