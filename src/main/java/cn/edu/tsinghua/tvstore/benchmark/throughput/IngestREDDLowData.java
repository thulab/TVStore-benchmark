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
import cn.edu.tsinghua.tvstore.benchmark.utils.DataReader;
import com.samsung.sra.datastore.ingest.CountBasedWBMH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;

public class IngestREDDLowData {

    private static final Logger logger = LoggerFactory.getLogger(IngestREDDLowData.class);
    private static final String prefix = "/house_";
    private static final String suffix = "/channel_1.dat";
    private static final int[] cycles = {50_000, 100_000, 50_000, 50_000, 250_000};
    private static final int threadsNum = 5;

    private static final String[] fileNames = new String[5];

    public static void main(String[] args) throws IOException, InterruptedException {
        String storeType = ConfigDescriptor.getInstance().getConfig().getStoreType();
        String directory = ConfigDescriptor.getInstance().getConfig().getDirectory();
        String reddLowDirectory = ConfigDescriptor.getInstance().getConfig().getReddLowDirectory();
        for (int i = 0; i < 5; i++) {
            fileNames[i] = reddLowDirectory + prefix + (i + 1) + suffix;
        }
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
        StreamWriter[] writers = new StreamWriter[threadsNum];
        Thread[] writerThreads = new Thread[threadsNum];
        for (int i = 0; i < threadsNum; i++) {
            writers[i] = new StreamWriter(store, parallelismSem, i, fileNames[i]);
            writerThreads[i] = new Thread(writers[i], i + "-appender");
        }
        for (int i = 0; i < threadsNum; ++i) {
            writerThreads[i].start();
        }
        for (int i = 0; i < threadsNum; ++i) {
            writerThreads[i].join();
        }
    }

    private static class StreamWriter implements Runnable {
        private final long streamID;
        private final Store store;
        private final Semaphore semaphore;
        private final String fileName;

        private StreamWriter(Store store, Semaphore semaphore, long streamID, String fileName) {
            this.store = store;
            this.semaphore = semaphore;
            this.streamID = streamID;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            if (semaphore != null) {
                semaphore.acquireUninterruptibly();
            }
            try {
                CountBasedWBMH wbmh = store.prepare(streamID);
                DataReader reader = new DataReader(fileName);
                List<String> data = reader.readData();

                long[] time = new long[data.size()];
                long[] value = new long[data.size()];

                int cnt = 0;
                for (String point : data) {
                    time[cnt] = Long.parseLong(point.split(" ")[0]);
                    value[cnt] = Long.parseLong(point.split(" ")[1].replace(".", ""));
                    cnt++;
                }

                for (int i = 0; i < cycles[(int) streamID]; i++) {
                    store.append(streamID, time, value);
                    logger.info("streamID {} cycle {}", streamID, i);
                    int len = time.length;
                    long base = time[time.length - 1] - time[0];
                    for (int j = 0; j < len; j++) {
                        time[j] += base + 1;
                    }
                    if ((i + 1) % 1_000 == 0) {
                        store.flush(streamID);
                    }
                }
                if (semaphore != null) {
                    store.finish(streamID, wbmh);
                }
            } finally {
                if (semaphore != null) {
                    semaphore.release();
                }
            }
        }
    }
}
