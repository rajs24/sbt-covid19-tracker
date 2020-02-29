package com.sbt.covid19.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sbt.covid19.models.CovidData;

@Service
public class DataService {

	@Autowired
	RestTemplate restTemplate;

	private static String DATA_COVID_CONFIRMED_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
	private static String DATA_COVID_DEATHS_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Deaths.csv";
	private static String DATA_COVID_RECOVERED_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Recovered.csv";

	private List<CovidData> listOfData = new ArrayList<CovidData>();
	private List<CovidData> listOfConfirmedCasesData = new ArrayList<CovidData>();
	private List<CovidData> listOfDeathsData = new ArrayList<CovidData>();
	private List<CovidData> listOfRecoveredData = new ArrayList<CovidData>();
	private List<CovidData> listOfConfirmedCasesByCountry = new ArrayList<CovidData>();
	private List<CovidData> listOfDeathCasesByCountry = new ArrayList<CovidData>();
	private List<CovidData> listOfRecoveredCasesByCountry = new ArrayList<CovidData>();

	@PostConstruct
	@Scheduled(cron = "1 * 1 * * *")
	public void fecthCovidTotalConfiredCasesData() throws IOException {

		listOfConfirmedCasesData = fetchCovid19Data(DATA_COVID_CONFIRMED_URL);
		listOfConfirmedCasesByCountry = fetchCovid19DataByCountry(DATA_COVID_CONFIRMED_URL);

	}

	@PostConstruct
	@Scheduled(cron = "1 * 1 * * *")
	public void fecthCovidTotalDeathCasesData() throws IOException {

		listOfDeathsData = fetchCovid19Data(DATA_COVID_DEATHS_URL);
		listOfDeathCasesByCountry = fetchCovid19DataByCountry(DATA_COVID_DEATHS_URL);
	}

	@PostConstruct
	@Scheduled(cron = "1 * 1 * * *")
	public void fecthCovidTotalRecoveredCasesData() throws IOException {

		listOfRecoveredData = fetchCovid19Data(DATA_COVID_RECOVERED_URL);
		listOfRecoveredCasesByCountry = fetchCovid19DataByCountry(DATA_COVID_RECOVERED_URL);
	}

	private List<CovidData> fetchCovid19Data(String dataUrl) throws IOException {

		List<CovidData> listOfNewData = new ArrayList<CovidData>();
		String readData = restTemplate.getForObject(dataUrl, String.class);
		StringReader csvResReader = new StringReader(readData);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvResReader);
		for (CSVRecord record : records) {
			int currDayCases = Integer.parseInt(record.get(record.size() - 1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			listOfNewData.add(new CovidData(record.get("Province/State"), record.get("Country/Region"), currDayCases,
					currDayCases - prevDayCases));
		}
		listOfNewData.sort(Comparator.comparingInt(CovidData::getTotalCases).reversed());
		return this.listOfData = listOfNewData;
	}

	private List<CovidData> fetchCovid19DataByCountry(String dataUrl) throws IOException {

		List<CovidData> listOfNewData = new ArrayList<CovidData>();
		String readData = restTemplate.getForObject(dataUrl, String.class);
		StringReader csvResReader = new StringReader(readData);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvResReader);
		for (CSVRecord record : records) {
			int currDayCases = Integer.parseInt(record.get(record.size() - 1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			listOfNewData.add(new CovidData(record.get("Province/State"), record.get("Country/Region"), currDayCases,
					currDayCases - prevDayCases));
		}

		List<CovidData> totalCasesByCountry = listOfNewData.stream()
				.collect(Collectors.groupingBy(data -> data.getCountry())).entrySet().stream()
				.map(e -> e.getValue().stream().reduce(
						(cvd1, cvd2) -> new CovidData(cvd1.getCountry(), cvd1.getTotalCases() + cvd2.getTotalCases())))
				.map(f -> f.get()).collect(Collectors.toList());
		totalCasesByCountry.sort(Comparator.comparingInt(CovidData::getTotalCases).reversed());
		return this.listOfData = totalCasesByCountry;
	}

	public List<CovidData> getListOfConfirmedCasesData() {
		return listOfConfirmedCasesData;
	}

	public List<CovidData> getListOfDeathsData() {
		return listOfDeathsData;
	}

	public List<CovidData> getListOfRecoveredData() {
		return listOfRecoveredData;
	}

	public List<CovidData> getListOfConfirmedCasesByCountry() {
		return listOfConfirmedCasesByCountry;
	}

	public List<CovidData> getListOfDeathCasesByCountry() {
		return listOfDeathCasesByCountry;
	}

	public List<CovidData> getListOfRecoveredCasesByCountry() {
		return listOfRecoveredCasesByCountry;
	}

//	Example patterns:
//
//		"0 0 * * * *" = the top of every hour of every day.
//		"*/10 * * * * *" = every ten seconds.
//		"0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.
//		"0 0 6,19 * * *" = 6:00 AM and 7:00 PM every day.
//		"0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day.
//		"0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays
//		"0 0 0 25 12 ?" = every Christmas Day at midnight
}
