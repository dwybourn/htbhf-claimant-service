package uk.gov.dhsc.htbhf.claimant.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.dhsc.htbhf.claimant.message.MessageProcessor;
import uk.gov.dhsc.htbhf.claimant.message.MessageType;
import uk.gov.dhsc.htbhf.requestcontext.aop.NewRequestContextWithSessionId;

/**
 * Component responsible for scheduling the processing of messages.
 * Each MessageType has its own cron schedule - see https://stackoverflow.com/a/26147143 for examples.
 * Each messageType also has its own minimum and maximum lock period. For a definition of the ISO-8601 duration format, see
 * https://www.digi.com/resources/documentation/digidocs/90001437-13/reference/r_iso_8601_duration_format.htm.
 * Note that the {@link MessageProcessor} also specifies a limit on the number of messages to process
 * in one go.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@SuppressWarnings("PMD.TooManyMethods")
public class MessageProcessorScheduler {

    private static final String DEFAULT_SCHEDULE = "${message-processor.default-schedule}";
    private static final String MAX_LOCK_TIME = "${message-processor.default-max-lock-time}";
    private static final String MIN_LOCK_TIME = "${message-processor.default-min-lock-time}";
    // every 30 seconds, offset from other schedules by 5 seconds to prevent clashes with other scheduled processes
    private static final String OFFSET_SCHEDULE = "${message-processor.offset-schedule}";

    private final MessageProcessor messageProcessor;

    @Scheduled(cron = DEFAULT_SCHEDULE)
    @SchedulerLock(
            name = "Process ADDITIONAL_PREGNANCY_PAYMENT messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:ADDITIONAL_PREGNANCY_PAYMENT")
    public void processAdditionalPregnancyPaymentMessages() {
        messageProcessor.processMessagesOfType(MessageType.ADDITIONAL_PREGNANCY_PAYMENT);
    }

    @Scheduled(cron = DEFAULT_SCHEDULE)
    @SchedulerLock(
            name = "Process SEND_EMAIL messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:SEND_EMAIL")
    public void processSendEmailMessages() {
        messageProcessor.processMessagesOfType(MessageType.SEND_EMAIL);
    }

    @Scheduled(cron = DEFAULT_SCHEDULE)
    @SchedulerLock(
            name = "Process SEND_LETTER messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:SEND_LETTER")
    public void processSendLetterMessages() {
        messageProcessor.processMessagesOfType(MessageType.SEND_LETTER);
    }

    @Scheduled(cron = DEFAULT_SCHEDULE)
    @SchedulerLock(
            name = "Process REQUEST_NEW_CARD messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:REQUEST_NEW_CARD")
    public void processRequestNewCardMessages() {
        messageProcessor.processMessagesOfType(MessageType.REQUEST_NEW_CARD);
    }

    @Scheduled(cron = DEFAULT_SCHEDULE)
    @SchedulerLock(
            name = "Process COMPLETE_NEW_CARD_PROCESS messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:COMPLETE_NEW_CARD_PROCESS")
    public void processCompleteNewCardMessages() {
        messageProcessor.processMessagesOfType(MessageType.COMPLETE_NEW_CARD_PROCESS);
    }

    @Scheduled(cron = DEFAULT_SCHEDULE)
    @SchedulerLock(
            name = "Process DETERMINE_ENTITLEMENT messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:DETERMINE_ENTITLEMENT")
    public void processDetermineEntitlementMessages() {
        messageProcessor.processMessagesOfType(MessageType.DETERMINE_ENTITLEMENT);
    }

    @Scheduled(cron = DEFAULT_SCHEDULE)
    @SchedulerLock(
            name = "Process REQUEST_PAYMENT messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:REQUEST_PAYMENT")
    public void processRequestPaymentMessages() {
        messageProcessor.processMessagesOfType(MessageType.REQUEST_PAYMENT);
    }

    @Scheduled(cron = DEFAULT_SCHEDULE)
    @SchedulerLock(
            name = "Process COMPLETE_PAYMENT messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:COMPLETE_PAYMENT")
    public void processCompletePaymentMessages() {
        messageProcessor.processMessagesOfType(MessageType.COMPLETE_PAYMENT);
    }

    @Scheduled(cron = OFFSET_SCHEDULE)
    @SchedulerLock(
            name = "Process REPORT_CLAIM messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:REPORT_CLAIM")
    public void processReportClaimMessages() {
        messageProcessor.processMessagesOfType(MessageType.REPORT_CLAIM);
    }

    @Scheduled(cron = OFFSET_SCHEDULE)
    @SchedulerLock(
            name = "Process REPORT_PAYMENT messages",
            lockAtLeastForString = MIN_LOCK_TIME,
            lockAtMostForString = MAX_LOCK_TIME)
    @NewRequestContextWithSessionId(sessionId = "MessageProcessor:REPORT_PAYMENT")
    public void processReportPaymentMessages() {
        messageProcessor.processMessagesOfType(MessageType.REPORT_PAYMENT);
    }
}
