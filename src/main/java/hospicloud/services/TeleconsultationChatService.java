package hospicloud.services;

import hospicloud.dtos.TeleconsultationChatMessageDTO;
import hospicloud.dtos.TeleconsultationChatReadReceiptDTO;

import java.util.List;

public interface TeleconsultationChatService {

    List<TeleconsultationChatMessageDTO> listMessages(Integer idRdv);

    TeleconsultationChatMessageDTO sendMessage(Integer idRdv, String content);

    TeleconsultationChatReadReceiptDTO markMessagesAsRead(Integer idRdv);
}
