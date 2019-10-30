package pl.akademiaspring.weather_check.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import pl.akademiaspring.weather_check.model.City;
import pl.akademiaspring.weather_check.model.CityData;
import pl.akademiaspring.weather_check.model.Icon;
import pl.akademiaspring.weather_check.model.WeatherData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WeatherApi {

    @GetMapping("/weather")
    public String getCity(Model model) {
        model.addAttribute("checkCity", new City());
        return "city";
    }

    public int[] getWoeid(String city) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.metaweather.com/api/location/search/?query=" + city;
        CityData[] cityData = restTemplate.getForObject(url, CityData[].class);

        int[] woeidData = new int[cityData.length];
        for (CityData cityDataObject : cityData) {
            for (int x = 0; x < woeidData.length; x++) {
                woeidData[x] = cityDataObject.getWoeid();
            }
        }
        return woeidData;
    }

    public List<WeatherData> getWeather(String city) {
        RestTemplate restTemplate = new RestTemplate();
        int[] woeidData = getWoeid(city);
        List<WeatherData> weatherDataList = null;
        for (int woeid : woeidData) {
            String url = "https://www.metaweather.com/api/location/" + woeid;
            JsonNode table = restTemplate.getForObject(url, JsonNode.class).get("consolidated_weather");
            ObjectMapper mapper = new ObjectMapper();

            try {
                weatherDataList = mapper.readValue(table.toString(), new TypeReference<List<WeatherData>>() {
                });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return weatherDataList;
    }

    @PostMapping("/getWeather")
    public ModelAndView displayWeather(@ModelAttribute City city) {
        ModelAndView mav = new ModelAndView();
        List<WeatherData> list = getWeather(city.getCity());
        List<WeatherData> actualWeatherList = getActualWeather(list);
        if (actualWeatherList != null) {
            mav.setViewName("weather");
            mav.addObject("weatherList", actualWeatherList);
            mav.addObject("city", city);
            mav.addObject("iconList", getIcon(getWeatherAbbrName(actualWeatherList)));
            return mav;
        } else {
            mav.setViewName("error");
            return mav;
        }
    }

    public List<WeatherData> getActualWeather(List<WeatherData> weatherDataList) {
        List<WeatherData> actualWeatherData = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.now();
        for (WeatherData weatherData : weatherDataList) {
            if (weatherData.getApplicableDate().equals(dtf.format(localDate))) {
                actualWeatherData.add(weatherData);
            }
        }
        return actualWeatherData;
    }

    public List<Icon> getIcon(List<String> weatherStateAbbrList) {
        List<Icon> iconList = new ArrayList<>();
        for (String weatherStateAbbr : weatherStateAbbrList) {
            String url = "https://www.metaweather.com/static/img/weather/png/" + weatherStateAbbr + ".png";
            Icon icon = new Icon(url);
            iconList.add(icon);
        }
        return iconList;
    }

    public List<String> getWeatherAbbrName(List<WeatherData> actualWeatherData) {
        List<String> weatherStateAbbrList = new ArrayList<>();
        for (WeatherData weatherData : actualWeatherData) {
            weatherStateAbbrList.add(weatherData.getWeatherStateAbbr());
        }
        return weatherStateAbbrList;
    }

}
