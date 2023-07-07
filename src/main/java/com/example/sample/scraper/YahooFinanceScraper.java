package com.example.sample.scraper;

import com.example.sample.model.Company;
import com.example.sample.model.Dividend;
import com.example.sample.model.ScrapedResult;
import com.example.sample.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {
    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400;

    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000; //초 단위로 바꾸기 위해 1000으로 나눔
            //System.currentTimeMillis()는 1970년 1월 1일부터 현재까지 MillisSeconds 로 시간을 가져온 값

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            //HTTP Connection을 맺고 그 Connection으로부터 html문서를 받아 파싱된 형태로
            // document 인스턴스로 만들어 주는 작업을 Jsoup 라이브러리가 해주었다.

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0); //table 전체 가져오기

            Element tbody = tableEle.children().get(1);
            //thead = .get(0), tbody = .get(1), tfoot = .get(2)

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

//                System.out.println(year + "/" + month + "/" + day + " -> " + dividend);

            }
            scrapResult.setDividends(dividends);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);
        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0);
            String title = titleEle.text().split(" - ")[1].trim();

            return new Company(ticker, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
