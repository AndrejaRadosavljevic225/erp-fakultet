package com.aradosavljevic.schedule_service.domain.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
//    TODO: Dodatni podaci
    private Long capacity;


    private Integer computerCount;
    private boolean bookable;




}

/*
room_id uuid [pk]/
code varchar [unique]/
name varchar
building varchar
floor int
room_number varchar
capacity int/
room_type varchar/
computer_count int/
is_bookable bool
is_active bool
created_at timestamp

 */