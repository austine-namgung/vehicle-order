package com.example.order.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;



import lombok.Data;

@Entity
@Data
@Table(name = "vehicle_order")
public class VehicleOrder {
    @Id
    @Column(name = "order_tx")
    private String orderTx;
    @Column(name = "object_id")
    private String objectId;
    @Column(name = "make")
    private String make;
    @Column(name = "year")
    private String year;
    @Column(name = "model")
    private String model;
    @Column(name = "category")
    private String category;

    @Column(name = "order_status")
    private String orderStatus;

}
