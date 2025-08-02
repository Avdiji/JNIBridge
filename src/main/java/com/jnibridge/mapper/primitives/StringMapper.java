package com.jnibridge.mapper.primitives;

import com.jnibridge.annotations.typemapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for simple strings.
 */
@Mapping(
        cType = "std::string",
        jniType = "jstring",
        inPath = "com/jnibridge/mappings/primitives/StringMapper.in.mapping",
        outPath = "com/jnibridge/mappings/primitives/StringMapper.out.mapping"
)
public class StringMapper implements TypeMapper {
}
