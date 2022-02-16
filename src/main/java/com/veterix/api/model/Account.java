package com.veterix.api.model;


import java.util.List;
import java.util.UUID;

public record Account (UUID id, String name,String email,String phoneNumber,
                       String address, List<Pet> pets,List<PaymentInformation> paymentInformation){
}
