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
package cn.edu.tsinghua.tvstore.benchmark.store;

import com.samsung.sra.datastore.ingest.CountBasedWBMH;
import org.apache.iotdb.db.exception.StorageEngineException;
import org.apache.iotdb.db.exception.metadata.MetadataException;
import org.apache.iotdb.db.exception.path.PathException;
import org.apache.iotdb.db.exception.storageGroup.StorageGroupException;
import org.apache.iotdb.db.qp.physical.crud.BatchInsertPlan;
import org.apache.iotdb.db.service.IoTDB;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TVStore extends Store {

    private IoTDB tvstore;

    public TVStore(String directory) {
        super(directory);
        this.tvstore = IoTDB.getInstance();
        this.tvstore.active();
    }

    @Override
    public CountBasedWBMH prepare(long streamID) {
        String storageGroupName = "root.group_" + streamID;
        String deviceName = "d";
        String sensorName = "s" + streamID;
        String dateType = "INT64";
        String encoding = "GORILLA";

        try {
            tvstore.register(storageGroupName, deviceName, sensorName, dateType, encoding);
        } catch (StorageEngineException | MetadataException | PathException | StorageGroupException | IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void append(long streamID, long[] time, long[] value) {
        String storageGroupName = "root.group_" + streamID;
        String deviceName = "d";
        String sensorName = "s" + streamID;

        String[] measurements = {sensorName};
        List<Integer> dataTypes = new ArrayList<>();
        dataTypes.add(TSDataType.INT64.ordinal());
        BatchInsertPlan batchInsertPlan = new BatchInsertPlan(
                storageGroupName + "." + deviceName,
                measurements, dataTypes);

        int t = 0;
        while (t < time.length) {
            int size = Math.min(time.length - t, 50_000);
            long[] times = Arrays.copyOfRange(time, t, t + size);
            Object[] columns = new Object[1];
            long[] values = Arrays.copyOfRange(value, t, t + size);
            columns[0] = values;
            batchInsertPlan.setTimes(times);
            batchInsertPlan.setColumns(columns);
            batchInsertPlan.setRowCount(times.length);
            try {
                tvstore.insertBatch(batchInsertPlan);
            } catch (StorageEngineException e) {
                logger.info(e.getMessage());
            }
            t += size;
        }
    }

    @Override
    public double query(long streamID, long startTime, long endTime, int aggregateNum) {
        String storageGroupName = "root.group_" + streamID;
        String deviceName = "d";
        String sensorName = "s" + streamID;
        String aggregateName;
        switch (aggregateNum) {
            case 0:
                aggregateName = "max";
                break;
            case 1:
                aggregateName = "min";
                break;
            case 2:
                aggregateName = "count";
                break;
            case 3:
                aggregateName = "sum";
                break;
            default:
                return 0.0;
        }
        try {
            return tvstore.query(storageGroupName + "." + deviceName + "." + sensorName, aggregateName, startTime, endTime);
        } catch (StorageEngineException e) {
            logger.info(e.getMessage());
        }
        return 0.0;
    }

    @Override
    public void flush(long streamID) {
        // do nothing
    }

    @Override
    public void finish(long streamID, CountBasedWBMH wbmh) {
        // do nothing
    }
}
