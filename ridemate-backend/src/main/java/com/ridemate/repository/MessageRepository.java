package com.ridemate.repository;

import com.ridemate.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** All messages for a booking chat, chronological */
    List<Message> findByBookingIdOrderBySentAtAsc(Long bookingId);

    /** All messages for a parcel chat, chronological */
    List<Message> findByParcelIdOrderBySentAtAsc(Long parcelId);

    /** Count unread messages for a booking not sent by a given user */
    long countByBookingIdAndReadFalseAndSenderIdNot(Long bookingId, Long senderId);

    /** Count unread messages for a parcel chat not sent by a given user */
    long countByParcelIdAndReadFalseAndSenderIdNot(Long parcelId, Long senderId);

    /** Mark all messages in a booking as read (called when recipient opens chat) */
    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.booking.id = :bookingId AND m.sender.id != :userId AND m.read = false")
    void markBookingMessagesRead(Long bookingId, Long userId);

    /** Mark all messages in a parcel chat as read */
    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.parcelId = :parcelId AND m.sender.id != :userId AND m.read = false")
    void markParcelMessagesRead(Long parcelId, Long userId);
}
