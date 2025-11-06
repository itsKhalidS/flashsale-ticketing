package com.devon.flashsale.validation;

import java.util.ArrayList;
import java.util.List;

import com.devon.flashsale.exceptions.FlashSaleAppException;
import com.devon.flashsale.exceptions.ResourceNotFoundException;
import com.devon.flashsale.service.EventService;

public class OrderValidator {

	private static EventService eventService;
	
	public OrderValidator(EventService eventService) {
		OrderValidator.eventService = eventService;
	}
	
	public static List<FlashSaleAppException> validateNewOrder(Long eventId){
		List<FlashSaleAppException> exceptions = new ArrayList<>();
		try{
			eventService.getEventById(eventId);
		}catch(ResourceNotFoundException exp) {
			exceptions.add(exp);
		}
		return exceptions;
	}
}
