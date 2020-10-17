package com.example.order.saga;

import com.example.order.model.ResultMessage;
import com.example.order.model.VehicleOrder;
import com.example.order.model.VehicleTx;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SagaManager {

    private static final String PRODUCT_TOPIC = "product-topic";
    private static final String PRODUCT_COMPENSATE_TOPIC = "product-cancel-topic";
    private static final String DELIVERY_TOPIC = "delivery-topic";
    private static final String SAGA_RREPLY_TOPIC = "saga-reply-topic";
    private static final String SAGA_COMPENSATE_RREPLY_TOPIC = "saga-cancel-reply-topic";

    private final OrderRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    public int sagaTransaction(VehicleOrder order) {
        log.info("====== SAGA 시작 ===============order: {}", order.toString());

        log.info("====== Saga 상태값: Pending ======");
        order.setOrderStatus("Pending");

        repository.save(order);

        try {
            VehicleTx vehicleTx = VehicleTx.builder()
                    .orderTx(order.getOrderTx())
                    .objectId(order.getObjectId())
                    .make(order.getMake())
                    .year(order.getYear())
                    .model(order.getModel())
                    .category(order.getCategory())
                    .build();

            String jsonData = mapper.writeValueAsString(vehicleTx);

            log.info("====== Product 서비스에 처리 요청  ===== jsonData: {}", jsonData);
           

            sendTopic(PRODUCT_TOPIC, jsonData);
        } catch (Exception e) {
            return 0;
        }
        return 1;
    }

    public void sendTopic(String topicName, String jsonData) throws JsonProcessingException {
        kafkaTemplate.send(topicName, jsonData);
    }

    @KafkaListener(topics = SAGA_RREPLY_TOPIC, groupId = "saga")
    public void listen(String message) throws JsonMappingException, JsonProcessingException {
        

        ResultMessage resultMessage = mapper.readValue(message,new TypeReference<ResultMessage>(){});
        String sucessYn = resultMessage.getSuccessYn();
        String serviceCode = resultMessage.getServiceCode();
        
        if("Y".equals(sucessYn) ){
            
            if("product".equals(serviceCode) ){
                log.info("======= Product 서비스 에서보낸 메세지 saga-reply-topic - Consumer 도착 ========정상========= message: {}" + message);
               
                log.info("======= Delivery 서비스에  처리 요청 DELIVERY_TOPIC=========");
                sendTopic(DELIVERY_TOPIC, resultMessage.getJasonTx());

            }else if("delivery".equals(serviceCode) ){
                log.info("======= Delivery 서비스에서 보낸 메세지 saga-reply-topic - Consumer 도착 ========정상========= message: {}" + message);
             
                VehicleOrder order = mapper.readValue(resultMessage.getJasonTx(),new TypeReference<VehicleOrder>(){});
                order.setOrderStatus("Completed");
                log.info("======= Saga 상태값: Completed ======");
                repository.save(order);     
                log.info("===== 성공성공 =====Order 서비스에 최종 완료 처리 저장 ===completed Trasaction ======");     
                log.info("======= End Transaction ======");        
                 
            }      
        }else{
            if("product".equals(serviceCode)){
                log.info("===[장애 수신]=== Product 서비스 장애 수신 ======"); 
                VehicleOrder order = mapper.readValue(resultMessage.getJasonTx(),new TypeReference<VehicleOrder>(){});
                order.setOrderStatus("Canceled");               
                repository.save(order);
                log.info("=====[장애처리]== Saga 상태값: Canceled ==처리====");

                log.info("====[트랜잭션 종료]=== End Transaction ======");  

            }else if("delivery".equals(serviceCode) ){
                log.info("===[장애 수신]=== Delivery 서비스 장애 수신 ======");   

                log.info("====[보상처리] 보상 Trasaction 시작 ========");
                log.info("====[보상처리]  Product에 보상처리 요청 전송 (Topic : PRODUCT_COMPENSATE_TOPIC) ========");
                sendTopic(PRODUCT_COMPENSATE_TOPIC, resultMessage.getJasonTx());
            }
        }
    }

    @KafkaListener(topics = SAGA_COMPENSATE_RREPLY_TOPIC, groupId = "saga")
    public void cancelListen(String message) throws JsonMappingException, JsonProcessingException {
        

        ResultMessage resultMessage = mapper.readValue(message,new TypeReference<ResultMessage>(){});
        String sucessYn = resultMessage.getSuccessYn();
        String serviceCode = resultMessage.getServiceCode();
        if("Y".equals(sucessYn) ){
            if("product".equals(serviceCode) ){
                log.info("====[보상처리]  Product 서비스의  보상 처리 결과 수신 message: {}",message);
                VehicleOrder order = mapper.readValue(resultMessage.getJasonTx(),new TypeReference<VehicleOrder>(){});
                order.setOrderStatus("Canceled");
                repository.save(order);
                log.info("=====[보상처리]== Saga 상태값: Canceled ==처리====");
            }
        }
        log.info("====[보상처리]=== End Transaction ======");  

    }

	
}
