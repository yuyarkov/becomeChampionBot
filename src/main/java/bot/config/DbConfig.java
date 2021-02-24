package bot.config;

import bot.model.Tables;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;

@Configuration
public class DbConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        return new HikariDataSource(config);
    }

    @Bean
    public SimpleJdbcInsert waitDanceSimpleInsert() {
        return new SimpleJdbcInsert(dataSource()).withTableName(Tables.WAIT_LIST_TABLE_NAME);
    }

    @Bean
    public SimpleJdbcInsert signedDanceSimpleInsert() {
        return new SimpleJdbcInsert(dataSource()).withTableName(Tables.SIGNED_LIST_TABLE_NAME);
    }

}
