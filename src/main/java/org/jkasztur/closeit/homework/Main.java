package org.jkasztur.closeit.homework;

import lombok.extern.java.Log;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.*;
import java.net.URL;
import java.util.List;

@Log
public class Main {
	private final static String DATA_URL = "http://stat-computing.org/dataexpo/2009/1989.csv.bz2";
	private final static String DOWNLOAD_TARGET = "target/data.csv.bz2";

	public static void main(String[] args) throws IOException, CompressorException {
		downloadData();

		List<Flight> flights = parseDataFromCsv();

		// Slower solution using database
		flights.stream()
				.filter(flight -> flight.getDest().equals("LAX"))
				.filter(flight -> flight.getCancelled() == 0)
				.forEach(Main::insertFlightToDB);
		Double average = getAverageFromDB();
		HibernateUtil.shutdown();

		/* Faster solution using Java streams
		Double average = flights.stream()
				.filter(flight -> flight.getDest().equals("LAX"))
				.filter(flight -> flight.getCancelled() == 0)
				.filter(flight -> !flight.getArrDelay().equals("NA"))
				.mapToDouble(flight -> Double.parseDouble(flight.getArrDelay()))
				.average().orElse(Double.NaN);
		*/

		log.info("--------------------------------------");
		log.info("Average arrival delay is: " + average);
		log.info("--------------------------------------");
	}

	private static void downloadData() {
		File targetDownload = new File(DOWNLOAD_TARGET);
		if (targetDownload.exists()) {
			log.info("Data already downloaded");
			return;
		}
		try {
			FileUtils.copyURLToFile(new URL(DATA_URL), new File(DOWNLOAD_TARGET));
			log.info("Data successfully downloaded");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<Flight> parseDataFromCsv() throws IOException, CompressorException {
		FileInputStream fin = new FileInputStream(DOWNLOAD_TARGET);
		BufferedInputStream bis = new BufferedInputStream(fin);
		CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
		List<Flight> flights = CsvUtil.read(Flight.class, input);
		log.info(flights.get(6).toString());
		log.info("Found flights: " + flights.size());
		return flights;
	}

	private static void insertFlightToDB(Flight f) {
		Transaction transaction = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			transaction = session.beginTransaction();
			session.save(f);
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			throw e;
		}
	}

	private static Double getAverageFromDB() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return (Double) session.createQuery("SELECT avg(arrDelay) FROM Flight WHERE NOT arrDelay='NA'").getSingleResult();
		}
	}
}
