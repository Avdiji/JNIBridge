package com.jnibridge.mapper.standard.string;

import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for <code>std::string_view</code>
 */
@Mapping(
        cType = "std::string",
        jniType = "jstring",
        templates = @Mapping.MappingTemplate(
                inPath = "com/jnibridge/mappings/standard/string/String.in.mapping",
                outPath = "com/jnibridge/mappings/standard/string/StringView.out.mapping"
        )
)
public class StringViewMapper implements TypeMapper {
}
