package com.jnibridge.mapper.standard;

import com.jnibridge.annotations.typemapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for simple strings.
 */
@Mapping(
        cType = "std::string",
        jniType = "jstring",
        inPath = "com/jnibridge/mappings/standard/StringMapper.in.mapping",
        outPath = "com/jnibridge/mappings/standard/StringMapper.out.mapping"
)
public class StringMapper implements TypeMapper {
}
