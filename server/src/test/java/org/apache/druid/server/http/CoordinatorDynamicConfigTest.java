/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.server.http;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.apache.druid.segment.TestHelper;
import org.apache.druid.server.coordinator.CoordinatorDynamicConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 *
 */
public class CoordinatorDynamicConfigTest
{
  private static final int EXPECTED_DEFAULT_MAX_SEGMENTS_IN_NODE_LOADING_QUEUE = 500;

  private final ObjectMapper mapper = TestHelper.makeJsonMapper();

  @Test
  public void testSerde() throws Exception
  {
    String jsonStr = "{\n"
                     + "  \"millisToWaitBeforeDeleting\": 1,\n"
                     + "  \"mergeBytesLimit\": 1,\n"
                     + "  \"mergeSegmentsLimit\" : 1,\n"
                     + "  \"maxSegmentsToMove\": 1,\n"
                     + "  \"percentOfSegmentsToConsiderPerMove\": 1,\n"
                     + "  \"replicantLifetime\": 1,\n"
                     + "  \"replicationThrottleLimit\": 1,\n"
                     + "  \"balancerComputeThreads\": 2, \n"
                     + "  \"emitBalancingStats\": true,\n"
                     + "  \"killDataSourceWhitelist\": [\"test1\",\"test2\"],\n"
                     + "  \"maxSegmentsInNodeLoadingQueue\": 1,\n"
                     + "  \"decommissioningNodes\": [\"host1\", \"host2\"],\n"
                     + "  \"decommissioningMaxPercentOfMaxSegmentsToMove\": 9,\n"
                     + "  \"pauseCoordination\": false,\n"
                     + "  \"replicateAfterLoadTimeout\": false,\n"
                     + "  \"maxNonPrimaryReplicantsToLoad\": 2147483647\n"
                     + "}\n";

    CoordinatorDynamicConfig actual = mapper.readValue(
        mapper.writeValueAsString(
            mapper.readValue(
                jsonStr,
                CoordinatorDynamicConfig.class
            )
        ),
        CoordinatorDynamicConfig.class
    );
    ImmutableSet<String> decommissioning = ImmutableSet.of("host1", "host2");
    ImmutableSet<String> whitelist = ImmutableSet.of("test1", "test2");
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        1,
        true,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        decommissioning,
        9,
        false,
        false,
        Integer.MAX_VALUE
    );

    actual = CoordinatorDynamicConfig.builder().withDecommissioningNodes(ImmutableSet.of("host1")).build(actual);
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        1,
        true,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        ImmutableSet.of("host1"),
        9,
        false,
        false,
        Integer.MAX_VALUE
    );

    actual = CoordinatorDynamicConfig.builder().withDecommissioningMaxPercentOfMaxSegmentsToMove(5).build(actual);
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        1,
        true,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        ImmutableSet.of("host1"),
        5,
        false,
        false,
        Integer.MAX_VALUE
    );

    actual = CoordinatorDynamicConfig.builder().withPauseCoordination(true).build(actual);
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        1,
        true,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        ImmutableSet.of("host1"),
        5,
        true,
        false,
        Integer.MAX_VALUE
    );

    actual = CoordinatorDynamicConfig.builder()
                                     .withPercentOfSegmentsToConsiderPerMove(10)
                                     .withUseBatchedSegmentSampler(false)
                                     .build(actual);
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        10,
        false,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        ImmutableSet.of("host1"),
        5,
        true,
        false,
        Integer.MAX_VALUE
    );

    actual = CoordinatorDynamicConfig.builder().withReplicateAfterLoadTimeout(true).build(actual);
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        10,
        false,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        ImmutableSet.of("host1"),
        5,
        true,
        true,
        Integer.MAX_VALUE
    );

    actual = CoordinatorDynamicConfig.builder().withMaxNonPrimaryReplicantsToLoad(10).build(actual);
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        10,
        false,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        ImmutableSet.of("host1"),
        5,
        true,
        true,
        10
    );
  }

  @Test
  public void testConstructorWithNullsShouldKillUnusedSegmentsInAllDataSources()
  {
    CoordinatorDynamicConfig config = new CoordinatorDynamicConfig(
        1,
        1,
        1,
        1,
        null,
        false,
        1,
        2,
        10,
        true,
        null,
        null,
        null,
        ImmutableSet.of("host1"),
        5,
        true,
        true,
        10,
        false
    );
    Assert.assertTrue(config.isKillUnusedSegmentsInAllDataSources());
    Assert.assertTrue(config.getSpecificDataSourcesToKillUnusedSegmentsIn().isEmpty());
  }

  @Test
  public void testConstructorWithSpecificDataSourcesToKillShouldNotKillUnusedSegmentsInAllDatasources()
  {
    CoordinatorDynamicConfig config = new CoordinatorDynamicConfig(
        1,
        1,
        1,
        1,
        null,
        false,
        1,
        2,
        10,
        true,
        ImmutableSet.of("test1"),
        null,
        null,
        ImmutableSet.of("host1"),
        5,
        true,
        true,
        10,
        false
    );
    Assert.assertFalse(config.isKillUnusedSegmentsInAllDataSources());
    Assert.assertEquals(ImmutableSet.of("test1"), config.getSpecificDataSourcesToKillUnusedSegmentsIn());
  }

  @Test
  public void testDecommissioningParametersBackwardCompatibility() throws Exception
  {
    String jsonStr = "{\n"
                     + "  \"millisToWaitBeforeDeleting\": 1,\n"
                     + "  \"mergeBytesLimit\": 1,\n"
                     + "  \"mergeSegmentsLimit\" : 1,\n"
                     + "  \"maxSegmentsToMove\": 1,\n"
                     + "  \"replicantLifetime\": 1,\n"
                     + "  \"replicationThrottleLimit\": 1,\n"
                     + "  \"balancerComputeThreads\": 2, \n"
                     + "  \"emitBalancingStats\": true,\n"
                     + "  \"killDataSourceWhitelist\": [\"test1\",\"test2\"],\n"
                     + "  \"maxSegmentsInNodeLoadingQueue\": 1\n"
                     + "}\n";

    CoordinatorDynamicConfig actual = mapper.readValue(
        mapper.writeValueAsString(
            mapper.readValue(
                jsonStr,
                CoordinatorDynamicConfig.class
            )
        ),
        CoordinatorDynamicConfig.class
    );
    ImmutableSet<String> decommissioning = ImmutableSet.of();
    ImmutableSet<String> whitelist = ImmutableSet.of("test1", "test2");
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        100,
        true, 1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        decommissioning,
        0,
        false,
        false,
        Integer.MAX_VALUE
    );

    actual = CoordinatorDynamicConfig.builder().withDecommissioningNodes(ImmutableSet.of("host1")).build(actual);
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        100,
        true,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        ImmutableSet.of("host1"),
        0,
        false,
        false,
        Integer.MAX_VALUE
    );

    actual = CoordinatorDynamicConfig.builder().withDecommissioningMaxPercentOfMaxSegmentsToMove(5).build(actual);
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        100,
        true,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        ImmutableSet.of("host1"),
        5,
        false,
        false,
        Integer.MAX_VALUE
    );
  }

  @Test
  public void testSerdeWithStringinKillDataSourceWhitelist() throws Exception
  {
    String jsonStr = "{\n"
                     + "  \"millisToWaitBeforeDeleting\": 1,\n"
                     + "  \"mergeBytesLimit\": 1,\n"
                     + "  \"mergeSegmentsLimit\" : 1,\n"
                     + "  \"maxSegmentsToMove\": 1,\n"
                     + "  \"percentOfSegmentsToConsiderPerMove\": 1,\n"
                     + "  \"replicantLifetime\": 1,\n"
                     + "  \"replicationThrottleLimit\": 1,\n"
                     + "  \"balancerComputeThreads\": 2, \n"
                     + "  \"emitBalancingStats\": true,\n"
                     + "  \"killDataSourceWhitelist\": \"test1, test2\", \n"
                     + "  \"maxSegmentsInNodeLoadingQueue\": 1\n"
                     + "}\n";

    CoordinatorDynamicConfig actual = mapper.readValue(
        mapper.writeValueAsString(
            mapper.readValue(
                jsonStr,
                CoordinatorDynamicConfig.class
            )
        ),
        CoordinatorDynamicConfig.class
    );
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        1,
        true,
        1,
        1,
        2,
        true,
        ImmutableSet.of("test1", "test2"),
        false,
        1,
        ImmutableSet.of(),
        0,
        false,
        false,
        Integer.MAX_VALUE
    );
  }

  @Test
  public void testSerdeHandleInvalidPercentOfSegmentsToConsiderPerMove() throws Exception
  {
    try {
      String jsonStr = "{\n"
                       + "  \"percentOfSegmentsToConsiderPerMove\": 0\n"
                       + "}\n";

      mapper.readValue(
          mapper.writeValueAsString(
              mapper.readValue(
                  jsonStr,
                  CoordinatorDynamicConfig.class
              )
          ),
          CoordinatorDynamicConfig.class
      );

      Assert.fail("deserialization should fail.");
    }
    catch (JsonMappingException e) {
      Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
    }

    try {
      String jsonStr = "{\n"
                       + "  \"percentOfSegmentsToConsiderPerMove\": -100\n"
                       + "}\n";

      mapper.readValue(
          mapper.writeValueAsString(
              mapper.readValue(
                  jsonStr,
                  CoordinatorDynamicConfig.class
              )
          ),
          CoordinatorDynamicConfig.class
      );

      Assert.fail("deserialization should fail.");
    }
    catch (JsonMappingException e) {
      Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
    }

    try {
      String jsonStr = "{\n"
                       + "  \"percentOfSegmentsToConsiderPerMove\": 105\n"
                       + "}\n";

      mapper.readValue(
          mapper.writeValueAsString(
              mapper.readValue(
                  jsonStr,
                  CoordinatorDynamicConfig.class
              )
          ),
          CoordinatorDynamicConfig.class
      );

      Assert.fail("deserialization should fail.");
    }
    catch (JsonMappingException e) {
      Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testHandleMissingPercentOfSegmentsToConsiderPerMove() throws Exception
  {
    String jsonStr = "{\n"
                     + "  \"millisToWaitBeforeDeleting\": 1,\n"
                     + "  \"mergeBytesLimit\": 1,\n"
                     + "  \"mergeSegmentsLimit\" : 1,\n"
                     + "  \"maxSegmentsToMove\": 1,\n"
                     + "  \"replicantLifetime\": 1,\n"
                     + "  \"replicationThrottleLimit\": 1,\n"
                     + "  \"balancerComputeThreads\": 2, \n"
                     + "  \"emitBalancingStats\": true,\n"
                     + "  \"killDataSourceWhitelist\": [\"test1\",\"test2\"],\n"
                     + "  \"maxSegmentsInNodeLoadingQueue\": 1,\n"
                     + "  \"decommissioningNodes\": [\"host1\", \"host2\"],\n"
                     + "  \"decommissioningMaxPercentOfMaxSegmentsToMove\": 9,\n"
                     + "  \"pauseCoordination\": false\n"
                     + "}\n";
    CoordinatorDynamicConfig actual = mapper.readValue(
        mapper.writeValueAsString(
            mapper.readValue(
                jsonStr,
                CoordinatorDynamicConfig.class
            )
        ),
        CoordinatorDynamicConfig.class
    );
    ImmutableSet<String> decommissioning = ImmutableSet.of("host1", "host2");
    ImmutableSet<String> whitelist = ImmutableSet.of("test1", "test2");
    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        100,
        true,
        1,
        1,
        2,
        true,
        whitelist,
        false,
        1,
        decommissioning,
        9,
        false,
        false,
        Integer.MAX_VALUE
    );
  }

  @Test
  public void testSerdeWithKillAllDataSources() throws Exception
  {
    String jsonStr = "{\n"
                     + "  \"millisToWaitBeforeDeleting\": 1,\n"
                     + "  \"mergeBytesLimit\": 1,\n"
                     + "  \"mergeSegmentsLimit\" : 1,\n"
                     + "  \"maxSegmentsToMove\": 1,\n"
                     + "  \"percentOfSegmentsToConsiderPerMove\": 1,\n"
                     + "  \"replicantLifetime\": 1,\n"
                     + "  \"replicationThrottleLimit\": 1,\n"
                     + "  \"balancerComputeThreads\": 2, \n"
                     + "  \"emitBalancingStats\": true,\n"
                     + "  \"killAllDataSources\": true,\n"
                     + "  \"maxSegmentsInNodeLoadingQueue\": 1\n"
                     + "}\n";

    CoordinatorDynamicConfig actual = mapper.readValue(
        mapper.writeValueAsString(
            mapper.readValue(
                jsonStr,
                CoordinatorDynamicConfig.class
            )
        ),
        CoordinatorDynamicConfig.class
    );

    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        1,
        true,
        1,
        1,
        2,
        true,
        ImmutableSet.of(),
        true,
        1,
        ImmutableSet.of(),
        0,
        false,
        false,
        Integer.MAX_VALUE
    );

    // killAllDataSources is a config in versions 0.22.x and older and is no longer used.
    // This used to be an invalid config, but as of 0.23.0 the killAllDataSources flag no longer exsist,
    // so this is a valid config
    jsonStr = "{\n"
              + "  \"killDataSourceWhitelist\": [\"test1\",\"test2\"],\n"
              + "  \"killAllDataSources\": true,\n"
              + "  \"percentOfSegmentsToConsiderPerMove\": 1\n"
              + "}\n";
    actual = mapper.readValue(
        jsonStr,
        CoordinatorDynamicConfig.class
    );

    Assert.assertFalse(actual.isKillUnusedSegmentsInAllDataSources());
    Assert.assertEquals(2, actual.getSpecificDataSourcesToKillUnusedSegmentsIn().size());
  }

  @Test
  public void testDeserializeWithoutMaxSegmentsInNodeLoadingQueue() throws Exception
  {
    String jsonStr = "{\n"
                     + "  \"millisToWaitBeforeDeleting\": 1,\n"
                     + "  \"mergeBytesLimit\": 1,\n"
                     + "  \"mergeSegmentsLimit\" : 1,\n"
                     + "  \"maxSegmentsToMove\": 1,\n"
                     + "  \"percentOfSegmentsToConsiderPerMove\": 1,\n"
                     + "  \"replicantLifetime\": 1,\n"
                     + "  \"replicationThrottleLimit\": 1,\n"
                     + "  \"balancerComputeThreads\": 2, \n"
                     + "  \"emitBalancingStats\": true,\n"
                     + "  \"killAllDataSources\": true\n"
                     + "}\n";

    CoordinatorDynamicConfig actual = mapper.readValue(
        mapper.writeValueAsString(
            mapper.readValue(
                jsonStr,
                CoordinatorDynamicConfig.class
            )
        ),
        CoordinatorDynamicConfig.class
    );

    assertConfig(
        actual,
        1,
        1,
        1,
        1,
        1,
        true,
        1,
        1,
        2,
        true,
        ImmutableSet.of(),
        true,
        EXPECTED_DEFAULT_MAX_SEGMENTS_IN_NODE_LOADING_QUEUE,
        ImmutableSet.of(),
        0,
        false,
        false,
        Integer.MAX_VALUE
    );
  }

  @Test
  public void testBuilderDefaults()
  {
    CoordinatorDynamicConfig defaultConfig = CoordinatorDynamicConfig.builder().build();
    ImmutableSet<String> emptyList = ImmutableSet.of();
    assertConfig(
        defaultConfig,
        900000,
        524288000,
        100,
        100,
        100,
        true,
        15,
        500,
        1,
        false,
        emptyList,
        true,
        EXPECTED_DEFAULT_MAX_SEGMENTS_IN_NODE_LOADING_QUEUE,
        emptyList,
        70,
        false,
        false,
        Integer.MAX_VALUE
    );
  }

  @Test
  public void testBuilderWithDefaultSpecificDataSourcesToKillUnusedSegmentsInSpecified()
  {
    CoordinatorDynamicConfig defaultConfig =
        CoordinatorDynamicConfig.builder()
                                .withSpecificDataSourcesToKillUnusedSegmentsIn(ImmutableSet.of("DATASOURCE"))
                                .build();
    CoordinatorDynamicConfig config = CoordinatorDynamicConfig.builder().build(defaultConfig);
    assertConfig(
        config,
        900000,
        524288000,
        100,
        100,
        100,
        true,
        15,
        500,
        1,
        false,
        ImmutableSet.of("DATASOURCE"),
        false,
        EXPECTED_DEFAULT_MAX_SEGMENTS_IN_NODE_LOADING_QUEUE,
        ImmutableSet.of(),
        70,
        false,
        false,
        Integer.MAX_VALUE
    );
  }

  @Test
  public void testUpdate()
  {
    CoordinatorDynamicConfig current = CoordinatorDynamicConfig
        .builder()
        .withSpecificDataSourcesToKillUnusedSegmentsIn(ImmutableSet.of("x"))
        .build();

    Assert.assertEquals(
        current,
        new CoordinatorDynamicConfig.Builder(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        ).build(current)
    );
  }

  @Test
  public void testSerdeHandleInvalidMaxNonPrimaryReplicantsToLoad() throws Exception
  {
    try {
      String jsonStr = "{\n"
                       + "  \"maxNonPrimaryReplicantsToLoad\": -1\n"
                       + "}\n";

      mapper.readValue(
          mapper.writeValueAsString(
              mapper.readValue(
                  jsonStr,
                  CoordinatorDynamicConfig.class
              )
          ),
          CoordinatorDynamicConfig.class
      );

      Assert.fail("deserialization should fail.");
    }
    catch (JsonMappingException e) {
      Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testEqualsAndHashCodeSanity()
  {
    CoordinatorDynamicConfig config1 = CoordinatorDynamicConfig.builder().build();
    CoordinatorDynamicConfig config2 = CoordinatorDynamicConfig.builder().build();
    Assert.assertEquals(config1, config2);
    Assert.assertEquals(config1.hashCode(), config2.hashCode());
  }

  private void assertConfig(
      CoordinatorDynamicConfig config,
      long expectedLeadingTimeMillisBeforeCanMarkAsUnusedOvershadowedSegments,
      long expectedMergeBytesLimit,
      int expectedMergeSegmentsLimit,
      int expectedMaxSegmentsToMove,
      int expectedPercentOfSegmentsToConsiderPerMove,
      boolean expectedUseBatchedSegmentSampler,
      int expectedReplicantLifetime,
      int expectedReplicationThrottleLimit,
      int expectedBalancerComputeThreads,
      boolean expectedEmitingBalancingStats,
      Set<String> expectedSpecificDataSourcesToKillUnusedSegmentsIn,
      boolean expectedKillUnusedSegmentsInAllDataSources,
      int expectedMaxSegmentsInNodeLoadingQueue,
      Set<String> decommissioningNodes,
      int decommissioningMaxPercentOfMaxSegmentsToMove,
      boolean pauseCoordination,
      boolean replicateAfterLoadTimeout,
      int maxNonPrimaryReplicantsToLoad
  )
  {
    Assert.assertEquals(
        expectedLeadingTimeMillisBeforeCanMarkAsUnusedOvershadowedSegments,
        config.getLeadingTimeMillisBeforeCanMarkAsUnusedOvershadowedSegments()
    );
    Assert.assertEquals(expectedMergeBytesLimit, config.getMergeBytesLimit());
    Assert.assertEquals(expectedMergeSegmentsLimit, config.getMergeSegmentsLimit());
    Assert.assertEquals(expectedMaxSegmentsToMove, config.getMaxSegmentsToMove());
    Assert.assertEquals(expectedPercentOfSegmentsToConsiderPerMove, config.getPercentOfSegmentsToConsiderPerMove(), 0);
    Assert.assertEquals(expectedUseBatchedSegmentSampler, config.useBatchedSegmentSampler());
    Assert.assertEquals(expectedReplicantLifetime, config.getReplicantLifetime());
    Assert.assertEquals(expectedReplicationThrottleLimit, config.getReplicationThrottleLimit());
    Assert.assertEquals(expectedBalancerComputeThreads, config.getBalancerComputeThreads());
    Assert.assertEquals(expectedEmitingBalancingStats, config.emitBalancingStats());
    Assert.assertEquals(
        expectedSpecificDataSourcesToKillUnusedSegmentsIn,
        config.getSpecificDataSourcesToKillUnusedSegmentsIn()
    );
    Assert.assertEquals(expectedKillUnusedSegmentsInAllDataSources, config.isKillUnusedSegmentsInAllDataSources());
    Assert.assertEquals(expectedMaxSegmentsInNodeLoadingQueue, config.getMaxSegmentsInNodeLoadingQueue());
    Assert.assertEquals(decommissioningNodes, config.getDecommissioningNodes());
    Assert.assertEquals(
        decommissioningMaxPercentOfMaxSegmentsToMove,
        config.getDecommissioningMaxPercentOfMaxSegmentsToMove()
    );
    Assert.assertEquals(pauseCoordination, config.getPauseCoordination());
    Assert.assertEquals(replicateAfterLoadTimeout, config.getReplicateAfterLoadTimeout());
    Assert.assertEquals(maxNonPrimaryReplicantsToLoad, config.getMaxNonPrimaryReplicantsToLoad());
  }
}
