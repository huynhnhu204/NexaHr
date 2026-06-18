const EARTH_RADIUS_METERS = 6_371_000;

export function calculateDistanceMeters(lat1, lon1, lat2, lon2) {
  const lat1Rad = (lat1 * Math.PI) / 180;
  const lat2Rad = (lat2 * Math.PI) / 180;
  const deltaLat = ((lat2 - lat1) * Math.PI) / 180;
  const deltaLon = ((lon2 - lon1) * Math.PI) / 180;

  const a = Math.sin(deltaLat / 2) ** 2
    + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(deltaLon / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return EARTH_RADIUS_METERS * c;
}

export function formatDistance(meters) {
  if (meters == null || Number.isNaN(meters)) return '—';
  if (meters < 1000) return `${Math.round(meters)} m`;
  return `${(meters / 1000).toFixed(1)} km`;
}

export function formatCoords(lat, lon) {
  if (lat == null || lon == null) return '—';
  return `${lat.toFixed(6)}, ${lon.toFixed(6)}`;
}

export function googleMapsUrl(lat, lon) {
  return `https://www.google.com/maps?q=${lat},${lon}`;
}

export function getCurrentPosition() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Trình duyệt không hỗ trợ định vị.'));
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve({
        latitude: pos.coords.latitude,
        longitude: pos.coords.longitude,
        accuracy: pos.coords.accuracy,
      }),
      (err) => {
        if (err.code === 1) {
          reject(new Error('Vui lòng cho phép trình duyệt truy cập vị trí.'));
        } else if (err.code === 3) {
          reject(new Error('Hết thời gian chờ lấy vị trí. Vui lòng thử lại.'));
        } else {
          reject(new Error('Không lấy được vị trí.'));
        }
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 },
    );
  });
}
