package org.tk;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.streams.kstream.Produced;

import static java.lang.Thread.currentThread;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG,"my-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:29092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        String inputTopic = "input-topic";
        String outputTopic = "output-topic";

        Topology topology = buildTopology(inputTopic,outputTopic);

        try (KafkaStreams streams = new KafkaStreams(topology, config)) {
            Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
            runKafkaStreams(streams);
        }
    }

    static void runKafkaStreams(final KafkaStreams streams) {
        final CountDownLatch latch = new CountDownLatch(1);
        streams.setStateListener((newState, oldState) -> {
            if (oldState == KafkaStreams.State.RUNNING && newState != KafkaStreams.State.RUNNING) {
                latch.countDown();
            }
        });

        streams.start();

        try {
            latch.await();
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }

        logger.info("Streams Closed");
    }

    static Topology buildTopology(String inputTopic, String outputTopic) {
        final Serde<String> stringSerde = Serdes.String();
        StreamsBuilder builder = new StreamsBuilder();
        builder.stream(inputTopic, Consumed.with(stringSerde, stringSerde))
                .peek((k,v) -> logger.info("Observed event: {}", v))
                .mapValues(s -> s.toUpperCase())
                .filter((k,v) -> v.startsWith("A")) // already uppercased !
                .flatMapValues(v -> Arrays.asList(v.split("\\s+")))
                .peek((k,v) -> logger.info("Transformed event: {}", v))
                .to(outputTopic, Produced.with(stringSerde, stringSerde));
        return builder.build();
    }
}
