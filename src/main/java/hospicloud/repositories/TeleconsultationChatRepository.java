package hospicloud.repositories;

import hospicloud.dtos.TeleconsultationChatMessageDTO;

import java.util.List;

public interface TeleconsultationChatRepository {

    void ensureSchema();

    TeleconsultationChatMessageDTO save(TeleconsultationChatMessageDTO message);

    List<TeleconsultationChatMessageDTO> findByRdv(Integer idHopital, Integer idRdv);

    List<Long> markAsReadByRecipient(Integer idHopital, Integer idRdv, boolean readerIsDoctor);
}
