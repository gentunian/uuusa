package org.grupolys.hazelcast;

import java.util.Collection;
import java.util.Map;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import org.grupolys.hazelcast.discovery.HazelcastCustomDiscovery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfiguration {
    @Bean
    public Config config() {
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        config.setProperty("hazelcast.discovery.enabled", "true");
        config.setProperty("hazelcast.socket.bind.any", "false");
        config.setProperty("hazelcast.discovery.public.ip.enabled", "false");
        config.setProperty("hazelcast.local.localAddress", System.getenv("IP"));
        config.setProperty("hazelcast.local.publicAddress", System.getenv("IP"));

        DiscoveryConfig discoveryConfig = joinConfig.getDiscoveryConfig();
        DiscoveryStrategyConfig strategyConfig = new DiscoveryStrategyConfig(new DiscoveryStrategyFactory() {

            @Override
            public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger logger,
                    Map<String, Comparable> properties) {
                return new HazelcastCustomDiscovery(discoveryNode, logger, properties);
            }

            @Override
            public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
                return HazelcastCustomDiscovery.class;
            }

            @Override
            public Collection<PropertyDefinition> getConfigurationProperties() {
                return null;
            }
        });

        discoveryConfig.addDiscoveryStrategyConfig(strategyConfig);
        return config;
    }
}
