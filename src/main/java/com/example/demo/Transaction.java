package com.example.demo;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Transaction {
    private String fromCountry = "NO";
    private String toCountry = "NO";
    private double amount;
}
