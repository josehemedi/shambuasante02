package hospicloud.services;

import hospicloud.dtos.SignatureConsultationResponseDTO;
import hospicloud.dtos.SignerConsultationRequestDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface ConsultationSignatureService {

    SignatureConsultationResponseDTO signerConsultation(
            Long consultationId,
            SignerConsultationRequestDTO request,
            HttpServletRequest httpRequest);
}
