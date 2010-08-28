package edu.iu.scipolicy.preprocessing.geocoder;

import org.osgi.service.component.ComponentContext;
import edu.iu.scipolicy.preprocessing.geocoder.coders.yahoo.YahooFamilyOfGeocoder;

/**
 * 
 * Geocoder factory for Yahoo Geocoding.
 * @author kongch
 *
 */
public class YahooGeocoderFactory extends AbstractGeocoderFactory {
	public static final String[] SUPPORTED_PLACE_TYPE = {   ADDRESS, 
															COUNTRY, 
															US_STATE, 
															US_ZIP_CODE};
	
	@Override
	protected void activate(ComponentContext componentContext) {
		this.setFamilyGeocoder(new YahooFamilyOfGeocoder());
	}

	@Override
	protected String[] getPlaceTypeOptionLabels() {
		return SUPPORTED_PLACE_TYPE;
	}

	@Override
	protected String[] getPlaceTypeOptionValues() {
		return SUPPORTED_PLACE_TYPE;
	}
}
