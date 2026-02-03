// currency options: flagCode, long name, short name (code), alphabetically by country/currency name
export const CURRENCIES = [
  { code: 'AUD', name: 'Australian Dollar', flagCode: 'au' },
  { code: 'GBP', name: 'British Pound Sterling', flagCode: 'gb' },
  { code: 'KHR', name: 'Cambodian Riel', flagCode: 'kh' },
  { code: 'EUR', name: 'Euro', flagCode: 'eu' },
  { code: 'INR', name: 'Indian Rupee', flagCode: 'in' },
  { code: 'JPY', name: 'Japanese Yen', flagCode: 'jp' },
  { code: 'LAK', name: 'Lao Kip', flagCode: 'la' },
  { code: 'MYR', name: 'Malaysian Ringgit', flagCode: 'my' },
  { code: 'MMK', name: 'Myanmar Kyat', flagCode: 'mm' },
  { code: 'SGD', name: 'Singapore Dollar', flagCode: 'sg' },
  { code: 'KRW', name: 'South Korean Won', flagCode: 'kr' },
  { code: 'THB', name: 'Thai Baht', flagCode: 'th' },
  { code: 'AED', name: 'UAE Dirham', flagCode: 'ae' },
  { code: 'USD', name: 'US Dollar', flagCode: 'us' },
  { code: 'VND', name: 'Vietnamese Dong', flagCode: 'vn' },
]

// flag images from flagcdn.com (2-letter ISO country/region code)
const FLAG_BASE = 'https://flagcdn.com/w40'
export const getFlagUrl = (flagCode) => `${FLAG_BASE}/${flagCode}.png`