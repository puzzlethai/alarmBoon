package com.example.bot.spring;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CustomerRepository extends MongoRepository<Customer, Long> {

    Customer findByUserId(String userId);

    //List<Customer> findAllCustomer();

    @Override
    List<Customer> findAll();

    @Override
    void delete(Customer entity);
}