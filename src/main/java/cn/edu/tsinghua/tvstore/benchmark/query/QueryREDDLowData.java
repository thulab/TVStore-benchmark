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
package cn.edu.tsinghua.tvstore.benchmark.query;

import cn.edu.tsinghua.tvstore.benchmark.conf.ConfigDescriptor;
import cn.edu.tsinghua.tvstore.benchmark.store.Store;
import cn.edu.tsinghua.tvstore.benchmark.store.SummaryStore;
import cn.edu.tsinghua.tvstore.benchmark.store.TVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class QueryREDDLowData {

    private static final Logger logger = LoggerFactory.getLogger(QueryREDDLowData.class);
    private static final Random random = new Random(11132313);

    private static final long N[] = {158_010_967_022L, 303_951_108_756L, 194_832_201_194L,
            208_575_498_345L, 948_051_387_620L};

    public static void main(String[] args) {
        String storeType = ConfigDescriptor.getInstance().getConfig().getStoreType();
        String directory = ConfigDescriptor.getInstance().getConfig().getDirectory();
        int totalStreamNum = ConfigDescriptor.getInstance().getConfig().getTotalStreamNum();
        int queryTimes = ConfigDescriptor.getInstance().getConfig().getQueryTimes();

        Store store;
        if (storeType.equals("tvstore")) {
            store = new TVStore(directory);
        } else if (storeType.equals("summarystore")) {
            store = new SummaryStore(directory);
        } else {
            logger.error("invalid store {}", storeType);
            return;
        }
        long st = System.currentTimeMillis();

        query(store, 3, totalStreamNum, queryTimes);
        query(store, 2, totalStreamNum, queryTimes);
        query(store, 0, totalStreamNum, queryTimes);
        query(store, 1, totalStreamNum, queryTimes);

        logger.info("-QUERY-ALL TASK FINISH in {} min", (System.currentTimeMillis() - st) / 1_000d / 60d);
    }

    public static void query(Store store, int aggreFun, int totalStreamNum, int totalTimes) {
        long st = System.currentTimeMillis();

        // queryId, offset, queryLen
        long[][][] latency = new long [totalTimes][4][4];
        double[][][] result = new double [totalTimes][4][4];

        for(int i = 0; i< totalTimes; i++){
            for(TimeUnit offset: TimeUnit.values()){
                for(TimeUnit queryLen : TimeUnit.values()){
                    long streamID = random.nextInt(totalStreamNum);
                    long endTime = N[(int)streamID] - queryOffsetLen(offset, (int) streamID) * offset.timeInSec;
                    int range = random.nextInt((int) (offset.timeInSec * 0.05));
                    endTime += random.nextDouble() > 0.5 ? range: -range;
                    long startTime = endTime - queryLen.timeInSec;
                    logger.info("stream = {}, aggreFun = {}, startTime = {}, endTime = {}, len = {}", streamID, aggreFun, startTime, endTime, queryLen.timeInSec);

                    long t0 = System.currentTimeMillis();
                    double re = store.query(streamID, startTime, endTime, aggreFun);
                    result[i][offset.ordinal()][queryLen.ordinal()] = re;
                    long t1 = System.currentTimeMillis();
                    latency[i][offset.ordinal()][queryLen.ordinal()] = t1 - t0;
                }
            }
        }
        logger.info("-QUERY-Execute {} {} queries in {} s.", totalTimes * 16, aggreFun, (System.currentTimeMillis() - st) / 1_000d);
        printLatency(latency);
        printQueryResult(result);
    }

    private static int queryOffsetLen(TimeUnit offset, int streamId){
        if(offset.equals(TimeUnit.YEAR)){
            return random.nextInt(10);
        } else if(offset.equals(TimeUnit.TENYEARS)){
            return random.nextInt(10);
        } else if(offset.equals(TimeUnit.HUNDREDYEARS)){
            return random.nextInt(10);
        } else {
            long maxTime = N[streamId];
            int range = (int)(maxTime / offset.timeInSec - 1);
            return random.nextInt(range);
        }
    }

    private static void printLatency(long[][][] latency){
        int totalTimes = latency.length;
        logger.info("-QUERY-****Latency in ms(row:offset, col:queryLen)***");
        for(int i = 0; i < totalTimes; i++){
            logger.info("-QUERY-Time {}:",i);
            for(TimeUnit offset: TimeUnit.values()){
                logger.info("-QUERY-,{},{},{},{},",
                        latency[i][offset.ordinal()][0],
                        latency[i][offset.ordinal()][1],
                        latency[i][offset.ordinal()][2],
                        latency[i][offset.ordinal()][3]
                );
            }
        }
    }

    private static void printQueryResult(double[][][] result){
        int totalTimes = result.length;
        logger.info("-QUERY-****Query Result(row:offset, col:queryLen)***");
        for(int i = 0; i < totalTimes; i++){
            logger.info("-QUERY-Time {}:", i);
            for(TimeUnit offset: TimeUnit.values()){
                logger.info("-QUERY-,{},{},{},{},",
                        result[i][offset.ordinal()][0],
                        result[i][offset.ordinal()][1],
                        result[i][offset.ordinal()][2],
                        result[i][offset.ordinal()][3]
                );
            }
        }
    }

    enum TimeUnit{
        YEAR(31_536_000L, "YEAR"),
        TENYEARS(315_360_000L, "TENYEARS"),
        HUNDREDYEARS(3_153_600_000L, "HUNDREDYEARS"),
        THOUSANDYEARS(31_536_000_000L, "THOUSANDYEARS");

        final long timeInSec;
        final String name;

        TimeUnit(long timeInSec, String name) {
            this.timeInSec = timeInSec;
            this.name = name;
        }
    }
}
