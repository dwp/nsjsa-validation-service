package uk.gov.dwp.jsa.validation.service.models.http;

import java.time.LocalDateTime;

public class CurrentStatusDto {

    private StatusDto bookingStatus;

    private StatusDto pushStatus;

    private LocalDateTime createdTimestamp;

    public CurrentStatusDto() {
    }

    public StatusDto getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(final StatusDto bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public StatusDto getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(final StatusDto pushStatus) {
        this.pushStatus = pushStatus;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(final LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }


    public static class StatusDto {
        private String status;
        private String substatus;

        public StatusDto(final String status, final String substatus) {
            this.status = status;
            this.substatus = substatus;
        }

        public void setStatus(final String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setSubstatus(final String substatus) {
            this.substatus = substatus;

        }

        public String getSubstatus() {
            return substatus;
        }
    }

}


