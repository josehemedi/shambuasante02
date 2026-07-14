package hospicloud.services;

import hospicloud.dtos.SupportTicketCreateDTO;
import hospicloud.dtos.SupportTicketDTO;
import hospicloud.dtos.SupportTicketStatusUpdateDTO;

import java.util.List;

public interface SupportTicketService {

    SupportTicketDTO create(SupportTicketCreateDTO request);

    List<SupportTicketDTO> search(Integer hopitalId, String status, String module,
                                  String priority, String requestId, String search, int limit);

    SupportTicketDTO updateStatus(Long id, SupportTicketStatusUpdateDTO update);
}
