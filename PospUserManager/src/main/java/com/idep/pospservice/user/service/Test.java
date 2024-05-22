package com.idep.pospservice.user.service;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.pospservice.util.POSPServiceConstant;

public class Test {
	
	public static void main(String[] args) {
		
		
	
		
		
		
		
		
	}
		public void readJsonFormat(){ 
		ObjectMapper obj = new ObjectMapper();
		String req = "[{ 	\"pospMenuConfig\": [{ 		\"defaultScreenId\": \"POSPSHOME\", 		\"icon\": \"home.svg\", 		\"isActive\": false, 		\"read\": true, 		\"write\": false, 		\"delete\": false, 		\"menuId\": \"POSPMENUDASH\", 		\"menuName\": \"DashBoard\", 		\"url\": \"/Dashboard\" 	}, 	{ 		\"defaultScreenId\": \"POSPSHOME\", 		\"icon\": \"user.svg\", 		\"isActive\": true, 		\"read\": true, 		\"write\": false, 		\"delete\": true, 		\"menuId\": \"POSPMENULEADS\", 		\"menuName\": \"My Leads\", 		\"subMenu\": [{ 			\"isActive\": true, 			\"read\": false, 			\"write\": false, 			\"delete\": false, 			\"menuId\": \"POSPMENULEADS\", 			\"subMenu\": \"Add New Leads\", 			\"subMenuId\": \"SMENUNELEAD\", 			\"url\": \"/leads/addLead\" 		}, 		{ 			\"isActive\": false, 			\"read\": false, 			\"write\": false, 			\"delete\": false, 			\"menuId\": \"POSPMENULEADS\", 			\"subMenu\": \"Viewing Leads\", 			\"subMenuId\": \"SMENUVIEWLEAD\", 			\"url\": \"/leads/viewLeads\" 		}, 		{ 			\"isActive\": true, 			\"read\": true, 			\"write\": false, 			\"delete\": true, 			\"menuId\": \"POSPMENULEADS\", 			\"subMenu\": \"Pending Payment\", 			\"subMenuId\": \"SMENUPAYPENDIN\", 			\"url\": \"/leads/payments\" 		}, 		{ 			\"isActive\": true, 			\"read\": false, 			\"write\": false, 			\"delete\": false, 			\"menuId\": \"POSPMENULEADS\", 			\"subMenu\": \"Pending Proposal\", 			\"subMenuId\": \"SMENUPROPPENDI\", 			\"url\": \"/leads/proposals\" 		}], 		\"url\": \"\" 	}, 	{ 		\"defaultScreenId\": \"POSPSHOME\", 		\"icon\": \"repeat.svg\", 		\"isActive\": true, 		\"read\": true, 		\"write\": false, 		\"delete\": true, 		\"menuId\": \"POSPMENURENEW\", 		\"menuName\": \"My Renewals\", 		\"url\": \"\" 	}] }, { 	\"pospMenuConfig\": [{ 		\"defaultScreenId\": \"POSPSHOME\", 		\"icon\": \"home.svg\", 		\"isActive\": true, 		\"read\": true, 		\"write\": true, 		\"delete\": true, 		\"menuId\": \"POSPMENUDASH\", 		\"menuName\": \"DashBoard\", 		\"url\": \"/Dashboard\" 	}, 	{ 		\"defaultScreenId\": \"POSPSHOME\", 		\"icon\": \"user.svg\", 		\"isActive\": true, 		\"read\": true, 		\"write\": true, 		\"delete\": true, 		\"menuId\": \"POSPMENULEADS\", 		\"menuName\": \"My Leads\", 		\"subMenu\": [{ 			\"isActive\": true, 			\"read\": true, 			\"write\": true, 			\"delete\": true, 			\"menuId\": \"POSPMENULEADS\", 			\"subMenu\": \"Add New Leads\", 			\"subMenuId\": \"SMENUNELEAD\", 			\"url\": \"/leads/addLead\" 		}, 		{ 			\"isActive\": true, 			\"read\": true, 			\"write\": false, 			\"delete\": true, 			\"menuId\": \"POSPMENULEADS\", 			\"subMenu\": \"Viewing Leads\", 			\"subMenuId\": \"SMENUVIEWLEAD\", 			\"url\": \"/leads/viewLeads\" 		}, 		{ 			\"isActive\": true, 			\"read\": true, 			\"write\": false, 			\"delete\": true, 			\"menuId\": \"POSPMENULEADS\", 			\"subMenu\": \"Pending Payment\", 			\"subMenuId\": \"SMENUPAYPENDIN\", 			\"url\": \"/leads/payments\" 		}, 		{ 			\"isActive\": true, 			\"read\": true, 			\"write\": true, 			\"delete\": true, 			\"menuId\": \"POSPMENULEADS\", 			\"subMenu\": \"Pending Proposal\", 			\"subMenuId\": \"SMENUPROPPENDI\", 			\"url\": \"/leads/proposals\" 		}], 		\"url\": \"\" 	}, 	{ 		\"defaultScreenId\": \"POSPSHOME\", 		\"icon\": \"repeat.svg\", 		\"isActive\": true, 		\"read\": true, 		\"write\": false, 		\"delete\": true, 		\"menuId\": \"POSPMENURENEW\", 		\"menuName\": \"My Renewals\", 		\"url\": \"\" 	}] }]";
		try {
			ArrayNode screenConfig = (ArrayNode)obj.readTree(req);
			System.out.println("Query Response "+screenConfig);
			if(screenConfig.size() > 1){
				
				ArrayNode screenConfigResNode  = (ArrayNode) screenConfig.get(0).get(POSPServiceConstant.POSPMENUCONFIG);
				
				for(int i=1 ;i < screenConfig.size(); i++ ){
					System.out.println("Node : "+screenConfig.get(i).get(POSPServiceConstant.POSPMENUCONFIG));
					ArrayNode itrArraynode = (ArrayNode)screenConfig.get(i).get(POSPServiceConstant.POSPMENUCONFIG);
					for(JsonNode innerMenu : itrArraynode){
						for(JsonNode outerMenu : screenConfigResNode){
							if(outerMenu.get(POSPServiceConstant.MENUID).asText().equalsIgnoreCase(innerMenu.get(POSPServiceConstant.MENUID).asText())){
								if(innerMenu.get(POSPServiceConstant.ISACTIVE).asBoolean()){
									((ObjectNode)outerMenu).put(POSPServiceConstant.ISACTIVE, true);
								}
								if(innerMenu.get(POSPServiceConstant.READ).asBoolean()){
									((ObjectNode)outerMenu).put(POSPServiceConstant.READ, true);
								}
								if(innerMenu.get(POSPServiceConstant.WRITE).asBoolean()){
									((ObjectNode)outerMenu).put(POSPServiceConstant.WRITE, true);
								}
								if(innerMenu.get(POSPServiceConstant.DELETE).asBoolean()){
									((ObjectNode)outerMenu).put(POSPServiceConstant.DELETE, true);
								}
								if(outerMenu.has(POSPServiceConstant.SUBMENU)){
									ArrayNode outerSubmenu = (ArrayNode)outerMenu.get(POSPServiceConstant.SUBMENU);
									for(JsonNode menuOuterSubmenu : outerSubmenu){
										if(innerMenu.has(POSPServiceConstant.SUBMENU)){
											for(JsonNode innerSubmenu : innerMenu.get(POSPServiceConstant.SUBMENU)){
												if(innerSubmenu.get(POSPServiceConstant.SUBMENUID).asText().equalsIgnoreCase(menuOuterSubmenu.get(POSPServiceConstant.SUBMENUID).asText())){
													if(innerSubmenu.get(POSPServiceConstant.ISACTIVE).asBoolean()){
														((ObjectNode)menuOuterSubmenu).put(POSPServiceConstant.ISACTIVE, true);
													}
													if(innerSubmenu.get(POSPServiceConstant.READ).asBoolean()){
														((ObjectNode)menuOuterSubmenu).put(POSPServiceConstant.READ, true);
													}
													if(innerSubmenu.get(POSPServiceConstant.WRITE).asBoolean()){
														((ObjectNode)menuOuterSubmenu).put(POSPServiceConstant.WRITE, true);
													}
													if(innerSubmenu.get(POSPServiceConstant.DELETE).asBoolean()){
														((ObjectNode)menuOuterSubmenu).put(POSPServiceConstant.DELETE, true);
													}
													break;
												}
											}
										}// innerMenu if condition END
									}
								}// outerMenu if condition END
							}
						}// outerMenu iteration for END
					}	
				 }//ScreenConfig response iteration for end 
				System.out.println("output : "+screenConfigResNode);
				}else if(screenConfig.size() ==1 ){
				
			}else{
				
			}
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	
	
	
}
