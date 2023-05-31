## Kafka Streams explained

Kafka streams is a Java library to process data from Kafka topics and save data to kafka topics

### Why use Kafka Streams ?

Kafka streams and dataflow programming has several advantages when used for stream processing.

1) easy to reason: It's easy to reason about program represented as directed graph, no need to track complicated "if logic" to track data flow. 
 Source and sink can be easily identified. Easy to reason about each step, where data can be enriched, transformed and send to sink.
2) easy to visualize: Directed graphs are easy to visualize and easy to communicate with non-technicals
3) easy to parallelize: Kafka streams contains abstractions helping create processing templates which are easy to parallelize 


```mermaid
flowchart TB
    subgraph Java application
        KStream{{KStream}}:::someclass
    end
        IN --> KStream
        KStream --> OUT
    subgraph Kafka
        direction LR
        IN(input topic) 
        OUT(output topic)
    end
        
    click IN callback "Kafka Topic: input-topic"
    click OUT callback "Kafka Topic: output-topic"
    click KStream callback "Kafka streams in app memory"
    classDef someclass fill: #0000ff, color: white
```
### Basic concepts

To understand Kafka streams it's necessary to become familiar with a few concepts. 
The most important among them are: **KStream** and **KTable**.

#### KStream

> KStream definition
: KStream is an abstraction of partitioned record stream, in which data is represented using insert semantics 
> i.e. each record is independent of other events 

As a developer you can consider KStream as never ending (unbound) event log, 
which you can modify using kafka stream operations.

important properties:
- always insert only
- similar to Log
- unbounded data stream

> what does the *unbound* mean ?
> 
> having start but not end - being possibly infinite at the end

Here is how KStream content looks like (right column) after new records appear in Kafka

| Topic(key, value) | KStream             |
|-------------------|---------------------|
| (a,1)             | (a,1)               |
| (b,1)             | (a,1), (b,1)        | 
| (a,2)             | (a,1), (b,1), (a,2) |


#### KTable


> KTable definition
: KTable is a table of key value records, which are upserted 

- upserts on non null values
- deletes on null values
- similar to table
- parallel with log compacted topics

Here is how KTable content looks like when adding new records in Kafka

| Topic(key, value) | KTable              |
|-------------------|---------------------|
| (a,1)             | (a,1)               |
| (b,1)             | (a,1), (b,1)        | 
| (a,2)             | (a,2), (b,1)        |
| (b,2)             | (a,2), (b,2)        |
| (c,3)             | (a,2), (b,2), (c,3) |
| (a,null)          | (b,2), (c,3)        |


### KStream/KTable transformations

There are two main categories of transformations in kafka streams: **stateless** and **stateful** transformations


#### Stateless Transformations
Stateless transformations are transformations which do not require state for processing.
Below you could find a couple of basic stateless transformations examples with processing visualization. 

Detailed documentation can be found at [Stateless Transformations] documentation page.

##### mapValues and map 

Mapping values in record stream

*mapValue* and *map*

e.g.
Let's assume our source is stream of key value events, where **key is an integer** and **value is a string**.

Stream is processed as follows:
```jshelllanguage
// using lambda expression on KStream<byte[],String>
stream.mapValues(value -> value.toUpperCase());
```
Each event in the stream is processed one by one as it's shown on a timeline:
 
```mermaid
timeline
    title map values - uppercase
    1, aaa : 1, AAA
    2, abc : 2, ABC     
    3, san : 3, SAN
    4, you: 4, YOU
```
> TIP: notice that the keys are not changed in the output stream

repartitioning operations
: mapValues is cheaper than map, since map is causing repartitioning,
thus use mapValues whenever possible


##### Filter and filterNot

Filtering records in stream

e.g. remove all records not starting with "a"

```jshelllanguage
// using lambda expression on KStream<byte[],String>
stream.filter((key, value) -> value.startsWith("a"));
```
gives us:
```mermaid
timeline
    title filter values - to only those starting with "a"
    1, aaa : 1, aaa
    2, abc : 2, abc     
    3, san 
    4, you
```
##### Flatmap

Takes one record and produces zero one or more records

e.g. split input messages by white characters

```jshelllanguage
// using lambda expression on KStream<byte[],String>
stream.flatMapValues(value -> value.split("\\s+"));
```
gives us:
```mermaid
timeline
    title flat map split by white chars
    1, cat : 1, cat
    2, dog and cat  : 2, dog     
                    : 2, and
                    : 2, cat
    3, you win! :   3, you
                :   3, win!
```
##### Split

Split stream into branches.

e.g. split input stream records by value into branches **branch-a** and **branch-b** 
by words starting with letter **a** and send them to **output topic** and all other to **dead-end topic**

```jshelllanguage
    var branches = stream.split(Named.as("branch-"))
             .branch((k, v) -> v.startsWith("a"), 
                       Branched.as("a"))
                .branch((k, v) -> !v.startsWith("a"),
                        Branched.as("b"))
                .noDefaultBranch();
    branches.get("branch-a").to(outputTopic, Produced.with(stringSerde, stringSerde));
    branches.get("branch-b").to(deadEndTopic, Produced.with(stringSerde, stringSerde));
```

![Split diagram](split_diagram.svg "split diagram")

#### Stateful Transformations

1) Joining: Enrich an event with additional information or context
that was captured in a separate stream or tableOperators
2) Aggregating: Compute a continuously updating mathematical or
combinatorial transformation of related events
3) Windowing data:Group events that have close temporal proximity

**TODO**



[Stateless Transformations]: https://docs.confluent.io/platform/current/streams/developer-guide/dsl-api.html#stateless-transformations
