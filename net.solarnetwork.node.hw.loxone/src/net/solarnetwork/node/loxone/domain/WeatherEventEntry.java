/* ==================================================================
 * WeatherEventEntry.java - 15/06/2017 2:39:57 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.loxone.domain;

/**
 * An entry in a weather event.
 * 
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public class WeatherEventEntry {

	/** The Loxone weather epoch: midnight Jan 1, 2009 UTC. */
	public static final long WEATHER_EPOCH = 1230768000000L;

	private final long timestamp;
	private final int weatherType;
	private final int windDirection;
	private final int solarRadiation;
	private final int relativeHumidity;
	private final double temperature;
	private final double perceivedTemperature;
	private final double dewPoint;
	private final double precipitation;
	private final double windSpeed;
	private final double barometricPressure;

	public WeatherEventEntry(int ts, int weatherType, int windDirection, int solarRadiation,
			int relativeHumidity, double temperature, double perceivedTemperature, double dewPoint,
			double precipitation, double windSpeed, double barometricPressure) {
		super();
		this.timestamp = WEATHER_EPOCH + ts;
		this.weatherType = weatherType;
		this.windDirection = windDirection;
		this.solarRadiation = solarRadiation;
		this.relativeHumidity = relativeHumidity;
		this.temperature = temperature;
		this.perceivedTemperature = perceivedTemperature;
		this.dewPoint = dewPoint;
		this.precipitation = precipitation;
		this.windSpeed = windSpeed;
		this.barometricPressure = barometricPressure;
	}

	/**
	 * Get the date of the information, in milliseconds since the Java epoch.
	 * 
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the weatherType
	 */
	public int getWeatherType() {
		return weatherType;
	}

	/**
	 * @return the windDirection
	 */
	public int getWindDirection() {
		return windDirection;
	}

	/**
	 * @return the solarRadiation
	 */
	public int getSolarRadiation() {
		return solarRadiation;
	}

	/**
	 * @return the relativeHumidity
	 */
	public int getRelativeHumidity() {
		return relativeHumidity;
	}

	/**
	 * @return the temperature
	 */
	public double getTemperature() {
		return temperature;
	}

	/**
	 * @return the perceivedTemperature
	 */
	public double getPerceivedTemperature() {
		return perceivedTemperature;
	}

	/**
	 * @return the dewPoint
	 */
	public double getDewPoint() {
		return dewPoint;
	}

	/**
	 * @return the precipitation
	 */
	public double getPrecipitation() {
		return precipitation;
	}

	/**
	 * @return the windSpeed
	 */
	public double getWindSpeed() {
		return windSpeed;
	}

	/**
	 * @return the barometricPressure
	 */
	public double getBarometricPressure() {
		return barometricPressure;
	}

}
