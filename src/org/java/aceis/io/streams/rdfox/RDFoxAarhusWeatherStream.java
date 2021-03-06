package org.java.aceis.io.streams.rdfox;

import com.csvreader.CsvReader;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import org.java.aceis.eventmodel.EventDeclaration;
import org.java.aceis.io.rdf.RDFFileManager;
import org.java.aceis.io.streams.DataWrapper;
import org.java.aceis.observations.SensorObservation;
import org.java.aceis.observations.WeatherObservation;
import org.java.aceis.utils.RDFox.RDFoxWrapper;
import org.java.citybench.main.CityBench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RDFoxAarhusWeatherStream extends RDFoxSensorStream implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	CsvReader streamData;
	EventDeclaration ed;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private Date startDate = null;
	private Date endDate = null;
	private Random random = new Random();

	public RDFoxAarhusWeatherStream(String uri, String txtFile, EventDeclaration ed) throws IOException {
		super(uri);
		streamData = new CsvReader(String.valueOf(txtFile));
		this.ed = ed;
		streamData.setTrimWhitespace(false);
		streamData.setDelimiter(',');
		streamData.readHeaders();
	}

	public RDFoxAarhusWeatherStream(String uri, String txtFile, EventDeclaration ed, Date start, Date end)
			throws IOException {
		super(uri);
		streamData = new CsvReader(String.valueOf(txtFile));
		this.ed = ed;
		streamData.setTrimWhitespace(false);
		streamData.setDelimiter(',');
		streamData.readHeaders();
		this.startDate = start;
		this.endDate = end;
	}

	@Override
	public void run() {
		logger.info("Starting sensor stream: " + this.getIRI());
		try {
			//logger.info("Hier sind wir");
			while (streamData.readRecord() && !stop) {
				//logger.info("Hier sind wir2");
				// logger.info("Reading: " + streamData.toString());
				Date obTime = sdf.parse(streamData.get("TIMESTAMP").toString());
				if (this.startDate != null && this.endDate != null) {
					if (obTime.before(this.startDate) || obTime.after(this.endDate)) {
						logger.debug(this.getIRI() + ": Disgarded observation @" + obTime);
						continue;
					}
				}
				// logger.info("Reading data: " + streamData.toString());
				if(random.nextDouble() < (1 - ((double)sleep/1000))) {
					RDFoxWrapper.getRDFoxWrapper().flushIfNecessary(getIRI());
				}
				WeatherObservation po = (WeatherObservation) this.createObservation(streamData);
				// logger.debug("Reading data: " + new Gson().toJson(po));
				List<Statement> stmts = this.getStatements(po);
				if(random.nextDouble() < (1 - ((double)sleep/1000))) {
					RDFoxWrapper.getRDFoxWrapper().flushIfNecessary(getIRI());
				}
				for (Statement st : stmts) {
					try {
						logger.debug(this.getIRI() + " Streaming: " + st.toString());
						//final RdfQuadruple q = new RdfQuadruple(st.getSubject().toString(), st.getPredicate()
						//		.toString(), st.getObject().toString(), System.currentTimeMillis());
						//logger.info(q.toString());
						RDFoxWrapper.getRDFoxWrapper().putData(getIRI(), st);

					} catch (Exception e) {
						e.printStackTrace();
						logger.error(this.getIRI() + " CSPARQL streamming error.");
					}
					// messageByte += st.toString().getBytes().length;
				}
				RDFoxWrapper.getRDFoxWrapper().flushIfNecessary(getIRI());
				CityBench.pm.addNumberOfStreamedStatements(stmts.size());
				try {
					if (this.getRate() == 1.0)
						Thread.sleep(sleep);
				} catch (Exception e) {

					e.printStackTrace();
					this.stop();
				}

			}
		} catch (Exception e) {
			logger.error("Unexpected thread termination");
			e.printStackTrace();
		} finally {
			logger.info("Stream Terminated: " + this.getIRI());
			this.stop();
		}

	}

	@Override
	protected List<Statement> getStatements(SensorObservation wo) throws NumberFormatException, IOException {
		Model m = ModelFactory.createDefaultModel();
		if (ed != null)
			for (String s : ed.getPayloads()) {
				Resource observation = m
						.createResource(RDFFileManager.defaultPrefix + wo.getObId() + UUID.randomUUID());
				// wo.setObId(observation.toString());
				CityBench.obMap.put(observation.toString(), wo);
				observation.addProperty(RDF.type, m.createResource(RDFFileManager.ssnPrefix + "Observation"));
				Resource serviceID = m.createResource(ed.getServiceId());
				observation.addProperty(m.createProperty(RDFFileManager.ssnPrefix + "observedBy"), serviceID);
				observation.addProperty(m.createProperty(RDFFileManager.ssnPrefix + "observedProperty"),
						m.createResource(s.split("\\|")[2]));
				Property hasValue = m.createProperty(RDFFileManager.saoPrefix + "hasValue");
				if (s.contains("Temperature"))
					observation.addLiteral(hasValue, ((WeatherObservation) wo).getTemperature());
				else if (s.toString().contains("Humidity"))
					observation.addLiteral(hasValue, ((WeatherObservation) wo).getHumidity());
				else if (s.toString().contains("WindSpeed"))
					observation.addLiteral(hasValue, ((WeatherObservation) wo).getWindSpeed());
			}
		return m.listStatements().toList();
	}

	@Override
	protected SensorObservation createObservation(Object data) {
		try {
			// CsvReader streamData = (CsvReader) data;
			int hum = Integer.parseInt(streamData.get("hum"));
			double tempm = Double.parseDouble(streamData.get("tempm"));
			double wspdm = Double.parseDouble(streamData.get("wspdm"));
			Date obTime = sdf.parse(streamData.get("TIMESTAMP"));
			WeatherObservation wo = new WeatherObservation(tempm, hum, wspdm, obTime);
			logger.debug(ed.getServiceId() + ": streaming record @" + wo.getObTimeStamp());
			wo.setObId("AarhusWeatherObservation-" + (int) Math.random() * 1000);
			// this.currentObservation = wo;
			DataWrapper.waitForInterval(currentObservation, wo, startDate, getRate());
			this.currentObservation = wo;
			return wo;
		} catch (NumberFormatException | IOException | ParseException e) {
			e.printStackTrace();
		}
		return null;

	}

}
