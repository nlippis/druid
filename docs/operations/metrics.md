---
id: metrics
title: "Metrics"
---

<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->


You can configure Druid to [emit metrics](../configuration/index.md#enabling-metrics) that are essential for monitoring query execution, ingestion, coordination, and so on.

All Druid metrics share a common set of fields:

* `timestamp`: the time the metric was created
* `metric`: the name of the metric
* `service`: the service name that emitted the metric
* `host`: the host name that emitted the metric
* `value`: some numeric value associated with the metric

Metrics may have additional dimensions beyond those listed above.

> Most metric values reset each emission period, as specified in `druid.monitoring.emissionPeriod`.

## Query metrics

### Router
|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`query/time`|Milliseconds taken to complete a query.|Native Query: `dataSource`, `type`, `interval`, `hasFilters`, `duration`, `context`, `remoteAddress`, `id`.|< 1s|

### Broker

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`query/time`|Milliseconds taken to complete a query.|<p>Common: `dataSource`, `type`, `interval`, `hasFilters`, `duration`, `context`, `remoteAddress`, `id`.</p><p>Aggregation Queries: `numMetrics`, `numComplexMetrics`.</p><p>GroupBy: `numDimensions`.</p><p> TopN: `threshold`, `dimension`.</p>|< 1s|
|`query/bytes`|The total number of bytes returned to the requesting client in the query response from the broker. Other services report the total bytes for their portion of the query. |<p>Common: `dataSource`, `type`, `interval`, `hasFilters`, `duration`, `context`, `remoteAddress`, `id`.</p><p> Aggregation Queries: `numMetrics`, `numComplexMetrics`.</p><p> GroupBy: `numDimensions`.</p><p> TopN: `threshold`, `dimension`.</p>| |
|`query/node/time`|Milliseconds taken to query individual historical/realtime processes.|`id`, `status`, `server`|< 1s|
|`query/node/bytes`|Number of bytes returned from querying individual historical/realtime processes.|`id`, `status`, `server`| |
|`query/node/ttfb`|Time to first byte. Milliseconds elapsed until Broker starts receiving the response from individual historical/realtime processes.|`id`, `status`, `server`|< 1s|
|`query/node/backpressure`|Milliseconds that the channel to this process has spent suspended due to backpressure.|`id`, `status`, `server`.| |
|`query/count`|Number of total queries.|This metric is only available if the `QueryCountStatsMonitor` module is included.| |
|`query/success/count`|Number of queries successfully processed.|This metric is only available if the `QueryCountStatsMonitor` module is included.| |
|`query/failed/count`|Number of failed queries.|This metric is only available if the `QueryCountStatsMonitor` module is included.| |
|`query/interrupted/count`|Number of queries interrupted due to cancellation.|This metric is only available if the `QueryCountStatsMonitor` module is included.| |
|`query/timeout/count`|Number of timed out queries.|This metric is only available if the `QueryCountStatsMonitor` module is included.| |
|`query/segments/count`|This metric is not enabled by default. See the `QueryMetrics` Interface for reference regarding enabling this metric. Number of segments that will be touched by the query. In the broker, it makes a plan to distribute the query to realtime tasks and historicals based on a snapshot of segment distribution state. If there are some segments moved after this snapshot is created, certain historicals and realtime tasks can report those segments as missing to the broker. The broker will resend the query to the new servers that serve those segments after move. In this case, those segments can be counted more than once in this metric.||Varies|
|`query/priority`|Assigned lane and priority, only if Laning strategy is enabled. Refer to [Laning strategies](../configuration/index.md#laning-strategies)|`lane`, `dataSource`, `type`|0|
|`sqlQuery/time`|Milliseconds taken to complete a SQL query.|`id`, `nativeQueryIds`, `dataSource`, `remoteAddress`, `success`, `engine`|< 1s|
|`sqlQuery/planningTimeMs`|Milliseconds taken to plan a SQL to native query.|`id`, `nativeQueryIds`, `dataSource`, `remoteAddress`, `success`, `engine`| |
|`sqlQuery/bytes`|Number of bytes returned in the SQL query response.|`id`, `nativeQueryIds`, `dataSource`, `remoteAddress`, `success`, `engine`| |
|`init/serverview/time`|Time taken to initialize the broker server view. Useful to detect if brokers are taking too long to start.||Depends on the number of segments.|
|`init/metadatacache/time`|Time taken to initialize the broker segment metadata cache. Useful to detect if brokers are taking too long to start||Depends on the number of segments.|

### Historical

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`query/time`|Milliseconds taken to complete a query.|<p>Common: `dataSource`, `type`, `interval`, `hasFilters`, `duration`, `context`, `remoteAddress`, `id`.</p><p> Aggregation Queries: `numMetrics`, `numComplexMetrics`.</p><p> GroupBy: `numDimensions`.</p><p> TopN: `threshold`, `dimension`.</p>|< 1s|
|`query/segment/time`|Milliseconds taken to query individual segment. Includes time to page in the segment from disk.|`id`, `status`, `segment`, `vectorized`.|several hundred milliseconds|
|`query/wait/time`|Milliseconds spent waiting for a segment to be scanned.|`id`, `segment`|< several hundred milliseconds|
|`segment/scan/pending`|Number of segments in queue waiting to be scanned.||Close to 0|
|`query/segmentAndCache/time`|Milliseconds taken to query individual segment or hit the cache (if it is enabled on the Historical process).|`id`, `segment`|several hundred milliseconds|
|`query/cpu/time`|Microseconds of CPU time taken to complete a query.|<p>Common: `dataSource`, `type`, `interval`, `hasFilters`, `duration`, `context`, `remoteAddress`, `id`.</p><p> Aggregation Queries: `numMetrics`, `numComplexMetrics`.</p><p> GroupBy: `numDimensions`.</p><p> TopN: `threshold`, `dimension`.</p>|Varies|
|`query/count`|Total number of queries.|This metric is only available if the `QueryCountStatsMonitor` module is included.||
|`query/success/count`|Number of queries successfully processed.|This metric is only available if the `QueryCountStatsMonitor` module is included.||
|`query/failed/count`|Number of failed queries.|This metric is only available if the `QueryCountStatsMonitor` module is included.||
|`query/interrupted/count`|Number of queries interrupted due to cancellation.|This metric is only available if the `QueryCountStatsMonitor` module is included.||
|`query/timeout/count`|Number of timed out queries.|This metric is only available if the `QueryCountStatsMonitor` module is included.||

### Real-time

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`query/time`|Milliseconds taken to complete a query.|<p>Common: `dataSource`, `type`, `interval`, `hasFilters`, `duration`, `context`, `remoteAddress`, `id`.</p><p> Aggregation Queries: `numMetrics`, `numComplexMetrics`.</p><p> GroupBy: `numDimensions`.</p><p> TopN: `threshold`, `dimension`.</p>|< 1s|
|`query/wait/time`|Milliseconds spent waiting for a segment to be scanned.|`id`, `segment`|several hundred milliseconds|
|`segment/scan/pending`|Number of segments in queue waiting to be scanned.||Close to 0|
|`query/cpu/time`|Microseconds of CPU time taken to complete a query.|<p>Common: `dataSource`, `type`, `interval`, `hasFilters`, `duration`, `context`, `remoteAddress`, `id`.</p><p> Aggregation Queries: `numMetrics`, `numComplexMetrics`.</p><p> GroupBy: `numDimensions`. </p><p>TopN: `threshold`, `dimension`.</p>|Varies|
|`query/count`|Number of total queries.|This metric is only available if the `QueryCountStatsMonitor` module is included.||
|`query/success/count`|Number of queries successfully processed.|This metric is only available if the `QueryCountStatsMonitor` module is included.||
|`query/failed/count`|Number of failed queries.|This metric is only available if the `QueryCountStatsMonitor` module is included.||
|`query/interrupted/count`|Number of queries interrupted due to cancellation.|This metric is only available if the `QueryCountStatsMonitor` module is included.||
|`query/timeout/count`|Number of timed out queries.|This metric is only available if the `QueryCountStatsMonitor` module is included.||

### Jetty

|Metric|Description|Normal Value|
|------|-----------|------------|
|`jetty/numOpenConnections`|Number of open jetty connections.|Not much higher than number of jetty threads.|
|`jetty/threadPool/total`|Number of total workable threads allocated.|The number should equal to `threadPoolNumIdleThreads` + `threadPoolNumBusyThreads`.|
|`jetty/threadPool/idle`|Number of idle threads.|Less than or equal to `threadPoolNumTotalThreads`. Non zero number means there is less work to do than configured capacity.|
|`jetty/threadPool/busy`|Number of busy threads that has work to do from the worker queue.|Less than or equal to `threadPoolNumTotalThreads`.|
|`jetty/threadPool/isLowOnThreads`|A rough indicator of whether number of total workable threads allocated is enough to handle the works in the work queue.|0|
|`jetty/threadPool/min`|Number of minimum threads allocatable.|`druid.server.http.numThreads` plus a small fixed number of threads allocated for Jetty acceptors and selectors.|
|`jetty/threadPool/max`|Number of maximum threads allocatable.|`druid.server.http.numThreads` plus a small fixed number of threads allocated for Jetty acceptors and selectors.|
|`jetty/threadPool/queueSize`|Size of the worker queue.|Not much higher than `druid.server.http.queueSize`.|

### Cache

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`query/cache/delta/*`|Cache metrics since the last emission.||N/A|
|`query/cache/total/*`|Total cache metrics.||N/A|
|`*/numEntries`|Number of cache entries.||Varies|
|`*/sizeBytes`|Size in bytes of cache entries.||Varies|
|`*/hits`|Number of cache hits.||Varies|
|`*/misses`|Number of cache misses.||Varies|
|`*/evictions`|Number of cache evictions.||Varies|
|`*/hitRate`|Cache hit rate.||~40%|
|`*/averageByte`|Average cache entry byte size.||Varies|
|`*/timeouts`|Number of cache timeouts.||0|
|`*/errors`|Number of cache errors.||0|
|`*/put/ok`|Number of new cache entries successfully cached.||Varies, but more than zero|
|`*/put/error`|Number of new cache entries that could not be cached due to errors.||Varies, but more than zero|
|`*/put/oversized`|Number of potential new cache entries that were skipped due to being too large (based on `druid.{broker,historical,realtime}.cache.maxEntrySize` properties).||Varies|

#### Memcached only metrics

Memcached client metrics are reported as per the following. These metrics come directly from the client as opposed to from the cache retrieval layer.

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`query/cache/memcached/total`|Cache metrics unique to memcached (only if `druid.cache.type=memcached`) as their actual values.|Variable|N/A|
|`query/cache/memcached/delta`|Cache metrics unique to memcached (only if `druid.cache.type=memcached`) as their delta from the prior event emission.|Variable|N/A|

## SQL Metrics

If SQL is enabled, the Broker will emit the following metrics for SQL.

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`sqlQuery/time`|Milliseconds taken to complete a SQL.|`id`, `nativeQueryIds`, `dataSource`, `remoteAddress`, `success`|< 1s|
|`sqlQuery/planningTimeMs`|Milliseconds taken to plan a SQL to native query.|`id`, `nativeQueryIds`, `dataSource`, `remoteAddress`, `success`| |
|`sqlQuery/bytes`|number of bytes returned in SQL response.|`id`, `nativeQueryIds`, `dataSource`, `remoteAddress`, `success`| |

## Ingestion metrics

## General native ingestion metrics

|Metric|Description| Dimensions                                              |Normal Value|
|------|-----------|---------------------------------------------------------|------------|
|`ingest/count`|Count of `1` every time an ingestion job runs (includes compaction jobs). Aggregate using dimensions. | `dataSource`, `taskId`, `taskType`, `groupId`, `taskIngestionMode`, `tags` |Always `1`.|
|`ingest/segments/count`|Count of final segments created by job (includes tombstones). | `dataSource`, `taskId`, `taskType`, `groupId`, `taskIngestionMode`, `tags` |At least `1`.|
|`ingest/tombstones/count`|Count of tombstones created by job. | `dataSource`, `taskId`, `taskType`, `groupId`, `taskIngestionMode`, `tags` |Zero or more for replace. Always zero for non-replace tasks (always zero for legacy replace, see below).|

The `taskIngestionMode` dimension includes the following modes: 
* `APPEND`: a native ingestion job appending to existing segments 
* `REPLACE_LEGACY`: the original replace before tombstones
* `REPLACE`: a native ingestion job replacing existing segments using tombstones

The mode is decided using the values
of the `isAppendToExisting` and `isDropExisting` flags in the
task's `IOConfig` as follows:

| `isAppendToExisting` | `isDropExisting` | mode |
|----------------------|-------------------|------|
| `true`               | `false` | `APPEND`|
| `true`               | `true  ` | Invalid combination, exception thrown. |
| `false`              | `false` | `REPLACE_LEGACY` (this is the default for native batch ingestion). |
 | `false`              | `true` | `REPLACE`|

The `tags` dimension is reported only for metrics emitted from ingestion tasks whose ingest spec specifies the `tags`
field in the `context` field of the ingestion spec. `tags` is expected to be a map of string to object.  

### Ingestion metrics for Kafka

These metrics apply to the [Kafka indexing service](../development/extensions-core/kafka-ingestion.md).

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`ingest/kafka/lag`|Total lag between the offsets consumed by the Kafka indexing tasks and latest offsets in Kafka brokers across all partitions. Minimum emission period for this metric is a minute.|`dataSource`, `stream`, `tags`|Greater than 0, should not be a very high number. |
|`ingest/kafka/maxLag`|Max lag between the offsets consumed by the Kafka indexing tasks and latest offsets in Kafka brokers across all partitions. Minimum emission period for this metric is a minute.|`dataSource`, `stream`, `tags`|Greater than 0, should not be a very high number. |
|`ingest/kafka/avgLag`|Average lag between the offsets consumed by the Kafka indexing tasks and latest offsets in Kafka brokers across all partitions. Minimum emission period for this metric is a minute.|`dataSource`, `stream`, `tags`|Greater than 0, should not be a very high number. |
|`ingest/kafka/partitionLag`|Partition-wise lag between the offsets consumed by the Kafka indexing tasks and latest offsets in Kafka brokers. Minimum emission period for this metric is a minute.|`dataSource`, `stream`, `partition`, `tags`|Greater than 0, should not be a very high number. |

### Ingestion metrics for Kinesis

These metrics apply to the [Kinesis indexing service](../development/extensions-core/kinesis-ingestion.md).

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`ingest/kinesis/lag/time`|Total lag time in milliseconds between the current message sequence number consumed by the Kinesis indexing tasks and latest sequence number in Kinesis across all shards. Minimum emission period for this metric is a minute.|`dataSource`, `stream`, `tags`|Greater than 0, up to max Kinesis retention period in milliseconds. |
|`ingest/kinesis/maxLag/time`|Max lag time in milliseconds between the current message sequence number consumed by the Kinesis indexing tasks and latest sequence number in Kinesis across all shards. Minimum emission period for this metric is a minute.|`dataSource`, `stream`, `tags`|Greater than 0, up to max Kinesis retention period in milliseconds. |
|`ingest/kinesis/avgLag/time`|Average lag time in milliseconds between the current message sequence number consumed by the Kinesis indexing tasks and latest sequence number in Kinesis across all shards. Minimum emission period for this metric is a minute.|`dataSource`, `stream`, `tags`|Greater than 0, up to max Kinesis retention period in milliseconds. |
|`ingest/kinesis/partitionLag/time`|Partition-wise lag time in milliseconds between the current message sequence number consumed by the Kinesis indexing tasks and latest sequence number in Kinesis. Minimum emission period for this metric is a minute.|`dataSource`, `stream`, `partition`, `tags`|Greater than 0, up to max Kinesis retention period in milliseconds. |

### Other ingestion metrics

Streaming ingestion tasks and certain types of
batch ingestion emit the following metrics. These metrics are deltas for each emission period.

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`ingest/events/thrownAway`|Number of events rejected because they are either null, or filtered by the transform spec, or outside the windowPeriod.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|0|
|`ingest/events/unparseable`|Number of events rejected because the events are unparseable.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|0|
|`ingest/events/duplicate`|Number of events rejected because the events are duplicated.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|0|
|`ingest/events/processed`|Number of events successfully processed per emission period.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Equal to the number of events per emission period.|
|`ingest/rows/output`|Number of Druid rows persisted.|`dataSource`, `taskId`, `taskType`, `groupId`|Your number of events with rollup.|
|`ingest/persists/count`|Number of times persist occurred.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Depends on configuration.|
|`ingest/persists/time`|Milliseconds spent doing intermediate persist.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Depends on configuration. Generally a few minutes at most.|
|`ingest/persists/cpu`|Cpu time in Nanoseconds spent on doing intermediate persist.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Depends on configuration. Generally a few minutes at most.|
|`ingest/persists/backPressure`|Milliseconds spent creating persist tasks and blocking waiting for them to finish.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|0 or very low|
|`ingest/persists/failed`|Number of persists that failed.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|0|
|`ingest/handoff/failed`|Number of handoffs that failed.|`dataSource`, `taskId`, `taskType`, `groupId`,`tags`|0|
|`ingest/merge/time`|Milliseconds spent merging intermediate segments.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Depends on configuration. Generally a few minutes at most.|
|`ingest/merge/cpu`|Cpu time in Nanoseconds spent on merging intermediate segments.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Depends on configuration. Generally a few minutes at most.|
|`ingest/handoff/count`|Number of handoffs that happened.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Varies. Generally greater than 0 once every segment granular period if cluster operating normally.|
|`ingest/sink/count`|Number of sinks not handoffed.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|1~3|
|`ingest/events/messageGap`|Time gap in milliseconds between the latest ingested event timestamp and the current system timestamp of metrics emission. If the value is increasing but lag is low, Druid may not be receiving new data. This metric is reset as new tasks spawn up.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Greater than 0, depends on the time carried in event. |
|`ingest/notices/queueSize`|Number of pending notices to be processed by the coordinator.|`dataSource`, `tags`|Typically 0 and occasionally in lower single digits. Should not be a very high number. |
|`ingest/notices/time`|Milliseconds taken to process a notice by the supervisor.|`dataSource`, `tags`| < 1s |
|`ingest/pause/time`|Milliseconds spent by a task in a paused state without ingesting.|`dataSource`, `taskId`, `tags`| < 10 seconds|
|`ingest/handoff/time`|Total number of milliseconds taken to handoff a set of segments.|`dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Depends on coordinator cycle time.|

Note: If the JVM does not support CPU time measurement for the current thread, `ingest/merge/cpu` and `ingest/persists/cpu` will be 0.

## Indexing service

|Metric|Description| Dimensions                                                 |Normal Value|
|------|-----------|------------------------------------------------------------|------------|
|`task/run/time`|Milliseconds taken to run a task.| `dataSource`, `taskId`, `taskType`, `groupId`, `taskStatus`, `tags`|Varies|
|`task/pending/time`|Milliseconds taken for a task to wait for running.| `dataSource`, `taskId`, `taskType`, `groupId`, `tags`|Varies|
|`task/action/log/time`|Milliseconds taken to log a task action to the audit log.| `dataSource`, `taskId`, `taskType`, `groupId`, `taskActionType`, `tags`|< 1000 (subsecond)|
|`task/action/run/time`|Milliseconds taken to execute a task action.| `dataSource`, `taskId`, `taskType`, `groupId`, `taskActionType`, `tags`|Varies from subsecond to a few seconds, based on action type.|
|`task/action/success/count`|Number of task actions that were executed successfully during the emission period. Currently only being emitted for [batched `segmentAllocate` actions](../ingestion/tasks.md#batching-segmentallocate-actions).| `dataSource`, `taskId`, `taskType`, `groupId`, `taskActionType`, `tags`|Varies|
|`task/action/failed/count`|Number of task actions that failed during the emission period. Currently only being emitted for [batched `segmentAllocate` actions](../ingestion/tasks.md#batching-segmentallocate-actions).| `dataSource`, `taskId`, `taskType`, `groupId`, `taskActionType`, `tags`|Varies|
|`task/action/batch/queueTime`|Milliseconds spent by a batch of task actions in queue. Currently only being emitted for [batched `segmentAllocate` actions](../ingestion/tasks.md#batching-segmentallocate-actions).| `dataSource`, `taskActionType`, `interval`|Varies based on the `batchAllocationWaitTime` and number of batches in queue.|
|`task/action/batch/runTime`|Milliseconds taken to execute a batch of task actions. Currently only being emitted for [batched `segmentAllocate` actions](../ingestion/tasks.md#batching-segmentallocate-actions).| `dataSource`, `taskActionType`, `interval`|Varies from subsecond to a few seconds, based on action type and batch size.|
|`task/action/batch/size`|Number of task actions in a batch that was executed during the emission period. Currently only being emitted for [batched `segmentAllocate` actions](../ingestion/tasks.md#batching-segmentallocate-actions).| `dataSource`, `taskActionType`, `interval`|Varies based on number of concurrent task actions.|
|`task/action/batch/attempts`|Number of execution attempts for a single batch of task actions. Currently only being emitted for [batched `segmentAllocate` actions](../ingestion/tasks.md#batching-segmentallocate-actions).| `dataSource`, `taskActionType`, `interval`|1 if there are no failures or retries.|
|`task/segmentAvailability/wait/time`|The amount of milliseconds a batch indexing task waited for newly created segments to become available for querying.| `dataSource`, `taskType`, `groupId`, `taskId`, `segmentAvailabilityConfirmed`, `tags`|Varies|
|`segment/added/bytes`|Size in bytes of new segments created.| `dataSource`, `taskId`, `taskType`, `groupId`, `interval`, `tags`|Varies|
|`segment/moved/bytes`|Size in bytes of segments moved/archived via the Move Task.| `dataSource`, `taskId`, `taskType`, `groupId`, `interval`, `tags`|Varies|
|`segment/nuked/bytes`|Size in bytes of segments deleted via the Kill Task.| `dataSource`, `taskId`, `taskType`, `groupId`, `interval`, `tags`|Varies|
|`task/success/count`|Number of successful tasks per emission period. This metric is only available if the TaskCountStatsMonitor module is included.| `dataSource`|Varies|
|`task/failed/count`|Number of failed tasks per emission period. This metric is only available if the TaskCountStatsMonitor module is included.|`dataSource`|Varies|
|`task/running/count`|Number of current running tasks. This metric is only available if the `TaskCountStatsMonitor` module is included.|`dataSource`|Varies|
|`task/pending/count`|Number of current pending tasks. This metric is only available if the `TaskCountStatsMonitor` module is included.|`dataSource`|Varies|
|`task/waiting/count`|Number of current waiting tasks. This metric is only available if the `TaskCountStatsMonitor` module is included.|`dataSource`|Varies|
|`taskSlot/total/count`|Number of total task slots per emission period. This metric is only available if the `TaskSlotCountStatsMonitor` module is included.| `category`|Varies|
|`taskSlot/idle/count`|Number of idle task slots per emission period. This metric is only available if the `TaskSlotCountStatsMonitor` module is included.| `category`|Varies|
|`taskSlot/used/count`|Number of busy task slots per emission period. This metric is only available if the `TaskSlotCountStatsMonitor` module is included.| `category`|Varies|
|`taskSlot/lazy/count`|Number of total task slots in lazy marked MiddleManagers and Indexers per emission period. This metric is only available if the `TaskSlotCountStatsMonitor` module is included.| `category`|Varies|
|`taskSlot/blacklisted/count`|Number of total task slots in blacklisted MiddleManagers and Indexers per emission period. This metric is only available if the `TaskSlotCountStatsMonitor` module is included.| `category`|Varies|
|`worker/task/failed/count`|Number of failed tasks run on the reporting worker per emission period. This metric is only available if the `WorkerTaskCountStatsMonitor` module is included, and is only supported for middleManager nodes.| `category`, `workerVersion`|Varies|
|`worker/task/success/count`|Number of successful tasks run on the reporting worker per emission period. This metric is only available if the `WorkerTaskCountStatsMonitor` module is included, and is only supported for middleManager nodes.| `category`,`workerVersion`|Varies|
|`worker/taskSlot/idle/count`|Number of idle task slots on the reporting worker per emission period. This metric is only available if the `WorkerTaskCountStatsMonitor` module is included, and is only supported for middleManager nodes.| `category`, `workerVersion`|Varies|
|`worker/taskSlot/total/count`|Number of total task slots on the reporting worker per emission period. This metric is only available if the `WorkerTaskCountStatsMonitor` module is included.| `category`, `workerVersion`|Varies|
|`worker/taskSlot/used/count`|Number of busy task slots on the reporting worker per emission period. This metric is only available if the `WorkerTaskCountStatsMonitor` module is included.| `category`, `workerVersion`|Varies|

## Shuffle metrics (Native parallel task)

The shuffle metrics can be enabled by adding `org.apache.druid.indexing.worker.shuffle.ShuffleMonitor` in `druid.monitoring.monitors`
See [Enabling Metrics](../configuration/index.md#enabling-metrics) for more details.

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`ingest/shuffle/bytes`|Number of bytes shuffled per emission period.|`supervisorTaskId`|Varies|
|`ingest/shuffle/requests`|Number of shuffle requests per emission period.|`supervisorTaskId`|Varies|

## Coordination

These metrics are for the Druid Coordinator and are reset each time the Coordinator runs the coordination logic.

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`segment/assigned/count`|Number of segments assigned to be loaded in the cluster.|`tier`|Varies|
|`segment/moved/count`|Number of segments moved in the cluster.|`tier`|Varies|
|`segment/unmoved/count`|Number of segments which were chosen for balancing but were found to be already optimally placed.|`tier`|Varies|
|`segment/dropped/count`|Number of segments chosen to be dropped from the cluster due to being over-replicated.|`tier`|Varies|
|`segment/deleted/count`|Number of segments marked as unused due to drop rules.| |Varies|
|`segment/unneeded/count`|Number of segments dropped due to being marked as unused.|`tier`|Varies|
|`segment/cost/raw`|Used in cost balancing. The raw cost of hosting segments.|`tier`|Varies|
|`segment/cost/normalization`|Used in cost balancing. The normalization of hosting segments.|`tier`|Varies|
|`segment/cost/normalized`|Used in cost balancing. The normalized cost of hosting segments.|`tier`|Varies|
|`segment/loadQueue/size`|Size in bytes of segments to load.|`server`|Varies|
|`segment/loadQueue/failed`|Number of segments that failed to load.|`server`|0|
|`segment/loadQueue/count`|Number of segments to load.|`server`|Varies|
|`segment/dropQueue/count`|Number of segments to drop.|`server`|Varies|
|`segment/size`|Total size of used segments in a data source. Emitted only for data sources to which at least one used segment belongs.|`dataSource`|Varies|
|`segment/count`|Number of used segments belonging to a data source. Emitted only for data sources to which at least one used segment belongs.|`dataSource`|< max|
|`segment/overShadowed/count`|Number of segments marked as unused due to being overshadowed.| |Varies|
|`segment/unavailable/count`|Number of segments (not including replicas) left to load until segments that should be loaded in the cluster are available for queries.|`dataSource`|0|
|`segment/underReplicated/count`|Number of segments (including replicas) left to load until segments that should be loaded in the cluster are available for queries.|`tier`, `dataSource`|0|
|`tier/historical/count`|Number of available historical nodes in each tier.|`tier`|Varies|
|`tier/replication/factor`|Configured maximum replication factor in each tier.|`tier`|Varies|
|`tier/required/capacity`|Total capacity in bytes required in each tier.|`tier`|Varies|
|`tier/total/capacity`|Total capacity in bytes available in each tier.|`tier`|Varies|
|`compact/task/count`|Number of tasks issued in the auto compaction run.| |Varies|
|`compactTask/maxSlot/count`|Max number of task slots that can be used for auto compaction tasks in the auto compaction run.| |Varies|
|`compactTask/availableSlot/count`|Number of available task slots that can be used for auto compaction tasks in the auto compaction run. This is the max number of task slots minus any currently running compaction tasks.| |Varies|
|`segment/waitCompact/bytes`|Total bytes of this datasource waiting to be compacted by the auto compaction (only consider intervals/segments that are eligible for auto compaction).|`dataSource`|Varies|
|`segment/waitCompact/count`|Total number of segments of this datasource waiting to be compacted by the auto compaction (only consider intervals/segments that are eligible for auto compaction).|`dataSource`|Varies|
|`interval/waitCompact/count`|Total number of intervals of this datasource waiting to be compacted by the auto compaction (only consider intervals/segments that are eligible for auto compaction).|`dataSource`|Varies|
|`segment/compacted/bytes`|Total bytes of this datasource that are already compacted with the spec set in the auto compaction config.|`dataSource`|Varies|
|`segment/compacted/count`|Total number of segments of this datasource that are already compacted with the spec set in the auto compaction config.|`dataSource`|Varies|
|`interval/compacted/count`|Total number of intervals of this datasource that are already compacted with the spec set in the auto compaction config.|`dataSource`|Varies|
|`segment/skipCompact/bytes`|Total bytes of this datasource that are skipped (not eligible for auto compaction) by the auto compaction.|`dataSource`|Varies|
|`segment/skipCompact/count`|Total number of segments of this datasource that are skipped (not eligible for auto compaction) by the auto compaction.|`dataSource`|Varies|
|`interval/skipCompact/count`|Total number of intervals of this datasource that are skipped (not eligible for auto compaction) by the auto compaction.|`dataSource`|Varies|
|`coordinator/time`|Approximate Coordinator duty runtime in milliseconds. The duty dimension is the string alias of the Duty that is being run.|`duty`|Varies|
|`coordinator/global/time`|Approximate runtime of a full coordination cycle in milliseconds. The `dutyGroup` dimension indicates what type of coordination this run was. i.e. Historical Management vs Indexing|`dutyGroup`|Varies|
|`metadata/kill/supervisor/count`|Total number of terminated supervisors that were automatically deleted from metadata store per each Coordinator kill supervisor duty run. This metric can help adjust `druid.coordinator.kill.supervisor.durationToRetain` configuration based on whether more or less terminated supervisors need to be deleted per cycle. Note that this metric is only emitted when `druid.coordinator.kill.supervisor.on` is set to true.| |Varies|
|`metadata/kill/audit/count`|Total number of audit logs that were automatically deleted from metadata store per each Coordinator kill audit duty run. This metric can help adjust `druid.coordinator.kill.audit.durationToRetain` configuration based on whether more or less audit logs need to be deleted per cycle. Note that this metric is only emitted when `druid.coordinator.kill.audit.on` is set to true.| |Varies|
|`metadata/kill/compaction/count`|Total number of compaction configurations that were automatically deleted from metadata store per each Coordinator kill compaction configuration duty run. Note that this metric is only emitted when `druid.coordinator.kill.compaction.on` is set to true.| |Varies|
|`metadata/kill/rule/count`|Total number of rules that were automatically deleted from metadata store per each Coordinator kill rule duty run. This metric can help adjust `druid.coordinator.kill.rule.durationToRetain` configuration based on whether more or less rules need to be deleted per cycle. Note that this metric is only emitted when `druid.coordinator.kill.rule.on` is set to true.| |Varies|
|`metadata/kill/datasource/count`|Total number of datasource metadata that were automatically deleted from metadata store per each Coordinator kill datasource duty run (Note: datasource metadata only exists for datasource created from supervisor). This metric can help adjust `druid.coordinator.kill.datasource.durationToRetain` configuration based on whether more or less datasource metadata need to be deleted per cycle. Note that this metric is only emitted when `druid.coordinator.kill.datasource.on` is set to true.| |Varies|
|`init/serverview/time`|Time taken to initialize the coordinator server view.||Depends on the number of segments|

If `emitBalancingStats` is set to `true` in the Coordinator [dynamic configuration](../configuration/index.md#dynamic-configuration), then [log entries](../configuration/logging.md) for class `org.apache.druid.server.coordinator.duty.EmitClusterStatsAndMetrics` will have extra information on balancing decisions.

## General Health

### Historical

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`segment/max`|Maximum byte limit available for segments.| |Varies.|
|`segment/used`|Bytes used for served segments.|`dataSource`, `tier`, `priority`|< max|
|`segment/usedPercent`|Percentage of space used by served segments.|`dataSource`, `tier`, `priority`|< 100%|
|`segment/count`|Number of served segments.|`dataSource`, `tier`, `priority`|Varies|
|`segment/pendingDelete`|On-disk size in bytes of segments that are waiting to be cleared out.| |Varies|
|`segment/rowCount/avg`| The average number of rows per segment on a historical. `SegmentStatsMonitor` must be enabled.| `dataSource`, `tier`, `priority`|Varies. See [segment optimization](../operations/segment-optimization.md) for guidance on optimal segment sizes. |
|`segment/rowCount/range/count`| The number of segments in a bucket. `SegmentStatsMonitor` must be enabled.| `dataSource`, `tier`, `priority`, `range`|Varies|

### JVM

These metrics are only available if the `JVMMonitor` module is included.

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`jvm/pool/committed`|Committed pool|`poolKind`, `poolName`|Close to max pool|
|`jvm/pool/init`|Initial pool|`poolKind`, `poolName`|Varies|
|`jvm/pool/max`|Max pool|`poolKind`, `poolName`|Varies|
|`jvm/pool/used`|Pool used|`poolKind`, `poolName`|< max pool|
|`jvm/bufferpool/count`|Bufferpool count|`bufferpoolName`|Varies|
|`jvm/bufferpool/used`|Bufferpool used|`bufferpoolName`|Close to capacity|
|`jvm/bufferpool/capacity`|Bufferpool capacity|`bufferpoolName`|Varies|
|`jvm/mem/init`|Initial memory|`memKind`|Varies|
|`jvm/mem/max`|Max memory|`memKind`|Varies|
|`jvm/mem/used`|Used memory|`memKind`|< max memory|
|`jvm/mem/committed`|Committed memory|`memKind`|Close to max memory|
|`jvm/gc/count`|Garbage collection count|`gcName` (cms/g1/parallel/etc.), `gcGen` (old/young)|Varies|
|`jvm/gc/cpu`|Count of CPU time in Nanoseconds spent on garbage collection. Note: `jvm/gc/cpu` represents the total time over multiple GC cycles; divide by `jvm/gc/count` to get the mean GC time per cycle.|`gcName`, `gcGen`|Sum of `jvm/gc/cpu` should be within 10-30% of sum of `jvm/cpu/total`, depending on the GC algorithm used (reported by [`JvmCpuMonitor`](../configuration/index.md#enabling-metrics)). |

### EventReceiverFirehose

The following metric is only available if the `EventReceiverFirehoseMonitor` module is included.

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`ingest/events/buffered`|Number of events queued in the `EventReceiverFirehose` buffer.|`serviceName`, `dataSource`, `taskId`, `taskType`, `bufferCapacity`|Equal to current number of events in the buffer queue.|
|`ingest/bytes/received`|Number of bytes received by the `EventReceiverFirehose`.|`serviceName`, `dataSource`, `taskId`, `taskType`|Varies|

## Sys

These metrics are only available if the `SysMonitor` module is included.

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`sys/swap/free`|Free swap||Varies|
|`sys/swap/max`|Max swap||Varies|
|`sys/swap/pageIn`|Paged in swap||Varies|
|`sys/swap/pageOut`|Paged out swap||Varies|
|`sys/disk/write/count`|Writes to disk|`fsDevName`, `fsDirName`, `fsTypeName`, `fsSysTypeName`, `fsOptions`|Varies|
|`sys/disk/read/count`|Reads from disk|`fsDevName`, `fsDirName`, `fsTypeName`, `fsSysTypeName`, `fsOptions`|Varies|
|`sys/disk/write/size`|Bytes written to disk. One indicator of the amount of paging occurring for segments.|`fsDevName`,`fsDirName`,`fsTypeName`, `fsSysTypeName`, `fsOptions`|Varies|
|`sys/disk/read/size`|Bytes read from disk. One indicator of the amount of paging occurring for segments.|`fsDevName`,`fsDirName`, `fsTypeName`, `fsSysTypeName`, `fsOptions`|Varies|
|`sys/net/write/size`|Bytes written to the network|`netName`, `netAddress`, `netHwaddr`|Varies|
|`sys/net/read/size`|Bytes read from the network|`netName`, `netAddress`, `netHwaddr`|Varies|
|`sys/fs/used`|Filesystem bytes used|`fsDevName`, `fsDirName`, `fsTypeName`, `fsSysTypeName`, `fsOptions`|< max|
|`sys/fs/max`|Filesystem bytes max|`fsDevName`, `fsDirName`, `fsTypeName`, `fsSysTypeName`, `fsOptions`|Varies|
|`sys/mem/used`|Memory used||< max|
|`sys/mem/max`|Memory max||Varies|
|`sys/storage/used`|Disk space used|`fsDirName`|Varies|
|`sys/cpu`|CPU used|`cpuName`, `cpuTime`|Varies|

## Cgroup

These metrics are available on operating systems with the cgroup kernel feature. All the values are derived by reading from `/sys/fs/cgroup`.

|Metric|Description|Dimensions|Normal Value|
|------|-----------|----------|------------|
|`cgroup/cpu/shares`|Relative value of CPU time available to this process. Read from `cpu.shares`.||Varies|
|`cgroup/cpu/cores_quota`|Number of cores available to this process. Derived from `cpu.cfs_quota_us`/`cpu.cfs_period_us`.||Varies. A value of -1 indicates there is no explicit quota set.|
|`cgroup/memory/*`|Memory stats for this process (e.g. `cache`, `total_swap`, etc.). Each stat produces a separate metric. Read from `memory.stat`.||Varies|
|`cgroup/memory_numa/*/pages`|Memory stats, per NUMA node, for this process (e.g. `total`, `unevictable`, etc.). Each stat produces a separate metric. Read from `memory.num_stat`.|`numaZone`|Varies|
|`cgroup/cpuset/cpu_count`|Total number of CPUs available to the process. Derived from `cpuset.cpus`.||Varies|
|`cgroup/cpuset/effective_cpu_count`|Total number of active CPUs available to the process. Derived from `cpuset.effective_cpus`.||Varies|
|`cgroup/cpuset/mems_count`|Total number of memory nodes available to the process. Derived from `cpuset.mems`.||Varies|
|`cgroup/cpuset/effective_mems_count`|Total number of active memory nodes available to the process. Derived from `cpuset.effective_mems`.||Varies|
