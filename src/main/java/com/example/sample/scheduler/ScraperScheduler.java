package com.example.sample.scheduler;

import com.example.sample.model.Company;
import com.example.sample.model.ScrapedResult;
import com.example.sample.model.constants.CacheKey;
import com.example.sample.persist.CompanyRepository;
import com.example.sample.persist.DividendRepository;
import com.example.sample.persist.entity.CompanyEntity;
import com.example.sample.persist.entity.DividendEntity;
import com.example.sample.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;

//    @Scheduled(fixedDelay = 1000)
//    public void test1() throws InterruptedException{
//        Thread.sleep(10000);
//        System.out.println(Thread.currentThread().getName() + " -> 테스트 1 : " + LocalDateTime.now());
//    }
//
//    @Scheduled(fixedDelay = 1000)
//    public void test2(){
//        System.out.println(Thread.currentThread().getName() + " -> 테스트 2 : " + LocalDateTime.now());
//    }

    // 일정 주기마다 수행 cron = "초 분 시 일 월 요일 년('년'은 생략 가능)"
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    // @CacheEvict(value = "finance", allEntries = true) -> finance 에 있는 모든 데이터를 삭제
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("Scraping Schedule is  Started");
        // 저장된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        for (var company : companies) {
            log.info("Scraping Schedule is  Started -> " + company.getName());
            ScrapedResult scrapedResult =
                    this.yahooFinanceScraper.scrap(
                            new Company(company.getTicker(), company.getName()));

            // 스크래핑한 배당금 정보 중 데이터베이스 없는 값은 저장
            scrapedResult.getDividends().stream()
                    // Dividend 모델을 Dividend Entity 로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 엘리먼트를 하나씩 Dividend Repository 에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository
                                .existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });

            //연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
