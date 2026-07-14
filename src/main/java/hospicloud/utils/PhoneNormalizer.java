package hospicloud.utils;

public final class PhoneNormalizer {

    private PhoneNormalizer() {
    }

    public static String normalize(String raw, String defaultCountryCode) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String trimmed = raw.trim();
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (c == '+' && digits.isEmpty()) {
                digits.append(c);
            }
        }

        if (digits.isEmpty()) {
            return null;
        }

        String normalized = digits.toString();
        if (normalized.startsWith("+")) {
            return normalized.length() >= 8 ? normalized : null;
        }
        if (normalized.startsWith("00") && normalized.length() > 2) {
            return "+" + normalized.substring(2);
        }

        String countryCode = defaultCountryCode == null || defaultCountryCode.isBlank()
                ? "+224"
                : defaultCountryCode.trim();
        if (!countryCode.startsWith("+")) {
            countryCode = "+" + countryCode;
        }

        if (normalized.startsWith("0")) {
            normalized = normalized.substring(1);
        }

        return countryCode + normalized;
    }
}
