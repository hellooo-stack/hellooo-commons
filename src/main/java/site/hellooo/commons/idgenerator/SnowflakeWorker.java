package site.hellooo.commons.idgenerator;

import java.util.Optional;

public class SnowflakeWorker {

//    64位比特位分布：按官方约定分，即：1个保留位、41位时间戳、10位工作机器id、12位序列号
//    其中，10位工作机器id(workId)分5位数据中心id(dataCenterId) + 5位机器id(machineId)
    private static final int SEQUENCE_BITS = 12;
    private static final int WORKER_ID_BITS = 10;
    private static final int TIMESTAMP_BITS = 41;
    private static final int REMAINING_BITS = 1;

//    41位时间戳从2022-07-01 00:00:00 开始计算
//    2022-07-01 00:00:00
    private static final long START_TIMESTAMP = 1656604800000L;

    private long dataCenterId;
    private long dataCenterIdBits;
    private long machineId;
    private long machineIdBits;

    private final long machineIdShift;
    private final long dataCenterIdShift;
    private final long timestampShift;

//    当前bit下的最大值
    private final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private long sequence;
    private long lastTimestamp = -1L;


    private SnowflakeWorker(SnowflakeWorkerBuilder builder) {
        this.dataCenterId = builder.dataCenterId;
        this.dataCenterIdBits = builder.dataCenterIdBits;
        this.machineId = builder.machineId;
        this.machineIdBits = builder.machineIdBits;

        this.machineIdShift = SEQUENCE_BITS;
        this.dataCenterIdShift = machineIdShift + machineIdBits;
        this.timestampShift = dataCenterIdShift + WORKER_ID_BITS;
    }

    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();
        boolean throwException = false;

        if (currentTimestamp == lastTimestamp) {
//            如果sequence溢出，那么0 & 最大值还是0
//            如果sequence不溢出，那么sequence & 最大值 还是sequence
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
//                如果当前时间戳序列号已满，那么时间戳++，序号从0开始算
                currentTimestamp = tillNextMills(lastTimestamp);
            }
        } else if (currentTimestamp > lastTimestamp) {
            sequence = 0;
        } else {
            throwException = true;
        }

        if (throwException) {
            throw new IllegalArgumentException("Fatal: current timestamp is smaller that last timestamp, currentTimestamp=" + currentTimestamp + ", lastTimestamp=" + lastTimestamp);
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - START_TIMESTAMP) << timestampShift)
                | (dataCenterId << dataCenterIdShift)
                | (machineId << machineIdShift)
                | sequence;
    }

    private long tillNextMills(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public static final class SnowflakeWorkerBuilder {
        private static final long DEFAULT_DATA_CENTER_ID_BITS = 5;
        private static final long DEFAULT_MACHINE_ID_BITS = 5;

        private long dataCenterId;
        private long dataCenterIdBits = DEFAULT_DATA_CENTER_ID_BITS;
        private long machineId;
        private long machineIdBits = DEFAULT_MACHINE_ID_BITS;

        public SnowflakeWorker build() {
            Optional.of(this)
                    .filter(builder -> builder.dataCenterIdBits + builder.machineIdBits == WORKER_ID_BITS)
                    .orElseThrow(() -> new IllegalArgumentException("Fatal: dataCenterIdBits plus machineIdBits should be " + WORKER_ID_BITS + ", please check!"));

            return new SnowflakeWorker(this);
        }

        public SnowflakeWorkerBuilder dataCenterId(long dataCenterId) {
            this.dataCenterId = dataCenterId;
            long maxDataCenterId = ~(-1L << dataCenterIdBits);

            Optional.of(dataCenterId)
                    .filter(id -> id <= maxDataCenterId)
                    .orElseThrow(() -> new IllegalArgumentException("Fatal: dataCenterId should less than or equals " + maxDataCenterId + " but got " + dataCenterId + ", please check!"));

            return this;
        }

        public SnowflakeWorkerBuilder dataCenterIdBits(long dataCenterIdBits) {
            this.dataCenterIdBits = dataCenterIdBits;
            return this;
        }

        public SnowflakeWorkerBuilder machineId(long machineId) {
            this.machineId = machineId;
            long maxMachineId = ~(-1L << machineIdBits);

            Optional.of(machineId)
                    .filter(id -> id <= maxMachineId)
                    .orElseThrow(() -> new IllegalArgumentException("Fatal: maxMachineId should less than or equals " + maxMachineId + " but got " + machineId + ", please check!"));

            return this;
        }

        public SnowflakeWorkerBuilder machineIdBits(long machineIdBits) {
            this.machineIdBits = machineIdBits;
            return this;
        }
    }
}
