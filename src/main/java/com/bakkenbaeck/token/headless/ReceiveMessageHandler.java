package com.bakkenbaeck.token.headless;

import com.bakkenbaeck.token.headless.signal.Base64;
import com.bakkenbaeck.token.headless.signal.ContactInfo;
import com.bakkenbaeck.token.headless.signal.GroupInfo;
import com.bakkenbaeck.token.headless.signal.Manager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.whispersystems.signalservice.api.messages.*;
import org.whispersystems.signalservice.api.messages.multidevice.BlockedListMessage;
import org.whispersystems.signalservice.api.messages.multidevice.ReadMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SentTranscriptMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class ReceiveMessageHandler implements Manager.ReceiveMessageHandler {
    private final Manager m;
    private final JedisPool jedisPool;
    private final String address;
    private static final TimeZone tzUTC = TimeZone.getTimeZone("UTC");

    public ReceiveMessageHandler(Manager m, JedisPool jedisPool, String address) {
        this.m = m;
        this.jedisPool = jedisPool;
        this.address = address;
    }

    @Override
    public void handleMessage(SignalServiceEnvelope envelope, SignalServiceContent content, Throwable exception) {
        SignalServiceAddress source = envelope.getSourceAddress();
        ContactInfo sourceContact = m.getContact(source.getNumber());
        System.out.println(String.format("Envelope from: %s (device: %d)", (sourceContact == null ? "" : "“" + sourceContact.name + "” ") + source.getNumber(), envelope.getSourceDevice()));
        if (source.getRelay().isPresent()) {
            System.out.println("Relayed by: " + source.getRelay().get());
        }



        if (envelope.isReceipt()) {
            //noop
        } else if (envelope.isSignalMessage() | envelope.isPreKeySignalMessage()) {
            if (exception != null) {
                if (exception instanceof org.whispersystems.libsignal.UntrustedIdentityException) {
                    org.whispersystems.libsignal.UntrustedIdentityException e = (org.whispersystems.libsignal.UntrustedIdentityException) exception;
                    System.out.println("The user’s key is untrusted, either the user has reinstalled Signal or a third party sent this message.");
                    System.out.println("Use 'signal-cli -u " + m.getUsername() + " listIdentities -n " + e.getName() + "', verify the key and run 'signal-cli -u " + m.getUsername() + " trust -v \"FINGER_PRINT\" " + e.getName() + "' to mark it as trusted");
                    System.out.println("If you don't care about security, use 'signal-cli -u " + m.getUsername() + " trust -a " + e.getName() + "' to trust it without verification");
                } else {
                    System.out.println("Exception: " + exception.getMessage() + " (" + exception.getClass().getSimpleName() + ")");
                }
            }
            if (content == null) {
                System.out.println("Failed to decrypt message.");
            } else {

                SignalWrappedSOFA wrappedSOFA = new SignalWrappedSOFA();
                wrappedSOFA.setSender(envelope.getSourceAddress().getNumber());
                wrappedSOFA.setRecipient(address);

                if (content.getDataMessage().isPresent()) {
                    SignalServiceDataMessage message = content.getDataMessage().get();
                    handleSignalServiceDataMessage(message, wrappedSOFA);
                }
                if (content.getSyncMessage().isPresent()) {
                    //System.out.println("Received a sync message");
                    SignalServiceSyncMessage syncMessage = content.getSyncMessage().get();

                    if (syncMessage.getContacts().isPresent()) {
                        //System.out.println("Received sync contacts");
                        printAttachment(syncMessage.getContacts().get());
                    }
                    if (syncMessage.getGroups().isPresent()) {
                        //System.out.println("Received sync groups");
                        printAttachment(syncMessage.getGroups().get());
                    }
                    if (syncMessage.getRead().isPresent()) {
                        //System.out.println("Received sync read messages list");
                        for (ReadMessage rm : syncMessage.getRead().get()) {
                            ContactInfo fromContact = m.getContact(rm.getSender());
                            //System.out.println("From: " + (fromContact == null ? "" : "“" + fromContact.name + "” ") + rm.getSender() + " Message timestamp: " + formatTimestamp(rm.getTimestamp()));
                        }
                    }
                    if (syncMessage.getRequest().isPresent()) {
                        //System.out.println("Received sync request");
                        if (syncMessage.getRequest().get().isContactsRequest()) {
                            //System.out.println(" - contacts request");
                        }
                        if (syncMessage.getRequest().get().isGroupsRequest()) {
                            //System.out.println(" - groups request");
                        }
                    }
                    if (syncMessage.getSent().isPresent()) {
                        //System.out.println("Received sync sent message");
                        final SentTranscriptMessage sentTranscriptMessage = syncMessage.getSent().get();
                        String to;
                        if (sentTranscriptMessage.getDestination().isPresent()) {
                            String dest = sentTranscriptMessage.getDestination().get();
                            ContactInfo destContact = m.getContact(dest);
                            to = (destContact == null ? "" : "“" + destContact.name + "” ") + dest;
                        } else {
                            to = "Unknown";
                        }
                        //System.out.println("To: " + to + " , Message timestamp: " + formatTimestamp(sentTranscriptMessage.getTimestamp()));
                        if (sentTranscriptMessage.getExpirationStartTimestamp() > 0) {
                            //System.out.println("Expiration started at: " + formatTimestamp(sentTranscriptMessage.getExpirationStartTimestamp()));
                        }
                        SignalServiceDataMessage message = sentTranscriptMessage.getMessage();
                        handleSignalServiceDataMessage(message, wrappedSOFA);
                    }
                    if (syncMessage.getBlockedList().isPresent()) {
                        //System.out.println("Received sync message with block list");
                        //System.out.println("Blocked numbers:");
                        final BlockedListMessage blockedList = syncMessage.getBlockedList().get();
                        for (String number : blockedList.getNumbers()) {
                            //System.out.println(" - " + number);
                        }
                    }
                }
            }
        } else {
            //System.out.println("Unknown message received.");
        }
        //System.out.println();
    }

    private void handleSignalServiceDataMessage(SignalServiceDataMessage message, SignalWrappedSOFA wrappedSOFA) {
        //System.out.println("Message timestamp: " + formatTimestamp(message.getTimestamp()));

        if (message.getBody().isPresent()) {
            //System.out.println("Body: " + message.getBody().get());
            wrappedSOFA.setSofa(message.getBody().get());
            ObjectMapper mapper = new ObjectMapper();
            String wrappedMessage;
            try {
                wrappedMessage = mapper.writeValueAsString(wrappedSOFA);
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.publish(address, wrappedMessage);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if (message.getGroupInfo().isPresent()) {
            SignalServiceGroup groupInfo = message.getGroupInfo().get();
            //System.out.println("Group info:");
            //System.out.println("  Id: " + Base64.encodeBytes(groupInfo.getGroupId()));
            if (groupInfo.getType() == SignalServiceGroup.Type.UPDATE && groupInfo.getName().isPresent()) {
                //System.out.println("  Name: " + groupInfo.getName().get());
            } else {
                GroupInfo group = m.getGroup(groupInfo.getGroupId());
                if (group != null) {
                    //System.out.println("  Name: " + group.name);
                } else {
                    //System.out.println("  Name: <Unknown group>");
                }
            }
            System.out.println("  Type: " + groupInfo.getType());
            if (groupInfo.getMembers().isPresent()) {
                for (String member : groupInfo.getMembers().get()) {
                    //System.out.println("  Member: " + member);
                }
            }
            if (groupInfo.getAvatar().isPresent()) {
                //System.out.println("  Avatar:");
                printAttachment(groupInfo.getAvatar().get());
            }
        }
        if (message.isEndSession()) {
            //System.out.println("Is end session");
        }
        if (message.isExpirationUpdate()) {
            //System.out.println("Is Expiration update: " + message.isExpirationUpdate());
        }
        if (message.getExpiresInSeconds() > 0) {
            //System.out.println("Expires in: " + message.getExpiresInSeconds() + " seconds");
        }

        if (message.getAttachments().isPresent()) {
            //System.out.println("Attachments: ");
            for (SignalServiceAttachment attachment : message.getAttachments().get()) {
                printAttachment(attachment);
            }
        }
    }

    private void printAttachment(SignalServiceAttachment attachment) {
        System.out.println("- " + attachment.getContentType() + " (" + (attachment.isPointer() ? "Pointer" : "") + (attachment.isStream() ? "Stream" : "") + ")");
        if (attachment.isPointer()) {
            final SignalServiceAttachmentPointer pointer = attachment.asPointer();
            System.out.println("  Id: " + pointer.getId() + " Key length: " + pointer.getKey().length + (pointer.getRelay().isPresent() ? " Relay: " + pointer.getRelay().get() : ""));
            System.out.println("  Size: " + (pointer.getSize().isPresent() ? pointer.getSize().get() + " bytes" : "<unavailable>") + (pointer.getPreview().isPresent() ? " (Preview is available: " + pointer.getPreview().get().length + " bytes)" : ""));
            File file = m.getAttachmentFile(pointer.getId());
            if (file.exists()) {
                System.out.println("  Stored plaintext in: " + file);
            }
        }
    }

    private static String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tzUTC);
        return timestamp + " (" + df.format(date) + ")";
    }
}