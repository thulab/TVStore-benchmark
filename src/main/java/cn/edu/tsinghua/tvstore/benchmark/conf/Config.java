/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.edu.tsinghua.tvstore.benchmark.conf;

public class Config {

    private String storeType = "tvstore";

    private String directory = "/path/to/data";

    private long numValuesPerThread = 31_250_000_000L;

    private int numThreads = 10;

    private String distribution = "evenly-spaced";

    private String reddLowDirectory = "dataset/redd/low_freq";

    private int totalStreamNum = 10;

    private int queryTimes = 100;

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public long getNumValuesPerThread() {
        return numValuesPerThread;
    }

    public void setNumValuesPerThread(long numValuesPerThread) {
        this.numValuesPerThread = numValuesPerThread;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public String getReddLowDirectory() {
        return reddLowDirectory;
    }

    public void setReddLowDirectory(String reddLowDirectory) {
        this.reddLowDirectory = reddLowDirectory;
    }

    public int getTotalStreamNum() {
        return totalStreamNum;
    }

    public void setTotalStreamNum(int totalStreamNum) {
        this.totalStreamNum = totalStreamNum;
    }

    public int getQueryTimes() {
        return queryTimes;
    }

    public void setQueryTimes(int queryTimes) {
        this.queryTimes = queryTimes;
    }
}
