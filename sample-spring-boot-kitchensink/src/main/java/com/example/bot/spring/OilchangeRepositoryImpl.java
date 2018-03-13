package com.example.bot.spring;

import com.mongodb.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

//Impl postfix of the name on it compared to the core repository interface
public class OilchangeRepositoryImpl implements OilchangeRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public int updateOilchange(String oldoilchange,String newoilchange) {

        Query query = new Query(Criteria.where("oilchange").is(oldoilchange));
        Update update = new Update();
        update.set("oilchange", newoilchange);

        WriteResult result = mongoTemplate.updateFirst(query, update, Oilchange.class);
        if(result!=null)
            return result.getN();
        else
            return 0;

    }
}

