package org.miscbot.util;

public class Forecast {
    private String date;
    private Day day;
    private Location location;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public static class Day {
        private int maxtemp_c;
        private int mintemp_c;
        private int avgtemp_c;
        private int maxwind_kph;
        private int totalprecip_mm;
        private int daily_chance_of_rain;


        public int getMaxtemp_c() {
            return maxtemp_c;
        }

        public void setMaxtemp_c(int maxtemp_c) {
            this.maxtemp_c = maxtemp_c;
        }

        public int getMintemp_c() {
            return mintemp_c;
        }

        public void setMintemp_c(int mintemp_c) {
            this.mintemp_c = mintemp_c;
        }

        public int getAvgtemp_c() {
            return avgtemp_c;
        }

        public void setAvgtemp_c(int avgtemp_c) {
            this.avgtemp_c = avgtemp_c;
        }

        public int getMaxwind_kph() {
            return maxwind_kph;
        }

        public void setMaxwind_kph(int maxwind_kph) {
            this.maxwind_kph = maxwind_kph;
        }

        public int getTotalprecip_mm() {
            return totalprecip_mm;
        }

        public void setTotalprecip_mm(int totalprecip_mm) {
            this.totalprecip_mm = totalprecip_mm;
        }

        public int getDaily_chance_of_rain() {
            return daily_chance_of_rain;
        }

        public void setDaily_chance_of_rain(int daily_chance_of_rain) {
            this.daily_chance_of_rain = daily_chance_of_rain;
        }
    }
}
