package org.mq.dailyarticle.services;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionService {

    private static final String ADD_SUBSCRIPTION_QUERY =
        """
        INSERT INTO subscriptions(user_id)
        VALUES (?)
        """;

    private static final String GET_SUBSCRIPTIONS_QUERY =
        """
        SELECT user_id
        FROM subscriptions
        """;

    private static final String GET_SUBSCRIPTION_BY_USER_ID_QUERY =
        """
        SELECT id
        FROM subscriptions
        WHERE user_id = ?
        """;

    private static final String DELETE_SUBSCRIPTION_QUERY =
        """
        DELETE FROM subscriptions
        WHERE user_id = ?
        """;

    private final DataSource ds;

    public SubscriptionService(DataSource ds) {
        this.ds = ds;
    }

    public void addSubscription(long userId) {
        if (getUserSubscriptionId(userId) == 0) {
            try (Connection conn = ds.getConnection();
                 PreparedStatement statement = conn.prepareStatement(ADD_SUBSCRIPTION_QUERY)
            ) {
                statement.setLong(1, userId);
                statement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
    }

    public void deleteSubscription(long userId) {
        try (Connection conn = ds.getConnection();
             PreparedStatement statement = conn.prepareStatement(DELETE_SUBSCRIPTION_QUERY)
        ) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public List<Long> getSubscriptions() {
        try (Connection conn = ds.getConnection();
             PreparedStatement statement = conn.prepareStatement(GET_SUBSCRIPTIONS_QUERY);
             ResultSet rs = statement.executeQuery()
        ) {
            List<Long> userIds = new ArrayList<>();
            while (rs.next()) {
                userIds.add(rs.getLong("user_id"));
            }
            return userIds;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private int getUserSubscriptionId(long userId) {
        try (Connection conn = ds.getConnection();
             PreparedStatement statement = conn.prepareStatement(GET_SUBSCRIPTION_BY_USER_ID_QUERY)
        ) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return 0;
                }
                return rs.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
