package com.basedew;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Account extends PanacheEntity {

    @Column(nullable=false)
    public Long userId;

    @Column(columnDefinition = "numeric(19,2) default 0.0")
    public double funds;

    @Column(columnDefinition = "numeric(19,2) default 0.0")
    public double blockedFunds;

    @Column(nullable=false)
    public String currency;
}
