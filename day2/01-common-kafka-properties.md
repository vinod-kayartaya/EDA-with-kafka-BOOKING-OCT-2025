# Kafka Producer Properties

| Property                                | Description                                                     | Default                     |
| --------------------------------------- | --------------------------------------------------------------- | --------------------------- |
| `bootstrap.servers`                     | Initial broker list used for metadata lookup                    | No default (required)       |
| `key.serializer`                        | Class for serializing keys                                      | No default (required)       |
| `value.serializer`                      | Class for serializing values                                    | No default (required)       |
| `acks`                                  | Required broker acknowledgment level: `0`, `1`, `all`           | `1`                         |
| `retries`                               | Retries on failure (deprecated in favor of delivery.timeout.ms) | `2147483647` in new clients |
| `delivery.timeout.ms`                   | Total time for retries and request timeout                      | `120000` (2m)               |
| `linger.ms`                             | Wait time to batch messages before sending                      | `0`                         |
| `batch.size`                            | Size of batch buffer (per partition) in bytes                   | `16384` (16 KB)             |
| `buffer.memory`                         | Total producer memory buffer                                    | `33554432` (32 MB)          |
| `compression.type`                      | `none`, `gzip`, `snappy`, `lz4`, `zstd`                         | `none`                      |
| `enable.idempotence`                    | Guarantees no duplicates when retrying                          | `false`                     |
| `max.in.flight.requests.per.connection` | Concurrent inflight batches to maintain order                   | `5`                         |
| `request.timeout.ms`                    | How long the broker can take to respond                         | `30000`                     |
| `metadata.max.age.ms`                   | Metadata refresh interval                                       | `300000` (5m)               |

# Kafka Consumer Properties

| Property                  | Description                                                | Default                                      |
| ------------------------- | ---------------------------------------------------------- | -------------------------------------------- |
| `bootstrap.servers`       | Initial broker list                                        | No default (required)                        |
| `key.deserializer`        | Class for deserializing keys                               | No default (required)                        |
| `value.deserializer`      | Class for deserializing values                             | No default (required)                        |
| `group.id`                | Consumer group identifier                                  | No default (required for group coordination) |
| `auto.offset.reset`       | Offset behavior if none exists: `latest`, `earliest`       | `latest`                                     |
| `enable.auto.commit`      | Auto commit offsets if true                                | `true`                                       |
| `auto.commit.interval.ms` | Interval for auto commit                                   | `5000`                                       |
| `max.poll.records`        | Max records per poll                                       | `500`                                        |
| `max.poll.interval.ms`    | Max delay between polls before consumer is considered dead | `300000` (5m)                                |
| `session.timeout.ms`      | Heartbeat timeout for group membership                     | `10000`                                      |
| `fetch.min.bytes`         | Minimum data fetched per request                           | `1` byte                                     |
| `fetch.max.wait.ms`       | Max wait before replying with data                         | `500`                                        |
| `isolation.level`         | For transactions: `read_committed` or `read_uncommitted`   | `read_uncommitted`                           |

# Topic-level and Broker-side Properties (Operational)

| Property                         | Description                                | Default                                      |
| -------------------------------- | ------------------------------------------ | -------------------------------------------- |
| `num.partitions`                 | Default number of partitions per new topic | `1`                                          |
| `replication.factor`             | Default replicas per partition             | Must be set by admin                         |
| `retention.ms`                   | How long topic data is retained            | `7 days`                                     |
| `retention.bytes`                | Max size for topic log segments            | Unlimited (-1)                               |
| `segment.bytes`                  | Log segment file size limit                | `1 GB`                                       |
| `cleanup.policy`                 | `delete` or `compact`                      | `delete`                                     |
| `min.insync.replicas`            | Min replicas that must acknowledge writes  | `1`                                          |
| `unclean.leader.election.enable` | Whether non-ISR leaders can be elected     | `true` in older installs, better set `false` |

# Very practical tuning guidance

If your goal is safety (no lost messages):

- `acks=all`
- `min.insync.replicas` >= 2
- `enable.idempotence=true`
- Keep `max.in.flight.requests.per.connection=1` if strict ordering needed

If your goal is high throughput:

- Increase `batch.size` and `buffer.memory`
- Use compression like `lz4` or `zstd`
- Slightly increase `linger.ms` to allow batching

If your goal is lowest latency testing:

- `acks=1`, `linger.ms=0`, low batch size
- Expect more broker stress and higher risk of loss
