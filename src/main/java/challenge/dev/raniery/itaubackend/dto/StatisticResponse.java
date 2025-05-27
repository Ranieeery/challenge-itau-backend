package challenge.dev.raniery.itaubackend.dto;

import java.util.DoubleSummaryStatistics;

public class StatisticResponse {

    private final long count;
    private final double sum;
    private final double avg;
    private final double min;
    private final double max;

    public StatisticResponse(DoubleSummaryStatistics stats) {
        this.count = stats.getCount();
        this.sum = stats.getCount() == 0 ? 0.0 : stats.getSum();
        this.avg = stats.getCount() == 0 ? 0.0 : stats.getAverage();
        this.min = stats.getCount() == 0 ? 0.0 : stats.getMin();
        this.max = stats.getCount() == 0 ? 0.0 : stats.getMax();
    }

    public long getCount() {
        return count;
    }

    public double getSum() {
        return sum;
    }

    public double getAvg() {
        return avg;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
