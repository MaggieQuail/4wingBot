package org.mq.dailyarticle.services;

import org.mq.dailyarticle.models.VocabularyModel;

import javax.sql.DataSource;
import java.sql.*;

public class ArticleService {

    private static final String GET_ARTICLE_BY_ID_QUERY =
        """
        SELECT
          id,
          article
        FROM articles
        WHERE
          id = ?
        """;

    private static final String GET_ARTICLE_COUNT_QUERY =
        """
        SELECT
          COUNT(id) as article_count
        FROM
          articles
        """;

    private final DataSource ds;

    public ArticleService(DataSource ds) {
        this.ds = ds;
    }

    public VocabularyModel getById(int id) {
        try (Connection conn = ds.getConnection();
             PreparedStatement statement = conn.prepareStatement(GET_ARTICLE_BY_ID_QUERY)
        ) {
            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("article with id = " + id + " doesn't exist");
                }
                String article = rs.getString("article");
                return new VocabularyModel(id, article);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public int getArticleCount() {
        try (Connection conn = ds.getConnection();
             PreparedStatement statement = conn.prepareStatement(GET_ARTICLE_COUNT_QUERY);
             ResultSet rs = statement.executeQuery()
        ) {
            return rs.getInt("article_count");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
