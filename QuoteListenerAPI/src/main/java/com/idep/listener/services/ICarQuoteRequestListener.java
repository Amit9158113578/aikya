package com.idep.listener.services;

import java.io.IOException;
import java.text.ParseException;

import javax.jms.Message;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ICarQuoteRequestListener {
	ObjectNode preProcessing(ObjectNode input,String carrierId)throws JsonProcessingException, IOException, ParseException;
	boolean validate(ObjectNode input,String carrierId);
	ObjectNode process(ObjectNode input,JsonObject vehicleDetails,String carrierId) throws JsonProcessingException, IOException;
	String onMessage(Message message) throws ParseException;
	ObjectNode calculateVehicleAge(ObjectNode input) throws ParseException;
}
