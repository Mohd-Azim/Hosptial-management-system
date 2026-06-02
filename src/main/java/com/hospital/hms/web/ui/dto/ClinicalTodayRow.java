package com.hospital.hms.web.ui.dto;

import com.hospital.hms.domain.Appointment;
import com.hospital.hms.domain.Visit;
import com.hospital.hms.domain.enums.AppointmentStatus;
import com.hospital.hms.domain.enums.VisitStatus;

/** One row on clinical “today” screen with workflow phase. */
public record ClinicalTodayRow(Appointment appointment, Visit visit) {

    public String phase() {
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return "DONE";
        }
        if (visit == null) {
            return "CHECKIN";
        }
        if (visit.getStatus() == VisitStatus.CHECKED_IN) {
            return "PRESCRIBE";
        }
        return "OTHER";
    }
}
