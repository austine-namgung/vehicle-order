package com.example.order.controller;



import com.example.order.model.ResultMessage;
import com.example.order.model.VehicleOrder;
import com.example.order.repository.OrderRepository;
import com.example.order.saga.SagaManager;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {
	private final OrderRepository repository;
	private final SagaManager sagaManager;

    @GetMapping("/{tx}")
    public VehicleOrder vehicleTxInfo(@PathVariable String  tx){
        VehicleOrder vehicleOrder = repository.findById(tx).orElse(null);
        return vehicleOrder;
	}
	
	@PostMapping()
	public ResponseEntity<ResultMessage> insert( @RequestBody VehicleOrder order) throws Exception {
		
       
		int result = sagaManager.sagaTransaction(order);
        return getResponseEntity(result);
    }



    private ResponseEntity<ResultMessage> getResponseEntity(int result) {
		ResultMessage resultMessage;
		if (result > 0) {
			resultMessage = new ResultMessage("Y", "정상");
			return ResponseEntity.ok(resultMessage);
		}
		resultMessage = new ResultMessage("N", "오류");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultMessage);

	}

}
