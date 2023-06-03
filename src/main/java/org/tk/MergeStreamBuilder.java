package org.tk;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;

public class MergeStreamBuilder {
    final String INPUT_TOPIC_A = "input-topic-A";
    final String INPUT_TOPIC_B = "input-topic-B";
    final String OUTPUT_TOPIC = "output-topic";
    final Serde<String> stringSerde = Serdes.String();

    public void build(StreamsBuilder builder) {
        var streamA = builder.stream(INPUT_TOPIC_B, Consumed.with(stringSerde, stringSerde));
        var streamB = builder.stream(INPUT_TOPIC_A, Consumed.with(stringSerde, stringSerde));

        streamA.merge(streamB).to(OUTPUT_TOPIC);
    }
}
