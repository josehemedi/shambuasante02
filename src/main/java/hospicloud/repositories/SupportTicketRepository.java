package hospicloud.repositories;

import hospicloud.dtos.SupportTicketDTO;
import hospicloud.dtos.SupportTicketStatusUpdateDTO;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository {

    Long insert(Integer hopitalId, Integer createdByUserId, String createdByEmail, String createdByRole,
                String subject, String description, String module, String priority, String requestId);

    List<SupportTicketDTO> search(Integer hopitalId, String status, String module, String priority,
                                  String requestId, String search, int limit);

    Optional<SupportTicketDTO> findById(Long id);

    boolean updateStatus(Long id, SupportTicketStatusUpdateDTO update);

    long countOpenTickets();
}
