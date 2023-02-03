package site.hellooo.commons.hashing;

import com.google.common.hash.Hashing;
import site.hellooo.commons.checks.ArgChecker;
import site.hellooo.commons.checks.ObjectStateChecker;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConsistentHashing<N> {

    private static final int DEFAULT_VIRTUAL_NODES_NUM = 10;
    private static final KeyGenerator<String> STRING_KEY_GENERATOR = node -> node;

    private final int virtualNodesNum;
    private final ConcurrentSkipListMap<Integer, VirtualNode<N>> VIRTUAL_NODES = new ConcurrentSkipListMap<>();
    private final KeyGenerator<N> keyGenerator;

    private final Map<N, Integer> counterMap = new HashMap<>();


    public static ConsistentHashing<String> newStringHashing() {
        return newStringHashing(DEFAULT_VIRTUAL_NODES_NUM);
    }

    public static ConsistentHashing<String> newStringHashing(final int virtualNodesNum) {
        return newHashing(virtualNodesNum, STRING_KEY_GENERATOR);
    }

    public static ConsistentHashing<String> newStringHashing(final int virtualNodesNum, Collection<String> nodes) {
        return newHashing(virtualNodesNum, STRING_KEY_GENERATOR, nodes);
    }

    public static <NODE> ConsistentHashing<NODE> newHashing(KeyGenerator<NODE> keyGenerator) {
        return new ConsistentHashing<>(keyGenerator);
    }

    public static <NODE> ConsistentHashing<NODE> newHashing(int virtualNodesNum, KeyGenerator<NODE> keyGenerator) {
        return new ConsistentHashing<>(virtualNodesNum, keyGenerator);
    }

    public static <NODE> ConsistentHashing<NODE> newHashing(int virtualNodesNum, KeyGenerator<NODE> keyGenerator, Collection<NODE> nodes) {
        return new ConsistentHashing<>(virtualNodesNum, keyGenerator, nodes);
    }

    private ConsistentHashing(KeyGenerator<N> keyGenerator) {
        this(DEFAULT_VIRTUAL_NODES_NUM, keyGenerator, null);
    }

    private ConsistentHashing(int virtualNodesNum, KeyGenerator<N> keyGenerator) {
        this(virtualNodesNum, keyGenerator, null);
    }

    private ConsistentHashing(int virtualNodesNum, KeyGenerator<N> keyGenerator, Collection<N> nodes) {
        this.virtualNodesNum = virtualNodesNum;
        this.keyGenerator = keyGenerator;

        Optional.ofNullable(nodes)
                .filter(ns -> ns.size() > 0)
                .ifPresent(this::addNodes);
    }

    private void addNodeIfAbsent(N node, int virtualNodesNum) {

        Integer counter = counterMap.get(node);
        if (counter != null) {
            return;
        }

//        one physical node + virtual node
        int totalNodesNum = 1 + virtualNodesNum;
        for (int i = 0; i < totalNodesNum; i++) {
            VirtualNode<N> virtualNode = newVirtualNode(node);
            VIRTUAL_NODES.put(hashNode(node, virtualNode.index), virtualNode);
        }
    }

    public void addNode(N node) {
        addNode(node, virtualNodesNum);
    }

    public void addNodes(Collection<N> nodes) {
        nodes.forEach(this::addNode);
    }

    public void addNode(N node, int virtualNodesNum) {
        ArgChecker.checkNotNull(node, "adding node is expected to be not null");
        ArgChecker.check(virtualNodesNum >= 0, "virtualNodesNum is " + virtualNodesNum + " (expected >= 0).");

        addNodeIfAbsent(node, virtualNodesNum);
    }

    public void removeNode(N node) {
        ArgChecker.checkNotNull(node, "removing node is expected to be not null");

        Integer counter = counterMap.get(node);
        if (counter != null) {
            for (int i = 0; i < counter; i++) {
                VIRTUAL_NODES.remove(hashNode(node, i));
            }
        }
    }

    public N get(String target) {
        ArgChecker.checkNotNull(target, "target is expected to be not null");
        ObjectStateChecker.checkMapNotEmpty(VIRTUAL_NODES, "");
        Integer hashCode = hash(target);
        Map.Entry<Integer, VirtualNode<N>> nodeEntry = Optional.ofNullable(VIRTUAL_NODES.ceilingEntry(hashCode))
                .orElse(VIRTUAL_NODES.firstEntry());

        return nodeEntry.getValue().physicalNode;
    }

    private int hash(String key) {

        return Hashing.murmur3_128()
                .hashBytes(key.getBytes(StandardCharsets.UTF_8))
                .asInt();
    }

    private int hashNode(N node, int index) {
        return hash(keyGenerator.generate(node) + "#" + index);
    }

    private VirtualNode<N> newVirtualNode(N node) {
        Integer counter = counterMap.get(node);
        if (counter == null) {
            counterMap.put(node, counter = 0);
        } else {
            counterMap.put(node, ++counter);
        }

        return new VirtualNode<>(node, counter);
    }

    private static class VirtualNode<N> {
        private final N physicalNode;
        private final int index;

        private VirtualNode(N physicalNode, final int index) {
            this.physicalNode = physicalNode;
            this.index = index;
        }
    }

    public interface KeyGenerator<N> {
        String generate(N node);
    }
}
