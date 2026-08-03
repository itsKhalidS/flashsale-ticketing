package com.devon.flashsale.validation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.exceptions.FlashSaleAppException;
import com.devon.flashsale.exceptions.ValidationException;

/**
 * Helper class to help in Event Validation
 */
public class EventValidator {

	/**
	 * Validates an incoming Event Object
	 * @param event : The event object
	 * @return The List of errors
	 */
	public static List<FlashSaleAppException> validateNewEvent(Event event){
		
		List<FlashSaleAppException> exceptions = new ArrayList<>();
		LocalDateTime currentDateTime = LocalDateTime.now();
		
		if(StringUtils.isEmpty(event.getEventName())){
			exceptions.add(new ValidationException("Event name is required"));
		}
		
		if(StringUtils.isEmpty(event.getVenue())){
			exceptions.add(new ValidationException("Event venue is required"));
		}
		
		if(event.getTotalSeats() == null) {
			exceptions.add(new ValidationException("Total seats is required"));
		}else if(event.getTotalSeats() < 0){
			exceptions.add(new ValidationException("Value of seats cannot be negative"));
		}else {
			event.setRemainingSeats(event.getTotalSeats());
		}
		
		if(event.getPrice().compareTo(BigDecimal.ZERO) < 0 ){
			exceptions.add(new ValidationException("Price cannot be negative"));
		}
		
		if(event.getEventDate()==null) {
			exceptions.add(new ValidationException("Event Date is required"));
		}else {
			if(event.getEventDate().isBefore(event.getStartTime())) {
				exceptions.add(new ValidationException("Event Date should be after Start time"));
			}
		}
		
		if(event.getStartTime()==null) {
			exceptions.add(new ValidationException("Start time is required"));
		}else if(event.getEndTime()==null) {
			exceptions.add(new ValidationException("End time is required"));
		}else {
			if(event.getStartTime().isEqual(event.getEndTime()) || event.getStartTime().isAfter(event.getEndTime())) {
				exceptions.add(new ValidationException("Start time should be before End time"));
			}
			if(currentDateTime.isAfter(event.getEndTime())) {
				exceptions.add(new ValidationException("Cannot add an event which has already ended"));
			}			
			if((currentDateTime.isEqual(event.getStartTime()) || currentDateTime.isAfter(event.getStartTime())) && 
					(currentDateTime.isBefore(event.getEndTime()) || currentDateTime.isEqual(event.getEndTime()))) {
				event.setStatus(EventStatus.ACTIVE);
			}else {
				event.setStatus(EventStatus.INACTIVE);			
			}
			
		}
		
		return exceptions;
	}
}
