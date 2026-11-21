package com.testhakni.backend.shared.infrastructure.config;

import java.math.BigDecimal;
import java.util.List;

import org.bson.types.Decimal128;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * Configuration MongoDB. DB-01
 * - audit : remplit createdAt / updatedAt (@CreatedDate, @LastModifiedDate)
 * - BigDecimal stocke en Decimal128 (par defaut Spring le stocke en texte,
 *   ce qui casse le tri et les filtres par prix)
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(new BigDecimalToDecimal128(), new Decimal128ToBigDecimal()));
    }

    @WritingConverter
    static class BigDecimalToDecimal128 implements Converter<BigDecimal, Decimal128> {
        @Override
        public Decimal128 convert(BigDecimal source) {
            return new Decimal128(source);
        }
    }

    @ReadingConverter
    static class Decimal128ToBigDecimal implements Converter<Decimal128, BigDecimal> {
        @Override
        public BigDecimal convert(Decimal128 source) {
            return source.bigDecimalValue();
        }
    }
}
