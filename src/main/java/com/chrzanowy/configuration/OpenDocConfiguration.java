package com.chrzanowy.configuration;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Iterator;
import javax.money.MonetaryAmount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenDocConfiguration {

    @Bean
    public MonetaryAmountConverter samplePropertyConverter() {
        return new MonetaryAmountConverter();
    }

    public static class MonetaryAmountConverter implements ModelConverter {

        @Override
        public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
            if (type.isSchemaProperty()) {
                JavaType _type = Json.mapper().constructType(type.getType());
                if (_type != null) {
                    Class<?> cls = _type.getRawClass();
                    if (MonetaryAmount.class.isAssignableFrom(cls)) {
                        return new ObjectSchema()
                            .addProperty("amount", new NumberSchema()) // optionally add format
                            .addProperty("currency", new StringSchema());
                    }
                }
            }
            return (chain.hasNext()) ? chain.next().resolve(type, context, chain) : null;
        }
    }
}
