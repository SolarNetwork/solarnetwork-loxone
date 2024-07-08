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

	/**
	 * Constructor.
	 *
	 * @param ts
	 *        the time stamp
	 * @param weatherType
	 *        the weather type
	 * @param windDirection
	 *        the wind direction
	 * @param solarRadiation
	 *        the solar radiation
	 * @param relativeHumidity
	 *        the humidity
	 * @param temperature
	 *        the temperature
	 * @param perceivedTemperature
	 *        the perceived temperature
	 * @param dewPoint
	 *        the dew point
	 * @param precipitation
	 *        the precipitation
	 * @param windSpeed
	 *        the wind speed
	 * @param barometricPressure
	 *        the barometric pressure
	 */
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
	 * Get the weather type.
	 *
	 * @return the type
	 */
	public int getWeatherType() {
		return weatherType;
	}

	/**
	 * Get the wind direction.
	 *
	 * @return the wind direction
	 */
	public int getWindDirection() {
		return windDirection;
	}

	/**
	 * Get the solar radiation.
	 *
	 * @return the solar radiation
	 */
	public int getSolarRadiation() {
		return solarRadiation;
	}

	/**
	 * Get the humidity.
	 *
	 * @return the relative humidity
	 */
	public int getRelativeHumidity() {
		return relativeHumidity;
	}

	/**
	 * Get the temperature.
	 *
	 * @return the temperature
	 */
	public double getTemperature() {
		return temperature;
	}

	/**
	 * Get the perceived temperature.
	 *
	 * @return the perceivedTemperature
	 */
	public double getPerceivedTemperature() {
		return perceivedTemperature;
	}

	/**
	 * Get the dew point.
	 *
	 * @return the dew point
	 */
	public double getDewPoint() {
		return dewPoint;
	}

	/**
	 * Get the precipitation.
	 *
	 * @return the precipitation
	 */
	public double getPrecipitation() {
		return precipitation;
	}

	/**
	 * Get the wind speed.
	 *
	 * @return the windSpeed
	 */
	public double getWindSpeed() {
		return windSpeed;
	}

	/**
	 * Get the barometric pressure.
	 *
	 * @return the barometric pressure
	 */
	public double getBarometricPressure() {
		return barometricPressure;
	}

}
