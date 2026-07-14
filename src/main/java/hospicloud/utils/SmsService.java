package hospicloud.utils;

public interface SmsService {

    boolean isEnabled();

    void envoyerSms(String numero, String message);
}
