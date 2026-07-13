package org.mq.dailyarticle;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.mq.dailyarticle.services.ArticleService;
import org.mq.dailyarticle.services.DailyArticleBotService;
import org.mq.dailyarticle.services.SubscriptionService;

import java.util.concurrent.Executors;

public class Main {
    private static final String BOT_TOKEN = requireEnv("BOT_TOKEN");
    private static final String JDBC_URL = System.getenv().getOrDefault(
        "JDBC_URL",
        "jdbc:sqlite:4wing-articles.db"
    );

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }

    public static void main(String[] args) {
        HikariConfig dbCfg = new HikariConfig();
        dbCfg.setJdbcUrl(JDBC_URL);

        try {
            HikariDataSource db = new HikariDataSource(dbCfg);
            ArticleService articleGetter = new ArticleService(db);
            SubscriptionService subscriptions = new SubscriptionService(db);

            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new DailyArticleBotService(
                articleGetter,
                subscriptions,
                Executors.newScheduledThreadPool(1),
                BOT_TOKEN
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
