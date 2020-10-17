package com.example.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class VehicleTx {
    private String orderTx;
    private String objectId;
    private String make;
    private String year;
    private String model;
    private String category;

}
