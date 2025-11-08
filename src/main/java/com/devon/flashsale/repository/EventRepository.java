package com.devon.flashsale.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	final String incrementQuery = " UPDATE "+
			                      "   Event evt "+
			                      " SET evt.remainingSeats = evt.remainingSeats + :quantity "+
			                      "   WHERE evt.eventId = :eventId ";
	
	final String decrementQuery = " UPDATE "+
                                  "   Event evt "+
                                  " SET evt.remainingSeats = evt.remainingSeats - :quantity "+
                                  "   WHERE evt.eventId = :eventId "+
                                  "         AND evt.remainingSeats >= :quantity ";
	
	final String statusUpdateQuery = " UPDATE "+
							         "   Event evt "+
							         " SET evt.status = :status "+
							         "   WHERE evt.eventId = :eventId ";

	/** Increase the seats remaining for the particular event
	 * @param eventId : The eventId of the event
	 * @param quantity : The number of seats to increase
	 * @return The number of rows effected
	 */
	@Modifying
	@Query(incrementQuery)
	int incrementSeats(@Param("eventId") Long eventId, @Param("quantity") Integer quantity);
	
	/**
	 * Decrease the remaining seats for the particular event
	 * @param eventId : The eventId of the event
	 * @param quantity : The number of seats to decrease
	 * @return The number of rows effected
	 */
	@Modifying
	@Query(decrementQuery)
	int decrementSeatsIfAvailable(@Param("eventId") Long eventId, @Param("quantity") Integer quantity);
	
	/** Update the status of a particular event
	 * @param eventId : The eventId of the event
	 * @param status : The updated status
	 * @return The number of rows effected
	 */
	@Modifying
	@Query(statusUpdateQuery)
	int updateEventStatus(@Param("eventId") Long eventId, @Param("status") EventStatus status);
	
}
