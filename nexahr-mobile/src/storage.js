import AsyncStorage from '@react-native-async-storage/async-storage';

let SecureStore;
try {
  SecureStore = require('expo-secure-store');
} catch {
  SecureStore = null;
}

const canUseSecureStore = SecureStore && SecureStore.setItemAsync;

export const storage = {
  getItem: async (key) => {
    if (canUseSecureStore) {
      try {
        return await SecureStore.getItemAsync(key);
      } catch {
        return AsyncStorage.getItem(key);
      }
    }
    return AsyncStorage.getItem(key);
  },
  setItem: async (key, value) => {
    if (canUseSecureStore) {
      try {
        await SecureStore.setItemAsync(key, value);
        return;
      } catch {
        /* fallback */
      }
    }
    await AsyncStorage.setItem(key, value);
  },
  removeItem: async (key) => {
    if (canUseSecureStore) {
      try {
        await SecureStore.deleteItemAsync(key);
      } catch {
        /* fallback */
      }
    }
    await AsyncStorage.removeItem(key);
  },
  multiRemove: async (keys) => {
    await Promise.all(keys.map((key) => storage.removeItem(key)));
  },
};
