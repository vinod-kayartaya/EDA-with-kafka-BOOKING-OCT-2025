# Kafka setup guide for Windows (no docker)

Here’s a **step-by-step guide** to set up Apache Kafka _with_ Apache ZooKeeper on **Windows**.

## Prerequisites

1. You already have **JDK (11+)** installed. Just verify via:

   ```cmd
   java -version
   ```

2. Pick a simple path for Kafka, e.g., `C:\kafka`. Avoid paths with spaces such as `C:\Program Files\…` (which often lead to problems).

3. Ensure you have sufficient permissions for folders (read/write), and that firewall/antivirus aren’t blocking ports (default: 2181 for ZooKeeper, 9092 for Kafka).

## Step 1: Download & extract Kafka

1. Go to the Kafka download page and get a Kafka binary that supports ZooKeeper (most versions before the latest “KRaft-only” migrations do).
2. Extract the ZIP (or .tgz) into a folder, e.g., `C:\kafka\kafka_2.13-<version>`.
3. For ease, you may rename the folder to `C:\kafka`.

## Step 2: Configure ZooKeeper & Kafka for Windows

1. In `C:\kafka\config\zookeeper.properties` open the file. Locate `dataDir=` and set a path where ZooKeeper can store its data. Example:

   ```properties
   dataDir=C:\\kafka\\zookeeper-data
   ```

   Note the use of forward slashes or double backslashes.

2. In `C:\kafka\config\server.properties` open it. Modify `log.dirs=` to a folder, e.g.:

   ```properties
   log.dirs=C:\\kafka\\kafka-logs
   ```

   Again, ensure path is valid and writable.

3. (Optional) If you want you can change listeners etc., but for a local single-broker setup defaults are fine.

## Step 3: Start ZooKeeper

Open **Command Prompt** (preferably _as Administrator_, though not always required). Navigate to your Kafka folder:

```cmd
cd C:\kafka
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
```

You should see logs indicating ZooKeeper started on port 2181 (default).

Leave this window open.

## Step 4: Start Kafka Broker

Open a **new** Command Prompt window. Navigate again to the Kafka root:

```cmd
cd C:\kafka
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

You should see logs about the broker starting and bounding to port 9092. Since ZooKeeper is running, Kafka will register with it.

## Step 5: Test – create topic, produce & consume

1. **Create a topic**:

   ```cmd
   .\bin\windows\kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic my-topic
   ```

2. **Produce messages**:

   ```cmd
   .\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic my-topic
   ```

   Then type e.g., `hello from Kafka` + ENTER.

3. **Consume messages**:

   ```cmd
   .\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic my-topic --from-beginning
   ```

   You should see the message(s) you sent.

## Step 6: Stop & cleanup

- To stop Kafka, close the Kafka broker window (Ctrl +C).
- To stop ZooKeeper, close the ZooKeeper window (Ctrl +C).
- If you want a completely clean restart later: delete the `zookeeper-data` folder and `kafka-logs` folder (or whichever you configured). Also check `meta.properties` issues.
