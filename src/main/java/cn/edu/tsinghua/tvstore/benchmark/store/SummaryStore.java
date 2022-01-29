
package cn.edu.tsinghua.tvstore.benchmark.store;

import com.samsung.sra.datastore.RationalPowerWindowing;
import com.samsung.sra.datastore.ResultError;
import com.samsung.sra.datastore.StreamException;
import com.samsung.sra.datastore.aggregates.BloomFilterOperator;
import com.samsung.sra.datastore.aggregates.CMSOperator;
import com.samsung.sra.datastore.aggregates.MaxOperator;
import com.samsung.sra.datastore.aggregates.MinOperator;
import com.samsung.sra.datastore.aggregates.SimpleCountOperator;
import com.samsung.sra.datastore.aggregates.SumOperator;
import com.samsung.sra.datastore.ingest.CountBasedWBMH;
import com.samsung.sra.datastore.storage.BackingStoreException;

import java.io.IOException;

public class SummaryStore extends Store {

    com.samsung.sra.datastore.SummaryStore store;
    CountBasedWBMH wbmh;

    public SummaryStore(String directory) {
        super(directory);
    }

    @Override
    public void prepare(long streamID) {
        wbmh = new CountBasedWBMH(new RationalPowerWindowing(1, 1, 20, 1))
                .setValuesAreLongs(true)
                .setBufferSize(800_000_000)
                .setWindowsPerMergeBatch(100_000)
                .setParallelizeMerge(100);
        try {
            store.registerStream(streamID, false, wbmh,
                    new MaxOperator(),
                    new MinOperator(),
                    new SimpleCountOperator(),
                    new SumOperator(),
                    new CMSOperator(5, 1000, 0),
                    new BloomFilterOperator(5, 1000));
        } catch (StreamException | BackingStoreException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void append(long streamID, long[] time, long[] value) {
        try {
            for (int i = 0; i < time.length; i++) {
                store.append(streamID, time[i], value[i]);
            }
        } catch (StreamException | BackingStoreException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public double query(long streamID, long startTime, long endTime, int aggregateNum) {
        try {
            ResultError re = (ResultError) store.query(streamID, startTime, endTime, aggregateNum);
            return Double.parseDouble(re.result.toString());
        } catch (StreamException | BackingStoreException e) {
            logger.error(e.getMessage());
        }
        return 0.0;
    }

    @Override
    public void flush(long streamID) {
        try {
            store.flush(streamID);
        } catch (StreamException | BackingStoreException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void finish(long streamID) {
        try {
            wbmh.flushAndSetUnbuffered();
            store.unloadStream(streamID);
        } catch (IOException | StreamException | BackingStoreException e) {
            logger.error(e.getMessage());
        }
    }
}
