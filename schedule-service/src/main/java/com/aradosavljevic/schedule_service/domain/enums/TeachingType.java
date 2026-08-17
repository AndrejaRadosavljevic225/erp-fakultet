package com.aradosavljevic.schedule_service.domain.enums;

/**
 * Tip nastavne aktivnosti. Termini ovih tipova doprinose fondu casova profesora.
 * Obicna rezervacija prostorije (nenastavna) ima teachingType = null.
 */
public enum TeachingType {
    REGULAR,     // redovni cas
    EXTRA,       // dodatni cas
    MENTORSHIP,  // mentorstvo
    OTHER        // ostalo (nastavno)
}
