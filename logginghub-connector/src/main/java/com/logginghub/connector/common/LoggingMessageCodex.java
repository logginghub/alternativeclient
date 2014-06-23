package com.logginghub.connector.common;

import java.io.EOFException;
import java.io.Serializable;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.EnumSet;

import com.logginghub.connector.common.messages.AggregatedPatternData;
import com.logginghub.connector.common.messages.AggregationKey;
import com.logginghub.connector.common.messages.ChannelMessage;
import com.logginghub.connector.common.messages.ChannelSubscriptionRequestMessage;
import com.logginghub.connector.common.messages.ChannelSubscriptionResponseMessage;
import com.logginghub.connector.common.messages.EventSubscriptionRequestMessage;
import com.logginghub.connector.common.messages.EventSubscriptionResponseMessage;
import com.logginghub.connector.common.messages.FilterRequestMessage;
import com.logginghub.connector.common.messages.HistoricalDataRequest;
import com.logginghub.connector.common.messages.HistoricalDataResponse;
import com.logginghub.connector.common.messages.HistoricalIndexElement;
import com.logginghub.connector.common.messages.HistoricalIndexRequest;
import com.logginghub.connector.common.messages.HistoricalIndexResponse;
import com.logginghub.connector.common.messages.LogEventMessage;
import com.logginghub.connector.common.messages.SubscriptionRequestMessage;
import com.logginghub.connector.common.messages.SubscriptionResponseMessage;
import com.logginghub.connector.common.messages.UnsubscriptionRequestMessage;
import com.logginghub.connector.common.messages.UnsubscriptionResponseMessage;
import com.logginghub.connector.common.serialisation.CompressedBlock;
import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofConfiguration;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofExpandingBufferSerialiser;
import com.logginghub.sof.SofPartialDecodeException;
import com.logginghub.sof.SofUnknownTypeException;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.DataElement;
import com.logginghub.utils.DataStructure;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.HexDump;
import com.logginghub.utils.Logger;
import com.logginghub.utils.NotImplementedException;

public class LoggingMessageCodex {

    private static final Logger logger = Logger.getLoggerFor(LoggingMessageCodex.class);
    public final static byte LogEvent = 1;
    public final static byte LogEventCollection = 2;
    // public final static byte LogTelemetry = 3;
    // public final static byte SubscribeRequest = 4;
    // public final static byte SubscribeResponse = 5;
    // public final static byte UnsubscribeRequest = 6;
    // public final static byte UnsubscribeResponse = 7;

    public final static byte SerialisableObject = 98;

    // public final static byte JavaSerialised = 99;

//    public final static byte Encrypted = 100;
//    public final static byte Compressed = 101;

    public final static byte ExtendedType = -127;

    private boolean logUnknownSofTypes = !Boolean.getBoolean("loggingMessageCodex.suppressLoggingOfUnknownSofTypes");

    public enum Flags {
        Encrypted,
        Compressed
    };

//    private CompressingCodex compressingCodex = new CompressingCodex();
//    private LazyReference<EncryptingCodex> encryptingCodex = new LazyReference<EncryptingCodex>() {
//        @Override protected EncryptingCodex instantiate() {
//            return new EncryptingCodex();
//        }
//    };

    // jshaw - this is very important, dont change the IDs!!
    private SofConfiguration sofConfiguration = new SofConfiguration() {
        {
            // registerType(AggregatedPatternDataMessage.class, 1);
            // registerType(AggregatedPatternDataSubscriptionRequestMessage.class, 2);
            // registerType(AggregatedPatternDataSubscriptionResponseMessage.class, 3);
            registerType(AggregationKey.class, 4);

            registerType(HistoricalIndexElement.class, 5);
            registerType(HistoricalIndexRequest.class, 6);
            registerType(HistoricalIndexResponse.class, 7);

            registerType(DefaultLogEvent.class, 8);
            registerType(HistoricalDataRequest.class, 9);
            registerType(HistoricalDataResponse.class, 10);

            registerType(CompressedBlock.class, 11);
            registerType(ChannelMessage.class, 12);

            registerType(LogEventMessage.class, 13);

            registerType(ChannelSubscriptionRequestMessage.class, 14);
            registerType(ChannelSubscriptionResponseMessage.class, 15);

            registerType(EventSubscriptionRequestMessage.class, 16);
            registerType(EventSubscriptionResponseMessage.class, 17);

            registerType(DataStructure.class, 22);
            registerType(DataElement.class, 23);

            registerType(AggregatedPatternData.class, 24);
            
            registerType(SubscriptionRequestMessage.class, 25);
            registerType(SubscriptionResponseMessage.class, 26);
            
            registerType(UnsubscriptionRequestMessage.class, 27);
            registerType(UnsubscriptionResponseMessage.class, 28);
            
            registerType(FilterRequestMessage.class, 29);

        }
    };

    public LoggingMessage decode(ByteBuffer buffer) throws PartialMessageException {
        int type = buffer.get();

        logger.finest("Attempting to decode message with type '{}'", type);

        buffer.mark();
        try {
            switch (type) {
                case LogEvent: {
                    try {
                        LogEvent logEvent = LogEventCodex.decode(buffer);
                        LogEventMessage message = new LogEventMessage(logEvent);
                        return message;
                    }
                    catch (RuntimeException t) {
                        int position = buffer.position();
                        buffer.reset();
                        logger.severe(t, "Log event decode failed at position '{}' : {}", position, t.getMessage());
                        if (buffer.remaining() < ByteUtils.kilobytes(100)) {
                            logger.severe("Log event buffer was : {}", HexDump.format(buffer));
                        }
                        else {
                            logger.fine("Log event buffer was : {}", HexDump.format(buffer));
                        }
                        throw t;
                    }
                }
                // case LogTelemetry: {
                // try {
                // TelemetryStack telemetryStack = LogTelemetryCodex.decode(buffer);
                // LogTelemetryMessage message = new LogTelemetryMessage(telemetryStack);
                // return message;
                // }
                // catch (RuntimeException t) {
                // buffer.reset();
                // logger.severe("Log telemetry decode fail : {}", HexDump.format(buffer));
                // throw t;
                // }
                // }
                case LogEventCollection: {
                    LogEventCollection logEventCollection = LogEventCollectionCodex.decode(buffer);
                    LogEventCollectionMessage message = new LogEventCollectionMessage(logEventCollection);
                    return message;
                }
                // case SubscribeRequest: {
                // NewSubscriptionRequestMessage message = new
                // NewSubscriptionRequestMessage(LogEventCodex.decodeStringArray(buffer));
                // return message;
                // }
                // case SubscribeResponse: {
                // NewSubscriptionResponseMessage message = new
                // NewSubscriptionResponseMessage(LogEventCodex.decodeStringArray(buffer));
                // return message;
                // }
                // case UnsubscribeRequest: {
                // NewUnsubscribeRequestMessage message = new
                // NewUnsubscribeRequestMessage(LogEventCodex.decodeStringArray(buffer));
                // return message;
                // }
                // case UnsubscribeResponse: {
                // NewUnsubscribeResponseMessage message = new
                // NewUnsubscribeResponseMessage(LogEventCodex.decodeStringArray(buffer));
                // return message;
                // }
//                case Encrypted: {
//                    ByteBuffer decrypted = encryptingCodex.get().decrypt(buffer);
//                    return decode(decrypted);
//                }
//                case Compressed: {
//                    ByteBuffer uncompressed = compressingCodex.uncompress(buffer);
//                    return decode(uncompressed);
//                }

                // case JavaSerialised: {
                // throw new
                // NotImplementedException("Java serialisation has been removed - if you see this exception, its a serious bug.");
                // try {
                // LoggingMessage decode = JavaSerialisationCodex.decode(buffer);
                // return decode;
                // }
                // catch (Exception e) {
                // // Someone has sent us something we can't interpret,
                // // ignore it
                // return null;
                // }
                // }
                case SerialisableObject: {
                    SerialisableObject message = decodeSof(buffer);
                    return (LoggingMessage) message;
                }
                default: {
                    throw new RuntimeException("Message type " + type + " isn't recognised");
                }
            }
        }
        catch (BufferUnderflowException bue) {
            // This is ok, it just means the entire event isn't in the buffer
            // yet.
            buffer.reset();
            throw new PartialMessageException();
        }
    }

    public SerialisableObject decodeSof(ByteBuffer buffer) throws PartialMessageException {
        SerialisableObject message = null;
        try {
            message = SofExpandingBufferSerialiser.read(buffer, sofConfiguration);
        }
        catch (SofUnknownTypeException e) {
            // We've been sent an unknown type - for backwards compatibility reasons this might not
            // be a bad thing
            if (logUnknownSofTypes) {
                logger.warning("Recieved unknown sof message type - header details {}", e.getHeaderSummary());
            }
        }
        catch (SofPartialDecodeException e) {
            throw new PartialMessageException();
        }
        catch (SofException e) {
            throw new FormattedRuntimeException(e, "Failed to decode SOF serialisable object");
        }
        catch (EOFException e) {
            throw new FormattedRuntimeException(e, "End of file reported from sof serialiser");
        }
        return message;
    }

    public void encode(ExpandingByteBuffer expandingByteBuffer, LoggingMessage message) {
        if (message instanceof LogEventMessage) {
            LogEventMessage logEventMessage = (LogEventMessage) message;
            encode(expandingByteBuffer, logEventMessage.getLogEvent());
        }
        else if (message instanceof LogEventCollectionMessage) {
            LogEventCollectionMessage logEventCollectionMessage = (LogEventCollectionMessage) message;
            encode(expandingByteBuffer, logEventCollectionMessage.getLogEventCollection());
        }
        // else if (message instanceof LogTelemetryMessage) {
        // LogTelemetryMessage telemetryMessage = (LogTelemetryMessage) message;
        // encode(expandingByteBuffer, telemetryMessage.getTelemetryStack());
        // }
        // else if (message instanceof NewSubscriptionRequestMessage) {
        // NewSubscriptionRequestMessage actualMessage = (NewSubscriptionRequestMessage) message;
        // encode(expandingByteBuffer, actualMessage);
        // }
        // else if (message instanceof NewSubscriptionResponseMessage) {
        // NewSubscriptionResponseMessage actualMessage = (NewSubscriptionResponseMessage) message;
        // encode(expandingByteBuffer, actualMessage);
        // }
        // else if (message instanceof NewUnsubscribeResponseMessage) {
        // NewUnsubscribeResponseMessage actualMessage = (NewUnsubscribeResponseMessage) message;
        // encode(expandingByteBuffer, actualMessage);
        // }
        // else if (message instanceof NewUnsubscribeRequestMessage) {
        // NewUnsubscribeRequestMessage actualMessage = (NewUnsubscribeRequestMessage) message;
        // encode(expandingByteBuffer, actualMessage);
        // }
        // else if(message instanceof SerialisableObjectWrapper){
        // SerialisableObjectWrapper serialisableObjectWrapper = (SerialisableObjectWrapper)
        // message;
        // SerialisableObject serialisableObject =
        // serialisableObjectWrapper.getSerialisableObject();
        // encode(expandingByteBuffer, serialisableObject);
        // }
        else if (message instanceof SerialisableObject) {
            SerialisableObject serialisableObject = (SerialisableObject) message;
            encode(expandingByteBuffer, serialisableObject);
        }
        else if (message instanceof Serializable) {
            throw new NotImplementedException("Java serialisation has been removed - message class was {} - if you see this exception, its a serious bug.",
                                              message.getClass().getName());
            // expandingByteBuffer.put(LoggingMessageCodex.JavaSerialised);
            // JavaSerialisationCodex.encode(expandingByteBuffer, message);
        }
        else {
            throw new IllegalArgumentException("Dont know how to encode " + message);
        }
    }

    private void encode(ExpandingByteBuffer expandingByteBuffer, SerialisableObject object) {
        expandingByteBuffer.put(LoggingMessageCodex.SerialisableObject);

        try {
            SofExpandingBufferSerialiser.write(expandingByteBuffer, object, sofConfiguration);
        }
        catch (SofException e) {
            throw new FormattedRuntimeException("Failed to encode SOF serialisable object", e);
        }
    }

    // public void encode(ExpandingByteBuffer expandingBuffer, NewSubscriptionRequestMessage
    // message) {
    // expandingBuffer.put(LoggingMessageCodex.SubscribeRequest);
    // LogEventCodex.encodeStringArray(expandingBuffer, message.getChannels());
    // }
    //
    // public void encode(ExpandingByteBuffer expandingBuffer, NewSubscriptionResponseMessage
    // message) {
    // expandingBuffer.put(LoggingMessageCodex.SubscribeResponse);
    // LogEventCodex.encodeStringArray(expandingBuffer, message.getChannels());
    // }

    // public void encode(ExpandingByteBuffer expandingBuffer, NewUnsubscribeRequestMessage message)
    // {
    // expandingBuffer.put(LoggingMessageCodex.SubscribeRequest);
    // LogEventCodex.encodeStringArray(expandingBuffer, message.getChannels());
    // }
    //
    // public void encode(ExpandingByteBuffer expandingBuffer, NewUnsubscribeResponseMessage
    // message) {
    // expandingBuffer.put(LoggingMessageCodex.SubscribeResponse);
    // LogEventCodex.encodeStringArray(expandingBuffer, message.getChannels());
    // }

    public void encode(ExpandingByteBuffer expandingBuffer, LogEvent logEvent) {
        encode(expandingBuffer, logEvent, EnumSet.noneOf(Flags.class));
    }

    // public void encode(ExpandingByteBuffer expandingBuffer, TelemetryStack telemetryStack) {
    // encode(expandingBuffer, telemetryStack, EnumSet.noneOf(Flags.class));
    // }

    public void encode(ExpandingByteBuffer expandingBuffer, LogEvent logEvent, EnumSet<Flags> flags) {
        logger.finest("Encoding event into buffer '{}'", expandingBuffer);
        int position = expandingBuffer.position();
        expandingBuffer.put(LoggingMessageCodex.LogEvent);

        logger.finest("Type byte written, buffer is now '{}'", expandingBuffer);

        LogEventCodex.encode(expandingBuffer, logEvent);

        logger.finest("Log event written, buffer is now '{}'", expandingBuffer);

        processFlags(expandingBuffer, flags, position);

        logger.finest("Flag processing complete, buffer is now '{}'", expandingBuffer);
    }

    // public void encode(ExpandingByteBuffer expandingBuffer, TelemetryStack telemetryStack,
    // EnumSet<Flags> flags) {
    // logger.finest("Encoding event into buffer '{}'", expandingBuffer);
    // int position = expandingBuffer.position();
    // expandingBuffer.put(LoggingMessageCodex.LogTelemetry);
    //
    // logger.finest("Type byte written, buffer is now '{}'", expandingBuffer);
    //
    // LogTelemetryCodex.encode(expandingBuffer, telemetryStack);
    //
    // logger.finest("Log event written, buffer is now '{}'", expandingBuffer);
    //
    // processFlags(expandingBuffer, flags, position);
    //
    // logger.finest("Flag processing complete, buffer is now '{}'", expandingBuffer);
    // }

    public void encode(ExpandingByteBuffer expandingBuffer, LogEventCollection logEventCollection) {
        encode(expandingBuffer, logEventCollection, EnumSet.noneOf(Flags.class));
    }

    public void encode(ExpandingByteBuffer expandingBuffer, LogEventCollection collection, EnumSet<Flags> flags) {
        int position = expandingBuffer.getBuffer().position();
        expandingBuffer.put(LoggingMessageCodex.LogEventCollection);
        LogEventCollectionCodex.encode(expandingBuffer, collection);

        processFlags(expandingBuffer, flags, position);
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    private void processFlags(ExpandingByteBuffer expandingBuffer, EnumSet<Flags> flags, int position) {
//        if (flags.contains(Flags.Compressed)) {
//            expandingBuffer.insertByte(position, (byte) LoggingMessageCodex.Compressed);
//            compressingCodex.compress(expandingBuffer, position + 1);
//
//            // The compression will hopefully make the message shorter =)
//            // position = expandingBuffer.getBuffer().position();
//        }

//        if (flags.contains(Flags.Encrypted)) {
//            expandingBuffer.insertByte(position, (byte) LoggingMessageCodex.Encrypted);
//            encryptingCodex.get().encrypt(expandingBuffer, position + 1);
//
//            // The encryption will probably make the message a bit longer
//            // position = expandingBuffer.getBuffer().position();
//        }

    }

    public SofConfiguration getSofConfiguration() {
        return sofConfiguration;
    }

    public void setLogUnknownSofTypes(boolean logUnknownSofTypes) {
        this.logUnknownSofTypes = logUnknownSofTypes;
    }

    public boolean isLogUnknownSofTypes() {
        return logUnknownSofTypes;
    }
}
