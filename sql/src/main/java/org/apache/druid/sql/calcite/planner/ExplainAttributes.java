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

package org.apache.druid.sql.calcite.planner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.druid.java.util.common.granularity.Granularity;

import javax.annotation.Nullable;

/**
 * ExplainAttributes holds the attributes of a SQL statement that is used in the EXPLAIN PLAN result.
 */
public final class ExplainAttributes
{
  private final String statementType;

  @Nullable
  private final SqlNode targetDataSource;

  @Nullable
  private final Granularity partitionedBy;

  @Nullable
  private final SqlNodeList clusteredBy;

  @Nullable
  private final SqlNode replaceTimeChunks;

  public ExplainAttributes(
      @JsonProperty("statementType") final String statementType,
      @JsonProperty("targetDataSource") @Nullable final SqlNode targetDataSource,
      @JsonProperty("partitionedBy") @Nullable final Granularity partitionedBy,
      @JsonProperty("clusteredBy") @Nullable final SqlNodeList clusteredBy,
      @JsonProperty("replaceTimeChunks") @Nullable final SqlNode replaceTimeChunks
  )
  {
    this.statementType = statementType;
    this.targetDataSource = targetDataSource;
    this.partitionedBy = partitionedBy;
    this.clusteredBy = clusteredBy;
    this.replaceTimeChunks = replaceTimeChunks;
  }

  /**
   * @return the statement kind of a SQL statement. For example, SELECT, INSERT, or REPLACE.
   */
  @JsonProperty
  public String getStatementType()
  {
    return statementType;
  }

  /**
   * @return the target datasource in a SQL statement. Returns null
   * for SELECT statements where there is no target datasource.
   */
  @Nullable
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getTargetDataSource()
  {
    return targetDataSource == null ? null : targetDataSource.toString();
  }

  /**
   * @return the time-based partitioning granularity specified in the <code>PARTITIONED BY</code> clause
   * for an INSERT or REPLACE statement. Returns null for SELECT statements.
   */
  @Nullable
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public Granularity getPartitionedBy()
  {
    return partitionedBy;
  }

  /**
   * @return the clustering columns specified in the <code>CLUSTERED BY</code> clause
   * for an INSERT or REPLACE statement. Returns null for SELECT statements.
   */
  @Nullable
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getClusteredBy()
  {
    return clusteredBy == null ? null : clusteredBy.toString();
  }

  /**
   * @return the time chunks specified in the <code>OVERWRITE</code> clause
   * for a REPLACE statement. Returns null for INSERT and SELECT statements.
   */
  @Nullable
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getReplaceTimeChunks()
  {
    return replaceTimeChunks == null ? null : replaceTimeChunks.toString();
  }

  @Override
  public String toString()
  {
    return "ExplainAttributes{" +
           "statementType='" + statementType + '\'' +
           ", targetDataSource=" + targetDataSource +
           ", partitionedBy=" + partitionedBy +
           ", clusteredBy=" + clusteredBy +
           ", replaceTimeChunks=" + replaceTimeChunks +
           '}';
  }
}
