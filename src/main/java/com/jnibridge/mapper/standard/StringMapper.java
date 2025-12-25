package com.jnibridge.mapper.standard;

import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for  <code>std::string</code>.
 */
@Mapping(
        cType = "std::string",
        jniType = "jstring",
        inPath = "com/jnibridge/mappings/standard/StringMapper.in.mapping",
        outPath = "com/jnibridge/mappings/standard/StringMapper.out.mapping"
)
public class StringMapper implements TypeMapper {
}
