package com.ridemate.service;

import com.ridemate.dto.request.ChatMessageRequest;
import com.ridemate.dto.response.MessageResponse;
import com.ridemate.entity.Booking;
import com.ridemate.entity.Message;
import com.ridemate.entity.Parcel;
import com.ridemate.entity.User;
import com.ridemate.exception.AppException;
import com.ridemate.repository.BookingRepository;
import com.ridemate.repository.MessageRepository;
import com.ridemate.repository.ParcelRepository;
import com.ridemate.repository.UserRepository;
import com.ridemate.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles in-booking chat between passenger and driver.
 *
 * Message flow:
 *   1. Client sends to   /app/chat.booking.{bookingId}
 *   2. ChatController saves the message via this service
 *   3. Service broadcasts to  /topic/booking.{bookingId}
 *      (both parties are subscribed to that topic)
 *
 * Read-receipts:
 *   When a participant opens the chat view they call markRead(),
 *   which marks all messages sent by the OTHER party as read.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final MessageRepository     messageRepository;
    private final BookingRepository     bookingRepository;
    private final ParcelRepository      parcelRepository;
    private final UserRepository        userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper            userMapper;

    // ── Send a message ────────────────────────────────────────────────

    @Transactional
    public MessageResponse sendMessage(String senderEmail,
                                       Long bookingId,
                                       ChatMessageRequest req) {

        User    sender  = findUser(senderEmail);
        Booking booking = findBooking(bookingId);

        // Only the passenger or the driver may chat
        boolean isPassenger = booking.getPassenger().getId().equals(sender.getId());
        boolean isDriver    = booking.getRide().getDriver().getId().equals(sender.getId());

        if (!isPassenger && !isDriver) {
            throw new AppException("You are not a participant in this booking.", HttpStatus.FORBIDDEN);
        }

        Message msg = Message.builder()
            .booking(booking)
            .sender(sender)
            .text(req.getText().trim())
            .read(false)
            .build();

        Message saved = messageRepository.save(msg);
        MessageResponse response = toResponse(saved);

        // Push to both parties via WebSocket
        messagingTemplate.convertAndSend(
            "/topic/booking." + bookingId, response);

        log.debug("Chat [booking={}] {}: {}", bookingId, sender.getName(), req.getText());
        return response;
    }

    // ── Load history ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MessageResponse> getHistory(String viewerEmail, Long bookingId) {
        User    viewer  = findUser(viewerEmail);
        Booking booking = findBooking(bookingId);

        boolean isPassenger = booking.getPassenger().getId().equals(viewer.getId());
        boolean isDriver    = booking.getRide().getDriver().getId().equals(viewer.getId());

        if (!isPassenger && !isDriver) {
            throw new AppException("Access denied.", HttpStatus.FORBIDDEN);
        }

        return messageRepository.findByBookingIdOrderBySentAtAsc(bookingId)
            .stream().map(this::toResponse).toList();
    }

    // ── Mark messages as read ─────────────────────────────────────────

    @Transactional
    public void markRead(String viewerEmail, Long bookingId) {
        User    viewer  = findUser(viewerEmail);
        Booking booking = findBooking(bookingId);

        boolean isPassenger = booking.getPassenger().getId().equals(viewer.getId());
        boolean isDriver    = booking.getRide().getDriver().getId().equals(viewer.getId());

        if (!isPassenger && !isDriver) {
            throw new AppException("Access denied.", HttpStatus.FORBIDDEN);
        }

        // Mark messages sent by the OTHER party as read
        messageRepository.markBookingMessagesRead(bookingId, viewer.getId());
    }

    // ── Mapper ────────────────────────────────────────────────────────

    private MessageResponse toResponse(Message m) {
        User s = m.getSender();
        return MessageResponse.builder()
            .id(m.getId())
            .bookingId(m.getBooking() != null ? m.getBooking().getId() : null)
            .parcelId(m.getParcelId())   // fix B2: was always null
            .senderId(s.getId())
            .senderName(s.getName())
            .senderPhotoUrl(userMapper.toUrl(s.getPhotoPath()))
            .text(m.getText())
            .read(m.isRead())
            .sentAt(m.getSentAt())
            .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }

    // ── Parcel chat ───────────────────────────────────────────────────

    @Transactional
    public MessageResponse sendParcelMessage(String senderEmail,
                                              Long parcelId,
                                              ChatMessageRequest req) {
        User   sender = findUser(senderEmail);
        Parcel parcel = findParcel(parcelId);

        boolean isSender = parcel.getSender().getId().equals(sender.getId());
        boolean isDriver = parcel.getRide().getDriver().getId().equals(sender.getId());

        if (!isSender && !isDriver) {
            throw new AppException("You are not a participant in this parcel.", HttpStatus.FORBIDDEN);
        }

        Message msg = Message.builder()
            .parcelId(parcelId)
            .sender(sender)
            .text(req.getText().trim())
            .read(false)
            .build();

        Message saved = messageRepository.save(msg);
        MessageResponse response = toResponse(saved);

        messagingTemplate.convertAndSend("/topic/parcel." + parcelId, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getParcelHistory(String viewerEmail, Long parcelId) {
        User   viewer = findUser(viewerEmail);
        Parcel parcel = findParcel(parcelId);

        boolean isSender = parcel.getSender().getId().equals(viewer.getId());
        boolean isDriver = parcel.getRide().getDriver().getId().equals(viewer.getId());
        if (!isSender && !isDriver) throw new AppException("Access denied.", HttpStatus.FORBIDDEN);

        return messageRepository.findByParcelIdOrderBySentAtAsc(parcelId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void markParcelRead(String viewerEmail, Long parcelId) {
        User   viewer = findUser(viewerEmail);
        Parcel parcel = findParcel(parcelId);

        boolean isSender = parcel.getSender().getId().equals(viewer.getId());
        boolean isDriver = parcel.getRide().getDriver().getId().equals(viewer.getId());
        if (!isSender && !isDriver) throw new AppException("Access denied.", HttpStatus.FORBIDDEN);

        messageRepository.markParcelMessagesRead(parcelId, viewer.getId());
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new AppException("Booking not found.", HttpStatus.NOT_FOUND));
    }

    private Parcel findParcel(Long id) {
        return parcelRepository.findById(id)
            .orElseThrow(() -> new AppException("Parcel not found.", HttpStatus.NOT_FOUND));
    }
}
