package com.infoshareacademy.controller;

import com.infoshareacademy.model.InputData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class StockSorterTest {

    @Test
    public void shouldSortDateByPrice() {
        //Given
        String pathToFile = "src/test/resources/bitCoin.csv";
        ImportStock importStock = new ImportStock(pathToFile);

        //When
        importStock.readFromFile();
        StockSorter stockSorter = new StockSorter();
        List<InputData> list = importStock.getResources();
        list = stockSorter.sortDataBy(list, StockSorter.byPrice, LocalDate.parse("2017-01-01"), LocalDate.parse("2017-01-06"));
        Double actual = list.get(5).getPrice();

        //Then
        assertEquals(1156.73, (double) actual);

    }

}
