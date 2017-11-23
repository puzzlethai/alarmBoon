package com.example.bot.spring;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;

@Document(collection = "customer")
public class Customer {

    @Id
    private BigInteger id;

    @Indexed(unique = true)
    private String userId;

    private boolean monkDay;
    public Customer(String userId, boolean monkDay){
        this.userId = userId;
        this.monkDay = monkDay;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isMonkDay() {
        return monkDay;
    }

    public void setMonkDay(boolean monkDay) {
        this.monkDay = monkDay;
    }
//getters and setters
}
