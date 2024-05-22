 package com.idep.proposal.impl.service;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class CarProposalServiceImpl {
   ObjectMapper objectMapper = new ObjectMapper();
   
   public String submitCarProposal(String proposal) {
     JsonNode reqNode = null;
     try {
       reqNode = this.objectMapper.readTree(proposal);
       return reqNode.toString();
     } catch (Exception e) {
       return reqNode.toString();
     } 
   }
   public String sendMessage(String proposal) {
     return proposal;
   }
 }


