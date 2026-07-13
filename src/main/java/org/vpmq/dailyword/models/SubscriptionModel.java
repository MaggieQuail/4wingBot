package org.vpmq.dailyword.models;

public class SubscriptionModel {

    private final long id;
    private final long userId;
    private final long chatId;
    private final int lastMsgId;

    public SubscriptionModel(long id, long userId, long chatId, int lastMsgId) {
        this.id = id;
        this.userId = userId;
        this.chatId = chatId;
        this.lastMsgId = lastMsgId;
    }

    public long getId() {
        return id;
    }

    public long getChatId() {
        return chatId;
    }

    public int getLastMsgId() {
        return lastMsgId;
    }

    public long getUserId() {
        return userId;
    }
}
