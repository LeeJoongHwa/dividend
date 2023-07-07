package com.example.sample;

import com.example.sample.model.Company;
import com.example.sample.scraper.Scraper;
import com.example.sample.scraper.YahooFinanceScraper;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class SampleApplication {

    public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
    }
}
