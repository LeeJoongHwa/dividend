package com.example.sample.service;

import com.example.sample.exception.impl.NoCompanyException;
import com.example.sample.model.Company;
import com.example.sample.model.Dividend;
import com.example.sample.model.ScrapedResult;
import com.example.sample.model.constants.CacheKey;
import com.example.sample.persist.CompanyRepository;
import com.example.sample.persist.DividendRepository;
import com.example.sample.persist.entity.CompanyEntity;
import com.example.sample.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    // 캐싱 전 생각해볼 것
    // 요청이 얼마나 빈번하게 들어오는지?
    // 자주 변경되는 데이터인지?
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);
        //1. 회사명을 기준으로 회사 정보 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        //2. 조회된 회사 ID 로 배당금 정보 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

        //3. 결과 조합 후 반환
//        List<Dividend> dividends = new ArrayList<>();
//        for (var entity : dividendEntities) {
//            dividends.add(Dividend.builder()
//                    .date(entity.getDate())
//                    .dividend(entity.getDividend())
//                    .build());
//        } 아래 코드나 이 코드나 무엇을 쓰든 상관 없음!!

        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(),e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(), company.getName()), dividends
        );
    }
}
