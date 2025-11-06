package com.devon.flashsale.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devon.flashsale.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
