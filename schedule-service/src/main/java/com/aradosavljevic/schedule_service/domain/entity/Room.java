package com.aradosavljevic.schedule_service.domain.entity;

import jakarta.persistence.*;
import rs.raf.sk.api.Prostorija;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Entity
public class Room extends Prostorija {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long capacity;


    private Integer computerCount;
    private boolean bookable;


    public Room(String naziv, Map<String, Object> atributi) {
        super(naziv, atributi);
    }

    public Room(String naziv) {
        super(naziv);
    }

    public Room() {
        super("Room");
    }



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