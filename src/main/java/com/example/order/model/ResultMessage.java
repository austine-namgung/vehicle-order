package com.example.order.model;

import lombok.Getter;
import lombok.Setter;

/**
 * ResultMessage
 */
@Getter
@Setter
public class ResultMessage {

    private String successYn;
	private String txCode;
	private String serviceCode;	 
	private String jasonTx;
	private String message;
	private String devMessage;
	
	
	public ResultMessage() {		
	}

	public ResultMessage(String successYn) {
		this.successYn = successYn;
		this.txCode = null;
		this.serviceCode = null;
		this.jasonTx=null;
		this.message = null;
		this.devMessage = null;
	}
	public ResultMessage(String successYn, String message) {
		this.successYn = successYn;
		this.txCode = null;
		this.serviceCode = null;
		this.jasonTx=null;
		this.message = message;
		this.devMessage = null;
	}
	
	

}