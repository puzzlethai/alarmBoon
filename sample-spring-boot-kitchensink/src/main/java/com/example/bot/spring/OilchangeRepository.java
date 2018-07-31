package com.example.bot.spring;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OilchangeRepository extends MongoRepository<Oilchange, Long> {

    Oilchange findByOilchange(String oilchange);

    //List<Oilchange> findAllOilchange();

    @Override
    List<Oilchange> findAll();

    @Override
    void delete(Oilchange entity);
}
