package org.mq.dailyarticle.services;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberBanned;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.mq.dailyarticle.models.VocabularyModel;
import org.mq.dailyarticle.utils.MarkdownEscaper;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DailyArticleBotService extends TelegramLongPollingBot {

    private final SubscriptionService subscriptions;
    private final ArticleService articleGetter;
    private final Object articleLock = new Object();

    private VocabularyModel articleOfTheDay;

    public DailyArticleBotService(ArticleService articleGetter,
                                  SubscriptionService subscriptions,
                                  ScheduledExecutorService scheduler,
                                  String botToken) {
        super(botToken);
        this.subscriptions = subscriptions;
        this.articleGetter = articleGetter;
        this.articleOfTheDay = articleGetter.getById(1);
        this.updateArticleOfTheDayPeriodically(scheduler);
    }

    @Override
    public String getBotUsername() {
        return "4WingBot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && "/start".equalsIgnoreCase(message.getText())) {
            long userId = message.getFrom().getId();
            subscriptions.addSubscription(userId);
            sendText(userId, generateMessageText());
        }

        if (update.hasMyChatMember()) {
            ChatMemberUpdated chatMemberUpdate = update.getMyChatMember();
            String memberStatus = chatMemberUpdate.getNewChatMember().getStatus();
            if (ChatMemberBanned.STATUS.equalsIgnoreCase(memberStatus)
                || ChatMemberLeft.STATUS.equalsIgnoreCase(memberStatus)
            ) {
                subscriptions.deleteSubscription(chatMemberUpdate.getFrom().getId());
            }
        }
    }

    private Message sendText(long userId, String text) {
        SendMessage sm = SendMessage.builder()
            .chatId(Long.toString(userId))
            .parseMode("MarkdownV2")
            .text(MarkdownEscaper.escape(text)).build();
        try {
            return execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void updateArticleOfTheDayPeriodically(ScheduledExecutorService scheduler) {
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (articleLock) {
                try {
                    List<Long> subscriberIds = subscriptions.getSubscriptions();

                    int nextId = articleOfTheDay.id + 1;
                    if (nextId > articleGetter.getArticleCount()) {
                        nextId = 1;
                    }
                    articleOfTheDay = articleGetter.getById(nextId);

                    broadcastNewArticle(subscriberIds);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 24, 24, TimeUnit.HOURS);
    }

    private void broadcastNewArticle(List<Long> subscriberIds) {
        String text = generateMessageText();
        subscriberIds.forEach(userId -> sendText(userId, text));
    }

    private String generateMessageText() {
        synchronized (articleLock) {
            return "\uD83D\uDFE9 `" + articleOfTheDay.article + "`";
        }
    }
}
