package com.example.bot.spring;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

// No need implementation, just one interface, and you have CRUD, thanks Spring Data
public interface OilchangeRepository extends MongoRepository<Oilchange, Long>, OilchangeRepositoryCustom {

    Oilchange findByOilchange(String oilchange);


    @Override
    List<Oilchange> findAll();

    //Supports native JSON query string
    @Query("{oilchange:'?0'}")
    Oilchange findCustomByOilchange(String oilchange);

    @Query("{oilchange: { $regex: ?0 } })")
    List<Oilchange> findCustomByRegExOilchange(String oilchange);

}

