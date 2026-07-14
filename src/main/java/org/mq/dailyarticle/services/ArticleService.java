package org.mq.dailyarticle.services;

import org.mq.dailyarticle.models.VocabularyModel;

import javax.sql.DataSource;
import java.sql.*;

public class ArticleService {

    private static final String GET_RANDOM_ARTICLE_QUERY =
        """
        SELECT
          id,
          article
        FROM articles
        ORDER BY RANDOM()
        LIMIT 1
        """;

    private final DataSource ds;

    public ArticleService(DataSource ds) {
        this.ds = ds;
    }

    public VocabularyModel getRandom() {
        try (Connection conn = ds.getConnection();
             PreparedStatement statement = conn.prepareStatement(GET_RANDOM_ARTICLE_QUERY);
             ResultSet rs = statement.executeQuery()
        ) {
            if (!rs.next()) {
                throw new RuntimeException("no articles in database");
            }
            return new VocabularyModel(rs.getInt("id"), rs.getString("article"));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
