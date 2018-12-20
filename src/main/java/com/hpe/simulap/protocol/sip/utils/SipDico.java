//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.simulap.sip.headers.Header;
import com.hpe.simulap.sip.headers.HeaderList;
import com.hpe.simulap.sip.headers.ObjectFactory;

public class SipDico {
	private static final Logger log = LoggerFactory.getLogger(SipDico.class);
	
	String getLocalClassLocation() {
		String localClassLocation = "";
		try {
			File jarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			String jarPath = jarFile.getParentFile().getPath();
			localClassLocation = URLDecoder.decode(jarPath, "UTF-8");
		} catch (URISyntaxException e) {
			log.error("URISyntaxException: {}", e);
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException: {}", e);
		} finally {
			return localClassLocation;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Header> loadDico() throws JAXBException, FileNotFoundException {
		
		List<Header> readList = null;
		SipDico dico = new SipDico();
		String localClassLocation = dico.getLocalClassLocation();
		String dictionaryPath = System.getProperty("simulap.sip.dictionary.path", localClassLocation + "/../../dictionaries");

		//1. We need to create JAXContext instance
		JAXBContext jaxbContext;
		JAXBElement<HeaderList> unmarshalledObject = null;
			jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			//2. Use JAXBContext instance to create the Unmarshaller.
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			//3. Use the Unmarshaller to unmarshal the XML document to get an instance of JAXBElement.
			String xmlFileName = dictionaryPath+"/SipHeadersGUIDictionary.xml";
			unmarshalledObject =
			(JAXBElement<HeaderList>)unmarshaller.unmarshal(
					new FileReader(xmlFileName ));
			//4. Get the instance of the required JAXB Root Class from the JAXBElement.
			HeaderList listHeadersObj = unmarshalledObject.getValue();
			//Obtaining all the required data from the JAXB Root class instance.
			readList = new ArrayList<Header>();
			for ( Header item : listHeadersObj.getHeaderelem()){
				readList.add(item);
			}

		return readList;
	}

}
