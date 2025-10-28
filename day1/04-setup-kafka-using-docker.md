## Quick prerequisites

1. Docker installed and running (Engine + CLI).
2. Docker Compose (v2 preferred — `docker compose` or `docker-compose`).
3. Terminal / shell access.

## Option A — Simple single-node Kafka (Zookeeper) using Docker Compose

Create a file named `docker-compose.yml`:

```yaml
version: "3.8"
services:
  zookeeper:
    image: bitnami/zookeeper:latest
    container_name: zk
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
    ports:
      - "2181:2181"
    volumes:
      - zk_data:/bitnami

  kafka:
    image: bitnami/kafka:latest
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092
      # For local dev advertise the host IP or "localhost" so clients outside container can reach it:
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
      - KAFKA_AUTO_CREATE_TOPICS_ENABLE=true
      - ALLOW_PLAINTEXT_LISTENER=yes
    volumes:
      - kafka_data:/bitnami

volumes:
  zk_data:
  kafka_data:
```

### Start the stack

```bash
docker compose up -d
# or: docker-compose up -d
```

### Verify containers are running

```bash
docker ps
# should show containers named "zk" and "kafka"
```

### Create a topic (inside kafka container)

```bash
docker exec -it kafka bash
# once inside container:
kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --list --bootstrap-server localhost:9092
exit
```

### Produce and consume

Producer (from your host using `docker exec`):

```bash
docker exec -it kafka kafka-console-producer.sh --topic test-topic --bootstrap-server localhost:9092
# type lines; Ctrl+C to exit
```

Consumer (open new terminal):

```bash
docker exec -it kafka kafka-console-consumer.sh --topic test-topic --bootstrap-server localhost:9092 --from-beginning
# you will see lines typed by producer
```

## Option B — Single-node Kafka in KRaft (ZooKeeper-less) — modern mode (single process)

> Short note: Kafka newer versions support KRaft (no external ZooKeeper). Example below uses `confluentinc/cp-kafka`/other images typically; configuring KRaft precisely depends on image version. If you prefer KRaft, I can provide a tested Compose that matches your Kafka image/version. For stability and simple learning, the Zookeeper example above is the most portable.

(If you want KRaft now, tell me your preferred Kafka image/version — I’ll provide a ready Compose.)

## Option C — Minimal multi-broker cluster (2 brokers + ZK) — Compose example

This is an example pattern — multi-broker requires unique broker IDs, persistence, and proper advertised listeners. Put this in `docker-compose-cluster.yml`.

```yaml
version: "3.8"
services:
  zookeeper:
    image: bitnami/zookeeper:latest
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
    ports:
      - "2181:2181"

  kafka1:
    image: bitnami/kafka:latest
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
      - ALLOW_PLAINTEXT_LISTENER=yes
    ports:
      - "9092:9092"

  kafka2:
    image: bitnami/kafka:latest
    environment:
      - KAFKA_BROKER_ID=2
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9093
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9093
      - ALLOW_PLAINTEXT_LISTENER=yes
    ports:
      - "9093:9093"
```

**Important**: With multiple brokers on same host you must ensure ports, advertised listeners, and host bindings are correct. For production or multiple hosts, set `KAFKA_ADVERTISED_LISTENERS` to the broker's actual reachable IP/hostname.

## Important detailed explanations (step-by-step rationale & gotchas)

1. **Why Zookeeper? (classic mode)**

   - Zookeeper stores cluster metadata and manages leader election for partition leaders. Classic Kafka deployments used Zookeeper; it’s still widely used and well supported.
   - If you use KRaft (Kafka Raft metadata mode), ZooKeeper is removed. KRaft setup differs (controller nodes, quorum, configs). Use KRaft for newer Kafka versions once you fully test it.

2. **Key environment variables explained**

   - `KAFKA_BROKER_ID`: unique integer id for broker in cluster.
   - `KAFKA_ZOOKEEPER_CONNECT`: where to find ZK (host:port).
   - `KAFKA_LISTENERS`: where broker binds for incoming connections inside container.
   - `KAFKA_ADVERTISED_LISTENERS`: what the broker tells clients to use to reach it. For host testing set to `localhost:9092`. In real multi-host clusters use the host’s network-accessible hostname/IP.
   - `ALLOW_PLAINTEXT_LISTENER` (Bitnami): allows non-SSL plaintext; useful for local dev only.

3. **Advertised listeners — the most common problem**

   - If `ADVERTISED_LISTENERS` is wrong, clients cannot connect. Example: if broker advertises `kafka:9092` but a client running on host tries `localhost:9092`, the client will get unreachable address. For local dev, advertise `localhost` or your host IP. For docker networked apps, advertise the container name.

4. **Ports and host mapping**

   - When you map `9092:9092`, you expose the broker to the host. For multi-broker on one machine you must map distinct host ports (e.g., `9092:9092`, `9093:9093`) and set matching `ADVERTISED_LISTENERS`.

5. **Data persistence**

   - The `volumes:` lines preserve data across container restarts. Without volumes, topic data is lost if container removed. In Compose we used named volumes `kafka_data`, `zk_data`.

6. **Replication factor and partition strategy**

   - When creating topics for multi-broker setups pick `--replication-factor` ≤ number of brokers. Replication ensures fault tolerance: if a leader broker dies, a replica can take over.
   - For local single-broker `--replication-factor 1` is necessary.

7. **Create topics vs auto-create**

   - We set `KAFKA_AUTO_CREATE_TOPICS_ENABLE=true` for convenience. In production it’s better to set it to `false` and create topics explicitly with desired partitions/replication.

8. **Resource/config tuning for production**

   - Memory / Java heap: configure `KAFKA_HEAP_OPTS` if needed.
   - Disk: ensure adequate disk and fsync behavior; use dedicated disks for logs.
   - Network: use separate network interfaces for production brokers.
   - Security: enable TLS and SASL (SASL/PLAIN, SCRAM, or Kerberos) — do not use plaintext in production.

9. **Testing tips**

   - Use `kafka-topics.sh --describe` to check partition leaders and ISR (in-sync replicas).
   - `kafka-consumer-groups.sh` helps inspect consumer offsets and lag.
   - To test durability, produce messages, stop broker, start again and consume `--from-beginning`.

10. **Scaling to multiple brokers**

    - Add more kafka services with unique `KAFKA_BROKER_ID` and volumes.
    - In production across hosts: use host networking, or configure listeners with external IPs and ensure firewall rules allow inter-broker traffic (port 9092 and inter-broker listener ports).

11. **KRaft (ZooKeeper-less) considerations**

    - KRaft simplifies metadata management but needs careful controller/quorum setup.
    - Not every docker image or version has the same KRaft entrypoints — verify image docs before using KRaft in Compose.

12. **Common errors & fixes**

    - `Connection refused` from client: check `ADVERTISED_LISTENERS` and host port mapping.
    - `No coordinator found` / `GroupCoordinator` errors: ensure broker is reachable and consumer group config correct.
    - `Leader not available`: check broker logs, ensure brokers registered with ZK (or KRaft controller) and replication factor ≤ brokers.

## Useful docker commands summary

```bash
docker compose up -d
docker compose logs -f kafka     # follow logs
docker exec -it kafka bash       # enter kafka container
docker compose down              # stop and remove containers (volumes persist unless --volumes)
docker compose down -v           # stop and remove volumes too
```

## Security & production checklist (short)

- Do not use plaintext listeners on public networks. Enable TLS (SSL) and authentication (SASL/SCRAM).
- Use multiple brokers across multiple hosts for HA.
- Use topic replication and appropriate `min.insync.replicas`.
- Monitor disk, CPU, GC, network, and consumer lag (Prometheus + JMX exporter + Grafana common).
- Use proper backup strategy for critical topics (mirror-maker or connect sinks).

# KRaft mode

Here is a **ready-to-run Docker Compose file** for **Apache Kafka in KRaft mode (no ZooKeeper)**.
This uses **Bitnami’s Kafka image** (which supports KRaft mode out-of-the-box), is clean, production-aligned, and perfect for local testing or demonstrations.

## 🧩 **Docker Compose — Kafka (KRaft mode, no ZooKeeper)**

Create a file named **`docker-compose.yml`** in an empty folder:

```yaml
version: "3.8"

services:
  kafka:
    image: bitnami/kafka:3.7.0
    container_name: kafka
    restart: unless-stopped
    ports:
      - "9092:9092"
    environment:
      # Kafka KRaft mode
      - KAFKA_CFG_PROCESS_ROLES=broker,controller
      - KAFKA_CFG_NODE_ID=1
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@localhost:9093
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER
      - KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT
      # Basic settings
      - KAFKA_CFG_LOG_DIRS=/bitnami/kafka/data
      - KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true
      - KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR=1
      - KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1
      - KAFKA_CFG_TRANSACTION_STATE_LOG_MIN_ISR=1
      - ALLOW_PLAINTEXT_LISTENER=yes
    volumes:
      - kafka_data:/bitnami/kafka

volumes:
  kafka_data:
```

## 🚀 **Steps to Run**

### 1. Start Kafka

```bash
docker compose up -d
```

Wait for about **20–30 seconds** while Kafka initializes the **KRaft metadata log** (it will create it automatically if it doesn’t exist).

### 2. Verify Kafka is running

```bash
docker ps
```

You should see something like:

```
CONTAINER ID   IMAGE                   STATUS         PORTS
xxxxxx         bitnami/kafka:3.7.0     Up 20 seconds  0.0.0.0:9092->9092/tcp
```

### 3. Enter Kafka container

```bash
docker exec -it kafka bash
```

### 4. Create a topic

```bash
kafka-topics.sh --create \
  --topic test-topic \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

Check the topic:

```bash
kafka-topics.sh --list --bootstrap-server localhost:9092
```

### 5. Produce and consume messages

Producer:

```bash
kafka-console-producer.sh --topic test-topic --bootstrap-server localhost:9092
# Type messages, press Ctrl+C to exit
```

Consumer:

```bash
kafka-console-consumer.sh --topic test-topic --bootstrap-server localhost:9092 --from-beginning
```

## 🧠 **Detailed Explanation of Important Configurations**

| Variable                                              | Description                                                                                                                             |
| ----------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| `KAFKA_CFG_PROCESS_ROLES=broker,controller`           | Enables both **broker** and **controller** roles in the same process (valid for single-node setup). In production, these are separated. |
| `KAFKA_CFG_NODE_ID=1`                                 | Unique numeric ID for this broker in the KRaft cluster.                                                                                 |
| `KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@localhost:9093` | Defines the controller quorum: here only one node (ID 1) using port 9093 for the controller.                                            |
| `KAFKA_CFG_LISTENERS`                                 | Defines listeners: one for brokers (`PLAINTEXT://:9092`) and one for controller (`CONTROLLER://:9093`).                                 |
| `KAFKA_CFG_ADVERTISED_LISTENERS`                      | What clients outside the container should use to connect — here it’s `localhost:9092`.                                                  |
| `KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP`            | Maps listener names to security protocols. Both are plaintext in this example.                                                          |
| `KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER`      | Indicates which listener(s) the controller role uses.                                                                                   |
| `KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT`      | Defines the listener for inter-broker communication.                                                                                    |
| `KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true`            | Automatically creates topics when producers write to them. Useful for dev/testing.                                                      |
| `ALLOW_PLAINTEXT_LISTENER=yes`                        | Allows non-TLS connections — required for local usage.                                                                                  |
| `volumes`                                             | Ensures data persistence between restarts under `/bitnami/kafka`.                                                                       |

## ⚙️ **Why KRaft?**

KRaft (**Kafka Raft Metadata mode**) replaces **ZooKeeper** by storing and managing all cluster metadata within Kafka itself.
Advantages include:

- Simplified deployment (no external Zookeeper dependency)
- Faster startup and metadata recovery
- Unified configuration and security model
- Easier scaling and maintenance

## 🧪 **Common Issues and Fixes**

| Issue                        | Likely Cause                 | Fix                                                           |
| ---------------------------- | ---------------------------- | ------------------------------------------------------------- |
| `Connection refused`         | Wrong `ADVERTISED_LISTENERS` | Set `localhost:9092` for local testing.                       |
| `Failed to elect controller` | Metadata log not initialized | Let container run once; it auto-initializes the metadata log. |
| `Topic creation fails`       | Replication factor > brokers | Use `--replication-factor 1` for single node.                 |
| `Producer timeout`           | Firewall or port blocked     | Ensure port `9092` is open and mapped correctly.              |

## 🧩 **Optional: Clean up everything**

```bash
docker compose down -v
```

Removes containers and persistent volumes (topics/data).
