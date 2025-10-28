import os
from kafka import KafkaConsumer
from dotenv import load_dotenv

load_dotenv()

bootstrap_servers = os.getenv("KAFKA_BOOTSTRAP_SERVERS")
topic_name = os.getenv("TOPIC_NAME")
group_id = os.getenv("GROUP_ID")

consumer = KafkaConsumer(
    topic_name,
    bootstrap_servers=bootstrap_servers,
    auto_offset_reset="earliest",
    enable_auto_commit=True,
    group_id=group_id,
    value_deserializer=lambda x: x.decode("utf-8")
)

print(f"Connected to Kafka at {bootstrap_servers}")
print(f"Listening on topic: {topic_name}")

try:
    for message in consumer:
        print(f"Received => Partition: {message.partition} | Offset: {message.offset} | Value: {message.value}")
except KeyboardInterrupt:
    print("Consumer stopped")
finally:
    consumer.close()