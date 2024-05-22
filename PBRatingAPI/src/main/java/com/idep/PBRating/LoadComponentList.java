 package com.idep.PBRating;
 
 import com.idep.PBRating.Car.CalculateCarRating;

 public class LoadComponentList
 {
   public static void main(String[] args) {
     System.out.println("Execution Started : ");
     
     String requ = "{\"city\":\"PUNE\", \"make\":\"Toyota\", \"state\":\"Maharashtra\"}";
     
     CalculateCarRating cc = new CalculateCarRating();
     
     System.out.println(cc.generateCarRating(requ));
   }
 }