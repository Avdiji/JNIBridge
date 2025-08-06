package com.jnibridge.mapper.standard;

import com.jnibridge.annotations.typemapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for std::string_view.
 */
@Mapping(
        cType = "std::string",
        jniType = "jstring",
        inPath = "com/jnibridge/mappings/standard/StringMapper.in.mapping",
        outPath = "com/jnibridge/mappings/standard/StringViewMapper.out.mapping"
)
public class StringViewMapper implements TypeMapper {
}
