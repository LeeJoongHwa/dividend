package com.example.sample.scraper;

import com.example.sample.model.Company;
import com.example.sample.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
