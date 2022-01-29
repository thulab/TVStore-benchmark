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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigDescriptor {

    private static final Logger logger = LoggerFactory.getLogger(ConfigDescriptor.class);

    private final Config config;

    private ConfigDescriptor() {
        config = new Config();
        logger.info("load parameters from config.properties");
        loadPropsFromFile();
    }

    public static ConfigDescriptor getInstance() {
        return ConfigDescriptorHolder.INSTANCE;
    }

    private void loadPropsFromFile() {
        try (InputStream in = new FileInputStream(Constants.CONFIG_FILE)) {
            Properties properties = new Properties();
            properties.load(in);

            config.setStoreType(properties.getProperty("storeType", "tvstore"));
            config.setDirectory(properties.getProperty("directory", "/path/to/data"));
            config.setNumValuesPerThread(Long.parseLong(properties.getProperty("numValuesPerThread", "31_250_000_000").replace("_", "")));
            config.setNumThreads(Integer.parseInt(properties.getProperty("numThreads", "10")));
            config.setDistribution(properties.getProperty("distribution", "evenly-spaced"));
            config.setReddLowDirectory(properties.getProperty("reddLowDirectory", "dataset/redd/low_freq"));
            config.setTotalStreamNum(Integer.parseInt(properties.getProperty("totalStreamNum", "10")));
            config.setQueryTimes(Integer.parseInt(properties.getProperty("queryTimes", "100")));

        } catch (IOException e) {
            logger.error("Fail to load properties: ", e);
        }
    }

    public Config getConfig() {
        return config;
    }

    private static class ConfigDescriptorHolder {
        private static final ConfigDescriptor INSTANCE = new ConfigDescriptor();
    }
}
