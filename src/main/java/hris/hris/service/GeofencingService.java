package hris.hris.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeofencingService {

    private static final double EARTH_RADIUS = 6371000;

    public boolean isWithinGeofence(double userLat, double userLng,
                                   double officeLat, double officeLng,
                                   double radius) {
        try {
            double distance = calculateDistance(userLat, userLng, officeLat, officeLng);
            boolean isWithin = distance <= radius;

            log.debug("Distance from office: {} meters, Radius: {} meters, Within: {}",
                    distance, radius, isWithin);

            return isWithin;
        } catch (Exception e) {
            log.error("Error calculating geofence", e);
            return false;
        }
    }

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public String getAddressFromCoordinates(double latitude, double longitude) {
        return String.format("Lat: %.6f, Lng: %.6f", latitude, longitude);
    }

    public boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 &&
               longitude >= -180 && longitude <= 180;
    }

    public GeofenceResult checkGeofence(double userLat, double userLng,
                                       double officeLat, double officeLng,
                                       double radius) {
        if (!isValidCoordinate(userLat, userLng)) {
            return GeofenceResult.INVALID_COORDINATES;
        }

        double distance = calculateDistance(userLat, userLng, officeLat, officeLng);

        if (distance <= radius) {
            return GeofenceResult.WITHIN_GEOFENCE;
        } else {
            return GeofenceResult.OUTSIDE_GEOFENCE;
        }
    }

    public enum GeofenceResult {
        WITHIN_GEOFENCE,
        OUTSIDE_GEOFENCE,
        INVALID_COORDINATES
    }
}