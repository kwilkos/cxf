package org.apache.cxf.aegis.type.java5;

public class CurrencyService
{
    public enum Currency
    {
        USD,
        POUNDS,
        EURO
    }
    
    public int convert(int input, Currency inputCurrency, Currency outputCurrency)
    {
        return input;
    }
}
