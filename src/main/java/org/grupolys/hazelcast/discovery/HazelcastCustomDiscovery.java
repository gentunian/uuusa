package org.grupolys.hazelcast.discovery;

import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HazelcastCustomDiscovery extends AbstractDiscoveryStrategy {

    private final DiscoveryNode discoveryNode;

    public HazelcastCustomDiscovery(DiscoveryNode discoveryNode, ILogger logger, //
            Map<String, Comparable> properties) {
        super(logger, properties);
        this.discoveryNode = discoveryNode;

        logger.info("SomeRestService discovery strategy started ");
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        String dh = System.getenv("DISCOVERY_HOSTNAME");
        System.out.println("[DISCOVERING NODES] discover nodes named " + dh);
        List<DiscoveryNode> discoveryNodes = new ArrayList<>();
        try {
            for (InetAddress adr : InetAddress.getAllByName(dh)) {
                Address address = new Address(adr.getHostAddress(), 5701);
                discoveryNodes.add(new SimpleDiscoveryNode(address));
                System.out.println("[DISCOVERING NODES]: Node: " + adr.getHostAddress() + " added to discovery list");
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return discoveryNodes;
    }

    // @Override
    // public void start() {
    // Address address = discoveryNode.getPrivateAddress();
    // String host = address.getHost();
    // int port = address.getPort();
    // execute(() -> someRestService.register(applicationScope, host, port));
    // }

    // @Override
    // public void destroy() {
    // Address address = discoveryNode.getPrivateAddress();
    // String host = address.getHost();
    // int port = address.getPort();
    // execute(() -> someRestService.unregister(applicationScope, host, port));
    // }

    // private Iterable<DiscoveryNode> mapEndpoints(List<Endpoint> endpoints) {
    // List<DiscoveryNode> discoveryNodes = new ArrayList<>();
    // for (Endpoint endpoint : endpoints) {
    // discoveryNodes.add(new SimpleDiscoveryNode(mapEndpoint(endpoint)));
    // }
    // return discoveryNodes;
    // }

    // private Address mapEndpoint(Endpoint endpoint) {
    // try {
    // String host = endpoint.getHost();
    // int port = endpoint.getPort();
    // return new Address(host, port);
    // } catch (UnknownHostException e) {
    // throw new RuntimeException(e);
    // }
    // }

    // private <T> T execute(Supplier<Call<T>> supplier) {
    // try {
    // Call<T> call = supplier.get();
    // return call.execute().body();
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
}
