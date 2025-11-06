package com.devon.flashsale.validation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.exceptions.FlashSaleAppException;
import com.devon.flashsale.exceptions.ValidationException;

public class EventValidator {

	public static List<FlashSaleAppException> validateNewEvent(Event event){
		List<FlashSaleAppException> exceptions = new ArrayList<>();
		
		if(StringUtils.isEmpty(event.getEventName())){
			exceptions.add(new ValidationException("Event name is required"));
		}
		
		if(event.getTotalSeats() == null) {
			exceptions.add(new ValidationException("Total seats is required"));
		}else if(event.getTotalSeats() < 0){
			exceptions.add(new ValidationException("Value of seats cannot be negative"));
		}else {
			if(event.getRemainingSeats() != null && event.getRemainingSeats().compareTo(event.getTotalSeats()) > 1) {
				exceptions.add(new ValidationException("Remaining seats cannot be greater than Total seats"));
			}
			if(event.getRemainingSeats() != null && event.getRemainingSeats() < 0) {
				exceptions.add(new ValidationException("Remaining seats cannot be negative"));
			}
			event.setRemainingSeats(event.getTotalSeats());
		}
		
		if(event.getStartTime()==null) {
			exceptions.add(new ValidationException("Event start time is required"));
		}
		
		if(event.getEndTime()==null) {
			exceptions.add(new ValidationException("Event end time is required"));
		}
		
		if(event.getStartTime().isEqual(event.getEndTime()) || event.getStartTime().isAfter(event.getEndTime())) {
			exceptions.add(new ValidationException("Event start time should be before Event end time"));
		}
		
		LocalDateTime currentDateTime = LocalDateTime.now();
		if((currentDateTime.isEqual(event.getStartTime()) || currentDateTime.isAfter(event.getStartTime())) && 
				(currentDateTime.isBefore(event.getEndTime()) || currentDateTime.isEqual(event.getStartTime()))) {
			event.setStatus(EventStatus.ACTIVE);
		}else {
			event.setStatus(EventStatus.CLOSED);			
		}
		
		return exceptions;
	}
}
