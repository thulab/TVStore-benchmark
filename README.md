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