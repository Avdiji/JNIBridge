package com.jnibridge.mapper.primitives;

import com.jnibridge.annotations.typemapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for void.
 */
@Mapping(
        cType = "void",
        jniType = "void",
        inPath = "com/jnibridge/mappings/primitives/VoidMapper.in.mapping",
        outPath = "com/jnibridge/mappings/primitives/VoidMapper.out.mapping"
)
public class VoidMapper implements TypeMapper {
}
