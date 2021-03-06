package uk.gov.dhsc.htbhf.claimant.entity;

import lombok.*;
import uk.gov.dhsc.htbhf.claimant.message.MessageStatus;
import uk.gov.dhsc.htbhf.claimant.message.MessageType;

import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "message_queue")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = true)
public class Message extends BaseEntity {

    @NotNull
    @Column(name = "created_timestamp")
    @Builder.Default
    private LocalDateTime createdTimestamp = LocalDateTime.now();

    @NotNull
    @Column(name = "process_after")
    private LocalDateTime processAfter;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @NotNull
    @Column(name = "message_payload")
    private String messagePayload;

    @Column(name = "delivery_count")
    private int deliveryCount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
}
