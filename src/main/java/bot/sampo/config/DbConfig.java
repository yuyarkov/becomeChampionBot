package bot.sampo.config;

import bot.sampo.model.Tables;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.pro.packaged.S;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("!test")
public class DbConfig {

    @Bean
    public DataSource dataSource() {

        //https://devcenter.heroku.com/articles/connecting-to-relational-databases-on-heroku-with-java#using-the-database_url-in-spring-with-java-configuration

        var uri = URI.create(System.getenv("DATABASE_URL"));

        var url = "jdbc:postgresql://"+uri.getHost()+":"+uri.getPort()+uri.getPath();
        var userName = uri.getUserInfo().split(":")[0];
        var password=uri.getUserInfo().split(":")[1];

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(userName);
        config.setPassword(password);

        return new HikariDataSource(config);
    }


}
