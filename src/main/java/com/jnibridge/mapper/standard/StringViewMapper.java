package com.jnibridge.mapper.standard;

import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for <code>std::string_view</code>
 */
@Mapping(
        cType = "std::string",
        jniType = "jstring",
        inPath = "com/jnibridge/mappings/standard/StringMapper.in.mapping",
        outPath = "com/jnibridge/mappings/standard/StringViewMapper.out.mapping"
)
public class StringViewMapper implements TypeMapper {
}
