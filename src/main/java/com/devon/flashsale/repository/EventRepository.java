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

	@Modifying
	@Query(incrementQuery)
	int incrementSeats(@Param("eventId") Long eventId, @Param("quantity") Integer quantity);
	
	@Modifying
	@Query(decrementQuery)
	int decrementSeatsIfAvailable(@Param("eventId") Long eventId, @Param("quantity") Integer quantity);
	
	@Modifying
	@Query(statusUpdateQuery)
	int updateEventStatus(@Param("eventId") Long eventId, @Param("status") EventStatus status);
	
}
