package com.example.springdatajdbcconverterlog

import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.config.Charset
import com.wix.mysql.config.MysqldConfig
import com.wix.mysql.distribution.Version
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextStoppedEvent
import org.springframework.context.event.EventListener
import javax.sql.DataSource

@Configuration
class EmbeddedMysqlConfiguration {

    @Bean
    fun dataSource(
        dataSourceProperties: DataSourceProperties,
        @Value("\${embedded-mysql.port}") port: Int,
        @Value("\${embedded-mysql.username}") username: String,
        @Value("\${embedded-mysql.password}") password: String,
        @Value("\${embedded-mysql.schema}") schema: String
    ): DataSource? {

        startEmbeddedMysqlServer(port, username, password, schema)

        return DataSourceBuilder.create()
            .url(dataSourceProperties.url)
            .username(dataSourceProperties.username)
            .password(dataSourceProperties.password)
            .build()
    }

    @EventListener(ContextStoppedEvent::class)
    fun stopEmbeddedMysqlServer() {
        server?.stop()
    }

    companion object {
        private var server: EmbeddedMysql? = null

        fun startEmbeddedMysqlServer(port: Int, username: String, password: String, schema: String) {
            if (server == null) {
                val config = MysqldConfig.aMysqldConfig(Version.v5_7_latest)
                    .withCharset(Charset.UTF8)
                    .withPort(port)
                    .withUser(username, password)
                    .withTempDir("build/")
                    .build()
                server = EmbeddedMysql.anEmbeddedMysql(config)
                    .addSchema(schema)
                    .start()
            }
        }
    }
}
