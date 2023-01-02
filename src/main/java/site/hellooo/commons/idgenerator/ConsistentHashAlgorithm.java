package site.hellooo.commons.idgenerator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class ConsistentHashAlgorithm {
    private static final ConcurrentSkipListMap<Integer, Node> VIRTUAL_NODES = new ConcurrentSkipListMap<>();
    private static final int VIRTUAL_NODES_NUM = 10;
    private static CopyOnWriteArrayList<Node> nodes = new CopyOnWriteArrayList<>();

    private static void buildVirtualNodes() {
        nodes.forEach(node -> IntStream.range(0, VIRTUAL_NODES_NUM).forEach(index -> {
            String virtualNodeName = node.getUrl() + "&&VN" + index;
            Node virtualNode = new Node(virtualNodeName);
            int key = getHashCode(virtualNodeName);
            VIRTUAL_NODES.put(key, virtualNode);

        }));
    }

    private static CorePublisher<Node> doSelect(String ip) {
        int hash = getHashCode(ip);
        Map.Entry<Integer, Node> nodeEntry =
                Optional.ofNullable(VIRTUAL_NODES.ceilingEntry(hash)).orElse(VIRTUAL_NODES.firstEntry());
        Node node = nodeEntry.getValue();
        String url = node.getUrl();
        node = new Node(url.substring(0, url.indexOf("&&")));
        return new CorePublisher<>();
    }

    public static int getHashCode(String origin) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        hash = Math.abs(hash);

        return hash;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(getHashCode(i + ""));
        }
    }

    static class CorePublisher<T> {

    }
    static class Node {
        private String name;

        private String url;

        public String getUrl() {
            return this.url;
        }

        Node(String name) {
            this.name = name;
        }
    }
}
