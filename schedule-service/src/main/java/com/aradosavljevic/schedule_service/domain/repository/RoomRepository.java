package com.aradosavljevic.schedule_service.domain.repository;


import com.aradosavljevic.schedule_service.domain.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findRoomByName(String name);

    Optional<Room> findRoomById(Long id);

    Page<Room> findRoomByBookable(Boolean bookable, Pageable pageable);

    Page<Room> findAll(Pageable pageable);

}
