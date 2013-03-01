package com.spartansoftwareinc;

/**
 * Aggregate data representation for LQI displayed in SegmentAttributeTableView.
 */
public class LQIStatistics implements ITSStats {
    private String type, value;
    private Double minRange, maxRange;
    private Integer count = 0;

    @Override
    public String getDataCategory() {
        return "LQI";
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getValue() {
        if (minRange != null && maxRange != null) {
            return minRange + "-" + maxRange;
        } else if (value != null) {
            return value;
        }
        return null;
    }

    @Override
    public Integer getCount() {
        return count;
    }

    @Override
    public void setCount(Integer count) {
        this.count = count;
    }

    public void setRange(double range) {
        if (minRange == null || minRange > range) {
            minRange = range;
        }
        if (maxRange == null || maxRange < range) {
            maxRange = range;
        }
        count++;
    }

    public void setValue(String val) {
        this.value = val;
        count++;
    }
}
