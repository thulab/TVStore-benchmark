# TVStore-benchmark

TVStore-benchmark is a benchmarking suite, with workloads and datasets, for fair comparisons between various time series store solutions, which might or might not support lossy compression. Currently, TVStore-benchmark supports TVStore, SummaryStore, Apache IoTDB and RRDTool. It constitutes partial contribution of our work on TVStore. For details please refer to our FAST'22 paper:

* Yanzhe An, Yue Su, Yuqing Zhu, and Jianmin Wang. **"TVStore: Automatically Bounding Time Series Storage via Time-Varying Compression."** In 20th USENIX Conference on File and Storage Technologies (FAST 22). 2022. [[pdf](https://www.usenix.org/conference/fast22/technical-sessions)]  

## Prerequisites

To use TVStore-benchmark, you need to have:
1. Java >= 1.8
2. Maven >= 3.1

## Installation

Run the following commands to install TVStore-benchmark.

```
git clone https://github.com/thulab/TVStore-benchmark.git
cd TVStore-benchmark
mvn clean install -Dmaven.test.skip=true
```

## Usage

Take TVStore for example:

1. Install, compile and run TVStore

    Please refer to [TVStore](https://github.com/thulab/TVStore).

2. Modify configuration 

   Modify `conf/config.properties` according to your needs. Note that parameter **directory** must be modified.

3. Run TVStore-benchmark

   First, enter the binary directory.

    ```
    cd bin
    ```

   Then, run the corresponding command to meet your needs. 

   - To measure throughput on synthetic data:

     ```
     ./ingest_synthetic_data.sh
     ```

   - To execute query process on synthetic data:

     ```
     ./query_synthetic_data.sh
     ```

   - To measure throughput on redd low frequency data:

     ```
     ./ingest_redd_low_data.sh
     ```

   - To execute query process on redd low frequency data:

     ```
     ./query_redd_low_data.sh
     ```

## How to benchmark your solution

If you want to use TVStore-benchmark to benchmark your own time series store solution, you should complete 
a new class that extends `cn.edu.tsinghua.tvstore.benchmark.store.Store` and implement five necessary 
methods, including `prepare`, `append`, `query`, `flush` and `finish`.
