#!/bin/bash

echo "Waiting for Kafka to be ready..."
cub kafka-ready -b kafka-1:9092 1 60

echo "Creating topics..."

kafka-topics --create --if-not-exists --topic exchange-events \
  --bootstrap-server kafka-1:9092 \
  --replication-factor 3 \
  --partitions 3

kafka-topics --create --if-not-exists --topic notification-events \
  --bootstrap-server kafka-1:9092 \
  --replication-factor 3 \
  --partitions 3

kafka-topics --create --if-not-exists --topic book-events \
  --bootstrap-server kafka-1:9092 \
  --replication-factor 3 \
  --partitions 3

kafka-topics --create --if-not-exists --topic book-command-events \
  --bootstrap-server kafka-1:9092 \
  --replication-factor 3 \
  --partitions 3

kafka-topics --create --if-not-exists --topic book-domain-events \
  --bootstrap-server kafka-1:9092 \
  --replication-factor 3 \
  --partitions 3

kafka-topics --create --if-not-exists --topic file-events \
  --bootstrap-server kafka-1:9092 \
  --replication-factor 3 \
  --partitions 3

echo "Topics created successfully!"

kafka-topics --list --bootstrap-server kafka-1:9092

echo "Topic details:"
kafka-topics --describe --topic exchange-events --bootstrap-server kafka-1:9092
kafka-topics --describe --topic notification-events --bootstrap-server kafka-1:9092
kafka-topics --describe --topic book-events --bootstrap-server kafka-1:9092
kafka-topics --describe --topic book-command-events --bootstrap-server kafka-1:9092
kafka-topics --describe --topic book-domain-events --bootstrap-server kafka-1:9092