package com.metarnet.core.common.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Administrator on 2015/8/26.
 */
@Entity
@Table(name="UNI_SEQUENCE")
public class UniSequence {
    private String name;
    private Long currentValue;
    private Integer increment;

    @Id
    @Column(name="NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name="CURRENT_VALUE")
    public Long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
    }
    @Column(name="INCREMENT_VALUE")
    public Integer getIncrement() {
        return increment;
    }

    public void setIncrement(Integer increment) {
        this.increment = increment;
    }
}
