package pl.akademiaspring.weather_check.model;

public class City {

    private String cityName;

    public City(String cityName) {
        this.cityName = cityName;
    }

    public City() {
    }

    public String getCity() {
        return cityName;
    }

    public void setCity(String cityName) {
        this.cityName = cityName;
    }
}
