# TVStore-benchmark

TVStore-benchmark is a tool for benchmarking various time series data compression solutions. 
Currently, TVStore-benchmark can support TVStore and SummaryStore.

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

   To measure throughput on synthetic data, you can run the following command:

    ```
    java -jar tvstore-benchmark-0.1.0-SNAPSHOT.jar
    ```

   To execute query process on synthetic data, you can run the following command:

    ```
    java -cp cn.edu.tsinghua.tvstore.benchmark.query.QuerySyntheticData tvstore-benchmark-0.1.0-SNAPSHOT.jar
    ```

   To measure throughput on redd low frequency data, you can run the following command:

    ```
    java -cp cn.edu.tsinghua.tvstore.benchmark.throughput.IngestREDDLowData tvstore-benchmark-0.1.0-SNAPSHOT.jar
    ```

   To execute query process on redd low frequency data, you can run the following command:

    ```
    java -cp cn.edu.tsinghua.tvstore.benchmark.query.QueryREDDLowData tvstore-benchmark-0.1.0-SNAPSHOT.jar
    ```