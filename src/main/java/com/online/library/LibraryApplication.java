package com.online.library;

import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
@Log
@SpringBootApplication
public class LibraryApplication implements CommandLineRunner {

    private final DataSource dataSource;

    public LibraryApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	public static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        log.info("Connected to database: " + dataSource.getConnection());
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("SELECT 1");
    }
}
