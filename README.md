## Kafka Streams Demo

This project is meant to explain how Kafka Streams and KsqlDB works

Open [Kafka streams tutorial] in the docs folder.

For more information see [Kafka Streams docs] and [Ksql DB reference] 


### How to run

#### start containers

```shell
cd docker
docker-compose up -d
```

#### create input and output topics
create **input-topics**
```shell
docker exec -it broker kafka-topics \
    --bootstrap-server localhost:9092 \
    --create \
    --partitions 3 \
    --topic 'input-topic-A'
```
```shell
docker exec -it broker kafka-topics \
    --bootstrap-server localhost:9092 \
    --create \
    --partitions 3 \
    --topic 'input-topic-B'
```

```shell
docker exec -it broker kafka-topics \
    --bootstrap-server localhost:9092 \
    --create \
    --partitions 3 \
    --topic 'input-topic-message'
```


```shell
docker exec -it broker kafka-topics \
    --bootstrap-server localhost:9092 \
    --config "cleanup.policy=compact" \
    --create \
    --partitions 3 \
    --topic 'input-topic-lookup'
```



create **output-topic**
```shell
docker exec -it broker kafka-topics \
    --bootstrap-server localhost:9092 \
    --create \
    --partitions 3 \
    --topic 'output-topic'
```

create **dead-end-topic**
```shell
docker exec -it broker kafka-topics \
    --bootstrap-server localhost:9092 \
    --create \
    --partitions 3 \
    --topic 'dead-end-topic'
```
#### start application
###### Optional step: restore gradle wrapper
```shell
gradle wrapper
```
###### start solution
```shell
./gradlew run
```
#### Once you finish, clean up all containers 

###### stop and remove containers
```shell
cd docker
docker-compose down
```

[Kafka Streams docs]: https://kafka.apache.org/34/documentation/streams/
[Ksql DB reference]: https://docs.ksqldb.io/en/latest/reference/
[Kafka streams tutorial]: ./docs/kafka_streams_tutorial.md
