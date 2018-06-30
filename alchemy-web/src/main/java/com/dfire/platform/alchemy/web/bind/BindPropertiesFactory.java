package com.dfire.platform.alchemy.web.bind;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.core.CollectionFactory;
import org.springframework.core.io.ByteArrayResource;
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
public class BindPropertiesFactory {

    public static void bindPropertiesToTarget(Object target, String prefix, String value) throws Exception {
        PropertiesConfigurationFactory<Object> factory = new PropertiesConfigurationFactory<Object>(target);
        factory.setProperties(createProperty(value));
        factory.setIgnoreInvalidFields(false);
        factory.setIgnoreUnknownFields(true);
        factory.setExceptionIfInvalid(true);
        factory.setIgnoreNestedProperties(false);
        factory.setTargetName(prefix);
        factory.bindPropertiesToTarget();
    }

    public static void bindPropertiesToTarget(Object target, String prefix, Properties properties) throws Exception {
        PropertiesConfigurationFactory<Object> factory = new PropertiesConfigurationFactory<Object>(target);
        factory.setProperties(properties);
        factory.setIgnoreInvalidFields(false);
        factory.setIgnoreUnknownFields(true);
        factory.setExceptionIfInvalid(true);
        factory.setIgnoreNestedProperties(false);
        factory.setTargetName(prefix);
        factory.bindPropertiesToTarget();
    }

    private static Properties createProperty(String value) throws IOException {
        ByteArrayResource byteArrayResource = new ByteArrayResource(value.getBytes());
        Processor propertySources = new Processor(byteArrayResource);
        return propertySources.createProperties();
    }

    private static class Processor extends YamlProcessor {

        Processor(Resource resource) {
            setResources(new Resource[] {resource});
        }

        @Override
        protected Yaml createYaml() {
            return new Yaml(new StrictMapAppenderConstructor(), new Representer(), new DumperOptions(), new Resolver() {
                @Override
                public void addImplicitResolver(Tag tag, Pattern regexp, String first) {
                    if (tag == Tag.TIMESTAMP) {
                        return;
                    }
                    super.addImplicitResolver(tag, regexp, first);
                }
            });
        }

        protected Properties createProperties() {
            Properties result = CollectionFactory.createStringAdaptingProperties();
            this.process((properties, map) -> {
                result.putAll(properties);
            });
            return result;
        }
    }

}
