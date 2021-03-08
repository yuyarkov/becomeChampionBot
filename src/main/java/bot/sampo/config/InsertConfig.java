package bot.sampo.config;

import bot.sampo.model.Tables;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;

@Configuration
public class InsertConfig {

    @Bean
    public SimpleJdbcInsert waitDanceSimpleInsert(DataSource dataSource) {
        return new SimpleJdbcInsert(dataSource).withTableName(Tables.WAIT_LIST_TABLE_NAME);
    }

    @Bean
    public SimpleJdbcInsert pairDanceSimpleInsert(DataSource dataSource) {
        return new SimpleJdbcInsert(dataSource).withTableName(Tables.PAIR_LIST_TABLE_NAME);
    }

}