package com.devon.flashsale.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devon.flashsale.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

}
