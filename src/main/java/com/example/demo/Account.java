package com.example.demo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Required;

import java.math.BigDecimal;
import java.math.BigInteger;

@ToString
@Data
@RequiredArgsConstructor
public class Account {

    private String currency = "NOK";
    private String id;
    private BigDecimal balance = BigDecimal.valueOf(0);

    public BigDecimal getBalance() {
        return balance;
    }
}
