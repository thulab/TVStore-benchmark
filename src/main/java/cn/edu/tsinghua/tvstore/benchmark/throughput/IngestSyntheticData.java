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
package cn.edu.tsinghua.tvstore.benchmark.throughput;

import cn.edu.tsinghua.tvstore.benchmark.conf.ConfigDescriptor;
import cn.edu.tsinghua.tvstore.benchmark.store.Store;
import cn.edu.tsinghua.tvstore.benchmark.store.SummaryStore;
import cn.edu.tsinghua.tvstore.benchmark.store.TVStore;
import cn.edu.tsinghua.tvstore.benchmark.utils.ParetoDistribution;
import cn.edu.tsinghua.tvstore.benchmark.utils.PoissonDistribution;
import com.samsung.sra.datastore.ingest.CountBasedWBMH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SplittableRandom;
import java.util.concurrent.Semaphore;

public class IngestSyntheticData {

    private static final Logger logger = LoggerFactory.getLogger(IngestSyntheticData.class);
    private static final int batchSize = 50_000;

    public static void main(String[] args) throws Exception {
        String storeType = ConfigDescriptor.getInstance().getConfig().getStoreType();
        String directory = ConfigDescriptor.getInstance().getConfig().getDirectory();
        long T = ConfigDescriptor.getInstance().getConfig().getNumValuesPerThread();
        int nThreads = ConfigDescriptor.getInstance().getConfig().getNumThreads();
        Semaphore parallelismSem = new Semaphore(100);
        Runtime.getRuntime().exec(new String[]{"sh", "-c", "rm -rf " + directory}).waitFor();

        Store store;
        if (storeType.equals("tvstore")) {
            store = new TVStore(directory);
        } else if (storeType.equals("summarystore")) {
            store = new SummaryStore(directory);
        } else {
            logger.error("invalid store {}", storeType);
            return;
        }
        StreamWriter[] writers = new StreamWriter[nThreads];
        Thread[] writerThreads = new Thread[nThreads];
        for (int i = 0; i < nThreads; ++i) {
            writers[i] = new StreamWriter(store, parallelismSem, i, T);
            writerThreads[i] = new Thread(writers[i], i + "-appender");
        }
        for (int i = 0; i < nThreads; ++i) {
            writerThreads[i].start();
        }
        for (int i = 0; i < nThreads; ++i) {
            writerThreads[i].join();
        }
    }

    private static class StreamWriter implements Runnable {
        private final long streamID, N;
        private final Store store;
        private final Semaphore semaphore;
        private final SplittableRandom splittableRandom;

        private StreamWriter(Store store, Semaphore semaphore, long streamID, long N) {
            this.store = store;
            this.semaphore = semaphore;
            this.streamID = streamID;
            this.N = N;
            this.splittableRandom = new SplittableRandom(streamID);
        }

        @Override
        public void run() {
            if (semaphore != null) {
                semaphore.acquireUninterruptibly();
            }
            try {
                CountBasedWBMH wbmh = store.prepare(streamID);
                long maxLatency = Long.MIN_VALUE;
                long minLatency = Long.MAX_VALUE;
                double avgLatency = 0;
                long currentTime = System.currentTimeMillis();
                long startTime = System.currentTimeMillis();
                long[] times = new long[batchSize];
                long[] values = new long[batchSize];
                long time = 0;
                String distribution = ConfigDescriptor.getInstance().getConfig().getDirectory();
                PoissonDistribution poissonDistribution = new PoissonDistribution(10);
                ParetoDistribution paretoDistribution = new ParetoDistribution(1.0, 1.2);
                for (long t = 0; t < N; ++t) {
                    switch (distribution) {
                        case "evenly-spaced":
                            time = t;
                            break;
                        case "poisson":
                            time += 1 + poissonDistribution.next(splittableRandom);
                            break;
                        case "pareto":
                            time += 1 + paretoDistribution.next(splittableRandom);
                            break;
                        default:
                            logger.error("invalid distribution {}", distribution);
                            break;
                    }
                    long v = splittableRandom.nextInt(100);
                    times[(int) (t % batchSize)] = time;
                    values[(int) (t % batchSize)] = v;
                    if ((t + 1) % batchSize == 0) {
                        maxLatency = Math.max(System.currentTimeMillis() - startTime, maxLatency);
                        minLatency = Math.min(System.currentTimeMillis() - startTime, minLatency);
                        avgLatency += System.currentTimeMillis() - startTime;
                        startTime = System.currentTimeMillis();
                        store.append(streamID, times, values);
                    }
                    if ((t + 1) % 100_000_000 == 0) {
                        logger.info("Stream {} Batch {}: cost {}s, throughput {}points/s, max latency {}ms, " +
                                        "min latency {}ms, avg latency {}ms", streamID, (t + 1) / 100_000_000,
                                (System.currentTimeMillis() - currentTime) / 1_000d,
                                Math.round(100_000_000d / ((System.currentTimeMillis() - currentTime) / 1_000d)),
                                maxLatency, minLatency, avgLatency / 2_000d);
                        maxLatency = Long.MIN_VALUE;
                        minLatency = Long.MAX_VALUE;
                        avgLatency = 0;
                        currentTime = System.currentTimeMillis();
                    }
                    if ((t + 1) % 400_000_000 == 0) {
                        store.flush(streamID);
                    }
                }
                if (semaphore != null) {
                    store.finish(streamID, wbmh);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (semaphore != null) {
                    semaphore.release();
                }
            }
        }
    }
}
