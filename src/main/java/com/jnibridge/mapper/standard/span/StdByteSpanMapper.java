package com.jnibridge.mapper.standard.span;

import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for {@code std::span<std::byte>}.
 */
@SuppressWarnings("unused")
@Mapping(
        cType = "std::span<std::byte>",
        jniType = "jbyteArray",
        inPath = "com/jnibridge/mappings/standard/span/StdSpan.byte.std.in.mapping",
        outPath = "com/jnibridge/mappings/standard/span/StdSpan.byte.out.mapping"
)
public class StdByteSpanMapper implements TypeMapper {
}
