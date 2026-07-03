package com.devon.flashsale.validation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.exceptions.FileStorageException;
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
		
		if(event.getTotalSeats() == null) {
			exceptions.add(new ValidationException("Total seats is required"));
		}else if(event.getTotalSeats() < 0){
			exceptions.add(new ValidationException("Value of seats cannot be negative"));
		}else {
			event.setRemainingSeats(event.getTotalSeats());
		}
		
		if(event.getStartTime()==null) {
			exceptions.add(new ValidationException("Event start time is required"));
		}else if(event.getEndTime()==null) {
			exceptions.add(new ValidationException("Event end time is required"));
		}else {
			if(event.getStartTime().isEqual(event.getEndTime()) || event.getStartTime().isAfter(event.getEndTime())) {
				exceptions.add(new ValidationException("Event start time should be before Event end time"));
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
	
	public static List<FileStorageException> validateEventImage(MultipartFile file){
		List<FileStorageException> exceptions = new ArrayList<>();
		if (file == null || file.isEmpty()) {
            exceptions.add(new FileStorageException("Image cannot be empty"));
        }
		return exceptions;
	}
}
