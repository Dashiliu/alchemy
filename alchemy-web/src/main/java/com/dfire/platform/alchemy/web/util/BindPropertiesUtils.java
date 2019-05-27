package com.dfire.platform.alchemy.web.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;
import java.util.regex.Pattern;

import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonToken;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.io.IOContext;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.apache.flink.table.client.config.ConfigUtil;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.core.CollectionFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 * @author congbai
 * @date 2018/6/30
 */
public class BindPropertiesUtils {

    private static final ObjectMapper objectMapper = new LowerCaseYamlMapper();

    static {
//        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static <T> T bindProperties(String value, Class<T> clazz) throws Exception {
       return objectMapper.readValue(value, clazz);
    }

    public static <T> T bindProperties(File inputFile, Class<T> clazz) throws Exception {
        return objectMapper.readValue(inputFile.toURL(), clazz);
    }


    public static class LowerCaseYamlMapper extends ObjectMapper {
        public LowerCaseYamlMapper() {
            super(new YAMLFactory() {
                @Override
                protected YAMLParser _createParser(InputStream in, IOContext ctxt) throws IOException {
                    final Reader r = _createReader(in, null, ctxt);
                    // normalize all key to lower case keys
                    return new YAMLParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures, _objectCodec, r) {
                        @Override
                        public String getCurrentName() throws IOException {
                            if (_currToken == JsonToken.FIELD_NAME) {
                                return translate(_currentFieldName);
                            }
                            return super.getCurrentName();
                        }

                        @Override
                        public String getText() throws IOException {
                            if (_currToken == JsonToken.FIELD_NAME) {
                                return translate(_currentFieldName);
                            }
                            return super.getText();
                        }
                    };
                }

                @Override
                public YAMLParser createParser(String content) throws IOException {
                    final Reader reader =new StringReader(content);
                    IOContext ctxt = this._createContext(reader, true);
                    Reader r = this._decorate(reader, ctxt);
                    return new YAMLParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures, _objectCodec, r) {
                        @Override
                        public String getCurrentName() throws IOException {
                            if (_currToken == JsonToken.FIELD_NAME) {
                                return translate(_currentFieldName);
                            }
                            return super.getCurrentName();
                        }

                        @Override
                        public String getText() throws IOException {
                            if (_currToken == JsonToken.FIELD_NAME) {
                                return translate(_currentFieldName);
                            }
                            return super.getText();
                        }
                    };
                }
            });
        }
    }

    static String translate(String input) {
        if (input == null) {
            return input;
        } else {
            int length = input.length();
            if (length == 0) {
                return input;
            } else {
                StringBuilder result = new StringBuilder(length + (length << 1));
                int upperCount = 0;

                for (int i = 0; i < length; ++i) {
                    char ch = input.charAt(i);
                    if (upperCount > 0){
                        char uc = Character.toUpperCase(ch);
                        result.append(uc);
                        upperCount = 0;
                        continue;
                    }
                    if (ch == '-'){
                        ++upperCount;
                    }else{
                        result.append(Character.toLowerCase(ch));
                    }
                }

                return result.toString();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(translate("min"));
        System.out.println(translate("min-haha"));
        System.out.println(translate("min-haha-fewfwf"));
        System.out.println(translate("min-haha-fewfWf"));
    }
}
