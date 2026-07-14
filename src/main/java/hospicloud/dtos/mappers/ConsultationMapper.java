package hospicloud.dtos.mappers;

import hospicloud.dtos.ConsultationDto;
import hospicloud.model.Consultation;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface ConsultationMapper {
    ConsultationDto toDto(Consultation entity);
    Consultation toEntity(ConsultationDto dto);
}