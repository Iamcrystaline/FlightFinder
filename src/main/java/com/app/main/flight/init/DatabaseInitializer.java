package com.app.main.flight.init;

import com.app.main.flight.AirportsRepository;
import com.app.main.flight.models.AirportsForCity;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final AirportsRepository repository;

    @Value("${app.wiki.url}")
    private String wikiUrl;

    @EventListener
    public void initTable(ContextRefreshedEvent event) {
        Document wikiDoc;
        try {
            wikiDoc = Jsoup.connect(wikiUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            throw new TargetUnreachableException("Can't get information from " + wikiUrl + ", " +
                    "please check wikiUrl correctness and try again later");
        }
        Element airportsInfoTable = wikiDoc.selectFirst("table");
        if (airportsInfoTable == null) {
            throw new PageStructureException(wikiUrl + " page structure has been changed. Check out the url and fix data extraction");
        }
        repository.saveAll(extractDataFromTable(airportsInfoTable));
    }

    private List<AirportsForCity> extractDataFromTable(Element airportsInfoTable) {
        Elements tableCells = airportsInfoTable.select("tr > td");
        List<Airport> airports = new ArrayList<>(100);
        for (int i = 0; i < tableCells.size(); i += 5) {
            airports.add(new Airport(
                    tableCells.get(i + 1).text(),
                    tableCells.get(i + 3).text(),
                    tableCells.get(i + 4).text()
            ));
        }
        // #TODO: maybe we don't need to save ICAO codes
        return airports.stream()
                .collect(Collectors.groupingBy(Airport::city))
                .entrySet()
                .stream()
                .map(entry -> new AirportsForCity(
                        entry.getKey(),
                        entry.getValue()
                                .stream()
                                .map(Airport::IATACode)
                                .collect(Collectors.joining(", ")),
                        entry.getValue()
                                .stream()
                                .map(Airport::ICAOCode)
                                .collect(Collectors.joining(", "))
                ))
                .toList();
    }
}
