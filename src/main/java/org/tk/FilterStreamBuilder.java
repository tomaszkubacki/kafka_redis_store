package org.tk;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class FilterStreamBuilder {
    final String INPUT_TOPIC_A = "input-topic-A";
    final String OUTPUT_TOPIC = "output-topic";
    final String DEAD_END_TOPIC = "dead-end-topic";
    final Serde<String> stringSerde = Serdes.String();

    private static final Logger logger = LoggerFactory.getLogger(FilterStreamBuilder.class);

    public void build(StreamsBuilder builder) {
        var branches = builder.stream(INPUT_TOPIC_A, Consumed.with(stringSerde, stringSerde))
                .peek((k, v) -> logger.info("Observed event: {}", v))
                .mapValues(s -> s.toUpperCase())
                .filter((k, v) -> v.startsWith("A")) // already uppercase
                .flatMapValues(v -> Arrays.asList(v.split("\\s+"))) //split value by white chars
                .peek((k, v) -> logger.info("Transformed event: {}", v))
                .split(Named.as("Branch-"))
                .branch((k, v) -> v.startsWith("A"),  /* first predicate  */
                        Branched.as("A"))
                .branch((k, v) -> !v.startsWith("A"),  /* second predicate */
                        Branched.as("B"))
                .noDefaultBranch();

        branches.get("Branch-A").to(OUTPUT_TOPIC, Produced.with(stringSerde, stringSerde));
        branches.get("Branch-B").to(DEAD_END_TOPIC, Produced.with(stringSerde, stringSerde));
    }
}
