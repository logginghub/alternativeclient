package com.logginghub.connector.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import com.logginghub.connector.common.messages.ChannelMessage;
import com.logginghub.utils.Destination;

public abstract class SubscriptionController<T extends Destination> {

    private Map<String, List<T>> counterparts = new HashMap<String, List<T>>();

    private Map<String, Future<Boolean>> subscriptionFutures = new HashMap<String, Future<Boolean>>();

    public Future<Boolean> addSubscription(String channel, T counterpart) {
        channel = tidy(channel);
        List<T> list;
        synchronized (counterparts) {
            list = counterparts.get(channel);
            if (list == null) {
                list = new CopyOnWriteArrayList<T>();
                counterparts.put(channel, list);
                subscriptionFutures.put(channel, handleFirstSubscription(channel));
            }
        }

        list.add(counterpart);

        Future<Boolean> future = subscriptionFutures.get(channel);
        return future;
    }

    private String tidy(String channel) {
        if (channel.endsWith("/")) {
            return channel.substring(0, channel.length() - 1);
        }
        else {
            return channel;
        }
    }

    public void removeAllSubscriptions(T counterpart) {
        synchronized (counterparts) {
            List<String> emptySubscriptions = new ArrayList<String>();
            Set<Entry<String, List<T>>> entrySet = counterparts.entrySet();
            for (Entry<String, List<T>> entry : entrySet) {
                List<T> value = entry.getValue();
                boolean removed = value.remove(counterpart);
                if (removed && value.size() == 0) {
                    emptySubscriptions.add(entry.getKey());
                }
            }

            for (String channel : emptySubscriptions) {
                counterparts.remove(channel);
                handleLastSubscription(channel);
            }
        }
    }

    public void removeSubscription(String channel, T counterpart) {
        channel = tidy(channel);
        
        List<T> list;
        synchronized (counterparts) {
            list = counterparts.get(channel);

            if (list != null) {
                list.remove(counterpart);
                if (list.size() == 0) {
                    counterparts.remove(channel);
                    handleLastSubscription(channel);
                }
            }
        }
    }

    @SuppressWarnings("unchecked") public void dispatch(ChannelMessage message, T sourceCounterpart) {

        StringBuilder sb = new StringBuilder();
        String[] channels = message.getChannel();
        String div = "";
        for (String channelPart : channels) {

            sb.append(div).append(channelPart);
            div = "/";

            String channel = sb.toString();
            List<T> list;
            synchronized (counterparts) {
                list = counterparts.get(channel);
            }

            if (list != null) {
                for (T t : list) {
                    if (t != sourceCounterpart) {
                        t.send(message);
                    }
                }
            }

        }

        // Remember to check the global listeners too
        List<T> list;
        synchronized (counterparts) {
            list = counterparts.get("");
        }

        if (list != null) {
            for (T t : list) {
                if (t != sourceCounterpart) {
                    t.send(message);
                }
            }
        }

    }

    public List<T> getDestinations(String... channels) {

        List<T> destinations = new ArrayList<T>();

        StringBuilder sb = new StringBuilder();
        String div = "";
        for (String channelPart : channels) {
            sb.append(div).append(channelPart);
            div = "/";

            String channel = sb.toString();
            List<T> list;
            synchronized (counterparts) {
                list = counterparts.get(channel);
            }

            if (list != null) {
                destinations.addAll(list);
            }
        }

        return destinations;

    }

    protected abstract Future<Boolean> handleFirstSubscription(String channel);

    protected abstract Future<Boolean> handleLastSubscription(String channel);

    public Set<String> getChannels() {
        synchronized (counterparts) {
            return counterparts.keySet();
        }
    }

}
