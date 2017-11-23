package com.example.bot.spring;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

// No need implementation, just one interface, and you have CRUD, thanks Spring Data
public interface DomainRepository extends MongoRepository<Domain, Long>, DomainRepositoryCustom {

    Domain findByDomain(String domain);

    Domain findByDomainAndDisplayAds(String domain, boolean displayAds);

    @Override
    List<Domain> findAll();
    //Supports native JSON query string
    @Query("{domain:'?0'}")
    Domain findCustomByDomain(String domain);

    @Query("{domain: { $regex: ?0 } })")
    List<Domain> findCustomByRegExDomain(String domain);

}
