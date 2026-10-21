const DRIFT_NOTIFICATIONS_ENABLED_KEY = 'avanza.settings.drift-notifications-enabled';

export const areDriftNotificationsEnabled = () =>
  window.localStorage.getItem(DRIFT_NOTIFICATIONS_ENABLED_KEY) !== 'false';

export const setDriftNotificationsEnabled = (enabled: boolean) => {
  window.localStorage.setItem(DRIFT_NOTIFICATIONS_ENABLED_KEY, String(enabled));
};