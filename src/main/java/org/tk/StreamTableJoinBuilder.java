package org.tk;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.tk.model.Message;
import org.tk.model.Person;
import org.tk.serde.JsonSerde;

public class StreamTableJoinBuilder {
    final String INPUT_TOPIC_MESSAGE = "input-topic-message";
    final String INPUT_TOPIC_LOOKUP = "input-topic-lookup";
    final String OUTPUT_TOPIC = "output-topic";
    final Serde<String> stringSerde = Serdes.String();
    final Serde<Message> messageSerde = new JsonSerde<>(Message.class);
    final Serde<Person> personSerde = new JsonSerde<>(Person.class);

    public void build(StreamsBuilder builder) {
        var messageStream = builder.stream(INPUT_TOPIC_MESSAGE, Consumed.with(stringSerde, messageSerde));
        var personLookup = builder.table(INPUT_TOPIC_LOOKUP, Consumed.with(stringSerde, personSerde));
        messageStream.join(personLookup, (value1, value2) -> String.format("Message from %s to %s", value1.sender(), value2.name()))
                .to(OUTPUT_TOPIC);
    }
}
